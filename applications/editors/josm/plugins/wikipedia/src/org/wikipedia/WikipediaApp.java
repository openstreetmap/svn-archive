// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.gui.datatransfer.ClipboardUtils;
import org.openstreetmap.josm.tools.AlphanumComparator;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public final class WikipediaApp {

    public static final Pattern WIKIDATA_PATTERN = Pattern.compile("Q\\d+");
    private static final DocumentBuilder DOCUMENT_BUILDER = newDocumentBuilder();
    private static final XPath X_PATH = XPath.getInstance();

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
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document doc = DOCUMENT_BUILDER.parse(in);
                final List<WikipediaEntry> entries = X_PATH.evaluateNodes("//gs", doc).stream()
                        .map(node -> {
                            final String name = X_PATH.evaluateString("@title", node);
                            final LatLon latLon = new LatLon(
                                    X_PATH.evaluateDouble("@lat", node),
                                    X_PATH.evaluateDouble("@lon", node));
                            if ("wikidata".equals(wikipediaLang)) {
                                return new WikidataEntry(name, null, latLon, null);
                            } else {
                                return new WikipediaEntry(wikipediaLang, name, name, latLon);
                            }
                        }).collect(Collectors.toList());
                if ("wikidata".equals(wikipediaLang)) {
                    return getLabelForWikidata(entries, Locale.getDefault()).stream().collect(Collectors.toList());
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
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final List<WikidataEntry> r = X_PATH.evaluateNodes("//entity", xml).stream()
                        .map(node -> new WikidataEntry(X_PATH.evaluateString("@id", node), null, null, null))
                        .collect(Collectors.toList());
                return getLabelForWikidata(r, localeForLabels);
            }
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

            try (final BufferedReader reader = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia")
                    .connect().getContentReader()) {
                return reader.lines()
                        .map(line -> new WikipediaEntry(wikipediaLang, line.trim().replace("_", " ")))
                        .collect(Collectors.toList());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikipediaEntry> getEntriesFromClipboard(final String wikipediaLang) {
        return Pattern.compile("[\\n\\r]+")
                .splitAsStream(ClipboardUtils.getClipboardStringContent())
                .map(x -> new WikipediaEntry(wikipediaLang, x))
                .collect(Collectors.toList());
    }

    static void updateWIWOSMStatus(String wikipediaLang, List<WikipediaEntry> entries) {
        if (entries.size() > 20) {
            partitionList(entries, 20).forEach(chunk -> updateWIWOSMStatus(wikipediaLang, chunk));
            return;
        }
        Map<String, Boolean> status = new HashMap<>();
        if (!entries.isEmpty()) {
            final String url = "https://tools.wmflabs.org/wiwosm/osmjson/getGeoJSON.php?action=check&lang=" + wikipediaLang;
            try {
                final String articles = entries.stream().map(i -> i.wikipediaArticle).collect(Collectors.joining(","));
                final String requestBody = "articles=" + Utils.encodeUrl(articles);
                try (final BufferedReader reader = HttpClient.create(new URL(url), "POST").setReasonForRequest("Wikipedia")
                                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                                .setRequestBody(requestBody.getBytes(StandardCharsets.UTF_8))
                                .connect().getContentReader()) {
                    reader.lines().forEach(line -> {
                        //[article]\t[0|1]
                        final String[] x = line.split("\t");
                        if (x.length == 2) {
                            status.put(x[0], "1".equals(x[1]));
                        } else {
                            Main.error("Unknown element " + line);
                        }
                    });
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
        return Stream
                .of("wikipedia", "wikipedia:" + wikipediaLang)
                .map(key -> WikipediaLangArticle.parseTag(key, p.get(key)))
                .filter(Objects::nonNull)
                .filter(wp -> wikipediaLang.equals(wp.lang))
                .map(wp -> wp.article);
    }

    /**
     * Returns a map mapping wikipedia articles to wikidata ids.
     */
    static Map<String, String> getWikidataForArticles(String wikipediaLang, List<String> articles) {
        if (articles.size() > 50) {
            return partitionList(articles, 50).stream()
                    .flatMap(chunk -> getWikidataForArticles(wikipediaLang, chunk).entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else if (articles.isEmpty()) {
            return Collections.emptyMap();
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
                X_PATH.evaluateNodes("//entity", xml).forEach(node -> {
                    final String wikidata = X_PATH.evaluateString("./@id", node);
                    final String wikipedia = X_PATH.evaluateString("./sitelinks/sitelink/@title", node);
                    r.put(wikipedia, wikidata);
                });
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
                return X_PATH.evaluateNodes("//ps/@title", doc).stream()
                        .map(Node::getNodeValue)
                        .map(value -> value.contains(":") ? value.split(":", 2)[1] : value)
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static String getLabelForWikidata(String wikidataId, Locale locale, String ... preferredLanguage) {
        try {
            final List<WikidataEntry> entry = Collections.singletonList(new WikidataEntry(wikidataId, null, null, null));
            return getLabelForWikidata(entry, locale, preferredLanguage).get(0).label;
        } catch (IndexOutOfBoundsException ignore) {
            return null;
        }
    }

    static List<WikidataEntry> getLabelForWikidata(List<? extends WikipediaEntry> entries, Locale locale, String ... preferredLanguage) {
        if (entries.size() > 50) {
            return partitionList(entries, 50).stream()
                    .flatMap(chunk -> getLabelForWikidata(chunk, locale, preferredLanguage).stream())
                    .collect(Collectors.toList());
        } else if (entries.isEmpty()) {
            return Collections.emptyList();
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
                    final Node entity = X_PATH.evaluateNode("//entity[@id='" + entry.wikipediaArticle + "']", xml);
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

    private static String getFirstField(Collection<String> languages, String field, Node entity) {
        return languages.stream()
                .map(language -> X_PATH.evaluateString(language != null
                        ? ".//" + field + "[@language='" + language + "']/@value"
                        : ".//" + field + "/@value", entity))
                .filter(label -> label != null && !label.isEmpty())
                .findFirst()
                .orElse(null);
    }

    static Collection<WikipediaLangArticle> getInterwikiArticles(String wikipediaLang, String article) {
        try {
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php" +
                    "?action=query" +
                    "&prop=langlinks" +
                    "&titles=" + Utils.encodeUrl(article) +
                    "&lllimit=500" +
                    "&format=xml";
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                return X_PATH.evaluateNodes("//ll", xml).stream()
                        .map(node -> {
                            final String lang = X_PATH.evaluateString("@lang", node);
                            final String name = node.getTextContent();
                            return new WikipediaLangArticle(lang, name);
                        }).collect(Collectors.toList());
            }
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
                final Node node = X_PATH.evaluateNode("//coordinates/co", xml);
                if (node == null) {
                    return null;
                } else {
                    return new LatLon(X_PATH.evaluateDouble("@lat", node), X_PATH.evaluateDouble("@lon", node));
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
            return Comparator
                    .<WikipediaEntry, String>comparing(x -> x.label, AlphanumComparator.getInstance())
                    .thenComparing(x -> x.wikipediaArticle, AlphanumComparator.getInstance())
                    .compare(this, o);
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
