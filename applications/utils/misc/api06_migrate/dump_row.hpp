#ifndef DUMP_ROW_HPP
#define DUMP_ROW_HPP

#include <mysql++.h>
#include <pqxx/pqxx>
#include <string>
#include <iconv.h>

/**
 * Functor which dumps any input it gets straight into the
 * postgres table writer.
 */
class dump_row {
public:
  explicit dump_row(pqxx::tablewriter &);
  dump_row(const dump_row &);
  ~dump_row();
  void operator()(const mysqlpp::Row &);
  void commit();
private:
  pqxx::tablewriter &writer;
  iconv_t ct;
};

#endif /* DUMP_ROW_HPP */
