package org.openstreetmap.josm.plugins.tofix.bean;
/**
 *
 * @author ruben
 */
public class TaskBean {

    private String id;
    private String title;
    private String source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
