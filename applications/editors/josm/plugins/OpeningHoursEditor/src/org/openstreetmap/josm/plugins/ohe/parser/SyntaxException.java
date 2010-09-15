package org.openstreetmap.josm.plugins.ohe.parser;

public class SyntaxException extends Exception {

    private int startColumn;
    private int endColumn;
    private String info;

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public String getInfo() {
        return info;
    }

    public SyntaxException(String info, int startColumn, int endColumn) {
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.info = info;
    }
}
