#ifndef DUMP_ADD_VERSION_TAGS_HPP
#define DUMP_ADD_VERSION_TAGS_HPP

#include "block_vector.hpp"
#include <mysql++.h>
#include <pqxx/pqxx>
#include <iconv.h>
#include <boost/shared_ptr.hpp>

/**
 * Dumps id, version and tag to writer after separating them
 * from the "tags" column of the input row. Also, makes the
 * tags unique w.r.t. case and trailing whitespace so that
 * MySQL will accept them in the primary key.
 */
class dump_add_version_tags {
public:
  explicit dump_add_version_tags(pqxx::tablewriter &);
  dump_add_version_tags(const dump_add_version_tags &);
  ~dump_add_version_tags();
  void operator()(const mysqlpp::Row &);

  static void write(pqxx::tablewriter &, const mysqlpp::Row &, 
		    short int, iconv_t &);
private:
  pqxx::tablewriter &writer;
  size_t count;
  block_vector<short int> versions;
  iconv_t ct;
};

#endif /* DUMP_ADD_VERSION_TAGS_HPP */
