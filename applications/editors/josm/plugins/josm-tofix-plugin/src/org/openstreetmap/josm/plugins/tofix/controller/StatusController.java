package org.openstreetmap.josm.plugins.tofix.controller;

import com.google.gson.Gson;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.plugins.tofix.bean.StatusBean;
import org.openstreetmap.josm.plugins.tofix.util.Request;

/**
 *
 * @author ruben
 */
public class StatusController {

    private StatusBean statusBean;
    private final String url;
    Gson gson = new Gson();

    public StatusController(String url) {
        this.url = url;
    }

    public StatusBean getStatusBean() {
        String stringStatusBean = null;
        try {
            stringStatusBean = Request.sendGET(url);
        } catch (Exception ex) {
            Logger.getLogger(StatusController.class.getName()).log(Level.SEVERE, null, ex);
        }
        statusBean = gson.fromJson(stringStatusBean, StatusBean.class);
        return statusBean;
    }
}
