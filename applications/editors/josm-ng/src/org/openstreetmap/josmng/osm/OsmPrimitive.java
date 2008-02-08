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

package org.openstreetmap.josmng.osm;

import java.util.Date;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 * A common base for all kinds of OSM-stored primitives. It keeps identity,
 * timestamp, user (author) and few flags (deleted modified visible incomplete).
 * It also keeps all the tags assigned to the primitive.
 * 
 * @author nenik
 */
public abstract class OsmPrimitive {
    private static final DatatypeFactory XML_DATE;
    static {
        DatatypeFactory fact = null;
        try {
            fact = DatatypeFactory.newInstance();
        } catch(DatatypeConfigurationException ce) {
            ce.printStackTrace();
        }
        XML_DATE = fact;
    }
    
    private long id;
    private Object timestamp; // Either String or parsed date
    
    /**
     * A field containing one of the possible implementations
     * of the tag map. The value can be:<ul>
     * <li>null - equivalent of Map.EMPTY
     * <li>Object[] - a SmallSmallMap implementation, that is, array of 2x+1
     *  elements, where 0th element is the value of the DEFAULT_KEY,
     *  element 2n+1 is the n-th key and element 2n+2 is the n-th value. 
     * <li>any other object - the value for the DEFAULT_KEY
     * </ul>
     * Moreover, the value must be postprocessed to allow null values - 
     * they are encoded as reference to NULL_VALUE
     */
    private Object keys;
    
    /**
     * 
     * a bitfield containing all the boolean flags and the user identity as well
     */
    private int flags; 
    
    /**
     * Owner of this OsmPrimitive, i.e. the DataSet to which this item
     * belong to. The DataSet is used for resolving user references
     * and also for event notification.
     */
    protected final DataSet source;
        
    OsmPrimitive(DataSet source, long id, String stamp, String user, boolean vis) {
        this.source = source;
        this.id = id;
        this.timestamp = stamp;
        int uid = source.getIdForUser(user);
        flags = source.getIdForUser(user) & MASK_USER;
        if (vis) flags |= FLAG_VISIBLE;
    }

    public long getId() {
        return id;
    }
    
    
    public Date getTimestamp() {
        if (!(timestamp instanceof Date)) {
            try {
                timestamp = XML_DATE.newXMLGregorianCalendar((String)timestamp).toGregorianCalendar().getTime();
            } catch (Exception ex) {
	        timestamp = new Date();
            }
        }
        return (Date) timestamp;
    }

        
    // -------- Flags processing --------
    private static final int FLAG_MODIFIED = 1 << 30;
    private static final int FLAG_DELETED = 1 << 29;
    private static final int FLAG_INCOMPLETE = 1 << 28;
    private static final int FLAG_VISIBLE = 1 << 27;
    private static final int MASK_USER = 0xFFFFFF; // 24 bits, 16M users
    
    public boolean isModified() {
        return (flags & FLAG_MODIFIED) != 0;
    }
    
    public boolean isDeleted() {
        return (flags & FLAG_DELETED) != 0;
    }

    public boolean isIncomplete() {
        return (flags & FLAG_INCOMPLETE) != 0;
    }
    
    public boolean isVisible() {
        return (flags & FLAG_VISIBLE) != 0;
    }

    /**
     * Can return null if the user is not known
     * @return the name of the user that, according to the server, modified
     * this primitive last.
     */
    public String getUser() {
        String user = source.getUserForId(flags & MASK_USER);
        return user;
    }

    // --------- Tags processing ------------
    private static final String DEFAULT_KEY = "created_by";
    private static final Object NULL_VALUE = new Object();

    public String[] getTags() {
        if (keys == null) return new String[0]; // empty
        if (keys instanceof Object[]) { // 2n+1
            Object[] data = (Object[])keys;
            int i = (data[0] == null ? 0 : 1);
            String[] ret = new String[data.length/2 + i];
            if (i > 0) ret[0] = DEFAULT_KEY;
            for (int j=1; j<data.length; j+= 2) ret[i++] = (String)data[j];
            return ret;
        } else { // default only
            return new String[] {DEFAULT_KEY};
        }
    }
        
    public String putTag(String tag, String value) {
        String old = putTagImpl(tag, value);
        source.postEdit(new ChangeTagsEdit(tag, old, value)); // XXX
        return old;
    }

    public String removeTag(String tag) {
        String old = removeTagImpl(tag);
        source.postEdit(new ChangeTagsEdit(tag, old, null)); // XXX
        return old;
    }

    public String getTag(String tag) {
        if (keys == null) return null; // empty
        if (keys instanceof Object[]) { // 2n+1
            Object[] data = (Object[])keys;
            if (tag.equals(DEFAULT_KEY)) return unescape(data[0]);
            for (int i=1; i<data.length; i+= 2) {
                if (tag.equals(data[i])) return unescape(data[i+1]);
            }
        } else { // default only
            if (tag.equals(DEFAULT_KEY)) return unescape(keys);
        }
        return null;
    }

    private String putTagImpl(String tag, String value) {
        Object old = null;
        Object val = value == null ? NULL_VALUE : value; // escape nulls

        if (tag.equals(DEFAULT_KEY)) {
            if (keys instanceof Object[]) {
                Object[] data = (Object[])keys;
                old = data[0];
                data[0] = val;
            } else {
                old = keys;
                keys = val;
            }
        } else {
            if (keys instanceof Object[]) {
                Object[] data = (Object[])keys;
                
                // replace existing
                for (int i=1; i<data.length; i+= 2) {
                    if (tag.equals(data[i])) {
                        old = data[i+1];
                        data[i+1] = val;
                        return (String)old;
                    }
                    
                }
        
               // create new entry
               Object[] bigger = copyOf(data, data.length+2);
               bigger[data.length] = tag;
               bigger[data.length+1] = val;
               keys = bigger;
            } else {
                keys = new Object[] {keys, tag, val};
            }
        }
        return unescape(old);
    }
    
    
    private String removeTagImpl(String tag) {
        Object old = null;
        if (tag.equals(DEFAULT_KEY)) {
            if (keys instanceof Object[]) {
                Object[] data = (Object[])keys;
                old = data[0];
                data[0] = null;
            } else {
                old = keys;
                keys = null;
            }
        } else {
            if (keys instanceof Object[]) {
                Object[] data = (Object[])keys;
                for (int i=1; i<data.length; i+= 2) {
                    if (tag.equals(data[i])) {
                        old = data[i+1];
                        Object[] smaller = new Object[data.length-2];
                        System.arraycopy(data, 0, smaller, 0, i);
                        System.arraycopy(data, i+2, smaller, i, smaller.length - i);
                        keys = smaller;
                        break;
                    }
                }
    
            }
            // else it is certainly not there.
        }
        
        return unescape(old);
    }

    private static Object[] copyOf(Object[] orig, int newLen) {
        Object[] r = new Object[newLen];
        System.arraycopy(orig, 0, r, 0, orig.length);
        return r;
    }

    private String unescape(Object v) {
        return (v == NULL_VALUE) ? null : (String)v;
    }

    public @Override int hashCode() {
        if (id == 0) return super.hashCode();
        return (int)id ^ getClass().hashCode();
    }

    public @Override boolean equals(Object obj) {
        if (id == 0) return obj == this;
        if (obj instanceof OsmPrimitive) { // not null too
            return ((OsmPrimitive)obj).id == id && obj.getClass() == getClass();
        }
        return false; 
    }
    
    
    private class ChangeTagsEdit extends AbstractUndoableEdit {
        String key;
        String oldVal;
        String newVal;

        public ChangeTagsEdit(String key, String oldVal, String newVal) {
            this.key = key;
            this.oldVal = oldVal;
            this.newVal = newVal;
        }
        public @Override void undo() throws CannotUndoException {
            super.undo(); // to validate
            setTag(key, oldVal);
        }

        public @Override void redo() throws CannotRedoException {
            super.redo(); // to validate
            setTag(key, newVal);
        }
        
        private void setTag(String key, String val) {
            if (val == null) {
                removeTag(key);
            } else {
                putTagImpl(key, val);
            }
        }
    }

}
