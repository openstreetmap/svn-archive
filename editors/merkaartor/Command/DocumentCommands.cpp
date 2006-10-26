#include "DocumentCommands.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"
#include "Sync/DirtyList.h"

AddFeatureCommand::AddFeatureCommand(MapLayer* aDocument, MapFeature* aFeature, bool aUserAdded)
: theLayer(aDocument), theFeature(aFeature), UserAdded(aUserAdded)
{
	redo();
}

void AddFeatureCommand::undo()
{
	theLayer->remove(theFeature);
}

void AddFeatureCommand::redo()
{
	theLayer->add(theFeature);
}

bool AddFeatureCommand::buildDirtyList(DirtyList& theList)
{
	if (UserAdded)
		return theList.add(theFeature);
	return false;
}

/* REMOVEFEATURECOMMAND */

RemoveFeatureCommand::RemoveFeatureCommand(MapDocument *theDocument, MapFeature *aFeature)
: theLayer(0), Idx(0), theFeature(aFeature)
{
	for (FeatureIterator it(theDocument); !it.isEnd(); ++it)
	{
		if (it.get() == aFeature)
		{
			theLayer = it.layer();
			Idx = it.index();
			break;
		}
	}
	redo();
}

RemoveFeatureCommand::~RemoveFeatureCommand()
{
	delete theFeature;
}

void RemoveFeatureCommand::redo()
{
	theLayer->remove(theFeature);
}

void RemoveFeatureCommand::undo()
{
	theLayer->add(theFeature,Idx);
}

bool RemoveFeatureCommand::buildDirtyList(DirtyList &theList)
{
	return theList.erase(theFeature);
}
