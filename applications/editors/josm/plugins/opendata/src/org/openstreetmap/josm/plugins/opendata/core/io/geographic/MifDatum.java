// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.*;

/**
 * MapInfo Interchange File (MIF) datums, based on this specification:<ul>
 * <li><a href="https://github.com/tricycle/electrodrive-market-analysis/blob/master/specifications/Mapinfo_Mif.pdf">Mapinfo_Mif.pdf</a></li>
 * </ul>
 * This file has been stored in reference directory to avoid future dead links.
 */
public enum MifDatum {
	Adindan(1, "Ethiopia, Mali, Senegal, Sudan", Clarke_1880),
	Afgooye(2, "Somalia", Krassovsky),
	Ain_el_Abd_1970(3, "Bahrain Island", International),
	Anna_1_Astro_1965(4, "Cocos Islands", Australian_National),
	Arc_1950(5, "Botswana, Lesotho, Malawi, Swaziland, Zaire, Zambia, Zimbabwe", Clarke_1880),
	Arc_1960(6, "Kenya, Tanzania", Clarke_1880),
	Ascension_Island_1958(7, "Ascension Island", International),
	Astro_Beacon_E(8, "Iwo Jima Island", International),
	Astro_B4_Sorol_Atoll(9, "Tern Island", International),
	Astro_DOS_71_4(10, "St. Helena Island", International),
	Astronomic_Station_1952(11, "Marcus Island", International),
	Australian_Geodetic_1966_AGD_66(12, "Australia and Tasmania Island", Australian_National),
	Australian_Geodetic_1984_AGD_84(13, "Australia and Tasmania Island", Australian_National),
	Belgium(110, "Belgium", International),
	Bellevue_IGN(14, "Efate and Erromango Islands", International),
	Bermuda_1957(15, "Bermuda Islands", Clarke_1866),
	Bogota_Observatory(16, "Colombia", International),
	Campo_Inchauspe(17, "Argentina", International),
    Canton_Astro_1966(18, "Phoenix Islands", International),
    Cape(19, "South Africa", Clarke_1880),
    Cape_Canaveral(20, "Florida and Bahama Islands", Clarke_1866),
    Carthage(21, "Tunisia", Clarke_1880),
    Chatham_1971(22, "Chatham Island (New Zealand)", International),
    Chua_Astro(23, "Paraguay", International),
    Corrego_Alegre(24, "Brazil", International),
    Deutsches_Hauptdreicksnetz_DHDN(1000, "Germany", Bessel),
    Djakarta_Batavia(25, "Sumatra Island (Indonesia)", Bessel_1841),
    DOS_1968(26, "Gizo Island (New Georgia Islands)", International),
    Easter_Island_1967(27, "Easter Island", International),
	European_1950_ED_50(28, "Austria, Belgium, Denmark, Finland, France, Germany, Gibraltar, Greece, Italy, Luxembourg, Netherlands, Norway, Portugal, Spain, Sweden, Switzerland", International),
	European_1979_ED_79(29, "Austria, Finland, Netherlands, Norway, Spain, Sweden, Switzerland", International),
	European_1987_ED_87(108, "Europe", International),
	Gandajika_Base(30, "Republic of Maldives", International),
	Geodetic_Datum_1949(31, "New Zealand", International),
	Geodetic_Reference_System_1967_GRS_67(32, "Worldwide", GRS_67),
	Geodetic_Reference_System_1980_GRS_80(33, "Worldwide", GRS_80),
	Guam_1963(34, "Guam Island", Clarke_1866),
	GUX_1_Astro(35, "Guadalcanal Island", International),
	Hito_XVIII_1963(36, "South Chile (near 53°S)", International),
	Hjorsey_1955(37, "Iceland", International),
	Hong_Kong_1963(38, "Hong Kong", International),
	Hu_Tzu_Shan(39, "Taiwan", International),
	Indian_40(40, "Thailand and Vietnam", Everest),
	Indian_41(41, "Bangladesh, India, Nepal", Everest),
	Ireland_1965(42, "Ireland", Modified_Airy),
	ISTS_073_Astro_1969(43, "Diego Garcia", International),
	Johnston_Island_1961(44, "Johnston Island", International),
	Kandawala(45, "Sri Lanka", Everest),
	Kerguelen_Island(46, "Kerguelen Island", International),
	Kertau_1948(47, "West Malaysia and Singapore", Modified_Everest),
	LC_5_Astro(48, "Cayman Brac Island", Clarke_1866),
	Liberia_1964(49, "Liberia", Clarke_1880),
	Lisboa_DLx(113, "Portugal", International),
	Luzon_50(50, "Philippines (excluding Mindanao Island)", Clarke_1866),
	Luzon_51(51, "Mindanao Island", Clarke_1866),
    Mahe_1971(52, "Mahe Island", Clarke_1880),
    Marco_Astro(53, "Salvage Islands", International),
    Massawa(54, "Eritrea (Ethiopia)", Bessel_1841),
    Melrica_1973_D73(114, "Portugal", International),
    Merchich(55, "Morocco", Clarke_1880),
    Midway_Astro_1961(56, "Midway Island", International),
    Minna(57, "Nigeria", Clarke_1880),
    Nahrwan_58(58, "Masirah Island (Oman)", Clarke_1880),
    Nahrwan_59(59, "United Arab Emirates", Clarke_1880),
    Nahrwan_60(60, "Saudi Arabia", Clarke_1880),
    Naparima_BWI(61, "Trinidad and Tobago", International),
    Netherlands(109, "Netherlands", Bessel),
	North_American_1927_NAD_27_CONTINENTAL(62, "Continental US", Clarke_1866),
	North_American_1927_NAD_27_ALASKA(63, "Alaska", Clarke_1866),
	North_American_1927_NAD_27_BAHAMAS(64, "Bahamas (excluding San Salvador Island)", Clarke_1866),
	North_American_1927_NAD_27_SAN_SALVADOR(65, "San Salvador Island", Clarke_1866),
	North_American_1927_NAD_27_CANADA(66, "Canada (including Newfoundland Island)", Clarke_1866),
	North_American_1927_NAD_27_CANAL_ZONE(67, "Canal Zone", Clarke_1866),
	North_American_1927_NAD_27_CARIBBEAN(68, "Caribbean (Turks and Caicos Islands)", Clarke_1866),
	North_American_1927_NAD_27_CENTRAL_AMERICA(69, "Central America (Belize, Costa Rica, El Salvador, Guatemala, Honduras, Nicaragua)", Clarke_1866),
	North_American_1927_NAD_27_CUBA(70, "Cuba", Clarke_1866),
	North_American_1927_NAD_27_GREENLAND(71, "Greenland (Hayes Peninsula)", Clarke_1866),
	North_American_1927_NAD_27_MEXICO(72, "Mexico", Clarke_1866),
	North_American_1927_NAD_27_MICHIGAN(73, "Michigan (used only for State Plane Coordinate System 1927)", Modified_Clarke_1866),
	North_American_1983_NAD_83(74, "Alaska, Canada, Central America, Continental US, Mexico", GRS_80),
	Nouvelle_Triangulation_Francaise_NTF(107, "France", Clarke_1880),
	Nouvelle_Triangulation_Francaise_NTF_Greenwich_Prime_Meridian(1002, "France", Clarke_1880),
	NWGL_10(111, "Worldwide", WGS_72),
    Observatorio_1966(75, "Corvo and Flores Islands (Azores)", International),
    Old_Egyptian(76, "Egypt", Helmert_1906),
    Old_Hawaiian(77, "Hawaii", Clarke_1866),
    Oman(78, "Oman", Clarke_1880),
    Ordnance_Survey_of_Great_Britain_1936(79, "England, Isle of Man, Scotland, Shetland Islands, Wales", Airy),
    Pico_de_las_Nieves(80, "Canary Islands", International),
    Pitcairn_Astro_1967(81, "Pitcairn Island", International),
    Potsdam(1000, "Germany", Bessel),
    Provisional_South_Chilean_1963(36, "South Chile (near 53°S)", International),
    Provisional_South_American_1956(82, "Bolivia, Chile, Colombia, Ecuador, Guyana, Peru, Venezuela", International),
    Puerto_Rico(83, "Puerto Rico and Virgin Islands", Clarke_1866),
    Pulkovo_1942(1001, "Germany", Krassovsky),
    Qatar_National(84, "Qatar", International),
    Qornoq(85, "South Greenland", International),
    Rauenberg(1000, "Germany", Bessel),
    Reunion(86, "Mascarene Island", International),
    Rikets_Triangulering_1990_RT_90(112, "Sweden", Bessel),
    Rome_1940(87, "Sardinia Island", International),
    Santo_DOS(88, "Espirito Santo Island", International),
    Sao_Braz(89, "São Miguel, Santa Maria Islands (Azores)", International),
    Sapper_Hill_1943(90, "East Falkland Island", International),
    Schwarzeck(91, "Namibia Modified", Bessel_1841),
    South_American_1969(92, "Argentina, Bolivia, Brazil, Chile, Colombia, Ecuador, Guyana, Paraguay, Peru, Venezuela, Trinidad, and Tobago", MifEllipsoid.South_American_1969),
    South_Asia(93, "Singapore", Modified_Fischer_1960),
    Southeast_Base(94, "Porto Santo and Madeira Islands", International),
    Southwest_Base(95, "Faial, Graciosa, Pico, Sao Jorge, Terceira Islands (Azores)", International),
    Switzerland_CH_1903(1003, "Switzerland", Bessel),
    Timbalai_1948(96, "Brunei and East Malaysia (Sarawak and Sabah)", Everest),
    Tokyo(97, "Japan, Korea, Okinawa", Bessel_1841),
    Tristan_Astro_1968(98, "Tristan da Cunha", International),
    Viti_Levu_1916(99, "Viti Levu Island (Fiji Islands)", Clarke_1880),
	Wake_Eniwetok_1960(100, "", Hough),
	World_Geodetic_System_1960_WGS_60(101, "", WGS_60),
	World_Geodetic_System_1966_WGS_66(102, "", WGS_66),
	World_Geodetic_System_1972_WGS_72(103, "", WGS_72),
	World_Geodetic_System_1984_WGS_84(104, "", WGS_84),
	Yacare(105, "", International),
	Zanderij(106, "", International),
	Custom(999, null, null);
	
	private final Integer code;
    private final String area;
	private final MifEllipsoid ellipsoid;
	private MifDatum(Integer code, String area, MifEllipsoid ellipsoid) {
		this.code = code;
		this.area = area;
		this.ellipsoid = ellipsoid;
	}
	public final Integer getCode() {
		return code;
	}
    public final String getArea() {
        return area;
    }
	public final MifEllipsoid getEllipsoid() {
		return ellipsoid;
	}
	public static MifDatum forCode(Integer code) {
		for (MifDatum p : values()) {
			if (p.getCode().equals(code)) {
				return p;
			}
		}
		return null;
	}
}
