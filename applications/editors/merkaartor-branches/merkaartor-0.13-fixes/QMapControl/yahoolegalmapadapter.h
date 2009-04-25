/***************************************************************************
 *   Copyright (C) 2008 by Chris Browet                                    *
 *   cbro@semperpax.com                                                    *
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
#ifndef YAHOOLEGALMAPADAPTER_H
#define YAHOOLEGALMAPADAPTER_H

#include "tilemapadapter.h"

#ifdef BROWSERIMAGEMANAGER_IS_THREADED
	#include "QMapControl/browserimagemanagerthreaded.h"
#else
	#include "QMapControl/browserimagemanager.h"
#endif // BROWSERIMAGEMANAGER_IS_THREADED

//! MapAdapter for WMS servers
/*!
 * Use this derived MapAdapter to display maps from WMS servers
 *	@author Kai Winter <kaiwinter@gmx.de>
*/
class YahooLegalMapAdapter : public TileMapAdapter
{
	Q_OBJECT
	public:
		//! constructor
		/*! 
	 	 * This construct a Yahoo Adapter
		 */
		YahooLegalMapAdapter();
		YahooLegalMapAdapter(QString host, QString url);
		virtual ~YahooLegalMapAdapter();
		bool isValid(int x, int y, int z) const;
		IImageManager* getImageManager();

	protected:
		virtual QString getQuery(int x, int y, int z) const;
		virtual int tilesonzoomlevel(int zoomlevel) const;
		virtual int getyoffset(int y) const;

	private:
		virtual QString getQ(QPointF ul, QPointF br) const;

};

#endif
