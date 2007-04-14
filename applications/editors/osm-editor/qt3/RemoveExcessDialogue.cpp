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
#include "RemoveExcessDialogue.h"
#include <qlayout.h>
#include <qpushbutton.h>
#include <qlabel.h>

namespace OpenStreetMap 
{

RemoveExcessDialogue::RemoveExcessDialogue(QWidget* parent)
	   :QDialog(parent,"",true)
{
	setCaption("Remove excess track points");
	QVBoxLayout *topL = new QVBoxLayout(this);
	QGridLayout *layout = new QGridLayout(topL,2,2);
	layout->setMargin(10);
	layout->setSpacing(20);
	layout->addWidget(new QLabel("Angle:",this),0,0);
	angleEdit = new QLineEdit(this);
	layout->addWidget(angleEdit,0,1);
	layout->addWidget(new QLabel("Distance:",this),1,0);
	distanceEdit = new QLineEdit(this);
	layout->addWidget(distanceEdit,1,1);
	resetCheckbox = new QCheckBox("Reset to all trackpoints",this);
	topL->addWidget(resetCheckbox);
	QHBoxLayout *okcL=new QHBoxLayout(topL);
	QPushButton *ok=new QPushButton("OK",this),
				*cancel=new QPushButton("Cancel",this);
	okcL->addWidget(ok);
	okcL->addWidget(cancel);
	QObject::connect(ok,SIGNAL(clicked()),this,SLOT(accept()));
	QObject::connect(cancel,SIGNAL(clicked()),this,SLOT(reject()));
}

}
