#include <pqxx/pqxx>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/date_time/date_duration.hpp>
#include <boost/program_options.hpp>
#include <string>
#include <map>
//#include <ext/hash_map>
#include <list>
#include <iostream>

using namespace std;
namespace po = boost::program_options;
using namespace boost::posix_time;
using namespace boost::date_time;
using namespace pqxx;

enum element_t {
  element_node,
  element_way,
  element_relation
};

namespace pqxx {
  template<> void 
  from_string(const char *str, 
	      boost::posix_time::ptime &t) {
    string s(str);
    t = time_from_string(s);
  }

  template<> void
  from_string(const char *str,
	      element_t &type) {
    if (strncmp(str, "node", 4) == 0) {
      type = element_node;
    } else if (strncmp(str, "way", 3) == 0) {
      type = element_way;
    } else if (strncmp(str, "relation", 8) == 0) {
      type = element_relation;
    } else {
      throw runtime_error("couldn't parse element type.");
    };
  }
}

#define MAX_CHANGES 50000

struct changeset {
  changeset(int u, ptime t) 
    : opened_at(t), closed_at(t + hours(1)),
      user_id(u), num_changes(0) {}

  bool is_open(const ptime &t) const {
    return (t < closed_at) && (num_changes < MAX_CHANGES);
  }

  void insert_element(element_t type, const long int &id, const ptime &t) {
    if (!is_open(t)) {
      throw runtime_error("added element to closed changeset.");
    }

    if (type == element_node) {
      nodes.push_back(id);
    } else if (type == element_way) {
      ways.push_back(id);
    } else {
      relations.push_back(id);
    } 

    ++num_changes;
    closed_at = min(t + hours(1), opened_at + hours(24));
  }

  ptime opened_at, closed_at;
  int user_id, num_changes;
  vector<uint64_t> nodes, ways, relations;
  //map<string, string> lifted_tags;
};

struct synthesis : public transactor<work> {
  // the time during which detected changesets are allowed to start
  ptime from_time, to_time;

  synthesis(const ptime &f, const ptime &t) 
    : from_time(f), to_time(t) {
  }

  void update_elements(work &x,
		       const uint64_t id,
		       const char *table, 
		       const changeset &c,
		       const vector<uint64_t> &ids) {
    if (!ids.empty()) {
      stringstream q;
      q << "update " << table << " set new_changeset_id=" << id 
	<< " where changeset_id=" << c.user_id 
	<< " and timestamp >= '" << c.opened_at 
	<< "' and timestamp < '" << c.closed_at
	<< "' and id in (";
      
      // don't you just love Ruby's Array.join?
      vector<uint64_t>::const_iterator itr = ids.begin();
      vector<uint64_t>::const_iterator end = ids.end();
      while (itr != end) {
	q << *itr++;
	if (itr == end) break;
	q << ",";
      }
      q << ")";
      //cerr << q.str() << endl;
      x.exec(q, "updating elements");
    }
  }

  void insert_changeset(work &x, const changeset &c) {
    stringstream q;
    q << "insert into changesets (user_id, created_at, "
      << "closed_at, open, num_changes) values (" << c.user_id 
      << ", '" << c.opened_at << "', '" << c.closed_at 
      << "', false, " << c.num_changes << ") returning id";
    //cerr << q.str() << endl;
    result res = x.exec(q, "inserting changset");
    uint64_t id = 0;
    if (!res[0][0].to<uint64_t>(id))
      throw runtime_error("changeset ID is non-numeric.");
    update_elements(x, id, "nodes", c, c.nodes);
    update_elements(x, id, "ways", c, c.ways);
    update_elements(x, id, "relations", c, c.relations);
  }

  typedef map<int, changeset> cs_map;

  // generate the changesets list
  void operator()(work &x) {
    cs_map open_changesets;
    ptime checkpoint = time_from_string("2001-01-01 00:00:00");

    ostringstream q;
    q << "select 'node'::nwr_enum,id,version,changeset_id,timestamp "
      << "from nodes where timestamp >= '" << from_time << "' and "
      << "timestamp < '" << to_time << "' union all "
      << "select 'way'::nwr_enum,id,version,changeset_id,timestamp "
      << "from ways where timestamp >= '" << from_time << "' and "
      << "timestamp < '" << to_time << "' union all "
      << "select 'relation'::nwr_enum,id,version,changeset_id,timestamp "
      << "from relations where timestamp >= '" << from_time << "' and "
      << "timestamp < '" << to_time << "' order by timestamp";
    //cerr << "QUERY: " << q.str() << endl;

    // create a cursor to the across the input stream
    icursorstream in(x, q.str(), "fetch", 10000);
    
    const icursor_iterator ic_end;
    for (icursor_iterator ic_itr(in); ic_itr != ic_end; ++ic_itr) {
      const result &res = *ic_itr;
      for (result::const_iterator itr = res.begin(); 
	   itr != res.end(); ++itr) {
	int user_id;
	ptime timestamp;
	element_t type;
	uint64_t id;

	if (!(*itr)["changeset_id"].to<int>(user_id)) 
	  throw runtime_error("changeset_id was non-numeric.");
	
	if (!(*itr)["id"].to<uint64_t>(id)) 
	  throw runtime_error("element ID was non-numeric.");
	
	if (!(*itr)["timestamp"].to<ptime>(timestamp))
	  throw runtime_error("timestamp couldn't be parsed.");

	if (!(*itr)[0].to<element_t>(type)) 
	  throw runtime_error("element type not recognised.");
	
	if (timestamp > checkpoint) {
	  // iterate over open changesets, killing those which have
	  // auto-closed to keep memory requirements down
	  cs_map::iterator itr = open_changesets.begin();
	  int num_concurrent = 0;
	  while (itr != open_changesets.end()) {
	    cs_map::iterator jtr = itr;
	    ++itr;
	    if (jtr->second.is_open(timestamp)) {
	      ++num_concurrent;
	    } else {
	      insert_changeset(x, jtr->second);
	      open_changesets.erase(jtr);
	    }
	  }
	  cout << "Checkpoint: " << checkpoint 
	       << " concurrent open = " << num_concurrent << endl;
	  checkpoint = ptime(timestamp.date(), hours(24));
	}
	
	cs_map::iterator ctr = open_changesets.find(user_id);
	
	if (ctr == open_changesets.end()) {
	  // user has no open changesets, so create one
	  pair<cs_map::iterator, bool> status = 
	    open_changesets.insert(make_pair(user_id, 
					     changeset(user_id, timestamp)));

	  if (!status.second)
	    throw runtime_error("failed to insert changeset into map!");

	  // insert this element into the changeset
	  status.first->second.insert_element(type, id, timestamp);
	  
	} else {
	  // check changeset is open
	  if (ctr->second.is_open(timestamp)) {
	    // insert this element into the changeset
	    ctr->second.insert_element(type, id, timestamp);
	    
	  } else {
	    // if the changeset closed because of element overload, then
	    // we need to ensure its time-range doesn't overlap with any
	    // other changeset, so set the closed_at time to be now.
	    if (ctr->second.num_changes >= MAX_CHANGES) {
	      ctr->second.closed_at = timestamp;
	    }
	    
	    // insert changeset into the table
	    insert_changeset(x, ctr->second);
	    
	    // close one changeset
	    open_changesets.erase(ctr);
	    
	    // and open another
	    pair<cs_map::iterator, bool> status = 
	      open_changesets.insert(make_pair(user_id, 
					       changeset(user_id, timestamp)));

	    if (!status.second)
	      throw runtime_error("failed to insert changeset into map!");
	    
	    // insert this element into the changeset
	    status.first->second.insert_element(type, id, timestamp);
	  }
	}  
      }
    }

    // at the end, close all the changesets
    for (cs_map::iterator itr = open_changesets.begin();
	 itr != open_changesets.end(); ++itr) {
      // clip the closing time to the end time of the changeset synth
      // run so that we are assured nothing can "leak" out of this
      // session.
      itr->second.closed_at = min(itr->second.closed_at, to_time);

      insert_changeset(x, itr->second);
    }
    open_changesets.clear();
  }
};

int
main(int argc, char *argv[]) {
  string db_name, db_host, db_user, db_pass, from_str, to_str;
  bool verbose = false;
  ptime from, to;

  po::options_description desc("Options for copy-table program.");
  desc.add_options()
    ("help,h",    "This help message.")
    ("verbose,v", "Produce verbose output.")
    ("user", 
     po::value<string>(&db_user)->default_value("osm_api06"),
     "The username for the database.")
    ("host",
     po::value<string>(&db_host)->default_value("localhost"),
     "The database host.")
    ("name", 
     po::value<string>(&db_name)->default_value("osm_api06"),
     "The database name.")
    ("pass", 
     po::value<string>(&db_pass)->default_value("osm_api06"),
     "The password for the user on the db.")
    ("from",
     po::value<string>(&from_str),
     "Time to start creating changesets from.")
    ("to",
     po::value<string>(&to_str),
     "Time to create changesets until.")
    ("only",
     po::value<vector<string> >(),
     "Only these processes (for partial or resumed processing). "
     "If this option is not specified then all processes are run.");
    
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
      cout << "Processing only: ";
      copy(only.begin(), only.end(), ostream_iterator<string>(cout, ", "));
      cout << endl;
    }
  }

  // if the "from" time isn't set then set it to before OSM was
  // started. likewise for the "to" time.
  from = time_from_string(vm.count("from") > 0 ? from_str : "2001-01-01");
  to   = time_from_string(vm.count("to") > 0   ? to_str   : "2010-01-01");

  try {
    ostringstream ostr;
    ostr << "dbname=" << db_name;
    if (!db_host.empty()) { ostr << " host=" << db_host; }
    if (!db_user.empty()) { ostr << " user=" << db_user; }
    if (!db_pass.empty()) { ostr << " password=" << db_pass; }
    pqxx::connection db(ostr.str());
    db.set_client_encoding("UTF8");
    cout << "Opened 'to' database." << endl;

    if (only.empty() || only.count("synth") > 0) {
      cout << "Synthesising changesets..." << endl;
      db.perform(synthesis(from, to));
    }

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
