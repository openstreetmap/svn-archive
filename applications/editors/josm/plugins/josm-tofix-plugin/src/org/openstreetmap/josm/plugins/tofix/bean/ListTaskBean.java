package org.openstreetmap.josm.plugins.tofix.bean;

import java.util.List;

/**
 *
 * @author ruben
 */
public class ListTaskBean {

    private List<TaskBean> tasks;

    public List<TaskBean> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskBean> tasks) {
        this.tasks = tasks;
    }
}
