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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.swing.undo.UndoableEdit;

/**
 * A common base for all kinds of OSM-stored primitives. It keeps identity,
 * timestamp, user (author) and few flags (deleted modified visible incomplete).
 * It also keeps all the tags assigned to the primitive.
 * 
 * @author nenik
 */
public abstract class OsmPrimitive {
    
    private long id;
    /**
     * Seconds from epoch (January 1, 1970, 00:00:00 GMT)
     * This field have to be revisited before Jan 19, 2038, 03:14:08 GMT
     */
    private int timestamp;
    
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
    
    /**
     * A field listing the primitives referencing this primitive.
     * The value can be:<ul>
     * <li>null - no references
     * <li>OsmPrimitive[] - a list of all incomming references
     * <li>OsmPrimitive - the only incomming reference
     * </ul>
     */
    private Object referrers;
        
    OsmPrimitive(DataSet source, long id, int timestamp, String user, boolean vis) {
        this.source = source;
        this.id = id;
        this.timestamp = timestamp;
        flags = source.getIdForUser(user) & MASK_USER;
        if (vis) flags |= FLAG_VISIBLE;
    }

    public long getId() {
        return id;
    }
    
    final DataSet getOwner() {
        return source;
    }
    
    public Date getTimestamp() {
        return new Date(1000l*timestamp);
    }

    /**
     * Delete the primitive from its enclosing DataSet.
     * The primitive might be either really removed from the DataSet (in case
     * it has no ID assigned yet) or just marked (and notified)
     * as {@link #isDeleted()} if it already has one.
     */
    public void delete() {
        if (id == 0) { // locally created, really delete
            source.removePrimitive(this);
        }
        UndoableEdit edit = new DeleteEdit();
        setDeletedImpl(true);
        source.postEdit(edit);
    }

    abstract void visit(Visitor v);
 
    /**
     * Referes should make sure not to call if they are already registered
     * @param referrer
     */
    protected void addReferrer(OsmPrimitive referrer) {
        assert referrer != null;
        
        if (referrers == null) {
            referrers = referrer;
        } else if (referrers instanceof OsmPrimitive) {
            assert referrers != referrer;
            referrers = new OsmPrimitive[] { (OsmPrimitive)referrers, referrer };
        } else {
            assert referrers instanceof OsmPrimitive[];
            assert !Arrays.asList((OsmPrimitive[])referrers).contains(referrer);

            OsmPrimitive[] orig = (OsmPrimitive[])referrers;
            OsmPrimitive[] bigger = new OsmPrimitive[orig.length+1];
            System.arraycopy(orig, 0, bigger, 0, orig.length);
            bigger[orig.length] = referrer;
            referrers = bigger;
        }
    }
    
    /**
     * Referes should make sure not to call if they are not registered
     * @param referrer
     */
    protected void removeReferrer(OsmPrimitive referrer) {
        assert referrer != null;

        if (referrers == null) {
            assert false; // violating not-registered precondition
        } else if (referrers instanceof OsmPrimitive) {
            assert referrers == referrer;
            referrers = null;
        } else {
            assert referrers instanceof OsmPrimitive[];
            
            OsmPrimitive[] orig = (OsmPrimitive[])referrers;
            int idx = Arrays.asList(orig).indexOf(referrer);
            
            assert idx != -1;
            
            if (orig.length == 2) {
                referrers = orig[1-idx]; // idx is either 0 or 1, take the other
            } else { // downsize the array
                OsmPrimitive[] smaller = new OsmPrimitive[orig.length-1];
                System.arraycopy(orig, 0, smaller, 0, idx);
                System.arraycopy(orig, idx+1, smaller, idx, smaller.length-idx);
                referrers = smaller;
            }
        }
    }
    /**
     * Find all primitives that reference this primitive.
     * 
     * @return a colection of all primitives that reference this primitive.
     */

    public final Collection<OsmPrimitive> getReferrers() {
        if (referrers == null) return Collections.emptyList();
        if (referrers instanceof OsmPrimitive) return Collections.singleton((OsmPrimitive)referrers);
        return Arrays.asList((OsmPrimitive[])referrers);
    }

    void setDeletedImpl(boolean deleted) {
        if (deleted) {
            flags |= FLAG_DELETED;
            source.firePrimitivesRemoved(Collections.singleton(this));
        } else {
            flags &= ~FLAG_DELETED;
            source.firePrimitivesAdded(Collections.singleton(this));
        }
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

    protected void setModified(boolean modified) {
        if (modified) {
            flags |= FLAG_MODIFIED;
        } else {
            flags &= ~FLAG_MODIFIED;
        }
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
        UndoableEdit edit = new ChangeTagsEdit(tag);
        String old = putTagImpl(tag, value);
        source.postEdit(edit);
        return old;
    }

    public String removeTag(String tag) {
        UndoableEdit edit = new ChangeTagsEdit(tag);
        String old = removeTagImpl(tag);
        source.postEdit(edit);
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

    String putTagImpl(String tag, String value) {
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
                        source.fireTagsChanged(this);
                        return unescape(old);
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
        source.fireTagsChanged(this);
        return unescape(old);
    }
    
    /**
     * Package-private method to be called only from the DataSet factory
     * to load the initial 
     * @param pairs
     */
    void setTags(String[] pairs) {
        assert keys == null;
        assert (pairs.length & 1) == 0;
        
        // scan for default value
        int defIndex = -1;
        for (int i=0; i<pairs.length; i+=2) {
            if (DEFAULT_KEY.equals(pairs[i])) {
                defIndex = i;
                break;
            }
        }
        
        if (defIndex == -1) { // no default
            keys = new Object[pairs.length+1];
            System.arraycopy(pairs, 0, keys, 1, pairs.length);
        } else if (pairs.length == 2) { // default only
            keys = pairs[1] == null ? NULL_VALUE : pairs[1];
        } else { // defKey mixed in: [k v D V k v] -> [V k v k v] , defIndex=2
            Object[] kLocal = new Object[pairs.length-1];
            kLocal[0] = pairs[defIndex+1];
            System.arraycopy(pairs, 0, kLocal, 1, defIndex);
            System.arraycopy(pairs, defIndex+2, kLocal, defIndex+1, kLocal.length - defIndex - 1);
            keys = kLocal;
        }
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
        
        source.fireTagsChanged(this);
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

    public @Override String toString() {
        StringBuilder name = new StringBuilder();
        name.append(getClass().getSimpleName());
        name.append('#');
        name.append(id);
        if (isDeleted()) {
            name.append('x');
        } else if (isModified()) {
            name.append('*');
        }
        
        return name.toString();
    }

    private class DeleteEdit extends DataSet.BaseToggleEdit {
        boolean wasDeleted;

        public DeleteEdit() {
            super ("Delete");
            wasDeleted = isDeleted(); // false
        }
        
        protected void toggle() {
            boolean origVal = isDeleted();
            setDeletedImpl(wasDeleted);
            wasDeleted = origVal;
            if (id == 0) {
                if (wasDeleted) {
                    source.addPrimitive(OsmPrimitive.this);
                } else {
                    source.removePrimitive(OsmPrimitive.this);
                }
            }
        }
    }

    abstract class PrimitiveToggleEdit extends DataSet.BaseToggleEdit {
        private boolean oldModified;
        
        protected PrimitiveToggleEdit(String dispName) {
            super(dispName);
            oldModified = isModified();
            setModified(true);
        }

        protected OsmPrimitive getPrimitive() {
            return OsmPrimitive.this;
        }
        
        protected @Override final void doToggle() {
            boolean modified = isModified();
            super.doToggle();
            setModified(oldModified);
            oldModified = modified;
        }
    }
    
    private class ChangeTagsEdit extends PrimitiveToggleEdit {
        String key;
        String oldVal;

        public ChangeTagsEdit(String key) {
            super ("change tags");
            this.key = key;
            this.oldVal = getTag(key);
        }
        
        protected void toggle() {
            String origVal = getTag(key);
            setTag(key, oldVal);
            oldVal = origVal;
        }
       
        private void setTag(String key, String val) {
            if (val == null) {
                removeTagImpl(key);
            } else {
                putTagImpl(key, val);
            }
        }
    }

}
