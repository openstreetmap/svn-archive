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
#include "WaypointDialogue.h"
#include <qradiobutton.h>
#include <qvbuttongroup.h>
#include <qlayout.h>
#include <qpushbutton.h>
#include <qlabel.h>
#include "MainWindow2.h"

namespace OpenStreetMap 
{

WaypointDialogue::WaypointDialogue(QWidget* parent,
								 const map<QString,WaypointRep*>& 
								 waypointReps,
								 const QString& caption, 
								 const QString& origType,
								 const QString& origName) : 
				QDialog(parent,"",true)
{
	cerr<<"waypoint dialogue constructor" << endl;
	setCaption(caption);
	QVBoxLayout *topL = new QVBoxLayout(this);
	QGridLayout *layout = new QGridLayout(topL,2,2);
	layout->setMargin(10);
	layout->setSpacing(20);
	typeComboBox = new QComboBox(this);
	int index=0;
	layout->addWidget(new QLabel("Waypoint type:",this),0,0);
	cerr<<"going through waypoint types"<<endl;
	for(std::map<QString,WaypointRep*>::const_iterator i=waypointReps.begin();
		i!=waypointReps.end(); i++)
	{
		cerr<<"first: " << i->first << endl;
		typeComboBox->insertItem (i->second->getImage(),i->first);
		if(i->first == origType)
			typeComboBox->setCurrentItem(index);
		index++;
	}
	layout->addWidget(typeComboBox,0,1);
	layout->addWidget(new QLabel("Name:",this),1,0);
	nameEdit = new QLineEdit(this);
	nameEdit->setText(origName);
	layout->addWidget(nameEdit,1,1);
	QHBoxLayout *okcL=new QHBoxLayout(topL);
	QPushButton *ok=new QPushButton("OK",this),
				*cancel=new QPushButton("Cancel",this);
	okcL->addWidget(ok);
	okcL->addWidget(cancel);
	QObject::connect(ok,SIGNAL(clicked()),this,SLOT(accept()));
	QObject::connect(cancel,SIGNAL(clicked()),this,SLOT(reject()));

}

}
