#include <string>
#include <curl/curl.h>

namespace OSM
{

typedef struct
{
	char *data;
	int nbytes;
} Data;

class Client
{
public:
	Client(const std::string& urlbase);
	void setLoginDetails(const std::string& u, const std::string& p);
	std::string grabOSM(const char *apicall, double west, double south,
			double east, double north);
	std::string grabOSM(const char *apicall);
	std::string putToOSM(char* apicall, char* idata);

private:
	std::string urlbase, username, password;

	std::string grab(const char *url);
	std::string doGrab(CURL *curl, const char *url);
	static size_t responseCallback(void *ptr, size_t size, size_t nmemb,
			void *d);
	static size_t putCallback(void *bufptr, size_t size, size_t nitems,
			void *userp);
};

}
