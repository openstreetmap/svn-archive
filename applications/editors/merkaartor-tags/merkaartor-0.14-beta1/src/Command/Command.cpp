#include "Command/Command.h"
#include "Maps/MapDocument.h"
#include "Maps/MapLayer.h"
#include "Maps/MapFeature.h"
#include "Command/DocumentCommands.h"
#include "Command/RoadCommands.h"
#include "Command/TrackSegmentCommands.h"
#include "Command/RelationCommands.h"
#include "Command/TrackPointCommands.h"
#include "Command/FeatureCommands.h"

#include <QAction>
#include <QListWidget>
#include <QUuid>
#include <QProgressDialog>

#include <algorithm>
#include <utility>
#include <QList>

Command::Command(MapFeature* aF)
	: mainFeature(aF), commandDirtyLevel(0), oldCreated(""), isUndone(false), postUploadCommand(0)
{
	description = QApplication::translate("Command", "No description");
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

int Command::incDirtyLevel(MapLayer* aLayer)
{
	aLayer->incDirtyLevel();
	return ++commandDirtyLevel;
}

int Command::decDirtyLevel(MapLayer* aLayer)
{
	aLayer->decDirtyLevel();
	return commandDirtyLevel;
}

int Command::getDirtyLevel()
{
	return commandDirtyLevel;
}

void Command::undo()
{
	if (mainFeature && M_PREFS->apiVersionNum() < 0.6) {
		mainFeature->setUploaded(wasUploaded);
		if (oldCreated != TAG_UNDEF_VALUE)
			mainFeature->setTag("created_by", oldCreated);
		else
			mainFeature->clearTag("created_by");
	}
	isUndone = true;
	mainFeature->notifyChanges();
}

void Command::redo()
{
	if (mainFeature && M_PREFS->apiVersionNum() < 0.6) {
		oldCreated = mainFeature->tagValue("created_by", TAG_UNDEF_VALUE);
		mainFeature->setTag("created_by",QString("Merkaartor v%1%2").arg(STRINGIFY(VERSION)).arg(STRINGIFY(REVISION)));
		wasUploaded = mainFeature->isUploaded();
		mainFeature->setUploaded(false);
	}
	isUndone = false;
	mainFeature->notifyChanges();
}

bool Command::toXML(QDomElement& xParent) const
{
	bool OK = true;

	if (mainFeature) {
		QDomElement e = xParent.ownerDocument().createElement("Command");
		xParent.appendChild(e);

		e.setAttribute("xml:id", id());
		e.setAttribute("feature", mainFeature->xmlId());
		e.setAttribute("oldCreated", oldCreated);
		if (isUndone)
			e.setAttribute("undone", "true");
	}

	return OK;
}

void Command::fromXML(MapDocument* d, const QDomElement& e, Command* C)
{
	QDomElement c = e.firstChildElement();
	while(!c.isNull()) {
		if (c.tagName() == "Command") {
			MapFeature* F;
			if (!(F = d->getFeature(c.attribute("feature"), false)))
				return;

			C->setId(c.attribute("xml:id"));
			if (c.hasAttribute("oldCreated"))
				C->oldCreated = c.attribute("oldCreated");
			if (c.hasAttribute("undone"))
				C->isUndone = (c.attribute("undone") == "true" ? true : false);
			C->mainFeature = F;
		}
		c = c.nextSiblingElement();
	}
}

// COMMANDLIST

CommandList::CommandList()
: Command(0), Size(0)
{
}

CommandList::CommandList(QString aDesc, MapFeature* aFeat)
: Command(0), Size(0)
{
	description = aDesc;
	mainFeature = aFeat;
}

CommandList::~CommandList(void)
{
	for (int i=0; i<Subs.size(); ++i)
		delete Subs[i];
}

void CommandList::add(Command* aCommand)
{
	Subs.push_back(aCommand);
	Size++;
}

bool CommandList::empty() const
{
	return Size == 0;
}

int CommandList::size()
{
	return Size;
}

void CommandList::redo()
{
	for (int i=0; i<Size; ++i)
		Subs[i]->redo();
}

void CommandList::undo()
{
	for (int i=Size; i; --i)
		Subs[i-1]->undo();
}

bool CommandList::buildDirtyList(DirtyList& theList)
{
	for (int i=0; i<Size;)
	{
		if (Subs[i]->buildDirtyList(theList))
		{
			//delete Subs[i];
			//Subs.erase(Subs.begin()+i);
			std::rotate(Subs.begin()+i,Subs.begin()+i+1,Subs.end());
			--Size;
		}
		else
			++i;
	}
	
	return Size == 0;
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

	for (int i=0; i<Size; ++i) {
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
			l->mainFeature = (MapFeature*) MapFeature::getTrackPointOrCreatePlaceHolder(d, (MapLayer *) d->getDirtyOrOriginLayer(), e.attribute("feature"));
		} else 
		if (e.attribute("featureclass") == "Road") {
			l->mainFeature = (MapFeature*) MapFeature::getWayOrCreatePlaceHolder(d, (MapLayer *) d->getDirtyOrOriginLayer(), e.attribute("feature"));
		} else 
		if (e.attribute("featureclass") == "Relation") {
			l->mainFeature = (MapFeature*) MapFeature::getRelationOrCreatePlaceHolder(d, (MapLayer *) d->getDirtyOrOriginLayer(), e.attribute("feature"));
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
: Index(0), Size(0), UndoAction(0), RedoAction(0), UploadAction(0)
{
}

CommandHistory::~CommandHistory()
{
	cleanup();
}

void CommandHistory::cleanup()
{
	//FIXME Is there a point to this?
	//for (int i=Index; i<Subs.size(); ++i)
	//	Subs[i]->redo();
	for (int i=0; i<Subs.size(); ++i)
		delete Subs[i];
	Subs.clear();
	Index = 0;
	Size = 0;
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
	if (Index < Size)
	{
		Subs[Index++]->redo();
		updateActions();
	}
}

void CommandHistory::add(Command* aCommand)
{
	//for (int i=Index; i<Subs.size(); ++i)
	//	delete Subs[i];
	//Subs.erase(Subs.begin()+Index,Subs.end());
	//Subs.push_back(aCommand);
	//Index = Subs.size();
	Subs.insert(Subs.begin()+Index, aCommand);
	Index++;
	Size = Index;
	updateActions();
}

void CommandHistory::setActions(QAction* anUndo, QAction* aRedo, QAction* anUploadAction)
{
	UndoAction = anUndo;
	RedoAction = aRedo;
	UploadAction = anUploadAction;
	updateActions();
}

void CommandHistory::updateActions()
{
	if (UndoAction)
		UndoAction->setEnabled(Index>0);
	if (RedoAction)
		RedoAction->setEnabled(Index<Size);
	if (UploadAction && !M_PREFS->getOfflineMode())
		UploadAction->setEnabled(Subs.size());
}

int CommandHistory::index() const
{
	return Index;
}

int CommandHistory::buildDirtyList(DirtyList& theList)
{
	for (int i=0; i<Subs.size();)
		if (Subs[i]->buildDirtyList(theList))
		{
			//delete Subs[i];
			//Subs.erase(Subs.begin()+i);
			std::rotate(Subs.begin()+i,Subs.begin()+i+1,Subs.end());
			--Index;
			--Size;
			if (!Size)
				cleanup();
		}
		else
			++i;

	return Index;
}

int CommandHistory::buildUndoList(QListWidget* theList)
{
	for (int i=0; i<Index; ++i)
		Subs[i]->buildUndoList(theList);

	return Index;
}

bool CommandHistory::toXML(QDomElement& xParent, QProgressDialog & /*progress*/) const
{
	bool OK = true;

	QDomElement e = xParent.namedItem("CommandHistory").toElement();
	if (!e.isNull()) {
		xParent.removeChild(e);
	}

	e = xParent.ownerDocument().createElement("CommandHistory");
	xParent.appendChild(e);

	e.setAttribute("index", QString::number(Index));

	for (int i=0; i<Size; ++i) {
		OK = Subs[i]->toXML(e);
	}

	return OK;
}

CommandHistory* CommandHistory::fromXML(MapDocument* d, QDomElement& e, QProgressDialog & progress)
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
		if (c.tagName() == "MoveTrackPointCommand") {
			MoveTrackPointCommand* C = MoveTrackPointCommand::fromXML(d, c);
			if (C)
				h->add(C);
		}

		if (progress.wasCanceled())
			break;

		c = c.nextSiblingElement();
	}
	h->Index = e.attribute("index").toUInt();

	return h;
}


