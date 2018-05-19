package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.preferences.sources.SourceEntry;
import org.openstreetmap.josm.data.preferences.sources.SourceType;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.download.UserQueryList.SelectorItem;
import org.openstreetmap.josm.gui.io.CustomConfigurator;
import org.openstreetmap.josm.gui.mappaint.MapPaintStyles;
import org.openstreetmap.josm.gui.mappaint.StyleSource;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.StringSetting;
import org.openstreetmap.josm.tools.GBC;;

public final class PTWizardAction extends JosmAction {

	public boolean noDialogBox;
	/**
     * Constructs a new {@code PTWizardAction}
     */
    public PTWizardAction() {
        super(
                tr("PT Wizard"),
                "clock",
                tr("Set up PT Assistant for more convinient use"),
                null, false);

        putValue("help", ht("/Action/PTWizard"));
        putValue("toolbar", "help/PTWizard");
        MainApplication.getToolbar().register(this);
    }

	@Override
	public void actionPerformed(ActionEvent e) {

		boolean defaultAction = false;
		boolean action = false;
		if (this.noDialogBox) {
			Set<String> keySet = Main.pref.getKeySet();
			if (!keySet.contains("pt_assistant.wizard.pages")) {
				defaultAction = true;
			} else {
				String pages = Main.pref.get("pt_assistant.wizard.pages");
				if (pages.isEmpty()) {
					defaultAction = true;
				}
			}

			if (defaultAction) {
				action = false;
				readPreferencesFromXML();
				addDefaultValues();
			}
			this.noDialogBox = false;
		} else {
			action = true;
		}

		if (action) {

	        JPanel panel = new JPanel(new GridBagLayout());
	        panel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

	        ExtendedDialog ed = new ExtendedDialog(Main.parent,
	                tr("PT Wizard"),
	                tr("OK"), tr("Close"));
	        ed.setPreferredSize(new Dimension(250, 300));
	        ed.setButtonIcons("ok", "cancel");
	        JScrollPane scrollPanel = new JScrollPane(panel);
	        ed.setContent(scrollPanel, false);
    		    ed.setUndecorated(true);

	        nextAct(0, panel);

	        String pages = Main.pref.get("pt_assistant.wizard.pages");

	        try {
	        		for(int i=1;i<=Integer.parseInt(pages)+1;i++) {
	        			ExtendedDialog dialog = ed.showDialog();
	        			switch (dialog.getValue()) {
	        				case 1: nextAct(i, panel); break;
	        				default: return; // Do nothing
	        			}
	        		}
	        } catch (Exception excp) {
	        			excp.printStackTrace();
	        	}
		}
	}

	private void readPreferencesFromXML() {
		System.out.println("READING FROM XML FILE");

		try {
			CachedFile cf = getCachedFile();
			List<String> lines = new ArrayList<>();
			try (BufferedReader in = cf.getContentReader()){
				String line;
				while ((line = in.readLine()) != null) {
	                if (!line.contains("{{{") && !line.contains("}}}")) {
	                		lines.add(line);
	                }
	            }

	            in.close();
	            File f = new File(".tempfile.xml");
	            FileWriter fw = new FileWriter(f);
	            BufferedWriter out = new BufferedWriter(fw);
	            for(String s : lines)
	                 out.write(s);
	            out.flush();
	            out.close();
				fw.close();

				InputStream is = Files.newInputStream(f.toPath());
				new CustomConfigurator.XMLCommandProcessor(Main.pref).openAndReadXML(is);
				f.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

    }

	@SuppressWarnings("resource")
    public CachedFile getCachedFile() throws IOException {
        return new CachedFile("https://josm.openstreetmap.de/wiki/Plugin/PT_Assistant/Wizard?format=txt").setHttpAccept("text");
    }


	private void addDefaultValues() {
		String value1 = Main.pref.get("pt_assistant.wizard.1.suggestion");
		question1ChangeValues(value1);

		List<List<String>> question2Suggestion = Main.pref.getListOfLists("pt_assistant.wizard.2.suggestion");
		List<String> value2 = new ArrayList<>();
		for(int i=0;i<question2Suggestion.size();i++) {
			value2.add(question2Suggestion.get(i).get(0));
		}
		question2ChangeValues(value2);

		List<List<String>> question3Suggestion = Main.pref.getListOfLists("pt_assistant.wizard.3.suggestion");
		List<String> value3 = new ArrayList<>();
		for(int i=0;i<question3Suggestion.size();i++) {
			if (question3Suggestion.get(i).get(1) == "Default")
				value3.add(question3Suggestion.get(i).get(0));
		}
		question3ChangeValues(value3);

		List<List<String>> question4Suggestion = Main.pref.getListOfLists("pt_assistant.wizard.4.suggestion");
		List<String> value4 = new ArrayList<>();
		for(int i=0;i<question4Suggestion.size();i++) {
			if (question4Suggestion.get(i).get(1) == "Default")
				value4.add(question4Suggestion.get(i).get(0));
		}
		question4ChangeValues(value4);

	}


	private void addLabel(JPanel panel, int questionNumber, boolean Title, boolean Question, boolean Suggestion) {

		if (Title) {
			String title = Main.pref.get("pt_assistant.wizard."+ questionNumber +".title");
			JTextArea j = new JTextArea(tr(title));
			j.setLineWrap(true);
			j.setWrapStyleWord(true);
			j.setEditable(false);
			j.setOpaque(false);
	        j.setFont(new java.awt.Font("Serif", Font.BOLD, 18));
	        panel.add(j, GBC.eol().fill(GBC.HORIZONTAL));
		}

		if (Question) {
			String question = Main.pref.get("pt_assistant.wizard."+ questionNumber +".question");
			JTextArea j = new JTextArea(tr(question));
			j.setLineWrap(true);
			j.setWrapStyleWord(true);
			j.setEditable(false);
			j.setOpaque(false);
			panel.add(new JLabel("<html><br></html>"),GBC.eol().fill(GBC.HORIZONTAL));
	        panel.add(j, GBC.eol().fill(GBC.HORIZONTAL));
		}

		if (Suggestion) {
	        String suggestion = Main.pref.get("pt_assistant.wizard."+ questionNumber +".suggestion");
	        JTextArea j = new JTextArea(tr("suggested value : "+ suggestion));
			j.setLineWrap(true);
			j.setWrapStyleWord(true);
			j.setEditable(false);
			j.setOpaque(false);
			panel.add(new JLabel("<html><br></html>"),GBC.eol().fill(GBC.HORIZONTAL));
	        panel.add(j, GBC.eol().fill(GBC.HORIZONTAL));
		}
	}


	private void introduction(JPanel panel) {
		addLabel(panel, 0, true, false, false);

		String information = Main.pref.get("pt_assistant.wizard.0.information");
		JTextArea j = new JTextArea(tr(information));
		j.setLineWrap(true);
		j.setWrapStyleWord(true);
		j.setEditable(false);
		j.setOpaque(false);
		panel.add(new JLabel("<html><br></html>"),GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(j, GBC.eol().fill(GBC.HORIZONTAL));
	}


	private void question1(JPanel panel) {

		addLabel(panel, 1, true, true, false);

        String currentValue = Main.pref.get("properties.recently-added-tags");
        SpinnerModel model = new SpinnerNumberModel(Integer.parseInt(currentValue), 0, 30, 1);
        JSpinner tagsArea = new JSpinner(model);
        panel.add(new JLabel("<html><br></html>"),GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(tagsArea, GBC.eol().fill(GBC.HORIZONTAL));
	}

	private void question2(JPanel panel) {

		addLabel(panel, 2, true, true, false);

        Box checkBoxPanel = Box.createVerticalBox();
        checkBoxPanel.setOpaque(true);
        checkBoxPanel.setBackground(Color.white);

        List<List<String>> suggestionLists = Main.pref.getListOfLists("pt_assistant.wizard.2.suggestion");
        List<String> currentList = Config.getPref().getList("toolbar");

        try {
            for(int i=0;i<suggestionLists.size();i++) {
                String content = suggestionLists.get(i).get(0);
                String value = suggestionLists.get(i).get(1);
                JCheckBox con = new JCheckBox(content);
                if (currentList.contains(value))
                		con.setSelected(true);
                checkBoxPanel.add(con);
            }
        } catch (Exception e) {
        	     e.printStackTrace();
        }

        checkBoxPanel.setBackground(Color.white);
        checkBoxPanel.add(Box.createRigidArea(new Dimension(10,0)));

        panel.add(new JLabel("<html><br></html>"),GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(checkBoxPanel, GBC.eol().fill(GBC.HORIZONTAL));
	}

	private void question3(JPanel panel) {

		addLabel(panel, 3, true, true, false);

		Box checkBoxPanel = Box.createVerticalBox();
		checkBoxPanel.setOpaque(true);
		checkBoxPanel.setBackground(Color.white);

		List<List<String>> suggestionLists = Main.pref.getListOfLists("pt_assistant.wizard.3.suggestion");
		List<StyleSource> styleList = MapPaintStyles.getStyles().getStyleSources();
        try {
            for(int i=0;i<suggestionLists.size();i++) {
            	    String paintStyle = suggestionLists.get(i).get(0);
                JCheckBox con = new JCheckBox(paintStyle);
                for(StyleSource style : styleList) {
	    				if (style.title.equals(paintStyle)) {
	    					con.setSelected(true);
	    					break;
	    				}
	    			}
                checkBoxPanel.add(con);
            }
        } catch (Exception e) {
        	     e.printStackTrace();
        }

        checkBoxPanel.setBackground(Color.white);
        checkBoxPanel.add(Box.createRigidArea(new Dimension(10,0)));

        panel.add(new JLabel("<html><br></html>"),GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(checkBoxPanel, GBC.eol().fill(GBC.HORIZONTAL));
	}

	private void question4(JPanel panel) {

    		addLabel(panel, 4, true, true, false);

    		Box checkBoxPanel = Box.createVerticalBox();
    		checkBoxPanel.setOpaque(true);
    		checkBoxPanel.setBackground(Color.white);

        List<List<String>> suggestionLists = Main.pref.getListOfLists("pt_assistant.wizard.4.suggestion");

        Map<String, SelectorItem> items;
	    items = restorePreferences();

        try {
            for(int i=0;i<suggestionLists.size();i++) {
                String content = suggestionLists.get(i).get(0);
                JCheckBox con = new JCheckBox(content);
                if (items.containsKey(content))
                		con.setSelected(true);
                checkBoxPanel.add(con);
            }
        } catch (Exception e) {
        	     e.printStackTrace();
        }

        checkBoxPanel.setBackground(Color.white);
        checkBoxPanel.add(Box.createRigidArea(new Dimension(10,0)));

        panel.add(new JLabel("<html><br></html>"),GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(checkBoxPanel, GBC.eol().fill(GBC.HORIZONTAL));

	}

	private void question1Action(JPanel panel) {
		String value = new String();
		Component[] components = panel.getComponents();panel.getComponents();
		for (Component comp : components) {
			if (comp instanceof JSpinner) {
				JSpinner textField = (JSpinner) comp;
                Integer intValue = (Integer) textField.getValue();
                value = Integer.toString(intValue);
            }
		}

		question1ChangeValues(value);
	}

	/*
	 * the following function is common action for questions 2 to 4
	 */
	private void question1ChangeValues(String value) {
		StringSetting valueSetting = new StringSetting(value);
		System.out.println(valueSetting.getValue());

		Main.pref.putSetting("properties.recently-added-tags", valueSetting);
	}

	private void question2to4Action(JPanel panel, int questionNumber) {

		List<String> finalValues = new ArrayList<>();
		Component[] components = panel.getComponents();
		for (Component comp : components) {
			if (comp instanceof Box) {
				Box checkBox = (Box) comp;
				Component[] checkBoxComponents = checkBox.getComponents();
				for (Component checkComponent : checkBoxComponents) {
					if (checkComponent instanceof JCheckBox) {
					    JCheckBox checks = (JCheckBox) checkComponent;
					    if (checks.isSelected()) {
					        finalValues.add(checks.getText());
					    }
					}
				}

            }
		}

		switch (questionNumber) {
            case 2: question2ChangeValues(finalValues);break;
            case 3: question3ChangeValues(finalValues);break;
            case 4: question4ChangeValues(finalValues);break;
            default : // Do nothing
        }
	}


	private void question2ChangeValues(List<String> finalValues) {
		for(String fv : finalValues) {
			System.out.println(fv);
		}
		if(finalValues.contains("Sort stops in route relation"))
			MainMenu.add(MainApplication.getMenu().toolsMenu, new SortPTRouteMembersAction());

		List<String> current = Config.getPref().getList("toolbar");

		List<String> newList = new ArrayList<>();
		for ( String k : current){
			newList.add(k);
		}

		if (current.size() == 0) {
			String [] def = defaultToolBar();
			for ( String deftool : def){
				newList.add(deftool);
			}
		}

		List<List<String>> suggestionLists = Main.pref.getListOfLists("pt_assistant.wizard.2.suggestion");
		try {
            for(int i=0;i<suggestionLists.size();i++) {
                String content = suggestionLists.get(i).get(0);
                String value = suggestionLists.get(i).get(1);
                if (finalValues.contains(content) && !newList.contains(value))
        			newList.add(value);
        			else if (!finalValues.contains(content)){
        			    if (newList.contains(value))
        				    newList.remove(value);
        			}
            }
        } catch (Exception e) {
        	     e.printStackTrace();
        }

		List<String> t = new LinkedList<>(newList);
		try {
			Config.getPref().putList("toolbar", t);
			MainApplication.getToolbar().refreshToolbarControl();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void question3ChangeValues(List<String> finalValues) {
    	    List<StyleSource> styleList = MapPaintStyles.getStyles().getStyleSources();
    	    List<List<String>> suggestionLists = Main.pref.getListOfLists("pt_assistant.wizard.3.suggestion");

    	    try {
                for(int i=0;i<suggestionLists.size();i++) {
                	    String paintStyle = suggestionLists.get(i).get(0);
                	    String url = suggestionLists.get(i).get(2);

                	    if (finalValues.contains(paintStyle)) {
                	        	boolean exists = false;
                	    		for(StyleSource style : styleList) {
                	    			if (style.title.equals(paintStyle)) {
                	    				exists = true;
                	    				break;
                	    			}
                	    		}

                	    		if (!exists) {
                	    			SourceEntry source = new SourceEntry(SourceType.MAP_PAINT_STYLE, url, null, paintStyle, true);
                	        	    MapPaintStyles.addStyle(source);
                	    		}

                	    } else {
                	    		for(StyleSource style : styleList) {
                	    			if (style.title.equals(paintStyle)) {
                	    				SourceEntry source = new SourceEntry(SourceType.MAP_PAINT_STYLE, url, null, paintStyle, true);
                	    				MapPaintStyles.removeStyle(source);
                	    				break;
            	    			    }
            	    		    }
                	    }
                }
            } catch (Exception e) {
            	     e.printStackTrace();
            }
	}



	private void question4ChangeValues(List<String> finalValues) {
    	    List<List<String>> suggestionLists = Main.pref.getListOfLists("pt_assistant.wizard.4.suggestion");
    	    List<SelectorItem> itemList = new ArrayList<>();
    	    List<String> unmarkedKeyList = new ArrayList<>();

    	    Map<String, SelectorItem> items;
    	    items = restorePreferences();

    	    for(List<String> keys : suggestionLists) {
    		    String Key = keys.get(0);
    		    if (!finalValues.contains(Key))
    		    		unmarkedKeyList.add(Key);
    	    }

    	    for (String fv : finalValues) {
    	    	    if (!items.containsKey(fv)) {
    	    	    	    for(List<String> suggestions : suggestionLists) {
    	    	    		    String key = suggestions.get(0);
    	    	    		    if (key == fv) {
    	    	        	        String Value = "";
    	    	        	        for (int i=2;i<suggestions.size();i++) {
    	    	        	    		    Value = Value + suggestions.get(i);
    	    	        	        }
    	    	        	        SelectorItem item = new SelectorItem(key, Value);
    	    	        	        itemList.add(item);
    	    	        	        break;
    	    	    		    }
    			    }
    	    	    }

    	    }

    	    for (String unmarkedKey : unmarkedKeyList) {
    	    		if (items.containsKey(unmarkedKey)) {
    	    			items.remove(unmarkedKey);
    	    		}
    	    }

    	    for (SelectorItem item : itemList)
    	    		items.put(item.getKey(), item);
    	    try {
    	    		savePreferences(items);
    	    }  catch (Exception e) {
    	    		e.printStackTrace();
    	    }
    }


    /**
     * Loads the user saved items from {@link Main#pref}.
     * @return A set of the user saved items.
     */
    private Map<String, SelectorItem> restorePreferences() {
        Collection<Map<String, String>> toRetrieve =
                Config.getPref().getListOfMaps("download.overpass.queries", Collections.emptyList());
        Map<String, SelectorItem> result = new HashMap<>();
        final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss, dd-MM-yyyy");

        for (Map<String, String> entry : toRetrieve) {
            try {
                String key = entry.get("key");
                String query = entry.get("query");
                String lastEditText = entry.get("lastEdit");
                // Compatibility: Some entries may not have a last edit set.
                LocalDateTime lastEdit = lastEditText == null ? LocalDateTime.MIN : LocalDateTime.parse(lastEditText, FORMAT);

                result.put(key, new SelectorItem(key, query, lastEdit));
            } catch (Exception e) {
            		e.printStackTrace();
            }
        }

        return result;
    }


    /**
     * Saves all elements from the list to {@link Main#pref}.
     */
    private void savePreferences(Map<String, SelectorItem> items) {
    	    final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss, dd-MM-yyyy");
        List<Map<String, String>> toSave = new ArrayList<>(items.size());
        for (SelectorItem item : items.values()) {
            Map<String, String> it = new HashMap<>();
            it.put("key", item.getKey());
            it.put("query", item.getQuery());
            it.put("lastEdit", item.getLastEdit().format(FORMAT));

            toSave.add(it);
        }

        Config.getPref().putListOfMaps("download.overpass.queries", toSave);
    }






    private String[] defaultToolBar() {
    		String[] deftoolbar = {"open", "save", "download", "upload", "|",
    			    "undo", "redo", "|", "dialogs/search", "preference", "|", "splitway", "combineway",
    			    "wayflip", "|", "imagery-offset", "|", "tagginggroup_Highways/Streets",
    			    "tagginggroup_Highways/Ways", "tagginggroup_Highways/Waypoints",
    			    "tagginggroup_Highways/Barriers", "|", "tagginggroup_Transport/Car",
    			    "tagginggroup_Transport/Public Transport", "|", "tagginggroup_Facilities/Tourism",
    			    "tagginggroup_Facilities/Food+Drinks", "|", "tagginggroup_Man Made/Historic Places", "|",
    			    "tagginggroup_Man Made/Man Made"};
    		return deftoolbar;
    }



    private void nextAct(int pageNumber, JPanel panel) {

		switch (pageNumber) {
            case 2: question1Action(panel);break;
            case 3: question2to4Action(panel, 2);break;
            case 4: question2to4Action(panel, 3);break;
            case 5: question2to4Action(panel, 4);break;
            default : // Do nothing
        }

	    panel.removeAll();

	    switch (pageNumber) {
	        case 0: introduction(panel);break;
	        case 1: question1(panel);break;
	        case 2: question2(panel);break;
	        case 3: question3(panel);break;
	        case 4: question4(panel);break;
	        default : // Do nothing
	    }
	}
}
