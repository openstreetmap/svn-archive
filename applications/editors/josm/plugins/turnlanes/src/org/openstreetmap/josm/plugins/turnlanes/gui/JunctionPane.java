package org.openstreetmap.josm.plugins.turnlanes.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

import javax.swing.JComponent;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.turnlanes.model.ModelContainer;

class JunctionPane extends JComponent {
	private final class MouseInputProcessor extends MouseAdapter {
		private int originX;
		private int originY;
		private int button;
		
		public void mousePressed(MouseEvent e) {
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
						repaint();
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
				
				repaint();
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
	
	private JunctionGui junction;
	
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
	
	public JunctionPane(JunctionGui junction) {
		setJunction(junction);
	}
	
	public void setJunction(JunctionGui junction) {
		removeMouseListener(mip);
		removeMouseMotionListener(mip);
		removeMouseWheelListener(mip);
		interactives.clear();
		dragging = null;
		this.junction = junction;
		
		dirty = true;
		repaint();
		
		if (junction == null) {
			this.state = null;
		} else {
			this.state = new State.Default(junction);
			
			final Rectangle2D bounds = junction.getBounds();
			scale = Math.min(getHeight() / 2 / bounds.getHeight(), getWidth() / 2 / bounds.getWidth());
			
			translationX = -bounds.getCenterX();
			translationY = -bounds.getCenterY();
			
			translate(getWidth() / 2d, getHeight() / 2d);
			
			addMouseListener(mip);
			addMouseMotionListener(mip);
			addMouseWheelListener(mip);
		}
	}
	
	private void setState(State state) {
		if (state instanceof State.Invalid) {
			final Node n = junction.getModel().getNode();
			final ModelContainer m = ModelContainer.create(n);
			
			final GuiContainer c = junction.getContainer().empty();
			junction = c.getGui(m.getJunction(n));
			
			dirty = true;
			this.state = new State.Default(junction);
		} else if (state instanceof State.Dirty) {
			dirty = true;
			this.state = ((State.Dirty) state).unwrap();
		} else {
			this.state = state;
		}
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
		
		if (junction == null) {
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
		for (RoadGui r : junction.getRoads()) {
			addAllInteractives(r.paint(g2d));
		}
		
		addAllInteractives(junction.paint(g2d));
		
		dot(g2d, new Point2D.Double(junction.x, junction.y), junction.getContainer().getLaneWidth() / 5);
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
