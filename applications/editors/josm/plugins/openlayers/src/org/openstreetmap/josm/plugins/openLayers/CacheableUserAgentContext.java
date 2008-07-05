package org.openstreetmap.josm.plugins.openLayers;

import org.lobobrowser.html.HttpRequest;
import org.lobobrowser.html.test.SimpleUserAgentContext;

/**
 * UserAgentContext that allows request caching
 */
public class CacheableUserAgentContext extends SimpleUserAgentContext {

    /** 
     * Returns a cache aware HttpRequest
     */
    @Override
    public HttpRequest createHttpRequest() {
	return new CacheableHttpRequest(this, this.getProxy());
    }
}
