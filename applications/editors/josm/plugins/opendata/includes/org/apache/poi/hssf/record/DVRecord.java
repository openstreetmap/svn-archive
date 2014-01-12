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

import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * Title:        DATAVALIDATION Record (0x01BE)<p/>
 * Description:  This record stores data validation settings and a list of cell ranges
 *               which contain these settings. The data validation settings of a sheet
 *               are stored in a sequential list of DV records. This list is followed by
 *               DVAL record(s)
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 * @author Josh Micich
 */
public final class DVRecord extends StandardRecord {
	public final static short sid = 0x01BE;
	
	/** Option flags */
	private int _option_flags;
	/** Title of the prompt box */
	private UnicodeString _promptTitle;
	/** Title of the error box */
	private UnicodeString _errorTitle;
	/** Text of the prompt box */
	private UnicodeString _promptText;
	/** Text of the error box */
	private UnicodeString _errorText;
	/** Not used - Excel seems to always write 0x3FE0 */
	private short _not_used_1 = 0x3FE0;
	/** Formula data for first condition (RPN token array without size field) */
	private Formula _formula1;
	/** Not used - Excel seems to always write 0x0000 */
	private short _not_used_2 = 0x0000;
	/** Formula data for second condition (RPN token array without size field) */
	private Formula _formula2;
	/** Cell range address list with all affected ranges */
	private CellRangeAddressList _regions;

	/**
	 * Option flags field
	 * 
	 * @see HSSFDataValidation utility class
	 */

	public DVRecord(RecordInputStream in) { // NO_UCD

		_option_flags = in.readInt();

		_promptTitle = readUnicodeString(in);
		_errorTitle = readUnicodeString(in);
		_promptText = readUnicodeString(in);
		_errorText = readUnicodeString(in);

		int field_size_first_formula = in.readUShort();
		_not_used_1 = in.readShort();

		// "You may not use unions, intersections or array constants in Data Validation criteria"

		// read first formula data condition
		_formula1 = Formula.read(field_size_first_formula, in);

		int field_size_sec_formula = in.readUShort();
		_not_used_2 = in.readShort();

		// read sec formula data condition
		_formula2 = Formula.read(field_size_sec_formula, in);

		// read cell range address list with all affected ranges
		_regions = new CellRangeAddressList(in);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[DV]\n");
		sb.append(" options=").append(Integer.toHexString(_option_flags));
		sb.append(" title-prompt=").append(formatTextTitle(_promptTitle));
		sb.append(" title-error=").append(formatTextTitle(_errorTitle));
		sb.append(" text-prompt=").append(formatTextTitle(_promptText));
		sb.append(" text-error=").append(formatTextTitle(_errorText));
		sb.append("\n");
		appendFormula(sb, "Formula 1:",  _formula1);
		appendFormula(sb, "Formula 2:",  _formula2);
		sb.append("Regions: ");
		int nRegions = _regions.countRanges();
		for(int i=0; i<nRegions; i++) {
			if (i>0) {
				sb.append(", ");
			}
			CellRangeAddress addr = _regions.getCellRangeAddress(i);
			sb.append('(').append(addr.getFirstRow()).append(',').append(addr.getLastRow());
			sb.append(',').append(addr.getFirstColumn()).append(',').append(addr.getLastColumn()).append(')');
		}
		sb.append("\n");
		sb.append("[/DV]");

		return sb.toString();
	}

	private static String formatTextTitle(UnicodeString us) {
		String str = us.getString();
		if (str.length() == 1 && str.charAt(0) == '\0') {
			return "'\\0'";
		}
		return str;
	}

	private static void appendFormula(StringBuffer sb, String label, Formula f) {
		sb.append(label);
		
		if (f == null) {
			sb.append("<empty>\n");
			return;
		}
		Ptg[] ptgs = f.getTokens();
		sb.append('\n');
		for (int i = 0; i < ptgs.length; i++) {
			sb.append('\t').append(ptgs[i].toString()).append('\n');
		}
	}

	public void serialize(LittleEndianOutput out) {

		out.writeInt(_option_flags);
		
		serializeUnicodeString(_promptTitle, out);
		serializeUnicodeString(_errorTitle, out);
		serializeUnicodeString(_promptText, out);
		serializeUnicodeString(_errorText, out);
		out.writeShort(_formula1.getEncodedTokenSize());
		out.writeShort(_not_used_1);
		_formula1.serializeTokens(out);
		
		out.writeShort(_formula2.getEncodedTokenSize());
		out.writeShort(_not_used_2);
		_formula2.serializeTokens(out);
		
		_regions.serialize(out);
	}

	private static UnicodeString readUnicodeString(RecordInputStream in) {
		return new UnicodeString(in);
	}

	private static void serializeUnicodeString(UnicodeString us, LittleEndianOutput out) {
		StringUtil.writeUnicodeString(out, us.getString());
	}
	private static int getUnicodeStringSize(UnicodeString us) {
		String str = us.getString();
		return 3 + str.length() * (StringUtil.hasMultibyte(str) ? 2 : 1);
	}

	protected int getDataSize() {
		int size = 4+2+2+2+2;//options_field+first_formula_size+first_unused+sec_formula_size+sec+unused;
		size += getUnicodeStringSize(_promptTitle);
		size += getUnicodeStringSize(_errorTitle);
		size += getUnicodeStringSize(_promptText);
		size += getUnicodeStringSize(_errorText);
		size += _formula1.getEncodedTokenSize();
		size += _formula2.getEncodedTokenSize();
		size += _regions.getSize();
		return size;
	}

	public short getSid() {
		return sid;
	}
	
	/**
	 * Clones the object. Uses serialisation, as the
	 *  contents are somewhat complex
	 */
	public Object clone() {
		return cloneViaReserialise();
	}
}
