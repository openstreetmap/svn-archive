
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Extended Format Record
 * Description:  Probably one of the more complex records.  There are two breeds:
 *               Style and Cell.
 *<P>
 *               It should be noted that fields in the extended format record are
 *               somewhat arbitrary.  Almost all of the fields are bit-level, but
 *               we name them as best as possible by functional group.  In some
 *               places this is better than others.
 *<P>
 *
 * REFERENCE:  PG 426 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public final class ExtendedFormatRecord
    extends StandardRecord
{
    public final static short     sid                 = 0xE0;

    // xf type
    public final static short     XF_STYLE            = 1;
    public final static short     XF_CELL             = 0;

    // fields in BOTH style and Cell XF records
    private short                 field_1_font_index;             // not bit-mapped
    private short                 field_2_format_index;           // not bit-mapped

    // field_3_cell_options bit map
    static final private BitField _locked       = BitFieldFactory.getInstance(0x0001);
    static final private BitField _hidden       = BitFieldFactory.getInstance(0x0002);
    static final private BitField _xf_type      = BitFieldFactory.getInstance(0x0004);
    static final private BitField _parent_index = BitFieldFactory.getInstance(0xFFF0);
    private short                 field_3_cell_options;

    // field_4_alignment_options bit map
    static final private BitField _alignment          = BitFieldFactory.getInstance(0x0007);
    static final private BitField _wrap_text          = BitFieldFactory.getInstance(0x0008);
    static final private BitField _vertical_alignment = BitFieldFactory.getInstance(0x0070);
    static final private BitField _justify_last       = BitFieldFactory.getInstance(0x0080);
    static final private BitField _rotation           = BitFieldFactory.getInstance(0xFF00);
    private short                 field_4_alignment_options;

    // field_5_indention_options
    static final private BitField _indent                         =
        BitFieldFactory.getInstance(0x000F);
    static final private BitField _shrink_to_fit                  =
        BitFieldFactory.getInstance(0x0010);
    static final private BitField _merge_cells                    =
        BitFieldFactory.getInstance(0x0020);
    static final private BitField _reading_order                  =
        BitFieldFactory.getInstance(0x00C0);

    // apparently bits 8 and 9 are unused
    static final private BitField _indent_not_parent_format       =
        BitFieldFactory.getInstance(0x0400);
    static final private BitField _indent_not_parent_font         =
        BitFieldFactory.getInstance(0x0800);
    static final private BitField _indent_not_parent_alignment    =
        BitFieldFactory.getInstance(0x1000);
    static final private BitField _indent_not_parent_border       =
        BitFieldFactory.getInstance(0x2000);
    static final private BitField _indent_not_parent_pattern      =
        BitFieldFactory.getInstance(0x4000);
    static final private BitField _indent_not_parent_cell_options =
        BitFieldFactory.getInstance(0x8000);
    private short                 field_5_indention_options;

    // field_6_border_options bit map
    static final private BitField _border_left   = BitFieldFactory.getInstance(0x000F);
    static final private BitField _border_right  = BitFieldFactory.getInstance(0x00F0);
    static final private BitField _border_top    = BitFieldFactory.getInstance(0x0F00);
    static final private BitField _border_bottom = BitFieldFactory.getInstance(0xF000);
    private short                 field_6_border_options;

    // all three of the following attributes are palette options
    // field_7_palette_options bit map
    static final private BitField _left_border_palette_idx  =
        BitFieldFactory.getInstance(0x007F);
    static final private BitField _right_border_palette_idx =
        BitFieldFactory.getInstance(0x3F80);
    static final private BitField _diag                     =
        BitFieldFactory.getInstance(0xC000);
    private short                 field_7_palette_options;

    // field_8_adtl_palette_options bit map
    static final private BitField _top_border_palette_idx    =
        BitFieldFactory.getInstance(0x0000007F);
    static final private BitField _bottom_border_palette_idx =
        BitFieldFactory.getInstance(0x00003F80);
    static final private BitField _adtl_diag                 =
        BitFieldFactory.getInstance(0x001fc000);
    static final private BitField _adtl_diag_line_style      =
        BitFieldFactory.getInstance(0x01e00000);

    // apparently bit 25 is unused
    static final private BitField _adtl_fill_pattern         =
        BitFieldFactory.getInstance(0xfc000000);
    private int                   field_8_adtl_palette_options;   // additional to avoid 2

    // field_9_fill_palette_options bit map
    static final private BitField _fill_foreground = BitFieldFactory.getInstance(0x007F);
    static final private BitField _fill_background = BitFieldFactory.getInstance(0x3f80);

    // apparently bits 15 and 14 are unused
    private short                 field_9_fill_palette_options;

    /**
     * Constructor ExtendedFormatRecord
     *
     *
     */

    public ExtendedFormatRecord()
    {
    }

    public ExtendedFormatRecord(RecordInputStream in) // NO_UCD
    {
        field_1_font_index           = in.readShort();
        field_2_format_index         = in.readShort();
        field_3_cell_options         = in.readShort();
        field_4_alignment_options    = in.readShort();
        field_5_indention_options    = in.readShort();
        field_6_border_options       = in.readShort();
        field_7_palette_options      = in.readShort();
        field_8_adtl_palette_options = in.readInt();
        field_9_fill_palette_options = in.readShort();
    }


    /**
     * get the index to the FONT record (which font to use 0 based)
     *
     *
     * @return index to the font
     * @see org.apache.poi.hssf.record.FontRecord
     */

    public short getFontIndex()
    {
        return field_1_font_index;
    }

    /**
     *  get the index to the Format record (which FORMAT to use 0-based)
     *
     *
     * @return index to the format record
     * @see org.apache.poi.hssf.record.FormatRecord
     */

    public short getFormatIndex()
    {
        return field_2_format_index;
    }

    /**
     * gets the options bitmask - you can also use corresponding option bit getters
     * (see other methods that reference this one)
     *
     *
     * @return options bitmask
     *
     */

    public short getCellOptions()
    {
        return field_3_cell_options;
    }

    // These are the bit fields in cell options

    /**
     * get whether the cell is locked or not
     *
     *
     * @return locked - if the cell is locked
     * @see #getCellOptions()
     */

    public boolean isLocked()
    {
        return _locked.isSet(field_3_cell_options);
    }

    /**
     * get whether the cell is hidden or not
     *
     *
     * @return hidden - if the cell is hidden
     * @see #getCellOptions()
     */

    public boolean isHidden()
    {
        return _hidden.isSet(field_3_cell_options);
    }

    /**
     * get whether the cell is a cell or style XFRecord
     *
     *
     * @return type - cell or style (0/1)
     * @see #XF_STYLE
     * @see #XF_CELL
     * @see #getCellOptions()
     */

    public short getXFType()
    {
        return _xf_type.getShortValue(field_3_cell_options);
    }


    /**
     * for cell XF types this is the parent style (usually 0/normal).  For
     * style this should be NULL.
     *
     * @return index of parent XF
     * @see #NULL
     * @see #getCellOptions()
     */

    public short getParentIndex()
    {
        return _parent_index.getShortValue(field_3_cell_options);
    }

    // end bitfields in cell options

    /**
     * get the alignment options bitmask.  See corresponding bitgetter methods
     * that reference this one.
     *
     *
     * @return options     - the bitmask
     */

    public short getAlignmentOptions()
    {
        return field_4_alignment_options;
    }

    // bitfields in alignment options

    /**
     * get the horizontal alignment of the cell.
     *
     *
     * @return align - how to align the cell (see constants)
     * @see #GENERAL
     * @see #LEFT
     * @see #CENTER
     * @see #RIGHT
     * @see #FILL
     * @see #JUSTIFY
     * @see #CENTER_SELECTION
     * @see #getAlignmentOptions()
     */

    public short getAlignment()
    {
        return _alignment.getShortValue(field_4_alignment_options);
    }

    /**
     * get whether to wrap the text in the cell
     *
     *
     * @return wrapped - whether or not to wrap the cell text
     * @see #getAlignmentOptions()
     */

    public boolean getWrapText()
    {
        return _wrap_text.isSet(field_4_alignment_options);
    }

    /**
     * get the vertical alignment of text in the cell
     *
     *
     * @return where to align the text
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     *
     * @see #getAlignmentOptions()
     */

    public short getVerticalAlignment()
    {
        return _vertical_alignment.getShortValue(field_4_alignment_options);
    }

    /**
     * Dunno.  Docs just say this is for far east versions..  (I'm guessing it
     * justifies for right-to-left read languages)
     *
     *
     * @return justify
     * @see #getAlignmentOptions()
     */

    public short getJustifyLast()
    {   // for far east languages supported only for format always 0 for US
        return _justify_last.getShortValue(field_4_alignment_options);
    }

    /**
     * get the degree of rotation.  (I've not actually seen this used anywhere)
     *
     *
     * @return rotation - the degree of rotation
     * @see #getAlignmentOptions()
     */

    public short getRotation()
    {
        return _rotation.getShortValue(field_4_alignment_options);
    }

    // end alignment options bitfields

    /**
     * get the indent options bitmask  (see corresponding bit getters that reference
     * this field)
     *
     *
     * @return options bitmask
     *
     */

    public short getIndentionOptions()
    {
        return field_5_indention_options;
    }

    // bitfields for indention options

    /**
     * get indention (not sure of the units, think its spaces)
     *
     * @return indent - how far to indent the cell
     * @see #getIndentionOptions()
     */

    public short getIndent()
    {
        return _indent.getShortValue(field_5_indention_options);
    }

    /**
     * get whether to shrink the text to fit
     *
     *
     * @return shrink - shrink to fit or not
     * @see #getIndentionOptions()
     */

    public boolean getShrinkToFit()
    {
        return _shrink_to_fit.isSet(field_5_indention_options);
    }

    /**
     * get whether to merge cells
     *
     *
     * @return merge - merge cells or not
     * @see #getIndentionOptions()
     */

    public boolean getMergeCells()
    {
        return _merge_cells.isSet(field_5_indention_options);
    }

    /**
     * get the reading order for far east versions (0 - Context, 1 - Left to right,
     * 2 - right to left) - We could use some help with support for the far east.
     *
     * @return order - the reading order (0,1,2)
     * @see #getIndentionOptions()
     */

    public short getReadingOrder()
    {   // only for far east  always 0 in US
        return _reading_order.getShortValue(field_5_indention_options);
    }

    /**
     * get whether or not to use the format in this XF instead of the parent XF.
     *
     *
     * @return parent - true if this XF has a different format value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentFormat()
    {
        return _indent_not_parent_format.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the font in this XF instead of the parent XF.
     *
     *
     * @return font   - true if this XF has a different font value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentFont()
    {
        return _indent_not_parent_font.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the alignment in this XF instead of the parent XF.
     *
     *
     * @return alignment true if this XF has a different alignment value than its parent,
     *                  false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentAlignment()
    {
        return _indent_not_parent_alignment.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the border in this XF instead of the parent XF.
     *
     *
     * @return border - true if this XF has a different border value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentBorder()
    {
        return _indent_not_parent_border.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the pattern in this XF instead of the parent XF.
     * (foregrount/background)
     *
     * @return pattern- true if this XF has a different pattern value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentPattern()
    {
        return _indent_not_parent_pattern.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the locking/hidden in this XF instead of the parent XF.
     *
     *
     * @return options- true if this XF has a different locking or hidden value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentCellOptions()
    {
        return _indent_not_parent_cell_options
            .isSet(field_5_indention_options);
    }

    // end of bitfields for indention options
    // border options

    /**
     * get the border options bitmask (see the corresponding bit getter methods
     * that reference back to this one)
     *
     * @return options - the bit mask to set
     *
     */

    public short getBorderOptions()
    {
        return field_6_border_options;
    }

    // bitfields for border options

    /**
     * get the borderline style for the left border
     *
     *
     * @return border - type of border for the left side of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderLeft()
    {
        return _border_left.getShortValue(field_6_border_options);
    }

    /**
     * get the borderline style for the right border
     *
     *
     * @return  border - type of border for the right side of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderRight()
    {
        return _border_right.getShortValue(field_6_border_options);
    }

    /**
     * get the borderline style for the top border
     *
     *
     * @return border - type of border for the top of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderTop()
    {
        return _border_top.getShortValue(field_6_border_options);
    }

    /**
     * get the borderline style for the bottom border
     *
     *
     * @return border - type of border for the bottom of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderBottom()
    {
        return _border_bottom.getShortValue(field_6_border_options);
    }

    // record types -- palette options

    /**
     * get the palette options bitmask (see the individual bit getter methods that
     * reference this one)
     *
     *
     * @return options - the bitmask
     *
     */

    public short getPaletteOptions()
    {
        return field_7_palette_options;
    }

    // bitfields for palette options

    /**
     * get the palette index for the left border color
     *
     *
     * @return border - palette index
     * @see #getPaletteOptions()
     */

    public short getLeftBorderPaletteIdx()
    {
        return _left_border_palette_idx
            .getShortValue(field_7_palette_options);
    }

    /**
     * get the palette index for the right border color
     *
     *
     * @return border - palette index
     * @see #getPaletteOptions()
     */

    public short getRightBorderPaletteIdx()
    {
        return _right_border_palette_idx
            .getShortValue(field_7_palette_options);
    }

    // i've no idea.. possible values are 1 for down, 2 for up and 3 for both...0 for none..
    // maybe a diagnal line?

    /**
     * Not sure what this is for (maybe fill lines?) 1 = down, 2 = up, 3 = both, 0 for none..
     *
     *
     * @return diag - whatever it is that this is.
     * @see #getPaletteOptions()
     */

    public short getDiag()
    {
        return _diag.getShortValue(field_7_palette_options);
    }

    // end of style palette options
    // additional palette options

    /**
     * get the additional palette options bitmask (see individual bit getter methods
     * that reference this method)
     *
     *
     * @return options - bitmask to set
     *
     */

    public int getAdtlPaletteOptions()
    {
        return field_8_adtl_palette_options;
    }

    // bitfields for additional palette options

    /**
     * get the palette index for the top border
     *
     *
     * @return border - palette index
     * @see #getAdtlPaletteOptions()
     */

    public short getTopBorderPaletteIdx()
    {
        return ( short ) _top_border_palette_idx
            .getValue(field_8_adtl_palette_options);
    }

    /**
     * get the palette index for the bottom border
     *
     *
     * @return border - palette index
     * @see #getAdtlPaletteOptions()
     */

    public short getBottomBorderPaletteIdx()
    {
        return ( short ) _bottom_border_palette_idx
            .getValue(field_8_adtl_palette_options);
    }

    /**
     * get for diagonal borders?  No idea (its a palette color for the other function
     * we didn't know what was?)
     *
     *
     * @return diag - the palette index?
     * @see #getAdtlPaletteOptions()
     */

    public short getAdtlDiag()
    {
        return ( short ) _adtl_diag.getValue(field_8_adtl_palette_options);
    }

    /**
     * get the diagonal border line style?  Who the heck ever heard of a diagonal border?
     *
     *
     * @return diag - the line style
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getAdtlPaletteOptions()
     */

    public short getAdtlDiagLineStyle()
    {
        return ( short ) _adtl_diag_line_style
            .getValue(field_8_adtl_palette_options);
    }

    /**
     * get the additional fill pattern
     *
     * @see #NO_FILL
     * @see #SOLID_FILL
     * @see #FINE_DOTS
     * @see #ALT_BARS
     * @see #SPARSE_DOTS
     * @see #THICK_HORZ_BANDS
     * @see #THICK_VERT_BANDS
     * @see #THICK_BACKWARD_DIAG
     * @see #THICK_FORWARD_DIAG
     * @see #BIG_SPOTS
     * @see #BRICKS
     * @see #THIN_HORZ_BANDS
     * @see #THIN_VERT_BANDS
     * @see #THIN_BACKWARD_DIAG
     * @see #THIN_FORWARD_DIAG
     * @see #SQUARES
     * @see #DIAMONDS
     *
     * @return fill - fill pattern??
     * @see #getAdtlPaletteOptions()
     */

    public short getAdtlFillPattern()
    {
        return ( short ) _adtl_fill_pattern
            .getValue(field_8_adtl_palette_options);
    }

    // end bitfields for additional palette options
    // fill palette options

    /**
     * get the fill palette options bitmask (see indivdual bit getters that
     * reference this method)
     *
     * @return options
     *
     */

    public short getFillPaletteOptions()
    {
        return field_9_fill_palette_options;
    }

    // bitfields for fill palette options

    /**
     * get the foreground palette color index
     *
     *
     * @return color - palette index
     * @see #getFillPaletteOptions()
     */

    public short getFillForeground()
    {
        return _fill_foreground.getShortValue(field_9_fill_palette_options);
    }

    /**
     * get the background palette color index
     *
     * @return color palette index
     * @see #getFillPaletteOptions()
     */

    public short getFillBackground()
    {
        return _fill_background.getShortValue(field_9_fill_palette_options);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[EXTENDEDFORMAT]\n");
        if (getXFType() == XF_STYLE)
        {
            buffer.append(" STYLE_RECORD_TYPE\n");
        }
        else if (getXFType() == XF_CELL)
        {
            buffer.append(" CELL_RECORD_TYPE\n");
        }
        buffer.append("    .fontindex       = ")
            .append(Integer.toHexString(getFontIndex())).append("\n");
        buffer.append("    .formatindex     = ")
            .append(Integer.toHexString(getFormatIndex())).append("\n");
        buffer.append("    .celloptions     = ")
            .append(Integer.toHexString(getCellOptions())).append("\n");
        buffer.append("          .islocked  = ").append(isLocked())
            .append("\n");
        buffer.append("          .ishidden  = ").append(isHidden())
            .append("\n");
        buffer.append("          .recordtype= ")
            .append(Integer.toHexString(getXFType())).append("\n");
        buffer.append("          .parentidx = ")
            .append(Integer.toHexString(getParentIndex())).append("\n");
        buffer.append("    .alignmentoptions= ")
            .append(Integer.toHexString(getAlignmentOptions())).append("\n");
        buffer.append("          .alignment = ").append(getAlignment())
            .append("\n");
        buffer.append("          .wraptext  = ").append(getWrapText())
            .append("\n");
        buffer.append("          .valignment= ")
            .append(Integer.toHexString(getVerticalAlignment())).append("\n");
        buffer.append("          .justlast  = ")
            .append(Integer.toHexString(getJustifyLast())).append("\n");
        buffer.append("          .rotation  = ")
            .append(Integer.toHexString(getRotation())).append("\n");
        buffer.append("    .indentionoptions= ")
            .append(Integer.toHexString(getIndentionOptions())).append("\n");
        buffer.append("          .indent    = ")
            .append(Integer.toHexString(getIndent())).append("\n");
        buffer.append("          .shrinktoft= ").append(getShrinkToFit())
            .append("\n");
        buffer.append("          .mergecells= ").append(getMergeCells())
            .append("\n");
        buffer.append("          .readngordr= ")
            .append(Integer.toHexString(getReadingOrder())).append("\n");
        buffer.append("          .formatflag= ")
            .append(isIndentNotParentFormat()).append("\n");
        buffer.append("          .fontflag  = ")
            .append(isIndentNotParentFont()).append("\n");
        buffer.append("          .prntalgnmt= ")
            .append(isIndentNotParentAlignment()).append("\n");
        buffer.append("          .borderflag= ")
            .append(isIndentNotParentBorder()).append("\n");
        buffer.append("          .paternflag= ")
            .append(isIndentNotParentPattern()).append("\n");
        buffer.append("          .celloption= ")
            .append(isIndentNotParentCellOptions()).append("\n");
        buffer.append("    .borderoptns     = ")
            .append(Integer.toHexString(getBorderOptions())).append("\n");
        buffer.append("          .lftln     = ")
            .append(Integer.toHexString(getBorderLeft())).append("\n");
        buffer.append("          .rgtln     = ")
            .append(Integer.toHexString(getBorderRight())).append("\n");
        buffer.append("          .topln     = ")
            .append(Integer.toHexString(getBorderTop())).append("\n");
        buffer.append("          .btmln     = ")
            .append(Integer.toHexString(getBorderBottom())).append("\n");
        buffer.append("    .paleteoptns     = ")
            .append(Integer.toHexString(getPaletteOptions())).append("\n");
        buffer.append("          .leftborder= ")
            .append(Integer.toHexString(getLeftBorderPaletteIdx()))
            .append("\n");
        buffer.append("          .rghtborder= ")
            .append(Integer.toHexString(getRightBorderPaletteIdx()))
            .append("\n");
        buffer.append("          .diag      = ")
            .append(Integer.toHexString(getDiag())).append("\n");
        buffer.append("    .paleteoptn2     = ")
            .append(Integer.toHexString(getAdtlPaletteOptions()))
            .append("\n");
        buffer.append("          .topborder = ")
            .append(Integer.toHexString(getTopBorderPaletteIdx()))
            .append("\n");
        buffer.append("          .botmborder= ")
            .append(Integer.toHexString(getBottomBorderPaletteIdx()))
            .append("\n");
        buffer.append("          .adtldiag  = ")
            .append(Integer.toHexString(getAdtlDiag())).append("\n");
        buffer.append("          .diaglnstyl= ")
            .append(Integer.toHexString(getAdtlDiagLineStyle())).append("\n");
        buffer.append("          .fillpattrn= ")
            .append(Integer.toHexString(getAdtlFillPattern())).append("\n");
        buffer.append("    .fillpaloptn     = ")
            .append(Integer.toHexString(getFillPaletteOptions()))
            .append("\n");
        buffer.append("          .foreground= ")
            .append(Integer.toHexString(getFillForeground())).append("\n");
        buffer.append("          .background= ")
            .append(Integer.toHexString(getFillBackground())).append("\n");
        buffer.append("[/EXTENDEDFORMAT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getFontIndex());
        out.writeShort(getFormatIndex());
        out.writeShort(getCellOptions());
        out.writeShort(getAlignmentOptions());
        out.writeShort(getIndentionOptions());
        out.writeShort(getBorderOptions());
        out.writeShort(getPaletteOptions());
        out.writeInt(getAdtlPaletteOptions());
        out.writeShort(getFillPaletteOptions());
    }

    protected int getDataSize() {
        return 20;
    }

    public short getSid()
    {
        return sid;
    }
    

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + field_1_font_index;
		result = prime * result + field_2_format_index;
		result = prime * result + field_3_cell_options;
		result = prime * result + field_4_alignment_options;
		result = prime * result + field_5_indention_options;
		result = prime * result + field_6_border_options;
		result = prime * result + field_7_palette_options;
		result = prime * result + field_8_adtl_palette_options;
		result = prime * result + field_9_fill_palette_options;
		return result;
	}

	/**
	 * Will consider two different records with the same
	 *  contents as equals, as the various indexes
	 *  that matter are embedded in the records
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof ExtendedFormatRecord) {
			final ExtendedFormatRecord other = (ExtendedFormatRecord) obj;
			if (field_1_font_index != other.field_1_font_index)
				return false;
			if (field_2_format_index != other.field_2_format_index)
				return false;
			if (field_3_cell_options != other.field_3_cell_options)
				return false;
			if (field_4_alignment_options != other.field_4_alignment_options)
				return false;
			if (field_5_indention_options != other.field_5_indention_options)
				return false;
			if (field_6_border_options != other.field_6_border_options)
				return false;
			if (field_7_palette_options != other.field_7_palette_options)
				return false;
			if (field_8_adtl_palette_options != other.field_8_adtl_palette_options)
				return false;
			if (field_9_fill_palette_options != other.field_9_fill_palette_options)
				return false;
			return true;
		}
		return false;
	}
    
    
}
