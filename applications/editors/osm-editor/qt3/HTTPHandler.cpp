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
#include "HTTPHandler.h"
#include "qmdcodec.h"
#include "MainWindow2.h"

#include <iostream>
using namespace std;

namespace OpenStreetMap
{

void HTTPHandler::scheduleCommand(const QString& requestType,
					const QString& apicall,const QByteArray& data,
							QObject *receiver,
							const char* callback, 
							void* transferObject,
							const char* errorCallback,
							QObject* errorReceiver)
{
	if (requests.empty())
	{
		if(receiver&&callback)
		{
        	this->disconnect 
				(SIGNAL(responseReceived(const QByteArray&,void*)));
			QObject::connect(this,
						SIGNAL(responseReceived(const QByteArray&,void*)),
						receiver,callback);
			QObject *theErrRec = (errorReceiver) ? errorReceiver: receiver;
			this->disconnect 
					(SIGNAL(errorOccurred(const QString&)));
			QObject::connect(this, SIGNAL(errorOccurred(const QString&)),
						theErrRec,errorCallback);
		}

		sendRequest(requestType,apicall,data);
	}

	requests.push_back
					(Request(requestType,apicall,data,receiver,callback,
								errorReceiver,errorCallback,transferObject));

}

void HTTPHandler::clearRequests()
{
	// First disconnect the slots
	this->disconnect (SIGNAL(responseReceived(const QByteArray&,void*)));
	this->disconnect (SIGNAL(errorOccurred(const QString&)));

	// Now clear the pending requests
	requests.clear();

	makingRequest = false;
}

void HTTPHandler::sendRequest(const QString& requestType, 
							const QString& url,
							const QByteArray& b)
{
	if(!makingRequest)
	{
		makingRequest=true;
		cerr<<"Making request:" 
				<< " host: " << host
				<< " requestType: " <<requestType <<
				"URL :" << url << endl;
		QString s = b;
		if(!s.isNull())
		cerr<<"SENDING REQUEST: "<< s<<endl;
		method = requestType;
		QHttpRequestHeader header(requestType,url);
		header.setValue("Host",host);

		httpError = false;


		if(username!="" && password!="")
		{
			QString userpwd=QCodecs::base64Encode
					(QCString(username+":"+password));
			header.setValue("Authorization","Basic " + userpwd);
		}

		http->setHost(host);

		// 280306 prevent the error being received about 20 million times, one
		// for each HTTP response chunk, presumably
		doEmitErrorOccurred = true;

		//cerr<<"curReqId is  " << curReqId << endl;

		if(b.size())
		{
			//cerr<<"sending request with b " << endl;
			curReqId = http->request(header,b);
		}
		else
		{
			//cerr<<"sending request" << endl;
			curReqId = http->request(header);
		}
		//cerr<<"curReqId returned from request()=" << curReqId << endl;


		//curReqIDs.push_back(curReqId);
	}
	else
	{
		cerr<<"already making a request!" << endl;
		emit errorOccurred
			("Already making a request to the server. Please try again later");
	}
}

void HTTPHandler::responseHeaderReceived(const QHttpResponseHeader& header)
{
//	cerr<<"Status code:" << header.statusCode() << endl;
//	cerr<<"Reason phrase:" << header.reasonPhrase() << endl;
	httpError = header.statusCode()!=200;	
	if(httpError && doEmitErrorOccurred)
	{
		QString err;
		err.sprintf("%d %s",header.statusCode(),
						header.reasonPhrase().ascii());
		//emit httpErrorOccurred(header.statusCode(), header.reasonPhrase());
		cerr << "HTTP ERROR: " << err << endl;
		emit errorOccurred(err);

		// 280306 prevents emitting the error about 20 times. This must be 
		// something to do with chunked http responses, I guess.
		doEmitErrorOccurred = false;
	}
}

void HTTPHandler::responseReceived(int id, bool error)
{
	bool found=false;
	//cerr<<"responseReceived(): id="<<id << ", curReqId=" << curReqId << endl;

	if(id==curReqId)
	{
		makingRequest = false;
		if(!httpError && !error)
		{
			//cerr<<"response: id=" << id << " error=" << error << endl;
			cerr<<"RESPONSE RECEIVED!" << endl;
			cerr << "Size of requests : " << requests.size() << endl;
			if(requests.size())
			{
				//cerr<<"popping the front request"<<endl;

				if(requests[0].callback)
				{
					cerr << "emitting responseReceived" << endl;
					cerr << requests[0].callback << endl;
					emit responseReceived(http->readAll(),requests[0].recObj);
				}
				requests.pop_front();
				//cerr << "length of requests now=" << requests.size() << endl;
				if(requests.size())
				{
					if(requests[0].receiver && requests[0].callback)
					{
						cerr << "RECEIVER AND CALLBACK EXIST" << endl;
        				this->disconnect 
							(SIGNAL(responseReceived(const QByteArray&,void*)));
						QObject::connect(this,
						SIGNAL(responseReceived(const QByteArray&,void*)),
							requests[0].receiver,requests[0].callback);

						QObject *theErrRec = 
								(requests[0].errorReceiver) ? 
								requests[0].errorReceiver: requests[0].receiver;
						this->disconnect 
								(SIGNAL(errorOccurred(const QString&)));
						QObject::connect(this, 
										SIGNAL(errorOccurred(const QString&)),
										theErrRec, requests[0].errorCallback);
					}

					cerr<<"responseReceived(): sending the next request:" <<
								 requests[0].requestType << " " 
								 << requests[0].apicall << endl;

					//cerr<<"length of requests now="<<requests.size()<<endl;
					sendRequest(requests[0].requestType,
							requests[0].apicall,
							requests[0].data);
				}
			}
			else
			{
				emit responseReceived(http->readAll(),NULL);
			}
		}
		else 
		{
			cerr<<"Error encountered" << endl;
			if(!httpError)
			{
				emit errorOccurred
						("An unknown error occurred trying to connect.");
			}

			// Clear all pending requests - the error might affect them
			requests.clear();
		}
		//curReqId = 0;
	}
}

}
