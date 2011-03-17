/*
 * TraceyView.java
 */

package org.openstreetmap.tracey;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The application's main frame.
 */
public class TraceyView extends FrameView {

	//private boolean deleteEnabled = false;
	//private boolean uploadEnabled = false;

	private JFileChooser traceChooser;
	private TraceListModel traceListModel = new TraceListModel();

    public TraceyView(SingleFrameApplication app) {
        super(app);
		//Icon appIcon = this.getFrame().setIconImage();

        initComponents();

		System.setProperty("http.agent", "Tracey/0.1");

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = TraceyApp.getApplication().getMainFrame();
            aboutBox = new TraceyAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        TraceyApp.getApplication().show(aboutBox);
    }

	@Action
	public void addFiles() {
		if (traceChooser == null) {
			traceChooser = new JFileChooser();
			traceChooser.setMultiSelectionEnabled(true);
			traceChooser.setApproveButtonText("Add");
			FileFilter gpxFilter = new FileNameExtensionFilter("GPX Files", "gpx", "gpx.gz");
			traceChooser.addChoosableFileFilter(gpxFilter);
		}

		int retval = traceChooser.showDialog(TraceyApp.getApplication().getMainFrame(), "Add files");

		if (retval == JFileChooser.APPROVE_OPTION) {
			System.err.println("Files picked\n");
			File[] chosenTraces = traceChooser.getSelectedFiles();
			for (File newTrace : chosenTraces) {
				System.err.println("Adding: " + newTrace.getAbsolutePath());
				traceListModel.addElement(newTrace);
			}
		}
	}

	@Action //(enabledProperty = "deleteEnabled")
	public void removeFiles() {
		int[] filesToRemove = traceList.getSelectedIndices();
		for (int fileindex : filesToRemove) {
			System.err.println("Removing:" + traceListModel.getElementAt(fileindex));
			traceListModel.removeElementAt(fileindex);
		}
		traceList.clearSelection();
	}

	@Action
	public void clearFiles() {
		traceListModel.clear();
	}

	@Action
	public void setPreferences() {
		// Open preferences box and set prefs
        if (preferencesBox == null) {
            JFrame mainFrame = TraceyApp.getApplication().getMainFrame();
            preferencesBox = new TraceyPreferencesBox(mainFrame, true);
            preferencesBox.setLocationRelativeTo(mainFrame);
        }
        TraceyApp.getApplication().show(preferencesBox);
	}

	@Action
	public void uploadFiles() {
		String description;
		String tags;
		Privacy privacy;

		if (propertiesBox == null) {
				JFrame mainFrame = TraceyApp.getApplication().getMainFrame();
				propertiesBox = new TraceyPropertiesDialog(mainFrame, true);
				propertiesBox.setLocationRelativeTo(mainFrame);
        }
		TraceyApp.getApplication().show(propertiesBox);

		if (propertiesBox.getReturnStatus() == TraceyPropertiesDialog.UPLOAD) {
			description = propertiesBox.getDescription();
			tags = propertiesBox.getTags();
			privacy = propertiesBox.getPrivacy();
			GpxUpload uploader = new GpxUpload();
			for (File fileToUpload : traceListModel) {
				try {
					uploader.upload(description, tags, privacy, fileToUpload);
					Thread.sleep(60000);
					//traceListModel.remove(fileToUpload);
				}
				catch (IOException fault) {
					System.err.println("IOException: " + fault.getMessage());
				}
				catch (InterruptedException interruption) {
				}
			}
			traceListModel.clear();
		}
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        traceList = new javax.swing.JList();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        addFilesMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        preferencesMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        toolBar = new javax.swing.JToolBar();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(366, 366));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        traceList.setModel(traceListModel);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.openstreetmap.tracey.TraceyApp.class).getContext().getResourceMap(TraceyView.class);
        traceList.setToolTipText(resourceMap.getString("traceList.toolTipText")); // NOI18N
        traceList.setDragEnabled(true);
        traceList.setName("traceList"); // NOI18N
        jScrollPane1.setViewportView(traceList);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.openstreetmap.tracey.TraceyApp.class).getContext().getActionMap(TraceyView.class, this);
        addFilesMenuItem.setAction(actionMap.get("addFiles")); // NOI18N
        addFilesMenuItem.setText(resourceMap.getString("addFilesMenuItem.text")); // NOI18N
        addFilesMenuItem.setName("addFilesMenuItem"); // NOI18N
        fileMenu.add(addFilesMenuItem);

        jMenuItem1.setAction(actionMap.get("uploadFiles")); // NOI18N
        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setToolTipText(resourceMap.getString("jMenuItem1.toolTipText")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        jMenuItem2.setAction(actionMap.get("clearFiles")); // NOI18N
        jMenuItem2.setText(resourceMap.getString("clearListMenuItem.text")); // NOI18N
        jMenuItem2.setName("clearListMenuItem"); // NOI18N
        fileMenu.add(jMenuItem2);

        preferencesMenuItem.setAction(actionMap.get("setPreferences")); // NOI18N
        preferencesMenuItem.setText(resourceMap.getString("preferencesMenuItem.text")); // NOI18N
        preferencesMenuItem.setToolTipText(resourceMap.getString("preferencesMenuItem.toolTipText")); // NOI18N
        preferencesMenuItem.setName("preferencesMenuItem"); // NOI18N
        fileMenu.add(preferencesMenuItem);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setFocusable(false);
        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 182, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setAlignmentX(5.0F);
        toolBar.setAlignmentY(5.0F);
        toolBar.setName("toolBar"); // NOI18N

        addButton.setAction(actionMap.get("addFiles")); // NOI18N
        addButton.setIcon(resourceMap.getIcon("addButton.icon")); // NOI18N
        addButton.setText(resourceMap.getString("addButton.text")); // NOI18N
        addButton.setToolTipText(resourceMap.getString("addButton.toolTipText")); // NOI18N
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setMaximumSize(new java.awt.Dimension(43, 53));
        addButton.setMinimumSize(new java.awt.Dimension(43, 53));
        addButton.setName("addButton"); // NOI18N
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(addButton);

        deleteButton.setAction(actionMap.get("removeFiles")); // NOI18N
        deleteButton.setIcon(resourceMap.getIcon("deleteButton.icon")); // NOI18N
        deleteButton.setText(resourceMap.getString("deleteButton.text")); // NOI18N
        deleteButton.setToolTipText(resourceMap.getString("deleteButton.toolTipText")); // NOI18N
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setName("deleteButton"); // NOI18N
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        toolBar.add(deleteButton);

        uploadButton.setAction(actionMap.get("uploadFiles")); // NOI18N
        uploadButton.setIcon(resourceMap.getIcon("uploadButton.icon")); // NOI18N
        uploadButton.setText(resourceMap.getString("uploadButton.text")); // NOI18N
        uploadButton.setToolTipText(resourceMap.getString("uploadButton.toolTipText")); // NOI18N
        uploadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        uploadButton.setName("uploadButton"); // NOI18N
        uploadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(uploadButton);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(toolBar);
    }// </editor-fold>//GEN-END:initComponents

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed

	}//GEN-LAST:event_deleteButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JMenuItem addFilesMenuItem;
    private javax.swing.JButton deleteButton;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JList traceList;
    private javax.swing.JButton uploadButton;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
	private JDialog preferencesBox;
	private TraceyPropertiesDialog propertiesBox;
}
