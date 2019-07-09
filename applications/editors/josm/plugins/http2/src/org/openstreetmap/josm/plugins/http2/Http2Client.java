// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.http2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.tools.JosmRuntimeException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

/**
 * HTTP/2 client based on Java 11 HTTP client.
 */
public final class Http2Client extends org.openstreetmap.josm.tools.HttpClient {

    private HttpClient.Builder clientBuilder;
    private HttpRequest.Builder requestBuilder;
    private HttpResponse<InputStream> response;

    Http2Client(URL url, String requestMethod) {
        super(url, requestMethod);
    }

    @Override
    protected void setupConnection(ProgressMonitor progressMonitor) throws IOException {
        clientBuilder = HttpClient.newBuilder().followRedirects(Redirect.NEVER); // we do that ourselves
        int timeout = getConnectTimeout();
        if (timeout > 0) {
            clientBuilder.connectTimeout(Duration.ofMillis(timeout));
        }
        try {
            requestBuilder = HttpRequest.newBuilder()
                      .uri(getURL().toURI())
                      .method(getRequestMethod(), hasRequestBody()
                              ? BodyPublishers.ofByteArray(getRequestBody())
                              : BodyPublishers.noBody())
                      .header("User-Agent", Version.getInstance().getFullAgentString());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        timeout = getReadTimeout();
        if (timeout > 0) {
            requestBuilder.timeout(Duration.ofMillis(timeout));
        }
        if (getIfModifiedSince() > 0) {
            requestBuilder.header("If-Modified-Since", DateTimeFormatter.RFC_1123_DATE_TIME.format(
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(getIfModifiedSince()), ZoneId.systemDefault())));
        }
        if (!isUseCache()) {
            requestBuilder.header("Cache-Control", "no-cache");
        }
        for (Map.Entry<String, String> header : getHeaders().entrySet()) {
            if (header.getValue() != null) {
                try {
                    requestBuilder.header(header.getKey(), header.getValue());
                } catch (IllegalArgumentException e) {
                    Logging.warn(e.getMessage());
                }
            }
        }

        notifyConnect(progressMonitor);
        
        if (requiresBody()) {
            logRequestBody();
        }
    }

    @Override
    protected ConnectionResponse performConnection() throws IOException {
        try {
            response = clientBuilder.build().send(requestBuilder.build(), BodyHandlers.ofInputStream());
            return new ConnectionResponse() {
                @Override
                public String getResponseVersion() {
                    return response.version().name();
                }
                
                @Override
                public int getResponseCode() throws IOException {
                    return response.statusCode();
                }

                @Override
                public long getContentLengthLong() {
                    return response.headers().firstValueAsLong("Content-Length").orElse(-1L);
                }

                @Override
                public Map<String, List<String>> getHeaderFields() {
                    return response.headers().map();
                }

                @Override
                public String getHeaderField(String name) {
                    final String list = String.join(",", response.headers().allValues(name));
                    return list.isEmpty() ? null : list;
                }
            };
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected Response buildResponse(ProgressMonitor progressMonitor) throws IOException {
        return new Http2Response(response, progressMonitor);
    }

    /**
     * A wrapper for the HTTP 2.x response.
     */
    public static final class Http2Response extends Response {
        private final HttpResponse<InputStream> response;

        public Http2Response(HttpResponse<InputStream> response, ProgressMonitor progressMonitor) throws IOException {
            super(progressMonitor, response.statusCode(), null);
            this.response = Objects.requireNonNull(response);
            debugRedirect();
        }

        @Override
        public URL getURL() {
            try {
                return response.uri().toURL();
            } catch (MalformedURLException e) {
                throw new JosmRuntimeException(e);
            }
        }

        @Override
        public String getRequestMethod() {
            return response.request().method();
        }

        @Override
        protected InputStream getInputStream() throws IOException {
            return response.body();
        }

        @Override
        public String getContentEncoding() {
            return response.headers().firstValue("Content-Encoding").orElse(null);
        }

        @Override
        public String getContentType() {
            return response.headers().firstValue("Content-Type").orElse(null);
        }

        @Override
        public long getExpiration() {
            return response.headers().firstValue("Expires")
                    .map(DateTimeFormatter.RFC_1123_DATE_TIME::parse)
                    .map(t -> 1000L * t.getLong(ChronoField.INSTANT_SECONDS))
                    .orElse(0L);
        }

        @Override
        public long getLastModified() {
            return response.headers().firstValue("Last-Modified")
                    .map(DateTimeFormatter.RFC_1123_DATE_TIME::parse)
                    .map(t -> 1000L * t.getLong(ChronoField.INSTANT_SECONDS))
                    .orElse(0L);
        }

        @Override
        public long getContentLength() {
            return response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        }

        @Override
        public String getHeaderField(String name) {
            final String list = String.join(",", response.headers().allValues(name));
            return list.isEmpty() ? null : list;
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return response.headers().map();
        }

        @Override
        public void disconnect() {
            Utils.close(response.body());
        }
    }

    @Override
    protected void performDisconnection() throws IOException {
        // Do nothing
    }

    @Override
    public void disconnect() {
        // Do nothing
    }
}
