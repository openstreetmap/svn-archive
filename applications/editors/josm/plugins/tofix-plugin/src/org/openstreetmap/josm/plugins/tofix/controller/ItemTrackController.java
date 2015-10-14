package org.openstreetmap.josm.plugins.tofix.controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.plugins.tofix.bean.FixedBean;
import org.openstreetmap.josm.plugins.tofix.bean.TrackBean;
import org.openstreetmap.josm.plugins.tofix.util.Request;

public class ItemTrackController {
    public void send_track_edit(String url, TrackBean trackBean) {
        Gson gson = new Gson();
        String string_obj = gson.toJson(trackBean);
        try {
            Request.sendPOST_Json(url, string_obj);
        } catch (IOException ex) {
            Logger.getLogger(ItemTrackController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void send_track_skip(String url, TrackBean trackBean) {
        Gson gson = new Gson();
        String string_obj = gson.toJson(trackBean);
        try {
            Request.sendPOST_Json(url, string_obj);
        } catch (IOException ex) {
            Logger.getLogger(ItemTrackController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void send_track_fix(String url, FixedBean fixedBean) {
        Gson gson = new Gson();
        String string_obj = gson.toJson(fixedBean);
        try {
            Request.sendPOST_Json(url, string_obj);
        } catch (IOException ex) {
            Logger.getLogger(ItemTrackController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void send_track_noterror(String url, FixedBean NoterrorBean) {
        Gson gson = new Gson();
        String string_obj = gson.toJson(NoterrorBean);
        try {
            Request.sendPOST_Json(url, string_obj);
        } catch (IOException ex) {
            Logger.getLogger(ItemTrackController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
