package org.openstreetmap.josm.plugins.namemanager.listeners;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
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
public class NameManagerAction extends JosmAction implements SelectionChangedListener {

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
        DataSet.addSelectionListener(this);
        setEnabled(false);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) {
            return;
        }
        NameManagerDialog dialog = NameManagerDialog.getInstance();
        Way selectedWay = (Way) Main.main.getCurrentDataSet().getSelectedWays().iterator().next();
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
     * 
     * @see org.openstreetmap.josm.data.SelectionChangedListener#selectionChanged(java.util.Collection)
     */
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        setEnabled(newSelection != null
                && Main.main.getCurrentDataSet().getSelectedWays().size() == 1
                && Main.main.getCurrentDataSet().getSelectedWays().iterator().next().firstNode() == Main.main.getCurrentDataSet()
                        .getSelectedWays().iterator().next().lastNode());
    }
}
