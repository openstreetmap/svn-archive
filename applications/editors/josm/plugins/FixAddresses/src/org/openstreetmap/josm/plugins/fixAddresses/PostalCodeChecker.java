/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.HashMap;
import java.util.Locale;

import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * The Class PostcodeChecker.
 */
public class PostalCodeChecker {
	private static HashMap<String, String> postalCodePatternMap = new HashMap<String, String>();

	static {
		fillMap();
	}

	/**
	 * Checks if given address has a valid postal code.
	 *
	 * @param address the address to check the postal code for
	 * @return true, if postal code is valid (this implies
	 * also that a country is NOT supported); otherwise false.
	 */
	public static boolean hasValidPostalCode(OSMAddress address) {
		CheckParameterUtil.ensureParameterNotNull(address, "address");

		if (!address.hasPostalCode()) {
			return false; // no postal code available
		}

		String ctry = getCountry(address);
		String postalCode = address.getPostalCode();

		return hasValidPostalCode(ctry, postalCode);
	}

	/**
	 * Checks if postal code is valid for the country reported by the Java VM.
	 *
	 * @param postalCode the postal code
	 * @return true, if successful
	 */
	public static boolean hasValidPostalCode(String postalCode) {
		return hasValidPostalCode(getCountry(), postalCode);
	}

	/**
	 * Checks if given postal code if valid for the specified country.
	 *
	 * @param country the country
	 * @param postalCode the postal code
	 * @return true, if successful
	 */
	public static boolean hasValidPostalCode(String country, String postalCode) {
		// Get country-specific pattern for postal code
		if (postalCodePatternMap.containsKey(country)) {
			String pattern = postalCodePatternMap.get(country);
			// Check if postal code matches pattern
			return postalCode.matches(pattern);
		} else {
			// we cannot validate; assume postal code as valid until we know better
			return true;
		}
	}

	/**
	 * Checks if validation for the given country is supported.
	 *
	 * @param country 2-letter ISO-Code (e. g. "GB", "US", "IT") of the country to check.
	 * @return true, if is validation supported; otherwise false
	 */
	public static boolean isValidationSupported(String country) {
		CheckParameterUtil.ensureParameterNotNull(country, "country");
		return postalCodePatternMap.containsKey(country.toUpperCase());
	}

	/**
	 * Checks if validation for the given address is supported.
	 *
	 * @param address the address to check postal code validation support for.
	 * @return true, if is validation supported; otherwise false
	 */
	public static boolean isValidationSupported(OSMAddress address) {
		CheckParameterUtil.ensureParameterNotNull(address, "address");

		String ctry = getCountry(address);
		return postalCodePatternMap.containsKey(ctry);
	}

	/**
	 * Gets the current country.
	 *
	 * @return the country of the Java VM.
	 */
	public static String getCountry() {
		return getCountry(null);
	}

	/**
	 * Gets the country of the given address.
	 *
	 * @param address the address to get the country for
	 * @return the country of the address. If the given address has no postal code,
	 * the default country of the Java VM is returned instead.
	 */
	private static String getCountry(OSMAddress address) {
		String ctry = Locale.getDefault().getCountry();
		if (address != null && address.hasCountry()) {
			// If address has a country, use this one (e. g. a dutch edits UK data)
			ctry = address.getCountry().toUpperCase();
		}
		return ctry;
	}

	/**
	 * Fills the country-postal code pattern map.
	 */
	private static void fillMap() {
		/*
		String[] countries = Locale.getISOCountries();

		for (int i = 0; i < countries.length; i++) {
			System.out.println("//postalCodePatternMap.put(\"" + countries[i] + "\", \"[0-9]{5}\");");
		}

		String x = "A9999AAA";

		if (x.matches("[A-Z]{1}[0-9]{4}[A-Z]{3}")) {
			System.out.println("YES");
		}

		String xx = "99999-999";
		// "[0-9]{5}\-[0-9]{3}"); //
		if (xx.matches("[0-9]{5}-[0-9]{3}")) {
			System.out.println("YES");
		}


		String[] xxx = new String[]{"A9 9AA", "A99 9AA", "A9A 9AA", "AA9 9AA", "AA99 9AA", "AA9A 9AA"};
		for (int i = 0; i < xxx.length; i++) {
			if (!xxx[i].matches("[A-Z]{1,2}[0-9]{1,2}[A-Z]? [0-9]{1}[A-Z]{2}")) {
				System.err.println(xxx[i]);
			}
		}*/
		// see http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html for country codes
		//

		//postalCodePatternMap.put("AD", "[0-9]{5}");
		//postalCodePatternMap.put("AE", "[0-9]{5}");
		//postalCodePatternMap.put("AF", "[0-9]{5}");
		//postalCodePatternMap.put("AG", "[0-9]{5}");
		//postalCodePatternMap.put("AI", "[0-9]{5}");
		postalCodePatternMap.put("AL", "[0-9]{5}");
		postalCodePatternMap.put("AM", "[0-9]{4}");
		//postalCodePatternMap.put("AN", "[0-9]{5}");
		//postalCodePatternMap.put("AO", "[0-9]{5}");
		//postalCodePatternMap.put("AQ", "[0-9]{5}");
		postalCodePatternMap.put("AR", "[A-Z]{1}[0-9]{4}[A-Z]{3}"); // Argentina
		//postalCodePatternMap.put("AS", "[0-9]{5}");
		postalCodePatternMap.put("AT", "[0-9]{4}"); // Austria
		postalCodePatternMap.put("AU", "[0-9]{4}"); // Australia
		//postalCodePatternMap.put("AW", "[0-9]{5}");
		//postalCodePatternMap.put("AX", "[0-9]{5}");
		//postalCodePatternMap.put("AZ", "[0-9]{5}");
		//postalCodePatternMap.put("BA", "[0-9]{5}");
		//postalCodePatternMap.put("BB", "[0-9]{5}");
		//postalCodePatternMap.put("BD", "[0-9]{5}");
		//postalCodePatternMap.put("BE", "[0-9]{5}");
		//postalCodePatternMap.put("BF", "[0-9]{5}");
		//postalCodePatternMap.put("BG", "[0-9]{5}");
		//postalCodePatternMap.put("BH", "[0-9]{5}");
		//postalCodePatternMap.put("BI", "[0-9]{5}");
		//postalCodePatternMap.put("BJ", "[0-9]{5}");
		//postalCodePatternMap.put("BL", "[0-9]{5}");
		//postalCodePatternMap.put("BM", "[0-9]{5}");
		//postalCodePatternMap.put("BN", "[0-9]{5}");
		//postalCodePatternMap.put("BO", "[0-9]{5}");
		postalCodePatternMap.put("BR", "[0-9]{5}-[0-9]{3}"); // 99999-999
		//postalCodePatternMap.put("BS", "[0-9]{5}");
		//postalCodePatternMap.put("BT", "[0-9]{5}");
		//postalCodePatternMap.put("BV", "[0-9]{5}");
		//postalCodePatternMap.put("BW", "[0-9]{5}");
		//postalCodePatternMap.put("BY", "[0-9]{5}");
		//postalCodePatternMap.put("BZ", "[0-9]{5}");
		postalCodePatternMap.put("CA", "[A-Z][0-9][A-Z] [0-9][A-Z][0-9]"); // Canada A9A 9A9
		//postalCodePatternMap.put("CC", "[0-9]{5}");
		//postalCodePatternMap.put("CD", "[0-9]{5}");
		//postalCodePatternMap.put("CF", "[0-9]{5}");
		//postalCodePatternMap.put("CG", "[0-9]{5}");
		postalCodePatternMap.put("CH", "[0-9]{4}"); // Switzerland
		//postalCodePatternMap.put("CI", "[0-9]{5}");
		//postalCodePatternMap.put("CK", "[0-9]{5}");
		//postalCodePatternMap.put("CL", "[0-9]{5}");
		//postalCodePatternMap.put("CM", "[0-9]{5}");
		postalCodePatternMap.put("CN", "[0-9]{6}"); // China
		//postalCodePatternMap.put("CO", "[0-9]{5}");
		//postalCodePatternMap.put("CR", "[0-9]{5}");
		//postalCodePatternMap.put("CS", "[0-9]{5}");
		//postalCodePatternMap.put("CU", "[0-9]{5}");
		//postalCodePatternMap.put("CV", "[0-9]{5}");
		//postalCodePatternMap.put("CX", "[0-9]{5}");
		//postalCodePatternMap.put("CY", "[0-9]{5}");
		postalCodePatternMap.put("CZ", "[0-9]{3} [0-9]{2}"); // Czech: 999-99
		postalCodePatternMap.put("DE", "[0-9]{5}"); // Germany
		//postalCodePatternMap.put("DJ", "[0-9]{5}");
		postalCodePatternMap.put("DK", "[0-9]{4}"); // Denmark
		//postalCodePatternMap.put("DM", "[0-9]{5}");
		//postalCodePatternMap.put("DO", "[0-9]{5}");
		//postalCodePatternMap.put("DZ", "[0-9]{5}");
		//postalCodePatternMap.put("EC", "[0-9]{5}");
		postalCodePatternMap.put("EE", "[0-9]{5}"); // Estonia
		//postalCodePatternMap.put("EG", "[0-9]{5}");
		//postalCodePatternMap.put("EH", "[0-9]{5}");
		//postalCodePatternMap.put("ER", "[0-9]{5}");
		postalCodePatternMap.put("ES", "[0-9]{5}");
		//postalCodePatternMap.put("ET", "[0-9]{5}");
		postalCodePatternMap.put("FI", "[0-9]{5}");
		//postalCodePatternMap.put("FJ", "[0-9]{5}");
		//postalCodePatternMap.put("FK", "[0-9]{5}");
		//postalCodePatternMap.put("FM", "[0-9]{5}");
		//postalCodePatternMap.put("FO", "[0-9]{5}");
		postalCodePatternMap.put("FR", "[0-9]{5}"); // France
		//postalCodePatternMap.put("GA", "[0-9]{5}");
		postalCodePatternMap.put("GB", "[A-Z]{1,2}[0-9]{1,2}[A-Z]? [0-9]{1}[A-Z]{2}"); // UK
		//postalCodePatternMap.put("GD", "[0-9]{5}");
		//postalCodePatternMap.put("GE", "[0-9]{5}");
		//postalCodePatternMap.put("GF", "[0-9]{5}");
		//postalCodePatternMap.put("GG", "[0-9]{5}");
		//postalCodePatternMap.put("GH", "[0-9]{5}");
		//postalCodePatternMap.put("GI", "[0-9]{5}");
		//postalCodePatternMap.put("GL", "[0-9]{5}");
		//postalCodePatternMap.put("GM", "[0-9]{5}");
		//postalCodePatternMap.put("GN", "[0-9]{5}");
		//postalCodePatternMap.put("GP", "[0-9]{5}");
		//postalCodePatternMap.put("GQ", "[0-9]{5}");
		postalCodePatternMap.put("GR", "[0-9]{5}"); // Greece
		//postalCodePatternMap.put("GS", "[0-9]{5}");
		//postalCodePatternMap.put("GT", "[0-9]{5}");
		//postalCodePatternMap.put("GU", "[0-9]{5}");
		//postalCodePatternMap.put("GW", "[0-9]{5}");
		//postalCodePatternMap.put("GY", "[0-9]{5}");
		//postalCodePatternMap.put("HK", "[0-9]{5}");
		//postalCodePatternMap.put("HM", "[0-9]{5}");
		//postalCodePatternMap.put("HN", "[0-9]{5}");
		postalCodePatternMap.put("HR", "[0-9]{5}"); // Croatia (Hrvatska)
		//postalCodePatternMap.put("HT", "[0-9]{5}");
		postalCodePatternMap.put("HU", "[0-9]{4}"); // Hungary
		//postalCodePatternMap.put("ID", "[0-9]{5}");
		//postalCodePatternMap.put("IE", "[0-9]{5}");
		postalCodePatternMap.put("IL", "[0-9]{5}");
		//postalCodePatternMap.put("IM", "[0-9]{5}");
		//postalCodePatternMap.put("IN", "[0-9]{5}");
		//postalCodePatternMap.put("IO", "[0-9]{5}");
		//postalCodePatternMap.put("IQ", "[0-9]{5}");
		//postalCodePatternMap.put("IR", "[0-9]{5}");
		postalCodePatternMap.put("IS", "[0-9]{3}"); // Iceland
		postalCodePatternMap.put("IT", "[0-9]{5}"); // Italy
		//postalCodePatternMap.put("JE", "[0-9]{5}");
		//postalCodePatternMap.put("JM", "[0-9]{5}");
		//postalCodePatternMap.put("JO", "[0-9]{5}");
		postalCodePatternMap.put("JP", "[0-9]{3}-[0-9]{4}"); // Japan: 999-9999
		//postalCodePatternMap.put("KE", "[0-9]{5}");
		//postalCodePatternMap.put("KG", "[0-9]{5}");
		//postalCodePatternMap.put("KH", "[0-9]{5}");
		//postalCodePatternMap.put("KI", "[0-9]{5}");
		//postalCodePatternMap.put("KM", "[0-9]{5}");
		//postalCodePatternMap.put("KN", "[0-9]{5}");
		//postalCodePatternMap.put("KP", "[0-9]{5}");
		//postalCodePatternMap.put("KR", "[0-9]{5}");
		//postalCodePatternMap.put("KW", "[0-9]{5}");
		//postalCodePatternMap.put("KY", "[0-9]{5}");
		//postalCodePatternMap.put("KZ", "[0-9]{5}");
		postalCodePatternMap.put("LA", "[0-9]{5}");
		//postalCodePatternMap.put("LB", "[0-9]{5}");
		//postalCodePatternMap.put("LC", "[0-9]{5}");
		postalCodePatternMap.put("LI", "[0-9]{4}");
		//postalCodePatternMap.put("LK", "[0-9]{5}");
		//postalCodePatternMap.put("LR", "[0-9]{5}");
		//postalCodePatternMap.put("LS", "[0-9]{5}");
		postalCodePatternMap.put("LT", "[0-9]{5}");
		postalCodePatternMap.put("LU", "[0-9]{4}");
		postalCodePatternMap.put("LV", "[0-9]{4}"); // Latvia
		//postalCodePatternMap.put("LY", "[0-9]{5}");
		//postalCodePatternMap.put("MA", "[0-9]{5}");
		//postalCodePatternMap.put("MC", "[0-9]{5}");
		//postalCodePatternMap.put("MD", "[0-9]{5}");
		postalCodePatternMap.put("ME", "[0-9]{5}"); // Montenegro
		//postalCodePatternMap.put("MF", "[0-9]{5}");
		//postalCodePatternMap.put("MG", "[0-9]{5}");
		//postalCodePatternMap.put("MH", "[0-9]{5}");
		//postalCodePatternMap.put("MK", "[0-9]{5}");
		//postalCodePatternMap.put("ML", "[0-9]{5}");
		//postalCodePatternMap.put("MM", "[0-9]{5}");
		//postalCodePatternMap.put("MN", "[0-9]{5}");
		//postalCodePatternMap.put("MO", "[0-9]{5}");
		//postalCodePatternMap.put("MP", "[0-9]{5}");
		//postalCodePatternMap.put("MQ", "[0-9]{5}");
		//postalCodePatternMap.put("MR", "[0-9]{5}");
		//postalCodePatternMap.put("MS", "[0-9]{5}");
		//postalCodePatternMap.put("MT", "[0-9]{5}");
		//postalCodePatternMap.put("MU", "[0-9]{5}");
		//postalCodePatternMap.put("MV", "[0-9]{5}");
		//postalCodePatternMap.put("MW", "[0-9]{5}");
		postalCodePatternMap.put("MX", "[0-9]{5}"); // Mexico
		//postalCodePatternMap.put("MY", "[0-9]{5}");
		//postalCodePatternMap.put("MZ", "[0-9]{5}");
		//postalCodePatternMap.put("NA", "[0-9]{5}");
		//postalCodePatternMap.put("NC", "[0-9]{5}");
		//postalCodePatternMap.put("NE", "[0-9]{5}");
		//postalCodePatternMap.put("NF", "[0-9]{5}");
		//postalCodePatternMap.put("NG", "[0-9]{5}");
		//postalCodePatternMap.put("NI", "[0-9]{5}");
		postalCodePatternMap.put("NL", "[0-9]{4} [A-Z]{2}"); // Dutch
		postalCodePatternMap.put("NO", "[0-9]{4}"); // Norway
		//postalCodePatternMap.put("NP", "[0-9]{5}");
		//postalCodePatternMap.put("NR", "[0-9]{5}");
		//postalCodePatternMap.put("NU", "[0-9]{5}");
		//postalCodePatternMap.put("NZ", "[0-9]{5}");
		//postalCodePatternMap.put("OM", "[0-9]{5}");
		//postalCodePatternMap.put("PA", "[0-9]{5}");
		//postalCodePatternMap.put("PE", "[0-9]{5}");
		//postalCodePatternMap.put("PF", "[0-9]{5}");
		//postalCodePatternMap.put("PG", "[0-9]{5}");
		//postalCodePatternMap.put("PH", "[0-9]{5}");
		//postalCodePatternMap.put("PK", "[0-9]{5}");
		postalCodePatternMap.put("PL", "[0-9]{2}-[0-9]{3}"); // Poland
		//postalCodePatternMap.put("PM", "[0-9]{5}");
		//postalCodePatternMap.put("PN", "[0-9]{5}");
		//postalCodePatternMap.put("PR", "[0-9]{5}");
		//postalCodePatternMap.put("PS", "[0-9]{5}");
		postalCodePatternMap.put("PT", "[0-9]{4}-[0-9]{3}"); // Portugal
		//postalCodePatternMap.put("PW", "[0-9]{5}");
		//postalCodePatternMap.put("PY", "[0-9]{5}");
		//postalCodePatternMap.put("QA", "[0-9]{5}");
		//postalCodePatternMap.put("RE", "[0-9]{5}");
		postalCodePatternMap.put("RO", "[0-9]{6}"); // Romania
		//postalCodePatternMap.put("RS", "[0-9]{5}");
		postalCodePatternMap.put("RU", "[0-9]{6}"); // Russia
		//postalCodePatternMap.put("RW", "[0-9]{5}");
		//postalCodePatternMap.put("SA", "[0-9]{5}");
		//postalCodePatternMap.put("SB", "[0-9]{5}");
		//postalCodePatternMap.put("SC", "[0-9]{5}");
		//postalCodePatternMap.put("SD", "[0-9]{5}");
		postalCodePatternMap.put("SE", "[0-9]{3} [0-9]{2}"); // Sweden: 999-99
		//postalCodePatternMap.put("SG", "[0-9]{5}");
		//postalCodePatternMap.put("SH", "[0-9]{5}");
		postalCodePatternMap.put("SI", "[0-9]{4}");
		//postalCodePatternMap.put("SJ", "[0-9]{5}");
		postalCodePatternMap.put("SK", "[0-9]{3} [0-9]{2}"); // Slovakia: 999-99
		postalCodePatternMap.put("SL", "[0-9]{4}"); // Slowenia
		postalCodePatternMap.put("SM", "[0-9]{5}"); // san marino -> Italy
		//postalCodePatternMap.put("SN", "[0-9]{5}");
		//postalCodePatternMap.put("SO", "[0-9]{5}");
		//postalCodePatternMap.put("SR", "[0-9]{5}");
		//postalCodePatternMap.put("ST", "[0-9]{5}");
		//postalCodePatternMap.put("SV", "[0-9]{5}");
		//postalCodePatternMap.put("SY", "[0-9]{5}");
		//postalCodePatternMap.put("SZ", "[0-9]{5}");
		//postalCodePatternMap.put("TC", "[0-9]{5}");
		//postalCodePatternMap.put("TD", "[0-9]{5}");
		//postalCodePatternMap.put("TF", "[0-9]{5}");
		//postalCodePatternMap.put("TG", "[0-9]{5}");
		//postalCodePatternMap.put("TH", "[0-9]{5}");
		//postalCodePatternMap.put("TJ", "[0-9]{5}");
		//postalCodePatternMap.put("TK", "[0-9]{5}");
		//postalCodePatternMap.put("TL", "[0-9]{5}");
		//postalCodePatternMap.put("TM", "[0-9]{5}");
		//postalCodePatternMap.put("TN", "[0-9]{5}");
		//postalCodePatternMap.put("TO", "[0-9]{5}");
		postalCodePatternMap.put("TR", "[0-9]{5}"); // turkye
		//postalCodePatternMap.put("TT", "[0-9]{5}");
		//postalCodePatternMap.put("TV", "[0-9]{5}");
		//postalCodePatternMap.put("TW", "[0-9]{5}");
		//postalCodePatternMap.put("TZ", "[0-9]{5}");
		postalCodePatternMap.put("UA", "[0-9]{5}"); // Ukraine
		//postalCodePatternMap.put("UG", "[0-9]{5}");
		//postalCodePatternMap.put("UM", "[0-9]{5}");
		postalCodePatternMap.put("US", "([A-Z]{2} )?[0-9]{5}"); // USA: support "99999" and "IL 99999"
		//postalCodePatternMap.put("UY", "[0-9]{5}");
		//postalCodePatternMap.put("UZ", "[0-9]{5}");
		//postalCodePatternMap.put("VA", "[0-9]{5}");
		//postalCodePatternMap.put("VC", "[0-9]{5}");
		//postalCodePatternMap.put("VE", "[0-9]{5}");
		//postalCodePatternMap.put("VG", "[0-9]{5}");
		//postalCodePatternMap.put("VI", "[0-9]{5}");
		//postalCodePatternMap.put("VN", "[0-9]{5}");
		//postalCodePatternMap.put("VU", "[0-9]{5}");
		//postalCodePatternMap.put("WF", "[0-9]{5}");
		//postalCodePatternMap.put("WS", "[0-9]{5}");
		//postalCodePatternMap.put("YE", "[0-9]{5}");
		//postalCodePatternMap.put("YT", "[0-9]{5}");
		//postalCodePatternMap.put("ZA", "[0-9]{5}");
		//postalCodePatternMap.put("ZM", "[0-9]{5}");
		//postalCodePatternMap.put("ZW", "[0-9]{5}");
	}
}
