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
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The common object data record is used to store all common preferences for an excel object.<p/>
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class CommonObjectDataSubRecord extends SubRecord {
    public final static short sid = 0x0015;

    private static final BitField locked    = BitFieldFactory.getInstance(0x0001);
    private static final BitField printable = BitFieldFactory.getInstance(0x0010);
    private static final BitField autofill  = BitFieldFactory.getInstance(0x2000);
    private static final BitField autoline  = BitFieldFactory.getInstance(0x4000);

    private  short      field_1_objectType;
    private  int        field_2_objectId;
    private  short      field_3_option;
    private  int        field_4_reserved1;
    private  int        field_5_reserved2;
    private  int        field_6_reserved3;


    public CommonObjectDataSubRecord()
    {

    }

    public CommonObjectDataSubRecord(LittleEndianInput in, int size) {
        if (size != 18) {
            throw new RecordFormatException("Expected size 18 but got (" + size + ")");
        }
        field_1_objectType             = in.readShort();
        field_2_objectId               = in.readUShort();
        field_3_option                 = in.readShort();
        field_4_reserved1              = in.readInt();
        field_5_reserved2              = in.readInt();
        field_6_reserved3              = in.readInt();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ftCmo]\n");
        buffer.append("    .objectType           = ")
            .append("0x").append(HexDump.toHex(  getObjectType ()))
            .append(" (").append( getObjectType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .objectId             = ")
            .append("0x").append(HexDump.toHex(  getObjectId ()))
            .append(" (").append( getObjectId() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .option               = ")
            .append("0x").append(HexDump.toHex(  getOption ()))
            .append(" (").append( getOption() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .locked                   = ").append(isLocked()).append('\n'); 
        buffer.append("         .printable                = ").append(isPrintable()).append('\n'); 
        buffer.append("         .autofill                 = ").append(isAutofill()).append('\n'); 
        buffer.append("         .autoline                 = ").append(isAutoline()).append('\n'); 
        buffer.append("    .reserved1            = ")
            .append("0x").append(HexDump.toHex(  getReserved1 ()))
            .append(" (").append( getReserved1() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .reserved2            = ")
            .append("0x").append(HexDump.toHex(  getReserved2 ()))
            .append(" (").append( getReserved2() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .reserved3            = ")
            .append("0x").append(HexDump.toHex(  getReserved3 ()))
            .append(" (").append( getReserved3() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/ftCmo]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {

        out.writeShort(sid);
        out.writeShort(getDataSize());

        out.writeShort(field_1_objectType);
        out.writeShort(field_2_objectId);
        out.writeShort(field_3_option);
        out.writeInt(field_4_reserved1);
        out.writeInt(field_5_reserved2);
        out.writeInt(field_6_reserved3);
    }

	protected int getDataSize() {
        return 2 + 2 + 2 + 4 + 4 + 4;
    }

    public short getSid() // NO_UCD
    {
        return sid;
    }

    public Object clone() {
        CommonObjectDataSubRecord rec = new CommonObjectDataSubRecord();
    
        rec.field_1_objectType = field_1_objectType;
        rec.field_2_objectId = field_2_objectId;
        rec.field_3_option = field_3_option;
        rec.field_4_reserved1 = field_4_reserved1;
        rec.field_5_reserved2 = field_5_reserved2;
        rec.field_6_reserved3 = field_6_reserved3;
        return rec;
    }


    /**
     * Get the object type field for the CommonObjectData record.
     *
     * @return  One of 
     *        OBJECT_TYPE_GROUP
     *        OBJECT_TYPE_LINE
     *        OBJECT_TYPE_RECTANGLE
     *        OBJECT_TYPE_OVAL
     *        OBJECT_TYPE_ARC
     *        OBJECT_TYPE_CHART
     *        OBJECT_TYPE_TEXT
     *        OBJECT_TYPE_BUTTON
     *        OBJECT_TYPE_PICTURE
     *        OBJECT_TYPE_POLYGON
     *        OBJECT_TYPE_RESERVED1
     *        OBJECT_TYPE_CHECKBOX
     *        OBJECT_TYPE_OPTION_BUTTON
     *        OBJECT_TYPE_EDIT_BOX
     *        OBJECT_TYPE_LABEL
     *        OBJECT_TYPE_DIALOG_BOX
     *        OBJECT_TYPE_SPINNER
     *        OBJECT_TYPE_SCROLL_BAR
     *        OBJECT_TYPE_LIST_BOX
     *        OBJECT_TYPE_GROUP_BOX
     *        OBJECT_TYPE_COMBO_BOX
     *        OBJECT_TYPE_RESERVED2
     *        OBJECT_TYPE_RESERVED3
     *        OBJECT_TYPE_RESERVED4
     *        OBJECT_TYPE_RESERVED5
     *        OBJECT_TYPE_COMMENT
     *        OBJECT_TYPE_RESERVED6
     *        OBJECT_TYPE_RESERVED7
     *        OBJECT_TYPE_RESERVED8
     *        OBJECT_TYPE_RESERVED9
     *        OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING
     */
    public short getObjectType()
    {
        return field_1_objectType;
    }

    /**
     * Get the object id field for the CommonObjectData record.
     */
    public int getObjectId()
    {
        return field_2_objectId;
    }

    /**
     * Get the option field for the CommonObjectData record.
     */
    public short getOption()
    {
        return field_3_option;
    }

    /**
     * Get the reserved1 field for the CommonObjectData record.
     */
    public int getReserved1()
    {
        return field_4_reserved1;
    }

    /**
     * Get the reserved2 field for the CommonObjectData record.
     */
    public int getReserved2()
    {
        return field_5_reserved2;
    }

    /**
     * Get the reserved3 field for the CommonObjectData record.
     */
    public int getReserved3()
    {
        return field_6_reserved3;
    }

    /**
     * true if object is locked when sheet has been protected
     * @return  the locked field value.
     */
    public boolean isLocked()
    {
        return locked.isSet(field_3_option);
    }

    /**
     * object appears when printed
     * @return  the printable field value.
     */
    public boolean isPrintable()
    {
        return printable.isSet(field_3_option);
    }

    /**
     * whether object uses an automatic fill style
     * @return  the autofill field value.
     */
    public boolean isAutofill()
    {
        return autofill.isSet(field_3_option);
    }

    /**
     * whether object uses an automatic line style
     * @return  the autoline field value.
     */
    public boolean isAutoline()
    {
        return autoline.isSet(field_3_option);
    }
}
