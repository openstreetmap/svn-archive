/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Handles command line parsing and stores the options.
  */
#ifndef __SETTINGS_H__
#define __SETTINGS_H__

/** Stores all settings. */
class Settings
{
    public:
        Settings() {
            store_uncompressed = false;
            srtm_server = 0;
            input = 0;
            output = 0;
        };
        
        void parseSettings(int argc, char **argv);
        
        /** Get input file.
          *
          * If none is given read from stdin. */
        const char *getInput() const {
            if (input) return input;
            return "/dev/stdin";
        }

        /** Get output file.
          *
          * If none is given write to stdout. */
        const char *getOutput() const {
            if (output) return output;
            return "/dev/stdout";
        }

        /** Get URL of SRTM server. */
        const char *getSrtmServer() const {
            if (srtm_server) return srtm_server;
            return "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
        }

        /** Get cache dir. */
        const char *getCacheDir() const {
            if (cache_dir) return cache_dir;
            return "cache";
        }

        /** Should the uncompressed data also be stored on disk? */
        bool getStoreUncompressed() const { return store_uncompressed; }
        
        void usage();
    private:
        bool store_uncompressed;
        char *srtm_server;
        char *input;
        char *output;
        char *cache_dir;
};

extern Settings global_settings;


#endif