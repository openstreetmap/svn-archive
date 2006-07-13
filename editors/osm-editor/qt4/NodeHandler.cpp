/*
Copyright (C) 2006 Nick Whitelegg, Hogweed Software, nick@hogweed.org

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
#include "NodeHandler.h"
#include <qstring.h>
#include <qstringlist.h>
#include "Node.h"

#include "Segment.h"

#include <iostream>
#include <utility>
using namespace std;

namespace OpenStreetMap
{

NodeHandler::NodeHandler()
{
	emitdata = NULL;
	finalNode = NULL;
}

void NodeHandler::setEmit(void* rd,QObject* receiver,const char* slot)
{
	cerr<<"********setting emitdata**********" << endl;
	emitdata=rd;
	QObject::connect(this,SIGNAL(newNodeAddedSig(void*)),receiver,slot);
}

void NodeHandler::newNodeAdded(const QByteArray& array, void *node)
{
	Node *n = (Node*)node;
    QString str = array;
    QStringList ids;

    cerr<<"**** HANDLING A NEW NODE RESPONSE ****" << endl;
    ids = str.split("\n");
    if(n)
    {
        cerr<<"NEW UPLOADED NODE IS NOT NULL::SETTING ID"<<endl;
        n->setOSMID(atoi(ids[0].toAscii().constData()));
        cerr<<"DONE."<<endl;
		cerr<<"***********emitting signal**************"<<endl;
		emit newNodeAddedSig(emitdata);

		// 290406 disconnect as soon as the signal is emitted
		discnnect();

		if(n==finalNode)
		{
			finalNode = NULL;
			cerr << "FINAL NODE HAS BEEN ADDED: ID=" << n->getOSMID()<<endl;
			emit finalNodeAddedSig();
		}
    }
    else
        cerr<<"NEW UPLAODED NODE IS NULL" << endl;
}

}
