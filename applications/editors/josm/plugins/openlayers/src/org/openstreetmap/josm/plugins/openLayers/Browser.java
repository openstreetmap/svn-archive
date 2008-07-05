package org.openstreetmap.josm.plugins.openLayers;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.HtmlBlockPanel;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.js.Window;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.mozilla.javascript.EcmaError;

public class Browser extends HtmlPanel {

    protected static final Logger logger = Logger.getLogger(Browser.class.getName());

    static
    {
	Logger.getLogger("org.lobobrowser").setLevel(Level.WARNING);
    }

    private final SimpleHtmlRendererContext rcontext;

    Dimension oldSize = null;

    public Browser(String uri) {
	super();

	UserAgentContext ucontext = new CacheableUserAgentContext();
	rcontext = new SimpleHtmlRendererContext(this, ucontext);
	addNotify();

	process( uri );
    }

    private void process(String uri) {
	try {
	    URL url;
	    try {
		url = new URL(uri);
	    } catch (java.net.MalformedURLException mfu) {
		int idx = uri.indexOf(':');
		if (idx == -1 || idx == 1) {
		    // try file
		    url = new URL("file:" + uri);
		} else {
		    throw mfu;
		}
	    }
	    // Call SimpleHtmlRendererContext.navigate()
	    // which implements incremental rendering.
	    this.rcontext.navigate(url, null);
	} catch (Exception err) {
	    err.printStackTrace();
	}
    }
    
    @Override
    public void setSize(final Dimension newSize)
    {
	if (!newSize.equals(oldSize)) {
	    oldSize = newSize;
	    super.setSize(newSize);
	    validate();

	    executeAsyncScript("resizeMap(" + newSize.width + "," + newSize.height + ");");
	}
    }

    public void executeAsyncScript(final String script)
    {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		executeScript(script);
	    }
	});
    }
    
    public Object executeScript(String script)
    {
	System.out.println("Executing script " + script);
	try {
	    Window window = Window.getWindow(rcontext);
	    if( window.getDocumentNode() == null )
		return null; // Document not loaded yet

	    return window.eval(script);
	} catch (EcmaError ecmaError) {
	    logger.log(Level.WARNING, "Javascript error at " + ecmaError.sourceName() + ":" + ecmaError.lineNumber() + ": " + ecmaError.getMessage());
	} catch (Throwable err) {
	    logger.log(Level.WARNING, "Unable to evaluate Javascript code", err);
	}
	
	return null;
    }
    
    
    /**
     * Overrided to hide hardcoded scrollbars and insets
     */
    @Override
    protected HtmlBlockPanel createHtmlBlockPanel(UserAgentContext ucontext, HtmlRendererContext rcontext) {
	return new MyHtmlBlockPanel(java.awt.Color.WHITE, true, ucontext, rcontext, this);
    }
}
