#include <mysql++.h>
#include <options.h>
#include <pqxx/pqxx>
#include <iostream>
#include <iomanip>
#include <fstream>
#include <vector>
#include <set>
#include <boost/thread.hpp>
#include <boost/bind.hpp>
#include <boost/program_options.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/regex.hpp>
#include <boost/progress.hpp>

#include "split_tags.hpp"
#include "dumpfile.hpp"
#include "dump_row.hpp"
#include "dump_add_version.hpp"
#include "dump_add_version_tags.hpp"
#include "dump_uniq_tag.hpp"
#include "dump_add_version_wr.hpp"
#include "dump_add_sequence.hpp"
#include "globals.hpp"

extern "C" {
#include <sys/time.h>
#include <time.h>
}

using namespace std;
namespace po = boost::program_options;
namespace al = boost::algorithm;

struct progress_timer {
public:
  progress_timer() { gettimeofday(&start_, NULL); }
  ~progress_timer() throw() {
    struct timeval end;
    gettimeofday(&end, NULL);
    double elapsed = double(end.tv_sec - start_.tv_sec) + 
      double(end.tv_usec - start_.tv_usec) / 1000000.0;

    // NOTE: following is stolen from boost::progress_timer, which unfortunately
    // seems to measure elapsed CPU time, not wall-clock time.
    try {
      // use istream instead of ios_base to workaround GNU problem (Greg Chicares)
      std::istream::fmtflags old_flags = cout.setf( std::istream::fixed,
						    std::istream::floatfield );
      std::streamsize old_prec = cout.precision( 2 );
      cout << elapsed << " s" // "s" is System International d'Units std
	   << std::endl;
      cout.flags( old_flags );
      cout.precision( old_prec );
    
    } catch (...) {} // eat any exceptions
  }

private:

  struct timeval start_;
};

static const char byte_order_mark[] = { 0xfe, 0xff, 0x00 };

template <class formatter>
void
copy_table(mysqlpp::Connection &from_db,
	   pqxx::connection &to_db,
	   const string &columns_from,
	   const string &table_from,
	   const string &table_to,
	   const string &order_by,
	   bool append = false,
	   bool nulls = true) {
  // create a timer object so that we can see how long each load is taking
  progress_timer timer;
  pqxx::work transaction(to_db, table_from);

  transaction.exec("truncate " + table_to);

  // Retrieve a subset of the sample stock table set up by resetdb
  mysqlpp::Query query = from_db.query();
  query << "select " << columns_from << " from " << table_from; // << " limit 10";
  if (!order_by.empty()) {
    query << " " << order_by;
  }

  //cout << "Running query." << endl;
  cout << "Copying " << columns_from << " from " 
       << table_from << " to " << table_to << "... " << endl;
  cout << query.str() << endl;
  string null_string = byte_order_mark;
  pqxx::tablewriter writer(transaction, table_to, null_string);
  query.for_each(formatter(writer));
  cout << "\nCopied " << columns_from << " from " 
       << table_from << " to " << table_to << "... " << flush;
  writer.complete();
  //cout << "Query finished." << endl;
  transaction.commit();
}

template <class formatter>
void
copy_table(mysqlpp::Connection &from_db,
	   pqxx::connection &to_db,
	   const string &columns_from,
	   const string &table_from,
	   const string &table_to) {
  copy_table<formatter>(from_db, to_db, columns_from, table_from, table_to, "");
}

template <class formatter>
void
copy_table(mysqlpp::Connection &from_db,
	   pqxx::connection &to_db,
	   const string &columns_from,
	   const string &table_from) {
  copy_table<formatter>(from_db, to_db, columns_from, table_from, table_from);
}

int
main(int argc, char *argv[]) {
  string from_name, from_host, from_user, from_pass, 
    to_name, to_host, to_user, to_pass;

  po::options_description desc("Options for copy-table program.");
  desc.add_options()
    ("help,h",    "This help message.")
    ("verbose,v", "Produce verbose output.")
    ("from-user", 
     po::value<string>(&from_user)->default_value("openstreetmap"),
     "The username for the database to copy from.")
    ("from-host",
     po::value<string>(&from_host)->default_value("localhost"),
     "The host to copy the database from.")
    ("from-name", 
     po::value<string>(&from_name)->default_value("openstreetmap"),
     "The database name to copy from.")
    ("from-pass", 
     po::value<string>(&from_pass)->default_value("openstreetmap"),
     "The password for the from-user on the from-db.")
    ("to-user", 
     po::value<string>(&to_user)->default_value("osm_api06"),
     "The username for the database to copy to.")
    ("to-host",
     po::value<string>(&to_host)->default_value("localhost"),
     "The host to copy the database to.")
    ("to-name", 
     po::value<string>(&to_name)->default_value("osm_api06"),
     "The database name to copy to.")
    ("to-pass", 
     po::value<string>(&to_pass)->default_value("osm_api06"),
     "The password for the to-user on the to-db.")
    ("only",
     po::value<vector<string> >(),
     "Only these tables (for partial or resumed load). "
     "If this option is not specified then all tables will be loaded.");
    
  po::variables_map vm;
  po::store(po::parse_command_line(argc, argv, desc), vm);
  po::notify(vm);

  if (vm.count("help")) {
    cout << desc << "\n";
    return 1;
  }

  if (vm.count("verbose")) {
    cout << "Setting verbose mode." << endl;
    verbose = true;
  }

  // get the list of only's and turn them into a set
  set<string> only;
  if (vm.count("only")) {
    vector<string> args = vm["only"].as<vector<string> >();
    copy(args.begin(), args.end(), inserter(only, only.begin()));

    if (verbose) {
      cout << "Copying only: ";
      copy(only.begin(), only.end(), ostream_iterator<string>(cout, ", "));
      cout << endl;
    }
  }

  try {
    // connect to the remote "from" database.
    mysqlpp::Connection from_db(false);
    from_db.set_option(new mysqlpp::SetCharsetNameOption("latin1"));
    if (!from_db.connect(from_name.c_str(), from_host.c_str(), 
			 from_user.c_str(), from_pass.c_str())) {
      cerr << "Failed to connect to 'from' database.\n";
      return 1;
    }
    cout << "Opened 'from' database." << endl;
    
    // connect to the local "to" database
    ostringstream ostr;
    ostr << "dbname=" << to_name;
    if (!to_host.empty()) { ostr << " host=" << to_host; }
    if (!to_user.empty()) { ostr << " user=" << to_user; }
    if (!to_pass.empty()) { ostr << " password=" << to_pass; }
    pqxx::connection to_db(ostr.str());
    to_db.set_client_encoding("UTF8");
    cout << "Opened 'to' database." << endl;

    // level 0
    if (only.empty() || (only.count("users") > 0)) {
      copy_table<dump_row>(from_db, to_db, "*", "users");
    } 

    // level 1 (except changesets)
    if (only.empty() || (only.count("user_preferences") > 0)) 
      copy_table<dump_row>(from_db, to_db, "*", "user_preferences");

    if (only.empty() || (only.count("user_tokens") > 0)) 
    copy_table<dump_row>(from_db, to_db, "*", "user_tokens");

    if (only.empty() || (only.count("friends") > 0)) 
    copy_table<dump_row>(from_db, to_db, "*", "friends");

    if (only.empty() || (only.count("diary_entries") > 0)) 
    copy_table<dump_row>(from_db, to_db, "*", "diary_entries");

    if (only.empty() || (only.count("gpx_files") > 0)) 
    copy_table<dump_row>(from_db, to_db, "*", "gpx_files");

    //update_list updated_ways, updated_relations; 

    // level 2 (stuff that is basically unmodified) 
    if (only.empty() || (only.count("ways") > 0)) {
      copy_table<dump_row>(from_db, to_db, "*", "ways");
    }
    
    if (only.empty() || (only.count("relations") > 0)) {
      copy_table<dump_row>(from_db, to_db, "*", "relations");
    }
    
    if (only.empty() || (only.count("diary_comments") > 0)) 
      copy_table<dump_row>(from_db, to_db, "*", "diary_comments");
    
    if (only.empty() || (only.count("gpx_file_tags") > 0)) 
      copy_table<dump_row>(from_db, to_db, "*", "gpx_file_tags");
    
    // MySQL allows a special timestamp 0000-00-00 00:00:00 which is
    // invalid (because you want a null value in a not null column??)
    // which we have to remove because it chokes postgres.
    if (only.empty() || (only.count("gps_points") > 0)) 
      copy_table<dump_row>(from_db, to_db, "altitude,trackid,latitude,"
			   "longitude,gpx_id,(case when timestamp=0 then "
			   "null else timestamp end) as timestamp,tile", 
			   "gps_points");

    // leave changeset tags as empty at the moment

    // level 2 (nodes)
    // first need to pull the nodes into the new table, giving them versions based
    // on their timestamp order.
    if (only.empty() || (only.count("nodes") > 0)) {
      copy_table<dump_add_version>(from_db, to_db, "id,latitude,longitude,user_id,"
      			   "visible,timestamp,tile", "nodes", "nodes", 
      			   "force index(nodes_timestamp_idx) order by timestamp");
    }

    // level 3
    // pull the node tags out into a separate table, uniquifying them
    // uniquify the other tags too
    if (only.empty() || (only.count("node_tags") > 0)) {
      copy_table<dump_add_version_tags>(from_db, to_db, "id,timestamp,tags", 
					"nodes", "node_tags", 
					"force index(nodes_timestamp_idx) "
					"order by timestamp");
    }

    if (only.empty() || (only.count("way_tags") > 0)) {
      copy_table<dump_uniq_tag>(from_db, to_db, "*", "way_tags", "way_tags",
				"order by id, version", false, false);
    }
    
    if (only.empty() || (only.count("relation_tags") > 0)) {
      copy_table<dump_uniq_tag>(from_db, to_db, "*", "relation_tags", "relation_tags",
				"order by id, version", false, false);
    }

    // add ordering to the relation members (at this point, any consistent order 
    // will do, as none of the clients treat them as ordered)
    if (only.empty() || (only.count("relation_members") > 0)) {
      copy_table<dump_add_sequence>(from_db, to_db, "id,member_id,"
				    "member_role,version,member_type", 
				    "relation_members", "relation_members", 
				    "order by id");
    }

    // add way nodes. these shouldn't need changing.
    if (only.empty() || (only.count("way_nodes") > 0)) 
      copy_table<dump_row>(from_db, to_db, "*", "way_nodes", "way_nodes");

    if (verbose) {
      cout << "Import complete." << endl;
    }

  } catch (const mysqlpp::Exception &e) {
    cerr << "MYSQL FAIL: " << e.what() << " [" << typeid(e).name() << "]\n";
    return 1;

  } catch (const ios_base::failure &e) {
    cerr << "I/O FAIL: " << e.what() << "\n";
    return 1;

  } catch (const runtime_error &e) {
    cerr << "RUNTIME ERROR: " << e.what() << "\n";
    return 1;

  } catch (const exception &e) {
    cerr << "EXCEPTION: " << e.what() << "\n";
    return 1;

  } catch (...) {
    cerr << "UNSPECIFIED ERROR\n";
    return 1;
  }

  return 0;
}
