#ifndef DUMP_ADD_SEQUENCE_HPP
#define DUMP_ADD_SEQUENCE_HPP

#include "dumpfile.hpp"
#include <mysql++.h>
#include <pqxx/pqxx>

/**
 * dumps data adding a sequence number which resets for each 
 * unique ID. this means that input data should be sorted by ID.
 */
class dump_add_sequence {
public:
  dump_add_sequence(pqxx::tablewriter &);
  void operator()(const mysqlpp::Row &);
private:
  pqxx::tablewriter &writer;
  long int last_id;
  short int sequence;
};

#endif /* DUMP_ADD_SEQUENCE_HPP */
