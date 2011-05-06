// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.*;

public class BasicLicenseCheck extends Check 
{

    private LicenseChangePlugin plugin; 

    public BasicLicenseCheck(LicenseChangePlugin p) 
    {
        super(tr("Basic License Check."),
            tr("Checks if all contributors have agreed to the new CT/License."));
        plugin = p;
    }

    @Override public void visit(Node n) 
    {
        List<User> users = plugin.getUsers(n);
        doCheck(n, users);
    }
    @Override public void visit(Way w) 
    {
        List<User> users = plugin.getUsers(w);
        doCheck(w, users);
    }
    @Override public void visit(Relation r) 
    {
        List<User> users = plugin.getUsers(r);
        doCheck(r, users);
    }

    private void doCheck(OsmPrimitive n, List<User> users)
    {
        Severity sev = null;
        if (users != null)
        {
            int u0 = users.get(0).getRelicensingStatus();
            String msg = null;
            if (u0 == User.STATUS_NOT_AGREED || u0 == User.STATUS_ANONYMOUS)
            {
                sev = Severity.DATA_LOSS;
                msg = (u0 == User.STATUS_NOT_AGREED) ? tr("Creator has rejected CT") : tr("Creator unknown");
            }
            else if (u0 == User.STATUS_UNDECIDED)
            {
                sev = Severity.POSSIBLE_DATA_LOSS;
                msg = tr("Creator has not (yet) accepted CT");
            }
            else
            {
                for (int i=1; i<users.size(); i++)
                {
                    int ux = users.get(i).getRelicensingStatus();
                    if (ux == User.STATUS_NOT_AGREED || ux == User.STATUS_ANONYMOUS || ux == User.STATUS_UNDECIDED)
                    {
                        sev = Severity.DATA_REDUCTION;
                        msg = tr("Object modified by user(s) who have rejected, or not agreed to, CT");
                        break;
                    }
                }
            }

            if (sev != null)
            {
                List<? extends org.openstreetmap.josm.data.osm.OsmPrimitive> x = Arrays.asList(n);
                errors.add(new LicenseProblem(this, sev, msg, x, x));
            }
        }
    }
}
