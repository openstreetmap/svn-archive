package org.openstreetmap.josm.plugins.lakewalker;

import java.io.File;
import java.util.ArrayList;

import org.openstreetmap.josm.gui.progress.NullProgressMonitor;

public class LakewalkerApp {
    public static void main(String[] args){
        double lat = 52.31384;
        double lon = -79.135;
        double toplat = 52.3165;
        double botlat = 52.3041;
        double leftlon = -79.1442;
        double rightlon = -79.1093;

        // ?lat=39.15579999999999&lon=2.9411&zoom=12&layers=B000F000F
        lat = 39.1422;
        lon = 2.9102;

        toplat = 39.2229;
        botlat = 39.0977;
        leftlon = 2.8560;
        rightlon = 3.0462;

        int waylen = 250;
        int maxnode = 5000;
        int threshold = 100;
        double dp = 0.0003;
        int tilesize = 2000;
        int resolution = 4000;
        String startdir = "East";
        String wmslayer = "IR2";

        File working_dir = new File("Lakewalker");

        ArrayList<double[]> nodelist = null;

        Lakewalker lw = new Lakewalker(waylen,maxnode,threshold,dp,resolution,tilesize,startdir,wmslayer,working_dir);
        try {
            nodelist = lw.trace(lat,lon,leftlon,rightlon,toplat,botlat, NullProgressMonitor.INSTANCE);
        } catch(LakewalkerException e){
            System.out.println(e.getError());
        }

        System.out.println(nodelist.size()+" nodes generated");

        nodelist = lw.vertexReduce(nodelist, dp);

        System.out.println("After vertex reduction, "+nodelist.size()+" nodes remain.");

        nodelist = lw.douglasPeucker(nodelist, dp, 0);

        System.out.println("After dp approximation, "+nodelist.size()+" nodes remain.");



    }
}
