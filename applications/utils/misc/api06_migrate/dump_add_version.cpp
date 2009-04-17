#include "dump_add_version.hpp"
#include "globals.hpp"
#include <sstream>
#include <vector>

using namespace std;

dump_add_version::dump_add_version(pqxx::tablewriter &w) 
  : writer(w), count(0), versions(1 << 29) {
  // NOTE: change the constant above if the node number 
  // goes above 536 million!
}

void
dump_add_version::operator()(const mysqlpp::Row &r) {
  // lookup the id and version
  size_t id = r["id"];
  short int version = ++versions[id];

  write(writer, r, version);

  if (verbose) {
    ++count;
    if ((count % 100000) == 0) {
      cout << "row count: " << count / 1000 << "k (" << r["timestamp"] << ")" << endl;
    }
  }
}

void
dump_add_version::write(pqxx::tablewriter &w,
			const mysqlpp::Row &r,
			short int version) {
  vector<string> row;
  row.reserve(r.size() + 1);
  // clucking stupid mysqlpp::String + std::string constructors...
  for (mysqlpp::Row::const_iterator itr = r.begin();
       itr != r.end(); ++itr) {
    row.push_back((*itr).c_str());
  }
  ostringstream ostr;
  ostr << version;
  row.push_back(ostr.str());
  w.push_back(row);
}
