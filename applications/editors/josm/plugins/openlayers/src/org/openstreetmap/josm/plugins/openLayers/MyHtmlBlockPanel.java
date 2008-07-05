package org.openstreetmap.josm.plugins.openLayers;

import java.awt.Color;
import java.awt.Insets;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.NodeImpl;
import org.lobobrowser.html.gui.HtmlBlockPanel;
import org.lobobrowser.html.renderer.FrameContext;
import org.lobobrowser.html.renderer.RBlock;

/**
 * Overrided class to hide hardcoded scrollbars and insets
 */
class MyHtmlBlockPanel extends HtmlBlockPanel {

    private static final long serialVersionUID = -4778865358510293592L;

    /**
     * Constructor
     * @param background
     * @param opaque
     * @param pcontext
     * @param rcontext
     * @param frameContext
     */
    public MyHtmlBlockPanel(Color background, boolean opaque, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext) {
        super(background, opaque, pcontext, rcontext, frameContext);
    }

    /**
     * Override to hide hardcoded scrollbars and insets
     */
    @Override
    public void setRootNode(NodeImpl node) {
        if (node != null) {
            RBlock block = new RBlock(node, 0, this.ucontext, this.rcontext, this.frameContext, this, RBlock.OVERFLOW_HIDDEN);
            block.setDefaultPaddingInsets( new Insets(0, 0, 0, 0) );
            node.setUINode(block);
            this.rblock = block;
        } else {
            this.rblock = null;
        }
        
        this.invalidate();
        this.validateAll();
        this.repaint();
    }
}