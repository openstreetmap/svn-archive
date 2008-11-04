#include "Client.h"

#include <cstdio>
#include <cstdlib>
#include <cstring>


/*
Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

*/


/* CREDIT: The approach taken (of a struct storing the read-in-data and its
* size) is inspired by the callback example on the cURL web site. */

namespace OSM
{

Client::Client(const std::string& urlbase)
{
	this->urlbase=urlbase;
	username="";
	password="";
}

void Client::setLoginDetails(const std::string& u, const std::string& p)
{
	username=u;
	password=p;
}

std::string  Client::grabOSM(const char *apicall,
					double west,double south,double east,double north)
{
	char url[1024];
	sprintf(url,"%s/%s?bbox=%lf,%lf,%lf,%lf",
					urlbase.c_str(),apicall,west,south,east,north);
	return grab(url);
}

std::string  Client::grabOSM(const char *apicall)
{
	char url[1024];
	sprintf(url,"%s/%s", urlbase.c_str(),apicall);
	return grab(url);
}

std::string  Client::grab(const char *url)
{
	std::string data;

	fprintf(stderr, "grab(): URL=%s\n",url);
	CURL *curl =  curl_easy_init();


	if(curl)
	{
		data = doGrab(curl,url);
		curl_easy_cleanup(curl);
		return data;
	}
	return "";
}

std::string  Client::doGrab(CURL *curl,const char *url)
{
	char uname_pwd[1024];
	CURLcode res;
	std::string returned="";

	if(username!="" && password!="")
	{
		Data *data = (Data *)malloc(sizeof(Data));
		data->data = NULL;
		data->nbytes = 0;

		sprintf(uname_pwd,"%s:%s",username.c_str(),password.c_str());
		curl_easy_setopt(curl,CURLOPT_USERPWD,uname_pwd);

		curl_easy_setopt(curl,CURLOPT_URL,url);
		curl_easy_setopt(curl,CURLOPT_WRITEFUNCTION,Client::responseCallback);
		curl_easy_setopt(curl,CURLOPT_WRITEDATA,data);

		res=curl_easy_perform(curl);

		fprintf(stderr,"Got data.\n");

		data->data=(char *)realloc(data->data,(data->nbytes+1)*sizeof(char));
		data->data[data->nbytes] = '\0';
		returned = data->data;
		free(data->data);
		free(data);
	}

	return returned;
}

size_t Client::responseCallback(void *ptr,size_t size,size_t nmemb,void *d)
{
	size_t rsize=size*nmemb;
	Data *data=(Data *)d;
	data->data=(char *)realloc(data->data,(data->nbytes+rsize)
                                *sizeof(char));
	memcpy(&(data->data[data->nbytes]),ptr,rsize);
	data->nbytes += rsize;
 	//      fprintf(stderr,"data->nbytes is %d\n", data->nbytes);
	return rsize;
}

size_t Client::putCallback
		(void *bufptr,size_t size, size_t nitems, void *userp)
{
	char *p1= (char*)bufptr, *p2=(char*)userp;
 	//      fprintf(stderr,"callback: userp=%s\n",(char *)userp);
	strcpy(p1,p2);
	return strlen(p1)+1;
}

std::string Client::putToOSM(char* apicall, char* idata)
{
	CURL *curl;

	char uname_pwd[1024];
	char url[1024];
	std::string returned="";
	if(username!="" && password!="")
	{
		sprintf(uname_pwd,"%s:%s",username.c_str(),password.c_str());
		sprintf(url,"%s/%s",urlbase.c_str(),apicall);

		Data *odata = (Data *)malloc(sizeof(Data));
		odata->data = NULL;
		odata->nbytes = 0;

		curl_global_init(CURL_GLOBAL_ALL);
		curl = curl_easy_init();

		if(curl)
		{
			curl_easy_setopt(curl,CURLOPT_READFUNCTION,Client::putCallback);
			curl_easy_setopt(curl,CURLOPT_UPLOAD, true);
			curl_easy_setopt(curl,CURLOPT_PUT, true);
			curl_easy_setopt(curl,CURLOPT_URL, url);
			curl_easy_setopt(curl,CURLOPT_USERPWD,uname_pwd);
			curl_easy_setopt(curl,CURLOPT_READDATA,idata);
			curl_easy_setopt(curl,CURLOPT_INFILESIZE,(long)(strlen(idata)+1));
			curl_easy_setopt(curl,CURLOPT_WRITEFUNCTION,Client::putCallback);
			curl_easy_setopt(curl,CURLOPT_WRITEDATA,odata);
			curl_easy_perform(curl);
			curl_easy_cleanup(curl);
			odata->data=(char *)realloc
				(odata->data,(odata->nbytes+1)*sizeof(char));
			odata->data[odata->nbytes] = '\0';
			returned = odata->data;
			free(odata->data);
		}

		curl_global_cleanup();
		free(odata);
	}
	return returned;
}

}
