#include "dump_uniq_tag.hpp"
#include "globals.hpp"
#include "split_tags.hpp"
#include <boost/algorithm/string.hpp>
#include <boost/regex.hpp>

using namespace std;
namespace al = boost::algorithm;

dump_uniq_tag::dump_uniq_tag(pqxx::tablewriter &w) 
  : writer(w), key_counts(), last_id(0), last_version(-1) {
  int flag = 1;
  ct = iconv_open("UTF-8//IGNORE", "UTF-8");
  iconvctl(ct, ICONV_SET_DISCARD_ILSEQ, &flag);
}

dump_uniq_tag::dump_uniq_tag(const dump_uniq_tag &o) 
  : writer(o.writer), key_counts(o.key_counts), 
    last_id(o.last_id), last_version(o.last_version) {
  int flag = 1;
  ct = iconv_open("UTF-8//IGNORE", "UTF-8");
  iconvctl(ct, ICONV_SET_DISCARD_ILSEQ, &flag);
}

dump_uniq_tag::~dump_uniq_tag() {
  iconv_close(ct);
}

static string
sanitise_utf8(iconv_t &ct, const string &s, const size_t id) {
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

void
dump_uniq_tag::operator()(const mysqlpp::Row &r) {
  const size_t id = r["id"];
  const int version = r["version"];
  const string k(r["k"]);
  vector<string> tag;
  string value = r["v"].is_null() ? 
    "<null>" : 
    sanitise_utf8(ct, string(r["v"].data()), id);

  tag.push_back(r["id"].c_str());

  // MySQL decided that trailing whitespace and case isn't 
  // significant for key comparisons, so we have to ensure that
  // no duplicates are entered in this case.
  string trimmed_key = al::trim_right_copy(k);
  al::to_lower(trimmed_key);
    
  // input will be sorted by id, version.
  if ((id != last_id) || (version != last_version)) {
    key_counts.clear();
    last_id = id;
    last_version = version;

    tag.push_back(k);
    tag.push_back(value);
    tag.push_back(r["version"].c_str());
    
    // don't forget to count *this* key as well :-)
    key_counts[trimmed_key] = 1;

  } else {
    if (key_counts.count(trimmed_key) > 0) {
      int tag_num = key_counts[trimmed_key];
      while (true) {
	ostringstream ostr;
	ostr << trimmed_key << "_" << tag_num;
	if (key_counts[ostr.str()] == 0)
	  break;
	tag_num++;
      }
      ostringstream ostr;
      ostr << trimmed_key << "_" << tag_num;
      tag.push_back(ostr.str());
      key_counts[trimmed_key] = tag_num;
      key_counts[ostr.str()] = 1;

    } else {
      key_counts[trimmed_key] = 1;
      tag.push_back(k);
    }
    tag.push_back(value);
    tag.push_back(r["version"].c_str());
  }

  writer.push_back(tag.begin(), tag.end());
}
