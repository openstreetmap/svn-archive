package org.openstreetmap.josm.plugins.osmrec.personalization;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.opengis.referencing.FactoryException;
import org.openstreetmap.josm.plugins.container.OSMWay;
import org.openstreetmap.josm.plugins.core.TrainWorker;
import org.openstreetmap.josm.plugins.extractor.LanguageDetector;
import org.openstreetmap.josm.plugins.parsers.OSMParser;

/**
 * Extracts user information about history edits or by area and initiates the training process based on that user.
 * 
 * @author imis-nkarag
 */

public class UserDataExtractAndTrainWorker extends SwingWorker<Void, Void> {
    
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final ArrayList<String> timeIntervals = new ArrayList<>();
    private final String username;
    private final Integer days;
    private List<OSMWay> wayList;
    private final boolean byArea;
    private final String inputFilePath;
    private final boolean validateFlag;
    private final double cParameterFromUser;
    private final int topK;
    private final int frequency;
    private final boolean topKIsSelected;
    private final LanguageDetector languageDetector;
    
    public UserDataExtractAndTrainWorker(String inputFilePath, String username, Integer days, boolean byArea, boolean validateFlag, 
            double cParameterFromUser, int topK, int frequency, boolean topKIsSelected, LanguageDetector languageDetector){
       
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
        if(byArea){
           extractByArea(); 
        }
        else{ 
           extractHistory(); 
        }
       
        TrainByUser trainByUser = new TrainByUser(inputFilePath, username, validateFlag, 
                cParameterFromUser, topK, frequency, topKIsSelected, languageDetector, wayList);

        trainByUser.executeTraining();
        return null;
    }
    
    private void extractHistory() {
        wayList.clear();
        produceTimeIntervals(days);
        HistoryParser historyParser = new HistoryParser(username);
        
        for(String time : timeIntervals){
            System.out.println("interval\n " + time);
            historyParser.historyParse(time);
        }
        wayList = historyParser.getWayList();
    }
    
    private void extractByArea() {
        wayList.clear();
        OSMParser osmParser = null;
        try {
            osmParser = new OSMParser(inputFilePath);
            
        } catch (FactoryException ex) {
            Logger.getLogger(TrainWorker.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        List<OSMWay> completeWayList = osmParser.getWayList();
        for(OSMWay way : completeWayList){
            if(way.getUser().equals(username)){
                wayList.add(way);
            }            
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
        //System.out.println("starting date: " + startingTimeString);
        //add to list 
        
        String nextIntervalTime = startingTimeString;
        
        //do, while date is before current date,after the addition of the 10 days        
        do {
            cal.add(Calendar.DATE, 10);
            
            Date intervalTime = cal.getTime();
            String intervalTimeString = sdf.format(intervalTime);
            String timeOsmApiArgument = nextIntervalTime + "," + intervalTimeString;        
            
            if(cal.getTime().after(currentDate)){
                //add the offset of remaining days as last interval
                timeOsmApiArgument = nextIntervalTime + "," + currentTimeString;
                timeIntervals.add(timeOsmApiArgument);
                System.out.println(timeOsmApiArgument);
                break;
            }
            else{
                timeIntervals.add(timeOsmApiArgument);
            }
            nextIntervalTime = intervalTimeString;
            System.out.println(" ti: " + timeOsmApiArgument);
        } while(cal.getTime().before(currentDate));
    }
}
