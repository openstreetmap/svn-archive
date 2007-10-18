package UtilsPlugin.JosmLint;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import java.awt.event.KeyEvent;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class JosmLint extends ToggleDialog implements SelectionChangedListener {
        /**
         * The selection's list data.
         */
        private final DefaultListModel list = new DefaultListModel();
        private final Map<OsmPrimitive,JosmLintTestResult> results = new TreeMap<OsmPrimitive,JosmLintTestResult>();
        /**
         * The display list.
         */
        private JList displaylist = new JList(list);
        List<JosmLintTest> tests = new ArrayList<JosmLintTest>();

        private class JosmLintWorker implements Runnable
        {
                public boolean stop;
                private JosmLint parent;
                
                public JosmLintWorker(JosmLint p)
                {
                        parent = p;
                        stop = false;
                }
                
                public void run()
                {
			/* Background loop, checks 200 objects per second... */
                        while( !stop )
                        {
                                Collection<OsmPrimitive> ds = Main.ds.allNonDeletedPrimitives();
                                for( OsmPrimitive o : ds )
                                {
                                        if( stop )
                                                break;
                                        parent.checkObject(o);
                                        simpleSleep(5);
                                }
                                simpleSleep(10000);
                        }
                }
        }
        
        private static void simpleSleep( int millis )
        {
                try {
                        Thread.sleep(millis);
                } 
                catch(InterruptedException e) {}
        }
        private void checkObject( OsmPrimitive o )
        {
                JosmLintTestResult res = results.get(o);
                
                if( res != null )
                {
                      if( !res.recheck() )
                      {
                              results.remove(o);
                              list.removeElement(res);
                              res = null;
                      }
                }
                if( res != null )
                        return;

                for( JosmLintTest test : tests )
                {
                        res = test.runTest(o);
                        if( res != null )
                        {
                                System.out.println( "Got test failure: "+res );
                                
                                results.put(o,res);
                                list.addElement(res);
                                break;
                        }
                }
        }

        private JosmLintWorker worker;
        private static JosmLint lint;
        
        public JosmLint()
        {
                super( tr("JosmLint"), "josmlint", tr("Scans the current data for problems"), KeyEvent.VK_J, 150 );
                add(new JScrollPane(displaylist), BorderLayout.CENTER);
                displaylist.addMouseListener(new MouseAdapter(){
                        @Override public void mouseClicked(MouseEvent e) {
                                if (e.getClickCount() < 2)
                                        return;
                                selectObject();
                        }
                });
                                                                                                                                                                                        		
                tests.add( new ConsistancyTest() );
                tests.add( new WayCheckTest() );
        }

        @Override public void setVisible(boolean b) {
                if (b) {
                        try { Main.ds.addSelectionChangedListener(this); }
                        catch( NoSuchMethodError e )
                        {
                                try {
                                java.lang.reflect.Field f = DataSet.class.getDeclaredField("listeners");
                                ((Collection<SelectionChangedListener>)f.get(Main.ds)).add(this);
//                                Main.ds.listeners.add(this);
                                } catch (Exception x) { System.out.println( e ); }
                        }
                        selectionChanged(Main.ds.getSelected());
                        worker = new JosmLintWorker(this);
                        new Thread(worker).start();
                } else {
                        try { Main.ds.removeSelectionChangedListener(this); }
                        catch( NoSuchMethodError e )
                        {
                                try {
                                java.lang.reflect.Field f = DataSet.class.getDeclaredField("listeners");
                                ((Collection<SelectionChangedListener>)f.get(Main.ds)).remove(this);
//                                Main.ds.listeners.remove(this);
                                } catch (Exception x) { System.out.println( e ); }
                        }
			if( worker != null )
			{
			        worker.stop = true;
//				worker.join();
		        	worker = null;
                        }
                }
                super.setVisible(b);
        }
        public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
                if (list == null)
                        return; // selection changed may be received in base class constructor before init
		for( OsmPrimitive o : newSelection )
			checkObject(o);
	}
        /* When user doubleclicks an item, select that object */
	public void selectObject()
	{
	        JosmLintTestResult res = (JosmLintTestResult)displaylist.getSelectedValue();
	        OsmPrimitive obj = res.getSelection();
	        Main.ds.setSelected(obj);
	}


	public static void setupPlugin()
	{
		JPanel toggleDialogs = null;
		JToolBar toolBarActions = Main.map.toolBarActions;

		// Find the toggleDialogs
		for( final java.awt.Component c : Main.map.getComponents() )
		{
			if( c.getClass() != JPanel.class )
				continue;
			JPanel c2 = (JPanel)c;
			
			if( c2.getLayout().getClass() != BoxLayout.class )
				continue;
//			System.out.println( "Found: "+ c2.getComponent(1).getClass() );
			toggleDialogs = c2;
			break;
		}

		if( toggleDialogs == null )
		{
		        System.out.println( "Failed to insert dialog" );
		        return;
                }
                lint = new JosmLint();
                lint.addIconToggle( toggleDialogs, toolBarActions );
	}
        private void addIconToggle(JPanel toggleDialogs, JToolBar toolBarActions) {
                IconToggleButton button = new IconToggleButton(this.action);
                this.action.button = button;
                this.parent = toggleDialogs;
                toolBarActions.add(button);
                toggleDialogs.add(this);
        }
        public static void stopPlugin()
        {
                if( lint.worker != null )
                        lint.worker.stop = true;
        }
}
