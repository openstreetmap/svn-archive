/*
   Copyright (C) 2002 MySQL AB

      This program is free software; you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation; either version 2 of the License, or
      (at your option) any later version.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.jdbc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Mapping between MySQL charset names
 * and Java charset names.
 *
 * I've investigated placing these in a .properties file,
 * but unfortunately under most appservers this complicates
 * configuration because the security policy needs to be changed
 * by the user to allow the driver to read them :(
 *
 * @author Mark Matthews
 */
public class CharsetMapping {
    //~ Static fields/initializers ---------------------------------------------

    /**
     * Mapping of Java charset names to MySQL charset names
     */
    public static final Map JAVA_TO_MYSQL_CHARSET_MAP;
    
    /**
     * Mapping of upper-case Java charset names to MySQL charset names
     */
    public static final Map JAVA_UC_TO_MYSQL_CHARSET_MAP;
    
   
    /**
     * Mapping of MySQL charset names to Java charset names
     */
    public static final Map MYSQL_TO_JAVA_CHARSET_MAP;

    /**
     * Map/List of multibyte character sets (using MySQL names)
     */
    public static final Map MULTIBYTE_CHARSETS;

    /**
     * Map of MySQL-4.1 charset indexes to Java encoding names
     */
    public static final String[] INDEX_TO_CHARSET;

    static {
        HashMap tempMap = new HashMap();

        tempMap.put("usa7", "US-ASCII");
        tempMap.put("ascii", "US-ASCII");
        tempMap.put("big5", "Big5");
        tempMap.put("gbk", "GBK");
        tempMap.put("sjis", "SJIS");
        tempMap.put("gb2312", "EUC_CN");
        tempMap.put("ujis", "EUC_JP");
        tempMap.put("euc_kr", "EUC_KR");
        tempMap.put("latin1", "ISO8859_1");
        tempMap.put("latin1_de", "ISO8859_1");
        tempMap.put("german1", "ISO8859_1");
        tempMap.put("danish", "ISO8859_1");
        tempMap.put("latin2", "ISO8859_2");
        tempMap.put("czech", "ISO8859_2");
        tempMap.put("hungarian", "ISO8859_2");
        tempMap.put("croat", "ISO8859_2");
        tempMap.put("greek", "ISO8859_7");
        tempMap.put("latin7", "ISO8859_7");
        tempMap.put("hebrew", "ISO8859_8");
        tempMap.put("latin5", "ISO8859_9");
        tempMap.put("latvian", "ISO8859_13");
        tempMap.put("latvian1", "ISO8859_13");
        tempMap.put("estonia", "ISO8859_13");
        tempMap.put("dos", "Cp437");
        tempMap.put("Cp850", "Cp850");
        tempMap.put("Cp852", "Cp852");
        tempMap.put("cp866", "Cp866");
        tempMap.put("koi8_ru", "KOI8_R");
        tempMap.put("koi8r", "KOI8_R");
        tempMap.put("tis620", "TIS620");
        tempMap.put("Cp1250", "Cp1250");
        tempMap.put("Cp1250", "Cp1250");
        tempMap.put("win1251", "Cp1251");
        tempMap.put("cp1251", "Cp1251");
        tempMap.put("cp1256", "Cp1256");
        tempMap.put("win1251ukr", "Cp1251");
        tempMap.put("cp1257", "Cp1257");
        tempMap.put("macroman", "MacRoman");
        tempMap.put("macce", "MacCentralEurope");
        tempMap.put("utf8", "UTF-8");
        tempMap.put("ucs2", "UnicodeBig");
        tempMap.put("binary", "US-ASCII"); // closest match

        MYSQL_TO_JAVA_CHARSET_MAP = Collections.unmodifiableMap(tempMap);
        
        HashMap javaToMysqlMap = new HashMap();
        
        Set keySet = MYSQL_TO_JAVA_CHARSET_MAP.keySet();

        Iterator keys = keySet.iterator();

        while (keys.hasNext()) {
            Object mysqlEncodingName = keys.next();
            Object javaEncodingName = MYSQL_TO_JAVA_CHARSET_MAP.get(mysqlEncodingName);

            //
            // Use 'closest' encodings here...as Java encoding names
            // overlap with some MySQL character sets.
            //
            if ("ISO8859_1".equals(javaEncodingName)) {
            	if ("latin1".equals(mysqlEncodingName)) {
					javaToMysqlMap.put(javaEncodingName, mysqlEncodingName);
					
            	}
            } else if ("ISO8859_2".equals(javaEncodingName)) {
				if ("latin2".equals(mysqlEncodingName)) {
					javaToMysqlMap.put(javaEncodingName, mysqlEncodingName);
				}
			} 
			else if ("ISO8859_13".equals(javaEncodingName)) {
				if ("latin7".equals(mysqlEncodingName)) {
					javaToMysqlMap.put(javaEncodingName, mysqlEncodingName);
				}
			} else {
            	javaToMysqlMap.put(javaEncodingName, mysqlEncodingName);
            }
        }

        JAVA_TO_MYSQL_CHARSET_MAP = Collections.unmodifiableMap(javaToMysqlMap);
    
        HashMap ucMap = new HashMap(JAVA_TO_MYSQL_CHARSET_MAP.size());
        
        Iterator javaNamesKeys = JAVA_TO_MYSQL_CHARSET_MAP.keySet().iterator();
        
        while (javaNamesKeys.hasNext()) {
        	String key = (String)javaNamesKeys.next();
        	
        	ucMap.put(key.toUpperCase(), JAVA_TO_MYSQL_CHARSET_MAP.get(key));
        }
        
        ucMap.put("ASCII", "ascii"); // special case
        ucMap.put("LATIN5", "latin5");
        ucMap.put("LATIN7", "latin7");
        ucMap.put("HEBREW", "hebrew");
        ucMap.put("GREEK", "greek");
        ucMap.put("EUCKR", "euckr");
        ucMap.put("GB2312", "gb2312");
        ucMap.put("LATIN2", "latin2");
        
        JAVA_UC_TO_MYSQL_CHARSET_MAP = Collections.unmodifiableMap(ucMap);


        //
        // Character sets that we can't convert
        // ourselves.
        //
        HashMap tempMapMulti = new HashMap();

        tempMapMulti.put("big5", "big5");
        tempMapMulti.put("euc_kr", "euc_kr");
        tempMapMulti.put("gb2312", "gb2312");
        tempMapMulti.put("gbk", "gbk");
        tempMapMulti.put("sjis", "sjis");
        tempMapMulti.put("ujis", "ujist");
        tempMapMulti.put("utf8", "utf8");
        tempMapMulti.put("ucs2", "UnicodeBig");

        MULTIBYTE_CHARSETS = Collections.unmodifiableMap(tempMapMulti);

        INDEX_TO_CHARSET = new String[95];

        INDEX_TO_CHARSET[1]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("big5");
        INDEX_TO_CHARSET[2]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("czech");
        INDEX_TO_CHARSET[3]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("dec8");
        INDEX_TO_CHARSET[4]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("dos");
        INDEX_TO_CHARSET[5]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("german1");
        INDEX_TO_CHARSET[6]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("hp8");
        INDEX_TO_CHARSET[7]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("koi8_ru");
        INDEX_TO_CHARSET[8]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin1");
        INDEX_TO_CHARSET[9]  = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin2");
        INDEX_TO_CHARSET[10] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("swe7");
        INDEX_TO_CHARSET[11] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("usa7");
        INDEX_TO_CHARSET[12] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("ujis");
        INDEX_TO_CHARSET[13] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("sjis");
        INDEX_TO_CHARSET[14] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1251");
        INDEX_TO_CHARSET[15] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("danish");
        INDEX_TO_CHARSET[16] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("hebrew");
        INDEX_TO_CHARSET[18] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("tis620");
        INDEX_TO_CHARSET[19] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("euc_kr");
        INDEX_TO_CHARSET[20] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("estonia");
        INDEX_TO_CHARSET[21] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("hungarian");
        INDEX_TO_CHARSET[22] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("koi8_ukr");
        INDEX_TO_CHARSET[23] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("win1251ukr");
        INDEX_TO_CHARSET[24] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("gb2312");
        INDEX_TO_CHARSET[25] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("greek");
        INDEX_TO_CHARSET[26] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("win1250");
        INDEX_TO_CHARSET[27] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("croat");
        INDEX_TO_CHARSET[28] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("gbk");
        INDEX_TO_CHARSET[29] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1257");
        INDEX_TO_CHARSET[30] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin5");
        INDEX_TO_CHARSET[31] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin1_de");
        INDEX_TO_CHARSET[32] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("armscii8");
        INDEX_TO_CHARSET[33] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("utf8");
        INDEX_TO_CHARSET[34] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("win1250ch");
        INDEX_TO_CHARSET[35] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("ucs2");
        INDEX_TO_CHARSET[36] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp866");
        INDEX_TO_CHARSET[37] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("keybcs2");
        INDEX_TO_CHARSET[38] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("macce");
        INDEX_TO_CHARSET[39] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("macroman");
        INDEX_TO_CHARSET[40] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("pclatin2");
        INDEX_TO_CHARSET[41] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latvian");
        INDEX_TO_CHARSET[42] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latvian1");
        INDEX_TO_CHARSET[43] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("maccebin");
        INDEX_TO_CHARSET[44] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("macceciai");
        INDEX_TO_CHARSET[45] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("maccecias");
        INDEX_TO_CHARSET[46] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("maccecsas");
        INDEX_TO_CHARSET[47] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin1bin");
        INDEX_TO_CHARSET[48] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin1cias");
        INDEX_TO_CHARSET[49] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin1csas");
        INDEX_TO_CHARSET[50] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1251bin");
        INDEX_TO_CHARSET[51] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1251cias");
        INDEX_TO_CHARSET[52] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1251csas");
        INDEX_TO_CHARSET[53] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("macromanbin");
        INDEX_TO_CHARSET[54] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("macromancias");
        INDEX_TO_CHARSET[55] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("macromanciai");
        INDEX_TO_CHARSET[56] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("macromancsas");
        INDEX_TO_CHARSET[57] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1256");
        INDEX_TO_CHARSET[63] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("binary");
        INDEX_TO_CHARSET[64] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("armscii");
        INDEX_TO_CHARSET[65] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("ascii");
        INDEX_TO_CHARSET[66] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1250");
        INDEX_TO_CHARSET[67] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp1256");
        INDEX_TO_CHARSET[68] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp866");
        INDEX_TO_CHARSET[69] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("dec8");
        INDEX_TO_CHARSET[70] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("greek");
        INDEX_TO_CHARSET[71] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("hebrew");
        INDEX_TO_CHARSET[72] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("hp8");
        INDEX_TO_CHARSET[73] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("keybcs2");
        INDEX_TO_CHARSET[74] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("koi8r");
        INDEX_TO_CHARSET[75] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("koi8ukr");
        INDEX_TO_CHARSET[77] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin2");
        INDEX_TO_CHARSET[78] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin5");
        INDEX_TO_CHARSET[79] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin7");
        INDEX_TO_CHARSET[80] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp850");
        INDEX_TO_CHARSET[81] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("cp852");
        INDEX_TO_CHARSET[82] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("swe7");
        INDEX_TO_CHARSET[83] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("utf8");
        INDEX_TO_CHARSET[84] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("big5");
        INDEX_TO_CHARSET[85] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("euckr");
        INDEX_TO_CHARSET[86] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("gb2312");
        INDEX_TO_CHARSET[87] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("gbk");
        INDEX_TO_CHARSET[88] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("sjis");
        INDEX_TO_CHARSET[89] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("tis620");
        INDEX_TO_CHARSET[90] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("ucs2");
        INDEX_TO_CHARSET[91] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("ujis");
        INDEX_TO_CHARSET[92] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("geostd8");
        INDEX_TO_CHARSET[93] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("geostd8");
        INDEX_TO_CHARSET[94] = (String) MYSQL_TO_JAVA_CHARSET_MAP.get("latin1");
    }
}
