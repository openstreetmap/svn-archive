package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;

public class MenuActionResetCookie extends JosmAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MenuActionResetCookie() {
        super(tr("Reset cookie"), "cadastre_small", tr("Get a new cookie (session timeout)"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        CadastrePlugin.cadastreGrabber.getWmsInterface().resetCookie();
    }
}
