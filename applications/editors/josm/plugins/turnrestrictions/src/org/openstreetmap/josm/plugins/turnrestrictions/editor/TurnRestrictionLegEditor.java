package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListProvider;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListTransferHandler;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdTransferable;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * This is an editor for one of the two legs of a turn restriction.
 */
public class TurnRestrictionLegEditor extends JPanel implements Observer, PrimitiveIdListProvider {
    //static private final Logger logger = Logger.getLogger(TurnRestrictionLegEditor.class.getName());
 
    private JLabel lblOsmObject;
    private final Set<OsmPrimitive> legs = new HashSet<OsmPrimitive>();
    private TurnRestrictionEditorModel model;
    private TurnRestrictionLegRole role; 
    private DeleteAction actDelete;
    private CopyAction actCopy;
    private PasteAction actPaste;
    private AcceptAction actAccept;
    private TransferHandler transferHandler;
    
    /**
     * builds the UI 
     */
    protected void build() {
        setLayout(new BorderLayout());
        add(lblOsmObject = new JLabel(), BorderLayout.CENTER);      
        lblOsmObject.setOpaque(true);
        lblOsmObject.setBorder(null);
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(),
                        BorderFactory.createEmptyBorder(1,1,1,1)
                )
        );
        
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        pnlButtons.setBorder(null);
        JButton btn;
        actDelete = new DeleteAction();
        pnlButtons.add(btn = new JButton(actDelete));
        btn.setFocusable(false);
        btn.setText(null);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        
        actAccept = new AcceptAction();
        pnlButtons.add(btn = new JButton(actAccept));
        btn.setFocusable(false);
        btn.setText(null);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        add(pnlButtons, BorderLayout.EAST);
                
        // focus handling
        FocusHandler fh  = new FocusHandler();
        lblOsmObject.setFocusable(true);    
        lblOsmObject.addFocusListener(fh);      
        this.addFocusListener(fh);

        // mouse event handling
        MouseEventHandler meh = new MouseEventHandler();
        lblOsmObject.addMouseListener(meh);
        addMouseListener(meh);
        lblOsmObject.addMouseListener(new PopupLauncher());
        
        // enable DEL to remove the object from the turn restriction
        registerKeyboardAction(actDelete,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0) , JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        getInputMap().put(Shortcut.getCopyKeyStroke(), TransferHandler.getCopyAction().getValue(Action.NAME));;
        getInputMap().put(Shortcut.getPasteKeyStroke(), TransferHandler.getPasteAction().getValue(Action.NAME));;
        getActionMap().put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        getActionMap().put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
        lblOsmObject.setTransferHandler(transferHandler = new LegEditorTransferHandler(this));
        lblOsmObject.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e) {
                JComponent c = (JComponent)e.getSource();
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, e, TransferHandler.COPY);                
            }                   
        });
        actCopy = new CopyAction();
        actPaste = new PasteAction();
    }
    
    /**
     * Constructor 
     * 
     * @param model the model. Must not be null.
     * @param role the leg role of the leg this editor is editing. Must not be null.
     * @exception IllegalArgumentException thrown if model is null
     * @exception IllegalArgumentException thrown if role is null
     */
    public TurnRestrictionLegEditor(TurnRestrictionEditorModel model, TurnRestrictionLegRole role) {
        CheckParameterUtil.ensureParameterNotNull(model, "model");
        CheckParameterUtil.ensureParameterNotNull(role, "role");
        
        this.model = model;
        this.role = role;
        build();
        model.addObserver(this);
        refresh();  
    }

    protected void refresh(){
        legs.clear();
        legs.addAll(model.getTurnRestrictionLeg(role));
        if (legs.isEmpty()) {
            lblOsmObject.setFont(UIManager.getFont("Label.font").deriveFont(Font.ITALIC));
            lblOsmObject.setIcon(null);
            lblOsmObject.setText(tr("please select a way"));
            lblOsmObject.setToolTipText(null);
        } else if (legs.size() == 1){
            OsmPrimitive leg = legs.iterator().next();
            lblOsmObject.setFont(UIManager.getFont("Label.font"));
            lblOsmObject.setIcon(ImageProvider.get("data", "way"));
            lblOsmObject.setText(leg.getDisplayName(DefaultNameFormatter.getInstance()));
            lblOsmObject.setToolTipText(DefaultNameFormatter.getInstance().buildDefaultToolTip(leg));
        } else {
            lblOsmObject.setFont(UIManager.getFont("Label.font").deriveFont(Font.ITALIC));
            lblOsmObject.setIcon(null);
            lblOsmObject.setText(tr("multiple objects with role ''{0}''",this.role.getOsmRole()));
            lblOsmObject.setToolTipText(null);          
        }
        renderColors();
        actDelete.updateEnabledState();
    }
    
    /**
     * Render the foreground and background color
     */
    protected void renderColors() {
        if (lblOsmObject.hasFocus()) {
            setBackground(UIManager.getColor("List.selectionBackground"));
            setForeground(UIManager.getColor("List.selectionForeground"));
            lblOsmObject.setBackground(UIManager.getColor("List.selectionBackground"));
            lblOsmObject.setForeground(UIManager.getColor("List.selectionForeground"));
        } else {
            lblOsmObject.setBackground(UIManager.getColor("List.background"));
            lblOsmObject.setForeground(UIManager.getColor("List.foreground"));
        }
    }
    
    /**
     * Replies the model for this editor
     * 
     * @return the model 
     */
    public TurnRestrictionEditorModel getModel() {
        return model;
    }
    
    /**
     * Replies the role of this editor 
     * 
     * @return the role 
     */
    public TurnRestrictionLegRole getRole() {
        return role;
    }       
    
    /* ----------------------------------------------------------------------------- */
    /* interface Observer                                                            */
    /* ----------------------------------------------------------------------------- */
    public void update(Observable o, Object arg) {
        refresh();      
    }
    
    /* ----------------------------------------------------------------------------- */
    /* interface PrimitiveIdListProvider                                                            */
    /* ----------------------------------------------------------------------------- */
    public List<PrimitiveId> getSelectedPrimitiveIds() {
        if (legs.size() == 1) {
            return Collections.singletonList(legs.iterator().next().getPrimitiveId());
        }
        return Collections.emptyList();
    }
    
    /* ----------------------------------------------------------------------------- */
    /* inner classes                                                                 */
    /* ----------------------------------------------------------------------------- */ 
    /**
     * Responds to focus change events  
     */
    class FocusHandler extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            renderColors();
        }

        @Override
        public void focusLost(FocusEvent e) {
            renderColors();
        }       
    }
    
    class MouseEventHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            lblOsmObject.requestFocusInWindow();
        }       
    }
    
    /**
     * Deletes the way from the turn restriction 
     */
    class DeleteAction extends AbstractAction {
        public DeleteAction() {
            putValue(SHORT_DESCRIPTION, tr("Delete from turn restriction"));
            putValue(NAME, tr("Delete"));
            putValue(SMALL_ICON, ImageProvider.get("deletesmall"));
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
            updateEnabledState();
        }
        
        public void actionPerformed(ActionEvent e) {
            model.setTurnRestrictionLeg(role, null);            
        }       
        
        public void updateEnabledState() {
            setEnabled(legs.size()>0);
        }
    }
    
    /**
     * Accepts the currently selected way as turn restriction leg. Only enabled,
     * if there is exactly one way selected 
     */
    class AcceptAction extends AbstractAction implements ListSelectionListener {
    	
    	public AcceptAction() {
			 putValue(SHORT_DESCRIPTION, tr("Accept the currently selected way"));
	         putValue(NAME, tr("Accept"));
	         putValue(SMALL_ICON, ImageProvider.get("accept"));
	         model.getJosmSelectionListModel().getListSelectionModel().addListSelectionListener(this);
	         updateEnabledState();	         
    	}
    	
    	 public void actionPerformed(ActionEvent e) {
    		 List<Way> selWays = OsmPrimitive.getFilteredList(model.getJosmSelectionListModel().getSelected(), Way.class);
    		 if (selWays.size() != 1) return;
    		 Way w = selWays.get(0);    		 
             model.setTurnRestrictionLeg(role, w);            
         }       
         
         public void updateEnabledState() {
        	setEnabled(OsmPrimitive.getFilteredList(model.getJosmSelectionListModel().getSelected(), Way.class).size() == 1);
         }

		@Override
		public void valueChanged(ListSelectionEvent e) {
			updateEnabledState();
		}
    }
    
    /**
     * The transfer handler for Drag-and-Drop. 
     */
    class LegEditorTransferHandler extends PrimitiveIdListTransferHandler {
        Logger logger = Logger.getLogger(LegEditorTransferHandler.class.getName());
        
        public LegEditorTransferHandler(PrimitiveIdListProvider provider){
            super(provider);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                List<PrimitiveId> ids = (List<PrimitiveId>)t.getTransferData(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR);
                if (ids.size() !=1) {
                    return false;
                }
                PrimitiveId id = ids.get(0);
                if (!id.getType().equals(OsmPrimitiveType.WAY)) return false;
                model.setTurnRestrictionLeg(role, id);
                return true;
            } catch(IOException e) {
                // ignore
                return false;
            } catch(UnsupportedFlavorException e) {
                // ignore
                return false;
            }
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (legs.size() != 1) return null;
            return super.createTransferable(c);
        }
    }
    
    class PopupLauncher extends PopupMenuLauncher {
        @Override
        public void launch(MouseEvent evt) {
            new PopupMenu().show(lblOsmObject, evt.getX(), evt.getY());
        }       
    }
    
    class PopupMenu extends JPopupMenu {
        public PopupMenu() {
            actCopy.updateEnabledState();
            JMenuItem item = add(actCopy);
            item.setTransferHandler(transferHandler);
            actPaste.updateEnabledState();
            item = add(actPaste);           
            item.setTransferHandler(transferHandler);
            addSeparator();
            add(actDelete);
        }
    }
    
    class CopyAction extends AbstractAction {
        private Action delegate;
        
        public CopyAction(){
            putValue(NAME, tr("Copy"));
            putValue(SHORT_DESCRIPTION, tr("Copy to the clipboard"));
            putValue(SMALL_ICON, ImageProvider.get("copy"));
            putValue(ACCELERATOR_KEY, Shortcut.getCopyKeyStroke());
            delegate = TurnRestrictionLegEditor.this.getActionMap().get("copy");
            updateEnabledState();
        }

        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }
        
        public void updateEnabledState() {
            setEnabled(legs.size() == 1);
        }
    }
    
    class PasteAction extends AbstractAction {
        private Action delegate;
        
        public boolean canPaste() {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            for (DataFlavor df: clipboard.getAvailableDataFlavors()) {
                if (df.equals(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR)) return true;
            }           
            // FIXME: check whether there are selected objects in the JOSM copy/paste buffer  
            return false;
        }
        
        public PasteAction(){
            putValue(NAME, tr("Paste"));
            putValue(SHORT_DESCRIPTION, tr("Paste from the clipboard"));
            putValue(SMALL_ICON, ImageProvider.get("paste"));
            putValue(ACCELERATOR_KEY, Shortcut.getPasteKeyStroke());
            delegate = TurnRestrictionLegEditor.this.getActionMap().get("paste");
        }
        
        public void updateEnabledState() {
            setEnabled(canPaste());
        }

        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);            
        }
    }
    
     
}
