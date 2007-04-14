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
#ifndef REMOVEEXCESSDIALOGUE_H
#define REMOVEEXCESSDIALOGUE_H


#include <iostream>
using namespace std;
#include <qdialog.h>
#include <qlineedit.h>
#include <qcheckbox.h>
#include <cstdlib>

namespace OpenStreetMap 
{


class RemoveExcessDialogue: public QDialog 
{
		
private:
	QLineEdit * distanceEdit, *angleEdit;
	QCheckBox * resetCheckbox;

public:
	RemoveExcessDialogue(QWidget*);
	double getAngle() { return atof(angleEdit->text().ascii()); }
	double getDistance() { 
			return distanceEdit->text().stripWhiteSpace().isEmpty() ? 
			-1 : atof(distanceEdit->text().ascii()); }
	bool reset() { return resetCheckbox->isChecked(); }
};

}
#endif
