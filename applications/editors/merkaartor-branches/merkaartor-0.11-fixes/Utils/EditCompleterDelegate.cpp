//
// C++ Implementation: editcompleterdelegate
//
// Description:
//
//
// Author: cbro <cbro@semperpax.com>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "Utils/EditCompleterDelegate.h"
#include "MainWindow.h"
#include "Map/MapDocument.h"
#include "TagModel.h"

#include <QLineEdit>

EditCompleterDelegate::EditCompleterDelegate(QObject* parent): QItemDelegate(parent)
{
}


EditCompleterDelegate::~EditCompleterDelegate()
{
}

QWidget* EditCompleterDelegate::createEditor(QWidget* parent, const QStyleOptionViewItem& /* option */, const QModelIndex& index) const
{
    QCompleter* completer;

    QComboBox *edit = new QComboBox(parent);
    edit->setInsertPolicy(QComboBox::InsertAlphabetically);
    MainWindow* mw = (MainWindow *)(this->parent());
    if (index.column() == 0) {
        completer = new QCompleter(mw->document()->getTagList(), (QObject *)this);
        edit->insertItems(-1, mw->document()->getTagList());
    } else {
        QModelIndex i = index.model()->index(index.row(), 0);
        QString k = index.model()->data(i).toString();
        QStringList sl = mw->document()->getTagValueList(k);
        completer = new QCompleter(sl, (QObject *)this);
        edit->insertItems(-1, mw->document()->getTagValueList(k));
    }
    completer->setCompletionMode(QCompleter::InlineCompletion);
    completer->setModelSorting(QCompleter::CaseInsensitivelySortedModel);
    edit->setCompleter(completer);
    edit->setEditable(true);
    return edit;
}

void EditCompleterDelegate::setEditorData(QWidget* editor, const QModelIndex& index) const
{
    QComboBox *edit = static_cast<QComboBox*>(editor);
    if (index.model()->data(index).toString() != TagModel::newKeyText())
        edit->setEditText(index.model()->data(index).toString());
    else
        edit->clearEditText();
    edit->lineEdit()->selectAll();
}

void EditCompleterDelegate::setModelData(QWidget* editor, QAbstractItemModel* model, const QModelIndex& index) const
{
    QComboBox *edit = static_cast<QComboBox*>(editor);
    if (!edit->currentText().isEmpty())
        model->setData(index, edit->currentText());
}

void EditCompleterDelegate::updateEditorGeometry(QWidget* editor, const QStyleOptionViewItem& option, const QModelIndex& /* index */) const
{
    editor->setGeometry(option.rect);
}

