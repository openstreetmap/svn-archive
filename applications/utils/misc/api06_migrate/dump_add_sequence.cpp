#include "dump_add_sequence.hpp"
extern "C" {
#include <ctype.h>
}

using namespace std;

dump_add_sequence::dump_add_sequence(pqxx::tablewriter &w)
  : writer(w), last_id(-1), sequence(0) {
}

/**
 * Note: this isn't a general-purpose capitalise function, as i
 * know there are only 3 possible inputs: "node","way" or "relation".
 */
static string capitalise(const std::string &s) {
  string rv(s);
  rv[0] = toupper(rv[0]);
  return rv;
}

void 
dump_add_sequence::operator()(const mysqlpp::Row &r) {
  long int id = r["id"];

  if (id != last_id) {
    last_id = id;
    sequence = 0;
  }

  vector<string> row;
  row.reserve(r.size() + 1);
  // postgres is being *really* annoying with its case-sensitive
  // enumerations, so a little adjustment is needed here...
  row.push_back(r["id"].c_str());
  row.push_back(r["member_id"].c_str());
  row.push_back(r["member_role"].c_str());
  row.push_back(r["version"].c_str());
  row.push_back(capitalise(r["member_type"].c_str()));
  ostringstream ostr;
  ostr << sequence;
  row.push_back(ostr.str());
  writer.push_back(row);
  ++sequence;
}


