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

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParam;

public class TagValues {
    private String m_strTag = "";
    private String m_strPreferredValues = "";
    
    private int m_nPos = 0;
    private boolean m_bPreferred = false;
    private String[] m_astrTagValues = null;
    private String[] m_astrTagValuesPreferred = null;
    
    public TagValues() {
    	clearAll();
    }
    
    public void readBuildingTags(ServerParam param) {
        if (param==null || param.getTag() == null || param.getTag().equals("")) {
        	clearAll();
        	return;
        }
        if (param.getTag().equals(m_strTag) && param.getPreferredValues().equals(m_strPreferredValues)) {
        	return;
        }
        clearAll();
        
        m_strTag = param.getTag();
        m_strPreferredValues = param.getPreferredValues();
        
    	// get values
        List<String> tagValues = new ArrayList<>();
        List<AutoCompletionListItem> values = Main.getLayerManager().getEditDataSet().getAutoCompletionManager().getValues(m_strTag);
        for ( AutoCompletionListItem i : values ) {
        	tagValues.add(i.getValue());
        }
    	m_astrTagValues = (String[])tagValues.toArray(new String[tagValues.size()]);
        
    	// get preferred values
    	if ( m_strPreferredValues.equals("") ) {
    		m_astrTagValuesPreferred = new String[0];
    	} else {
    		String[] prefered = m_strPreferredValues.split(";");
    		tagValues = new ArrayList<>();
    		
    		for (String str: prefered) {
    			String temp = str.trim();
    			if (getPos(temp, m_astrTagValues) >= 0 ) {
    				tagValues.add(temp);
    			}
    		}
    		m_astrTagValuesPreferred = (String[])tagValues.toArray(new String[tagValues.size()]);
    		
    		// set to the first preferred
    		m_bPreferred = true;
   			m_nPos = 0;
    	}
    }
    
    private void clearAll() {
        m_strTag = "";
        m_strPreferredValues = "";
        
        m_nPos = 0;
        m_bPreferred = false;
        m_astrTagValues = null;
        m_astrTagValuesPreferred = null;
    }
    
    public String getTag() {
    	if (m_strTag == null || m_strTag.equals("")) {
    		return null;
    	}
    	return m_strTag;
    }
    
    public String getTagValue() {
    	if ( m_bPreferred == false ) {
    		if (m_astrTagValues != null && m_astrTagValues.length > m_nPos) {
    			return m_astrTagValues[m_nPos];
    		}
    	} else {
    		if (m_astrTagValuesPreferred != null && m_astrTagValuesPreferred.length > m_nPos) {
    			return m_astrTagValuesPreferred[m_nPos];
    		}
    	}
    	return null;
    }
    
    private int getPos( String value, String[] values) {
    	if (value != null && values != null ) {
    		for ( int i = 0; i<values.length ; i++ ) {
    			if ( value.equals(values[i]) ) {
    				return i;
    			}
    		}
    	}
    	return -1;
    }
    
    public void left() {
    	if (m_astrTagValues == null || m_astrTagValues.length == 0) {
    		return;
    	}
    	if (m_bPreferred == false) {
    		if (m_astrTagValuesPreferred == null || m_astrTagValuesPreferred.length == 0) {
    			return;
    		}
    		m_bPreferred = true;
    		m_nPos = getPos( m_astrTagValues[m_nPos], m_astrTagValuesPreferred);
    		if ( m_nPos < 0 ) {
    			m_nPos = 0;
        		return;
    		}
    	}
		m_nPos--;
    	if (m_nPos < 0) m_nPos = m_astrTagValuesPreferred.length-1;
    }
    
    public void right() {
    	if (m_astrTagValues == null || m_astrTagValues.length == 0) {
    		return;
    	}
    	if (m_bPreferred == false) {
    		if (m_astrTagValuesPreferred == null || m_astrTagValuesPreferred.length == 0) {
    			return;
    		}
    		m_bPreferred = true;
    		m_nPos = getPos( m_astrTagValues[m_nPos], m_astrTagValuesPreferred);
    		if ( m_nPos < 0 ) {
    			m_nPos = 0;
        		return;
    		}
    	}
		m_nPos++;
    	if (m_nPos >= m_astrTagValuesPreferred.length) m_nPos = 0;
    }
    
    public void up() {
    	if (m_astrTagValues == null || m_astrTagValues.length == 0) {
    		return;
    	}
    	if (m_bPreferred == true) {
    		m_bPreferred = false;
    		m_nPos = getPos( m_astrTagValuesPreferred[m_nPos], m_astrTagValues);
    		if ( m_nPos < 0 ) {
    			m_nPos = 0;
        		return;
    		}
    	}
    	m_nPos--;
    	if (m_nPos < 0) m_nPos = m_astrTagValues.length-1;
    }
    
    public void down() {
    	if (m_astrTagValues == null || m_astrTagValues.length == 0) {
    		return;
    	}
    	if (m_bPreferred == true) {
    		m_bPreferred = false;
    		m_nPos = getPos( m_astrTagValuesPreferred[m_nPos], m_astrTagValues);
    		if ( m_nPos < 0 ) {
    			m_nPos = 0;
        		return;
    		}
    	}
		m_nPos++;
    	if (m_nPos >= m_astrTagValues.length) m_nPos = 0;
    }
    
}
