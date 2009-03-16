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
#ifndef LAYER_H
#define LAYER_H

#include <QObject>
#include <QDebug>
#include <QPainter>
#include <QMouseEvent>

#include "IMapAdapter.h"
#include "layermanager.h"

#include "wmsmapadapter.h"
#include "tilemapadapter.h"
//! Layer class
/*!
 * There are two different layer types:
 *  - MapLayer: Displays Maps, but also Geometries. The configuration for displaying maps have to be done in the MapAdapter
 *  - GeometryLayer: Only displays Geometry objects.
 * 
 * MapLayers also can display Geometry objects. The difference to the GeometryLayer is the repainting. Objects that are
 * added to a MapLayer are "baken" on the map. This means, when you change it´s position for example the changes are
 * not visible until a new offscreen image has been drawn. If you have "static" Geometries which won´t change their
 * position this is fine. But if you want to change the objects position or pen you should use a GeometryLayer. Those
 * are repainted immediately on changes.
 * You can either use this class and give a layertype on creation or you can use the classes MapLayer and GeometryLayer.
 * 
 *	@author Kai Winter <kaiwinter@gmx.de>
*/

class Layer : public QObject
{
Q_OBJECT
public:
	friend class LayerManager;
	friend class RenderThread;
	
	//! sets the type of a layer, see Layer class doc for further information
	enum LayerType
	{
		MapLayer, /*!< uses the MapAdapter to display maps, only gets refreshed when a new offscreen image is needed */
		GeometryLayer /*!< gets refreshed everytime when a geometry changes */
	};

	//! Layer constructor
	/*!
	 * This is used to construct a layer.
	 * 
	 * @param layername The name of the Layer
	 * @param mapadapter The MapAdapter which does coordinate translation and Query-String-Forming
	 * @param layertype The above explained LayerType
	 * @param takeevents Should the Layer receive MouseEvents? This is set to true by default. Setting it to false could
	 * be something like a "speed up hint"
	 */
	Layer(QString layername, IMapAdapter* mapadapter, enum LayerType layertype, bool takeevents=true);
	virtual ~Layer();

	//! returns the layer's name
	/*!
	 * @return the name of this layer
	 */
	QString	getLayername() const;
	
	//! returns the layer´s MapAdapter
	/*!
	 * This method returns the MapAdapter of this Layer, which can be useful
	 * to do coordinate transformations.
	 * @return the MapAdapter which us used by this Layer
	 */
	IMapAdapter*	getMapAdapter() const;
	
	//! adds a Geometry object to this Layer
	/*!
	 * Please notice the different LayerTypes (MapLayer and GeometryLayer) and the differences
	 * @param  geometry the new Geometry
	 */
	//void	addGeometry(Geometry* geometry);
	//
	//void	removeGeometry(Geometry* geometry);
	
	//! return true if the layer is visible
	/**
	 * @return if the layer is visible
	 */
	bool	isVisible() const;
	
	//! returns the LayerType of the Layer
	/*!
	 * There are two LayerTypes: MapLayer and GeometryLayer
	 * @return the LayerType of this Layer
	 */
	Layer::LayerType getLayertype() const;
	
	void setMapAdapter(IMapAdapter* mapadapter);
	
	Layer& operator=(const Layer& rhs);
	Layer(const Layer& old);
	
private:
	//void moveWidgets(const QPoint mapmiddle_px) const;
	void drawYourImage(QPainter* painter, const QPoint mapmiddle_px) const;
	//void drawYourGeometries(QPainter* painter, const QPoint mapmiddle_px, QRect viewport) const;
	void setSize(QSize size);
	QRect getOffscreenViewport() const;
	//bool takesMouseEvents() const;
	//void mouseEvent(const QMouseEvent*, const QPoint mapmiddle_px);
	void zoomIn() const;
	void zoomOut() const;
	void _draw(QPainter* painter, const QPoint mapmiddle_px) const;
	
	bool visible;
	QString layername;
	LayerType layertype;
	QSize size;	
	QPoint screenmiddle;
	
	//QList<Geometry*> geometries;
	IMapAdapter*	mapAdapter;
	bool takeevents;
	mutable QRect offscreenViewport;

	
	struct Tile
	{
		Tile(int i, int j, double priority)
		    : i(i), j(j), priority(priority)
		{}

		int i, j;
		double priority;

		bool operator<(const Tile& rhs) const { return priority < rhs.priority; }
	};

	
	signals:
		//! This signal is emitted when a Geometry is clicked
		/*!
		 * A Geometry is clickable, if the containing layer is clickable.
		 * The layer emits a signal for every clicked geometry
		 * @param  geometry The clicked Geometry
		 * @param  point The coordinate (in widget coordinates) of the click
		 */
		//void geometryClickEvent(Geometry* geometry, QPoint point);
	
		void updateRequest(QRectF rect);
		void updateRequest();
		
	public slots:
	//! if visible is true, the layer is made visible
	/*!
	 * @param  visible if the layer should be visible
	 */
	void	setVisible(bool visible);
	
};

#endif
