#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#undef USE_ICONV

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <limits.h>
#include <time.h>
#include <utime.h>

#include <pqxx/pqxx>
#include <stdexcept>
#include <signal.h>
#include <stdarg.h>
#include <assert.h>
#include <cstdlib>

#include "users.hpp"
extern "C" {
#include "keyvals.h"
#include "output_osm.h"
}

#define SCALE 10000000.0

using namespace std;
using namespace pqxx;

const char *reformDate(const char *str)
{
    static char out[64], prev[64]; // Not thread safe

    time_t tmp;
    struct tm tm;

    // Re-use the previous answer if we asked to convert the same timestamp twice
    // This accelerates bulk uploaded data where sequential features often have the same timestamp
    if (!strncmp(prev, str, sizeof(prev)))
        return out;
    else
        strncpy(prev, str, sizeof(prev));

    // 2007-05-20 13:51:35
    bzero(&tm, sizeof(tm));
    int n = sscanf(str, "%d-%d-%d %d:%d:%d",
                   &tm.tm_year, &tm.tm_mon, &tm.tm_mday, &tm.tm_hour, &tm.tm_min, &tm.tm_sec);

    if (n !=6)
        printf("failed to parse date string, got(%d): %s\n", n, str);

    tm.tm_year -= 1900;
    tm.tm_mon  -= 1;
    tm.tm_isdst = -1;

    // Rails stores the timestamps in the DB using UK localtime (ugh), convert to UTC
    tmp = mktime(&tm);
    gmtime_r(&tmp, &tm);

    //2007-07-10T11:32:32Z
    snprintf(out, sizeof(out), "%d-%02d-%02dT%02d:%02d:%02dZ",
             tm.tm_year+1900, tm.tm_mon+1, tm.tm_mday, tm.tm_hour, tm.tm_min, tm.tm_sec);

    return out;
}

/**
 * Uses a cursor through a particular table to give the appearance 
 * of an array of elements. This only works if the IDs are accessed 
 * in a strictly ascending order and its only efficient if most of 
 * the elements are accessed.
 */
template <typename id_type>
class table_stream {
public:

  table_stream(pqxx::work &x, string query, string name) 
    : stream(x, query, name, 1000),
      ic_itr(stream), ic_end(), next_id(0) {
    // initialise the iterators to the beginning of the table
    // and read the first element from it.
    r_itr = ic_itr->begin();

    if (r_itr != ic_itr->end()) {
      if (!(*r_itr)[0].to<id_type>(next_id)) {
	throw std::runtime_error("ID is not numeric.");
      }
    }
  }

  bool find_id(id_type id) {
    while (id > next_id) {
      next();
    }

    return id == next_id;
  }

private:

  icursorstream stream;
  icursor_iterator ic_itr;
  const icursor_iterator ic_end;

protected:

  void next() {
    ++r_itr;
    if (r_itr == ic_itr->end()) {
      // we're at the end of this chunk, so grab another chunk from
      // the cursor
      ++ic_itr;
      if (ic_itr == ic_end) {
	// reached the end of the table, so flag this to the other
	// routines by setting next_id to an invalid value
	next_id = std::numeric_limits<id_type>::max();
      } else {
	r_itr = ic_itr->begin();
      }
    }

    // get the next ID unless the end of the table has been hit
    if (next_id < std::numeric_limits<id_type>::max()) {
      if (!(*r_itr)[0].to<id_type>(next_id)) {
	throw std::runtime_error("Next ID is non-numeric.");
      }
    }
  }

  result::const_iterator r_itr;
  id_type next_id;
};

/**
 * extends the table stream to provide utility methods for getting
 * tags out of the table.
 */
struct tag_stream 
  : public table_stream<int> {

  tag_stream(pqxx::work &x, const char *table) 
    : table_stream<int>(x, query(table), "fetch_tags") {
  }

  bool get(int id, struct keyval *kv) {
    bool has_tags = find_id(id);
    resetList(kv);
    if (has_tags) {
      while (id == next_id) {
	addItem(kv, (*r_itr)[1].c_str(), (*r_itr)[2].c_str(), 0);
	next();
      }
    }
    return has_tags;
  }

private:

  string query(const char *table) {
    ostringstream ostr;
    ostr << "select id, k, v from " << table << " order by id, k";
    return ostr.str();
  }
};

/**
 * gets way nodes out of the stream and returns them in the 
 * keyval struct that the output functions are expecting.
 */
struct way_node_stream 
  : public table_stream<int> {

  way_node_stream(pqxx::work &x) 
    : table_stream<int>(x, "select id, node_id from current_way_nodes "
			"ORDER BY id, sequence_id", "fetch_way_nodes") {
  }

  bool get(int id, struct keyval *kv) {
    bool has_nodes = find_id(id);
    resetList(kv);
    if (has_nodes) {
      while (id == next_id) {
	addItem(kv, "", (*r_itr)[1].c_str(), 0);
	next();
      }
    }
    return has_nodes;
  }
};

/**
 * gets relation members out of the stream and returns them in the 
 * pair of keyval structs that the output functions are expecting.
 */
struct relation_member_stream 
  : public table_stream<int> {

  relation_member_stream(pqxx::work &x) 
    : table_stream<int>(x, "select id, member_id, member_type, "
			"lower(member_role) from current_relation_members "
			"ORDER BY id, sequence_id", "fetch_relation_members") {
  }

  bool get(int id, struct keyval *members, struct keyval *roles) {
    bool has_members = find_id(id);
    resetList(members);
    resetList(roles);
    if (has_members) {
      while (id == next_id) {
	addItem(members, (*r_itr)[2].c_str(), (*r_itr)[1].c_str(), 0);
	addItem(roles, "", (*r_itr)[3].c_str(), 0);
	next();
      }
    }
    return has_members;
  }
};

void nodes(pqxx::work &xaction) {
  struct keyval tags;
  initList(&tags);
  
  ostringstream query;
  query << "select n.id, n.latitude, n.longitude, n.timestamp, "
	<< "c.user_id, n.version, n.changeset_id "
	<< "from current_nodes n join changesets c on n.changeset_id=c.id "
	<< "where n.visible = true order by n.id";
  
  icursorstream nodes(xaction, query.str(), "fetch_nodes", 1000);
  tag_stream tagstream(xaction, "current_node_tags");

  const icursor_iterator ic_end;
  for (icursor_iterator ic_itr(nodes); ic_itr != ic_end; ++ic_itr) {
    const pqxx::result &res = *ic_itr;
    for (pqxx::result::const_iterator itr = res.begin();
	 itr != res.end(); ++itr) {
      int id, version, latitude, longitude, changeset;

      if (!(*itr)[0].to<int>(id)) {
	throw std::runtime_error("Node ID is not numeric.");
      }
      if (!(*itr)[1].to<int>(latitude)) {
	throw std::runtime_error("Latitude is not numeric.");
      }
      if (!(*itr)[2].to<int>(longitude)) {
	throw std::runtime_error("Longitude is not numeric.");
      }
      if (!(*itr)[5].to<int>(version)) {
	throw std::runtime_error("Version is not numeric.");
      }
      if (!(*itr)[6].to<int>(changeset)) {
	throw std::runtime_error("Changeset ID is not numeric.");
      }

      if (!tagstream.get(id, &tags)) {
	resetList(&tags);
      }

      osm_node(id, latitude / SCALE, longitude / SCALE, &tags, 
	       reformDate((*itr)[3].c_str()), 
	       lookup_user((*itr)[4].c_str()), 
	       version, changeset);
    }
  }

  resetList(&tags);
}

void ways(pqxx::work &xaction) {
  struct keyval tags, nodes;
  initList(&tags);
  initList(&nodes);
  
  ostringstream query;
  query << "select w.id, w.timestamp, cs.user_id, w.version, "
	<< "w.changeset_id from current_ways w join changesets cs on "
	<< "w.changeset_id=cs.id where visible = true order by id";
  
  icursorstream ways(xaction, query.str(), "fetch_ways", 1000);
  tag_stream tagstream(xaction, "current_way_tags");
  way_node_stream nodestream(xaction);

  const icursor_iterator ic_end;
  for (icursor_iterator ic_itr(ways); ic_itr != ic_end; ++ic_itr) {
    const pqxx::result &res = *ic_itr;
    for (pqxx::result::const_iterator itr = res.begin();
	 itr != res.end(); ++itr) {
      int id, version, changeset;

      if (!(*itr)[0].to<int>(id)) {
	throw std::runtime_error("Node ID is not numeric.");
      }
      if (!(*itr)[3].to<int>(version)) {
	throw std::runtime_error("Version is not numeric.");
      }
      if (!(*itr)[4].to<int>(changeset)) {
	throw std::runtime_error("Changeset ID is not numeric.");
      }

      tagstream.get(id, &tags);
      nodestream.get(id, &nodes);

      osm_way(id, &nodes, &tags, 
	      reformDate((*itr)[1].c_str()), 
	      lookup_user((*itr)[2].c_str()), 
	      version, changeset);
    }
  }

  resetList(&tags);
  resetList(&nodes);
}

void relations(pqxx::work &xaction) {
  struct keyval tags, members, roles;
  initList(&tags);
  initList(&members);
  initList(&roles);
  
  ostringstream query;
  query << "select r.id, r.timestamp, c.user_id, r.version, r.changeset_id "
	<< "from current_relations r join changesets c on "
	<< "r.changeset_id=c.id where visible = true ORDER BY id";
  
  icursorstream relations(xaction, query.str(), "fetch_relations", 1000);
  tag_stream tagstream(xaction, "current_relation_tags");
  relation_member_stream memstream(xaction);

  const icursor_iterator ic_end;
  for (icursor_iterator ic_itr(relations); ic_itr != ic_end; ++ic_itr) {
    const pqxx::result &res = *ic_itr;
    for (pqxx::result::const_iterator itr = res.begin();
	 itr != res.end(); ++itr) {
      int id, version, changeset;

      if (!(*itr)[0].to<int>(id)) {
	throw std::runtime_error("Relation ID is not numeric.");
      }
      if (!(*itr)[3].to<int>(version)) {
	throw std::runtime_error("Version is not numeric.");
      }
      if (!(*itr)[4].to<int>(changeset)) {
	throw std::runtime_error("Changeset ID is not numeric.");
      }

      tagstream.get(id, &tags);
      memstream.get(id, &members, &roles);

      osm_relation(id, &members, &roles, &tags, 
		   reformDate((*itr)[1].c_str()), 
		   lookup_user((*itr)[2].c_str()), 
		   version, changeset);
    }
  }

  resetList(&tags);
  resetList(&members);
  resetList(&roles);
}

int main(int argc, char **argv)
{
  int i;
  int want_nodes, want_ways, want_relations;
    
  if (argc == 1)
    want_nodes = want_ways = want_relations = 1;
  else {
    want_nodes = want_ways = want_relations = 0;
    for(i=1; i<argc; i++) {
      if (!strcmp(argv[i], "--nodes"))
	want_nodes = 1;
      else if (!strcmp(argv[i], "--ways"))
	want_ways = 1;
      else if (!strcmp(argv[i], "--relations"))
	want_relations = 1;
      else {
	fprintf(stderr, "Usage error:\n");
	fprintf(stderr, "\t%s [--nodes] [--ways] [--relations]\n\n", argv[0]);
	fprintf(stderr, "Writes OSM planet dump to STDOUT. If no flags are specified then all data is output.\n");
	fprintf(stderr, "If one or more flags are set then only the requested data is dumped.\n");
	exit(2);
      }
    }
  }

  char *connection_params = NULL;
  if ((connection_params = getenv("CONNECTION_PARAMS")) == NULL) {
    fprintf(stderr, "ERROR: you must set the $CONNECTION_PARAMS environment "
	    "variable to the appropriate connection parameters.\n");
    exit(2);
  }

  try {
    // open database connection
    pqxx::connection conn(connection_params);
    pqxx::work xaction(conn);

    fetch_users(xaction);

    osm_header();
    
    if (want_nodes)
      nodes(xaction);

    if (want_ways)
      ways(xaction);
    
    if (want_relations)
      relations(xaction);

    osm_footer();
    
    free_users();

    // rollback happens automatically here
  } catch (const std::exception &e) {
    fprintf(stderr, "ERROR: %s\n", e.what());
    return 1;

  } catch (...) {
    fprintf(stderr, "UNKNOWN ERROR!\n");
    return 1;
  }

  return 0;
}
