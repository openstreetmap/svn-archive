/*                                                                         */
/*         Apache module to serve tiles@home tiles from tilesets           */
/*                                                                         */
/* Update basetilepath, statictilepath and OCEANS_DB_FILE                  */
/* Compile and install with apxs2 -ci mod_tah.c                            */
/* echo  LoadModule tilesAtHome_module /usr/lib/apache2/modules/mod_tah.so */
/*   >/etc/apache2/mods-available/mod_tah.load                             */
/* cd /etc/apache2/mods-enabled ; ln -s ../mods-available/mod_tah.load     */
/* In apache <Location /Tiles> use  SetHandler tah_handler                 */

#include <httpd.h>
#include <http_config.h>
#include <http_protocol.h>
#include <http_log.h>
#include <ap_config.h>
#include <apr_strings.h>
#include <apr_file_info.h>
#include <apr_file_io.h>

#include <stdio.h>

module AP_MODULE_DECLARE_DATA tilesAtHome_module;

#define FILEVERSION 2
#define MIN_VALID_OFFSET 4
const char * const content_imagepng = "image/png";

static char * basetilepath = "/SX40/Tiles/";
static char * statictilepath = "/SX40/Tiles/";
#define OCEANS_DB_FILE "/SX40/oceantiles_12.dat"

/* These layer return a transparent tile if no tileset file exists */
const char * transparent_layers[] = {
	"maplint",
	"caption"
	};

const char land[] =
  "\x89\x50\x4e\x47\x0d\x0a\x1a\x0a\x00\x00\x00\x0d\x49\x48\x44\x52"
  "\x00\x00\x01\x00\x00\x00\x01\x00\x01\x03\x00\x00\x00\x66\xbc\x3a"
  "\x25\x00\x00\x00\x06\x50\x4c\x54\x45\xde\xde\xde\xf8\xf8\xf8\xa9"
  "\xc9\xf8\xf1\x00\x00\x00\x01\x62\x4b\x47\x44\x00\x88\x05\x1d\x48"
  "\x00\x00\x00\x09\x70\x48\x59\x73\x00\x00\x0b\x13\x00\x00\x0b\x13"
  "\x01\x00\x9a\x9c\x18\x00\x00\x00\x07\x74\x49\x4d\x45\x07\xd7\x05"
  "\x1c\x14\x02\x24\x04\xda\x6c\x9f\x00\x00\x00\x33\x49\x44\x41\x54"
  "\x68\xde\xed\xca\xa1\x01\x00\x00\x0c\x02\x20\xff\x7f\x5a\x4f\x58"
  "\x5d\x80\x4c\x7a\x88\x20\x08\x82\x20\x08\x82\x20\x08\x82\x20\x08"
  "\x82\x20\x08\x82\x20\x08\x82\x20\x08\x82\x20\x08\xc2\xef\x30\xf3"
  "\xd2\xe1\xd2\x54\x6c\x9d\x47\x00\x00\x00\x00\x49\x45\x4e\x44\xae"
  "\x42\x60\x82";

const char sea[] =
  "\x89\x50\x4e\x47\x0d\x0a\x1a\x0a\x00\x00\x00\x0d\x49\x48\x44\x52"
  "\x00\x00\x01\x00\x00\x00\x01\x00\x01\x03\x00\x00\x00\x66\xbc\x3a"
  "\x25\x00\x00\x00\x03\x50\x4c\x54\x45\xb5\xd6\xf1\x79\x37\xa1\x32"
  "\x00\x00\x00\x1f\x49\x44\x41\x54\x68\xde\xed\xc1\x01\x0d\x00\x00"
  "\x00\xc2\xa0\xf7\x4f\x6d\x0e\x37\xa0\x00\x00\x00\x00\x00\x00\x00"
  "\x00\xbe\x0d\x21\x00\x00\x01\x7f\x19\x9c\xa7\x00\x00\x00\x00\x49"
  "\x45\x4e\x44\xae\x42\x60\x82";

const char transparent[] =
  "\x89\x50\x4e\x47\x0d\x0a\x1a\x0a\x00\x00\x00\x0d\x49\x48\x44\x52"
  "\x00\x00\x01\x00\x00\x00\x01\x00\x08\x06\x00\x00\x00\x5c\x72\xa8"
  "\x66\x00\x00\x01\x15\x49\x44\x41\x54\x78\xda\xed\xc1\x31\x01\x00"
  "\x00\x00\xc2\xa0\xf5\x4f\xed\x6b\x08\xa0\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
  "\x00\x00\x00\x00\x00\x00\x00\x00\x78\x03\x01\x3c\x00\x01\xd8\x29"
  "\x43\x04\x00\x00\x00\x00\x49\x45\x4e\x44\xae\x42\x60\x82";


typedef struct dir_data_t dir_data_t;
struct dir_data_t {
  int state;
  apr_file_t* oceanDB_file;
  apr_mmap_t* oceanDB_mmap;
}; /* dir_data_t */

typedef struct request_data request_data;
struct request_data {
  int x, y, z;
  int baseX, baseY,  baseZ;
  char layer[32];
  apr_file_t * tileset;
  unsigned int tileOffset, tileLength;
};


/* Convert x y z triple to tileset file name. */
static void basexyz_to_tilesetname(apr_pool_t *p, char ** tilesetName, char * layer, int x, int y, int z) {
  char * fileName;
  fileName = apr_psprintf(p, "%s/%s_%d/%04i/%i_%i", basetilepath, layer, z, x, x, y);
  *tilesetName = fileName;
};


/* Find the composite tileset containing a given tile. */
static void xyz_to_basexyz( request_data* d ) {
  if (d->z < 6) {
    d->baseZ = 0;
  } else
    if (d->z > 11) {
      d->baseZ = 12;
    } else {
      d->baseZ = 6;
    }
  d->baseX = d->x >> (d->z - d->baseZ);
  d->baseY = d->y >> (d->z - d->baseZ);	
}


/* Find the (index) position of a tile in a tileset. */
static int xyz_to_n(request_data * d) {
  int i;
  int tileNo = 0;

  for (i = d->baseZ; i < d->z; i++) {
    tileNo += 1 << (2*(i - d->baseZ));
  }
  int offsetX = d->baseX << (d->z - d->baseZ);
  int offsetY = d->baseY << (d->z - d->baseZ);
  tileNo += (d->y - offsetY) * (1 << (d->z - d->baseZ));
  tileNo += (d->x - offsetX);

  return tileNo;
}



static int serve_tileset(request_rec* r, request_data* d) {
  char * tilesetName;
  basexyz_to_tilesetname(r->pool, &tilesetName, d->layer, d->baseX, d->baseY, d->baseZ);
  //ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "tilesetfile: %s", tilesetName);
	
  struct apr_finfo_t finfo;
  apr_status_t res;
  if ((res = apr_stat(&finfo, tilesetName, APR_FINFO_MTIME|APR_FINFO_SIZE, r->pool)) != APR_SUCCESS) { 
    return HTTP_NOT_FOUND; /* will go on */
  }
  if (finfo.size < 8 + 1366*4) {
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "tileset %s too small, %i!", tilesetName, finfo.size );
    return HTTP_NOT_FOUND;
  }

  ap_update_mtime(r, finfo.mtime);
  ap_set_last_modified(r);

  if ((res = ap_meets_conditions(r)) != OK) return res;

  /* open the tileset file */
  if ((res = apr_file_open(&d->tileset, tilesetName, APR_READ | APR_FOPEN_SENDFILE_ENABLED, APR_OS_DEFAULT, r->pool)) != APR_SUCCESS) {
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "ERROR: failed to open tilesetfile");
    //this shouldn't happen, as we checked the file before;
    return HTTP_NOT_FOUND;
  };
  apr_mmap_t* header;
  if ((res = apr_mmap_create( &header, d->tileset, 0, 8+1366*4, APR_MMAP_READ, r->pool )) != APR_SUCCESS) {
    char err[256];
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "mmap failed for %s header, %s", tilesetName, apr_strerror( res, err, 256 ));
    apr_file_close(d->tileset);
    return HTTP_NOT_FOUND;
  } // if
	
  /* read the header */
  char* version = header->mm;
  if (*version > FILEVERSION) {
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "tilesetfile header %s is CORRUPT", tilesetName);
    apr_mmap_delete(header);
    apr_file_close(d->tileset);
    return HTTP_NOT_FOUND;
  }
    
  unsigned* offset_table = header->mm + 8;
  int tileNo = xyz_to_n(d);
  d->tileOffset = offset_table[tileNo];

  if (d->tileOffset < MIN_VALID_OFFSET) {
    switch (d->tileOffset) {
      case 0: {
        apr_mmap_delete(header);
        apr_file_close(d->tileset);
        return HTTP_NOT_FOUND;
      }
      case 1: {
        ap_set_content_length(r, sizeof(sea));
        ap_set_content_type(r, content_imagepng);
        ap_rwrite(sea,sizeof(sea),r);
        apr_mmap_delete(header);
        apr_file_close(d->tileset);
        return OK;
      }
      case 2: {
        ap_set_content_length(r, sizeof(land));
        ap_set_content_type(r, content_imagepng);
        ap_rwrite(land,sizeof(land),r);
        apr_mmap_delete(header);
        apr_file_close(d->tileset);
        return OK;
      }
      case 3: {
        ap_set_content_length(r, sizeof(transparent));   /* TRANSPARENT */
        ap_set_content_type(r, content_imagepng);
        ap_rwrite(transparent,sizeof(transparent),r);
        apr_mmap_delete(header);
        apr_file_close(d->tileset);
        return OK;
      }
      default: { /* ERROR_TILE */
        apr_mmap_delete(header);
        apr_file_close(d->tileset);
        return HTTP_NOT_FOUND;
      }
    }
  } /* if */

  /* >= MIN_VALID_OFFSET */
  d->tileLength = 0;
  int tile;
  /* look for the next non-land/sea/... tile */
  for (tile = tileNo + 1; tile <= 1366; ++tile) {
    if (offset_table[tile] > MIN_VALID_OFFSET) {
      d->tileLength = offset_table[tile] - d->tileOffset;
      break;
    }
  }
  if (d->tileLength == 0)
  {
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "failed to get tile length. file %s, tile %i", r->filename, tileNo );
    apr_mmap_delete(header);
    apr_file_close(d->tileset);
    return HTTP_NOT_FOUND;
  }
  apr_mmap_delete(header);
  //ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "tile %i, offset %i, length %i", tileNo, d->tileOffset, d->tileLength );

  if (finfo.size < d->tileOffset + d->tileLength) {
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "tileset %s too small, %i/%i!", tilesetName, (d->tileOffset + d->tileLength), finfo.size );
    apr_file_close(d->tileset);
    return HTTP_NOT_FOUND;
  }

  //ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "tilesetfile %s offset % i, length %i, %i %i", r->filename, tileOffset, tileLength, tile, sizeof(int));

  /* actually send the data */
  ap_set_content_length(r, d->tileLength);
  ap_set_content_type(r, content_imagepng);
	
  apr_size_t bytes_sent = 0;
  ap_send_fd(d->tileset, r, d->tileOffset, d->tileLength, &bytes_sent);
  if (bytes_sent != d->tileLength) {
    /* no way to fix this. just remember it in the log. */
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "sendfile failed to deliver correct number of bytes, ", r->filename);
  }
    apr_file_close(d->tileset);
    return OK;
} /* serve_tileset */


/* Checks if the layername is handled as transparent layer */
static int is_transparent_layer( const char * layername) {
  int i;
  for( i=0; i < sizeof(transparent_layers)/sizeof(char*); i++)
    if( strcmp(layername,transparent_layers[i]) == 0)
      return 1;
  return 0;
}

static int serve_oceantile(request_rec *r, request_data* rd) {
  char * fileName;
  apr_status_t res;
  apr_size_t len;
  apr_off_t offset;
  unsigned char data;
  int bit_off;
  int type;

  /* send transparent tile if transparent layer */
  if (is_transparent_layer(rd->layer)) {
    ap_set_content_type(r, content_imagepng);
    ap_set_content_length(r, sizeof(transparent));
    ap_rwrite(transparent,sizeof(transparent),r);
    return OK;
  }

  if (rd->z < 12) {
    /* only available for zooms levels >= 12. */
    return HTTP_NOT_FOUND;
  } else {		
    offset = (4096*rd->baseY + rd->baseX) >> 2;
    dir_data_t* d = ap_get_module_config(r->per_dir_config, &tilesAtHome_module );
    unsigned char* data_start = d->oceanDB_mmap->mm;
    data = data_start[ offset ];
    bit_off = 3 - (rd->baseX % 4);  	   /* extract the actual blankness data. */
    type = ((data >> (2*bit_off)) & 3);

//  register_timeout ("send", r);
//  ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "blank lookup %d %d %d type %d", x, y, z, type );

    switch (type) {
      case 0: {
        return HTTP_NOT_FOUND;
      }
    case 1: { /* blank land */
        ap_set_content_type(r, content_imagepng);
        ap_set_content_length(r, sizeof(land));
        ap_rwrite(land,sizeof(land),r);
        return OK;
      }
      case 2: {  /* blank sea */
        ap_set_content_type(r, content_imagepng);
        ap_set_content_length(r, sizeof(sea));
        ap_rwrite(sea,sizeof(sea),r);
        return OK;
      }
      case 3: {  /* Also for type mixed */
        ap_set_content_type(r, content_imagepng);
        ap_set_content_length(r, sizeof(transparent));
        ap_rwrite(transparent,sizeof(transparent),r);
        return OK;
      }
    } 
  }
} /* serve_oceantile */

	
static int tah_handler(request_rec *r)
{ /* Only when its our turn. SetHandler tah_handler in <Location */
  if (!r->handler || strcasecmp(r->handler, "tah_handler") != 0) {
    /* r->handler wasn't us, so it's not our business */
    return DECLINED;
  }
  if (r->method_number != M_GET) return DECLINED;

  apr_status_t res;
  request_data* d = apr_palloc( r->pool, sizeof( request_data ) );

  d->layer[0] = '\0';
  /* URI = ...Tiles/[layer]/<z>/<x>/<y>.png   Safe?*/
  int n = sscanf(r->uri, "/Tiles/%31[a-z]/%d/%d/%d.png", d->layer, &d->z, &d->x, &d->y);
  if (n < 4) return DECLINED;  

  /* Servers SHOULD send the must-revalidate directive if and only if failure to revalidate a request on the entity
     could result in incorrect operation, such as a silently unexecuted financial transaction. */
  apr_table_setn(r->headers_out, "Cache-Control","max-age=10800");

  //ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "serve handler(%s), uri(%s), filename(%s), path_info(%s)",
  //  r->handler, r->uri, r->filename, r->path_info);

  xyz_to_basexyz( d );    /* search for the tileset. */
  //ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "serving layer %s @ %i, x %i, y %i, baseZ %i, baseX %i, baseY %i", d->layer, d->z, d->x, d->y, d->baseZ, d->baseX, d->baseY );

  int code;
  if( (code = serve_tileset(r, d)) != HTTP_NOT_FOUND ) { return code; }
  /* not found. look into the OceanDB. Fail if problem */
  return serve_oceantile(r, d);
} /* tah_handler */


static void* tah_create_dir_conf( apr_pool_t* pool, char* x )
{
  dir_data_t* dir = apr_pcalloc( pool, sizeof( dir_data_t ) );
  apr_status_t res;

  /* TODO: should this be moved into the setmodTahEnabled function? */
  if ((res = apr_file_open(&dir->oceanDB_file, OCEANS_DB_FILE, APR_READ, APR_OS_DEFAULT, pool)) != APR_SUCCESS) {
    ap_log_perror(APLOG_MARK, APLOG_ERR, res, pool, "ERROR: failed to open Oceans DB %s", OCEANS_DB_FILE);
    return 0;
  };
  if ((res = apr_mmap_create( &dir->oceanDB_mmap, dir->oceanDB_file, 0, 4*1024*1024, APR_MMAP_READ, pool )) != APR_SUCCESS) {
    ap_log_perror(APLOG_MARK, APLOG_ERR, res, pool, "ERROR: failed to mmap Oceans DB %s", OCEANS_DB_FILE);
    return 0;
  };		
  //ap_log_perror(APLOG_MARK, APLOG_ERR, res, pool, "mmaping2 done :%i", dir->oceanDB_mmap);
  return dir;
}

static const char *set_OceanFile(cmd_parms *cmd,void *data,const char *arg){
  //tah_config_t *conf=data;
  //if(!(conf->cfgfn=ap_server_root_relative(cmd->pool,arg)))
  //return apr_pstrcat(cmd->pool,"Invalid OceanFile: ",arg,NULL);
  return NULL;
}

static const command_rec mod_tah_cmds[] =
{
  AP_INIT_TAKE1("OceanFile",set_OceanFile,NULL,ACCESS_CONF|RSRC_CONF,
                "Fullpath with OceanDBFile"),
  {NULL}
};


static void tah_register_hooks(apr_pool_t *p) {
  ap_hook_handler(tah_handler, NULL, NULL, APR_HOOK_MIDDLE);
}

// API hooks
module AP_MODULE_DECLARE_DATA tilesAtHome_module = {
  STANDARD20_MODULE_STUFF, 
  tah_create_dir_conf,         /* create per-dir    config structures */
  NULL,                        /* merge  per-dir    config structures */
  NULL,                        /* create per-server config structures */
  NULL,                        /* merge  per-server config structures */
  mod_tah_cmds,                /* table of config file commands       */
  tah_register_hooks           /* register hooks                      */
};
