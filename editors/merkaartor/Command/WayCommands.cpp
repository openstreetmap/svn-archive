#include "Command/WayCommands.h"
#include "Map/Way.h"
#include "Sync/DirtyList.h"

WaySetWidthCommand::WaySetWidthCommand(Way* W, double w)
: theWay(W), OldWidth(W->width()), NewWidth(w)
{
	redo();
}

void WaySetWidthCommand::redo()
{
	theWay->setWidth(NewWidth);
}

void WaySetWidthCommand::undo()
{
	theWay->setWidth(OldWidth);
}

bool WaySetWidthCommand::buildDirtyList(DirtyList &theList)
{
	return theList.update(theWay);
}

/* WAYSETFROMTOCOMMAND */

WaySetFromToCommand::WaySetFromToCommand(Way* W, TrackPoint* aFrom, TrackPoint* aTo)
: theWay(W), OldFrom(W->from()), OldTo(W->to()), NewFrom(aFrom), NewTo(aTo)
{
	redo();
}

void WaySetFromToCommand::undo()
{
	theWay->setFromTo(OldFrom,OldTo);
}

void WaySetFromToCommand::redo()
{
	theWay->setFromTo(NewFrom, NewTo);
}

bool WaySetFromToCommand::buildDirtyList(DirtyList &theList)
{
	return theList.update(theWay);
}



