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
#include "WayDialogue.h"
#include <qlayout.h>
#include <qpushbutton.h>
#include <qlabel.h>
#include "MainWindow2.h"

namespace OpenStreetMap 
{

WayDialogue::WayDialogue(QWidget* parent,
								 const vector<QString>& segTypes,
								 const vector<QString>& areaTypes,
								 const QString& name,const QString& type,
								 const QString& ref) : 
				QDialog(parent,"",true)
{
	setCaption("Enter way details");
	this->areaTypes = areaTypes;
	this->wayTypes = segTypes;
	area = false;

	int itemIndex=0;

	QVBoxLayout *topL = new QVBoxLayout(this);
	QGridLayout *layout = new QGridLayout(topL,4,2);
	layout->setMargin(10);
	layout->setSpacing(20);

	waComboBox = new QComboBox(this);
	layout->addWidget(new QLabel("Way or area:",this),0,0);
	waComboBox->insertItem("way");
	waComboBox->insertItem("area");
	waComboBox->setCurrentItem(0);
	layout->addWidget(waComboBox,0,1);
	
	typeComboBox = new QComboBox(this);
	layout->addWidget(new QLabel("Type:",this),1,0);
	for(int count=0; count<segTypes.size(); count++)
	{
		typeComboBox->insertItem (segTypes[count]);
		if(segTypes[count]==type)
			itemIndex = count;
	}
	typeComboBox->setCurrentItem(itemIndex);

	layout->addWidget(typeComboBox,1,1);
	layout->addWidget(new QLabel("Name:",this),2,0);
	nameEdit = new QLineEdit(this);
	nameEdit->setText(name);
	layout->addWidget(nameEdit,2,1);
	layout->addWidget(new QLabel("Number:",this),3,0);
	refEdit = new QLineEdit(this);
	refEdit->setText(ref);
	layout->addWidget(refEdit,3,1);
	QHBoxLayout *okcL=new QHBoxLayout(topL);
	QPushButton *ok=new QPushButton("OK",this),
				*cancel=new QPushButton("Cancel",this);
	okcL->addWidget(ok);
	okcL->addWidget(cancel);
	QObject::connect(ok,SIGNAL(clicked()),this,SLOT(accept()));
	QObject::connect(cancel,SIGNAL(clicked()),this,SLOT(reject()));
	QObject::connect(waComboBox,SIGNAL(activated(const QString&)),
						this,SLOT(changeWA(const QString&)));
}

void WayDialogue::changeWA(const QString& wayOrArea)
{
	typeComboBox->clear();
	if(wayOrArea=="way")
	{
		for(int count=0; count<wayTypes.size(); count++)
		{
			typeComboBox->insertItem (wayTypes[count]);
		}
		area=false;
	}
	else
	{
		for(int count=0; count<areaTypes.size(); count++)
		{
			typeComboBox->insertItem (areaTypes[count]);
		}
		area=true;
	}
}

}
