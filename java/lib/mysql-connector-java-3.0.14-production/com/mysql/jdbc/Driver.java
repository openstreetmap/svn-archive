/*
   Copyright (C) 2002 MySQL AB

      This program is free software; you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation; either version 2 of the License, or
      (at your option) any later version.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.jdbc;

/**
 * The Java SQL framework allows for multiple database drivers.  Each
 * driver should supply a class that implements the Driver interface
 *
 * <p>The DriverManager will try to load as many drivers as it can find and
 * then for any given connection request, it will ask each driver in turn
 * to try to connect to the target URL.
 *
 * <p>It is strongly recommended that each Driver class should be small and
 * standalone so that the Driver class can be loaded and queried without
 * bringing in vast quantities of supporting code.
 *
 * <p>When a Driver class is loaded, it should create an instance of itself
 * and register it with the DriverManager.  This means that a user can load
 * and register a driver by doing Class.forName("foo.bah.Driver")
 *
 * @see org.gjt.mm.mysql.Connection
 * @see java.sql.Driver
 * @author Mark Matthews
 * @version $Id: Driver.java,v 1.20.2.6 2003/03/05 22:37:21 mmatthew Exp $
 */
public class Driver extends NonRegisteringDriver {
  

    //
    // Register ourselves with the DriverManager
    //
    static {
        try {
            java.sql.DriverManager.registerDriver(new Driver());
        } catch (java.sql.SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }

        if (DEBUG) {
            Debug.trace("ALL");
        }
    }

    /**
     * Construct a new driver and register it with DriverManager
     *
     * @throws java.sql.SQLException if a database error occurs.
     */
    public Driver() throws java.sql.SQLException {
        // Required for Class.forName().newInstance()
    }
}
