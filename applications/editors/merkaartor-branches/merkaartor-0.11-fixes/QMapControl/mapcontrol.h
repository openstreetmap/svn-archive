/***************************************************************************
 *   Copyright (C) 2007 by Kai Winter   *
 *   kaiwinter@gmx.de   *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
#ifndef MAPCONTROL_H
#define MAPCONTROL_H

#include <QtGui>

#include "layermanager.h"
#include "layer.h"
#include "mapadapter.h"
#include "geometry.h"
#include "imagemanager.h"
class LayerManager;
class MapAdapter;
class Layer;


//! The control element of the widget and also the widget itself
/*!
 * This is the main widget.
 * To this control layers can be added.
 * A MapControl have to be instantiated with a QSize which sets the size the widget takes in a layout.
 * The given size is also the size, which is asured to be filled with map images.
 *
 * @author Kai Winter <kaiwinter@gmx.de>
*/
class MapControl : public QWidget
{
	Q_OBJECT

	public:
		//! Declares what actions the mouse move has on the map
		enum MouseMode
		{
			Panning, /*!< The map is moved */
			Dragging, /*!< A rectangular can be drawn */
			None, /*!< Mouse move events have no efect to the map */
		};

		//! The constructor of MapControl
		/*!
		 * The MapControl is the widget which displays the maps.
		 * The size describes the area, which gets filled with map data
		 * When you give no MouseMode, the mouse is moving the map.
		 * You can change the MouseMode on runtime, to e.g. Dragging, which lets the user drag a rectangular box.
		 * After the dragging a signal with the size of the box is emitted.
		 * The mousemode ´None´ can be used, to completely define the control of the map yourself.
		 * @param size the size which the widget should fill with map data
		 * @param mousemode the way mouseevents are handled
		 */
		MapControl(QSize size, MouseMode mousemode = Panning);
		
		~MapControl();
		
		//! adds a layer
		/*!
		 * If multiple layers are added, they are painted in the added order.
		 * @param layer the layer which should be added
		 */
		void addLayer(Layer* layer);
		
		//! returns the layer with the given name
		/*!
		 * @param  layername name of the wanted layer
		 * @return the layer with the given name
		 */
		Layer* getLayer(const QString& layername) const;
		
		//! returns the names of all layers
		/*!
		 * @return returns a QList with the names of all layers
		 */
		QList<QString> getLayers() const;
		
		//! returns the number of existing layers
		/*!
		 * @return returns the number of existing layers
		 */
		int getNumberOfLayers() const;
		
		//! returns the coordinate of the center of the map
		/*!
		 * @return returns the coordinate of the middle of the screen
		 */
		QPointF	getCurrentCoordinate() const;
		
		//! returns the current zoom level
		/*!
		 * @return returns the current zoom level
		 */
		int getCurrentZoom() const;
		
		//! sets the middle of the map to the given coordinate
		/*!
		 * @param  coordinate the coordinate which the view´s middle should be set to
		 */
		void setView(const QPointF& coordinate) const;
		
		//! sets the view, so all coordinates are visible
		/*!
		 * The code of setting the view to multiple coordinates is "brute force" and pretty slow.
		 * Have to be reworked.
		 * @param  coordinates the Coorinates which should be visible
		 */
		void setView(const QList<QPointF> coordinates) const;
		
		//! sets the view to the given Point
		/*!
		 * 
		 * @param point the geometric point the view should be set to
		 */
		void setView(const Point* point) const;
		
		//! Keeps the center of the map on the Geometry, even when it moves
		/*!
		 * To stop the following the method stopFollowing() have to be called
		 * @param  geometry the Geometry which should stay centered.
		 */
		void followGeometry(const Geometry* geometry) const;
		//TODO:
// 		void			followGeometry(const QList<Geometry*>) const;
		
		//! Stops the following of a Geometry
		/*!
		 * if the view is set to follow a Geometry this method stops the trace.
		 * See followGeometry().
		 * @param geometry the Geometry which should not followed anymore
		 */
		void stopFollowing(Geometry* geometry);
		
		//! Smoothly moves the center of the view to the given Coordinate
		/*!
		 * @param  coordinate the Coordinate which the center of the view should moved to
		 */
		void moveTo	(QPointF coordinate);
		
		//! sets the Mouse Mode of the MapControl
		/*!
		 * There are three MouseModes declard by an enum.
		 * The MouesMode Dragging draws an rectangular in the map while the MouseButton is pressed.
		 * When the Button is released a draggedRect() signal is emitted.
		 * 
		 * The second MouseMode (the default) is Panning, which allows to drag the map around.
		 * @param mousemode the MouseMode
		 */
		void setMouseMode(MouseMode mousemode);
		
		//! returns the current MouseMode
		/*!
		 * For a explanation for the MouseModes see setMouseMode()
		 * @return the current MouseMode
		 */
		MapControl::MouseMode getMouseMode();
		
		int rotation;
		
	private:
		LayerManager*	layermanager;
		QPoint screen_middle;	// middle of the widget (half size)
		
		QPoint pre_click_px;			// used for scrolling (MouseMode Panning)
		QPoint current_mouse_pos;	// used for scrolling and dragging (MouseMode Panning/Dragging)
		
		QSize size;		// size of the widget
		
		bool mousepressed;
		
		MouseMode mousemode;
		
		QMutex moveMutex;	// used for method moveTo()
		QPointF target;	// used for method moveTo()
		int steps;			// used for method moveTo()
		
		QPointF clickToWorldCoordinate(QPoint click);
		MapControl& operator=(const MapControl& rhs);
		MapControl(const MapControl& old);
	
	protected:			
		void paintEvent(QPaintEvent* evnt);
		void mousePressEvent(QMouseEvent* evnt);
		void mouseReleaseEvent(QMouseEvent* evnt);
		void mouseMoveEvent(QMouseEvent* evnt);
		

	signals:		
// 		void mouseEvent(const QMouseEvent* evnt);
		
		//! Emitted AFTER a MouseEvent occured
		/*!
		 * This signals allows to receive click events within the MapWidget together with the world coordinate.
		 * It is emitted on MousePressEvents and MouseReleaseEvents.
		 * The kind of the event can be obtained by checking the events type.
		 * @param  evnt the QMouseEvent that occured
		 * @param  coordinate the corresponding world coordinate
		 */
		void mouseEventCoordinate(const QMouseEvent* evnt, const QPointF coordinate);
		
		//! Emitted, after a Rectangular is dragged.
		/*!
		 * It is possible to select a rectangular area in the map, if the MouseMode is set to Dragging.
		 * The coordinates are in world coordinates
		 * @param  QRectF the dragged Rect
		 */
		void draggedRect(const QRectF);
		
		//! This signal is emmited, when a Geometry is clicked
		/*!
		 * @param geometry 
		 * @param coord_px  asd
		 */
		void geometryClickEvent(Geometry* geometry, QPoint coord_px);
		
	public slots:
		//! zooms in one step
		void zoomIn();
		
		//! zooms out one step
		void zoomOut();
		
		//! sets the given zoomlevel
		/*!
		 * @param zoomlevel the zoomlevel
		*/
		void setZoom(int zoomlevel);
		
		//! scrolls the view to the left
		void scrollLeft(int pixel=10);
		
		//! scrolls the view to the right
		void scrollRight(int pixel=10);
		
		//! scrolls the view up
		void scrollUp(int pixel=10);
		
		//! scrolls the view down
		void scrollDown(int pixel=10);
		
		//! scrolls the view by the given point
		void scroll(const QPoint scroll);
		
		//! updates the map for the given rect
		/*!
		 * @param rect the area which should be repainted
		 */
		void updateRequest(QRect rect);
		
		//! updates the hole map by creating a new offscreen image
		/*!
		 * 
		 */
		void updateRequestNew();
		
	private slots:
		void tick();
		void loadingFinished();
		void positionChanged(Geometry* geom);
};

#endif
