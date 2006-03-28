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
#include <qcstring.h>
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
	QObject *receiver;
	const char *callback;
	void *recObj;

	Request(const QString& rt,const QString& ac, const QByteArray& d, 
					QObject *rc, const char *cb,
					void *dt)
		{ requestType=rt;
			apicall=ac; data=d; receiver=rc; callback=cb; recObj=dt; }
};

class HTTPHandler  : public QObject
{
Q_OBJECT

private:
	QHttp* http;
	QString host, method;
	QString username, password;
	bool makingRequest, httpError;
	vector<int> curReqIDs;
	bool locked;
	int respCount;
	std::deque<Request> requests;
	bool doEmitErrorOccurred;

public:
	HTTPHandler(const QString& host)
		{ this->host=host; http=new QHttp; username=password="";
			makingRequest=false; locked=true; respCount=0; }
	~HTTPHandler()
		{ delete http; }
	void setAuthentication(const QString& u, const QString &p)
		{ username=u; password=p; }
	void sendRequest(const QString&, const QString&,
					const QByteArray& b = QByteArray());
	bool isMakingRequest()
		{ return makingRequest; }
	void lock() { locked=true; }
	void unlock() { locked=false; }

	void scheduleCommand(const QString& requestType,
					const QString& apicall,const QByteArray& data=QByteArray(),
							QObject *receiver=NULL,
							const char* callback=NULL, 
							void* transferObject=NULL)
	{
		if (requests.empty())
		{
			if(receiver&&callback)
			{
				QObject::connect(this,
						SIGNAL(responseReceived(const QByteArray&,void*)),
						receiver,callback);
			}

			sendRequest(requestType,apicall,data);
		}

		cerr<<"scheduleCommand(): pushing back a request:" <<
								 requestType << " " << apicall << endl;

		cerr << "before push_back: length of requests now=" << requests.size() << endl;
		requests.push_back
					(Request(requestType,apicall,data,receiver,callback,
								transferObject));
		cerr << "after push_back: length of requests now=" << requests.size() << endl;

	}

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
