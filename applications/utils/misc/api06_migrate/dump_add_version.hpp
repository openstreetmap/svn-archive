#ifndef DUMP_ADD_VERSION_HPP
#define DUMP_ADD_VERSION_HPP

#include "block_vector.hpp"
#include <mysql++.h>
#include <pqxx/pqxx>

/**
 * Dumps input to the writer, adding a version number to objects
 * based on their timestamp field. This is basically non-reusable,
 * as it only works for the nodes table :-(
 */
class dump_add_version {
public:
  dump_add_version(pqxx::tablewriter &);
  void operator()(const mysqlpp::Row &);

  static void write(pqxx::tablewriter &,
		    const mysqlpp::Row &,
		    short int);

private:
  pqxx::tablewriter &writer;

  // counter used for verbose logging
  size_t count;

  // sparse vector for accumulating version numbers
  block_vector<short int> versions;
};

#endif /* DUMP_ADD_VERSION_HPP */
