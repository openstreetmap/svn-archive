package org.openstreetmap.josm.plugins.czechaddress;

import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;
import org.openstreetmap.josm.plugins.czechaddress.parser.DatabaseParser;

/**
 * Exception occuring during parsing the database.
 * 
 * <p>This exception is used during <i>download</i>, <i>extraction</i> or
 * <i>parsing</i> of the database. It can set a message to be displayed
 * to the user by front-end components.</p>
 *
 * @author Radomír Černcoh radomir.cernoch@gmail.com
 * @see Database
 * @see DatabaseParser
 */
public class DatabaseLoadException extends Exception {

    /**
     * Default constructor, which sets the message description.
     * @param message message to be shown to user
     */
    public DatabaseLoadException(String message) {
        super(message);
    }
}
