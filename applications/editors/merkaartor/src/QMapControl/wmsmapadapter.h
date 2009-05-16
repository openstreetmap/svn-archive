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

#include "IMapAdapter.h"
#include "Preferences/WmsServersList.h"

#include <QLocale>

//! MapAdapter for WMS servers
/*!
 * Use this derived MapAdapter to display maps from WMS servers
 *	@author Kai Winter <kaiwinter@gmx.de>
*/
class WMSMapAdapter : public IMapAdapter
{
public:

	WMSMapAdapter(WmsServer aServer);
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

	//! returns the name of this MapAdapter
	/*!
	 * @return  the name of this MapAdapter
	 */
	virtual QString	getName		() const;

	//! returns the host of this MapAdapter
	/*!
	 * @return  the host of this MapAdapter
	 */
	virtual QString	getHost		() const;

	//! returns the size of the tiles
	/*!
	 * @return the size of the tiles
	 */
	virtual int		getTileSize	() const { return -1; }

	//! returns the min zoom value
	/*!
	 * @return the min zoom value
	 */
	virtual int 		getMinZoom	() const { return -1; }

	//! returns the max zoom value
	/*!
	 * @return the max zoom value
	 */
	virtual int		getMaxZoom	() const { return -1; }

	//! returns the current zoom
	/*!
	 * @return the current zoom
	 */
	virtual int 		getZoom		() const { return -1; }

	virtual int		getAdaptedZoom() const { return -1; }
	virtual int 	getAdaptedMinZoom() const { return -1; }
	virtual int		getAdaptedMaxZoom() const { return -1; }

	virtual void	zoom_in() {}
	virtual void	zoom_out() {}

	virtual bool	isValid(int, int, int) const { return true; }
	virtual QString getQuery(int, int, int)  const { return ""; }
	virtual QString getQuery(const QRectF& bbox, const QRect& size) const ;

	//! translates a world coordinate to display coordinate
	/*!
	 * The calculations also needs the current zoom. The current zoom is managed by the MapAdapter, so this is no problem.
	 * To divide model from view the current zoom should be moved to the layers.
	 * @param  coordinate the world coordinate
	 * @return the display coordinate (in widget coordinates)
	 */
	virtual QPoint		coordinateToDisplay(const QPointF& ) const { return QPoint(); }

	//! translates display coordinate to world coordinate
	/*!
	 * The calculations also needs the current zoom. The current zoom is managed by the MapAdapter, so this is no problem.
	 * To divide model from view the current zoom should be moved to the layers.
	 * @param  point the display coordinate
	 * @return the world coordinate
	 */
	virtual QPointF	displayToCoordinate(const QPoint& )  const { return QPointF(); }

	virtual bool isTiled() const { return false; }
	virtual QString projection() const;

	virtual IImageManager* getImageManager();
	virtual void setImageManager(IImageManager* anImageManager);

private:

	QLocale loc;
	WmsServer theServer;
	IImageManager* theImageManager;
};

#endif
