package org.openstreetmap.osmolt.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import org.openstreetmap.osmolt.OutputInterface;
import org.openstreetmap.osmolt.TranslationAccess;

public class ErrorReporter extends JFrame {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  TranslationAccess translation;
  
  OutputInterface gui;
  
  String message = "";
  
  JTextArea comment;
   
 
  public ErrorReporter(OutputInterface gui, TranslationAccess translation, Throwable error) {
    this.translation = translation;
    this.gui = gui;
    
    setTitle(translation.translate("report_Bug"));
        
    message += "errorname: " + error.getClass().getName() + "\n";
    message += "LocalizedMessage: " + error.getLocalizedMessage() + "\n";
    message += "Message: " + error.getMessage() + "\n";
    message += "Trace:\n";
    
    StackTraceElement[] stack = error.getStackTrace();
    for (StackTraceElement element : stack) {
      message += element.toString() + "\n";
    }
    JButton developer = new JButton(translation.translate("developer"));
    JButton clipboard = new JButton(translation.translate("clipboard"));
    JButton cancel = new JButton(translation.translate("cancel"));
    
    developer.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
        writeToDeveloper();
        close();
      }
    });
    clipboard.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
        writeToClipboard();
        close();
      }
    });
    cancel.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
        close();
      }
    });
    JTextArea errorPanel = new JTextArea(message,10,60);
    
    comment = new JTextArea(5,60);
    
    errorPanel.setEditable(false);
    JPanel p = new JPanel(new MigLayout());
    p.add(new JScrollPane(errorPanel),"wrap");
    p.add(new JLabel(translation.translate("comment")),"wrap");
    p.add(new JScrollPane(comment),"wrap");
    p.add(new JLabel(translation.translate("question_copy_error_to_clipboard_or_send_to_developer")),"wrap");
    p.add(developer,"split 3");
    p.add(clipboard);
    p.add(cancel);
    add(p);
  }
  
  /**
   * Writes to clipboard source: http://www.devx.com/java/Article/22326/0/page/4
   * 
   * @param writeMe
   */
  public void writeToClipboard() {
    // get the system clipboard
    Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    // set the textual content on the clipboard to our Transferable
    // object we use the
    Transferable transferableText = new StringSelection(message);
    systemClipboard.setContents(transferableText, null);
  }
  
  /**
   * 
   * 
   * @param writeMe
   */
  public void writeToDeveloper() {
    String developperURL = "http://osm.youseeus.de/osmolt/bug/send.php";
    URL url;
    try {
      // setup url connection. use POST to send forms data
      url = new URL(developperURL);
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      urlConn.setRequestMethod("POST");
      urlConn.setDoInput(true);
      urlConn.setDoOutput(true);
      urlConn.setUseCaches(false);
      urlConn.setAllowUserInteraction(true);
      HttpURLConnection.setFollowRedirects(true);
      urlConn.setInstanceFollowRedirects(true);
      urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
      String content = "bugtrace=" + message+"\nComment:\n"+comment.getText();
      gui.printDebugMessage("osmoltGui", "sending form to HTTP server ...");
      out.writeBytes(content);
      out.flush();
      out.close();
      // get input connection
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      String line;
      
      try {
        gui.printDebugMessage("osmoltGui", "reading HTML from HTTP server ...");
        while ((line = in.readLine()) != null) {
          System.out.println(line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      gui.printDebugMessage("osmoltGui", "done.");
      OsmoltGui.osmoltGui.printMessage("Error sent successfully");
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  public void close() {
    dispose();
  }
  
}
