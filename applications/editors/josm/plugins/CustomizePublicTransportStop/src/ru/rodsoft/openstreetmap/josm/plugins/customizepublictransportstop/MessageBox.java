package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;
import javax.swing.JOptionPane;
import java.awt.Component;
/** 
 * 
 * @author Moderator
 * MessageBox static class
 * It is used for debug
 * Source: Java forums <a>http://www.java-forums.org/java-tip/6578-messagebox-example.html</a>
 */
public class MessageBox {
	    /* 
	    These are a list of STATIC MODAL dialogs
	 
	    int return codes of button pressed:
	 
	        -1 - WINDOW CLOSED - the X PRESSED
	         0 - YES and OK
	         1 - NO
	         2 - CANCEL
	 
	    (thanks to flipside for the idea)
	     */
	 
	/**
	 * Show message box with "yes" and "no" buttons
	 * @param theMessage Message for user
	 * @return Code of pressed button
	 */
	    public static int yesno(String theMessage){
	        int result = JOptionPane.showConfirmDialog((Component)
	                null, theMessage, "alert", JOptionPane.YES_NO_OPTION);
	        return result;
	    }
	 
		/**
		 * Show message box with "yes", "no" and "cancel" buttons
		 * @param theMessage Message for user
		 * @return Code of pressed button
		 */
	    public static int yesnocancel(String theMessage){
	        int result = JOptionPane.showConfirmDialog((Component)
	                null, theMessage, "alert", JOptionPane.YES_NO_CANCEL_OPTION);
	        return result;
	    }
	 
		/**
		 * Show message box with "Ok" and "Cancel" buttons
		 * @param theMessage Message for user
		 * @return Code of pressed button
		 */
	    public static int okcancel(String theMessage){
	        int result = JOptionPane.showConfirmDialog((Component)
	                null, theMessage, "alert", JOptionPane.OK_CANCEL_OPTION);
	        return result;
	    }
	 
		/**
		 * Show message box with "ok" button
		 * @param theMessage Message for user
		 * @return Code of pressed button
		 */
	    public static int ok(String theMessage){
	        int result = JOptionPane.showConfirmDialog((Component)
	                null, theMessage, "alert", JOptionPane.DEFAULT_OPTION);
	        return result;
	    }
	 
	    /**
	     * Main method for test launch
	     * 
	     * @param args Arguments of command line
	     */
	    public static void main(String args[]){
	        int i = MessageBox.yesno("Are your sure ?");
	        System.out.println("ret : " + i );
	        i = MessageBox.yesnocancel("Are your sure ?");
	        System.out.println("ret : " + i );
	        i = MessageBox.okcancel("Are your sure ?");
	        System.out.println("ret : " + i );
	        i = MessageBox.ok("Done.");
	        System.out.println("ret : " + i );
	    }
}
