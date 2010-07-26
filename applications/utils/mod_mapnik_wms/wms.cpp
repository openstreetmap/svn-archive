/**
 * Mapnik WMS module for Apache
 *
 * This is the C++ code where Mapnik is actually called to do something.
 *
 */

#define BOOST_SPIRIT_THREADSAFE

/* for jpeg compression */
#define BUFFER_SIZE 4096
#define QUALITY 90

extern "C"
{
#include <png.h>
#include <gd.h>
#include <gdfonts.h>
#include <httpd.h>
#include <http_log.h>
#include <http_protocol.h>
#include <apr_strings.h>
#include <apr_pools.h>
#include <db.h>
#include <proj_api.h>
#include <jpeglib.h>
}

#include <mapnik/map.hpp>
#include <mapnik/datasource_cache.hpp>
#include <mapnik/font_engine_freetype.hpp>
#include <mapnik/agg_renderer.hpp>
#include <mapnik/filter_factory.hpp>
#include <mapnik/color_factory.hpp>
#include <mapnik/image_util.hpp>
#include <mapnik/config_error.hpp>
#include <mapnik/load_map.hpp>
#include <mapnik/octree.hpp>

#include "wms.h"

#include <iostream>
#include <vector>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <pthread.h>

#include "logbuffer.h"


extern "C" 
{
struct wms_cfg *get_wms_cfg(request_rec *r);
bool load_configured_map(server_rec *s, struct wms_cfg *cfg)
{
    //cfg->mapnik_map = (mapnik::Map *) apr_pcalloc(p, sizeof(mapnik::Map));
    try 
    {
        cfg->mapnik_map = new mapnik::Map();
        load_map(*((mapnik::Map *)cfg->mapnik_map), cfg->map);
        ((mapnik::Map *) cfg->mapnik_map)->setAspectFixMode(mapnik::Map::ADJUST_CANVAS_HEIGHT);
    }
    catch (const mapnik::config_error & ex)
    {
        ap_log_error(APLOG_MARK, APLOG_EMERG, 0, s,
             "error initializing map: %s.", ex.what());
        std::clog << "error loading map: " << ex.what() << std::endl;
        delete (mapnik::Map *) cfg->mapnik_map;
        cfg->mapnik_map = NULL;
        return false;
    }
    return true;
};

int wms_initialize(server_rec *s, struct wms_cfg *cfg, apr_pool_t *p)
{
    if (!cfg->datasource_count)
    {
        return HTTP_INTERNAL_SERVER_ERROR;
    }

    for (int i=0; i<cfg->datasource_count; i++)
        mapnik::datasource_cache::instance()->register_datasources(cfg->datasource[i]);

    if (!cfg->font_count)
    {
        return HTTP_INTERNAL_SERVER_ERROR;
    }

    for (int i=0; i<cfg->font_count; i++)
    {
        mapnik::freetype_engine::register_font(cfg->font[i]);
    }

    if (!cfg->map)
    {
        return HTTP_INTERNAL_SERVER_ERROR;
    }

    if (!cfg->url)
    {
        return HTTP_INTERNAL_SERVER_ERROR;
    }
    if (!cfg->title)
    {
        return HTTP_INTERNAL_SERVER_ERROR;
    }

    cfg->initialized = 1;

    if (cfg->debug)
    {
        // will create map later
        std::clog << "debug mode, load map file later" << std::endl;
        return OK;
    }

    if (!load_configured_map(s, cfg))
        return HTTP_INTERNAL_SERVER_ERROR;

    std::clog << "init complete" << std::endl;
    return OK;
}
} /* extern C */


/**
 * Callback for libpng functions.
 */
void user_flush_data(png_structp png_ptr)
{
    /* no-op */
}

/**
 * Used as a data sink for libpng functions. Sends PNG data to Apache.
 */
void user_write_data(png_structp png_ptr,
               png_bytep data, png_size_t length)
{
    request_rec *r = (request_rec *) png_get_io_ptr(png_ptr);
    unsigned int offset = 0;
    while(1)
    {
        int written = ap_rwrite(data + offset, length, r);
        /* FIXME if this should somehow constantly return 0 we'll loop forever. */
        if (written < 0) return;
        if (written + offset == length) return;
        offset += written;
    }
}

/**
 * Used as a data sink for libgd functions. Sends PNG data to Apache.
 */
static int gd_png_sink(void *ctx, const char *data, int length)
{
    request_rec *r = (request_rec *) ctx;
    return ap_rwrite(data, length, r);
}

/** 
 * Callbacks for jpeg library
 */
typedef struct
{
     struct jpeg_destination_mgr pub;
     request_rec *out;
     JOCTET * buffer;
} dest_mgr;

inline void init_destination(j_compress_ptr cinfo)
{
  dest_mgr * dest = reinterpret_cast<dest_mgr*>(cinfo->dest);
  dest->buffer = (JOCTET*) (*cinfo->mem->alloc_small) ((j_common_ptr) cinfo, JPOOL_IMAGE,
                                                       BUFFER_SIZE * sizeof(JOCTET));
  dest->pub.next_output_byte = dest->buffer;
  dest->pub.free_in_buffer = BUFFER_SIZE;
}

inline boolean empty_output_buffer(j_compress_ptr cinfo)
{
    dest_mgr *dest = reinterpret_cast<dest_mgr*>(cinfo->dest);
    unsigned int offset = 0;
    while(1)
    {
        int written = ap_rwrite(dest->buffer + offset, BUFFER_SIZE, dest->out);
        if (written < 0) return false;
        if (written + offset == BUFFER_SIZE) break;
        offset += written;
    }
    dest->pub.next_output_byte = dest->buffer;
    dest->pub.free_in_buffer = BUFFER_SIZE;
    return true;
}

inline void term_destination( j_compress_ptr cinfo)
{
    dest_mgr *dest = reinterpret_cast<dest_mgr*>(cinfo->dest);
    size_t size  = BUFFER_SIZE - dest->pub.free_in_buffer;
    if (size > 0)
    {
        unsigned int offset = 0;
        while(1)
        {
            int written = ap_rwrite(dest->buffer + offset, size, dest->out);
            if (written < 0) return;
            if (written + offset == size) break;
            offset += written;
        }
    }
}

/**
 * Decodes URI, overwrites original buffer.
 */
void decode_uri_inplace(char *uri)
{
    char *c, *d, code[3] = {0, 0, 0};

    for (c = uri, d = uri; *c; c++)
    {
        if ((*c=='%') && isxdigit(*(c+1)) && isxdigit(*(c+2)))
        {
            strncpy(code, c+1, 2); 
            *d++ = (char) strtol(code, 0, 16);
            c+=2;
        } 
        else 
        {
            *d++ = *c;
        }
    }
    *d = 0;
}

/**
 * Sends a HTTP error return code and logs the error.
 */
int http_error(request_rec *r, int code, const char *fmt, ...)
{
    va_list ap;
    char msg[1024]; 
    va_start(ap, fmt);
    vsnprintf(msg, 1023, fmt, ap);
    msg[1023]=0;
    std::clog << msg << std::endl;
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "%s", msg);
    ap_set_content_type(r, "text/plain");
    ap_rputs(msg, r);
    return code;
}

/**
 * Sends a WMS error message in the format requested by the client.
 * This is either an XML document, or an image. The HTTP return code
 * is 200 OK.
 */
int wms_error(request_rec *r, const char *code, const char *fmt, ...)
{
    va_list ap;
    char msg[1024]; 
    va_start(ap, fmt);
    vsnprintf(msg, 1023, fmt, ap);
    msg[1023]=0;
    ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "%s", msg);
    std::clog << msg << std::endl;

    char *args = r->args ? apr_pstrdup(r->pool, r->args) : 0;
    char *amp;
    char *current = args;
    char *equals;
    bool end = (args == 0);

    const char *exceptions = "";
    const char *width = 0;
    const char *height = 0;

    /* parse URL parameters into variables */
    while (!end)
    {
        amp = index(current, '&');
        if (amp == 0) { amp = current + strlen(current); end = true; }
        *amp = 0;
        equals = index(current, '=');
        if (equals > current)
        {
            *equals++ = 0;
            decode_uri_inplace(current);
            decode_uri_inplace(equals);

            if (!strcasecmp(current, "WIDTH")) width = equals;
            else if (!strcasecmp(current, "HEIGHT")) height = equals;
            else if (!strcasecmp(current, "EXCEPTIONS")) exceptions = equals;
        }
        current = amp + 1;
    }

    if (!strcmp(exceptions, "application/vnd.ogc.se_xml") || !width || !height)
    {
        /* XML error message was requested. */
        ap_set_content_type(r, exceptions);
        ap_rprintf(r, "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n"
            "<!DOCTYPE ServiceExceptionReport SYSTEM 'http://www.digitalearth.gov/wmt/xml/exception_1_1_0.dtd'>\n"
            "<ServiceExceptionReport version='1.1.0'>\n"
            "<ServiceException code='%s'>\n%s\n</ServiceException>\n"
            "</ServiceExceptionReport>", code, msg);
    }
    else if (!strcmp(exceptions, "application/vnd.ogc.se_inimage"))
    {
        /* Image error message was requested. We use libgd to create one. */
        int n_width = atoi(width);
        int n_height = atoi(height);
        if (n_width > 0 && n_width < 9999 && n_height > 0 && n_height < 9999)
        {
            gdImagePtr img = gdImageCreate(n_width, n_height);
            (void) gdImageColorAllocate(img, 255, 255, 255);
            int black = gdImageColorAllocate(img, 0, 0, 0);
            gdImageString(img, gdFontGetSmall(), 0, 0, (unsigned char *) msg, black);
            gdSink sink;
            sink.context = (void *) r;
            sink.sink = gd_png_sink;
            ap_set_content_type(r, "image/png");
            gdImagePngToSink(img, &sink);
        }
        else
        {
            return http_error(r, HTTP_INTERNAL_SERVER_ERROR, "Cannot satisfy requested exception type (%s)", exceptions);
        }
    }
    else if (!strcmp(exceptions, "application/vnd.ogc.se_blank"))
    {
        /* Empty image in error was requested. */
        int n_width = atoi(width);
        int n_height = atoi(height);
        if (n_width > 0 && n_width < 9999 && n_height > 0 && n_height < 9999)
        {
            gdImagePtr img = gdImageCreate(n_width, n_height);
            gdImageColorAllocate(img, 255, 255, 255);
            gdSink sink;
            sink.context = (void *) r;
            sink.sink = gd_png_sink;
            ap_set_content_type(r, "image/png");
            gdImagePngToSink(img, &sink);
        }
        else
        {
            return http_error(r, HTTP_INTERNAL_SERVER_ERROR, "Cannot satisfy requested exception type (%s)", exceptions);
        }
    }
    else
    {
        return http_error(r, HTTP_INTERNAL_SERVER_ERROR, "Invalid exception type (%s)", exceptions);
    }
    return OK;
}

/* From Mapnik. */
void reduce_8 (mapnik::ImageData32 const& in, mapnik::ImageData8 &out, mapnik::octree<mapnik::rgb> &tree)
{
    unsigned width = in.width();
    unsigned height = in.height();
    for (unsigned y = 0; y < height; ++y)
    {
        mapnik::ImageData32::pixel_type const * row = in.getRow(y);
        mapnik::ImageData8::pixel_type  * row_out = out.getRow(y);
        for (unsigned x = 0; x < width; ++x)
        {
            unsigned val = row[x];
            mapnik::rgb c((val)&0xff, (val>>8)&0xff, (val>>16) & 0xff);
            uint8_t index = tree.quantize(c);
            row_out[x] = index;
        }
    }
}
     
/* From Mapnik. */
void reduce_4 (mapnik::ImageData32 const& in, mapnik::ImageData8 &out, mapnik::octree<mapnik::rgb> &tree)
{
    unsigned width = in.width();
    unsigned height = in.height();

    for (unsigned y = 0; y < height; ++y)
    {
        mapnik::ImageData32::pixel_type const * row = in.getRow(y);
        mapnik::ImageData8::pixel_type  * row_out = out.getRow(y);

        for (unsigned x = 0; x < width; ++x)
        {
            unsigned val = row[x];
            mapnik::rgb c((val)&0xff, (val>>8)&0xff, (val>>16) & 0xff);
            uint8_t index = tree.quantize(c);
            if (x%2 >  0) index = index<<4;
            row_out[x>>1] |= index;  
        }
    }
}
   
/* From Mapnik. */
void reduce_1(mapnik::ImageData32 const&, mapnik::ImageData8 & out, mapnik::octree<mapnik::rgb> &)
{
    out.set(0); // only one color!
}

/**
 * Generates PNG and sends it to the client.
 * Based on PNG writer code from Mapnik. 
 */
void send_png_response(request_rec *r, mapnik::Image32 buf, unsigned int height, bool smallpng)
{
    mapnik::ImageData32 image = buf.data();
    int depth = 32;
    std::vector<mapnik::rgb> palette;
    mapnik::ImageData8 *reduced;
    unsigned width = image.width();

    if (smallpng)
    {
        mapnik::octree<mapnik::rgb> tree(256);
        for (unsigned y = 0; y < height; ++y)
        {
            mapnik::ImageData32::pixel_type const * row = image.getRow(y);
            for (unsigned x = 0; x < width; ++x)
            {
                unsigned val = row[x];
                tree.insert(mapnik::rgb((val)&0xff, (val>>8)&0xff, (val>>16) & 0xff));
            }
        }

        tree.create_palette(palette);
        assert(palette.size() <= 256);

        if (palette.size() > 16 )
        {
            // >16 && <=256 colors -> write 8-bit color depth
            reduced = new mapnik::ImageData8(width,height);   
            reduce_8(image,*reduced,tree);
            depth = 8;
        }
        else if (palette.size() == 1) 
        {
            // 1 color image ->  write 1-bit color depth PNG
            reduced = new mapnik::ImageData8(width,height);   
            width  = (int(0.125*width) + 7)&~7;
            reduce_1(image,*reduced,tree); 
            depth = 1;
        }
        else 
        {
            // <=16 colors -> write 4-bit color depth PNG
            reduced = new mapnik::ImageData8(width,height);   
            width = (int(0.5*width) + 3)&~3;
            reduce_4(image,*reduced,tree);
            depth = 4;
        }
    }

    png_voidp error_ptr=0;
    png_structp png_ptr=png_create_write_struct(PNG_LIBPNG_VER_STRING, error_ptr,0, 0);

    if (!png_ptr) return;
#if defined(PNG_LIBPNG_VER) && (PNG_LIBPNG_VER >= 10200) && defined(PNG_MMX_CODE_SUPPORTED)
      png_uint_32 mask, flags;
      flags = png_get_asm_flags(png_ptr);
      mask = png_get_asm_flagmask(PNG_SELECT_READ | PNG_SELECT_WRITE);
      png_set_asm_flags(png_ptr, flags | mask);
#endif
    png_set_filter (png_ptr, 0, PNG_FILTER_NONE);
    png_infop info_ptr = png_create_info_struct(png_ptr);
    if (!info_ptr)
    {
        png_destroy_write_struct(&png_ptr,(png_infopp)0);
        return;
    }
    if (setjmp(png_jmpbuf(png_ptr)))
    {
        png_destroy_write_struct(&png_ptr, &info_ptr);
        return;
    }
    png_set_write_fn(png_ptr,
            (void *) r, user_write_data, user_flush_data);

    std::clog << "png preparation complete" << std::endl;
    if (smallpng)
    {
        png_set_IHDR(png_ptr, info_ptr,width,height,depth,
                PNG_COLOR_TYPE_PALETTE,PNG_INTERLACE_NONE,
                PNG_COMPRESSION_TYPE_DEFAULT,PNG_FILTER_TYPE_DEFAULT);
        png_set_PLTE(png_ptr,info_ptr,reinterpret_cast<png_color*>(&palette[0]),palette.size());
    }
    else
    {
        png_set_IHDR(png_ptr, info_ptr,width,height,8,
                PNG_COLOR_TYPE_RGB_ALPHA,PNG_INTERLACE_NONE,
                PNG_COMPRESSION_TYPE_DEFAULT,PNG_FILTER_TYPE_DEFAULT);
    }
    png_write_info(png_ptr, info_ptr);

    for (unsigned int i = 0; i < height; i++)
    {
        // this is a primitive way of making sure that the client gets exatly the number of
        // rows it asked for, even if the generated image should be larger or smaller. This
        // method drops rows, or duplicates them, which makes for bad image quality if the
        // client asked for a "wrong" size. proper rescaling would be better
        unsigned int src_row = (int) (i * image.height() * 1.0 / height + 0.5);
        png_write_row(png_ptr,
            (smallpng) ? (png_bytep)reduced->getRow(src_row)
                       : (png_bytep)image.getRow(src_row));
    }

    png_write_end(png_ptr, info_ptr);
    png_destroy_write_struct(&png_ptr, &info_ptr);
    if (smallpng) delete reduced;
}

/**
 * Generates JPEG and sends it to the client.
 * Based on JPEG writer code from Mapnik. 
 */
void send_jpeg_response(request_rec *r, mapnik::Image32 buf, unsigned int height)
{
    mapnik::ImageData32 image = buf.data();
    struct jpeg_compress_struct cinfo;
    struct jpeg_error_mgr jerr;

    int iwidth=image.width();
    int iheight=image.height();

    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&cinfo);

    cinfo.dest = (struct jpeg_destination_mgr *)(*cinfo.mem->alloc_small)
        ((j_common_ptr) &cinfo, JPOOL_PERMANENT, sizeof(dest_mgr));
    dest_mgr * dest = (dest_mgr*) cinfo.dest;
    dest->pub.init_destination = init_destination;
    dest->pub.empty_output_buffer = empty_output_buffer;
    dest->pub.term_destination = term_destination;
    dest->out = r;

    //jpeg_stdio_dest(&cinfo, fp);
    cinfo.image_width = iwidth;
    cinfo.image_height = height;
    cinfo.input_components = 3;
    cinfo.in_color_space = JCS_RGB;
    jpeg_set_defaults(&cinfo);
    jpeg_set_quality(&cinfo, QUALITY,1);
    jpeg_start_compress(&cinfo, 1);
    JSAMPROW row_pointer[1];
    JSAMPLE* row=reinterpret_cast<JSAMPLE*>( ::operator new (sizeof(JSAMPLE) * iwidth*3));
    for (unsigned int i = 0; i < height; i++)
    {
        // this is a primitive way of making sure that the client gets exatly the number of
        // rows it asked for, even if the generated image should be larger or smaller. This
        // method drops rows, or duplicates them, which makes for bad image quality if the
        // client asked for a "wrong" size. proper rescaling would be better

        unsigned int src_row = (int) (i * iheight * 1.0 / height + 0.5);
        const unsigned* imageRow=image.getRow(src_row);
        int index=0;
        for (int j=0; j<iwidth; j++)
        {
            row[index++]=(imageRow[j])&0xff;
            row[index++]=(imageRow[j]>>8)&0xff;
            row[index++]=(imageRow[j]>>16)&0xff;
        }
        row_pointer[0] = &row[0];
        (void) jpeg_write_scanlines(&cinfo, row_pointer, 1);
    }
    ::operator delete(row);

    jpeg_finish_compress(&cinfo);
    jpeg_destroy_compress(&cinfo);
}

/**
 * Creates a GetCapabilties response and sends it to the client.
 */
int wms_getcap(request_rec *r)
{
    struct wms_cfg *config = get_wms_cfg(r);

    std::string srs_list;
    std::string bbox_list;

    char buffer[1024];
    sprintf(buffer, "   <LatLonBoundingBox minx='%f' miny='%f' maxx='%f' maxy='%f' />\n", 
        config->minx, config->miny, config->maxx, config->maxy);
    bbox_list.append(buffer);
    sprintf(buffer, "   <BoundingBox SRS='EPSG:4326' minx='%f' miny='%f' maxx='%f' maxy='%f' />\n", 
        config->minx, config->miny, config->maxx, config->maxy);
    bbox_list.append(buffer);

    for (int i=0; i<config->srs_count; i++)
    {
        sprintf(buffer, "+init=%s", config->srs[i]);
        for (char *j = buffer; *j; j++) *j=tolower(*j);
        projPJ pj = pj_init_plus(buffer);
        if (!pj)
        {
            std::clog << "error while initializing projection '" << config->srs[i] << "': '" << pj_strerrno(pj_errno) << "', dropped" << std::endl;
            continue;
        }
        projUV uv;
        uv.u = config->minx;
        uv.v = config->miny;
        if (strcmp(config->srs[i], "EPSG:4326"))
        {
            uv.u *= DEG_TO_RAD;
            uv.v *= DEG_TO_RAD;
            uv = pj_fwd(uv, pj);
        }

        srs_list.append("    <SRS>");
        srs_list.append(config->srs[i]);
        srs_list.append("</SRS>\n");

    /*
     * add bounding boxes in layer coordinates. this seems not to be required,
     * plus it makes things a bit difficult for those SRSes which give "nan"
     * values for large extents.
     
        sprintf(buffer, "   <BoundingBox SRS='%s' minx='%f' miny='%f' ", config->srs[i], uv.u, uv.v);
        bbox_list.append(buffer);

        uv.u = config->maxx;
        uv.v = config->maxy;
        if (strcmp(config->srs[i], "EPSG:4326"))
        {
            uv.u *= DEG_TO_RAD;
            uv.v *= DEG_TO_RAD;
            uv = pj_fwd(uv, pj);
        }
        pj_free(pj);
        sprintf(buffer, "maxx='%f' maxy='%f' />\n", uv.u, uv.v);
        bbox_list.append(buffer);
    */
    }

    ap_set_content_type(r, "application/vnd.ogc.wms_xml");
    ap_rprintf(r, 
        "<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n"
        "<!DOCTYPE WMT_MS_Capabilities SYSTEM 'http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd'\n"
        " [\n"
        " <!ELEMENT VendorSpecificCapabilities EMPTY>\n"
        " ]>  <!-- end of DOCTYPE declaration -->\n"
        "\n"
        "<WMT_MS_Capabilities version='1.1.1'>\n"
        "\n"
        "<Service>\n"
        "  <Name>OGC:WMS</Name>\n"
        "  <Title>%s</Title>\n"
        "  <OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink' xlink:href='%s%s?'/>\n"
        "</Service>\n"
        "\n"
        "<Capability>\n"
        "  <Request>\n"
        "    <GetCapabilities>\n"
        "      <Format>application/vnd.ogc.wms_xml</Format>\n"
        "      <DCPType>\n"
        "        <HTTP>\n"
        "          <Get><OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink' xlink:href='%s%s?'/></Get>\n"
        "        </HTTP>\n"
        "      </DCPType>\n"
        "    </GetCapabilities>\n"
        "    <GetMap>\n"
        "      <Format>image/png</Format>\n"
        "      <Format>image/png8</Format>\n"
        "      <Format>image/jpeg</Format>\n"
        "      <DCPType>\n"
        "        <HTTP>\n"
        "          <Get><OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink' xlink:href='%s%s?'/></Get>\n"
        "        </HTTP>\n"
        "      </DCPType>\n"
        "    </GetMap>\n"
        "  </Request>\n"
        "  <Exception>\n"
        "    <Format>application/vnd.ogc.se_xml</Format>\n"
        "    <Format>application/vnd.ogc.se_inimage</Format>\n"
        "    <Format>application/vnd.ogc.se_blank</Format>\n"
        "  </Exception>\n"
        "  <UserDefinedSymbolization SupportSLD='0' UserLayer='0' UserStyle='0' RemoteWFS='0'/>\n", 
                config->title, config->url, r->uri, config->url, r->uri, config->url, r->uri);

    // FIXME more if this should be configurable.
    ap_rprintf(r, 
        "  <Layer>\n"
        "    <Name>%s</Name>\n"
        "    <Title>%s</Title>\n"
        "%s\n"
        "%s\n"
        "    <Attribution>\n"
        "        <Title>www.openstreetmap.org/CC-BY-SA2.0</Title>\n"
        "        <OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink' xlink:href='http://www.openstreetmap.org/'/>\n"
        "    </Attribution>\n"
        "    <Layer queryable='0' opaque='1' cascaded='0'>\n"
        "        <Name>%s</Name>\n"
        "        <Title>%s</Title>\n"
        "        <Abstract>Full OSM Mapnik rendering.</Abstract>\n"
        "%s\n"
        "%s\n",
            config->top_layer_name, config->top_layer_title, srs_list.c_str(), bbox_list.c_str(), 
            config->top_layer_name, config->top_layer_title, srs_list.c_str(), bbox_list.c_str());

    if (config->include_sub_layers)
    {
        /* TODO - add auto-generated set of layers from Mapnik map file, should look like so:

        ap_rprintf(r, 
        "        <Layer queryable='0' opaque='1' cascaded='0'><Name>places</Name><Title>places</Title></Layer>\n");

        */
    }
    ap_rprintf(r, 
        "    </Layer>\n"
        "  </Layer>\n"
        "</Capability>\n"
        "</WMT_MS_Capabilities>");
    return OK;
}

/**
 * Handles the GetMap request.
 */
int wms_getmap(request_rec *r)
{
    int rv = OK;
    struct wms_cfg *config = get_wms_cfg(r);

    const char *layers = 0;
    char *srs = 0;
    const char *bbox = 0;
    const char *width = 0;
    const char *height = 0;
    const char *styles = 0;
    const char *format = 0;
    const char *transparent = 0;
    const char *bgcolor = 0;
    const char *exceptions = "application/vnd.ogc.se_xml";

    char *args = r->args ? apr_pstrdup(r->pool, r->args) : 0;
    char *amp;
    char *current = args;
    char *equals;
    bool end = (args == 0);

    /* 
     * in debug mode, the map is loaded/parsed for each request. that makes
     * it easier to make changes (no apache restart required)
     */

    if ((config->debug) && (!load_configured_map(r->server, config)))
    {
        return wms_error(r, "InvalidDimensionValue", "error parsing map file");
    }

    if (!config->mapnik_map)
    {
        return wms_error(r, "InvalidDimensionValue", "error parsing map file");
    }

    /* parse URL parameters into variables */
    while (!end)
    {
        amp = index(current, '&');
        if (amp == 0) { amp = current + strlen(current); end = true; }
        *amp = 0;
        equals = index(current, '=');
        if (equals > current)
        {
            *equals++ = 0;
            decode_uri_inplace(current);
            decode_uri_inplace(equals);

            if (!strcasecmp(current, "LAYERS")) layers = equals;
            else if (!strcasecmp(current, "SRS")) srs = equals;
            else if (!strcasecmp(current, "BBOX")) bbox = equals;
            else if (!strcasecmp(current, "WIDTH")) width = equals;
            else if (!strcasecmp(current, "HEIGHT")) height = equals;
            else if (!strcasecmp(current, "STYLES")) styles = equals;
            else if (!strcasecmp(current, "FORMAT")) format = equals;
            else if (!strcasecmp(current, "TRANSPARENT")) transparent = equals;
            else if (!strcasecmp(current, "BGCOLOR")) bgcolor = equals;
            else if (!strcasecmp(current, "EXCEPTIONS")) exceptions = equals;
        }
        current = amp + 1;
    }

    if (!layers) return wms_error(r, "MissingDimensionValue", "required parameter 'layers' not set");
    if (!srs) return wms_error(r, "MissingDimensionValue", "required parameter 'srs' not set");
    if (!bbox) return wms_error(r, "MissingDimensionValue", "required parameter 'bbox' not set");
    if (!width) return wms_error(r, "MissingDimensionValue", "required parameter 'width' not set");
    if (!height) return wms_error(r, "MissingDimensionValue", "required parameter 'height' not set");
    if (!styles) return wms_error(r, "MissingDimensionValue", "required parameter 'styles' not set");
    if (!format) return wms_error(r, "MissingDimensionValue", "required parameter 'format' not set");

    int n_width = atoi(width);
    int n_height = atoi(height);

    if (n_width < 1 || n_width > 9999)
    {
        return wms_error(r, "InvalidDimensionValue", "requested width (%d) is not in range 1...9999", n_width);
    }
    if (n_height < 1 || n_height > 9999)
    {
        return wms_error(r, "InvalidDimensionValue", "requested height (%d) is not in range 1...9999", n_height);
    }

    double bboxvals[4];
    int bboxcnt = 0;
    char *dup = apr_pstrdup(r->pool, bbox);
    char *tok = strtok(dup, ",");
    while(tok)
    {
        if (bboxcnt<4) bboxvals[bboxcnt] = strtod(tok, NULL);
        bboxcnt++;
        tok = strtok(NULL, ",");
    }
    if (bboxcnt != 4)
    {
        return wms_error(r, "InvalidDimensionValue", "Invalid BBOX parameter ('%s'). Must contain four comma-separated values.", bbox);
    }

    /*
     * commented out due to client brokenness 
     *
    if (bboxvals[0] > bboxvals[2] ||
        bboxvals[1] > bboxvals[3] ||
        bboxvals[0] < -180 ||
        bboxvals[2] < -180 ||
        bboxvals[1] < -90 ||
        bboxvals[3] < -90 ||
        bboxvals[0] > 180 ||
        bboxvals[2] > 180 ||
        bboxvals[1] > 90 ||
        bboxvals[3] > 90)
    {
        return wms_error(r, "InvalidDimensionValue", "Invalid BBOX parameter ('%s'). Must describe an area on earth, with minlon,minlat,maxlon,maxlat", bbox);
    }
    */

    /** check if given SRS is supported by configuration */
    bool srs_ok = false;

    for (int i=0; i<config->srs_count; i++)
    {
        if (!strcmp(config->srs[i], srs))
        {
            srs_ok= true;
            break;
        }
    }
    if (!srs_ok)
    {
        return wms_error(r, "InvalidSRS", "The given SRS ('%s') is not supported by this WMS service.", srs);
    }

    /*
     * Layer selection is currently disabled. We always return all Mapnik
     * layers. But this could be used to let the client select individual layers. 

    // split up layers into a proper C++ set for easy access
    std::set<std::string> layermap;
    dup = apr_pstrdup(r->pool, layers);
    const char *token = strtok(dup, ",");
    while(token)
    {
        // if one of the layers requested is the "top" layer
        // then kill layer selection and return everything.
        if  (!strcmp(token, config->top_layer_name))
        {
            layermap.clear();
            break;
        }
        layermap.insert(token);
        token = strtok(NULL, ",");
    }
    */

    FILE *f = fopen(config->logfile, "a");
    std::streambuf *old = NULL;
    logbuffer *o = NULL;
    if (f)
    {
        o = new logbuffer(f);
        old = std::clog.rdbuf();
        std::clog.rdbuf(o);
    }

    std::clog << "NEW REQUEST: " << r->the_request << std::endl;

    char *type;
    char *customer_id;

#ifdef USE_KEY_DATABASE
    /*
     * See README for what the key database is about. It is basically
     * an access control scheme where clients have to give a certain
     * key in the URL to be granted access.
     */

    if (config->key_db_file)
    {
        std::clog << "checking key " << r->uri << std::endl;
        char *map_name = apr_pstrdup(r->pool, r->uri+1);
        char *user_key = index(map_name, '/');
        if (!user_key) 
        {
            return http_error(r, HTTP_FORBIDDEN, "No key in URL", exceptions);
        }
    
        *(user_key++) = 0;

        DB *dbp;
        DBT key, data;
        memset(&key, 0, sizeof(key));
        memset(&data, 0, sizeof(data));
        int ret;

        if ((ret = db_create(&dbp, NULL, 0)) != 0) 
        {
            std::clog << "db_create returns error: " << db_strerror(ret) << std::endl;
            return http_error(r, HTTP_INTERNAL_SERVER_ERROR, "database error", exceptions);
        }

        if ((ret = dbp->open(dbp,
            NULL, config->key_db_file, NULL, DB_UNKNOWN, DB_RDONLY, 0)) != 0) {
            dbp->err(dbp, ret, "%s", config->key_db_file);
            std::clog << "db_open returns error: " << db_strerror(ret) << std::endl;
            return http_error(r, HTTP_INTERNAL_SERVER_ERROR, "database error", exceptions);
        }
        key.data = user_key;
        key.size = strlen(user_key);

        if ((ret = dbp->get(dbp, NULL, &key, &data, 0)) != 0)
        {
            std::clog << "db_get returns error for key '" << user_key << "': " << db_strerror(ret) << std::endl;
            return http_error(r, HTTP_FORBIDDEN, "Key not known", exceptions);
        }

        char *colon = index((char *)data.data, ':');
        if (!colon) return http_error(r, HTTP_INTERNAL_SERVER_ERROR, "Bad db content", exceptions);
        *(colon++)=0;
        char *colon2 = index(colon, ':');
        if (!colon2) return http_error(r, HTTP_INTERNAL_SERVER_ERROR, "Bad db content", exceptions);
        *(colon2++) = 0;
        type = apr_pstrdup(r->pool, (char *) data.data);
        customer_id = apr_pstrdup(r->pool, colon2);
        
        char *token = strtok(colon, ",");
        bool found = false;
        while (token)
        {
            if (!strcmp(token, map_name))
            {
                found = true;
                break;
            }
            token = strtok(NULL, ",");
        }   
        
        if (!found)
        {
            std::clog << "requested map name '" << map_name << "' not in allowed list for key '" << user_key << "'" << std::endl;
            return http_error(r, HTTP_FORBIDDEN, "Map not allowed", exceptions);
        }
        
        std::clog << "user id " << customer_id << ", account type is '" << type << "'" << std::endl;

        if (!strcmp(type, "demo"))
        {
            if (config->max_demo_width && n_width > config->max_demo_width)
                return wms_error(r, "InvalidDimensionValue", 
                    "requested width (%d) is not in demo range 1...%d", n_width, config->max_demo_width);
            if (config->max_demo_height && n_height > config->max_demo_height)
                return wms_error(r, "InvalidDimensionValue", 
                    "requested height (%d) is not in demo range 1...%d", n_height, config->max_demo_height);
        }
    }
#endif

    using namespace mapnik;
    Map mymap = *((Map *)config->mapnik_map);

    /* If you have a flaky database connection you might want to set this to > 1. 
     * This is really a brute force way of handling problems. */
    int attempts = 1;

    while(attempts-- > 0)
    {
        try 
        {
            std::clog << "Configuring map parameters" << std::endl;
            char init[256];
            for (char *i = srs; *i; i++) *i=tolower(*i);
            snprintf(init, 256, "+init=%s",srs);
            mymap.set_srs(init);
            mymap.zoomToBox(Envelope<double>(bboxvals[0], bboxvals[1], bboxvals[2], bboxvals[3]));
            mymap.resize(n_width, n_height);

            /*
             * currently disabled. always render all layers.

            // remove those layers that are not in the WMS "layers" 
            // parameter. - unfortunately the map object doesn't
            // allow us to acces the layers non-const, otherweise
            // instead of copying the map object and removing layers,
            // we'd just set them invisible!
            
            std::vector<mapnik::Layer> ml = mymap.layers();
            if (layermap.size()) 
            {
                for (int i=ml.size()-1; i>=0; i--)
                {
                    if (layermap.find(ml[i].name()) == layermap.end())
                    {
                        mymap.removeLayer(i);
                    }
                }
            }
            */

            Image32 buf(mymap.getWidth(),mymap.getHeight());
            agg_renderer<Image32> ren(mymap, buf);

            /*
             * broken clients will request a width/height that does not match, so this log line
             * is worth looking out for. we will fix this to return the right image but quality
             * suffers.
             */
            std::clog << "Start rendering (computed height is " << mymap.getHeight() << ", requested is " << n_height << ")" << std::endl;
            ren.apply();
            std::clog << "Rendering complete" << std::endl;
            
            attempts = 0; // exit loop later

            if (!strcmp(format, "image/png"))
            {
                ap_set_content_type(r, "image/png");
                std::clog << "Start streaming PNG response" << std::endl;
                send_png_response(r, buf, n_height, false);
                std::clog << "PNG response complete" << std::endl;
            }
            else if (!strcmp(format, "image/png8"))
            {
                ap_set_content_type(r, "image/png");
                std::clog << "Start streaming PNG response (palette image)" << std::endl;
                send_png_response(r, buf, n_height, true);
                std::clog << "PNG response complete" << std::endl;
            }
            else if (!strcmp(format, "image/jpeg"))
            {
                ap_set_content_type(r, "image/jpeg");
                std::clog << "Start streaming JPEG response" << std::endl;
                send_jpeg_response(r, buf, n_height);
                std::clog << "JPEG response complete" << std::endl;
            }
            else
            {
                rv = wms_error(r, "InvalidFormat", "Cannot deliver requested data format ('%s')", format);
            }
        }
        catch ( const mapnik::config_error & ex )
        {
            rv = http_error(r, HTTP_INTERNAL_SERVER_ERROR, "mapnik config exception: %s", ex.what());
        }
        catch ( const std::exception & ex )
        {
            rv = http_error(r, HTTP_INTERNAL_SERVER_ERROR, "standard exception (pid %d): %s", getpid(), ex.what());
        }
        catch ( ... )
        {
            rv = http_error(r, HTTP_INTERNAL_SERVER_ERROR, "other exception");
        }
        if (attempts) ap_log_rerror(APLOG_MARK, APLOG_ERR, 0, r, "re-trying...");
    }

    // reset clog stream
    if (old) 
    {
        std::clog.rdbuf(old);
        delete o;
        fclose(f);
    }

    if (config->debug) delete (mapnik::Map *) config->mapnik_map;

    return rv;
}

extern "C" 
{
    /**
     * Called by the Apache hook. Parses request and delegates to
     * proper method.
     */
    int wms_handle(request_rec *r)
    {
        char *args = r->args ? apr_pstrdup(r->pool, r->args) : 0;
        char *amp;
        char *current = args;
        char *equals;
        bool end = (args == 0);

        const char *request = 0;
        const char *service = 0;
        const char *version = 0;

        /* parse URL parameters into variables */
        while (!end)
        {
            amp = index(current, '&');
            if (amp == 0) { amp = current + strlen(current); end = true; }
            *amp = 0;
            equals = index(current, '=');
            if (equals > current)
            {
                *equals++ = 0;
                decode_uri_inplace(current);
                decode_uri_inplace(equals);

                if (!strcasecmp(current, "REQUEST")) request = equals;
                else if (!strcasecmp(current, "SERVICE")) service = equals;
                else if (!strcasecmp(current, "VERSION")) version = equals;
            }
            current = amp + 1;
        }

        if (!request)
        {
            return wms_error(r, "MissingDimensionValue", "Required parameter 'request' not set.");
        }
        else if (!strcmp(request, "GetMap"))
        {
            return wms_getmap(r); 
        }
        else if (!strcmp(request, "GetCapabilities"))
        {
            return wms_getcap(r);
        }
        else
        {
            return wms_error(r, "Request type '%s' is not supported.", request);
        }
    }
}
