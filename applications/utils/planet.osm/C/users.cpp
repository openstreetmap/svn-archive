#include "users.hpp"
#include <sstream>
#include <cstdlib>
#include <cassert>
#include <cstring>

extern "C" {
// for xmlescape:
#include "output_osm.h" 
}

using namespace std;
using namespace pqxx;

static char **user_list;
static unsigned long max_uid;

const char *lookup_user(const char *s) {
  unsigned long int user_id = strtoul(s, NULL, 10);
  const char *user = (user_id >= 0 && user_id <= max_uid) ? user_list[user_id] : "";
  return user ? user : "";
}

unsigned long int max_userid(pqxx::work &xaction) {
  const char *sql = "SELECT MAX(id) FROM users";
  unsigned long int max = 0;

  pqxx::result res = xaction.exec(sql);

  if (!res[0][0].to<unsigned long int>(max)) {
    throw std::runtime_error("Max user ID is not numeric. "
			     "Maybe the users table is empty?");
  }

  return max;
}

void fetch_users(pqxx::work &xaction)
{
  char tmp[1024];
  max_uid = max_userid(xaction);

  user_list = (char **)calloc(max_uid + 1, sizeof(char *));
  if (!user_list) {
    ostringstream ostr;
    ostr << "Malloc of user_list failed for " << max_uid << " users\n";
    throw std::runtime_error(ostr.str());
  }

  ostringstream query;
  query << "SELECT id,display_name from users where id <= " << max_uid
	<< " and data_public = true";
  
  icursorstream users(xaction, query.str(), "fetch_users", 1000);
  
  const icursor_iterator ic_end;
  for (icursor_iterator ic_itr(users); ic_itr != ic_end; ++ic_itr) {
    const pqxx::result &res = *ic_itr;
    for (pqxx::result::const_iterator itr = res.begin();
	 itr != res.end(); ++itr) {
      unsigned long int id;
      string display_name;

      if (!(*itr)[0].to<unsigned long int>(id)) {
	throw std::runtime_error("User ID is not numeric.");
      }
      if (!(*itr)[1].to<string>(display_name)) {
	throw std::runtime_error("Could not get user display name.");
      }

      assert(id <= max_uid);
      snprintf(tmp, sizeof(tmp), " user=\"%s\" uid=\"%lu\"", 
	       xmlescape(display_name.c_str()), id);
      user_list[id] = strdup(tmp);
      assert(user_list[id]);
    }
  }
}

void free_users(void) {
  for(int i=0; i<=max_uid; i++)
    free(user_list[i]);
  free(user_list);
}
