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
#ifndef WMSMAPADAPTER_H
#define WMSMAPADAPTER_H

#include "tilemapadapter.h"

//! MapAdapter for WMS servers
/*!
 * Use this derived MapAdapter to display maps from WMS servers
 *	@author Kai Winter <kaiwinter@gmx.de>
*/
class WMSMapAdapter : public TileMapAdapter
{
	friend class ImageMapLayer;
	public:
	//! constructor
	/*!
	 * Sample of a correct initialization of a MapAdapter:<br/>
	 * MapAdapter* mapadapter = new WMSMapAdapter("www2.demis.nl", "/wms/wms.asp?wms=WorldMap[...]&BBOX=%1,%2,%3,%4&WIDTH=%5&HEIGHT=%5&TRANSPARENT=TRUE", 256);<br/>
	 * The placeholders %1, %2, %3, %4 creates the bounding box, %5 is for the tilesize
	 * The minZoom is 0 (means the whole world is visible). The maxZoom is 17 (means it is zoomed in to the max)
	 * @param host The servers URL
	 * @param serverPath The path to the tiles with placeholders
	 * @param tilesize the size of the tiles
	 */
		WMSMapAdapter(QString host, QString serverPath, QString wlayers, QString wSrs, QString wStyles, QString wImgFormat, int tilesize = 256);
		virtual ~WMSMapAdapter();

		//! returns the unique identifier (Uuid) of this MapAdapter
		/*!
		 * @return  the unique identifier (Uuid) of this MapAdapter
		 */
		virtual QUuid	getId		() const;

		//! returns the type of this MapAdapter
		/*!
		 * @return  the type of this MapAdapter
		 */
		virtual IMapAdapter::Type	getType		() const;

		virtual QPoint		coordinateToDisplay(const QPointF&) const;
		virtual QPointF	displayToCoordinate(const QPoint&) const;

		virtual QString projection() const;

	protected:
		virtual int tilesonzoomlevel(int zoomlevel) const;
		//virtual void zoom_in();
		//virtual void zoom_out();
		virtual QString getQuery(int x, int y, int z) const;
		virtual bool isValid(int x, int y, int z) const;

	private:
		virtual QString getQ(QPointF ul, QPointF br) const;

		//double coord_per_x_tile;
		//double coord_per_y_tile;

		QString wms_version;
		QString wms_request;
		QString wms_layers;
		QString wms_styles;
		QString wms_srs;
		QString wms_format;
		QString wms_transparent;
		QString wms_bgcolor;
		QString wms_exceptions;
		QString wms_time;
		QString wms_elevation;
		QString wms_width;
		QString wms_height;
};

#endif
