package org.openstreetmap.josm.plugins.ohe.parser;

public class SyntaxException extends Exception {

    private int startColumn;
    private int endColumn;

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public SyntaxException(String info, int startColumn, int endColumn) {
        super(info);
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }
}
