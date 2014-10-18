/**
 *  Tracer2 - plug-in for JOSM to capture contours
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
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

public class TracerDebug {

    private static String FormatPrimitive(String strIn) {
    	while (strIn.contains("{")) {
    		strIn = strIn.replace("{", "xxxxx");
    	}
		return strIn.replaceAll("xxxxx", "\r\n	{");
    }

	public  void OutputOsmPrimitive(Collection<OsmPrimitive> cOsmPrimitive) {
		if (cOsmPrimitive != null) {
    		for (OsmPrimitive p : cOsmPrimitive) {
    	    	System.out.println(" OsmPrimitive: " + FormatPrimitive(p.toString()));
    	    }
		}
	}
	
	public  void OutputOsmExtendsPrimitive(Collection<? extends OsmPrimitive> cOsmPrimitive) {
		if (cOsmPrimitive != null) {
    		for (OsmPrimitive p : cOsmPrimitive) {
    	    	System.out.println(" OsmPrimitive x: " + FormatPrimitive(p.toString()));
    	    }
		}
	}
	
	public void OutputCommands(LinkedList<Command> cmds) {
		
		for (Command c : cmds) {
			System.out.println("");

			Collection<OsmPrimitive> cp1 = null;
        	Collection<OsmPrimitive> cp2 = null;
        	Collection<OsmPrimitive> cp3 = null;
        	Collection<? extends OsmPrimitive> cpx = null;
        	
        	List<OsmPrimitive> lp1 = new LinkedList<>();
        	List<OsmPrimitive> lp2 = new LinkedList<>();
        	List<OsmPrimitive> lp3 = new LinkedList<>();
        	List<OsmPrimitive> lp = new LinkedList<>();
        	
        	cp1 = lp1;
        	cp2 = lp2;
        	cp3 = lp3;
        	cpx = lp;
        	
        	//OsmPrimitive op = new OsmPrimitive();
        	OsmPrimitive op1 = new Way();
        	
        	System.out.println("Command: " + c.toString());
        	
        	if (c instanceof AddCommand) {
        	    AddCommand x = (AddCommand) c;
         		x.fillModifiedData(cp1, cp2, cp3);
         		OutputOsmPrimitive(cp1);
         		OutputOsmPrimitive(cp2);
         		OutputOsmPrimitive(cp3);
         		cpx = x.getParticipatingPrimitives();
         		OutputOsmExtendsPrimitive(cpx);
         	} else if (c instanceof ChangeCommand) { // order is important!
         		ChangeCommand x = (ChangeCommand) c;
         		x.fillModifiedData(cp1, cp2, cp3);
         		x.getOrig(op1);
         		OutputOsmPrimitive(cp1);
         		OutputOsmPrimitive(cp2);
         		OutputOsmPrimitive(cp3);
         		cpx = x.getParticipatingPrimitives();
         		OutputOsmExtendsPrimitive(cpx);
        	} else if (c instanceof DeleteCommand) {
        		DeleteCommand x = (DeleteCommand) c;
         		x.fillModifiedData(cp1, cp2, cp3);
         		OutputOsmPrimitive(cp1);
         		OutputOsmPrimitive(cp2);
         		OutputOsmPrimitive(cp3);
         		cpx = x.getParticipatingPrimitives();
         		OutputOsmExtendsPrimitive(cpx);
         	} else if (c instanceof MoveCommand) { // order is important!
         		MoveCommand x = (MoveCommand) c;
         		x.fillModifiedData(cp1, cp2, cp3);
         		OutputOsmPrimitive(cp1);
         		OutputOsmPrimitive(cp2);
         		OutputOsmPrimitive(cp3);
         		cpx = x.getParticipatingPrimitives();
         		OutputOsmExtendsPrimitive(cpx);
        	} else {
         		c.fillModifiedData(cp1, cp2, cp3);
         		OutputOsmPrimitive(cp1);
         		OutputOsmPrimitive(cp2);
         		OutputOsmPrimitive(cp3);
         		cpx = c.getParticipatingPrimitives();
         		OutputOsmExtendsPrimitive(cpx);
        	}
        }
	}
	
}
