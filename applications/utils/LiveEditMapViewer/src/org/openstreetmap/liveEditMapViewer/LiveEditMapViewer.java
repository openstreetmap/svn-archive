package org.openstreetmap.liveEditMapViewer;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;

import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapViewChangeListener;
import org.openstreetmap.gui.jmapviewer.interfaces.OverlayPainter;
import org.openstreetmap.osmosis.core.xml.common.DateParser;

public class LiveEditMapViewer extends JFrame implements MapViewChangeListener,
		KeyListener, MouseListener, OverlayPainter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2976479683829295126L;

	public static LiveEditMapViewer instance;

	private JMapViewer map;
	private Timer repaintTimer;
	private Timer refetchTimer;

	private int[] drawX;
	private int[] drawY;
	private double[] drawLat;
	private double[] drawLon;

	private int[] drawMode;
	private long[] drawTime;
	private long[] drawID;
	private Image overlayI;

	private long currTime;
	private int seqNum;

	private Desktop desk;

	public LiveEditMapViewer() {
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		this.setTitle("LiveEditMapViewer");
		this.setLayout(gbl);
		this.addKeyListener(this);

		desk = Desktop.getDesktop();

		map = new JMapViewer();
		map.addChangeListener(this);
		map.addOverlayPainter(this);
		DefaultMapController mapController = new DefaultMapController(map);
		mapController.setMovementMouseButton(MouseEvent.BUTTON1);
		map.setSize(800, 800);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		gbc.gridwidth = 1;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(map, gbc);
		pack();
		setVisible(true);
		map.addKeyListener(this);
		map.addMouseListener(this);

		overlayI = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		repaintTimer = new Timer();
		repaintTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (drawX != null) {
					overlayI = new BufferedImage(getWidth(), getHeight(),
							BufferedImage.TYPE_INT_ARGB);
					Graphics g = overlayI.getGraphics();
					g.setColor(Color.BLACK);
					int noDisp = 0;
					synchronized (LiveEditMapViewer.instance) {
						for (int i = 0; i < drawX.length; i++) {
							if (drawX[i] < 0)
								continue;
							long deltaTime = currTime - drawTime[i];
							if (deltaTime > 0) {
								noDisp++;
								deltaTime = (long) (200.0 * Math.exp(-1.0
										* deltaTime / 20000.0)) + 50;

								// System.out.println(deltaTime);
								switch (drawMode[i]) {
								case 0: {
									g.setColor(new Color(0, 0, 255,
											(int) deltaTime));
									break;
								}
								case 1: {
									g.setColor(new Color(0, 255, 0,
											(int) deltaTime));
									break;
								}
								case 2: {
									g.setColor(new Color(255, 0, 0,
											(int) deltaTime));
									break;
								}
								}
								g.drawRect(drawX[i] - 2, drawY[i] - 2, 3, 3);
							}

						}
					}
					// System.out.println("Displaying " + noDisp + " nodes");

				}
				repaint();
				currTime += 1000;
			}

		}, 1000, 1000);

		refetchTimer = new Timer();
		refetchTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				setupLatLonArray();
			}

		}, 20000, 30000);

	}

	private void initChangeStream() {
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							new BufferedInputStream(
									new URL(
											"http://planet.openstreetmap.org/minute-replicate/state.txt")
											.openStream())));
			br.readLine();
			String seqNumStr = br.readLine();
			seqNum = Integer.parseInt(seqNumStr.substring(seqNumStr
					.indexOf("=") + 1));
			br.readLine();
			// Date maxDate = dp.parse(br.readLine());
			// System.out.println(maxDate);
			br.close();

			currTime = new Date().getTime() - 90000;

			drawMode = new int[0];
			drawTime = new long[0];
			drawID = new long[0];
			drawLat = new double[0];
			drawLon = new double[0];
			// System.exit(0);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}

		setupLatLonArray();

	}

	public void setupLatLonArray() {
		try {
			DecimalFormat myFormat = new DecimalFormat("000");
			String url = "http://planet.openstreetmap.org/minute-replicate/"
					+ myFormat.format(seqNum / 1000000) + "/"
					+ myFormat.format((seqNum / 1000) % 1000) + "/"
					+ myFormat.format(seqNum % 1000) + ".osc.gz";
			BufferedInputStream bis = new BufferedInputStream(
					new GZIPInputStream(new URL(url).openStream()));
			ChangesetParser cp = new ChangesetParser(bis);
			synchronized (this) {

				int[] tmpDrawMode = cp.getModes();
				long[] tmpDrawTime = cp.getTimes();
				long[] tmpDrawID = cp.getIDs();
				double[] tmpDrawLat = cp.getLats();
				double[] tmpDrawLon = cp.getLons();

				int[] tmpDrawMode2 = new int[drawMode.length
						+ tmpDrawMode.length];
				long[] tmpDrawTime2 = new long[drawMode.length
						+ tmpDrawMode.length];
				long[] tmpDrawID2 = new long[drawMode.length
						+ tmpDrawMode.length];
				double[] tmpDrawLat2 = new double[drawMode.length
						+ tmpDrawMode.length];
				double[] tmpDrawLon2 = new double[drawMode.length
						+ tmpDrawMode.length];

				System.arraycopy(drawMode, 0, tmpDrawMode2, 0, drawMode.length);
				System.arraycopy(tmpDrawMode, 0, tmpDrawMode2, drawMode.length,
						tmpDrawMode.length);
				System.arraycopy(drawTime, 0, tmpDrawTime2, 0, drawTime.length);
				System.arraycopy(tmpDrawTime, 0, tmpDrawTime2, drawTime.length,
						tmpDrawTime.length);
				System.arraycopy(drawID, 0, tmpDrawID2, 0, drawID.length);
				System.arraycopy(tmpDrawID, 0, tmpDrawID2, drawID.length,
						tmpDrawID.length);
				System.arraycopy(drawLat, 0, tmpDrawLat2, 0, drawLat.length);
				System.arraycopy(tmpDrawLat, 0, tmpDrawLat2, drawLat.length,
						tmpDrawLat.length);
				System.arraycopy(drawLon, 0, tmpDrawLon2, 0, drawLon.length);
				System.arraycopy(tmpDrawLon, 0, tmpDrawLon2, drawLon.length,
						tmpDrawLon.length);

				drawMode = tmpDrawMode2;
				drawTime = tmpDrawTime2;
				drawID = tmpDrawID2;
				drawLat = tmpDrawLat2;
				drawLon = tmpDrawLon2;

			}
			setupDrawArray();
			System.out.println("Fetched and processed " + url);
			System.out.println("Currently displaying " + new Date(currTime)
					+ " with " + drawLat.length + " nodes");
			seqNum++;
		} catch (IOException ioe) {
			if (ioe instanceof FileNotFoundException) {

			} else {
				ioe.printStackTrace();
			}
		}
	}

	public synchronized void setupDrawArray() {
		if (drawLat == null) {
			return;
		}
		drawX = new int[drawLat.length];
		drawY = new int[drawLat.length];
		for (int i = 0; i < drawLat.length; i++) {
			Point p = map.getMapPosition(drawLat[i], drawLon[i]);
			if (p != null) {
				drawX[i] = p.x;
				drawY[i] = p.y;
			} else {
				drawX[i] = -1;
				drawY[i] = -1;
			}
		}
	}

	public static void main(String[] args) {
		instance = new LiveEditMapViewer();

		instance.initChangeStream();
		instance.setVisible(true);

	}

	public void mapViewChanged() {
		setupDrawArray();
		repaint();

	}

	public void keyPressed(KeyEvent e) {
		System.out.println("Pressed " + e.getKeyChar());
	}

	public void keyReleased(KeyEvent e) {
		System.out.println("Released " + e.getKeyChar());

	}

	public void keyTyped(KeyEvent e) {
		System.out.println("Typed " + e.getKeyChar());

	}

	public void mouseClicked(MouseEvent me) {
		if ((me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {
			Point p = me.getPoint();
			for (int i = 0; i < drawX.length; i++) {
				if ((drawX[i] - 3 < p.x) && (drawX[i] + 3 > p.x)
						&& (drawY[i] - 3 < p.y) && (drawY[i] + 3 > p.y)) {
					if (desk != null) {
						try {
							desk.browse(new URI(
									"http://www.openstreetmap.org/browse/node/"
											+ drawID[i]));
							break;
						} catch (IOException ioe) {
							ioe.printStackTrace();
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void paintOverlay(Graphics g) {
		if (overlayI != null) {
			g.drawImage(overlayI, 0, 0, null);
		}
	}

}
