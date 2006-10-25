#include "RoadCommands.h"

#include "Map/Road.h"
#include "Map/Way.h"
#include "Sync/DirtyList.h"

RoadAddWayCommand::RoadAddWayCommand(Road* R, Way* W)
: theRoad(R), theWay(W)
{
	redo();
}

void RoadAddWayCommand::undo()
{
	theRoad->erase(theWay);
}

void RoadAddWayCommand::redo()
{
	theRoad->add(theWay);
}

bool RoadAddWayCommand::buildDirtyList(DirtyList& theList)
{
	return theList.isChanged(theRoad);
}




