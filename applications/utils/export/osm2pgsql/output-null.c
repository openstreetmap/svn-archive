#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <errno.h>

#ifdef HAVE_PTHREAD
#include <pthread.h>
#endif

#include "osmtypes.h"
#include "output.h"
#include "output-null.h"

#define UNUSED  __attribute__ ((unused))

static void null_out_cleanup(void) {
}

static int null_out_start(const struct output_options *opt UNUSED) {
    return 0;
}

static void null_out_stop() {
}

static int null_add_node(int a UNUSED, double b UNUSED, double c UNUSED, struct keyval *k UNUSED) {
  return 0;
}

static int null_add_way(int a UNUSED, int *b UNUSED, int c UNUSED, struct keyval *k UNUSED) {
  return 0;
}

static int null_add_relation(int a UNUSED, struct member *b UNUSED, int c UNUSED, struct keyval *k UNUSED) {
  return 0;
}

static int null_delete_node(int i UNUSED) {
  return 0;
}

static int null_delete_way(int i UNUSED) {
  return 0;
}

static int null_delete_relation(int i UNUSED) {
  return 0;
}

static int null_modify_node(int a UNUSED, double b UNUSED, double c UNUSED, struct keyval * k UNUSED) {
  return 0;
}

static int null_modify_way(int a UNUSED, int * b UNUSED, int c UNUSED, struct keyval * k UNUSED) {
  return 0;
}

static int null_modify_relation(int a UNUSED, struct member * b UNUSED, int c UNUSED, struct keyval * k UNUSED) {
  return 0;
}

struct output_t out_null = {
 start:         null_out_start,
 stop:          null_out_stop,
 cleanup:       null_out_cleanup,
 node_add:      null_add_node,
 way_add:       null_add_way,
 relation_add:  null_add_relation,
 
 node_modify:     null_modify_node,
 way_modify:      null_modify_way,
 relation_modify: null_modify_relation,
 
 node_delete:     null_delete_node,
 way_delete:      null_delete_way,
 relation_delete: null_delete_relation
};
