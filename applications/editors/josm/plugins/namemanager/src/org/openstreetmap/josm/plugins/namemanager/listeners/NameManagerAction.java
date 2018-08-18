// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.namemanager.listeners;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.plugins.namemanager.dialog.NameManagerDialog;
import org.openstreetmap.josm.plugins.namemanager.utils.NameManagerUtils;
import org.w3c.dom.Document;

/**
 * This is {@link JosmAction} subclass, which is responsible for executing mouse
 * click on toolmenu plugin field.
 * 
 * @author Rafal Jachowicz, Harman/Becker Automotive Systems (master's thesis)
 * 
 */
public class NameManagerAction extends JosmAction implements DataSelectionListener {

    private static final String ATTRIBUTE_DISTRICTS = "Attribute districts";
    private static final String NAME_MANAGER = "Name Manager";
    private static final String NAME_MANAGER_MENU = "selectall";
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 5638780111409126823L;

    /**
     * Constructor.
     */
    public NameManagerAction() {
        super(tr(NAME_MANAGER), NAME_MANAGER_MENU, tr(ATTRIBUTE_DISTRICTS), null, true, "namemanager", true);
        SelectionEventManager.getInstance().addSelectionListener(this);
        setEnabled(false);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) {
            return;
        }
        NameManagerDialog dialog = NameManagerDialog.getInstance();
        Way selectedWay = (Way) getLayerManager().getEditDataSet().getSelectedWays().iterator().next();
        dialog.setSelectedWay(selectedWay);
        dialog.setWaysInsideSelectedArea(NameManagerUtils.getWaysInsideSelectedArea(selectedWay));
        Document doc = NameManagerUtils.parseCountries();
        doc.getDocumentElement().normalize();
        NameManagerUtils.prepareCountryDataMemoryCache(doc);
        dialog.setCountryComboBox();
        dialog.repaint();
        dialog.setVisible(true);
    }

    /**
     * This method is responsible for enabling and disabling toolmenu
     * LaneManager button.
     */
    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        boolean enabledState = false;
        DataSet ds = getLayerManager().getEditDataSet();
        if (event.getSelection() != null && ds != null) {
            Collection<Way> selectedWays = ds.getSelectedWays();
            enabledState = selectedWays.size() == 1 && selectedWays.iterator().next().isClosed();
        }
        setEnabled(enabledState);
    }
}
