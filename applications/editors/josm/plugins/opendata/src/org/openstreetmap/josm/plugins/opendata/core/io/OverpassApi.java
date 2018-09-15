// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

public abstract class OverpassApi {

    public enum OaQueryType {
        NODE("node"),
        WAY("way"),
        RELATION("relation");

        @Override
        public String toString() {
            return this.value;
        }

        OaQueryType(final String value) {
            this.value = value;
        }

        private final String value;
    }

    public enum OaRecurseType {
        RELATION_RELATION("relation-relation"),
        RELATION_BACKWARDS("relation-backwards"),
        RELATION_WAY("relation-way"),
        RELATION_NODE("relation-node"),
        WAY_NODE("way-node"),
        WAY_RELATION("way-relation"),
        NODE_RELATION("node-relation"),
        NODE_WAY("node-way");

        @Override
        public String toString() {
            return this.value;
        }

        OaRecurseType(final String value) {
            this.value = value;
        }

        private final String value;
    }

    public static final StringBuilder union(CharSequence... queries) {
        StringBuilder result = new StringBuilder("<union>\n");
        for (CharSequence query : queries) {
            if (query != null) {
                result.append(query).append('\n');
            }
        }
        result.append("</union>");
        return result;
    }

    public static final StringBuilder query(String bbox, OaQueryType type, CharSequence... conditions) {
        StringBuilder result = new StringBuilder("<query type=\"").append(type).append("\" >\n");
        if (bbox != null) {
            result.append("<bbox-query ").append(bbox).append("/>\n");
        }
        for (CharSequence condition : conditions) {
            if (condition != null) {
                result.append(condition).append('\n');
            }
        }
        result.append("</query>");
        return result;
    }

    public static final String recurse(OaRecurseType type, String into) {
        return "<recurse type=\""+type+"\" into=\""+into+"\"/>\n";
    }

    public static final String recurse(OaRecurseType... types) {
        String result = "";
        for (OaRecurseType type : types) {
            result += "<recurse type=\""+type+"\"/>\n";
        }
        return result;
    }

    public static final String print() {
        return "<print mode=\"meta\"/>";
    }

    public static final String hasKey(String key) {
        return hasKey(key, null);
    }

    public static final String hasKey(String key, String value) {
        return "<has-kv k=\""+key+"\" "+(value != null && !value.isEmpty() ? "v=\""+value+"\"" : "")+" />";
    }
}
