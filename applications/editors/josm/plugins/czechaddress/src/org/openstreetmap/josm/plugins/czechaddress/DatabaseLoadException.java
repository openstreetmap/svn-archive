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
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     */
    public DatabaseLoadException(String message) {
        super(message);
    }

    /**
     * Default constructor, which sets the message description and cause.
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public DatabaseLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
