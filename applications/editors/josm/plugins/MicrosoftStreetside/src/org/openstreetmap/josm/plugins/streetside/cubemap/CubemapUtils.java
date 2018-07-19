// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;

public class CubemapUtils {

  final static Logger logger = Logger.getLogger(CubemapUtils.class);

  private CubemapUtils() {
    // Private constructor to avoid instantiation
  }

	public enum CubefaceType {
		    ONE(1),
		    FOUR(4),
		    SIXTEEN(16);

		    private final int value;
		    private static Map<Integer, CubefaceType> map = new HashMap<>();

		    private CubefaceType(int value) {
		        this.value = value;
		    }

		    static {
		        for (CubefaceType cubefaceType : CubefaceType.values()) {
		            map.put(cubefaceType.value, cubefaceType);
		        }
		    }

		    public static CubefaceType valueOf(int cubefaceType) {
		        return map.get(cubefaceType);
		    }

		    public int getValue() {
		        return value;
		    }
		}

	public static enum CubemapFaces {
		FRONT("01"),
		RIGHT("02"),
		BACK("03"),
		LEFT("10"),
		UP("11"),
		DOWN("12");

		public static Stream<CubemapFaces> stream() {
			return Stream.of(CubemapFaces.values());
		}

		private final String value;

		CubemapFaces(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static Map<String[],String> directionConversion = new HashMap<>();

	// numerical base for decimal conversion (quaternary in the case of Streetside)
	private static final int NUM_BASE = 4;
	public static final String TEST_IMAGE_ID = "00000000";
	public static final int NUM_SIDES = 6;
	public static Map<String,String> rowCol2StreetsideCellAddressMap = null;

	// Intialize utility map for storing row to Streetside cell number conversions
	static {

		CubemapUtils.rowCol2StreetsideCellAddressMap = new HashMap<>();
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("00","00");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("01","01");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("02","10");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("03","11");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("10","02");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("11","03");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("12","12");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("13","13");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("20","20");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("21","21");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("22","30");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("23","31");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("30","22");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("31","23");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("32","32");
		CubemapUtils.rowCol2StreetsideCellAddressMap.put("33","33");
	}

	public static int getMaxCols() {
	  return StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()?4:2;
	}

	public static int getMaxRows() {
	  return getMaxCols();
	}

	public static String convertDecimal2Quaternary(long inputNum) {
		String res = null;
		final StringBuilder sb = new StringBuilder();

		if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(MessageFormat.format("convertDecimal2Quaternary input: {0}", Long.toString(inputNum)));
		}

		while (inputNum > 0) {
			sb.append(inputNum % CubemapUtils.NUM_BASE);
			inputNum /= CubemapUtils.NUM_BASE;
		}

		res = sb.reverse().toString();

		if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(MessageFormat.format("convertDecimal2Quaternary output: {0}", res));
		}

		return res;
	}

	public static String convertQuaternary2Decimal(String inputNum) {

	  final String res;

	  if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(MessageFormat.format("convertQuaternary2Decimal input: {0}", inputNum));
    }

	  int len = inputNum.length();
    int power = 1;
    int num = 0;
    int i;

    for (i = len - 1; i >= 0; i--)
    {
        if (Integer.parseInt(inputNum.substring(i,i+1)) >= CubemapUtils.NUM_BASE)
        {
           logger.error("Error converting quadkey " + inputNum + " to decimal.");
           return "000000000";
        }

        num += Integer.parseInt(inputNum.substring(i,i+1)) * power;
        power = power * CubemapUtils.NUM_BASE;
    }

		res = Integer.toString(num);

		if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(MessageFormat.format("convertQuaternary2Decimal output: {0}", res));
    }

		return res;
	}

	public static String getFaceNumberForCount(int count) {
		final String res;

		switch (count) {
		case 0:
			res = CubemapFaces.FRONT.getValue();
			break;
		case 1:
			res = CubemapFaces.RIGHT.getValue();
			break;
		case 2:
			res = CubemapFaces.BACK.getValue();
			break;
		case 3:
			res = CubemapFaces.LEFT.getValue();
			break;
		case 4:
			res = CubemapFaces.UP.getValue();
			break;
		case 5:
			res = CubemapFaces.DOWN.getValue();
			break;
		default:
			res = null;
			break;
		}
		return res;
	}

	public static int getTileWidth() {
		// 4-tiled cubemap imagery has a 2-pixel overlap; 16-tiled has a 1-pixel
		// overlap
		if (!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
			return 255;
		} else {
			return 254;
		}
	}

	public static int getTileHeight() {
		// 4-tiled cubemap imagery has a 2-pixel overlap; 16-tiled has a 1-pixel
		// overlap
		if(!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
			return 255;
		} else {
			return 254;
		}
	}

	public static int getCount4FaceNumber(String faceString) {

		final int tileAddress;

		switch (faceString) {
        // back
		case "03":  tileAddress = 0;
                 break;
        // down
        case "12":  tileAddress = 1;
                 break;
        // front
        case "01":  tileAddress = 2;
                 break;
        // left
        case "10":  tileAddress = 3;
                 break;
        // right
        case "02":  tileAddress = 4;
                 break;
        // up
        case "11":  tileAddress = 5;
                 break;
        default: tileAddress = 6;
                 break;
		}

		return tileAddress;
	}

	public static String getFaceIdFromTileId(String tileId) {
		// magic numbers - the face id is contained in the 16th and 17th positions
		return tileId.substring(16, 18);
	}

	public static String convertDoubleCountNrto16TileNr(String countNr) {
		String tileAddress;

		switch (countNr) {
        case "00":  tileAddress = "00";
                 break;
        case "01":  tileAddress = "01";
                 break;
        case "02":  tileAddress = "10";
                 break;
        case "03":  tileAddress = "11";
                 break;
        case "10":  tileAddress = "02";
                 break;
        case "11":  tileAddress = "03";
                 break;
        case "12":  tileAddress = "12";
                 break;
        case "13":  tileAddress = "13";
                 break;
        case "20":  tileAddress = "20";
                 break;
        case "21":  tileAddress = "21";
                 break;
        case "22":  tileAddress = "30";
                 break;
        case "23":  tileAddress = "31";
        		break;
        case "30":  tileAddress = "22";
           break;
        case "31":  tileAddress = "23";
           break;
        case "32":  tileAddress = "32";
           break;
        case "33":  tileAddress = "33";
           break;
        // shouldn't happen
        default: tileAddress = null;
                 break;
		}

		return tileAddress;
	}
}