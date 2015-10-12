package org.openstreetmap.josm.plugins.tofix.bean;

/**
 *
 * @author ruben
 */
public class TrackBean {

    AttributesBean attributes = new AttributesBean();

    public AttributesBean getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributesBean attributes) {
        this.attributes = attributes;
    }
   
    //Atributos
    public class AttributesBean {

        String user;
        String action;
        String key;
        String editor;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getEditor() {
            return editor;
        }

        public void setEditor(String editor) {
            this.editor = editor;
        }

    }

}
