/**
 * header file for wms
 *
 * see comments in wms.cpp
 */

#include <httpd.h>


#ifndef WMS_H_INCLUDED
#define WMS_H_INCLUDED
struct wms_cfg {
    int srs_count;
    const char *srs[20];
    int datasource_count;
    const char *datasource[20];
    int font_count;
    const char *font[20];
    const char *title;
    const char *url;
    const char *map;
    const char *top_layer_name;
    const char *top_layer_title;
    int include_sub_layers; 
    int initialized;
    void *mapnik_map;
    const char *logfile;
    const char *key_db_file;
    int max_demo_width;
    int max_demo_height;
    int debug;
    float minx;
    float maxx;
    float miny;
    float maxy;
};

//extern "C" int wms_getcap(request_rec *r);
//extern "C" int wms_handle(request_rec *r);
//extern "C" int wms_initialize(struct wms_cfg *cfg, apr_pool_t *p);

//void wms_initialize(struct wms_cfg *c);

#endif
