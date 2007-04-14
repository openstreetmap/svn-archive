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
#ifndef WAYDIALOGUE_H
#define WAYDIALOGUE_H

#include <vector>
using std::vector;

#include <qdialog.h>
#include <qcombobox.h>
#include <qlineedit.h>
#include <qtextedit.h>

namespace OpenStreetMap 
{

class WayDialogue: public QDialog 
{

Q_OBJECT

private:
	QComboBox * typeComboBox, *waComboBox;
	QLineEdit * nameEdit, *refEdit;
	QTextEdit *noteText;
	vector<QString> wayTypes, areaTypes;
	bool area;

public:
	WayDialogue(QWidget* parent, const vector<QString>&,const vector<QString>&,
					const QString& name="",const QString& type="",
					const QString& ref="");

	QString getType() { return typeComboBox->currentText(); }
	QString getName() { return nameEdit->text(); }
	QString getRef() { return refEdit->text(); }
	QString getNote() { return noteText->toPlainText(); }
	void setNote(const QString& note) { noteText->setPlainText(note); }
	bool isArea();

};

}
#endif /* WAYDIALOGUE_H */
