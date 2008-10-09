/*
    This file is part of extract-polygons.

    extract-polygons is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    extract-polygons is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with extract-polygons.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <limits.h>
#include <stdlib.h>

#include "hashtable.h"
#include "table.h"

static unsigned int hashFromInt(void *k) {
    return ((*(int*)k)-INT_MIN);
}

static int keysEqual(void *k1, void* k2) {
    return *(int*)k1 == *(int*)k2;
}

struct hashtable *table_init() {
    return create_hashtable(4096, hashFromInt, keysEqual);
}

void table_destruct(struct hashtable *table) {
    hashtable_destroy(table, 1);
}

void table_set(struct hashtable *table, int id) {
    int *k = malloc(sizeof(int));
    char *v = malloc(sizeof(char));
    *k = id;
    *v = 1;
    hashtable_insert(table, k, v);
}

int table_get(struct hashtable *table, int id) {
    char *v;
    v = hashtable_search(table, &id);
    if (v == NULL) {
	return 0;
    }
    return (*v);
}
