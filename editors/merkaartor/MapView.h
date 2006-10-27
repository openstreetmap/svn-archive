#ifndef MERKATOR_MAPVIEW_H_
#define MERKATOR_MAPVIEW_H_

#include "Map/Projection.h"

#include <QtGui/QWidget>

class Interaction;
class MainWindow;
class MapDocument;
class PropertiesDock;


class MapView :	public QWidget
{
	public:
		MapView(MainWindow* aMain);
	public:
		virtual ~MapView(void);

		MainWindow* main();
		void setDocument(MapDocument* aDoc);
		MapDocument* document();
		void launch(Interaction* anInteraction);
		Interaction* interaction();

		virtual void paintEvent(QPaintEvent* anEvent);
		virtual void mousePressEvent(QMouseEvent * event);
		virtual void mouseReleaseEvent(QMouseEvent * event);
		virtual void mouseMoveEvent(QMouseEvent* event);
		virtual void wheelEvent(QWheelEvent* ev);
		
		Projection& projection();
		PropertiesDock* properties();

	private:
		MainWindow* Main;
		Projection theProjection;
		MapDocument* theDocument;
		Interaction* theInteraction;
};

#endif


