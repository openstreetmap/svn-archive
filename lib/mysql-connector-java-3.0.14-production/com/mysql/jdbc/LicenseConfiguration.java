/*
   Copyright (C) 2004 MySQL AB

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

import java.sql.SQLException;
import java.util.Map;


/**
 * Used in commercially-licensed clients that require  connections to
 * commercially-licensed servers as part of the licensing terms.
 *
 * @author Mark Matthews
 * @version $Id: LicenseConfiguration.java,v 1.1.2.2 2004/04/18 21:02:51 mmatthew Exp $
 */
class LicenseConfiguration {
   
    private LicenseConfiguration() {
    	// this is a static utility class
    }
     

    /**
     * Used in commercially-licensed clients that require  connections to
     * commercially-licensed servers as part of the licensing terms.
     *
     * @param serverVariables a Map of the output of 'show variables' from the
     *        server we're connecting to.
     *
     * @throws SQLException if commercial license is required, but not found
     */
    static void checkLicenseType(Map serverVariables) throws SQLException {
    	// This is a GPL build, so we don't check anything...
    }
}
