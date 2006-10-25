#ifndef MERKATOR_DOCUMENTCOMMANDS_H_
#define MERKATOR_DOCUMENTCOMMANDS_H_

#include "Command/Command.h"

class MapDocument;
class MapLayer;
class MapFeature;

class AddFeatureCommand : public Command
{
	public:
		AddFeatureCommand(MapLayer* aDocument, MapFeature* aFeature, bool aUserAdded);

		void undo();
		void redo();
		bool buildDirtyList(DirtyList& theList);

	private:
		MapLayer* theLayer;
		MapFeature* theFeature;
		bool UserAdded;
};

class RemoveFeatureCommand : public Command
{
	public:
		RemoveFeatureCommand(MapDocument* theDocument, MapFeature* aFeature);
		virtual ~RemoveFeatureCommand();

		void undo();
		void redo();
		bool buildDirtyList(DirtyList& theList);

	private:
		MapLayer* theLayer;
		unsigned int Idx;
		MapFeature* theFeature;
};

#endif


