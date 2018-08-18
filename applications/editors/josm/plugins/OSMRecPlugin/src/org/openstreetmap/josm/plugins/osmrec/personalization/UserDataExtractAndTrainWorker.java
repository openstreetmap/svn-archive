// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.personalization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.SwingWorker;

import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;
import org.openstreetmap.josm.plugins.osmrec.extractor.LanguageDetector;
import org.openstreetmap.josm.plugins.osmrec.parsers.OSMParser;

/**
 * Extracts user information about history edits or by area and initiates the training process based on that user.
 *
 * @author imis-nkarag
 */

public class UserDataExtractAndTrainWorker extends SwingWorker<Void, Void> implements ActionListener {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final ArrayList<String> timeIntervals = new ArrayList<>();
    private final String username;
    private final Integer days;
    private List<OSMWay> wayList = new ArrayList<>();
    private final boolean byArea;
    private final String inputFilePath;
    private final boolean validateFlag;
    private final double cParameterFromUser;
    private final int topK;
    private final int frequency;
    private final boolean topKIsSelected;
    private final LanguageDetector languageDetector;

    public UserDataExtractAndTrainWorker(String inputFilePath, String username, Integer days, boolean byArea, boolean validateFlag,
            double cParameterFromUser, int topK, int frequency, boolean topKIsSelected, LanguageDetector languageDetector) {

        this.inputFilePath = inputFilePath;
        this.username = username;
        this.days = days;
        this.byArea = byArea;
        this.validateFlag = validateFlag;
        this.cParameterFromUser = cParameterFromUser;
        this.topK = topK;
        this.frequency = frequency;
        this.topKIsSelected = topKIsSelected;
        this.languageDetector = languageDetector;

    }

    @Override
    protected Void doInBackground() throws Exception {

        System.out.println("UserDataExtractAndTrainWorker doInBackground initiating..");
        if (byArea) {
            extractByArea();
        } else {
            extractHistory();
        }

        TrainByUser trainByUser = new TrainByUser(inputFilePath, username, validateFlag,
                cParameterFromUser, topK, frequency, topKIsSelected, languageDetector, wayList);

        System.out.println("trainByUser executing..");
        trainByUser.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    int progress = (Integer) evt.getNewValue();
                    System.out.println("progress++ from property change listener, progress: " + progress);
                    setProgress(progress);

                }
            }
        });

        trainByUser.doInBackground();
        setProgress(100);
        return null;
    }

    private void extractHistory() {
        if (wayList != null) {
            wayList.clear();
        }

        produceTimeIntervals(days);
        HistoryParser historyParser = new HistoryParser(username);

        for (String time : timeIntervals) {
            System.out.println("interval\n " + time);
            historyParser.historyParse(time);
        }
        wayList = historyParser.getWayList();
    }

    private void extractByArea() {
        System.out.println("Extracting by Area..");

        if (wayList != null) {
            wayList.clear();
        }

        OSMParser osmParser = new OSMParser(inputFilePath);
        osmParser.parseDocument();

        List<OSMWay> completeWayList = osmParser.getWayList();
        System.out.println("completeWayList size: " + completeWayList.size());
        System.out.println("populating wayList with edits from username: " + username);
        for (OSMWay way : completeWayList) {
            //System.out.println("current way user: " + way.getUser());
            if (way.getUser().equals(username)) {
                System.out.println("found user edit!");
                wayList.add(way);
            }
        }
        System.out.println("weeding wayList by user done.");
        if (wayList.isEmpty()) {
            System.out.println("User has not edited this Area. Try \"By time\" option.");
        } else {
            System.out.println("User has edited " + wayList.size() + " OSM entities in this area.");
        }
    }

    private void produceTimeIntervals(Integer days) {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(currentDate);
        String currentTimeString = sdf.format(currentDate);

        cal.add(Calendar.DATE, -days); //starting date
        Date startingTime = cal.getTime();

        String startingTimeString = sdf.format(startingTime);
        //add to list

        String nextIntervalTime = startingTimeString;

        //do, while date is before current date,after the addition of the 10 days
        do {
            cal.add(Calendar.DATE, 10);

            Date intervalTime = cal.getTime();
            String intervalTimeString = sdf.format(intervalTime);
            String timeOsmApiArgument = nextIntervalTime + "," + intervalTimeString;

            if (cal.getTime().after(currentDate)) {
                //add the offset of remaining days as last interval
                timeOsmApiArgument = nextIntervalTime + "," + currentTimeString;
                timeIntervals.add(timeOsmApiArgument);
                System.out.println(timeOsmApiArgument);
                break;
            } else {
                timeIntervals.add(timeOsmApiArgument);
            }
            nextIntervalTime = intervalTimeString;
            System.out.println(" ti: " + timeOsmApiArgument);
        } while (cal.getTime().before(currentDate));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        //this.firePropertyChange("progress", userTrainProgress, userTrainProgress+10);
        //        if (userTrainProgress <100) {
        //            setProgress(userTrainProgress+10);
        //        }

    }
}
