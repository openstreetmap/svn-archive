/**
 * Mapnik WMS module for Apache
 *
 * This is the module code, in pure C. There's no functionality here,
 * instead we just make calls into the C++ code from wms.cpp.
 *
 * part of the Mapnik WMS server module for Apache
 */
#define _GNU_SOURCE

#include "apr.h"
#include "apr_strings.h"
#include "apr_thread_proc.h"
#include "apr_optional.h"
#include "apr_buckets.h"
#include "apr_lib.h"
#include "apr_poll.h"

#define APR_WANT_STRFUNC
#define APR_WANT_MEMFUNC
#include "apr_want.h"

#include "util_filter.h"
#include "ap_config.h"
#include "httpd.h"
#include "http_config.h"
#include "http_request.h"
#include "http_core.h"
#include "http_protocol.h"
#include "http_main.h"
#include "http_log.h"
#include "util_script.h"
#include "ap_mpm.h"
#include "mod_core.h"
#include "mod_cgi.h"
#include "wms.h"

module AP_MODULE_DECLARE_DATA mapnik_wms_module;

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
#include <assert.h>

pthread_mutex_t planet_lock = PTHREAD_MUTEX_INITIALIZER;

static int wms_handler(request_rec *r)
{
    ap_log_rerror(APLOG_MARK, APLOG_INFO, 0, r, "wms handler, h is %s", r->handler);
    
    if(strcmp(r->handler, "wms-handler"))
        return DECLINED;

    ap_log_rerror(APLOG_MARK, APLOG_INFO, 0, r, "wms handler: %s", r->uri);

    /* We set the content type before doing anything else */
    ap_set_content_type(r, "text/html");
    /* If the request is for a header only, and not a request for
     * the whole content, then return OK now. We don't have to do
     * anything else. */
    if (r->header_only) 
    {
         return OK;
    }

    return wms_handle(r);
}

static void child_init(apr_pool_t *p, server_rec *s)
{
    // unused
}

static const char *handle_srs_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->srs[cfg->srs_count++] = word;
    return NULL;
}
static const char *handle_log_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->logfile = word;
    return NULL;
}
static const char *handle_datasource_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->datasource[cfg->datasource_count++] = word;
    return NULL;
}
static const char *handle_font_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->font[cfg->font_count++] = word;
    return NULL;
}
static const char *handle_map_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->map = word;
    return NULL;
}
static const char *handle_title_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->title = word;
    return NULL;
}
static const char *handle_top_layer_title_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->top_layer_title = word;
    return NULL;
}
static const char *handle_top_layer_name_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->top_layer_name = word;
    return NULL;
}
static const char *handle_sub_layer_option(cmd_parms *cmd, void *mconfig, int yesno)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->include_sub_layers = yesno;
    return NULL;
}
static const char *handle_debug_option(cmd_parms *cmd, void *mconfig, int yesno)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->debug = yesno;
    return NULL;
}
static const char *handle_url_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->url = word;
    return NULL;
}
#ifdef USE_KEY_DATABASE
static const char *handle_keyfile_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->key_db_file = word;
    return NULL;
}
static const char *handle_mdw_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->max_demo_width = atoi(word);
    return NULL;
}
static const char *handle_mdh_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->max_demo_height = atoi(word);
    return NULL;
}
#endif
static const char *handle_minlat_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->miny = atof(word);
    return NULL;
}
static const char *handle_minlon_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->minx = atof(word);
    return NULL;
}
static const char *handle_maxlat_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->maxy = atof(word);
    return NULL;
}
static const char *handle_maxlon_option(cmd_parms *cmd, void *mconfig, const char *word)
{
    struct wms_cfg *cfg = ap_get_module_config(cmd->server->module_config, &mapnik_wms_module);
    cfg->maxx = atof(word);
    return NULL;
}
static int wms_post_config(apr_pool_t *pconf, apr_pool_t *plog,
   apr_pool_t *ptemp, server_rec *s)
{
   while(s)
   {
       struct wms_cfg *cfg = ap_get_module_config(s->module_config, &mapnik_wms_module);
        if (!cfg->initialized)
            wms_initialize(s, cfg, pconf);
       s=s->next;
    }
    return OK;
}

static void register_hooks(__attribute__((unused)) apr_pool_t *p)
{
    ap_hook_child_init(child_init, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_post_config(wms_post_config, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_handler(wms_handler, NULL, NULL, APR_HOOK_MIDDLE);
}

static const command_rec wms_options[] =
{
    AP_INIT_ITERATE(
        "WmsSrs",
        handle_srs_option,
        NULL,
        RSRC_CONF,
        "WmsSrs takes a list of allowed SRS names for its argument. They must be supported by the underlying Mapnik installation."
    ),
    AP_INIT_ITERATE(
        "MapnikDatasources",
        handle_datasource_option,
        NULL,
        RSRC_CONF,
        "MapnikDatasources is the path to Mapnik data source modules."
    ),
    AP_INIT_ITERATE(
        "MapnikFonts",
        handle_font_option,
        NULL,
        RSRC_CONF,
        "MapnikFonts takes a list of ttf files to make availalbe to Mapnik."
    ),
    AP_INIT_TAKE1(
        "MapnikMap",
        handle_map_option,
        NULL,
        RSRC_CONF,
        "MapnikMap is the path to the map file."
    ),
    AP_INIT_TAKE1(
        "WmsTitle",
        handle_title_option,
        NULL,
        RSRC_CONF,
        "WmsTitle is the title for your WMS server you want to return for GetCapability requests."
    ),
    AP_INIT_TAKE1(
        "WmsTopLayerTitle",
        handle_top_layer_title_option,
        NULL,
        RSRC_CONF,
        "WmsTopLayerTitle is the title for the top-level layer."
    ),
    AP_INIT_TAKE1(
        "WmsTopLayerName",
        handle_top_layer_name_option,
        NULL,
        RSRC_CONF,
        "WmsTopLayerName is the name for the top-level layer."
    ),
    AP_INIT_FLAG(
        "WmsIncludeSubLayers",
        handle_sub_layer_option,
        NULL,
        RSRC_CONF,
        "When WmsIncludeSubLayers is given, Mapnik's sub layers will be exposed."
    ),
    AP_INIT_FLAG(
        "WmsDebug",
        handle_debug_option,
        NULL,
        RSRC_CONF,
        "When WmsDebug is set, the map file will be loaded for each request instead of once at startup."
    ),
    AP_INIT_TAKE1(
        "MapnikLog",
        handle_log_option,
        NULL,
        RSRC_CONF,
        "MapnikLog is the name of the log file to write Mapnik debug output to."
    ),
    AP_INIT_TAKE1(
        "WmsUrl",
        handle_url_option,
        NULL,
        RSRC_CONF,
        "WmsUrl is the URL under which your WMS server can be reached from the outside. It is used in constructing the GetCapabilities response."
    ),
#ifdef USE_KEY_DATABASE
    AP_INIT_TAKE1(
        "WmsKeyDb",
        handle_keyfile_option,
        NULL,
        RSRC_CONF,
        "WmsKeyDb is the file name of the key data base. If unset, no key checking will be done."
    ),
    AP_INIT_TAKE1(
        "WmsMaxDemoWidth",
        handle_mdw_option,
        NULL,
        RSRC_CONF,
        "WmsMaxDemoWidth is the maximum image width served for demo accounts."
    ),
    AP_INIT_TAKE1(
        "WmsMaxDemoHeight",
        handle_mdh_option,
        NULL,
        RSRC_CONF,
        "WmsMaxDemoHeight is the maximum image height served for demo accounts."
    ),
#endif
    AP_INIT_TAKE1(
        "WmsExtentMinLon",
        handle_minlon_option,
        NULL,
        RSRC_CONF,
        "WmsExtentMinLon is the minimum longitude of data"
    ),
    AP_INIT_TAKE1(
        "WmsExtentMaxLon",
        handle_maxlon_option,
        NULL,
        RSRC_CONF,
        "WmsExtentMaxLon is the maximum longitude of data"
    ),
    AP_INIT_TAKE1(
        "WmsExtentMinLat",
        handle_minlat_option,
        NULL,
        RSRC_CONF,
        "WmsExtentMinLat is the minimum latitude of data"
    ),
    AP_INIT_TAKE1(
        "WmsExtentMaxLat",
        handle_maxlat_option,
        NULL,
        RSRC_CONF,
        "WmsExtentMaxLat is the maximum latitude of data"
    ),
    {NULL}
};

static void *create_wms_conf(apr_pool_t *p, server_rec *s)
{
  struct wms_cfg *newcfg;

  // Allocate memory from the provided pool.
  newcfg = (struct wms_cfg *) apr_pcalloc(p, sizeof(struct wms_cfg));

  newcfg->srs_count = 0;
  newcfg->font_count = 0;
  newcfg->datasource_count = 0;
  newcfg->title = 0;
  newcfg->url = 0;
  newcfg->map = 0;
  newcfg->initialized = 0;
  newcfg->top_layer_name = "OpenStreetMap WMS";
  newcfg->top_layer_title = "OpenStreetMap WMS";
  newcfg->include_sub_layers = 0;
  newcfg->key_db_file = 0;
  newcfg->max_demo_width = 0;
  newcfg->max_demo_height = 0;
  newcfg->debug = 0;
  newcfg->minx = -179.9999;
  newcfg->maxx = 179.9999;
  newcfg->miny = -89.9999;
  newcfg->maxy = 89.9999;

  // Return the created configuration struct.
  return (void *) newcfg;
}

module AP_MODULE_DECLARE_DATA mapnik_wms_module =
{
    STANDARD20_MODULE_STUFF,
    NULL,           /* dir config creater */
    NULL,           /* dir merger --- default is to override */
    create_wms_conf,/* server config */
    NULL,           /* merge server config */
    wms_options,    /* command apr_table_t */
    register_hooks  /* register hooks */
};

struct wms_conf *get_wms_cfg(request_rec *r)
{
   return (struct wms_conf *) ap_get_module_config(r->server->module_config, &mapnik_wms_module);
}

