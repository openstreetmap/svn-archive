package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListProvider;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListTransferHandler;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdTransferable;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * <p>ViaList is a JList which displays the 'via' members of a turn restriction.</p>
 * 
 * <p>A ViaList is connected to a {@link TurnRestrictionEditorModel} through its
 * {@link ViaListModel}.</p> 
 * 
 */
public class ViaList extends JList{
    
    static private final Logger logger = Logger.getLogger(ViaList.class.getName());

    private ViaListModel model;
    private DeleteAction actDelete;
    private MoveUpAction actMoveUp;
    private MoveDownAction actMoveDown;
    private CopyAction actCopy;
    private PasteAction actPaste;
    private TransferHandler transferHandler;
    
    /**
     * Constructor 
     * 
     * @param model the via list model. Must not be null.
     * @param selectionModel the selection model. Must not be null.
     * 
     */
    public ViaList(ViaListModel model, DefaultListSelectionModel selectionModel) {
        super(model);
        this.model = model;
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setSelectionModel(selectionModel);
        setCellRenderer(new OsmPrimitivRenderer());
        setDragEnabled(true);       
        setTransferHandler(transferHandler =new ViaListTransferHandler(model));
        setVisibleRowCount(4);
        
        actDelete = new DeleteAction();
        selectionModel.addListSelectionListener(actDelete);
        registerKeyboardAction(actDelete, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        actMoveDown = new MoveDownAction();
        selectionModel.addListSelectionListener(actMoveDown);
        registerKeyboardAction(actMoveDown, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        actMoveUp = new MoveUpAction();
        selectionModel.addListSelectionListener(actMoveUp);
        registerKeyboardAction(actMoveUp, KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        actCopy = new CopyAction();
        actPaste = new PasteAction();
        getSelectionModel().addListSelectionListener(actCopy);
        
        addMouseListener(new ViaListPopupMenuLaucher());            
    }
   
	/**
     * The transfer handler for Drag-and-Drop. 
     */
    class ViaListTransferHandler extends PrimitiveIdListTransferHandler {
        Logger logger = Logger.getLogger(ViaListTransferHandler.class.getName());
        
        private boolean isViaListInDragOperation = false;
        private List<Integer> selectedRowsMemento = null;
        
        public ViaListTransferHandler(PrimitiveIdListProvider provider) {
            super(provider);
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            // a drag operation on itself is always allowed
            if (isViaListInDragOperation) return true;
            return isSupportedFlavor(transferFlavors);          
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(JComponent comp, Transferable t) {    
            if (!isSupportedFlavor(t.getTransferDataFlavors())) return false;
            if (isViaListInDragOperation) {
                // this is a drag operation on itself
                int targetRow = getSelectedIndex();
                if (targetRow <0) return true;
                model.moveVias(selectedRowsMemento, targetRow);             
            } else {
                // this is a drag operation from another component
                try {
                    List<PrimitiveId> idsToAdd = (List<PrimitiveId>)t.getTransferData(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR);
                    model.insertVias(idsToAdd);
                } catch(IOException e){
                    e.printStackTrace();
                } catch(UnsupportedFlavorException e){
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            isViaListInDragOperation = false;
            super.exportDone(source, data, action);
        }

        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            isViaListInDragOperation = true;
            selectedRowsMemento = model.getSelectedRows();
            super.exportAsDrag(comp, e, action);
        }       
    }   
    
    class DeleteAction extends AbstractAction implements ListSelectionListener {
        public DeleteAction() {
            putValue(NAME, tr("Remove"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
            putValue(SHORT_DESCRIPTION,tr("Remove the currently selected vias"));       
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));         
            updateEnabledState();
        }
        
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();           
        }
        
        public void updateEnabledState() {
            setEnabled(getSelectedIndex() >= 0);
        }

        public void actionPerformed(ActionEvent e) {
            model.removeSelectedVias();         
        }
    }
    
    class MoveDownAction extends AbstractAction implements ListSelectionListener{       
        public MoveDownAction(){
            putValue(NAME, tr("Move down"));
            putValue(SHORT_DESCRIPTION, tr("Move the selected vias down by one position"));
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "movedown"));
            updateEnabledState();
        }
        
        public void actionPerformed(ActionEvent e) {
            model.moveDown();
        }

        public void updateEnabledState(){
            if (getSelectedIndex() < 0) {
                setEnabled(false);
                return;
            }
            setEnabled(getSelectionModel().getMaxSelectionIndex() < getModel().getSize() -1);
        }
        
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();           
        }
    }
    
    class MoveUpAction extends AbstractAction implements ListSelectionListener{     
        public MoveUpAction() {
            putValue(NAME, tr("Move up"));
            putValue(SHORT_DESCRIPTION, tr("Move the selected vias up by one position"));
            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "moveup"));
            updateEnabledState();
        }
        
        public void actionPerformed(ActionEvent e) {
            model.moveUp();
        }

        public void updateEnabledState(){
            if (getSelectedIndex() < 0) {
                setEnabled(false);
                return;
            }
            setEnabled(getSelectionModel().getMinSelectionIndex() > 0);
        }
        
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();           
        }
    }

    class CopyAction extends AbstractAction implements ListSelectionListener {
        private Action delegate;
        
        public CopyAction(){
            putValue(NAME, tr("Copy"));
            putValue(SHORT_DESCRIPTION, tr("Copy the selected vias to the clipboard"));
            putValue(SMALL_ICON, ImageProvider.get("copy"));
            putValue(ACCELERATOR_KEY, Shortcut.getCopyKeyStroke());
            delegate = ViaList.this.getActionMap().get("copy");
        }

        public void actionPerformed(ActionEvent e) {            
            delegate.actionPerformed(e);
        }

        protected void updateEnabledState() {
            setEnabled(!model.getSelectedVias().isEmpty());
        }
        
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
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
            putValue(SHORT_DESCRIPTION, tr("Insert ''via'' objects from the clipboard"));
            putValue(SMALL_ICON, ImageProvider.get("paste"));
            putValue(ACCELERATOR_KEY, Shortcut.getPasteKeyStroke());
            delegate = ViaList.this.getActionMap().get("paste");
            updateEnabledState();
        }

        public void updateEnabledState() {
            setEnabled(canPaste());
        }
        
        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);            
        }
    }
    
    class ViaListPopupMenu extends JPopupMenu {
        public ViaListPopupMenu() {
            JMenuItem item = add(actCopy);
            item.setTransferHandler(transferHandler);           
            item = add(actPaste);
            actPaste.updateEnabledState();
            item.setTransferHandler(transferHandler);
            addSeparator();
            add(actDelete);
            addSeparator();
            add(actMoveUp);
            add(actMoveDown);
        }
    }
    
    class ViaListPopupMenuLaucher extends PopupMenuLauncher {
        @Override
        public void launch(MouseEvent evt) {
            if (getSelectedIndex() <0) {
                int idx = locationToIndex(evt.getPoint());
                if (idx >=0) {
                    setSelectedIndex(idx);
                }
            }
            new ViaListPopupMenu().show(ViaList.this, evt.getX(), evt.getY());
        }       
    }   
}
