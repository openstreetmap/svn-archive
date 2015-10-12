package org.openstreetmap.josm.plugins.tofix.controller;

import com.google.gson.Gson;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.plugins.tofix.bean.ListTaskBean;
import org.openstreetmap.josm.plugins.tofix.bean.TrackBean;
import org.openstreetmap.josm.plugins.tofix.util.Config;
import org.openstreetmap.josm.plugins.tofix.util.Request;

/**
 *
 * @author ruben
 */
public class ListTaskController {

    private ListTaskBean listTasksBean = null;
    private String url;

    public ListTaskController() {
        this.url = Config.HOST + "tasks";
    }

    public ListTaskBean getListTasksBean() {
        Gson gson = new Gson();
        String stringListTaskBean = null;
        try {
            stringListTaskBean = Request.sendGET(url);

        } catch (Exception ex) {
            Logger.getLogger(StatusController.class.getName()).log(Level.SEVERE, null, ex);
        }
        listTasksBean = gson.fromJson(stringListTaskBean, ListTaskBean.class);
        return listTasksBean;

    }

}
