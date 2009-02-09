#ifndef MERKATOR_MAPVIEW_H_
#define MERKATOR_MAPVIEW_H_

#include "Map/Projection.h"

#include <QPixmap>
#include <QWidget>
#include <QShortcut>
#include <QLabel>

class MainWindow;
class MapFeature;
class Road;
class MapDocument;
class PropertiesDock;
class InfoDock;
class MapAdapter;
class Layer;
class LayerManager;
class Interaction;

class MapView :	public QWidget
{
	Q_OBJECT

	public:
		MapView(MainWindow* aMain);
	public:
		~MapView();

		MainWindow* main();
		void setDocument(MapDocument* aDoc);
		MapDocument* document();
		void launch(Interaction* anInteraction);
		Interaction* interaction();
		
		void buildFeatureSet(QRegion invalidRegion, Projection& aProj);
		void drawBackground(QPainter & painter, Projection& aProj);
		void drawFeatures(QPainter & painter, Projection& aProj);

		void panScreen(QPoint delta) ;
		void invalidate(bool updateStaticBuffer, bool updateMap);

		virtual void paintEvent(QPaintEvent* anEvent);
		virtual void mousePressEvent(QMouseEvent * event);
		virtual void mouseReleaseEvent(QMouseEvent * event);
		virtual void mouseMoveEvent(QMouseEvent* event);
		virtual void wheelEvent(QWheelEvent* ev);
		virtual void resizeEvent(QResizeEvent *event);

		Projection& projection();

		PropertiesDock* properties();
		//InfoDock* info();

        LayerManager*	layermanager;
		bool isSelectionLocked();
		void lockSelection();
		void unlockSelection();

		bool toXML(QDomElement xParent);
		void fromXML(const QDomElement e);

	private:
		void sortRenderingPriorityInLayers();
		void drawDownloadAreas(QPainter & painter);
		void drawScale(QPainter & painter);
		void drawGPS(QPainter & painter);
		void updateStaticBackground();
		void updateStaticBuffer();
		void updateLayersImage();
		MainWindow* Main;
		Projection theProjection;
		MapDocument* theDocument;
		Interaction* theInteraction;
		QPixmap* StaticBackground;
		QPixmap* StaticBuffer;
		QPixmap* StaticMap;
		bool StaticBufferUpToDate;
		bool StaticMapUpToDate;
		QPoint thePanDelta, theLastDelta;
		QRegion invalidRegion;
		bool SelectionLocked;
		QLabel* lockIcon;
		QList<MapFeature*> theSnapList;
		QList<MapFeature*> theFeatures;
		QList<Road*> theCoastlines;


		int numImages;

		QShortcut* MoveLeft;
		QShortcut* MoveRight;
		QShortcut* MoveUp;
		QShortcut* MoveDown;

	public slots:
		virtual void on_MoveLeft_activated();
		virtual void on_MoveRight_activated();
		virtual void on_MoveUp_activated();
		virtual void on_MoveDown_activated();
	
	signals:
		void interactionChanged(Interaction* anInteraction);

	protected:
		bool event(QEvent *event);

	private slots:
		void imageRequested();
		void imageReceived();
		void loadingFinished();
		void on_customContextMenuRequested(const QPoint & pos);
};

#endif


