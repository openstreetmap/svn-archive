#include "dumpfile.hpp"

#include <stdexcept>

extern "C" {
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>
}

using namespace std;

mode_t getumask() {
  mode_t mask = umask(0);
  umask(mask);
  return ~mask;
}

dumpfile::dumpfile(const string &n)
  : path(n) {
  // try removing first, just in case. but ignore any error.
  remove(path.c_str());

  mode_t mask = 0644; //getumask();
  if (mkfifo(path.c_str(), mask) != 0) {
    throw runtime_error("ARGH"); //strerror(errno));
  }
};

dumpfile::~dumpfile() {
  if (fh.is_open()) {
    //fh.exceptions(ofstream::goodbit);
    fh.close();
  }
  remove(path.c_str());
}

void
dumpfile::open() {
  try {
    //cout << ">>> setting exceptions..." << endl;
    //fh.exceptions(ofstream::failbit | ofstream::badbit);
    //cout << ">>> opening file..." << endl;
    fh.open(path.c_str(), ios::out | ios::binary);
  } catch (const ios_base::failure &e) {
    remove(path.c_str());
    throw e;
  }
}

void
dumpfile::close() {
  fh.exceptions(ofstream::goodbit);
  fh.flush();
  fh.close();
}

