/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.io;

import java.util.Stack;

import org.jopendocument.model.office.OfficeBody;
import org.jopendocument.model.office.OfficeSpreadsheet;
import org.jopendocument.model.table.TableTable;
import org.jopendocument.model.table.TableTableCell;
import org.jopendocument.model.table.TableTableColumn;
import org.jopendocument.model.table.TableTableRow;
import org.jopendocument.model.text.TextP;
import org.jopendocument.model.text.TextSpan;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SaxContentUnmarshaller extends DefaultHandler {

    private OfficeBody body;

    private Object current;

    private final Stack<Object> stack;

    // -----

    public SaxContentUnmarshaller() {
        this.stack = new Stack<Object>();
    }

    // ----- callbacks: -----

    private void assertParsed(final Attributes attribs, final int l) {
        if (attribs.getLength() > l) {
            for (int i = 0; i < attribs.getLength(); i++) {
                System.err.println(attribs.getQName(i) + "  -> " + attribs.getValue(i));
            }
            throw new IllegalStateException("Somme attributes are not parsed");
        }
    }

    public void characters(final char[] data, final int start, final int length) {
        final StringBuffer s = new StringBuffer();
        s.append(data, start, length);
        if (this.current instanceof TextP) {
            ((TextP) this.current).addToLastTextSpan(s.toString());
        } else if (this.current instanceof TextSpan) {
            ((TextSpan) this.current).concantValue(s.toString());
        }
    }

    // -----

    public void endElement(final String uri, final String localName, final String qName) {
        this.pop();
    }

    // -----

    public OfficeBody getBody() {
        return this.body;
    }

    // -----

    private void pop() {

        if (!this.stack.isEmpty()) {
            this.stack.pop();

        }
        if (!this.stack.isEmpty()) {
            this.current = this.stack.peek();
        }
    }

    private void push(final Object o) {
        this.current = o;
        this.stack.push(o);

    }

    public void startElement(final String uri, final String localName, final String qName, final Attributes attribs) {

        if (qName.equals("office:automatic-styles")) {
            /*this.autostyles = new OfficeAutomaticStyles();
            this.document.setAutomaticStyles(this.autostyles);
            this.push(this.autostyles);*/
        	this.push(new Object());
        } else if (qName.equals("style:style")) {
            final Object style = new Object();
        	/*final StyleStyle style = new StyleStyle();
            style.setStyleName(attribs.getValue("style:name"));
            style.setStyleFamily(attribs.getValue("style:family"));
            style.setStyleParentStyleName(attribs.getValue("style:parent-style-name"));
            style.setMasterPageName(attribs.getValue("style:master-page-name"));

            // style:data-style-name="N108"
            if (this.current instanceof OfficeAutomaticStyles) {
                this.autostyles.addStyle(style);
            } else {
                System.err.println("Not OfficeAutomaticStyles:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(style);
        } else if (qName.equals("number:number-style")) {
        	final Object style = new Object();
            /*final NumberNumberStyle style = new NumberNumberStyle();
            style.setStyleName(attribs.getValue("style:name"));
            style.setStyleFamily(attribs.getValue("style:family"));

            // style:data-style-name="N108"
            if (this.current instanceof OfficeAutomaticStyles) {
                this.autostyles.addStyle(style);
            } else {
                System.err.println("Not OfficeAutomaticStyles:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(style);
        } else if (qName.equals("style:table-row-properties")) {
        	final Object props = new Object();
            /*final StyleTableRowProperties props = new StyleTableRowProperties();
            props.setFoBreakBefore(attribs.getValue("fo:break-before"));
            props.setRowHeight(attribs.getValue("style:row-height"));
            //props.setUseOptimalRowHeight(attribs.getValue("style:use-optimal-row-height"));
            if (this.current instanceof StyleStyle) {
                ((StyleStyle) this.current).setTableRowProperties(props);
            } else {
                System.err.println("Not StyleStyle:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(props);
        } else if (qName.equals("style:table-properties")) {
            //final StyleTableProperties props = new StyleTableProperties();
            final Object props = new Object();
            //props.setDisplay(ValueHelper.getBoolean(attribs.getValue("table:display")));
            //props.setWritingMode(attribs.getValue("style:writing-mode"));

            /*if (this.current instanceof StyleStyle) {
                ((StyleStyle) this.current).setTableProperties(props);
            } else {
                System.err.println("Not StyleStyle:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(props);
        } else if (qName.equals("style:table-cell-properties")) {
        	final Object props = new Object();
            /*final StyleTableCellProperties props = new StyleTableCellProperties();
            props.setVerticalAlign(attribs.getValue("style:vertical-align"));
            props.setBackgroundColor(attribs.getValue("fo:background-color"));

            props.setPadding(attribs.getValue("fo:padding"));

            props.setTextAlignSource(attribs.getValue("style:text-align-source"));
            props.setRepeatContent(attribs.getValue("style:repeat-content"));

            props.setBorderLeft(attribs.getValue("fo:border-left"));
            props.setBorderRight(attribs.getValue("fo:border-right"));

            props.setBorderTop(attribs.getValue("fo:border-top"));
            props.setBorderBottom(attribs.getValue("fo:border-bottom"));
            // doit etre apres pour overrider le border!
            props.setBorder(attribs.getValue("fo:border"));
            props.setWrapOption(attribs.getValue("fo:wrap-option"));
            if (this.current instanceof StyleStyle) {
                ((StyleStyle) this.current).setTableCellProperties(props);

            } else {
                System.err.println("Not StyleStyle:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(props);
        } else if (qName.equals("style:text-properties")) {
        	final Object props = new Object();
            /*final StyleTextProperties props = new StyleTextProperties();
            props.setFontName(attribs.getValue("style:font-name"));
            props.setFontSize(attribs.getValue("fo:font-size"));
            props.setFontWeight(attribs.getValue("fo:font-weight"));
            props.setColor(attribs.getValue("fo:color"));
            // fo:hyphenate="true"
            if (this.current instanceof StyleStyle) {
                ((StyleStyle) this.current).setTextProperties(props);
            } else {
                System.err.println("Not StyleStyle:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(props);
        } else if (qName.equals("style:table-column-properties")) {
        	final Object props = new Object();
            /*final StyleTableColumnProperties props = new StyleTableColumnProperties();
            props.setFoBreakBefore(attribs.getValue("fo:break-before"));
            props.setStyleColumnWidth(attribs.getValue("style:column-width"));

            if (this.current instanceof StyleStyle) {
                ((StyleStyle) this.current).setTableColumnProperties(props);
            } else {
                System.err.println("Not StyleStyle:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(props);
        } else if (qName.equals("style:paragraph-properties")) {
        	final Object props = new Object();
            /*final StyleParagraphProperties props = new StyleParagraphProperties();
            props.setTextAlign(attribs.getValue("fo:text-align"));
            props.setMarginLeft(attribs.getValue("fo:margin-left"));

            if (this.current instanceof StyleStyle) {
                ((StyleStyle) this.current).setParagraphProperties(props);
            } else {
                System.err.println("Not StyleStyle:" + this.current);
                Thread.dumpStack();
            }*/
            this.push(props);
        } else if (qName.equals("office:body")) {
            this.body = new OfficeBody();
            this.push(this.body);
        } else if (qName.equals("office:spreadsheet")) {
            final OfficeSpreadsheet spread = new OfficeSpreadsheet();
            if (this.current instanceof OfficeBody) {
                ((OfficeBody) this.current).addOfficeSpreadsheet(spread);
            } else {
                System.err.println("Not StyleStyle:" + this.current);
                Thread.dumpStack();
            }

            this.push(spread);

        } else if (qName.equals("table:table")) {
            final TableTable table = new TableTable();
            final String printranges = attribs.getValue("table:print-ranges");
            if (printranges != null) {
                table.setTablePrintRanges(printranges);
            }
            this.assertParsed(attribs, 3);
            if (this.current instanceof OfficeSpreadsheet) {
                ((OfficeSpreadsheet) this.current).addTable(table);
            } else {
                System.err.println("Not OfficeSpreadsheet:" + this.current);
                Thread.dumpStack();
            }
            this.push(table);

        } else if (qName.equals("table:table-column")) {
            final TableTableColumn col = new TableTableColumn();
            col.setTableStyleName(attribs.getValue("table:style-name"));
            col.setTableDefaultCellStyleName(attribs.getValue("table:default-cell-style-name"));
            col.setTableNumberColumnsRepeated(attribs.getValue("table:number-columns-repeated"));

            this.assertParsed(attribs, 3);
            if (this.current instanceof TableTable) {
                ((TableTable) this.current).addColumn(col);
            } else {
                System.err.println("Not TableTable:" + this.current);
                Thread.dumpStack();
            }
            this.push(col);

        } else if (qName.equals("table:table-row")) {

            final TableTableRow row = new TableTableRow();
            row.setTableNumberRowsRepeated(attribs.getValue("table:number-rows-repeated"));

            if (this.current instanceof TableTable) {
                ((TableTable) this.current).addRow(row);
            } else {
                System.err.println("Not TableTable:" + this.current);
            }
            this.push(row);

        } else if (qName.equals("table:table-cell") || qName.equals("table:covered-table-cell")) {
            final TableTableCell cell = new TableTableCell();
            cell.setTableStyleName(attribs.getValue("table:style-name"));
            //cell.setTableNumberColumnsRepeated(attribs.getValue("table:number-columns-repeated"));
            //cell.setTableNumberColumnsSpanned(attribs.getValue("table:number-columns-spanned"));
            //cell.setTableNumberRowsSpanned(attribs.getValue("table:number-rows-spanned"));
            //cell.setTableValueType(attribs.getValue("office:value-type"));

            if (this.current instanceof TableTableRow) {
                ((TableTableRow) this.current).addCell(cell);
            } else {
                System.err.println("Not TableTableRow:" + this.current);
                Thread.dumpStack();
            }
            this.push(cell);

        } else if (qName.equals("text:p")) {
            final TextP p = new TextP();
            // TODO: gerer le multi textp dans une cellule
            if (this.current instanceof TableTableCell) {
                ((TableTableCell) this.current).setTextP(p);
            } /*else if (this.current instanceof DrawImage) {
                ((DrawImage) this.current).setTextP(p);
            }*/ else {
                System.err.println("Not TableTableCell:" + this.current + " classe:" + this.current.getClass());
                Thread.dumpStack();
            }
            this.push(p);

        } else if (qName.equals("text:span")) {
            final TextSpan textspan = new TextSpan();

            if (this.current instanceof TextP) {
                ((TextP) this.current).addTextSpan(textspan);
            } else {
                System.err.println("Not TextP:" + this.current);
                Thread.dumpStack();
            }
            this.push(textspan);

        } /*else if (qName.equals("draw:frame")) {
            final DrawFrame p = new DrawFrame();
            p.setSvgWidth(attribs.getValue("svg:width"));
            p.setSvgHeight(attribs.getValue("svg:height"));
            p.setSvgX(attribs.getValue("svg:x"));
            p.setSvgY(attribs.getValue("svg:y"));

            if (this.current instanceof TableTableCell) {
                ((TableTableCell) this.current).addDrawFrame(p);
            } else if (this.current instanceof TableShapes) {
                ((TableShapes) this.current).addDrawFrame(p);
            } else {
                System.err.println("Not TableTableCell:" + this.current);
                Thread.dumpStack();
            }
            this.push(p);

        } else if (qName.equals("draw:image")) {
            final DrawImage p = new DrawImage();
            final String link = attribs.getValue("xlink:href");
            p.setXlinkHref(link);
            this.document.preloadImage(link);
            if (this.current instanceof DrawFrame) {
                ((DrawFrame) this.current).setDrawImage(p);
            } else {
                System.err.println("Not DrawFrame:" + this.current);
                Thread.dumpStack();
            }
            this.push(p);

        } else if (qName.equals("table:shapes")) {
            final TableShapes p = new TableShapes();

            if (this.current instanceof TableTable) {
                ((TableTable) this.current).setTableShapes(p);
            } else {
                System.err.println("Not TableTable:" + this.current);
                Thread.dumpStack();
            }
            this.push(p);

        } else if (qName.equals("office:scripts")) {
            this.scripts = new OfficeScripts();

            this.push(this.scripts);

        } else if (qName.equals("office:font-face-decls")) {
            this.fontDeclarations = new FontFaceDecls();

            this.push(this.fontDeclarations);

        } else if (qName.equals("style:font-face")) {
            final StyleFontFace p = new StyleFontFace();
            p.setStyleName(attribs.getValue("style:name"));
            p.setFontFamily(attribs.getValue("svg:font-family"));
            p.setFontFamilyGeneric(attribs.getValue("style:font-family-generic"));
            p.setFontPitch(attribs.getValue("style:font-pitch"));

            if (this.current instanceof FontFaceDecls) {
                ((FontFaceDecls) this.current).addFontFace(p);
            } else {
                System.err.println("Not FontFaceDecls:" + this.current);
                Thread.dumpStack();
            }
            this.push(p);

        }*/

        else {
            //System.err.println("content.xml : ignoring :" + qName);
            this.push(uri);

        }
    }

}
