#include "dump_add_version_wr.hpp"
#include <iostream>

using namespace std;

dump_add_version_wr::dump_add_version_wr(dumpfile &f)
  : fh(f) {
}

void 
dump_add_version_wr::write(dumpfile &fh,
			   const mysqlpp::Row &row,
			   short int version) {
  // the first N-1 fields are the same
  const int size = row.size();
  for (int i = 0; i < size - 1; ++i) {
    const mysqlpp::String &col = row[i];
    if (col.is_null()) {
      fh.fh << "\\N";
      //cout << "\\N";
    } else {
      fh.fh << mysqlpp::quote << col;
      //cout << col;
    }
    fh.fh << "\t";
    //cout << "\t";
  }

  // then the version
  fh.fh << version << "\t";
  //cout << version << "\t";

  // then the last field
  {
    const mysqlpp::String &col = row[size - 1];
    if (col.is_null()) {
      fh.fh << "\\N";
      //cout << "\\N";
    } else {
      fh.fh << mysqlpp::quote << col;
      //cout << col;
    }
  }  
  fh.fh << "\n";
  //cout << "\n";
}

