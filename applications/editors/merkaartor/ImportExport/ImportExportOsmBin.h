//
// C++ Interface: ImportExportOsmBin
//
// Description: 
//
//
// Author: cbro <cbro@semperpax.com>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#ifndef IMPORTEXPORTOSMBIN_H
#define IMPORTEXPORTOSMBIN_H

#include <ImportExport/IImportExport.h>

#define TILE_WIDTH (int(UINT_MAX/40000))
#define REGION_WIDTH (int(UINT_MAX/500))
#define NUM_TILES (int(UINT_MAX/TILE_WIDTH))
#define NUM_REGIONS (int(UINT_MAX/REGION_WIDTH))

/**
	@author cbro <cbro@semperpax.com>
*/
class ImportExportOsmBin : public IImportExport
{
	friend class OsbMapLayer;

public:
    ImportExportOsmBin(MapDocument* doc);

    ~ImportExportOsmBin();

	// import the  input
	virtual bool import(MapLayer* aLayer);

	//export
	virtual bool export_(const QVector<MapFeature *>& featList);

protected:
//	void addTileIndex(MapFeature* F, qint64 pos);
	void addTileIndex(MapFeature* F);
	void tagsToBinary(MapFeature* F, QDataStream& ds);
	void tagsFromBinary(MapFeature* F, QDataStream& ds);
	void tagsPopularity(MapFeature * F);
	
	bool prepare();
	bool writeHeader(QDataStream& ds);
	bool writeIndex(QDataStream& ds);
	bool writeTagLists(QDataStream& ds);
	//bool writeNodes(QDataStream& ds);
	//bool writeRoads(QDataStream& ds);
	//bool writeRelations(QDataStream& ds);
	bool writeFeatures(QList<MapFeature*>, QDataStream& ds);

	bool readHeader(QDataStream& ds);
	bool readRegionToc(QDataStream& ds);
	bool readPopularTagLists(QDataStream& ds);
	//bool readTagLists(QDataStream& ds);
	//bool readNodes(QDataStream& ds, OsbMapLayer* aLayer);
	//bool readRoads(QDataStream& ds, OsbMapLayer* aLayer);
	//bool readRelations(QDataStream& ds, OsbMapLayer* aLayer);

	bool loadRegion(qint32 rg);
	bool loadTile(qint32 tile, MapDocument* d, OsbMapLayer* theLayer);
	bool clearTile(qint32 tile, MapDocument* d, OsbMapLayer* theLayer);
	MapFeature* getFeature(MapDocument* d, OsbMapLayer* theLayer, quint64 ref);
	MapFeature* getFeature(MapDocument* d, OsbMapLayer* theLayer);

protected:
	QMap <QString, qint32> keyPopularity;
	QMap <QString, qint32> valuePopularity;
	QMap <quint64, QString> keyTable;
	QMap <quint64, QString> valueTable;

	QMap< qint32, quint64 > theRegionToc;
	QMap< qint32, quint64 > theTileToc;

	QMap< qint32, QList< QPair < qint32, quint64 > > > theRegionIndex;
	QMap< qint32, QList<MapFeature*> > theTileIndex;
	QMap< qint32, QList<MapFeature*> > theTileNodesIndex;
	QMap< qint32, QList<MapFeature*> > theTileRoadsIndex;
	QMap< qint32, QList<MapFeature*> > theTileRelationsIndex;

	QHash <QString, quint64> theFeatureIndex;

	QMap<quint64, TrackPoint*> theNodes;
	QMap<quint64,Road*> theRoads;
	QMap<quint64, Relation*> theRelations;

	QMap <QString, quint64> theTagKeysIndex;
	QMap <QString, quint64> theTagValuesIndex;

	qint64 tocPos;
	qint64 tagKeysPos;
	qint64 tagValuesPos;
};

/*
class OsbRegion
{
public:
	OsbRegion(qint32 id);
	addFeature(MapFeature* F);
	bool read();

protected:
	qint32	rgId;
	QIODevice* Device;
	QDataStream ds;

	QMap< qint32, QList<quint64> > theTileIndex;

	QHash <QString, quint64> theFeatureIndex;

	QMap<quint64, TrackPoint*> theNodes;
	QMap<quint64,Road*> theRoads;
	QMap<quint64, Relation*> theRelations;

	QList <quint64> theTagKeysIndex;
	QList <quint64> theTagValuesIndex;

	qint64 tagKeysPos;
	qint64 tagValuesPos;

};

*/
#endif
