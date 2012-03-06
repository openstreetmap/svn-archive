//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifEllipsoid.*;

// TODO: finish
public enum MifDatum {
	Adindan(1, Clarke_1880),
	Afgooye(2, Krassovsky),
	Ain_el_Abd_1970(3, International),
	Anna_1_Astro_1965(4, Australian_National),
	Arc_1950(5, Clarke_1880),
	Arc_1960(6, Clarke_1880),
	Ascension_Island_1958(7, International),
	Astro_Beacon_E(8, International),
	Astro_B4_Sorol_Atoll(9, International),
	Astro_DOS_71_4(10, International),
	Astronomic_Station_1952(11, International),
	Australian_Geodetic_1966_AGD_66(12, Australian_National),
	Australian_Geodetic_1984_AGD_84(13, Australian_National),
	Belgium(110, International),
	Bellevue_IGN(14, International),
	Bermuda_1957(15, Clarke_1866),
	Bogota_Observatory(16, International),
//17 Campo_Inchauspe Argentina International),
//18 Canton_Astro_1966 Phoenix Islands International),
//19 Cape South Africa Clarke_1880),
//20 Cape Canaveral Florida and Bahama Islands Clarke_1866),
//21 Carthage Tunisia Clarke_1880),
//22 Chatham 1971 Chatham Island (New Zealand) International),
//23 Chua Astro Paraguay International),
//24 Corrego Alegre Brazil International),
//1000 Deutsches 	Hauptdreicksnetz 	(DHDN) 	Germany Bessel),
//25 Djakarta (Batavia) Sumatra Island (Indonesia) Bessel 1841),
//26 DOS 1968 Gizo Island (New Georgia Islands) International),
//27 Easter Island 1967 Easter Island International),
	European_1950_ED_50(28, International),
	European_1979_ED_79(29, International),
	European_1987_ED_87(108, International),
	Gandajika_Base(30, International),
	Geodetic_Datum_1949(31, International),
	Geodetic_Reference_System_1967_GRS_67(32, GRS_67),
	Geodetic_Reference_System_1980_GRS_80(33, GRS_80),
//34 Guam 1963 Guam Island Clarke_1866),
//35 GUX 1 Astro Guadalcanal Island International),
//36 Hito XVIII 1963 South Chile (near 53°S) International),
//37 Hjorsey 1955 Iceland International),
//38 Hong Kong 1963 Hong Kong International),
//39 Hu–Tzu–Shan Taiwan International),
//40 Indian Thailand and Vietnam Everest),
//41 Indian Bangladesh, India, Nepal Everest),
//42 Ireland 1965 Ireland Modified Airy),
//43 ISTS 073 Astro 1969 Diego Garcia International),
//44 Johnston Island 1961 Johnston Island International),
//45 Kandawala Sri Lanka Everest),
//46 Kerguelen Island Kerguelen Island International),
//47 Kertau 1948 West Malaysia and Singapore Modified Everest),
//48 L.C. 5 Astro Cayman Brac Island Clarke_1866),
//49 Liberia 1964 Liberia Clarke_1880),
//113 Lisboa (DLx) Portugal International),
//50 Luzon Philippines (excluding Mindanao Island) Clarke_1866),
//51 Luzon Mindanao Island Clarke_1866),
//52 Mahe 1971 Mahe Island Clarke_1880),
//53 Marco Astro Salvage Islands International),
//54 Massawa Eritrea (Ethiopia) Bessel 1841),
//114 Melrica 1973 (D73) Portugal International),
//55 Merchich Morocco Clarke_1880),
//56 Midway Astro 1961 Midway Island International),
//57 Minna Nigeria Clarke_1880),
//58 Nahrwan Masirah Island (Oman) Clarke_1880),
//59 Nahrwan United Arab Emirates Clarke_1880),
//60 Nahrwan Saudi Arabia Clarke_1880),
//61 Naparima, BWI Trinidad and Tobago International),
//109 Netherlands Netherlands Bessel),
	North_American_1927_NAD_27_CONTINENTAL(62, Clarke_1866),
	North_American_1927_NAD_27_ALASKA(63, Clarke_1866),
	North_American_1927_NAD_27_BAHAMAS(64, Clarke_1866),
	North_American_1927_NAD_27_SAN_SALVADOR(65, Clarke_1866),
	North_American_1927_NAD_27_CANADA(66, Clarke_1866),
	North_American_1927_NAD_27_CANAL_ZONE(67, Clarke_1866),
	North_American_1927_NAD_27_CARIBBEAN(68, Clarke_1866),
	North_American_1927_NAD_27_CENTRAL_AMERICA(69, Clarke_1866),
	North_American_1927_NAD_27_CUBA(70, Clarke_1866),
	North_American_1927_NAD_27_GREENLAND(71, Clarke_1866),
	North_American_1927_NAD_27_MEXICO(72, Clarke_1866),
	North_American_1927_NAD_27_MICHIGAN(73, Modified_Clarke_1866),
	North_American_1983_NAD_83(74, GRS_80),
	Nouvelle_Triangulation_Francaise_NTF(107, Clarke_1880),
	Nouvelle_Triangulation_Francaise_NTF_Greenwich_Prime_Meridian(1002, Clarke_1880),
	NWGL_10(111, WGS_72),
//75 Observatorio 1966 Corvo and Flores Islands (Azores) International),
//76 Old Egyptian Egypt Helmert 1906),
//77 Old Hawaiian Hawaii Clarke_1866),
//78 Oman Oman Clarke_1880),
//79 Ordnance Survey of Great Britain 1936 England, Isle of Man, Scotland, Shetland Islands, Wales Airy),
//80 Pico de las Nieves Canary Islands International),
//81 Pitcairn Astro 1967 Pitcairn Island International),
//1000 Potsdam Germany Bessel),
//36 Provisional South Chilean 1963 South Chile (near 53°S) International),
//82 Provisional South American 1956 Bolivia, Chile, Colombia, Ecuador, Guyana, Peru, Venezuela International),
//83 Puerto Rico Puerto Rico and Virgin Islands Clarke_1866),
//1001 Pulkovo 1942 Germany Krassovsky),
//84 Qatar National Qatar International),
//85 Qornoq South Greenland International),
//1000 Rauenberg Germany Bessel),
//86 Reunion Mascarene Island International),
//112 Rikets Triangulering 1990 (RT 90) Sweden Bessel),
//87 Rome 1940 Sardinia Island International),
//88 Santo (DOS) Espirito Santo Island International),
//89 São Braz São Miguel, Santa Maria Islands (Azores) International),
//90 Sapper Hill 1943 East Falkland Island International),
//91 Schwarzeck Namibia Modified Bessel 1841),
//92 South American 1969 Argentina, Bolivia, Brazil, Chile, Colombia, Ecuador, Guyana, Paraguay, Peru, Venezuela, Trinidad, and Tobago South American 1969),
//93 South Asia Singapore Modified Fischer 1960),
//94 Southeast Base Porto Santo and Madeira Islands International),
//95 Southwest Base Faial, Graciosa, Pico, Sao Jorge, Terceira Islands (Azores) International),
//1003 Switzerland (CH 1903) Switzerland Bessel),
//96 Timbalai 1948 Brunei and East Malaysia (Sarawak and Sabah) Everest),
//97 Tokyo Japan, Korea, Okinawa Bessel 1841),
//98 Tristan Astro 1968 Tristan da Cunha International),
//99 Viti Levu 1916 Viti Levu Island (Fiji Islands) Clarke_1880),
	Wake_Eniwetok_1960(100, Hough),
	World_Geodetic_System_1960_WGS_60(101, WGS_60),
	World_Geodetic_System_1966_WGS_66(102, WGS_66),
	World_Geodetic_System_1972_WGS_72(103, WGS_72),
	World_Geodetic_System_1984_WGS_84(104, WGS_84),
	Yacare(105, International),
	Zanderij(106, International),
	Custom(999, null);
	
	private final Integer code;
	private final MifEllipsoid ellipsoid;
	private MifDatum(Integer code, MifEllipsoid ellipsoid) {
		this.code = code;
		this.ellipsoid = ellipsoid;
	}
	public final Integer getCode() {
		return code;
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
