/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;

/**
 * @author cdaller
 *
 */
public class SystemExecuteAction extends AbstractSurveyorAction {

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.SurveyorAction#actionPerformed(at.dallermassl.josm.plugin.surveyor.GpsActionEvent, java.util.List)
     */
    //@Override
    public void actionPerformed(GpsActionEvent event) {
        final ProcessBuilder builder = new ProcessBuilder(getParameters());
        //Map<String, String> environ = builder.environment();
        builder.directory(new File(System.getProperty("user.home")));

        System.out.println("Directory : " + builder.directory());
        Thread executionThread = new Thread() {

            /* (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                try {
                    final Process process = builder.start();
                    InputStream is = process.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;

                    while ((line = br.readLine()) != null) {
                        System.out.println(getClass().getSimpleName() + ": " +  line);
                    }

                    System.out.println(getClass().getSimpleName() + "Program terminated!");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

        };
        executionThread.start();
//        try {
//            System.in.read();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}
