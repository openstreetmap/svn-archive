/*
 * Copyright (C) 2007 Dan Moore
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
package org.openstreetmap.util;

/**
 * Implemented by classes that can be requested to shutdown, as their controller
 * wants to discontinue their use.
 */
public interface Releaseable {
  
  /** 
   * Commands implementor to release resources, external refs and shutdown
   * any active threads.
   */
  void release();
}
