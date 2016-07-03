// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.AIRY;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.AUSTRALIAN_NATIONAL;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.BESSEL;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.BESSEL_1841;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.CLARKE_1866;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.CLARKE_1880;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.EVEREST;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.GRS_67;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.GRS_80;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.HELMERT_1906;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.HOUGH;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.INTERNATIONAL;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.KRASSOVSKY;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.MODIFIED_AIRY;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.MODIFIED_CLARKE_1866;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.MODIFIED_EVEREST;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.MODIFIED_FISCHER_1960;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.WGS_60;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.WGS_66;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.WGS_72;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.WGS_84;

/**
 * MapInfo Interchange File (MIF) datums, based on this specification:<ul>
 * <li><a href="https://github.com/tricycle/electrodrive-market-analysis/blob/master/specifications/Mapinfo_Mif.pdf">Mapinfo_Mif.pdf</a></li>
 * </ul>
 * This file has been stored in reference directory to avoid future dead links.
 */
public enum MifDatum {
    // CHECKSTYLE.OFF: LineLength
    ADINDAN(1, "Ethiopia, Mali, Senegal, Sudan", CLARKE_1880),
    AFGOOYE(2, "Somalia", KRASSOVSKY),
    AIN_EL_ABD_1970(3, "Bahrain Island", INTERNATIONAL),
    ANNA_1_ASTRO_1965(4, "Cocos Islands", AUSTRALIAN_NATIONAL),
    ARC_1950(5, "Botswana, Lesotho, Malawi, Swaziland, Zaire, Zambia, Zimbabwe", CLARKE_1880),
    ARC_1960(6, "Kenya, Tanzania", CLARKE_1880),
    ASCENSION_ISLAND_1958(7, "Ascension Island", INTERNATIONAL),
    ASTRO_BEACON_E(8, "Iwo Jima Island", INTERNATIONAL),
    ASTRO_B4_SOROL_ATOLL(9, "Tern Island", INTERNATIONAL),
    ASTRO_DOS_71_4(10, "St. Helena Island", INTERNATIONAL),
    ASTRONOMIC_STATION_1952(11, "Marcus Island", INTERNATIONAL),
    AUSTRALIAN_GEODETIC_1966_AGD_66(12, "Australia and Tasmania Island", AUSTRALIAN_NATIONAL),
    AUSTRALIAN_GEODETIC_1984_AGD_84(13, "Australia and Tasmania Island", AUSTRALIAN_NATIONAL),
    BELGIUM(110, "Belgium", INTERNATIONAL),
    BELLEVUE_IGN(14, "Efate and Erromango Islands", INTERNATIONAL),
    BERMUDA_1957(15, "Bermuda Islands", CLARKE_1866),
    BOGOTA_OBSERVATORY(16, "Colombia", INTERNATIONAL),
    CAMPO_INCHAUSPE(17, "Argentina", INTERNATIONAL),
    CANTON_ASTRO_1966(18, "Phoenix Islands", INTERNATIONAL),
    CAPE(19, "South Africa", CLARKE_1880),
    CAPE_CANAVERAL(20, "Florida and Bahama Islands", CLARKE_1866),
    CARTHAGE(21, "Tunisia", CLARKE_1880),
    CHATHAM_1971(22, "Chatham Island (New Zealand)", INTERNATIONAL),
    CHUA_ASTRO(23, "Paraguay", INTERNATIONAL),
    CORREGO_ALEGRE(24, "Brazil", INTERNATIONAL),
    DEUTSCHES_HAUPTDREICKSNETZ_DHDN(1000, "Germany", BESSEL),
    DJAKARTA_BATAVIA(25, "Sumatra Island (Indonesia)", BESSEL_1841),
    DOS_1968(26, "Gizo Island (New Georgia Islands)", INTERNATIONAL),
    EASTER_ISLAND_1967(27, "Easter Island", INTERNATIONAL),
    EUROPEAN_1950_ED_50(28, "Austria, Belgium, Denmark, Finland, France, Germany, Gibraltar, Greece, Italy, Luxembourg, Netherlands, Norway, Portugal, Spain, Sweden, Switzerland", INTERNATIONAL),
    EUROPEAN_1979_ED_79(29, "Austria, Finland, Netherlands, Norway, Spain, Sweden, Switzerland", INTERNATIONAL),
    EUROPEAN_1987_ED_87(108, "Europe", INTERNATIONAL),
    GANDAJIKA_BASE(30, "Republic of Maldives", INTERNATIONAL),
    GEODETIC_DATUM_1949(31, "New Zealand", INTERNATIONAL),
    GEODETIC_REFERENCE_SYSTEM_1967_GRS_67(32, "Worldwide", GRS_67),
    GEODETIC_REFERENCE_SYSTEM_1980_GRS_80(33, "Worldwide", GRS_80),
    GUAM_1963(34, "Guam Island", CLARKE_1866),
    GUX_1_ASTRO(35, "Guadalcanal Island", INTERNATIONAL),
    HITO_XVIII_1963(36, "South Chile (near 53°S)", INTERNATIONAL),
    HJORSEY_1955(37, "Iceland", INTERNATIONAL),
    HONG_KONG_1963(38, "Hong Kong", INTERNATIONAL),
    HU_TZU_SHAN(39, "Taiwan", INTERNATIONAL),
    INDIAN_40(40, "Thailand and Vietnam", EVEREST),
    INDIAN_41(41, "Bangladesh, India, Nepal", EVEREST),
    IRELAND_1965(42, "Ireland", MODIFIED_AIRY),
    ISTS_073_ASTRO_1969(43, "Diego Garcia", INTERNATIONAL),
    JOHNSTON_ISLAND_1961(44, "Johnston Island", INTERNATIONAL),
    KANDAWALA(45, "Sri Lanka", EVEREST),
    KERGUELEN_ISLAND(46, "Kerguelen Island", INTERNATIONAL),
    KERTAU_1948(47, "West Malaysia and Singapore", MODIFIED_EVEREST),
    LC_5_ASTRO(48, "Cayman Brac Island", CLARKE_1866),
    LIBERIA_1964(49, "Liberia", CLARKE_1880),
    LISBOA_DLX(113, "Portugal", INTERNATIONAL),
    LUZON_50(50, "Philippines (excluding Mindanao Island)", CLARKE_1866),
    LUZON_51(51, "Mindanao Island", CLARKE_1866),
    MAHE_1971(52, "Mahe Island", CLARKE_1880),
    MARCO_ASTRO(53, "Salvage Islands", INTERNATIONAL),
    MASSAWA(54, "Eritrea (Ethiopia)", BESSEL_1841),
    MELRICA_1973_D73(114, "Portugal", INTERNATIONAL),
    MERCHICH(55, "Morocco", CLARKE_1880),
    MIDWAY_ASTRO_1961(56, "Midway Island", INTERNATIONAL),
    MINNA(57, "Nigeria", CLARKE_1880),
    NAHRWAN_58(58, "Masirah Island (Oman)", CLARKE_1880),
    NAHRWAN_59(59, "United Arab Emirates", CLARKE_1880),
    NAHRWAN_60(60, "Saudi Arabia", CLARKE_1880),
    NAPARIMA_BWI(61, "Trinidad and Tobago", INTERNATIONAL),
    NETHERLANDS(109, "Netherlands", BESSEL),
    NORTH_AMERICAN_1927_NAD_27_CONTINENTAL(62, "Continental US", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_ALASKA(63, "Alaska", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_BAHAMAS(64, "Bahamas (excluding San Salvador Island)", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_SAN_SALVADOR(65, "San Salvador Island", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_CANADA(66, "Canada (including Newfoundland Island)", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_CANAL_ZONE(67, "Canal Zone", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_CARIBBEAN(68, "Caribbean (Turks and Caicos Islands)", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_CENTRAL_AMERICA(69, "Central America (Belize, Costa Rica, El Salvador, Guatemala, Honduras, Nicaragua)", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_CUBA(70, "Cuba", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_GREENLAND(71, "Greenland (Hayes Peninsula)", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_MEXICO(72, "Mexico", CLARKE_1866),
    NORTH_AMERICAN_1927_NAD_27_MICHIGAN(73, "Michigan (used only for State Plane Coordinate System 1927)", MODIFIED_CLARKE_1866),
    NORTH_AMERICAN_1983_NAD_83(74, "Alaska, Canada, Central America, Continental US, Mexico", GRS_80),
    NOUVELLE_TRIANGULATION_FRANCAISE_NTF(107, "France", CLARKE_1880),
    NOUVELLE_TRIANGULATION_FRANCAISE_NTF_GREENWICH_PRIME_MERIDIAN(1002, "France", CLARKE_1880),
    NWGL_10(111, "Worldwide", WGS_72),
    OBSERVATORIO_1966(75, "Corvo and Flores Islands (Azores)", INTERNATIONAL),
    OLD_EGYPTIAN(76, "Egypt", HELMERT_1906),
    OLD_HAWAIIAN(77, "Hawaii", CLARKE_1866),
    OMAN(78, "Oman", CLARKE_1880),
    ORDNANCE_SURVEY_OF_GREAT_BRITAIN_1936(79, "England, Isle of Man, Scotland, Shetland Islands, Wales", AIRY),
    PICO_DE_LAS_NIEVES(80, "Canary Islands", INTERNATIONAL),
    PITCAIRN_ASTRO_1967(81, "Pitcairn Island", INTERNATIONAL),
    POTSDAM(1000, "Germany", BESSEL),
    PROVISIONAL_SOUTH_CHILEAN_1963(36, "South Chile (near 53°S)", INTERNATIONAL),
    PROVISIONAL_SOUTH_AMERICAN_1956(82, "Bolivia, Chile, Colombia, Ecuador, Guyana, Peru, Venezuela", INTERNATIONAL),
    PUERTO_RICO(83, "Puerto Rico and Virgin Islands", CLARKE_1866),
    PULKOVO_1942(1001, "Germany", KRASSOVSKY),
    QATAR_NATIONAL(84, "Qatar", INTERNATIONAL),
    QORNOQ(85, "South Greenland", INTERNATIONAL),
    RAUENBERG(1000, "Germany", BESSEL),
    REUNION(86, "Mascarene Island", INTERNATIONAL),
    RIKETS_TRIANGULERING_1990_RT_90(112, "Sweden", BESSEL),
    ROME_1940(87, "Sardinia Island", INTERNATIONAL),
    SANTO_DOS(88, "Espirito Santo Island", INTERNATIONAL),
    SAO_BRAZ(89, "São Miguel, Santa Maria Islands (Azores)", INTERNATIONAL),
    SAPPER_HILL_1943(90, "East Falkland Island", INTERNATIONAL),
    SCHWARZECK(91, "Namibia Modified", BESSEL_1841),
    SOUTH_AMERICAN_1969(92, "Argentina, Bolivia, Brazil, Chile, Colombia, Ecuador, Guyana, Paraguay, Peru, Venezuela, Trinidad, and Tobago", MifEllipsoid.SOUTH_AMERICAN_1969),
    SOUTH_ASIA(93, "Singapore", MODIFIED_FISCHER_1960),
    SOUTHEAST_BASE(94, "Porto Santo and Madeira Islands", INTERNATIONAL),
    SOUTHWEST_BASE(95, "Faial, Graciosa, Pico, Sao Jorge, Terceira Islands (Azores)", INTERNATIONAL),
    SWITZERLAND_CH_1903(1003, "Switzerland", BESSEL),
    TIMBALAI_1948(96, "Brunei and East Malaysia (Sarawak and Sabah)", EVEREST),
    TOKYO(97, "Japan, Korea, Okinawa", BESSEL_1841),
    TRISTAN_ASTRO_1968(98, "Tristan da Cunha", INTERNATIONAL),
    VITI_LEVU_1916(99, "Viti Levu Island (Fiji Islands)", CLARKE_1880),
    WAKE_ENIWETOK_1960(100, "", HOUGH),
    WORLD_GEODETIC_SYSTEM_1960_WGS_60(101, "", WGS_60),
    WORLD_GEODETIC_SYSTEM_1966_WGS_66(102, "", WGS_66),
    WORLD_GEODETIC_System_1972_WGS_72(103, "", WGS_72),
    WORLD_GEODETIC_System_1984_WGS_84(104, "", WGS_84),
    YACARE(105, "", INTERNATIONAL),
    ZANDERIJ(106, "", INTERNATIONAL),
    CUSTOM(999, null, null);
    // CHECKSTYLE.ON: LineLength

    private final Integer code;
    private final String area;
    private final MifEllipsoid ellipsoid;

    MifDatum(Integer code, String area, MifEllipsoid ellipsoid) {
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
