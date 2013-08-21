package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * This is a model for a combo box to select a turn restriction type. The
 * user can choose from a list of standard types but the model also supports
 * non-standard tag values in the OSM data. 
 *
 */
public class TurnRestrictionComboBoxModel implements ComboBoxModel, Observer{
    //static private final Logger logger = Logger.getLogger(TurnRestrictionComboBoxModel.class.getName());
    
    private TurnRestrictionEditorModel model;
    final private List<Object> values = new ArrayList<Object>();
    private String selectedTagValue = null;
    private final transient EventListenerList listeners = new EventListenerList();
    
    /**
     * Populates the model with the list of standard values. If the
     * data contains a non-standard value it is displayed in the combo
     * box as an additional element. 
     */
    protected void populate() {
        values.clear();
        for (TurnRestrictionType type: TurnRestrictionType.values()) {
            values.add(type);
        }       
        
        String tagValue = model.getRestrictionTagValue();
        if (tagValue.trim().equals("")) {
            selectedTagValue = null;
        } else {
            TurnRestrictionType type = TurnRestrictionType.fromTagValue(tagValue);
            if (type == null) {
                values.add(0, tagValue);
                selectedTagValue = tagValue;
            } else {
                selectedTagValue = type.getTagValue();
            }
        }
        fireContentsChanged();
    }
    
    /**
     * Creates the combo box model. 
     * 
     * @param model the turn restriction editor model. Must not be null.
     */
    public TurnRestrictionComboBoxModel(TurnRestrictionEditorModel model){
        CheckParameterUtil.ensureParameterNotNull(model, "model");
        this.model = model;
        model.addObserver(this);
        populate();
    }

    public Object getSelectedItem() {
        TurnRestrictionType type = TurnRestrictionType.fromTagValue(selectedTagValue);
        if (type != null) return type;
        return selectedTagValue;
    }

    public void setSelectedItem(Object anItem) {
        String tagValue = null;
        if (anItem instanceof String) {
            tagValue = (String)anItem;
        } else if (anItem instanceof TurnRestrictionType){
            tagValue = ((TurnRestrictionType)anItem).getTagValue();
        }
        model.setRestrictionTagValue(tagValue);
    }

    public Object getElementAt(int index) {
        return values.get(index);
    }

    public int getSize() {
        return values.size();
    }
    
    public void addListDataListener(ListDataListener l) {
        listeners.add(ListDataListener.class, l);       
    }
    
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(ListDataListener.class, l);        
    }
    
    protected void fireContentsChanged() {
        for(ListDataListener l: listeners.getListeners(ListDataListener.class)) {
            l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
        }
    }
    
    /* ------------------------------------------------------------------------------------ */
    /* interface Observer                                                                   */
    /* ------------------------------------------------------------------------------------ */
    public void update(Observable o, Object arg) {      
        String tagValue = model.getRestrictionTagValue();
        if (tagValue == null && selectedTagValue != null) {
            populate();
        } else if (tagValue != null && selectedTagValue == null){
            populate();
        } else if (tagValue != null) {
            if (!tagValue.equals(selectedTagValue)) {
                populate();
            }
        } 
    }
}
