package org.openstreetmap.josm.plugins.turnlanes.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

class JunctionPane extends JComponent {
	private final class MouseInputProcessor extends MouseAdapter {
		private int originX;
		private int originY;
		private int button;
		
		public void mousePressed(MouseEvent e) {
			setFocusable(true);
			button = e.getButton();
			
			if (button == MouseEvent.BUTTON1) {
				final Point2D mouse = translateMouseCoords(e);
				for (InteractiveElement ie : interactives()) {
					if (ie.contains(mouse, state)) {
						setState(ie.activate(state));
						repaint();
						break;
					}
				}
			}
			
			originX = e.getX();
			originY = e.getY();
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragging != null) {
				final Point2D mouse = translateMouseCoords(e);
				setState(dragging.drop(mouse.getX(), mouse.getY(), dropTarget(mouse), state));
			}
			
			dragging = null;
			repaint();
		}
		
		private InteractiveElement dropTarget(Point2D mouse) {
			for (InteractiveElement ie : interactives()) {
				if (ie.contains(mouse, state)) {
					return ie;
				}
			}
			
			return null;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (button == MouseEvent.BUTTON1) {
				final Point2D mouse = translateMouseCoords(e);
				for (InteractiveElement ie : interactives()) {
					if (ie.contains(mouse, state)) {
						setState(ie.click(state));
						break;
					}
				}
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			if (button == MouseEvent.BUTTON1) {
				final Point2D mouse = translateMouseCoords(e);
				
				if (dragging == null) {
					final Point2D origin = translateCoords(originX, originY);
					for (InteractiveElement ie : interactives()) {
						if (ie.contains(origin, state)) {
							if (ie.beginDrag(origin.getX(), origin.getY())) {
								dragging = ie;
							}
							
							break;
						}
					}
				}
				
				if (dragging != null) {
					setState(dragging.drag(mouse.getX(), mouse.getY(), dropTarget(mouse), state));
				}
			} else if (button == MouseEvent.BUTTON3) {
				translate(e.getX() - originX, e.getY() - originY);
				
				originX = e.getX();
				originY = e.getY();
			}
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			scale(e.getX(), e.getY(), Math.pow(0.8, e.getWheelRotation()));
		}
		
		private Point2D translateMouseCoords(MouseEvent e) {
			return translateCoords(e.getX(), e.getY());
		}
		
		private Point2D translateCoords(int x, int y) {
			return new Point2D.Double(-translationX + x / scale, -translationY + y / scale);
		}
	}
	
	private static final long serialVersionUID = 6917061040674799271L;
	
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	
	private final MouseInputProcessor mip = new MouseInputProcessor();
	
	private GuiContainer container;
	
	private int width = 0;
	private int height = 0;
	private double scale = 10;
	private double translationX = 0;
	private double translationY = 0;
	private boolean dirty = true;
	private BufferedImage passive;
	private BufferedImage interactive;
	
	private final NavigableMap<Integer, List<InteractiveElement>> interactives = new TreeMap<Integer, List<InteractiveElement>>();
	private State state;
	private InteractiveElement dragging;
	
	public JunctionPane(GuiContainer container) {
		setJunction(container);
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh");
		getActionMap().put("refresh", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setState(new State.Invalid(state));
			}
		});
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "zoomIn");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "zoomIn");
		getActionMap().put("zoomIn", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				scale(Math.pow(0.8, -1));
			}
		});
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "zoomOut");
		getActionMap().put("zoomOut", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				scale(Math.pow(0.8, 1));
			}
		});
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "center");
		getActionMap().put("center", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				center();
			}
		});
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "toggleAllTurns");
		getActionMap().put("toggleAllTurns", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleAllTurns();
			}
		});
	}
	
	public void setJunction(GuiContainer container) {
		removeMouseListener(mip);
		removeMouseMotionListener(mip);
		removeMouseWheelListener(mip);
		interactives.clear();
		dragging = null;
		this.container = container;
		
		if (container == null) {
			this.state = null;
		} else {
			setState(new State.Dirty(new State.Default()));
			
			center();
			
			addMouseListener(mip);
			addMouseMotionListener(mip);
			addMouseWheelListener(mip);
		}
	}
	
	private void center() {
		final Rectangle2D bounds = container.getBounds();
		scale = Math.min(getHeight() / 2 / bounds.getHeight(), getWidth() / 2 / bounds.getWidth());
		
		translationX = -bounds.getCenterX();
		translationY = -bounds.getCenterY();
		
		translate(getWidth() / 2d, getHeight() / 2d);
	}
	
	private void toggleAllTurns() {
		if (state instanceof State.AllTurns) {
			setState(((State.AllTurns) state).unwrap());
		} else {
			setState(new State.AllTurns(state));
		}
	}
	
	private void setState(State state) {
		if (state instanceof State.AllTurns) {
			dirty = true;
			this.state = state;
		} else if (state instanceof State.Invalid) {
			container = container.recalculate();
			dirty = true;
			this.state = new State.Default();
		} else if (state instanceof State.Dirty) {
			dirty = true;
			this.state = ((State.Dirty) state).unwrap();
		} else {
			this.state = state;
		}
		
		repaint();
	}
	
	void scale(int x, int y, double scale) {
		this.scale *= scale;
		
		final double w = getWidth();
		final double h = getHeight();
		
		translationX -= (w * (scale - 1)) / (2 * this.scale);
		translationY -= (h * (scale - 1)) / (2 * this.scale);
		
		dirty = true;
		repaint();
	}
	
	void scale(double scale) {
		scale(getWidth() / 2, getHeight() / 2, scale);
	}
	
	void translate(double x, double y) {
		translationX += x / scale;
		translationY += y / scale;
		
		dirty = true;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (getWidth() != width || getHeight() != height) {
			translate((getWidth() - width) / 2d, (getHeight() - height) / 2d);
			width = getWidth();
			height = getHeight();
			
			// translate already set dirty flag
			passive = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			interactive = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
		
		if (container == null) {
			super.paintComponent(g);
			return;
		}
		
		if (dirty) {
			paintPassive((Graphics2D) passive.getGraphics());
			dirty = false;
		}
		paintInteractive((Graphics2D) interactive.getGraphics());
		
		final Graphics2D g2d = (Graphics2D) g;
		
		g2d.drawImage(passive, 0, 0, getWidth(), getHeight(), null);
		g2d.drawImage(interactive, 0, 0, getWidth(), getHeight(), null);
	}
	
	private void paintInteractive(Graphics2D g2d) {
		g2d.setBackground(TRANSPARENT);
		g2d.clearRect(0, 0, getWidth(), getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		g2d.scale(scale, scale);
		
		g2d.translate(translationX, translationY);
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.7f));
		
		for (Map.Entry<Integer, List<InteractiveElement>> e : interactives.entrySet()) {
			for (InteractiveElement ie : e.getValue()) {
				ie.paintBackground(g2d, state);
			}
			for (InteractiveElement ie : e.getValue()) {
				ie.paint(g2d, state);
			}
		}
	}
	
	private List<InteractiveElement> interactives() {
		final List<InteractiveElement> result = new ArrayList<InteractiveElement>();
		
		for (List<InteractiveElement> ies : interactives.descendingMap().values()) {
			result.addAll(ies);
		}
		
		return result;
	}
	
	private void paintPassive(Graphics2D g2d) {
		interactives.clear();
		
		g2d.setBackground(new Color(100, 160, 240));
		g2d.clearRect(0, 0, getWidth(), getHeight());
		
		g2d.scale(scale, scale);
		g2d.translate(translationX, translationY);
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(Color.GRAY);
		for (RoadGui r : container.getRoads()) {
			addAllInteractives(r.paint(g2d));
		}
		
		for (JunctionGui j : container.getJunctions()) {
			addAllInteractives(j.paint(g2d));
			dot(g2d, new Point2D.Double(j.x, j.y), container.getLaneWidth() / 5);
		}
	}
	
	private void addAllInteractives(List<InteractiveElement> ies) {
		for (InteractiveElement ie : ies) {
			final List<InteractiveElement> existing = interactives.get(ie.getZIndex());
			
			final List<InteractiveElement> list;
			if (existing == null) {
				list = new ArrayList<InteractiveElement>();
				interactives.put(ie.getZIndex(), list);
			} else {
				list = existing;
			}
			
			list.add(ie);
		}
	}
	
	static void dot(Graphics2D g2d, Point2D p, double r, Color c) {
		final Color old = g2d.getColor();
		
		g2d.setColor(c);
		g2d.fill(new Ellipse2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r));
		
		g2d.setColor(old);
	}
	
	static void dot(Graphics2D g2d, Point2D p, double r) {
		dot(g2d, p, r, Color.RED);
	}
}
