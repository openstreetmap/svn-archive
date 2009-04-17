#include <string>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cerrno>
#include <iconv.h>

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

void test(const string &s) {
  int flag = 1;
  iconv_t ct = iconv_open("UTF-8//IGNORE", "UTF-8");
  iconvctl(ct, ICONV_SET_DISCARD_ILSEQ, &flag);

  cout << "INPUT:  `" << s << "'" << endl;
  cout << "OUTPUT: `" << sanitise_utf8(ct, s) << "'" << endl;

  iconv_close(ct);
}

static char inv1[] = { 'f', 'o', 'o', ' ', 'b', 'a', 'r', 0xc0, 0x00 };
static char inv2[] = { 'f', 'o', 'o', 0xc0, 'b', 'a', 'r', 0x00 };
static char inv3[] = { 'f', 'o', 'o', 0xc1, 'b', 'a', 'r', 0xc0, 0x00 };
static char inv4[] = { 0xc3, 0xad, 0xc3, 0xae, 0xc3, 0xb1, 0xc3, 0xae, 
		       0xc3, 0xa2, 0xc3, 0xae, 0x20, 0x2d, 0x20, 0xc3, 
		       0xb2, 0xc3, 0xa0, 0xc3, 0xa3, 0xc3, 0xa0, 0xc3, 
		       0xad, 0xc3, 0xb0, 0xc3, 0xae, 0xc3, 0xa3, 0x00 };

static char inv5[] = { 0xE6, 0x00 };
static char inv6[] = { 0xF8, 0x00 };
static char inv7[] = { 0xB0, 0x00 };

int
main() {
  try {
    test("foobar foobar foobar");
    test(inv1);
    test(inv2);
    test(inv3);
    test(inv4);
    test(inv5);
    test(inv6);
    test(inv7);

  } catch (const exception &e) {
    cerr << "ERROR: " << e.what() << endl;
    return 1;
  }
  return 0;
}
