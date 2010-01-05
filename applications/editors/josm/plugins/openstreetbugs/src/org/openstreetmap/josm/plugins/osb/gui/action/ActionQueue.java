package org.openstreetmap.josm.plugins.osb.gui.action;

import java.util.LinkedList;

import javax.swing.AbstractListModel;

public class ActionQueue extends AbstractListModel {

    private static ActionQueue instance;

    private LinkedList<OsbAction> queue = new LinkedList<OsbAction>();

    private ActionQueue() {}

    public static synchronized ActionQueue getInstance() {
        if(instance == null) {
            instance = new ActionQueue();
        }
        return instance;
    }

    public boolean offer(OsbAction e) {
        boolean result = queue.offer(e);
        fireIntervalAdded(this, queue.size()-1, queue.size()-1);
        return result;
    }

    public OsbAction peek() {
        return queue.peek();
    }

    public OsbAction poll() {
        OsbAction action = queue.poll();
        fireIntervalRemoved(this, 0, 0);
        return action;
    }

    public boolean remove(Object o) {
        int index = queue.indexOf(o);
        if(index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return queue.remove(o);
    }

    public void processQueue() throws Exception {
        while(!queue.isEmpty()) {
            // get the first action, but leave it in queue
            OsbAction action = queue.peek();

            // execute the action
            action.execute();
            
            // notify observers
            for (OsbActionObserver obs : action.getActionObservers()) {
                obs.actionPerformed(action);
            }

            // if no exception has been thrown, remove the action from the queue
            queue.remove();
            fireIntervalRemoved(this, 0, 0);
        }
    }

    public Object getElementAt(int index) {
        return queue.get(index);
    }

    public int getSize() {
        return queue.size();
    }
}
