#include "dump_row.hpp"
#include <iostream>
#include <pqxx/pqxx>
#include <iconv.h>
#include <vector>
#include <sstream>

using namespace std;

static string
sanitise_utf8(iconv_t &ct, const string &s) {
  // it turns out there's quite a lot of tags in the 0.5 db which are
  // *very* long, so we need to keep a maximum of them here.
  // e.g: node 102399517
  static const unsigned int buf_size = 65536;
  static char buffer[buf_size];

  char *outbuf = buffer;
  const char *inbuf = s.c_str();
  size_t outsize = buf_size;
  size_t insize = s.length()+1;

  size_t rv = iconv(ct, (char **)&inbuf, &insize, &outbuf, &outsize);
  if (rv == ((size_t)(-1))) {
    if (errno != EINVAL) {
      ostringstream ostr;
      ostr << "FSCKED UTF-8: " << errno <<  " \"" << s << "\".";
      throw std::runtime_error(ostr.str());
    }
  }

  return outsize >= buf_size ?
    string("") :
    string(buffer, buf_size - outsize - 1);
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
	return string(s.begin(), itr);
      }
    }
  }
  // s is clearly smaller than c UTF-8 chars...
  return s;
}

dump_row::dump_row(pqxx::tablewriter &w) 
  : writer(w) { 
  int flag = 1;
  ct = iconv_open("UTF-8//IGNORE", "UTF-8");
  iconvctl(ct, ICONV_SET_DISCARD_ILSEQ, &flag);
}

dump_row::dump_row(const dump_row &d) 
  : writer(d.writer) { 
  int flag = 1;
  ct = iconv_open("UTF-8//IGNORE", "UTF-8");
  iconvctl(ct, ICONV_SET_DISCARD_ILSEQ, &flag);
}

dump_row::~dump_row() {
  iconv_close(ct);
}

static const char byte_order_mark[] = { 0xfe, 0xff, 0x00 };
static string null_str = byte_order_mark;

void
dump_row::operator()(const mysqlpp::Row &r) {
  vector<string> row;
  row.reserve(r.size());
  // clucking stupid mysqlpp::String + std::string constructors...
  for (mysqlpp::Row::const_iterator itr = r.begin();
       itr != r.end(); ++itr) {
    if (itr->is_null()) {
      row.push_back(null_str);
    } else {
      try {
	const string real_utf8 = sanitise_utf8(ct, (*itr).c_str());
	const size_t real_len = strlen_utf8(real_utf8.c_str());
	if (real_len > 255) {
	  row.push_back(truncate_utf8(real_utf8, 255));
	} else {
	  row.push_back(real_utf8);
	}
      } catch(...) {
	for (mysqlpp::Row::const_iterator jtr = r.begin();
	     jtr != r.end(); ++jtr) {
	  const mysqlpp::String &str = *jtr;
	  for (mysqlpp::String::const_iterator ktr = str.begin();
	       ktr != str.end(); ++ktr) {
	    unsigned char c = *ktr;
	    cout << std::hex << int(c >> 4)
		 << std::hex << int(c & 7)
		 << " ";
	  }
	  cout << "\t";
	}
	cout << endl;
	throw;
      }
    }
  }
  //std::copy(row.begin(), row.end(), ostream_iterator<string>(cout, "\t"));
  //cout << endl;
  writer.push_back(row);
}
