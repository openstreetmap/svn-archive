#ifndef DUMP_ADD_VERSION_WR_HPP
#define DUMP_ADD_VERSION_WR_HPP

#include "block_vector.hpp"
#include "dumpfile.hpp"
#include <mysql++.h>

/**
 * Dumps input to the FIFO, adding a version number to objects
 * based on their timestamp field. This is basically non-reusable,
 * as it only works for the ways and relations tables :-(
 */
class dump_add_version_wr {
public:
  dump_add_version_wr(dumpfile &);
  //void operator()(const mysqlpp::Row &);

  static void write(dumpfile &,
		    const mysqlpp::Row &,
		    short int);
private:
  dumpfile &fh;

  // counter used for verbose logging
  //size_t count;

  // sparse vector for accumulating version numbers
  //block_vector<short int> versions;
};

#endif /* DUMP_ADD_VERSION_HPP */
