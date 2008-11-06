#include <string>
#include <curl/curl.h>

namespace OSM
{

/**
 * I/O. Handles reading from and writing to OSM servers
 */
class Client
{
public:
	/**
	 * Constructor
	 * @param urlbase Server URL base (parameters are added automatically)
	 */
	Client(const std::string& urlbase);

	/**
	 * Change user/password used to login to the OSM server
	 * @param user Username for HTTP authentification
	 * @param pass Password for HTTP authentification
	 */
	void setLoginDetails(const std::string& user, const std::string& pass);

	/**
	 * Access data of the given type for the given bounding box
	 * @param apicall API string to identify wanted objects - e.g. "map" or "node" or "way" or "relation" or "*"
	 * @param west Longitude of the left (westernmost) side of the bounding box
	 * @param south Latitude of the bottom (southernmost) side of the bounding box
	 * @param east Longitude of the right (easternmost) side of the bounding box
	 * @param north Latitude of the top (northernmost) side of the bounding box
	 * @return Server output, OSM XML if all goes well
	 */
	std::string grabOSM(const char *apicall, double west, double south,
			double east, double north);

	/**
	 * Access data from the OSM server
	 * @param apicall Second part of the url. The URL used will be urlbase/apicall,
	 * where urlbase is the parameter set in the constructor
	 * @return Server output, OSM XML if all goes well
	 */
	std::string grabOSM(const char *apicall);

	/**
	 * Write data to the OSM server
	 * @param apicall Second part of the url. The URL used will be urlbase/apicall,
	 * where urlbase is the parameter set in the constructor
	 * @param data Data to post to the OSM server
	 * @return Server output
	 */
	std::string putToOSM(char* apicall, char* data);

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
