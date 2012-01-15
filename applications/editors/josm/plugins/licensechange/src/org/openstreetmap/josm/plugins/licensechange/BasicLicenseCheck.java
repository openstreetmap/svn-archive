// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.HashMap;
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
        HashMap<User,Severity> users = plugin.getUsers(n);
        doCheck(n, users);
    }
    @Override public void visit(Way w) 
    {
        HashMap<User, Severity> users = plugin.getUsers(w);
        doCheck(w, users);
    }
    @Override public void visit(Relation r) 
    {
        HashMap<User, Severity> users = plugin.getUsers(r);
        doCheck(r, users);
    }

    private void doCheck(OsmPrimitive n, HashMap<User, Severity> users)
    {
        Severity sev = null;
        if (users != null)
        {
            for (Entry<User, Severity> e : users.entrySet())
            {
                // larger sev value = less important
                if ((sev == null) || (sev.compareTo(e.getValue()) > 0)) 
                {
                    sev = e.getValue();
                }
            }
            String msg = null;
            if (sev == Severity.FIRST) {
                msg = tr("Creator has not agreed to CT");
            } else if (sev == Severity.NORMAL) {
                msg = tr("Object modified by user(s) who have rejected, or not agreed to, CT");
            } else {
                msg = tr("minor issue");
            }

            if (sev != null)
            {
                List<? extends org.openstreetmap.josm.data.osm.OsmPrimitive> x = Arrays.asList(n);
                errors.add(new LicenseProblem(this, sev, msg, x, x));
            }
        }
    }
}
