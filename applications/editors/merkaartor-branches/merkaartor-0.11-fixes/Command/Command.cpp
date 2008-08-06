#include "Command/Command.h"
#include "Map/MapDocument.h"
#include "Map/MapLayer.h"
#include "Map/MapFeature.h"
#include "Command/DocumentCommands.h"
#include "Command/RoadCommands.h"
#include "Command/TrackSegmentCommands.h"
#include "Command/RelationCommands.h"
#include "Command/TrackPointCommands.h"
#include "Command/FeatureCommands.h"

#include <QAction>
#include <QListWidget>
#include <QUuid>

Command::Command()
	: commandDirtyLevel(0)
{
	description = QApplication::translate("Command", "No description");
	mainFeature = NULL;
}

Command::~Command()
{
}

void Command::setId(const QString& id)
{
	Id = id;
}

const QString& Command::id() const
{
	if (Id == "")
		Id = QUuid::createUuid().toString();
	return Id;
}

QString Command::getDescription() 
{
	return description;
}

void Command::setDescription(QString desc) 
{
	description = desc;
}

MapFeature* Command::getFeature() 
{
	return mainFeature;
}

void Command::setFeature(MapFeature* feat) 
{
	mainFeature = feat;
}

bool Command::buildUndoList(QListWidget* theListWidget)
{
	QListWidgetItem* it = new QListWidgetItem(getDescription(), theListWidget);
	if (getFeature())
		it->setData(Qt::UserRole, getFeature()->id());

	return true;
}

unsigned int Command::incDirtyLevel(MapLayer* aLayer)
{
	aLayer->incDirtyLevel();
	return ++commandDirtyLevel;
}

unsigned int Command::decDirtyLevel(MapLayer* aLayer)
{
	aLayer->decDirtyLevel();
	return commandDirtyLevel;
}

unsigned int Command::getDirtyLevel()
{
	return commandDirtyLevel;
}


// COMMANDLIST

CommandList::CommandList()
: IsUpdateFromOSM(false)
{
}

CommandList::CommandList(QString aDesc, MapFeature* aFeat)
: IsUpdateFromOSM(false)
{
	description = aDesc;
	mainFeature = aFeat;
}

CommandList::~CommandList(void)
{
	for (unsigned int i=0; i<Subs.size(); ++i)
		delete Subs[i];
}

void CommandList::setIsUpdateFromOSM()
{
	IsUpdateFromOSM = true;
}

void CommandList::add(Command* aCommand)
{
	Subs.push_back(aCommand);
}

bool CommandList::empty() const
{
	return Subs.size() == 0;
}

unsigned int CommandList::size()
{
	return Subs.size();
}

void CommandList::redo()
{
	for (unsigned int i=0; i<Subs.size(); ++i)
		Subs[i]->redo();
}

void CommandList::undo()
{
	for (unsigned int i=Subs.size(); i; --i)
		Subs[i-1]->undo();
}

bool CommandList::buildDirtyList(DirtyList& theList)
{
	if (IsUpdateFromOSM)
		Subs.clear();
	for (unsigned int i=0; i<Subs.size();)
	{
		if (Subs[i]->buildDirtyList(theList))
		{
			delete Subs[i];
			Subs.erase(Subs.begin()+i);
		}
		else
			++i;
	}
	return Subs.size() == 0;
}

bool CommandList::toXML(QDomElement& xParent) const
{
	bool OK = true;

	QDomElement e = xParent.ownerDocument().createElement("CommandList");
	xParent.appendChild(e);

	e.setAttribute("xml:id", id());
	e.setAttribute("description", description);
	if (mainFeature) {
		e.setAttribute("feature", mainFeature->id());
		e.setAttribute("featureclass", mainFeature->getClass());
	}

	for (unsigned int i=0; i<Subs.size(); ++i) {
		OK = Subs[i]->toXML(e);
	}

	return OK;
}

CommandList* CommandList::fromXML(MapDocument* d, const QDomElement& e)
{
	CommandList* l = new CommandList();
	l->setId(e.attribute("xml:id"));
	if (e.hasAttribute("description")) 
		l->description = e.attribute("description");
	if (e.hasAttribute("feature")) {
		if (e.attribute("featureclass") == "TrackPoint") {
			l->mainFeature = (MapFeature*) MapFeature::getTrackPointOrCreatePlaceHolder(d, (MapLayer *) d->getDirtyLayer(), NULL, e.attribute("feature"));
		} else 
		if (e.attribute("featureclass") == "Road") {
			l->mainFeature = (MapFeature*) MapFeature::getWayOrCreatePlaceHolder(d, (MapLayer *) d->getDirtyLayer(), NULL, e.attribute("feature"));
		} else 
		if (e.attribute("featureclass") == "Relation") {
			l->mainFeature = (MapFeature*) MapFeature::getRelationOrCreatePlaceHolder(d, (MapLayer *) d->getDirtyLayer(), NULL, e.attribute("feature"));
		}
	}

	QDomElement c = e.firstChildElement();
	while(!c.isNull()) {
		if (c.tagName() == "AddFeatureCommand") {
			AddFeatureCommand* C = AddFeatureCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "MoveTrackPointCommand") {
			MoveTrackPointCommand* C = MoveTrackPointCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "RelationAddFeatureCommand") {
			RelationAddFeatureCommand* C = RelationAddFeatureCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "RelationRemoveFeatureCommand") {
			RelationRemoveFeatureCommand* C = RelationRemoveFeatureCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "RemoveFeatureCommand") {
			RemoveFeatureCommand* C = RemoveFeatureCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "RoadAddTrackPointCommand") {
			RoadAddTrackPointCommand* C = RoadAddTrackPointCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "RoadRemoveTrackPointCommand") {
			RoadRemoveTrackPointCommand* C = RoadRemoveTrackPointCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "TrackSegmentAddTrackPointCommand") {
			TrackSegmentAddTrackPointCommand* C = TrackSegmentAddTrackPointCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "TrackSegmentRemoveTrackPointCommand") {
			TrackSegmentRemoveTrackPointCommand* C = TrackSegmentRemoveTrackPointCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "ClearTagCommand") {
			ClearTagCommand* C = ClearTagCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "ClearTagsCommand") {
			ClearTagsCommand* C = ClearTagsCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "SetTagCommand") {
			SetTagCommand* C = SetTagCommand::fromXML(d, c);
			if (C)
				l->add(C);
		} else
		if (c.tagName() == "CommandList") {
			l->add(CommandList::fromXML(d, c));
		}

		c = c.nextSiblingElement();
	}

	return l;
}


// COMMANDHISTORY

CommandHistory::CommandHistory()
: Index(0), UndoAction(0), RedoAction(0)
{
}

CommandHistory::~CommandHistory()
{
	cleanup();
}

void CommandHistory::cleanup()
{
	//FIXME Is there a point to this?
	//for (unsigned int i=Index; i<Subs.size(); ++i)
	//	Subs[i]->redo();
	for (unsigned int i=0; i<Subs.size(); ++i)
		delete Subs[i];
	Subs.clear();
	Index = 0;
}

void CommandHistory::undo()
{
	if (Index)
	{
		Subs[--Index]->undo();
		updateActions();
	}
}

void CommandHistory::redo()
{
	if (Index < Subs.size())
	{
		Subs[Index++]->redo();
		updateActions();
	}
}

void CommandHistory::add(Command* aCommand)
{
	for (unsigned int i=Index; i<Subs.size(); ++i)
		delete Subs[i];
	Subs.erase(Subs.begin()+Index,Subs.end());
	Subs.push_back(aCommand);
	Index = Subs.size();
	updateActions();
}

void CommandHistory::setActions(QAction* anUndo, QAction* aRedo)
{
	UndoAction = anUndo;
	RedoAction = aRedo;
	updateActions();
}

void CommandHistory::updateActions()
{
	if (UndoAction)
		UndoAction->setEnabled(Index>0);
	if (RedoAction)
		RedoAction->setEnabled(Index<Subs.size());
}

unsigned int CommandHistory::index() const
{
	return Index;
}

unsigned int CommandHistory::buildDirtyList(DirtyList& theList)
{
	for (unsigned int i=0; i<Index;)
		if (Subs[i]->buildDirtyList(theList))
		{
			delete Subs[i];
			Subs.erase(Subs.begin()+i);
			--Index;
		}
		else
			++i;
	return Index;
}

unsigned int CommandHistory::buildUndoList(QListWidget* theList)
{
	for (unsigned int i=0; i<Index; ++i)
		Subs[i]->buildUndoList(theList);

	return Index;
}

bool CommandHistory::toXML(QDomElement& xParent) const
{
	bool OK = true;

	QDomElement e = xParent.namedItem("CommandHistory").toElement();
	if (!e.isNull()) {
		xParent.removeChild(e);
	}

	e = xParent.ownerDocument().createElement("CommandHistory");
	xParent.appendChild(e);

	e.setAttribute("index", QString::number(Index));

	for (unsigned int i=0; i<Subs.size(); ++i) {
		OK = Subs[i]->toXML(e);
	}

	return OK;
}

CommandHistory* CommandHistory::fromXML(MapDocument* d, QDomElement& e)
{
	CommandHistory* h = new CommandHistory();

	QDomElement c = e.firstChildElement();
	while(!c.isNull()) {
		if (c.tagName() == "CommandList") {
			CommandList* l = CommandList::fromXML(d, c);
			h->add(l);
		} else
		if (c.tagName() == "SetTagCommand") {
			SetTagCommand* C = SetTagCommand::fromXML(d, c);
			if (C)
				h->add(C);
		}
		if (c.tagName() == "ClearTagCommand") {
			ClearTagCommand* C = ClearTagCommand::fromXML(d, c);
			if (C)
				h->add(C);
		}
		c = c.nextSiblingElement();
	}
	h->Index = e.attribute("index").toUInt();

	return h;
}


