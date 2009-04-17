#ifndef DUMP_UNIQ_TAG_HPP
#define DUMP_UNIQ_TAG_HPP

#include "dumpfile.hpp"
#include <mysql++.h>
#include <pqxx/pqxx>
#include <map>
#include <string>
#include <iconv.h>

/**
 * dumps tags to the FIFO after making them unique according to
 * mysql's definition of unique...
 */
class dump_uniq_tag {
public:
  dump_uniq_tag(pqxx::tablewriter &);
  dump_uniq_tag(const dump_uniq_tag &);
  ~dump_uniq_tag();
  void operator()(const mysqlpp::Row &);
private:
  pqxx::tablewriter &writer;

  // for uniquifying the tag keys we add numbers on duplicates.
  // this probably isn't the best way of doing it, but it'll work.
  std::map<std::string,int> key_counts;

  // for keeping track of state during table traverse
  size_t last_id;
  int last_version;

  // iconv thing for char conversion
  iconv_t ct;
};

#endif /* DUMP_UNIQ_TAG_HPP */
