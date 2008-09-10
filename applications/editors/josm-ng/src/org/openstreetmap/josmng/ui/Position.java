/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;

import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.CoordinateImpl;
import org.openstreetmap.josmng.view.MapView;
import org.openstreetmap.josmng.view.ViewCoords;
/**
 * Position display in status bar.
 * Editable by double click. After edit view is changed to given position.
 *  
 * @author Petr Nejedly
 * @author Christian Malolepszy
 *
 */
class Position extends JTable implements MouseMotionListener {
	private final MapView mv;

	Position(MapView view) {
		super(1, 1);
		setSize(200, 20);
		setMinimumSize(new Dimension(161, 18));
		setMaximumSize(new Dimension(161, 18));
		this.mv = view;
		mv.addMouseMotionListener(this);
		updatePosition(new Point(0, 0));
		setToolTipText("Double click to enter a new view position.");
		
	}

	private static MessageFormat COORDS = new MessageFormat(
			"{0,number,0.000000};{1,number,0.000000}");

	private static String format(MessageFormat format, Object... args) {
		return format.format(args, new StringBuffer(), null).toString();
	}

	private void updatePosition(Point p) {
		ViewCoords vc = mv.getPoint(p);
		Coordinate coor = mv.getProjection().viewToCoord(vc);
		setValueAt(format(COORDS, coor.getLatitude(), coor.getLongitude()), 0, 0);			
	}	

	public void mouseDragged(MouseEvent e) {
		updatePosition(e.getPoint());
	}

	public void mouseMoved(MouseEvent e) {
		updatePosition(e.getPoint());
	}
	
	@Override
	public void editingStopped(ChangeEvent e) {		
		super.editingStopped(e);
		StringTokenizer st = new StringTokenizer(getValueAt(getSelectedRow(), getSelectedColumn()).toString(),";");
		if (st.countTokens() == 2)
		{
			double lat = Double.parseDouble(st.nextToken().replace(',', '.'));
			double lon = Double.parseDouble(st.nextToken().replace(',', '.'));
			mv.setCenter(mv.getProjection().coordToView(new CoordinateImpl(lat,lon)));						
		}
	}
}
