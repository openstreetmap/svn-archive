#include <pqxx/pqxx>

const char *lookup_user(const char *s);
void fetch_users(pqxx::work &xaction);
void free_users(void);
