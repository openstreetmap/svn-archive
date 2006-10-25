#include "TagModel.h"
#include "MainWindow.h"
#include "Command/FeatureCommands.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"

TagModel::TagModel(MainWindow* aMain)
: Main(aMain), theFeature(0)
{
}

TagModel::~TagModel(void)
{
}

void TagModel::setFeature(MapFeature* aF)
{
	theFeature = aF;
}

int TagModel::rowCount(const QModelIndex &) const
{
	if (!theFeature) return 0;
	return theFeature->tagSize()+1;
}

int TagModel::columnCount(const QModelIndex &) const
{
	return 2;
}

QVariant TagModel::data(const QModelIndex &index, int role) const
{
	if (!theFeature)
		return QVariant();
	if (!index.isValid())
		return QVariant();
	if (index.row() > theFeature->tagSize())
		return QVariant();
	if (role == Qt::DisplayRole)
	{
		if (index.row() == theFeature->tagSize())
		{
			if (index.column() == 0)
				return "Edit this to add...";
			else
				return "";
		}
		else
		{
			if (index.column() == 0)
				return theFeature->tagKey(index.row());
			else
				return theFeature->tagValue(index.row());
		}
	}
	return QVariant();
}

QVariant TagModel::headerData(int section, Qt::Orientation orientation, int role) const
{
	if (role != Qt::DisplayRole)
		return QVariant();
	if (orientation == Qt::Horizontal)
	{
		if (section == 0)
			return "Key";
		else
			return "Value";
	}
	return QVariant();
}

Qt::ItemFlags TagModel::flags(const QModelIndex &index) const
{
	if (!index.isValid())
		return Qt::ItemIsEnabled;
	return QAbstractTableModel::flags(index) | Qt::ItemIsEditable;
}

bool TagModel::setData(const QModelIndex &index, const QVariant &value, int role)
{
	if (!theFeature) return false;
	if (index.isValid() && role == Qt::EditRole)
	{
		if (index.row() == theFeature->tagSize())
		{
			if (index.column() == 0)
			{
				beginInsertRows(QModelIndex(), theFeature->tagSize()+1, theFeature->tagSize()+2);
				Main->document()->history().add(
					new SetTagCommand(theFeature,value.toString(),""));
				endInsertRows();
				theFeature->setLastUpdated(MapFeature::User);
			}
			else
				return false;
		}
		else
		{
			if (index.column() == 0)
				Main->document()->history().add(
					new SetTagCommand(theFeature,index.row(),value.toString(),theFeature->tagValue(index.row())));
			else
				Main->document()->history().add(
					new SetTagCommand(theFeature,index.row(),theFeature->tagKey(index.row()),value.toString()));
			theFeature->setLastUpdated(MapFeature::User);
		}
		emit dataChanged(index, index);
		return true;
	}
	return false;
}





