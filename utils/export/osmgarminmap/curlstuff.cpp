#include "curlstuff.h"
#include <stdlib.h>
#include <string.h>

CURL_LOAD_DATA  *grab_osm(const char *urlbase,
					double west,double south,double east,double north,
					const char* username,const char* password)
{
	char url[1024];
	sprintf(url,"%s?bbox=%lf,%lf,%lf,%lf",
                                        urlbase,west,south,east,north);
	return grab_http_response(url,username,password);
}
		 	
CURL_LOAD_DATA *grab_http_response(const char *url,
            const char *username,const char *password)
{
	CURL_LOAD_DATA *data;
    CURL *curl =  curl_easy_init();
	fprintf(stderr, "grab_http_response(): URL=%s\n",url);
	

    if(curl)
	{
		data = Do(curl,url,username,password);
		curl_easy_cleanup(curl);
		return data;
	}
	return NULL;
}
								 	
CURL_LOAD_DATA *Do(CURL *curl,const char *url,
					const char* username,const char* password)
{
     char uname_pwd[1024];
      CURLcode res;
     CURL_LOAD_DATA *data = (CURL_LOAD_DATA *)malloc(sizeof(CURL_LOAD_DATA));
     data->data = NULL;
     data->nbytes = 0;
       
     if(username && password)
     {
           sprintf(uname_pwd,"%s:%s",username,password);
           curl_easy_setopt(curl,CURLOPT_USERPWD,uname_pwd);
     }
	
	curl_easy_setopt(curl,CURLOPT_URL,url);
	curl_easy_setopt(curl,CURLOPT_WRITEFUNCTION,response_callback);
	curl_easy_setopt(curl,CURLOPT_WRITEDATA,data);
	res=curl_easy_perform(curl);
	fprintf(stderr,"Got data.\n");
	return data;
}

size_t response_callback(void *ptr,size_t size,size_t nmemb, void *d)
{
	size_t rsize=size*nmemb;
	CURL_LOAD_DATA *data=(CURL_LOAD_DATA *)d;
	data->data=(char *)realloc(data->data,(data->nbytes+rsize)
                                                          *sizeof(char));
	memcpy(&(data->data[data->nbytes]),ptr,rsize);
	data->nbytes += rsize;
	return rsize;
}
