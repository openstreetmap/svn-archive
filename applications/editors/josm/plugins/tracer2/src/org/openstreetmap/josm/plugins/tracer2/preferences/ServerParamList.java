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

package org.openstreetmap.josm.plugins.tracer2.preferences;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ServerParamList {
    ArrayList<ServerParam> m_listServerParam = new ArrayList<>();
    ServerParam m_oActivParam = null;
    String m_strFilename;
    
    public ServerParamList(String filename) {
        this.m_strFilename = filename;
        if (filename == null) {
        	loadDefault();
        } else {
        	load();
        }
    }
    
    public void load() {
        try {
            BufferedReader oReader = new BufferedReader(new InputStreamReader(new FileInputStream(m_strFilename), "UTF-8"));
            StringBuilder oBuilder = new StringBuilder();
            String strLine;
            while ((strLine = oReader.readLine()) != null) {
            	oBuilder.append(strLine).append('\n');
                if (strLine.equals("")) {
                	m_listServerParam.add(ServerParam.unserialize(oBuilder.toString()));
                	oBuilder = new StringBuilder();
                }
            }
            oReader.close();
        } catch (Exception e) {
        	loadDefault();
        }
    }
    
    public void loadDefault() {
        try {
        	InputStream oIP = getClass().getResourceAsStream("/resources/serverParam.cfg");
            BufferedReader oReader = new BufferedReader(new InputStreamReader(oIP));
            StringBuilder oBuilder = new StringBuilder();
            String strLine;
            while ((strLine = oReader.readLine()) != null) {
            	oBuilder.append(strLine).append('\n');
                if (strLine.equals("")) {
                	m_listServerParam.add(ServerParam.unserialize(oBuilder.toString()));
                	oBuilder = new StringBuilder();
                }
            }
            oReader.close();
        } catch (Exception e) {
        	System.err.println("Tracer2 warning: can't load file " + m_strFilename);
            //e.printStackTrace();
        }
    }

    public void save() {
        try {
            OutputStreamWriter oWriter = new OutputStreamWriter(new FileOutputStream(m_strFilename), "UTF-8");
            for (ServerParam param : m_listServerParam) {
            	oWriter.write(param.serialize());
            }
            oWriter.close();
        } catch (Exception e) {
        	System.err.println("Tracer2 warning: can't save file " + m_strFilename);
            //e.printStackTrace();
        }
    }
    
    public List<ServerParam> getParamList() {
        return m_listServerParam;
    }
    
    public ServerParam getActivParam() {
        return m_oActivParam;
    }
    public void setActivParam(ServerParam param) {
    	if ( m_listServerParam.contains(param)) {
    		m_oActivParam = param;
    	}
    }
    
    public List<ServerParam> getEnableParamList() {
    	List<ServerParam> listParam = new ArrayList<>();
    	for ( ServerParam param: m_listServerParam) {
        	if (param.isEnabled()) {
        		listParam.add(param);
        	}
        }
    	return listParam;
    }
    
    public void addParam(ServerParam param) {
    	m_listServerParam.add(param);
    }
    
    public void removeParam(ServerParam param) {
    	param.setEnabled(false);
    	m_listServerParam.remove(param);
    }
    
}
