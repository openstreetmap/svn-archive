#include "keyvals.h"

/* const char *xmlescape(char *in)
 *
 * Character escaping for valid XML output as per http://www.w3.org/TR/REC-xml/
 *
 * WARNING: this function uses a static buffer so do not rely on the result
 * being constant if called more than once
 */
const char *xmlescape(const char *in);

void osm_node(int id, 
	      long double lat, long double lon, 
	      struct keyval *tags, const char *ts, 
	      const char *user, int version, int changeset);

/* nodes are "tags" of "" -> node id.
 */
void osm_way(int id, 
	     struct keyval *nodes, 
	     struct keyval *tags, const char *ts, 
	     const char *user, int version, int changeset);

/* members are "tags" of type -> id, roles are "tags" of "" -> role
 * in the same order as members.
 */
void osm_relation(int id, 
		  struct keyval *members, struct keyval *roles, 
		  struct keyval *tags, const char *ts, 
		  const char *user, int version, int changeset);

/* output the header of the osm XML file.
 */
void osm_header();

/* output footer of the osm XML file.
 */
void osm_footer();
