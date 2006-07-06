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
#ifndef NODEHANDLER_H
#define NODEHANDLER_H

#include <qcstring.h>
#include <qobject.h>
#include "Node.h"

namespace OpenStreetMap
{

class NodeHandler : public QObject
{
Q_OBJECT

private:
	void *emitdata;
	Node *finalNode;	

public:
	NodeHandler();
	void setEmit(void*,QObject*,const char *);
	void discnnect() { this->disconnect(SIGNAL(newNodeAddedSig(void*))); }
	void setFinalNode(Node *n) { finalNode=n; }

public slots:
	void newNodeAdded(const QByteArray&,void*);

signals:
	void newNodeAddedSig(void*);
	void finalNodeAddedSig();
};

}
#endif
