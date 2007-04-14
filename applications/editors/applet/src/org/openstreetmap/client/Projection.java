/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com), Steve Coast (steve@asklater.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */
package org.openstreetmap.client;

/**
 * Implementors map from one coord space to another.
 */
public interface Projection {

  /**
   * @return Screen x from longitude
   */
  public double x(double l);

  /**
   * @return longitude from screen x
   */
  public double lon(double x);

  /**
   * @return Screen y from latitude
   */
  public double y(double l);

  /**
   * @return latitude from screen y
   */
  public double lat(double y);

}
