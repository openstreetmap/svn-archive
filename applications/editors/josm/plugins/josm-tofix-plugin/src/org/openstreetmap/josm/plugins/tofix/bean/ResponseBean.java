package org.openstreetmap.josm.plugins.tofix.bean;

/**
 * 
 *
 * @author ruben
 */
public class ResponseBean {

    private int status;
    private String value;//String from server response 

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
