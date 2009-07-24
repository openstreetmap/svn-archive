#ifndef __SETTINGS_H__
#define __SETTINGS_H__

class Settings
{
    public:
        Settings() {
            store_uncompressed = false;
            srtm_server = 0;
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
        void usage();
    private:
        bool store_uncompressed;
        char *srtm_server;
        char *input;
        char *output;
};

extern Settings global_settings;


#endif