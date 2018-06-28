// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapUtils;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

public final class StreetsideURL {
	/** Base URL of the Bing Bubble API. */
	private static final String STREETSIDE_BASE_URL = "https://dev.virtualearth.net/mapcontrol/HumanScaleServices/GetBubbles.ashx";
	//private static final String BASE_API_V2_URL = "https://a.mapillary.com/v2/";
	private static final String CLIENT_ID = "T1Fzd20xZjdtR0s1VDk5OFNIOXpYdzoxNDYyOGRkYzUyYTFiMzgz";
	private static final String BING_MAPS_KEY = "AuftgJsO0Xs8Ts4M1xZUQJQXJNsvmh3IV8DkNieCiy3tCwCUMq76-WpkrBtNAuEm";
	private static final String TEST_BUBBLE_ID = "80848005";

	private static final String STREETSIDE_PRIVACY_URL = "https://www.bing.com/maps/privacyreport/streetsideprivacyreport?bubbleid=";

	private static final int OSM_BBOX_NORTH = 3;
	private static final int OSM_BBOX_SOUTH = 1;
	private static final int OSM_BBOXEAST = 2;
	private static final int OSM_BBOX_WEST = 0;

	public static final class APIv3 {
		//private static final String BASE_URL = "https://a.mapillary.com/v3/";

		private APIv3() {
			// Private constructor to avoid instantiation
		}

		/*public static URL getUser(String key) {
			return StreetsideURL.string2URL(APIv3.BASE_URL, "users/", key, StreetsideURL.queryString(null));
		}

		*//**
		 * @return the URL where you can create, get and approve changesets
		 *//*
		public static URL submitChangeset() {
			return StreetsideURL.string2URL(APIv3.BASE_URL, "changesets", APIv3.queryString(null));
		}

		public static URL searchDetections(Bounds bounds) {
			return StreetsideURL.string2URL(APIv3.BASE_URL, "detections", APIv3.queryString(bounds));
		}

		public static URL searchImages(Bounds bounds) {
			return StreetsideURL.string2URL(APIv3.BASE_URL, "images", APIv3.queryStreetsideString(bounds));
		}*/

		public static URL searchStreetsideImages(Bounds bounds) {
			return StreetsideURL.string2URL(StreetsideURL.STREETSIDE_BASE_URL, APIv3.queryStreetsideString(bounds));
		}

		/*public static URL searchMapObjects(final Bounds bounds) {
			return StreetsideURL.string2URL(APIv3.BASE_URL, "objects", APIv3.queryString(bounds));
		}*/

		public static URL searchStreetsideSequences(final Bounds bounds) {
			return StreetsideURL.string2URL(StreetsideURL.STREETSIDE_BASE_URL, APIv3.queryStreetsideString(bounds));
		}

		/**
		 * The APIv3 returns a Link header for each request. It contains a URL for requesting more results.
		 * If you supply the value of the Link header, this method returns the next URL,
		 * if such a URL is defined in the header.
		 * @param value the value of the HTTP-header with key "Link"
		 * @return the {@link URL} for the next result page, or <code>null</code> if no such URL could be found
		 */
		public static URL parseNextFromLinkHeaderValue(String value) {
			if (value != null) {
				// Iterate over the different entries of the Link header
				for (final String link : value.split(",", Integer.MAX_VALUE)) {
					boolean isNext = false;
					URL url = null;
					// Iterate over the parts of each entry (typically it's one `rel="‹linkType›"` and one like `<https://URL>`)
					for (String linkPart : link.split(";", Integer.MAX_VALUE)) {
						linkPart = linkPart.trim();
						isNext |= linkPart.matches("rel\\s*=\\s*\"next\"");
						if (linkPart.length() >= 1 && linkPart.charAt(0) == '<' && linkPart.endsWith(">")) {
							try {
								url = new URL(linkPart.substring(1, linkPart.length() - 1));
							} catch (final MalformedURLException e) {
								Logging.log(Logging.LEVEL_WARN, "Mapillary API v3 returns a malformed URL in the Link header.", e);
							}
						}
					}
					// If both a URL and the rel=next attribute are present, return the URL. Otherwise null is returned
					if (url != null && isNext) {
						return url;
					}
				}
			}
			return null;
		}

		public static String queryString(final Bounds bounds) {
			if (bounds != null) {
				final Map<String, String> parts = new HashMap<>();
				parts.put("bbox", bounds.toBBox().toStringCSV(","));
				return StreetsideURL.queryString(parts);
			}
			return StreetsideURL.queryString(null);
		}

		public static String queryStreetsideString(final Bounds bounds) {
			if (bounds != null) {
				final Map<String, String> parts = new HashMap<>();
				parts.put("bbox", bounds.toBBox().toStringCSV(","));
				return StreetsideURL.queryStreetsideBoundsString(parts);
			}
			return StreetsideURL.queryStreetsideBoundsString(null);
		}

		/**
		 * @return the URL where you'll find information about the user account as JSON
		 */
		/*public static URL userURL() {
			return StreetsideURL.string2URL(APIv3.BASE_URL, "me", StreetsideURL.queryString(null));
		}*/
	}

	public static final class VirtualEarth {
		private static final String BASE_URL_PREFIX = "https://t.ssl.ak.tiles.virtualearth.net/tiles/hs";
		private static final String BASE_URL_SUFFIX = ".jpg?g=6338&n=z";

		private VirtualEarth() {
			// Private constructor to avoid instantiation
		}

		public static URL streetsideTile(String id, boolean thumbnail) {
			if(thumbnail) {
				id = id + "01";
			}
			URL url = StreetsideURL.string2URL(VirtualEarth.BASE_URL_PREFIX + id + VirtualEarth.BASE_URL_SUFFIX);
			Logging.info("Tile task URL {0} invoked.", url.toString());
			return url;
		}
	}

	public static final class MainWebsite {
		//private static final String BASE_URL = "https://www.mapillary.com/";

		private MainWebsite() {
			// Private constructor to avoid instantiation
		}

		/**
		 * Gives you the URL for the online viewer of a specific Streetside image.
		 * @param id the id of the image to which you want to link
		 * @return the URL of the online viewer for the image with the given image key
		 * @throws IllegalArgumentException if the image key is <code>null</code>
		 */
		public static URL browseImage(String id) {
			if (id == null) {
				throw new IllegalArgumentException("The image key must not be null!");
			}
			return StreetsideURL.string2URL(VirtualEarth.BASE_URL_PREFIX + id + VirtualEarth.BASE_URL_SUFFIX);
		}

		/**
		 * Gives you the URL for the blur editor of the image with the given key.
		 * @param key the key of the image for which you want to open the blur editor
		 * @return the URL of the blur editor
		 * @throws IllegalArgumentException if the image key is <code>null</code>
		 */
		/*public static URL blurEditImage(final String key) {
			if (key == null) {
				throw new IllegalArgumentException("The image key must not be null!");
			}
			String urlEncodedKey;
			try {
				urlEncodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
			} catch (final UnsupportedEncodingException e) {
				Logging.log(Logging.LEVEL_ERROR, "Unsupported encoding when URL encoding", e);
				urlEncodedKey = key;
			}
			return StreetsideURL.string2URL(MainWebsite.BASE_URL, "app/blur?focus=photo&pKey=", urlEncodedKey);
		}*/

		/**
		 * Gives you the URL for the blur editor of the image with the given key.
		 * @param id the key of the image for which you want to open the blur editor
		 * @return the URL of the blur editor
		 * @throws IllegalArgumentException if the image key is <code>null</code>
		 */
		public static URL streetsidePrivacyLink(final String id) {
			if (id == null) {
				throw new IllegalArgumentException("The image id must not be null!");
			}
			String urlEncodedId;
			try {
				urlEncodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.name());
			} catch (final UnsupportedEncodingException e) {
				Logging.log(Logging.LEVEL_ERROR, "Unsupported encoding when URL encoding", e);
				urlEncodedId = id;
			}
			return StreetsideURL.string2URL(StreetsideURL.STREETSIDE_PRIVACY_URL, urlEncodedId);
		}

		/**
		 * Gives you the URL which the user should visit to initiate the OAuth authentication process
		 * @param redirectURI the URI to which the user will be redirected when the authentication is finished.
		 *        When this is <code>null</code>, it's omitted from the query string.
		 * @return the URL that the user should visit to start the OAuth authentication
		 */
		/*public static URL connect(String redirectURI) {
			final HashMap<String, String> parts = new HashMap<>();
			if (redirectURI != null && redirectURI.length() >= 1) {
				parts.put("redirect_uri", redirectURI);
			}
			parts.put("response_type", "token");
			parts.put("scope", "user:read public:upload public:write");
			return StreetsideURL.string2URL(MainWebsite.BASE_URL, "connect", StreetsideURL.queryString(parts));
		}

		public static URL mapObjectIcon(String key) {
			return StreetsideURL.string2URL(MainWebsite.BASE_URL, "developer/api-documentation/images/traffic_sign/" + key + ".png");
		}*/
	}

	private StreetsideURL() {
		// Private constructor to avoid instantiation
	}

	public static URL[] string2URLs(String baseUrlPrefix, String cubemapImageId, String baseUrlSuffix) {
		List<URL> res = new ArrayList<>();

		switch (StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 16 : 4) {

		case 16:

			EnumSet.allOf(CubemapUtils.CubemapFaces.class).forEach(face -> {
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 4; j++) {
						try {
							final String urlStr = baseUrlPrefix + cubemapImageId
									+ CubemapUtils.rowCol2StreetsideCellAddressMap
											.get(String.valueOf(i) + String.valueOf(j))
									+ baseUrlSuffix;
							res.add(new URL(urlStr));
						} catch (final MalformedURLException e) {
							Logging.error(I18n.tr("Error creating URL String for cubemap {0}", cubemapImageId));
							e.printStackTrace();
						}

					}
				}
			});

		case 4:
			EnumSet.allOf(CubemapUtils.CubemapFaces.class).forEach(face -> {
				for (int i = 0; i < 4; i++) {

					try {
						final String urlStr = baseUrlPrefix + cubemapImageId
								+ CubemapUtils.rowCol2StreetsideCellAddressMap.get(String.valueOf(i)) + baseUrlSuffix;
						res.add(new URL(urlStr));
					} catch (final MalformedURLException e) {
						Logging.error(I18n.tr("Error creating URL String for cubemap {0}", cubemapImageId));
						e.printStackTrace();
					}

				}
			});
			break; // break is optional
		default:
			// Statements
		}
		return res.stream().toArray(URL[]::new);
	}

	/**
	 * @return the URL where you'll find the upload secrets as JSON
	 */
	/*public static URL uploadSecretsURL() {
		return StreetsideURL.string2URL(StreetsideURL.BASE_API_V2_URL, "me/uploads/secrets", StreetsideURL.queryString(null));
	}*/

	/**
	 * Builds a query string from it's parts that are supplied as a {@link Map}
	 * @param parts the parts of the query string
	 * @return the constructed query string (including a leading ?)
	 */
	static String queryString(Map<String, String> parts) {
		final StringBuilder ret = new StringBuilder("?client_id=").append(StreetsideURL.CLIENT_ID);
		if (parts != null) {
			for (final Entry<String, String> entry : parts.entrySet()) {
				try {
					ret.append('&')
					.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
					.append('=')
					.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
				} catch (final UnsupportedEncodingException e) {
					Logging.error(e); // This should not happen, as the encoding is hard-coded
				}
			}
		}

		Logging.info(I18n.tr("queryString result: {0}", ret.toString()));

		return ret.toString();
	}

	static String queryStreetsideBoundsString(Map<String, String> parts) {
		final StringBuilder ret = new StringBuilder("?n=");
		if (parts != null) {
			final List<String> bbox = new ArrayList<>(Arrays.asList(parts.get("bbox").split(",")));
			try {
				ret.append(URLEncoder.encode(bbox.get(StreetsideURL.OSM_BBOX_NORTH), StandardCharsets.UTF_8.name()))
				.append("&s=")
				.append(URLEncoder.encode(bbox.get(StreetsideURL.OSM_BBOX_SOUTH), StandardCharsets.UTF_8.name()))
				.append("&e=")
				.append(URLEncoder.encode(bbox.get(StreetsideURL.OSM_BBOXEAST), StandardCharsets.UTF_8.name()))
				.append("&w=")
				.append(URLEncoder.encode(bbox.get(StreetsideURL.OSM_BBOX_WEST), StandardCharsets.UTF_8.name()))
				.append("&c=1000")
				.append("&appkey=")
				.append(StreetsideURL.BING_MAPS_KEY);
			} catch (final UnsupportedEncodingException e) {
				Logging.error(e); // This should not happen, as the encoding is hard-coded
			}
		}
		Logging.info(I18n.tr("queryStreetsideBoundsString result: {0}", ret.toString()));

		return ret.toString();
	}

	static String queryByIdString(Map<String, String> parts) {
		final StringBuilder ret = new StringBuilder("?id=");
		try {
			ret.append(URLEncoder.encode(StreetsideURL.TEST_BUBBLE_ID, StandardCharsets.UTF_8.name()));
			ret.append('&').append(URLEncoder.encode("appkey=", StandardCharsets.UTF_8.name())).append('=')
			.append(URLEncoder.encode(StreetsideURL.BING_MAPS_KEY, StandardCharsets.UTF_8.name()));
		} catch (final UnsupportedEncodingException e) {
			Logging.error(e); // This should not happen, as the encoding is hard-coded
		}
		Logging.info(I18n.tr("queryById result: {0}", ret.toString()));
		return ret.toString();
	}

	/**
	 * Converts a {@link String} into a {@link URL} without throwing a {@link MalformedURLException}.
	 * Instead such an exception will lead to an {@link Logging#error(Throwable)}.
	 * So you should be very confident that your URL is well-formed when calling this method.
	 * @param strings the Strings describing the URL
	 * @return the URL that is constructed from the given string
	 */
	static URL string2URL(String... strings) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; strings != null && i < strings.length; i++) {
			builder.append(strings[i]);
		}
		try {
			return new URL(builder.toString());
		} catch (final MalformedURLException e) {
			Logging.log(Logging.LEVEL_ERROR, String.format(
					"The class '%s' produces malformed URLs like '%s'!",
					StreetsideURL.class.getName(),
					builder
					), e);
			return null;
		}
	}
}