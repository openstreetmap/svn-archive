#ifndef __SETTINGS_H__
#define __SETTINGS_H__

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
        const char *getInput() const {
            if (input) return input;
            return "/dev/stdin";
        }
        const char *getOutput() const {
            if (output) return output;
            return "/dev/stdout";
        }
        const char *getSrtmServer() const {
            if (srtm_server) return srtm_server;
            return "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
        }
        const char *getCacheDir() const {
            if (cache_dir) return cache_dir;
            return "cache";
        }
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