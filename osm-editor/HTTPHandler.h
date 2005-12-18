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

namespace OpenStreetMap
{

class HTTPHandler  : public QObject
{
Q_OBJECT

private:
	QHttp* http;
	QString host;
	QString username, password;
	bool makingRequest;

public:
	HTTPHandler(const QString& host)
		{ this->host=host; http=new QHttp; username=password="";
			makingRequest=false;}
	~HTTPHandler()
		{ delete http; }
	void setAuthorisation(const QString& u, const QString &p)
		{ username=u; password=p; }
	void sendRequest(const QString&, const QString&);

public slots:
	void responseHeaderReceived(const QHttpResponseHeader&);	
	void responseReceived(int,bool);
};

}

#endif
