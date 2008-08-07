#ifndef MERKATOR_COMMAND_H_
#define MERKATOR_COMMAND_H_

#include <vector>
#include <QtXml>

class MapDocument;
class MapLayer;
class MapFeature;
class DirtyList;

class QAction;
class QListWidget;
class QProgressDialog;

class Command
{
	public:
		Command();
		virtual ~Command(void) = 0;

		virtual void undo() = 0;
		virtual void redo() = 0;
		virtual bool buildDirtyList(DirtyList& theList) = 0;
		virtual bool buildUndoList(QListWidget* theList);

		void setId(const QString& id);
		const QString& id() const;
		virtual bool toXML(QDomElement& xParent) const = 0;

		virtual QString getDescription();
		virtual void setDescription(QString desc);
		virtual MapFeature* getFeature();
		virtual void setFeature(MapFeature* feat);

		unsigned int incDirtyLevel(MapLayer* aLayer);
		unsigned int decDirtyLevel(MapLayer* aLayer);
		unsigned int getDirtyLevel();

	protected:
		mutable QString Id;
		QString description;
		MapFeature* mainFeature;
		unsigned int commandDirtyLevel;
};

class CommandList : public Command
{
	public:
		CommandList();
		CommandList(QString aDesc, MapFeature* aFeat=NULL);
		virtual ~CommandList();

		virtual void undo();
		virtual void redo();
		bool empty() const;
		unsigned int size();
		void add(Command* aCommand);
		virtual bool buildDirtyList(DirtyList& theList);
		void setIsUpdateFromOSM();

		virtual bool toXML(QDomElement& xParent) const;
		static CommandList* fromXML(MapDocument* d, const QDomElement& e);

	private:
		std::vector<Command*> Subs;
		bool IsUpdateFromOSM;
};

class CommandHistory
{
	public:
		CommandHistory();
		virtual ~CommandHistory();

		void cleanup();
		void undo();
		void redo();
		void add(Command* aCommand);
		void setActions(QAction* anUndo, QAction* aRedo);
		void updateActions();
		unsigned int buildDirtyList(DirtyList& theList);
		unsigned int buildUndoList(QListWidget* theList);
		unsigned int index() const;

		virtual bool toXML(QDomElement& xParent, QProgressDialog & progress) const;
		static CommandHistory* fromXML(MapDocument* d, QDomElement& e, QProgressDialog & progress);

	private:
		std::vector<Command*> Subs;
		unsigned int Index;
		QAction* UndoAction;
		QAction* RedoAction;
};

#endif


