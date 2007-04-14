/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#ifndef REQUESTER_H
#define REQUESTER_H

#include <qhttp.h>
#include <qstring.h>
//#include <q3cstring.h>
#include <vector>
#include <deque>
using std::vector;

#include <iostream>
using namespace std;

namespace OpenStreetMap
{

class MainWindow2;

struct Request
{
	QString requestType;
	QString apicall;
	QByteArray data;
	QObject *receiver, *errorReceiver;
	const char *callback, *errorCallback;
	void *recObj;


	Request(const QString& rt,const QString& ac, const QByteArray& d, 
					QObject *rc, const char *cb, QObject *erc, const char *err,
					void *dt)
		{ requestType=rt;
			apicall=ac; data=d; receiver=rc; callback=cb; 
			errorReceiver=erc; errorCallback = err; recObj=dt; }
};

class HTTPHandler  : public QObject
{
Q_OBJECT

private:
	QHttp* http;
	QString host, method;
	QString username, password;
	bool makingRequest, httpError;
//	vector<int> curReqIDs;
	int curReqId;
	bool locked;
	int respCount;
	std::deque<Request> requests;
	bool doEmitErrorOccurred;

	const char *defaultErrorCallback;

	void sendRequest(const QString&, const QString&,
					const QByteArray& b = QByteArray());

public:
	HTTPHandler(const QString& host)
		{ this->host=host; http=new QHttp; username=password="";
			makingRequest=false; locked=true; respCount=0;
			curReqId=0;
		
		QObject::connect(http,
					SIGNAL(responseHeaderReceived (const QHttpResponseHeader&)),
					this,
					SLOT(responseHeaderReceived(const QHttpResponseHeader&))
					);

		QObject::connect(http,SIGNAL(requestFinished(int,bool)),
					this,SLOT(responseReceived(int,bool)));

		defaultErrorCallback = SLOT(handleNetCommError(const QString&));

		}
	~HTTPHandler()
		{ delete http; }
	void setAuthentication(const QString& u, const QString &p)
		{ username=u; password=p; }
	bool isMakingRequest()
		{ return makingRequest; }
	void lock() { locked=true; }
	void unlock() { locked=false; }

	void scheduleCommand(const QString& requestType,
					const QString& apicall,const QByteArray& data=QByteArray(),
							QObject *receiver=NULL,
							const char* callback=NULL, 
							void* transferObject=NULL,
							const char* errorCallback= 
								SLOT(handleNetCommError(const QString&)),
							QObject* errorReceiver=NULL);
	void scheduleCommand(const QString& requestType,
					const QString& apicall,
							QObject *receiver,
							const char* callback, 
							void* transferObject,
							const char* errorCallback=
								SLOT(handleNetCommError(const QString&)),
								QObject *errorReceiver=NULL)
		{ scheduleCommand(requestType,apicall,QByteArray(),receiver,
								callback,transferObject,errorCallback,
								errorReceiver); }

	void clearRequests();

public slots:
	void responseHeaderReceived(const QHttpResponseHeader&);	
	void responseReceived(int,bool);

signals:
	//void responseReceived(const QByteArray&);
	void responseReceived(const QByteArray&,void*);
	void httpErrorOccurred(int,const QString&);
	void errorOccurred(const QString&);
};

}

#endif
