// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.AlphanumComparator;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class WikipediaApp {

    public static Pattern WIKIDATA_PATTERN = Pattern.compile("Q\\d+");
    private static final DocumentBuilder DOCUMENT_BUILDER = newDocumentBuilder();
    private static final XPath X_PATH = XPathFactory.newInstance().newXPath();

    private WikipediaApp() {
    }

    static String getMediawikiLocale(Locale locale) {
        if (!locale.getCountry().isEmpty()) {
            return locale.getLanguage() + "-" + locale.getCountry().toLowerCase();
        } else {
            return locale.getLanguage();
        }
    }

    static String getSiteUrl(String wikipediaLang) {
        if ("wikidata".equals(wikipediaLang)) {
            return "https://www.wikidata.org";
        } else {
            return "https://" + wikipediaLang + ".wikipedia.org";
        }
    }

    static List<WikipediaEntry> getEntriesFromCoordinates(String wikipediaLang, LatLon min, LatLon max) {
        try {
            // construct url
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php"
                    + "?action=query"
                    + "&list=geosearch"
                    + "&format=xml"
                    + "&gslimit=500"
                    + "&gsbbox=" + max.lat() + "|" + min.lon() + "|" + min.lat() + "|" + max.lon();
            // parse XML document
            final XPathExpression xpathPlacemark = X_PATH.compile("//gs");
            final XPathExpression xpathName = X_PATH.compile("@title");
            final XPathExpression xpathLat = X_PATH.compile("@lat");
            final XPathExpression xpathLon = X_PATH.compile("@lon");
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document doc = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) xpathPlacemark.evaluate(doc, XPathConstants.NODESET);
                final List<WikipediaEntry> entries = new ArrayList<>(nodes.getLength());
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final String name = xpathName.evaluate(node);
                    final LatLon latLon = new LatLon((
                            (double) xpathLat.evaluate(node, XPathConstants.NUMBER)),
                            (double) xpathLon.evaluate(node, XPathConstants.NUMBER));
                    if ("wikidata".equals(wikipediaLang)) {
                        entries.add(new WikidataEntry(name, null, latLon, null));
                    } else {
                        entries.add(new WikipediaEntry(wikipediaLang, name, name, latLon
                        ));
                    }
                }
                if ("wikidata".equals(wikipediaLang)) {
                    final List<WikidataEntry> withLabel = getLabelForWikidata(entries, Locale.getDefault());
                    return new ArrayList<>(withLabel);
                } else {
                    return entries;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikidataEntry> getWikidataEntriesForQuery(final String languageForQuery, final String query, final Locale localeForLabels) {
        try {
            final String url = "https://www.wikidata.org/w/api.php" +
                    "?action=wbsearchentities" +
                    "&language=" + languageForQuery +
                    "&strictlanguage=false" +
                    "&search=" + Utils.encodeUrl(query) +
                    "&limit=50" +
                    "&format=xml";
            final List<WikidataEntry> r = new ArrayList<>();
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) X_PATH.compile("//entity").evaluate(xml, XPathConstants.NODESET);
                final XPathExpression xpathId = X_PATH.compile("@id");
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final String id = (String) xpathId.evaluate(node, XPathConstants.STRING);
                    r.add(new WikidataEntry(id, null, null, null));
                }
            }
            return getLabelForWikidata(r, localeForLabels);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikipediaEntry> getEntriesFromCategory(String wikipediaLang, String category, int depth) {
        try {
            final String url = "https://tools.wmflabs.org/cats-php/"
                    + "?lang=" + wikipediaLang
                    + "&depth=" + depth
                    + "&cat=" + Utils.encodeUrl(category);

            try (final Scanner scanner = new Scanner(
                    HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContentReader())
                    .useDelimiter("\n")) {
                final List<WikipediaEntry> entries = new ArrayList<>();
                while (scanner.hasNext()) {
                    final String article = scanner.next().trim().replace("_", " ");
                    entries.add(new WikipediaEntry(wikipediaLang, article));
                }
                return entries;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikipediaEntry> getEntriesFromClipboard(final String wikipediaLang) {
        return Arrays.stream(Utils.getClipboardContent().split("[\\n\\r]+"))
                .map(x -> new WikipediaEntry(wikipediaLang, x))
                .collect(Collectors.toList());
    }

    static void updateWIWOSMStatus(String wikipediaLang, Collection<WikipediaEntry> entries) {
        Collection<String> articleNames = new ArrayList<>();
        for (WikipediaEntry i : entries) {
            articleNames.add(i.wikipediaArticle);
        }
        Map<String, Boolean> status = new HashMap<>();
        if (!articleNames.isEmpty()) {
            final String url = "https://tools.wmflabs.org/wiwosm/osmjson/getGeoJSON.php?action=check"
                    + "&lang=" + wikipediaLang;

            try {
                final String requestBody = "articles=" + Utils.encodeUrl(Utils.join(",", articleNames));
                try (final Scanner scanner = new Scanner(
                        HttpClient.create(new URL(url), "POST").setReasonForRequest("Wikipedia")
                                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                                .setRequestBody(requestBody.getBytes(StandardCharsets.UTF_8))
                                .connect().getContentReader())
                        .useDelimiter("\n")) {
                    while (scanner.hasNext()) {
                        //[article]\t[0|1]
                        final String line = scanner.next();
                        final String[] x = line.split("\t");
                        if (x.length == 2) {
                            status.put(x[0], "1".equals(x[1]));
                        } else {
                            Main.error("Unknown element " + line);
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        for (WikipediaEntry i : entries) {
            i.setWiwosmStatus(status.get(i.wikipediaArticle));
        }
    }

    static Stream<String> getWikipediaArticles(final String wikipediaLang, OsmPrimitive p) {
        if ("wikidata".equals(wikipediaLang)) {
            return Stream.of(p.get("wikidata")).filter(Objects::nonNull);
        }
        final Map<String, String> tags = p.getKeys();
        return Stream
                .of(
                        WikipediaLangArticle.parseTag("wikipedia", tags.get("wikipedia")),
                        WikipediaLangArticle.parseTag("wikipedia:" + wikipediaLang, tags.get("wikipedia:" + wikipediaLang))
                ).filter(Objects::nonNull)
                .filter(wikipediaLang::equals)
                .map(wp -> wp.article);
    }

    /**
     * Returns a map mapping wikipedia articles to wikidata ids.
     */
    static Map<String, String> getWikidataForArticles(String wikipediaLang, List<String> articles) {
        if (articles.size() > 50) {
            final Map<String, String> wikidataItems = new HashMap<>();
            for (final List<String> chunk : partitionList(articles, 50)) {
                wikidataItems.putAll(getWikidataForArticles(wikipediaLang, chunk));
            }
            return wikidataItems;
        }
        try {
            final String url = "https://www.wikidata.org/w/api.php" +
                    "?action=wbgetentities" +
                    "&props=sitelinks" +
                    "&sites=" + wikipediaLang + "wiki" +
                    "&sitefilter=" + wikipediaLang + "wiki" +
                    "&format=xml" +
                    "&titles=" + articles.stream().map(Utils::encodeUrl).collect(Collectors.joining("|"));
            final Map<String, String> r = new TreeMap<>();
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) X_PATH.compile("//entity").evaluate(xml, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final String wikidata = (String) X_PATH.compile("./@id").evaluate(node, XPathConstants.STRING);
                    final String wikipedia = (String) X_PATH.compile("./sitelinks/sitelink/@title").evaluate(node, XPathConstants.STRING);
                    r.put(wikipedia, wikidata);
                }
            }
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<String> getCategoriesForPrefix(final String wikipediaLang, final String prefix) {
        try {
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php"
                    + "?action=query"
                    + "&list=prefixsearch"
                    + "&format=xml"
                    + "&psnamespace=14"
                    + "&pslimit=50"
                    + "&pssearch=" + Utils.encodeUrl(prefix);
            // parse XML document
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document doc = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) X_PATH.compile("//ps/@title").evaluate(doc, XPathConstants.NODESET);
                final List<String> categories = new ArrayList<>(nodes.getLength());
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final String value = node.getNodeValue();
                    categories.add(value.contains(":") ? value.split(":", 2)[1] : value);
                }
                return categories;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static String getLabelForWikidata(String wikidataId, Locale locale, String ... preferredLanguage) {
        try {
            return getLabelForWikidata(Collections.singletonList(new WikidataEntry(wikidataId, null, null, null)), locale, preferredLanguage).get(0).label;
        } catch (IndexOutOfBoundsException ignore) {
            return null;
        }
    }

    static List<WikidataEntry> getLabelForWikidata(List<? extends WikipediaEntry> entries, Locale locale, String ... preferredLanguage) {
        if (entries.size() > 50) {
            final List<WikidataEntry> entriesWithLabel = new ArrayList<>(entries.size());
            for (final List<? extends WikipediaEntry> chunk : partitionList(entries, 50)) {
                entriesWithLabel.addAll(getLabelForWikidata(chunk, locale, preferredLanguage));
            }
            return entriesWithLabel;
        }
        try {
            final String url = "https://www.wikidata.org/w/api.php" +
                    "?action=wbgetentities" +
                    "&props=labels|descriptions" +
                    "&ids=" + entries.stream().map(x -> x.wikipediaArticle).collect(Collectors.joining("|")) +
                    "&format=xml";
            final Collection<String> languages = new ArrayList<>();
            if (locale != null) {
                languages.add(getMediawikiLocale(locale));
                languages.add(getMediawikiLocale(new Locale(locale.getLanguage())));
            }
            languages.addAll(Arrays.asList(preferredLanguage));
            languages.add("en");
            languages.add(null);
            final List<WikidataEntry> r = new ArrayList<>(entries.size());
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                for (final WikipediaEntry entry : entries) {
                    final Node entity = (Node) X_PATH.compile("//entity[@id='" + entry.wikipediaArticle + "']").evaluate(xml, XPathConstants.NODE);
                    r.add(new WikidataEntry(
                            entry.wikipediaArticle,
                            getFirstField(languages, "label", entity),
                            entry.coordinate,
                            getFirstField(languages, "description", entity)
                    ));
                }
            }
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getFirstField(Iterable<String> languages, String field, Node entity) throws XPathExpressionException {
        for (String language : languages) {
            final String label = (String) X_PATH.compile(language != null
                    ? ".//" + field + "[@language='" + language + "']/@value"
                    : ".//" + field + "/@value"
            ).evaluate(entity, XPathConstants.STRING);
            if (label != null && !label.isEmpty()) {
                return label;
            }
        }
        return null;
    }

    static Collection<WikipediaLangArticle> getInterwikiArticles(String wikipediaLang, String article) {
        try {
            Collection<WikipediaLangArticle> r = new ArrayList<>();
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php" +
                    "?action=query" +
                    "&prop=langlinks" +
                    "&titles=" + Utils.encodeUrl(article) +
                    "&lllimit=500" +
                    "&format=xml";
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) X_PATH.compile("//ll").evaluate(xml, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    final String lang = nodes.item(i).getAttributes().getNamedItem("lang").getTextContent();
                    final String name = nodes.item(i).getTextContent();
                    r.add(new WikipediaLangArticle(lang, name));
                }
            }
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static LatLon getCoordinateForArticle(String wikipediaLang, String article) {
        try {
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php" +
                    "?action=query" +
                    "&prop=coordinates" +
                    "&titles=" + Utils.encodeUrl(article) +
                    "&format=xml";
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final Node node = (Node) X_PATH.compile("//coordinates/co").evaluate(xml, XPathConstants.NODE);
                if (node == null) {
                    return null;
                } else {
                    final double lat = Double.parseDouble(node.getAttributes().getNamedItem("lat").getTextContent());
                    final double lon = Double.parseDouble(node.getAttributes().getNamedItem("lon").getTextContent());
                    return new LatLon(lat, lon);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static class WikipediaLangArticle {

        final String lang, article;

        public WikipediaLangArticle(String lang, String article) {
            this.lang = lang;
            this.article = article;
        }

        public static WikipediaLangArticle parseFromUrl(String url) {
            if (url == null) {
                return null;
            }
            // decode URL for nicer value
            url = Utils.decodeUrl(url);
            // extract Wikipedia language and
            final Matcher m = Pattern.compile("(https?:)?//(\\w*)\\.wikipedia\\.org/wiki/(.*)").matcher(url);
            if (!m.matches()) {
                return null;
            }
            return new WikipediaLangArticle(m.group(2), m.group(3));
        }

        public static WikipediaLangArticle parseTag(String key, String value) {
            if (value == null) {
                return null;
            } else if (value.startsWith("http")) {
                //wikipedia=http...
                return parseFromUrl(value);
            } else if (value.contains(":")) {
                //wikipedia=[lang]:[article]
                //wikipedia:[lang]=[lang]:[article]
                final String[] item = Utils.decodeUrl(value).split(":", 2);
                final String article = item[1].replace("_", " ");
                return new WikipediaLangArticle(item[0], article);
            } else if (key.startsWith("wikipedia:")) {
                //wikipedia:[lang]=[lang]:[article]
                //wikipedia:[lang]=[article]
                final String lang = key.split(":", 2)[1];
                final String[] item = Utils.decodeUrl(value).split(":", 2);
                final String article = item[item.length == 2 ? 1 : 0].replace("_", " ");
                return new WikipediaLangArticle(lang, article);
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return lang + ":" + article;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final WikipediaLangArticle that = (WikipediaLangArticle) o;
            return Objects.equals(lang, that.lang) &&
                    Objects.equals(article, that.article);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lang, article);
        }
    }

    static class WikipediaEntry implements Comparable<WikipediaEntry> {

        final String label;
        final String wikipediaLang, wikipediaArticle;
        final LatLon coordinate;
        private Boolean wiwosmStatus;

        WikipediaEntry(String wikipediaLang, String wikipediaArticle) {
            this(wikipediaLang, wikipediaArticle, null, null);
        }

        WikipediaEntry(String wikipediaLang, String wikipediaArticle, String label, LatLon coordinate) {
            this.label = label;
            this.wikipediaLang = wikipediaLang;
            this.wikipediaArticle = wikipediaArticle;
            this.coordinate = coordinate;
        }

        protected Tag createWikipediaTag() {
            return new Tag("wikipedia", wikipediaLang + ":" + wikipediaArticle);
        }

        public void setWiwosmStatus(Boolean wiwosmStatus) {
            this.wiwosmStatus = wiwosmStatus;
        }

        public Boolean getWiwosmStatus() {
            return wiwosmStatus;
        }

        public String getBrowserUrl() {
            return getSiteUrl(wikipediaLang) + "/wiki/" + Utils.encodeUrl(wikipediaArticle.replace(" ", "_"));
        }

        public String getLabelText() {
            return wikipediaArticle;
        }

        @Override
        public String toString() {
            return wikipediaArticle;
        }

        @Override
        public int compareTo(WikipediaEntry o) {
            final int c = AlphanumComparator.getInstance().compare(label, o.label);
            return c != 0 ? c : AlphanumComparator.getInstance().compare(wikipediaArticle, o.wikipediaArticle);
        }
    }

    static class WikidataEntry extends WikipediaEntry {

        final String description;

        WikidataEntry(String id, String label, LatLon coordinate, String description) {
            super("wikidata", id, label, coordinate);
            this.description = description;
            ensureValidWikidataId(id);
        }

        @Override
        protected Tag createWikipediaTag() {
            return new Tag("wikidata", wikipediaArticle);
        }

        @Override
        public String getLabelText() {
            final String descriptionInParen = description == null ? "" : (" (" + description + ")");
            return getLabelText(label, wikipediaArticle + descriptionInParen);
        }

        static String getLabelText(String bold, String gray) {
            return Utils.escapeReservedCharactersHTML(bold) + " <span color='gray'>" + Utils.escapeReservedCharactersHTML(gray) + "</span>";
        }
    }

    static void ensureValidWikidataId(String id) {
        CheckParameterUtil.ensureThat(WIKIDATA_PATTERN.matcher(id).matches(), "Invalid Wikidata ID given: " + id);
    }

    public static <T> List<List<T>> partitionList(final List<T> list, final int size) {
        return new AbstractList<List<T>>() {
            @Override
            public List<T> get(int index) {
                final int fromIndex = index * size;
                final int toIndex = Math.min(fromIndex + size, list.size());
                return list.subList(fromIndex, toIndex);
            }

            @Override
            public int size() {
                return (int) Math.ceil(((float) list.size()) / size);
            }
        };
    }

    private static DocumentBuilder newDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            Main.warn("Cannot create DocumentBuilder");
            Main.warn(e);
            throw new RuntimeException(e);
        }
    }
}
