package org.openstreetmap.josm.plugins.turnrestrictions.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * <p>PrimitiveIdListTransferHandler is a transfer handler for components which 
 * provide and/or accept a list of {@link PrimitiveId} via copy/paste or
 * drag-and-drop.</p>
 * 
 * <p>It creates a {@link Transferable} by retrieving the list of primitive IDs
 * from a {@link PrimitiveIdListProvider}.</p>
 * 
 */
public class PrimitiveIdListTransferHandler extends TransferHandler {
    static private final Logger logger = Logger.getLogger(PrimitiveIdListTransferHandler.class.getName());
    private PrimitiveIdListProvider provider;
    
    /**
     * Replies true if {@code transferFlavors} includes the data flavor {@link PrimitiveIdTransferable#PRIMITIVE_ID_LIST_FLAVOR}.

     * @param transferFlavors an array of transferFlavors
     * @return true if {@code transferFlavors} includes the data flavor {@link PrimitiveIdTransferable#PRIMITIVE_ID_LIST_FLAVOR}.
     */
    public static boolean isSupportedFlavor(DataFlavor[] transferFlavors) {
        for (DataFlavor df: transferFlavors) {
            if (df.equals(PrimitiveIdTransferable.PRIMITIVE_ID_LIST_FLAVOR)) return true;
        }
        return false;
    }
    
    /**
     * Creates the transfer handler 
     * 
     * @param provider the provider of the primitive IDs. Must not be null.
     * @throws IllegalArgumentException thrown if provider is null.
     */
    public PrimitiveIdListTransferHandler(PrimitiveIdListProvider provider) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(provider, "provider");
        this.provider = provider;
    }
    
    protected Transferable createTransferable(JComponent c) {
        return new PrimitiveIdTransferable(provider.getSelectedPrimitiveIds());         
    }

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return isSupportedFlavor(transferFlavors);  
    }   
}
