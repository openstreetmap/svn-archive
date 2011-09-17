package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListTransferHandler;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdTransferable;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * RelationMemberTable is the table for editing the raw member list of
 * a turn restriction.
 *
 */
public class RelationMemberTable extends JTable {
    private TurnRestrictionEditorModel model;
    private DeleteAction actDelete;
    private PasteAction actPaste;
    private MoveUpAction actMoveUp;
    private MoveDownAction actMoveDown;
    private TransferHandler transferHandler;

    public RelationMemberTable(TurnRestrictionEditorModel model) {
        super(
                model.getRelationMemberEditorModel(),
                new RelationMemberColumnModel(model.getRelationMemberEditorModel().getColSelectionModel()),
                model.getRelationMemberEditorModel().getRowSelectionModel()
        );
        this.model = model;
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setFillsViewportHeight(true); // make sure we can drag onto an empty table

        // register the popup menu launcher
        addMouseListener(new TablePopupLauncher());

        // transfer handling
        setDragEnabled(true);
        setTransferHandler(new RelationMemberTransferHandler());
        setDropTarget(new RelationMemberTableDropTarget());

        // initialize the delete action
        //
        actDelete = new DeleteAction();
        model.getRelationMemberEditorModel().getRowSelectionModel().addListSelectionListener(actDelete);
        registerKeyboardAction(actDelete, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // initialize the paste action (will be used in the popup, the action map already includes
        // the standard paste action for transfer handling)
        actPaste = new PasteAction();

        actMoveUp = new MoveUpAction();
        model.getRelationMemberEditorModel().getRowSelectionModel().addListSelectionListener(actMoveUp);
        registerKeyboardAction(actMoveUp,actMoveUp.getKeyStroke(), WHEN_FOCUSED);

        actMoveDown = new MoveDownAction();
        model.getRelationMemberEditorModel().getRowSelectionModel().addListSelectionListener(actMoveDown);
        registerKeyboardAction(actMoveDown, actMoveDown.getKeyStroke(), WHEN_FOCUSED);
    }

    /**
     * The action for deleting the selected table cells
     *
     */
    class DeleteAction extends AbstractAction implements ListSelectionListener{
        public DeleteAction() {
            putValue(NAME, tr("Delete"));
            putValue(SHORT_DESCRIPTION, tr("Clear the selected roles or delete the selected members"));
            putValue(SMALL_ICON, ImageProvider.get("deletesmall"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
            updateEnabledState();
        }

        public void updateEnabledState() {
            setEnabled(model.getRelationMemberEditorModel().getRowSelectionModel().getMinSelectionIndex()>=0);
        }

        public void actionPerformed(ActionEvent e) {
            model.getRelationMemberEditorModel().deleteSelected();
        }

        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
    }

    /**
     * The action for pasting into the relation member table
     *
     */
    class PasteAction extends AbstractAction{
        public PasteAction() {
            putValue(NAME, tr("Paste"));
            putValue(SHORT_DESCRIPTION, tr("Insert new relation members from object in the clipboard"));
            putValue(SMALL_ICON, ImageProvider.get("paste"));
            putValue(ACCELERATOR_KEY, Shortcut.getPasteKeyStroke());
            updateEnabledState();
        }

        public void updateEnabledState() {
            DataFlavor[] flavors = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
            setEnabled(PrimitiveIdListTransferHandler.isSupportedFlavor(flavors));
        }

        public void actionPerformed(ActionEvent evt) {
            // tried to delegate to 'paste' action in the action map of the
            // table, but didn't work. Now duplicating the logic of importData(...) in
            // the transfer handler.
            //
            Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (!PrimitiveIdListTransferHandler.isSupportedFlavor(cp.getAvailableDataFlavors())) return;
            try {
                List<PrimitiveId> ids;
                ids = (List<PrimitiveId>)cp.getData(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR);
                try {
                    model.getRelationMemberEditorModel().insertMembers(ids);
                } catch(IllegalArgumentException e){
                    e.printStackTrace();
                    // FIXME: provide user feedback
                }
            } catch(IOException e){
                e.printStackTrace();
            } catch(UnsupportedFlavorException e){
                e.printStackTrace();
            }
        }
    }

    class MoveDownAction extends AbstractAction implements ListSelectionListener{
        private KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK);
        public MoveDownAction(){
            putValue(NAME, tr("Move down"));
            putValue(SHORT_DESCRIPTION, tr("Move the selected relation members down by one position"));
            putValue(ACCELERATOR_KEY,keyStroke);
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "movedown"));
            updateEnabledState();
        }

        public void actionPerformed(ActionEvent e) {
            model.getRelationMemberEditorModel().moveDownSelected();
        }

        public void updateEnabledState(){
            setEnabled(model.getRelationMemberEditorModel().canMoveDown());
        }

        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
        public KeyStroke getKeyStroke() {
            return keyStroke;
        }
    }

    class MoveUpAction extends AbstractAction implements ListSelectionListener{
        private KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK);

        public MoveUpAction() {
            putValue(NAME, tr("Move up"));
            putValue(SHORT_DESCRIPTION, tr("Move the selected relation members up by one position"));
            putValue(ACCELERATOR_KEY,keyStroke);
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "moveup"));
            updateEnabledState();
        }

        public void actionPerformed(ActionEvent e) {
            model.getRelationMemberEditorModel().moveUpSelected();
        }

        public void updateEnabledState(){
            setEnabled(model.getRelationMemberEditorModel().canMoveUp());
        }

        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
        public KeyStroke getKeyStroke() {
            return keyStroke;
        }
    }

    class TablePopupLauncher extends PopupMenuLauncher {
        @Override
        public void launch(MouseEvent evt) {
            int row = rowAtPoint(evt.getPoint());
            if (getSelectionModel().getMinSelectionIndex() < 0 && row >=0){
                getSelectionModel().setSelectionInterval(row, row);
                getColumnModel().getSelectionModel().setSelectionInterval(0, 1);
            }
            new PopupMenu().show(RelationMemberTable.this, evt.getX(), evt.getY());
        }
    }

    class PopupMenu extends JPopupMenu {
        public PopupMenu() {
            JMenuItem item = add(actPaste);
            item.setTransferHandler(transferHandler);
            actPaste.updateEnabledState();
            addSeparator();
            add(actDelete);
            addSeparator();
            add(actMoveUp);
            add(actMoveDown);
        }
    }

    /**
     * The transfer handler for the relation member table.
     *
     */
    class RelationMemberTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return PrimitiveIdListTransferHandler.isSupportedFlavor(transferFlavors);
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                List<PrimitiveId> ids;
                ids = (List<PrimitiveId>)t.getTransferData(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR);
                try {
                    model.getRelationMemberEditorModel().insertMembers(ids);
                } catch(IllegalArgumentException e){
                    e.printStackTrace();
                    // FIXME: provide user feedback
                    return false;
                }
                return true;
            } catch(IOException e){
                e.printStackTrace();
            } catch(UnsupportedFlavorException e){
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return  COPY_OR_MOVE;
        }
    }

    /**
     * A custom drop target for the relation member table. During dragging we need to
     * disable colum selection model.
     *
     */
    class RelationMemberTableDropTarget extends DropTarget{
        private boolean dropAccepted = false;

        /**
         * Replies true if {@code transferFlavors} includes the data flavor {@link PrimitiveIdTransferable#PRIMITIVE_ID_LIST_FLAVOR}.

         * @param transferFlavors an array of transferFlavors
         * @return
         */
        protected boolean isSupportedFlavor(DataFlavor[] transferFlavors) {
            for (DataFlavor df: transferFlavors) {
                if (df.equals(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR)) return true;
            }
            return false;
        }

        public synchronized void dragEnter(DropTargetDragEvent dtde) {
            if (isSupportedFlavor(dtde.getCurrentDataFlavors())) {
                if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0){
                    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                    setColumnSelectionAllowed(false);
                    dropAccepted  = true;
                } else {
                    dtde.rejectDrag();
                }
            } else {
                dtde.rejectDrag();
            }
        }

        public synchronized void dragExit(DropTargetEvent dte) {
            setColumnSelectionAllowed(true);
            dropAccepted = false;
        }

        @Override
        public synchronized void dragOver(DropTargetDragEvent dtde) {
            int row = rowAtPoint(dtde.getLocation());
            int selectedRow = getSelectionModel().getMinSelectionIndex();
            if (row >= 0 && row != selectedRow){
                getSelectionModel().setSelectionInterval(row, row);
            }
        }

        public synchronized void drop(DropTargetDropEvent dtde) {
            try {
                if (!dropAccepted) return;
                if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
                    return;
                }
                List<PrimitiveId> ids;
                ids = (List<PrimitiveId>)dtde.getTransferable().getTransferData(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR);
                try {
                    model.getRelationMemberEditorModel().insertMembers(ids);
                } catch(IllegalArgumentException e){
                    e.printStackTrace();
                    // FIXME: provide user feedback
                }
            } catch(IOException e){
                e.printStackTrace();
            } catch(UnsupportedFlavorException e){
                e.printStackTrace();
            } finally {
                setColumnSelectionAllowed(true);
            }
        }

        public synchronized void dropActionChanged(DropTargetDragEvent dtde) {
            if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
                dtde.rejectDrag();
            } else {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }
        }
    }
}
