#include "dump_add_version_tags.hpp"
#include "globals.hpp"
#include "split_tags.hpp"
#include <boost/algorithm/string.hpp>
#include <boost/regex.hpp>
#include <iconv.h>

#include <errno.h>

using namespace std;
namespace al = boost::algorithm;

dump_add_version_tags::dump_add_version_tags(pqxx::tablewriter &w) 
  : writer(w), count(0), versions(1 << 29) {
  // NOTE: change the constant above if the node number 
  // goes above 536 million!
  int flag = 1;
  ct = iconv_open("UTF-8//IGNORE", "UTF-8");
  iconvctl(ct, ICONV_SET_DISCARD_ILSEQ, &flag);
}

dump_add_version_tags::dump_add_version_tags(const dump_add_version_tags &o)
  : writer(o.writer), count(o.count), versions(o.versions) {
  int flag = 1;
  ct = iconv_open("UTF-8//IGNORE", "UTF-8");
  iconvctl(ct, ICONV_SET_DISCARD_ILSEQ, &flag);
}

dump_add_version_tags::~dump_add_version_tags() {
  iconv_close(ct);
}

void
dump_add_version_tags::operator()(const mysqlpp::Row &r) {
  // lookup the id and version
  size_t id = r["id"];
  short int version = ++versions[id];

  // do the actual write
  write(writer, r, version, ct);

  if (verbose) {
    ++count;
    if ((count % 100000) == 0) {
      cout << "row count: " << count / 1000 << "k (" << r["timestamp"] << ")" << endl;
    }
  }
}

static string
sanitise_utf8(iconv_t &ct, const string &s, const char *id) {
  // it turns out there's quite a lot of tags in the 0.5 db which are
  // *very* long, so we need to keep a maximum of them here.
  // e.g: node 102399517
  static const unsigned int buf_size = 65536;
  static char buffer[buf_size];

  char *outbuf = buffer;
  const char *inbuf = s.c_str();
  size_t outsize = buf_size;
  size_t insize = s.length()+1;

  if (iconv(ct, (char **)&inbuf, &insize, &outbuf, &outsize) == 
      ((size_t)(-1))) {
    ostringstream ostr;
    ostr << "FSCKED UTF-8: " << errno << " when encoding ID="
	 << id << " \"" << s << "\".";
    throw std::runtime_error(ostr.str());
  }

  return string(buffer);
}

static string to_hex(const string &s) {
  ostringstream out;
  for (string::const_iterator itr = s.begin(); itr != s.end(); ++itr) {
    unsigned char c = *itr;
    out << std::hex << int(c >> 4)
	<< std::hex << int(c & 7);
  }
  return out.str();
}

/**
 * Kragen's UTF-8 character counting code. It isn't the fastest, but its
 * good enough for <255 char strings, which is all we should be getting
 * most of the time.
 *
 * I might have brought in a whole new library to do this sort of thing,
 * but it seemed a little unnecessary, given that this code is so simple.
 */
static size_t strlen_utf8(const string &s) {
  size_t j = 0;
  for (string::const_iterator itr = s.begin(); itr != s.end(); ++itr) {
    if ((*itr & 0xC0) != 0x80)
      j++;
  }
  return (j);
}

/**
 * Truncate a string after a certain number of UTF-8 chars, reusing
 * a lot of the logic from the above function.
 */
static string truncate_utf8(const string &s, size_t c) {
  size_t j = 0;
  for (string::const_iterator itr = s.begin(); itr != s.end(); ++itr) {
    if ((*itr & 0xC0) != 0x80) {
      j++;
      if (j == c) {
	// early return when the end is found. ugly, i know.
	return string(s.begin(), itr);;
      }
    }
  }
  // s is clearly smaller than c UTF-8 chars...
  return s;
}

void 
dump_add_version_tags::write(pqxx::tablewriter &writer,
			     const mysqlpp::Row &r,
			     short int version,
			     iconv_t &ct) {
  // print the tags to the tags stream
  const string tag_str = r["tags"].c_str();
  if (tag_str.size() > 0) {
    vector<string> tags = tags_split(tag_str);
    if (tags.size() % 2 != 0) {
      ostringstream ostr;
      ostr << "error converting \"" << tag_str << "\" into tags: (";
      for (vector<string>::iterator itr = tags.begin();
	   itr != tags.end(); ++itr) {
	ostr << "\"" << *itr << "\" ";
      }
      ostr << ").";
      throw runtime_error(ostr.str());
    }

    // if we alter any tags then set this boolean and add another
    // tag to say that we altered it. we don't count UTF-8 fixups
    // as "alterations", just truncations.
    bool altered = false;
    
    ostringstream ver;
    ver << version;
    const string ver_str = ver.str();

    // for uniquifying the tag keys we add numbers on duplicates.
    // this probably isn't the best way of doing it, but it'll work.
    map<string,int> key_counts;
    for (vector<string>::iterator itr = tags.begin();
	 itr != tags.end(); itr += 2) {
      vector<string> tag;
      tag.push_back(r["id"].c_str());
      tag.push_back(ver_str);
      // SQL92 specifies that trailing whitespace and case isn't 
      // significant for key comparisons, so we have to ensure that
      // no duplicates are entered in this case.
      string trimmed_key = al::trim_right_copy(*itr);
      al::to_lower(trimmed_key);
      if (key_counts.count(trimmed_key) > 0) {
	ostringstream ostr;
	ostr << trimmed_key << "_" << key_counts[trimmed_key]++;
	tag.push_back(ostr.str());
      } else {
	key_counts[trimmed_key] = 1;
	tag.push_back((*itr).c_str());
      }
      const string value_utf8 = sanitise_utf8(ct, *(itr+1), r["id"].c_str());
      const size_t value_len = strlen_utf8(value_utf8.c_str());
      if (value_len > 255) {
	cout << "WARNING: truncating key '" << *itr << "' on node "
	     << r["id"] << " from " << value_len 
	     << " to 255 UTF-8 chars." << endl;
	tag.push_back(truncate_utf8(value_utf8, 255));
	altered = true;
      } else {
	tag.push_back(value_utf8);
      }
      writer.push_back(tag.begin(), tag.end());
    }

    // if we altered anything then add another tag - lets say a 
    // fixme - so that someone can come back and manually check it.
    if (altered) {
      vector<string> tag;
      tag.push_back(r["id"].c_str());
      tag.push_back(ver_str);
      if (key_counts.count("fixme") > 0) {
	ostringstream ostr;
	ostr << "fixme_" << key_counts["fixme"]++;
	tag.push_back(ostr.str());
      } else {
	tag.push_back("fixme");
      }
      tag.push_back("API 0.6 migration altered tags on this element "
		    "for schema compatibility. Manual check recommended.");
      writer.push_back(tag.begin(), tag.end());
    }
  }
}
