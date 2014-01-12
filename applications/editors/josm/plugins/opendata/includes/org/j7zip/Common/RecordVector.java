package org.j7zip.Common;

public class RecordVector<E> extends java.util.Vector<E>
{
    public RecordVector() {
        super();
    }
    
    public void Reserve(int s) {
        ensureCapacity(s);
    }
    
    public E Back() {
        return get(elementCount-1);
    }
}
