/* Copyright (c) 2008, Henrik Niehaus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.osb;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class OsbUploadHook implements UploadHook {

    public boolean checkUpload(APIDataSet apiData)
    {
        boolean containsOsbData = checkOpenStreetBugs(apiData.getPrimitivesToAdd());
        containsOsbData |= checkOpenStreetBugs(apiData.getPrimitivesToUpdate());
        containsOsbData |= checkOpenStreetBugs(apiData.getPrimitivesToDelete());
        if(containsOsbData) {
            JOptionPane.showMessageDialog(Main.parent,
                tr("<html>The selected data contains data from OpenStreetBugs.<br>" +
                "You cannot upload this data. Maybe you have selected the wrong layer?"),
                tr("Warning"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else {
            return true;
        }
    }

    private boolean checkOpenStreetBugs(Collection<OsmPrimitive> osmPrimitives) {
        for (Iterator<OsmPrimitive> iterator = osmPrimitives.iterator(); iterator.hasNext();) {
            OsmPrimitive osmPrimitive = iterator.next();
            if(osmPrimitive.get("openstreetbug") != null) {
                return true;
            }
        }
        return false;
    }
}
