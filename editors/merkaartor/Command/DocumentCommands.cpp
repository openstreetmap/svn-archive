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
: theLayer(0), Idx(0), theFeature(aFeature), CascadedCleanUp(0), RemoveExecuted(false)
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

RemoveFeatureCommand::RemoveFeatureCommand(MapDocument *theDocument, MapFeature *aFeature, const std::vector<MapFeature*>& Alternatives)
: theLayer(0), Idx(0), theFeature(aFeature), CascadedCleanUp(0), RemoveExecuted(false)
{
	CascadedCleanUp = new CommandList;
	for (FeatureIterator it(theDocument); !it.isEnd(); ++it)
		it.get()->cascadedRemoveIfUsing(theDocument, aFeature, CascadedCleanUp, Alternatives);
	if (CascadedCleanUp->empty())
	{
		delete CascadedCleanUp;
		CascadedCleanUp = 0;
	}
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
	if (CascadedCleanUp)
		CascadedCleanUp->redo();
	theLayer->remove(theFeature);
}

void RemoveFeatureCommand::undo()
{
	if (CascadedCleanUp)
		CascadedCleanUp->undo();
	theLayer->add(theFeature,Idx);
}

bool RemoveFeatureCommand::buildDirtyList(DirtyList &theList)
{
	if (CascadedCleanUp && CascadedCleanUp->buildDirtyList(theList))
	{
		delete CascadedCleanUp;
		CascadedCleanUp = 0;
	}
	if (!RemoveExecuted)
		RemoveExecuted = theList.erase(theFeature);
	return RemoveExecuted && (CascadedCleanUp == 0);
}
