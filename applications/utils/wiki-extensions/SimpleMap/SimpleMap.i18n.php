<?php
/**
 * Internationalisation file for SimpleMap extension.
 *
 ##################################################################################
 #
 # Copyright 2008 Harry Wood, Jens Frank, Grant Slater, Raymond Spekking
 #                and the authors of betawiki
 #
 # This program is free software; you can redistribute it and/or modify
 # it under the terms of the GNU General Public License as published by
 # the Free Software Foundation; either version 2 of the License, or
 # (at your option) any later version.
 #
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 # GNU General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with this program; if not, write to the Free Software
 # Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 #
 * @ingroup Extensions
 */

$messages = array();

$messages['en'] = array(
	'simplemap_desc' => "Allows the use of the <tt><nowiki>&lt;map&gt;</nowiki></tt> tag to display a static map image. Maps are from [http://openstreetmap.org openstreetmap.org]",
	'simplemap_latmissing' => "Missing lat value (for the latitude).",
	'simplemap_lonmissing' => "Missing lon value (for the longitude).",
	'simplemap_zoommissing' => "Missing z value (for the zoom level).",
	'simplemap_longdepreciated' => "Please use 'lon' instead of 'long' (parameter was renamed).",
	'simplemap_widthnan' => "width (w) value '%1' is not a valid integer",
	'simplemap_heightnan' => "height (h) value '%1' is not a valid integer",
	'simplemap_zoomnan' => "zoom (z) value '%1' is not a valid integer",
	'simplemap_latnan' => "latitude (lat) value '%1' is not a valid number",
	'simplemap_lonnan' => "longitude (lon) value '%1' is not a valid number",
	'simplemap_widthbig' => "width (w) value cannot be greater than 1000",
	'simplemap_widthsmall' => "width (w) value cannot be less than 100",
	'simplemap_heightbig' => "height (h) value cannot be greater than 1000",
	'simplemap_heightsmall' => "height (h) value cannot be less than 100",
	'simplemap_latbig' => "latitude (lat) value cannot be greater than 90",
	'simplemap_latsmall' => "latitude (lat) value cannot be less than -90",
	'simplemap_lonbig' => "longitude (lon) value cannot be greater than 180",
	'simplemap_lonsmall' => "longitude (lon) value cannot be less than -180",
	'simplemap_zoomsmall' => "zoom (z) value cannot be less than zero",
	'simplemap_zoom18' => "zoom (z) value cannot be greater than 17. Note that this MediaWiki extension hooks into the OpenStreetMap 'osmarender' layer which does not go beyond zoom level 17. The Mapnik layer available on openstreetmap.org, goes up to zoom level 18",
	'simplemap_zoombig' => "zoom (z) value cannot be greater than 17.",
	'simplemap_invalidlayer' => "Invalid 'layer' value '%1'",
	'simplemap_maperror' => "Map error:",
	'simplemap_osmlink' => 'http://www.openstreetmap.org/?lat=%1&lon=%2&zoom=%3', # do not translate or duplicate this message to other languages
	'simplemap_osmtext' => 'See this map on OpenStreetMap.org',
	'simplemap_license' => 'OpenStreetMap - CC-BY-SA-2.0', # do not translate or duplicate this message to other languages
);

/** Message documentation (Message documentation)
 * @author Purodha
 */
$messages['qqq'] = array(
	'simplemap_desc' => 'Short description of the Simple Map extension, shown in [[Special:Version]]. Do not translate or change links.',
);

/** Arabic (العربية)
 * @author Meno25
 */
$messages['ar'] = array(
	'simplemap_desc' => 'يسمح باستخدام وسم <tt><nowiki>&lt;map&gt;</nowiki></tt> لعرض خريطة static لزقة. الخرائط من [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'قيمة lat مفقودة (للارتفاع).',
	'simplemap_lonmissing' => 'قيمة lon مفقودة (لخط الطول).',
	'simplemap_zoommissing' => 'قيمة z مفقودة (لمستوى التقريب).',
	'simplemap_longdepreciated' => "من فضلك استخدم 'lon' بدلا من 'long' (المحدد تمت إعادة تسميته).",
	'simplemap_widthnan' => "قيمة العرض (w) '%1' ليست رقما صحيحا",
	'simplemap_heightnan' => "قيمة الارتفاع (h) '%1' ليست رقما صحيحا",
	'simplemap_zoomnan' => "قيمة التقريب (z) '%1' ليست رقما صحيحا",
	'simplemap_latnan' => "قيمة خط العرض (lat) '%1' ليست رقما صحيحا",
	'simplemap_lonnan' => "قيمة خط الطول (lon) '%1' ليست رقما صحيحا",
	'simplemap_widthbig' => 'قيمة العرض (w) لا يمكن أن تكون أكبر من 1000',
	'simplemap_widthsmall' => 'قيمة العرض (w) لا يمكن أن تكون أصغر من 100',
	'simplemap_heightbig' => 'قيمة الارتفاع (h) لا يمكن أن تكون أكبر من 1000',
	'simplemap_heightsmall' => 'قيمة الارتفاع (h) لا يمكن أن تكون أقل من 100',
	'simplemap_latbig' => 'قيمة دائرة العرض (lat) لا يمكن أن تكون أكبر من 90',
	'simplemap_latsmall' => 'قيمة دائرة العرض (lat) لا يمكن أن تكون أقل من -90',
	'simplemap_lonbig' => 'قيمة خط الطول (lon) لا يمكن أن تكون أكبر من 180',
	'simplemap_lonsmall' => 'قيمة خط الطول (lon) لا يمكن أن تكون أقل من -180',
	'simplemap_zoomsmall' => 'قيمة التقريب (z) لا يمكن أن تكون أقل من صفر',
	'simplemap_zoom18' => "قيمة التقريب (z) لا يمكن أن تكون أكبر من 17. لاحظ أن امتداد الميدياويكي هذا يخطف إلى طبقة OpenStreetMap 'osmarender' والتي لا تذهب أبعد من مستوى التقريب 17. طبقة Mapnik المتوفرة في openstreetmap.org، تذهب إلى مستوى تقريب 18",
	'simplemap_zoombig' => 'قيمة التقريب (z) لا يمكن أن تكون أكبر من 17.',
	'simplemap_invalidlayer' => "قيمة 'طبقة' غير صحيحة '%1'",
	'simplemap_maperror' => 'خطأ في الخريطة:',
	'simplemap_osmtext' => 'انظر هذه الخريطة في OpenStreetMap.org',
);

/** Egyptian Spoken Arabic (مصرى)
 * @author Meno25
 */
$messages['arz'] = array(
	'simplemap_desc' => 'يسمح باستخدام وسم <tt><nowiki>&lt;map&gt;</nowiki></tt> لعرض خريطة static لزقة. الخرائط من [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'قيمة lat مفقودة (للارتفاع).',
	'simplemap_lonmissing' => 'قيمة lon مفقودة (لخط الطول).',
	'simplemap_zoommissing' => 'قيمة z مفقودة (لمستوى التقريب).',
	'simplemap_longdepreciated' => "من فضلك استخدم 'lon' بدلا من 'long' (المحدد تمت إعادة تسميته).",
	'simplemap_widthnan' => "قيمة العرض (w) '%1' ليست رقما صحيحا",
	'simplemap_heightnan' => "قيمة الارتفاع (h) '%1' ليست رقما صحيحا",
	'simplemap_zoomnan' => "قيمة التقريب (z) '%1' ليست رقما صحيحا",
	'simplemap_latnan' => "قيمة خط العرض (lat) '%1' ليست رقما صحيحا",
	'simplemap_lonnan' => "قيمة خط الطول (lon) '%1' ليست رقما صحيحا",
	'simplemap_widthbig' => 'قيمة العرض (w) لا يمكن أن تكون أكبر من 1000',
	'simplemap_widthsmall' => 'قيمة العرض (w) لا يمكن أن تكون أصغر من 100',
	'simplemap_heightbig' => 'قيمة الارتفاع (h) لا يمكن أن تكون أكبر من 1000',
	'simplemap_heightsmall' => 'قيمة الارتفاع (h) لا يمكن أن تكون أقل من 100',
	'simplemap_latbig' => 'قيمة دائرة العرض (lat) لا يمكن أن تكون أكبر من 90',
	'simplemap_latsmall' => 'قيمة دائرة العرض (lat) لا يمكن أن تكون أقل من -90',
	'simplemap_lonbig' => 'قيمة خط الطول (lon) لا يمكن أن تكون أكبر من 180',
	'simplemap_lonsmall' => 'قيمة خط الطول (lon) لا يمكن أن تكون أقل من -180',
	'simplemap_zoomsmall' => 'قيمة التقريب (z) لا يمكن أن تكون أقل من صفر',
	'simplemap_zoom18' => "قيمة التقريب (z) لا يمكن أن تكون أكبر من 17. لاحظ أن امتداد الميدياويكى هذا يخطف إلى طبقة OpenStreetMap 'osmarender' والتى لا تذهب أبعد من مستوى التقريب 17. طبقة Mapnik المتوفرة فى openstreetmap.org، تذهب إلى مستوى تقريب 18",
	'simplemap_zoombig' => 'قيمة التقريب (z) لا يمكن أن تكون أكبر من 17.',
	'simplemap_invalidlayer' => "قيمة 'طبقة' غير صحيحة '%1'",
	'simplemap_maperror' => 'خطأ فى الخريطة:',
	'simplemap_osmtext' => 'انظر هذه الخريطة فى OpenStreetMap.org',
);

/** Belarusian (Taraškievica orthography) (Беларуская (тарашкевіца))
 * @author EugeneZelenko
 * @author Jim-by
 * @author Red Winged Duck
 */
$messages['be-tarask'] = array(
	'simplemap_desc' => 'Дазваляе карыстацца тэгам <tt><nowiki>&lt;map&gt;</nowiki></tt> для адлюстраваньня хуткай мапы static. Выкарыстоўваюцца мапы [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Няма значэньня парамэтру lat (для шыраты).',
	'simplemap_lonmissing' => 'Няма значэньня парамэтру lon (для даўгаты).',
	'simplemap_zoommissing' => 'Няма значэньня парамэтру z (для маштабу).',
	'simplemap_longdepreciated' => "Калі ласка, выкарыстоўвайце 'lon' замест 'long' (парамэтар быў перайменаваны).",
	'simplemap_widthnan' => "значэньне шырыні (w) '%1' ня ёсьць цэлы лік",
	'simplemap_heightnan' => "значэньне вышыні (h) '%1' ня ёсьць цэлы лік",
	'simplemap_zoomnan' => "значэньне маштабу (z) '%1' ня ёсьць цэлы лік",
	'simplemap_latnan' => "значэньне шыраты (lat) '%1' ня ёсьць лік",
	'simplemap_lonnan' => "значэньне даўгаты (lon) '%1' ня ёсьць лік",
	'simplemap_widthbig' => 'значэньне шырыні (w) ня можа быць больш за 1000',
	'simplemap_widthsmall' => 'значэньне шырыні (w) ня можа быць менш за 100',
	'simplemap_heightbig' => 'значэньне вышыні (h) ня можа быць больш за 1000',
	'simplemap_heightsmall' => 'значэньне вышыні (h) ня можа быць менш за 100',
	'simplemap_latbig' => 'значэньне шыраты (lat) ня можа быць больш за 90',
	'simplemap_latsmall' => 'значэньне шыраты (lat) ня можа быць менш за -90',
	'simplemap_lonbig' => 'значэньне даўгаты (lon) ня можа быць больш за 180',
	'simplemap_lonsmall' => 'значэньне даўгаты (lon) ня можа быць менш за -180',
	'simplemap_zoomsmall' => 'значэньне маштабу (z) ня можа быць менш за нуль',
	'simplemap_zoom18' => "значэньне маштабу (z) ня можа быць больш за 17. Заўважце, што  гэта пашырэньне MediaWiki выкарыстоўвае слой OpenStreetMap 'osmarender', які не падтрымлівае маштабы больш за 17. Слой Mapnik, які знаходзіцца на openstreetmap.org, падтрымлівае маштаб 18",
	'simplemap_zoombig' => 'значэньне маштабу (z) ня можа быць больш за 17',
	'simplemap_invalidlayer' => "Няслушнае значэньне '%1' парамэтру 'layer'",
	'simplemap_maperror' => 'Памылка мапы:',
	'simplemap_osmtext' => 'Глядзіце гэту мапу на OpenStreetMap.org',
);

/** Bulgarian (Български)
 * @author DCLXVI
 */
$messages['bg'] = array(
	'simplemap_desc' => 'Позволява използването на етикета <tt><nowiki>&lt;map&gt;</nowiki></tt> за показване на static карти. Картите са от [http://openstreetmap.org openstreetmap.org]',
	'simplemap_zoommissing' => 'Липсваща стойност z (за степен на приближаване).',
	'simplemap_zoomsmall' => 'стойността за приближаване (z) не може да бъде отрицателно число',
	'simplemap_zoombig' => 'стойността за приближаване (z) не може да бъде по-голяма от 17.',
	'simplemap_invalidlayer' => "Невалидна стойност на 'слоя' '%1'",
	'simplemap_maperror' => 'Грешка в картата:',
	'simplemap_osmtext' => 'Преглеждане на картата в OpenStreetMap.org',
);

/** Czech (Česky)
 * @author Danny B.
 * @author Mormegil
 */
$messages['cs'] = array(
	'simplemap_desc' => 'Umožňuje použití tagu <code><nowiki>&lt;map&gt;</nowiki></code> pro zobrazení posuvné mapy static. Mapy pocházejí z [http://openstreetmap.org openstreetmap.org].',
	'simplemap_latmissing' => 'Chybí hodnota lat (zeměpisná šířka)',
	'simplemap_lonmissing' => 'Chybí hodnota lon (zeměpisná délka)',
	'simplemap_zoommissing' => 'Chybí hodnota z (úroveň přiblížení)',
	'simplemap_longdepreciated' => 'Prosím, použijte „lon“ namísto „long“ (parametr byl přejmenován).',
	'simplemap_widthnan' => 'hodnota šířky (w) „%1“ není platné celé číslo',
	'simplemap_heightnan' => 'hodnota výšky (h) „%1“ není platné celé číslo',
	'simplemap_zoomnan' => 'hodnota úrovně přiblížení (z) „%1“ není platné celé číslo',
	'simplemap_latnan' => 'hodnota zeměpisné šířky (lat) „%1“ není platné číslo',
	'simplemap_lonnan' => 'hodnota zeměpisné délky (lon) „%1“ není platné číslo',
	'simplemap_widthbig' => 'hodnota šířky (w) nemůže být větší než 1000',
	'simplemap_widthsmall' => 'hodnota šířky (w) nemůže být menší než 100',
	'simplemap_heightbig' => 'hodnota výšky (h) nemůže být větší než 1000',
	'simplemap_heightsmall' => 'hodnota výšky (h) nemůže být menší než 100',
	'simplemap_latbig' => 'hodnota zeměpisné šířky (lat) nemůže být větší než 90',
	'simplemap_latsmall' => 'hodnota zeměpisné šířky (lat) nemůže být menší než -90',
	'simplemap_lonbig' => 'hodnota zeměpisné délky (lon) nemůže být větší než 180',
	'simplemap_lonsmall' => 'hodnota zeměpisné délky (lon) nemůže být menší než -180',
	'simplemap_zoomsmall' => 'hodnota úrovně přiblížení (z) nemůže být menší než nula',
	'simplemap_zoom18' => 'Hodnota úrovně přiblížení (z) nemůže být větší než 17. Uvědomte si, že toto rozšíření MediaWiki používá vrstvu „osmarender“ z OpenStreetMap, která neobsahuje podrobnější přiblížení než 17. Vrstva „Mapnik“ na openstreetmap.org umožňuje priblížení do úrovně 18.',
	'simplemap_zoombig' => 'Hodnota úrovně přiblížení (z) nemůže být větší než 17.',
	'simplemap_invalidlayer' => 'Neplatná hodnota „layer“ „%1“',
	'simplemap_maperror' => 'Chyba mapy:',
	'simplemap_osmtext' => 'Zobrazit tuto mapu na OpenStreetMap.org',
);

/** German (Deutsch)
 * @author Raimond Spekking
 * @author Umherirrender
 */
$messages['de'] = array(
	'simplemap_desc' => 'Ermöglicht die Nutzung des <tt><nowiki>&lt;map&gt;</nowiki></tt>-Tags zur Anzeige einer static Map. Die Karten stammen von [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Es wurde kein Wert für die geografische Breite (lat) angegeben.',
	'simplemap_lonmissing' => 'Es wurde kein Wert für die geografische Länge (lon) angegeben.',
	'simplemap_zoommissing' => 'Es wurde kein Zoom-Wert (z) angegeben.',
	'simplemap_longdepreciated' => 'Bitte benutze „lon“ an Stelle von „long“ (Parameter wurde umbenannt).',
	'simplemap_widthnan' => 'Der Wert für die Breite (w) „%1“ ist keine gültige Zahl',
	'simplemap_heightnan' => 'Der Wert für die Höhe (h) „%1“ ist keine gültige Zahl',
	'simplemap_zoomnan' => 'Der Wert für den Zoom (z) „%1“ ist keine gültige Zahl',
	'simplemap_latnan' => 'Der Wert für die geografische Breite (lat) „%1“ ist keine gültige Zahl',
	'simplemap_lonnan' => 'Der Wert für die geografische Länge (lon) „%1“ ist keine gültige Zahl',
	'simplemap_widthbig' => 'Die Breite (w) darf 1000 nicht überschreiten',
	'simplemap_widthsmall' => 'Die Breite (w) darf 100 nicht unterschreiten',
	'simplemap_heightbig' => 'Die Höhe (h) darf 1000 nicht überschreiten',
	'simplemap_heightsmall' => 'Die Höhe (h) darf 100 nicht unterschreiten',
	'simplemap_latbig' => 'Die geografische Breite darf nicht größer als 90 sein',
	'simplemap_latsmall' => 'Die geografische Breite darf nicht kleiner als -90 sein',
	'simplemap_lonbig' => 'Die geografische Länge darf nicht größer als 180 sein',
	'simplemap_lonsmall' => 'Die geografische Länge darf nicht kleiner als -180 sein',
	'simplemap_zoomsmall' => 'Der Zoomwert darf nicht negativ sein',
	'simplemap_zoom18' => 'Der Zoomwert (z) darf nicht größer als 17 sein. Beachte, dass diese MediaWiki-Erweiterung die OpenStreetMap „Osmarender“-Karte einbindet, die nicht höher als Zoom 17 geht. Die Mapnik-Karte ist auf openstreetmap.org verfügbar und geht bis Zoom 18.',
	'simplemap_zoombig' => 'Der Zoomwert (z) darf nicht größer als 17 sein.',
	'simplemap_invalidlayer' => 'Ungültiger „layer“-Wert „%1“',
	'simplemap_maperror' => 'Kartenfehler:',
	'simplemap_osmtext' => 'Diese Karte auf OpenStreetMap.org ansehen',
);

/** Lower Sorbian (Dolnoserbski)
 * @author Michawiki
 */
$messages['dsb'] = array(
	'simplemap_desc' => 'Zmóžnja wužywanje toflicki <tt><nowiki>&lt;map&gt;</nowiki></tt> za zwobraznjenje pśesuwajobneje kórty static. Kórty su z [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Gódnota za šyrinu (lat) felujo.',
	'simplemap_lonmissing' => 'Gódnota za dlininu (lon) felujo.',
	'simplemap_zoommissing' => 'Gódnota za skalěrowanje (z) felujo.',
	'simplemap_longdepreciated' => "Wužywaj pšosym 'lon' město 'long' (parameter jo se pśemjenił)",
	'simplemap_widthnan' => "Gódnota šyrokosći (w) '%1' njejo płaśiwa ceła licba",
	'simplemap_heightnan' => "Gódnota wusokosći (h) '%1' njejo płaśiwa ceła licba",
	'simplemap_zoomnan' => "Gódnota skalowanja (z) '%1' njejo płaśiwa ceła licba",
	'simplemap_latnan' => "Gódnota šyriny (lat) '%1' njejo płaśiwa licba",
	'simplemap_lonnan' => "Gódnota dlininy (lon) '%1' njejo płaśiwa licba",
	'simplemap_widthbig' => 'Gódnota šyrokosći (w) njesmějo wětša ako 1000 byś',
	'simplemap_widthsmall' => 'Gódnota šyrokosći njesmějo mjeńša ako 100 byś',
	'simplemap_heightbig' => 'Gódnota wusokosći (h) njesmějo wětša ako 1000 byś',
	'simplemap_heightsmall' => 'Gódnota wusokosći (h) njesmějo mjeńša ako 100 byś',
	'simplemap_latbig' => 'Gódnota dlininy (lat) njesmějo wětša ako 90 byś',
	'simplemap_latsmall' => 'Gódnota šyriny (lat) njesmějo mjeńša ako -90 byś',
	'simplemap_lonbig' => 'Gódnota dlininy (lon) njesmějo wětša ako 180 byś',
	'simplemap_lonsmall' => 'Gódnota dlininy (lon) njesmějo mjeńša ako -180 byś',
	'simplemap_zoomsmall' => 'Gódnota skalowanja (z) njesmějo mjeńša ako nul byś',
	'simplemap_zoom18' => "Gódnota skalowanja (z) njesmějo wětša ako 17 byś. Glědaj, až toś to rozšyrjenje MediaWiki zapśěgujo warstu OpenStreetMap 'Osmarender', kótaraž njepśesegujo skalowańsku rowninu 17. Warsta Mapnik, kótaraž stoj na openstreetmap.org k dispoziciji, dosega až k rowninje 18.",
	'simplemap_zoombig' => 'Gódnota skalowanja (z) njesmějo wětša ako 17 byś.',
	'simplemap_invalidlayer' => "Njepłaśiwa gódnota 'warsty' '%1'",
	'simplemap_maperror' => 'Kórtowa zmólka:',
	'simplemap_osmtext' => 'Glědaj toś tu kórtu na OpenStreetMap.org',
);

/** Esperanto (Esperanto)
 * @author Yekrats
 */
$messages['eo'] = array(
	'simplemap_maperror' => 'Mapa eraro:',
	'simplemap_osmtext' => 'Vidi ĉi tiun mapon en OpenStreetMap.org',
);

/** Spanish (Español)
 * @author Crazymadlover
 */
$messages['es'] = array(
	'simplemap_maperror' => 'Error en mapa:',
);



/** Finnish (Suomi)
 * @author Nike
 * @author Str4nd
 * @author Vililikku
 */
$messages['fi'] = array(
	'simplemap_desc' => 'Mahdollistaa <tt><nowiki>&lt;map&gt;</nowiki></tt>-elementin käytön static map -kartan näyttämiseen. Kartat ovat osoitteesta [http://openstreetmap.org openstreetmap.org].',
	'simplemap_latmissing' => 'Puuttuva ”lat”-arvo leveysasteille.',
	'simplemap_lonmissing' => 'Puuttuva ”lon”-arvo pituusasteille.',
	'simplemap_zoommissing' => 'Puuttuva ”z”-arvo zoomaukselle.',
	'simplemap_longdepreciated' => 'Käytä ”lon”-arvoa ”long”-arvon sijasta nimenmuutoksen vuoksi.',
	'simplemap_widthnan' => 'leveysarvo (w) ”%1” ei ole kelvollinen kokonaisluku',
	'simplemap_heightnan' => 'Korkeusarvo (h) ”%1” ei ole kelvollinen kokonaisluku',
	'simplemap_zoomnan' => 'zoom-arvo (z) ”%1” ei ole kelvollinen kokonaisluku',
	'simplemap_latnan' => 'leveysastearvo (lat) ”%1” ei ole kelvollinen luku',
	'simplemap_lonnan' => 'Pituusastearvo ”%1” ei ole kelvollinen luku',
	'simplemap_widthbig' => 'leveysarvo (w) ei voi olla yli 1000',
	'simplemap_widthsmall' => 'leveysarvo (w) ei voi olla alle 100',
	'simplemap_heightbig' => 'korkeusarvo (h) ei voi olla yli 1000',
	'simplemap_heightsmall' => 'korkeusarvo (h) ei voi olla alle 100',
	'simplemap_latbig' => 'leveysastearvo (lat) ei voi olla yli 90',
	'simplemap_latsmall' => 'leveysastearvo (lat) ei voi olla alle -90',
	'simplemap_lonbig' => 'pituusastearvo (lon) ei voi olla yli 180',
	'simplemap_lonsmall' => 'pituusastearvo (lon) ei voi olla alle -180',
	'simplemap_zoomsmall' => 'zoom-arvo (z) ei voi olla alle nollan',
	'simplemap_zoom18' => 'Zoomaus (z) -arvo ei voi olla suurempi kuin 17. Tämä MediaWiki-laajennos hakee OpenStreetMapin Osmarender-tason, jota ei voi lähentää tasoa 17 enempää. Mapnik-taso, joka myös on käytettävissä openstreetmap.org:ssa, sisältää myös 18. lähennystason.',
	'simplemap_zoombig' => 'zoom-arvo (z) ei voi olla yli 17.',
	'simplemap_invalidlayer' => 'Virheellinen ”layer”-arvo ”%1”',
	'simplemap_maperror' => 'Karttavirhe:',
	'simplemap_osmtext' => 'Katso tämä kartta osoitteessa OpenStreetMap.org.',
);

/** French (Français)
 * @author Cedric31
 * @author Grondin
 */
$messages['fr'] = array(
	'simplemap_desc' => 'Autorise l’utilisation de la balise <tt><nowiki>&lt;map&gt;</nowiki></tt> pour afficher une carte static. Les cartes proviennent de [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Valeur lat manquante (pour la latitude).',
	'simplemap_lonmissing' => 'Valeur lon manquante (pour la longitude).',
	'simplemap_zoommissing' => 'Valeur z manquante (pour le niveau du zoom).',
	'simplemap_longdepreciated' => 'Veuillez utiliser « lon » au lieu de « long » (le paramètre a été renommé).',
	'simplemap_widthnan' => 'La largeur (w) ayant pour valeur « %1 » n’est pas un nombre entier correct.',
	'simplemap_heightnan' => 'La hauteur (h) ayant pour valeur « %1 » n’est pas un nombre entier correct.',
	'simplemap_zoomnan' => 'Le zoom (z) ayant pour valeur « %1 » n’est pas un nombre entier correct.',
	'simplemap_latnan' => 'La latitude (lat) ayant pour valeur « %1 » n’est pas un nombre correct.',
	'simplemap_lonnan' => 'La longitude (lon) ayant pour valeur « %1 » n’est pas un nombre correct.',
	'simplemap_widthbig' => 'La valeur de la largeur (w) ne peut excéder 1000',
	'simplemap_widthsmall' => 'La valeur de la largeur (w) ne peut être inférieure à 100',
	'simplemap_heightbig' => 'La valeur de la hauteur (h) ne peut excéder 1000',
	'simplemap_heightsmall' => 'La valeur de la hauteur (h) ne peut être inférieure à 100',
	'simplemap_latbig' => 'La valeur de la latitude (lat) ne peut excéder 90',
	'simplemap_latsmall' => 'La valeur de la latitude (lat) ne peut être inférieure à -90',
	'simplemap_lonbig' => 'La valeur de la longitude (lon) ne peut excéder 180',
	'simplemap_lonsmall' => 'La valeur de la longitude (lon) ne peut être inférieure à -180',
	'simplemap_zoomsmall' => 'La valeur du zoom (z) ne peut être négative',
	'simplemap_zoom18' => 'La valeur du zoom (z) ne peut excéder 17. Notez que ce crochet d’extension MediaWiki dans la couche « osmarender » de OpenStreetMap ne peut aller au-delà du niveau 17 du zoop. La couche Mapnik disponible sur openstreetmap.org, ne peut aller au-delà du niveau 18.',
	'simplemap_zoombig' => 'La valeur du zoom (z) ne peut excéder 17.',
	'simplemap_invalidlayer' => 'Valeur de « %1 » de la « couche » incorrecte',
	'simplemap_maperror' => 'Erreur de carte :',
	'simplemap_osmtext' => 'Voyez cette carte sur OpenStreetMap.org',
);

/** Galician (Galego)
 * @author Toliño
 */
$messages['gl'] = array(
	'simplemap_desc' => 'Permite o uso da etiqueta <tt><nowiki>&lt;map&gt;</nowiki></tt> para amosar un mapa static. Os mapas son de [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Falta o valor lat (para a latitude).',
	'simplemap_lonmissing' => 'Falta o valor lan (para a lonxitude).',
	'simplemap_zoommissing' => 'Falta o valor z (para o nivel do zoom).',
	'simplemap_longdepreciated' => 'Por favor, use "lon" no canto de "long" (o parámetro foi renomeado).',
	'simplemap_widthnan' => "o valor '%1' do ancho (w) non é un número enteiro válido",
	'simplemap_heightnan' => "o valor '%1' da altura (h) non é un número enteiro válido",
	'simplemap_zoomnan' => "o valor '%1' do zoom (z) non é un número enteiro válido",
	'simplemap_latnan' => "o valor '%1' da latitude (lat) non é un número enteiro válido",
	'simplemap_lonnan' => "o valor '%1' da lonxitude (lon) non é un número enteiro válido",
	'simplemap_widthbig' => 'o valor do ancho (w) non pode ser máis de 1000',
	'simplemap_widthsmall' => 'o valor do ancho (w) non pode ser menos de 100',
	'simplemap_heightbig' => 'o valor da altura (h) non pode ser máis de 1000',
	'simplemap_heightsmall' => 'o valor da altura (h) non pode ser menos de 100',
	'simplemap_latbig' => 'o valor da latitude (lat) non pode ser máis de 90',
	'simplemap_latsmall' => 'o valor da latitude (lat) non pode ser menos de -90',
	'simplemap_lonbig' => 'o valor da lonxitude (lon) non pode ser máis de 180',
	'simplemap_lonsmall' => 'o valor da lonxitude (lon) non pode ser menos de -180',
	'simplemap_zoomsmall' => 'o valor do zoom (z) non pode ser menos de cero',
	'simplemap_zoom18' => 'o valor do zoom (z) non pode ser máis de 17. Déase conta de que esta extensión MediaWiki asocia no OpenStreetMap a capa "osmarender", que non vai máis alá do nivel 17 do zoom. A capa Mapnik dispoñible en openstreetmap.org, vai máis aló do nivel 18',
	'simplemap_zoombig' => 'o valor do zoom (z) non pode ser máis de 17.',
	'simplemap_invalidlayer' => 'Valor \'%1\' da "capa" inválido',
	'simplemap_maperror' => 'Erro no mapa:',
	'simplemap_osmtext' => 'Vexa este mapa en OpenStreetMap.org',
);

/** Swiss German (Alemannisch)
 * @author Als-Holder
 */
$messages['gsw'] = array(
	'simplemap_desc' => 'Macht s megli s <tt><nowiki>&lt;map&gt;</nowiki></tt>-Tag z nutze fir zum Aazeige vun ere static map. D Charte stamme vu [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'S isch kei Wärt fir di geografisch Breiti (lat) aagee wore.',
	'simplemap_lonmissing' => 'S isch kei Wärt fir di geografisch Lengi (lon) aagee wore.',
	'simplemap_zoommissing' => 'S isch kei Zoom-Wärt (z) aagee wore.',
	'simplemap_longdepreciated' => 'Bitte bruuch „lon“ statt „long“ (Parameter isch umgnännt wore).',
	'simplemap_widthnan' => 'Dr Wärt fir d Breiti (w) „%1“ isch kei giltigi Zahl',
	'simplemap_heightnan' => 'Dr Wert fir d Hechi (h) „%1“ isch kei giltigi Zahl',
	'simplemap_zoomnan' => 'Dr Wert fir dr Zoom (z) „%1“ isch kei giltigi Zahl',
	'simplemap_latnan' => 'Dr Wärt fir di geografisch Breiti (lat) „%1“ isch kei giltigi Zahl',
	'simplemap_lonnan' => 'Dr Wärt fir di geografisch Lengi (lon) „%1“ isch kei giltigi Zahl',
	'simplemap_widthbig' => 'D Breiti (w) derf nit greßer syy wie 1000',
	'simplemap_widthsmall' => 'D Breiti (w) derf nit greßer syy wie 100',
	'simplemap_heightbig' => 'D Hechi (h) derf nit greßer syy wie 1000',
	'simplemap_heightsmall' => 'D Hechi (h) derf nit greßer syy wie 100',
	'simplemap_latbig' => 'Di geografisch Breiti derf nit greßer syy wie 90',
	'simplemap_latsmall' => 'Di geografisch Breiti derf nit chleiner syy wie -90',
	'simplemap_lonbig' => 'Di geografisch Lengi derf nit greßer syy wie 180',
	'simplemap_lonsmall' => 'Di geografisch Lengi derf nit chleiner syy wie -180',
	'simplemap_zoomsmall' => 'Dr Zoomwärt derf nit negativ syy',
	'simplemap_zoom18' => 'Dr Zoomwärt (z) derf nit greßer syy wie 17. Gib acht, ass die MediaWiki-Erwyterig d OpenStreetMap „Osmarender“-Charte yybindet, wu nit hecher goht wie Zoom 17. D Mapnik-Charte isch uf openstreetmap.org verfiegbar un goht bis Zoom 18.',
	'simplemap_zoombig' => 'Dr Zoomwärt (z) derf nit greßer syy wie 17.',
	'simplemap_invalidlayer' => 'Uugiltige „layer“-Wärt „%1“',
	'simplemap_maperror' => 'Chartefähler:',
	'simplemap_osmtext' => 'Die Charte uf OpenStreetMap.org bschaue',
);

/** Hebrew (עברית)
 * @author Rotemliss
 * @author YaronSh
 */
$messages['he'] = array(
	'simplemap_desc' => 'מתן האפשרות לשימוש בתגית <tt><nowiki>&lt;map&gt;</nowiki></tt> להצגת מפת static רדומה. המפות הן מהאתר [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'ערך ה־lat חסר (עבור קו־הרוחב).',
	'simplemap_lonmissing' => 'ערך ה־lon חסר(עבור קו־האורך).',
	'simplemap_zoommissing' => 'ערך ה־z חסר (לרמת ההגדלה).',
	'simplemap_longdepreciated' => "אנא השתמשו ב־'lon' במקום ב־'long' (שם הפרמטר שונה).",
	'simplemap_widthnan' => "ערך הרוחב (w) '%1' אינו מספר שלם תקין",
	'simplemap_heightnan' => "ערך הגובה (h) '%1' אינו מספר שלם תקין",
	'simplemap_zoomnan' => "ערך ההגדלה (z) '%1' אינו מספר שלם תקין",
	'simplemap_latnan' => "ערך קו־הרוחב (lat) '%1' אינו מספר תקין",
	'simplemap_lonnan' => "ערך קו־האורך (lon) '%1' אינו מספר תקין",
	'simplemap_widthbig' => 'ערך הרוחב (w) לא יכול לחרוג מעבר ל־1000.',
	'simplemap_widthsmall' => 'ערך הרוחב (w) לא יכול לחרוג אל מתחת ל־100',
	'simplemap_heightbig' => 'ערך הגובה (h) לא יכול לחרוג אל מעבר ל־1000',
	'simplemap_heightsmall' => 'ערך הגובה (h) לא יכול לחרוג אל מתחת ל־100',
	'simplemap_latbig' => 'ערך קו־הרוחב (lat) לא יכול לחרוג מעבר ל־90',
	'simplemap_latsmall' => 'ערך קו־הרוחב (lat) לא יכול לחרוג אל מתחת ל־ -90',
	'simplemap_lonbig' => 'ערך קו־האורך (lon) לא יכול לחרוג אל מעבר ל־180',
	'simplemap_lonsmall' => 'ערך קו־האורך (lon) לא יכול לחרוג אל מתחת ל־ -180',
	'simplemap_zoomsmall' => 'ערך ההגדלה (z) לא יכול לחרוג אל מתחת לאפס',
	'simplemap_zoom18' => "ערך ההגדלה (z) לא יכול לחרוג אל מעבר ל־17. שימו לב שהרחבת מדיה־ויקי זו מתממשקת אל שכבת ה־'osmarender' של OpenStreetMap שאינה תומכת ברמת הגדלה הגדולה מ־17. שכבת ה־Mapnik הזמינה באתר openstreetmap.org, מגיעה לרמת הגדלה 18.",
	'simplemap_zoombig' => 'ערך ההגדלה (z) לא יכול לחרוג אל מעבר ל־17.',
	'simplemap_invalidlayer' => "ערך ה־'layer' אינו תקין '%1'",
	'simplemap_maperror' => 'שגיאת מפה:',
	'simplemap_osmtext' => 'עיינו במפה זו באתר OpenStreetMap.org',
);

/** Upper Sorbian (Hornjoserbsce)
 * @author Michawiki
 */
$messages['hsb'] = array(
	'simplemap_desc' => 'Zmóžnja wužiwanje taflički <tt><nowiki>&lt;map&gt;</nowiki></tt> za zwobraznjenje posuwneje karty static. Karty su z [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Hódnota za šěrinu (lat) pobrachuje',
	'simplemap_lonmissing' => 'Hódnota za geografisku dołhosć (lon) pobrachuje.',
	'simplemap_zoommissing' => 'Hódnota za skalowanje (z) pobrachuje.',
	'simplemap_longdepreciated' => "Prošu wužiwaj 'lon' město 'lon' (parameter je so přemjenował)",
	'simplemap_widthnan' => "Hódnota šěrokosće (w) '%1' njeje płaćiwa cyła ličba",
	'simplemap_heightnan' => "Hódnota wysokosće (h) '%1' njeje płaćiwa cyła ličba",
	'simplemap_zoomnan' => "Hódnota za skalowanje (z) '%1' njeje płaćiwa cyła ličba",
	'simplemap_latnan' => "Hódnota za šěrinu (lat) '%1' njeje płaćiwa ličba",
	'simplemap_lonnan' => "Hódnota za geografisku dołhosć (lon) '%1' njeje płaćiwa ličba",
	'simplemap_widthbig' => 'Hódnota šěrokosće (w) njesmě wjetša hač 1000 być',
	'simplemap_widthsmall' => 'Hódnota šěrokosće (w) njesmě mjeńša hač 100 być',
	'simplemap_heightbig' => 'Hódnota wysokosće (h) njesmě wjetša hač 1000 być',
	'simplemap_heightsmall' => 'Hódnota wysokosće (h) njesmě mjeńša hač 100 być',
	'simplemap_latbig' => 'Hódnota šěriny (lat) njesmě wjetša hač 90 być',
	'simplemap_latsmall' => 'Hódnota šěriny (lat) njesmě mjeńša hač -90 być',
	'simplemap_lonbig' => 'Hódnota geografiskeje dołhosće (lon) njesmě wjetša hač 180 być',
	'simplemap_lonsmall' => 'Hódnota geografiskeje dołhosće (lon) njesmě mjeńša hač -180 być',
	'simplemap_zoomsmall' => 'Hódnota skalowanja (z) njesmě mjeńša hač nul być',
	'simplemap_zoom18' => "Hódnota skalowanja (z) njesmě wjetša hač 17 być. Wobkedźbuj, zo tute rozšěrjenje MediaWiki wórštu OpenStreetMap 'Osmarender' zapřijima, kotraž skalowansku runinu 17 njepřesaha. Wóršta Mapnik, kotraž na openstreetmap.org k dispoziciji steji, saha hač k skalowanskej runinje 18.",
	'simplemap_zoombig' => 'Hódnota skalowanja (z) njesmě wjetša hač 17 być.',
	'simplemap_invalidlayer' => "Njepłaćiwa hódnota 'wóršty' '%1'",
	'simplemap_maperror' => 'Kartowy zmylk:',
	'simplemap_osmtext' => 'Hlej tutu kartu na OpenStreetMap.org',
);

/** Interlingua (Interlingua)
 * @author McDutchie
 */
$messages['ia'] = array(
	'simplemap_desc' => 'Permitte le uso del etiquetta <tt><nowiki>&lt;map&gt;</nowiki></tt> pro monstrar un carta static. Le cartas proveni de [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Valor lat mancante (pro le latitude).',
	'simplemap_lonmissing' => 'Valor lon mancante (pro le longitude).',
	'simplemap_zoommissing' => 'Valor z mancante (pro le nivello de zoom).',
	'simplemap_longdepreciated' => "Per favor usa 'lon' in loco de 'long' (le parametro ha essite renominate).",
	'simplemap_widthnan' => "Le valor '%1' del latitude (w) non es un numero integre valide",
	'simplemap_heightnan' => "Le valor '%1' del altitude (h) non es un numero integre valide",
	'simplemap_zoomnan' => "Le valor '%1' del zoom (z) non es un numero integre valide",
	'simplemap_latnan' => "Le valor '%1' del latitude (lat) non es un numero valide",
	'simplemap_lonnan' => "Le valor '%1' del longitude (lon) non es un numero valide",
	'simplemap_widthbig' => 'Le valor del latitude (w) non pote exceder 1000',
	'simplemap_widthsmall' => 'Le valor del latitude (w) non pote esser minus de 100',
	'simplemap_heightbig' => 'Le valor del altitude (h) non pote esser plus de 1000',
	'simplemap_heightsmall' => 'Le valor del altitude (h) non pote esser minus de 100',
	'simplemap_latbig' => 'Le valor del latitude (lat) non pote exceder 90',
	'simplemap_latsmall' => 'Le valor del latitude (lat) non pote esser minus de -90',
	'simplemap_lonbig' => 'Le valor del longitude (lon) non pote exceder 100',
	'simplemap_lonsmall' => 'Le valor del longitude (lon) non pote esser minus de -100',
	'simplemap_zoomsmall' => 'Le valor del zoom (z) non pote esser minus de zero',
	'simplemap_zoom18' => "Le valor del zoom (z) non pote exceder 17. Nota que iste extension de MediaWiki se installa in le strato 'osmarender' de OpenStreetMap, le qual non excede le nivello de zoom 17. Le strato Mapnik disponibile in openstreetmap.org ha un nivello de zoom maxime de 18.",
	'simplemap_zoombig' => 'Le valor del zoom (z) non pote exceder 17.',
	'simplemap_invalidlayer' => "Valor de 'strato' invalide '%1'",
	'simplemap_maperror' => 'Error de carta:',
	'simplemap_osmtext' => 'Vider iste carta in OpenStreetMap.org',
);

/** Italian (Italiano)
 * @author Darth Kule
 */
$messages['it'] = array(
	'simplemap_desc' => "Permette l'utilizzo del tag <tt><nowiki>&lt;map&gt;</nowiki></tt> per visualizzare una mappa static. Le mappe sono prese da [http://openstreetmap.org openstreetmap.org]",
	'simplemap_latmissing' => 'Manca il valore lat (per la latitudine).',
	'simplemap_lonmissing' => 'Manca il valore lon (per la longitudine).',
	'simplemap_zoommissing' => 'Manca il valore z (per il livello dello zoom).',
	'simplemap_longdepreciated' => "Usare 'lon' invece di 'long' (il parametro è stato rinominato).",
	'simplemap_widthnan' => "il valore '%1' della larghezza (w) non è un intero valido",
	'simplemap_heightnan' => "il valore '%1' dell'altezza (h) non è un intero valido",
	'simplemap_zoomnan' => "il valore '%1' dello zoom (z) non è un intero valido",
	'simplemap_latnan' => "il valore '%1' della latitudine (lat) non è un numero valido",
	'simplemap_lonnan' => "il valore '%1' della longitudine (long) non è un numero valido",
	'simplemap_widthbig' => 'il valore della larghezza (w) non può essere maggiore di 1000',
	'simplemap_widthsmall' => 'il valore della larghezza (w) non può essere minore di 100',
	'simplemap_heightbig' => "il valore dell'altezza (h) non può essere maggiore di 1000",
	'simplemap_heightsmall' => "il valore dell'altezza (h) non può essere minore di 100",
	'simplemap_latbig' => 'il valore della latitudine (lat) non può essere maggiore di 90',
	'simplemap_latsmall' => 'il valore della latitudine (lat) non può essere minore di -90',
	'simplemap_lonbig' => 'il valore della longitudine (lon) non può essere maggiore di 180',
	'simplemap_lonsmall' => 'il valore della longitudine (lon) non può essere minore di -180',
	'simplemap_zoomsmall' => 'il valore dello zoom (z) non può essere minore di zero',
	'simplemap_zoom18' => "il valore dello zoom (z) non può essere maggiore di 17. Nota che questa estensione MediaWiki utilizza il layer 'osmarender' di OpenStreetMap che non va oltre il livello 17 di zoom. Il layer Mapnik disponibile su openstreetmap.org arriva fino a un livello 18 di zoom",
	'simplemap_zoombig' => 'il valore dello zoom (z) non può essere maggiore di 17.',
	'simplemap_invalidlayer' => "Valore '%1' di 'layer' non valido",
	'simplemap_maperror' => 'Errore mappa:',
	'simplemap_osmtext' => 'Guarda questa mappa su OpenStreetMap.org',
);

/** Japanese (日本語)
 * @author Fryed-peach
 */
$messages['ja'] = array(
	'simplemap_desc' => 'static による滑らかな地図を表示するための <tt><nowiki>&lt;map&gt;</nowiki></tt> タグを利用できるようにする。地図は [http://openstreetmap.org openstreetmap.org] から取得される',
	'simplemap_latmissing' => '緯度値 lat が指定されていません。',
	'simplemap_lonmissing' => '経度値 lon が指定されていません。',
	'simplemap_zoommissing' => '拡大度 z が指定されていません。',
	'simplemap_longdepreciated' => '"long" ではなく "lon" を用いてください（引数が改名されました）。',
	'simplemap_widthnan' => '幅 (w) の値「%1」は有効な整数ではありません',
	'simplemap_heightnan' => '高さ (h) の値「%1」は有効な整数ではありません',
	'simplemap_zoomnan' => '拡大度 (z) の値「%1」は有効な整数ではありません',
	'simplemap_latnan' => '緯度 (lat) の値「%1」は有効な数値ではありません',
	'simplemap_lonnan' => '経度 (lon) の値「%1」は有効な数値ではありません',
	'simplemap_widthbig' => '幅 (w) の値は1000より大きくはできません',
	'simplemap_widthsmall' => '幅 (w) の値は100より小さくはできません',
	'simplemap_heightbig' => '高さ (h) の値は1000より大きくはできません',
	'simplemap_heightsmall' => '高さ (h) の値は100より小さくはできません',
	'simplemap_latbig' => '緯度 (lat) の値は90より大きくはできません',
	'simplemap_latsmall' => '緯度 (lat) の値は-90より小さくはできません',
	'simplemap_lonbig' => '経度 (lon) の値は180より大きくはできません',
	'simplemap_lonsmall' => '経度 (lon) の値は-180より小さくはできません',
	'simplemap_zoomsmall' => '拡大度 (z) の値は0より小さくはできません',
	'simplemap_zoom18' => '拡大度 (z) の値は17より大きくはできません。なお、この MediaWiki 拡張機能がフックしている、OpenStreetMap の "osmarender" レイヤーは17を超す拡大度を利用できません。openstreetmap.org で利用可能な "Mapnik" レイヤーは18までの拡大度が利用できます。',
	'simplemap_zoombig' => '拡大度 (z) の値は17より大きくはできません',
	'simplemap_invalidlayer' => '"layer" の値 "%1" は無効',
	'simplemap_maperror' => '地図エラー:',
	'simplemap_osmtext' => 'この地図を OpenStreetMap.org で見る',
);

/** Khmer (ភាសាខ្មែរ)
 * @author Thearith
 */
$messages['km'] = array(
	'simplemap_latmissing' => 'ខ្វះ​តម្លៃ​រយៈទទឹង (សម្រាប់​រយៈទទឹង)​។',
	'simplemap_lonmissing' => 'ខ្វះ​តម្លៃ​រយៈបណ្ដោយ (សម្រាប់​រយៈបណ្ដោយ)​។',
	'simplemap_zoommissing' => 'ខ្វះ​តម្លៃ Z (សម្រាប់​កម្រិត​ពង្រីក)​។',
	'simplemap_longdepreciated' => "សូម​ប្រើ 'lon' ជំនួស​ឱ្យ 'long' (ប៉ារ៉ាម៉ែត្រ​ត្រូវ​បាន​ប្ដូរឈ្មោះ)​។",
	'simplemap_widthnan' => "តម្លៃ​ទទឹង (w) '%1' មិនមែន​ជា​ចំនួនគត់​ត្រឹមត្រូវ​ទេ",
	'simplemap_heightnan' => "តម្លៃ​កម្ពស់ (h) '%1' មិនមែន​ជា​ចំនួនគត់​ត្រឹមត្រូវ​ទេ",
	'simplemap_zoomnan' => "តម្លៃ​ពង្រីក (z) '%1' មិនមែន​ជា​ចំនួនគត់​ត្រឹមត្រូវ​ទេ",
	'simplemap_latnan' => "តម្លៃ​ទទឹង (lat) '%1' មិនមែន​ជា​ចំនួន​ត្រឹមត្រូវ​ទេ",
	'simplemap_lonnan' => "តម្លៃ​បណ្ដោយ (lon) '%1' មិនមែន​ជា​ចំនួន​ត្រឹមត្រូវ​ទេ",
	'simplemap_widthbig' => 'តម្លៃ​ទទឹង (w) មិន​អាច​ធំជាង ១០០០ ទេ',
	'simplemap_widthsmall' => 'តម្លៃ​ទទឹង (w) មិន​អាច​តូចជាង ១០០ ទេ',
	'simplemap_heightbig' => 'តម្លៃ​កម្ពស់ (h) មិន​អាច​ធំជាង ១០០០ ទេ',
	'simplemap_heightsmall' => 'តម្លៃ​កម្ពស់ (h) មិន​អាច​តូចជាង ១០០ ទេ',
	'simplemap_latbig' => 'តម្លៃ​រយៈទទឹង (lat) មិន​អាច​ធំជាង ៩០ ទេ',
	'simplemap_latsmall' => 'តម្លៃ​រយៈទទឹង (lat) មិន​អាច​តូចជាង -៩០ ទេ',
	'simplemap_lonbig' => 'តម្លៃ​រយៈបណ្ដោយ (lon) មិន​អាច​ធំជាង ១៨០ ទេ',
	'simplemap_lonsmall' => 'តម្លៃ​រយៈបណ្ដោយ (lon) មិន​អាច​តូចជាង -១៨០ ទេ',
	'simplemap_zoomsmall' => 'តម្លៃ​ពង្រីក (z) មិន​អាច​តូចជាង​សូន្យ​ទេ',
	'simplemap_zoombig' => 'តម្លៃ​ពង្រីក (z) មិន​អាច​ធំជាង ១៧ ទេ​។',
	'simplemap_maperror' => 'កំហុស​ផែនទី​៖',
	'simplemap_osmtext' => 'មើល​ផែនទី​នេះ នៅលើ OpenStreetMap.org',
);

/** Ripoarisch (Ripoarisch)
 * @author Purodha
 */
$messages['ksh'] = array(
	'simplemap_desc' => 'Deit dä Befääl <tt> <nowiki>&lt;map&gt;</nowiki> </tt> em Wiki dobei, öm en <i lang="en">static map</i> Kaat aanzezeije. De Landkaate-Date kumme dobei fun <i lang="en">[http://openstreetmap.org openstreetmap.org]</i> her.',
	'simplemap_latmissing' => "Dä Wäät 'lat' för de Breed om Jlobus es nit aanjejovve.",
	'simplemap_lonmissing' => "Dä Wäät 'lon' för de Läng om Jlobus es nit aanjejovve.",
	'simplemap_zoommissing' => "Dä Wäät 'z' för dä Zoom es nit aanjejovve.",
	'simplemap_longdepreciated' => "Bes esu joot un donn dä Parrameeter 'lon' för de Läng om Jlobus nämme,
un nit mieh 'long' — dä Parrameeter wood enzwesche ömjanannt.",
	'simplemap_widthnan' => "„%1“ en kein jöltijje positive janze Zahl för dä Wäät 'w' för de Breed fum Beld.",
	'simplemap_heightnan' => "„%1“ en kein jöltijje positive janze Zahl för dä Wäät 'h' för de Hühde fum Beld.",
	'simplemap_zoomnan' => "„%1“ en kein jöltijje janze Zahl för dä Wäät 'z' för der Zoom.",
	'simplemap_latnan' => "„%1“ en kein jöltijje Zahl för dä Wäät 'lat' för de Brred om Jlobus.",
	'simplemap_lonnan' => "„%1“ es kein jöltijje Zahl för dä Wäät 'lon' för de Läng om Jlobus.",
	'simplemap_widthbig' => "Dä Wäät 'w' för de Breed fum Beld darf nit övver 1000 jonn.",
	'simplemap_widthsmall' => "Dä Wäät 'w' för de Breed fum Beld darf nit unger 100 jonn.",
	'simplemap_heightbig' => "Dä Wäät 'h' för de Hühde fum Beld darf nit övver 1000 jonn.",
	'simplemap_heightsmall' => "Dä Wäät 'h' för de Hühde fum Beld darf nit unger 100 jonn.",
	'simplemap_latbig' => "Dä Wäät 'lat' för de Breed om Jlobus darf nit övver 90 sin.",
	'simplemap_latsmall' => "Dä Wäät 'lat' för de Breed om Jlobus darf nit unger -90 sin.",
	'simplemap_lonbig' => "Dä Wäät 'lon' för de Läng om Jlobus darf nit övver 180 sin.",
	'simplemap_lonsmall' => "Dä Wäät 'lon' för de Läng om Jlobus darf nit unger -180 sin.",
	'simplemap_zoomsmall' => "Dä Wäät 'z' för der Zoom darf nit unger Noll sin.",
	'simplemap_zoom18' => 'Dä Wäät \'z\' för dä Zoom darf nit övver 17 sin.
Opjepaß: Hee dä Zosatz zor MediaWiki-ßoffwäer deiht de
<i lang="en">OpenStreetMap</i>-Kaate vum Tüp
\'<i lang="en">Osmarender</i>\' enbenge, wo dä Zoom bes 17 jeiht.
De \'<i lang="en">Mapnik</i>\' Kaate sen och op
http://openstreetmap.org/ ze fenge, un dänne iere Zoom jeiht bes 18.',
	'simplemap_zoombig' => "Dä Wäät 'z' för dä Zoom darf nit övver 17 sin.",
	'simplemap_invalidlayer' => "„%1“ es ene onjöltije Wäät för 'Schesch'.",
	'simplemap_maperror' => 'Fähler met dä Kaat:',
	'simplemap_osmtext' => 'Donn die Kaat op <i lang="en">OpenStreetMap.org</i> anloore',
);

/** Luxembourgish (Lëtzebuergesch)
 * @author Robby
 */
$messages['lb'] = array(
	'simplemap_desc' => "Erméiglecht d'Benotzung vum Tag <tt><nowiki>&lt;map&gt;</nowiki></tt> fir eng static map ze weisen. D'kaarte si vun [http://openstreetmap.org openstreetmap.org]",
	'simplemap_longdepreciated' => "Benitzt w.e.g. 'lon' aplaz vun  'long' (de parameter gouf ëmbennnt)",
	'simplemap_widthnan' => "Breet (w) de Wert '%1' ass keng gëlteg ganz Zuel",
	'simplemap_zoomnan' => "Zoom (z) de Wert '%1' ass keng gëlteg ganz Zuel",
	'simplemap_widthbig' => 'Breet (w) de Wert kann net méi grouss si wéi 1000',
	'simplemap_widthsmall' => 'Breet (w) de Wert kann net méi kleng si wéi 100',
	'simplemap_heightbig' => 'Héicht (h) de Wert kann net méi grouss wéi 1000 sinn',
	'simplemap_heightsmall' => 'Héicht (h) de Wert kann net méi kleng wéi 100 sinn',
	'simplemap_zoomsmall' => 'Zoom (z) de Wert kann net méi kleng si wéi null',
	'simplemap_zoombig' => 'Zoom (z) de Wert kann net méi grouss si wéi 17.',
	'simplemap_maperror' => 'Kaartefeeler:',
);

/** Nahuatl (Nāhuatl)
 * @author Fluence
 */
$messages['nah'] = array(
	'simplemap_maperror' => 'Ahcuallōtl āmatohcopa',
);

/** Dutch (Nederlands)
 * @author SPQRobin
 * @author Siebrand
 */
$messages['nl'] = array(
	'simplemap_desc' => 'Laat het gebruik van de tag <tt><nowiki>&lt;map&gt;</nowiki></tt> toe om een static-kaart weer te geven. Kaarten zijn van [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'De "lat"-waarde ontbreekt (voor de breedte).',
	'simplemap_lonmissing' => 'De "lon"-waarde ontbreekt (voor de lengte).',
	'simplemap_zoommissing' => 'De "z"-waarde ontbreekt (voor het zoomniveau).',
	'simplemap_longdepreciated' => 'Gebruik "lon" in plaats van "long" (parameter is hernoemd).',
	'simplemap_widthnan' => "De waarde '%1' voor de breedte (w) is geen geldige integer",
	'simplemap_heightnan' => "De waarde '%1' voor de hoogte (h) is geen geldige integer",
	'simplemap_zoomnan' => "De waarde '%1' voor de zoom (z) is geen geldige integer",
	'simplemap_latnan' => "De waarde '%1' voor de breedte (lat) is geen geldig nummer",
	'simplemap_lonnan' => "De waarde '%1' voor de lengte (lon) is geen geldig nummer",
	'simplemap_widthbig' => 'De breedte (w) kan niet groter dan 1000 zijn',
	'simplemap_widthsmall' => 'De breedte (w) kan niet kleiner dan 100 zijn',
	'simplemap_heightbig' => 'De hoogte (h) kan niet groter dan 1000 zijn',
	'simplemap_heightsmall' => 'De hoogte (h) kan niet kleiner dan 100 zijn',
	'simplemap_latbig' => 'De breedte (lat) kan niet groter dan -90 zijn',
	'simplemap_latsmall' => 'De breedte (lat) kan niet kleiner dan -90 zijn',
	'simplemap_lonbig' => 'De lengte (lon) kan niet groter dan 180 zijn',
	'simplemap_lonsmall' => 'De lengte (lon) kan niet kleiner dan -180 zijn',
	'simplemap_zoomsmall' => 'De zoom (z) kan niet minder dan nul zijn',
	'simplemap_zoom18' => 'De zoom (z) kan niet groter zijn dan 17. Merk op dat deze MediaWiki-uitbreiding de "Osmarender"-layer van OpenSteetMap gebruikt die niet dieper dan het niveau 17 gaat. de "Mapnik"-layer, beschikbaar op openstreetmap.org, gaat tot niveau 18.',
	'simplemap_zoombig' => 'De zoom (z) kan niet groter dan 17 zijn',
	'simplemap_invalidlayer' => 'Ongeldige waarde voor \'layer\' "%1"',
	'simplemap_maperror' => 'Kaartfout:',
	'simplemap_osmtext' => 'Deze kaart op OpenStreetMap.org bekijken',
);

/** Norwegian Nynorsk (‪Norsk (nynorsk)‬)
 * @author Harald Khan
 */
$messages['nn'] = array(
	'simplemap_desc' => 'Tillét bruk av merket <tt>&lt;map&gt;</tt> for å syna eit map frå static. Karti kjem frå [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Manglar «lat»-verdi (for breiddegrad).',
	'simplemap_lonmissing' => 'Manglar «lon»-verdi (for lengdegrad).',
	'simplemap_zoommissing' => 'Manglar «z»-verdi (for zoom-nivået).',
	'simplemap_longdepreciated' => 'Nytt «lon» i staden for «long» (parameteren fekk nytt namn).',
	'simplemap_widthnan' => 'breiddeverdien («w») «%1» er ikkje eit gyldig heiltal',
	'simplemap_heightnan' => 'høgdeverdien («h») «%1» er ikkje eit gyldig heiltal',
	'simplemap_zoomnan' => 'zoomverdien («z») «%1» er ikkje eit gyldig heiltal',
	'simplemap_latnan' => 'breiddegradsverdien («lat») «%1» er ikkje eit gyldig tal',
	'simplemap_lonnan' => 'lengdegradsverdien («lon») «%1» er ikkje eit gyldig tal',
	'simplemap_widthbig' => 'breiddeverdien («w») kan ikkje vera større enn 1000',
	'simplemap_widthsmall' => 'breiddeverdien («w») kan ikkje vera mindre enn 100',
	'simplemap_heightbig' => 'høgdeverdien («h») kan ikkje vera større enn 1000',
	'simplemap_heightsmall' => 'høgdeverdien («h») kan ikkje vera mindre enn 100',
	'simplemap_latbig' => 'breiddegraden («lat») kan ikkje vera større enn 90',
	'simplemap_latsmall' => 'breiddegraden («lat») kan ikkje vera mindre enn -90',
	'simplemap_lonbig' => 'lengdegraden («lon») kan ikkje vera større enn 180',
	'simplemap_lonsmall' => 'lengdegraden («lon») kan ikkje vera mindre enn -180',
	'simplemap_zoomsmall' => 'zoomverdien («z») kan ikkje vera mindre enn null',
	'simplemap_zoom18' => 'zoomverdien («z») kan ikkje vera større enn 17. Merk at denne MediaWiki-utvidingi nyttar OpenStreetMap-laget «osmarender», som ikkje kan zooma meir enn til nivå 17. «Mapnik»-laget på openstreetmap.org går til zoomnivå 18',
	'simplemap_zoombig' => 'zoomverdien («z») kan ikkje vera større enn 17.',
	'simplemap_invalidlayer' => 'Ugyldig «layer»-verdi «%1»',
	'simplemap_maperror' => 'Kartfeil:',
	'simplemap_osmtext' => 'Sjå dette kartet på OpenStreetMap.org',
);

/** Norwegian (bokmål)‬ (‪Norsk (bokmål)‬)
 * @author Harald Khan
 * @author Jon Harald Søby
 */
$messages['no'] = array(
	'simplemap_desc' => 'Tillater bruk av taggen <tt>&lt;map&gt;</tt> for å vise et static map. Kartene kommer fra [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Mangler «lat»-verdi (for breddegraden).',
	'simplemap_lonmissing' => 'Mangler «lon»-verdi (for lengdegraden).',
	'simplemap_zoommissing' => 'Mangler «z»-verdi (for zoom-nivået).',
	'simplemap_longdepreciated' => 'Bruk «lon» i stedet for «long» (parameteret fikk nytt navn).',
	'simplemap_widthnan' => 'breddeverdien («w») «%1» er ikke et gyldig heltall',
	'simplemap_heightnan' => 'høydeverdien («h») «%1» er ikke et gyldig heltall',
	'simplemap_zoomnan' => 'zoomverdien («z») «%1» er ikke et gyldig heltall',
	'simplemap_latnan' => 'breddegradsverdien («lat») «%1» er ikke et gyldig tall',
	'simplemap_lonnan' => 'lengdegradsverdien («lon») «%1» er ikke et gyldig tall',
	'simplemap_widthbig' => 'breddeverdien («w») kan ikke være større enn 1000',
	'simplemap_widthsmall' => 'breddeverdien («w») kan ikke være mindre enn 100',
	'simplemap_heightbig' => 'høydeverdien («h») kan ikke være større enn 1000',
	'simplemap_heightsmall' => 'høydeverdien («h») kan ikke være mindre enn 100',
	'simplemap_latbig' => 'breddegradsverdien («lat») kan ikke være større enn 90',
	'simplemap_latsmall' => 'breddegradsverdien («lat») kan ikke være mindre enn –90',
	'simplemap_lonbig' => 'lengdegradsverdien («lon») kan ikke være større enn 180',
	'simplemap_lonsmall' => 'lengdegradsverdien («lon») kan ikke være mindre enn –180',
	'simplemap_zoomsmall' => 'zoomverdien («z») kan ikke være mindre enn null',
	'simplemap_zoom18' => 'zoomverdien («z») kan ikke være større enn 17. Merk at denne MediaWiki-utvidelsen bruker OpenStreetMap-laget «osmarender», som ikke kan zoome mer enn til nivå 17. «Mapnik»-laget på openstreetmap.org går til zoomnivå 18.',
	'simplemap_zoombig' => 'zoomverdien («z») kan ikke være større enn 17.',
	'simplemap_invalidlayer' => 'Ugyldig «layer»-verdi «%1»',
	'simplemap_maperror' => 'Kartfeil:',
	'simplemap_osmtext' => 'Se dette kartet på OpenStreetMap.org',
);

/** Occitan (Occitan)
 * @author Cedric31
 */
$messages['oc'] = array(
	'simplemap_desc' => 'Autoriza l’utilizacion de la balisa <tt><nowiki>&lt;map&gt;</nowiki></tt> per afichar una mapa static. Las mapas provenon de [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Valor lat mancanta (per la latitud).',
	'simplemap_lonmissing' => 'Valor lon mancanta (per la longitud).',
	'simplemap_zoommissing' => 'Valor z mancanta (pel nivèl del zoom).',
	'simplemap_longdepreciated' => 'Utilizatz « lon » al luòc de « long » (lo paramètre es estat renomenat).',
	'simplemap_widthnan' => "La largor (w) qu'a per valor « %1 » es pas un nombre entièr corrèct.",
	'simplemap_heightnan' => "La nautor (h) qu'a per valor « %1 » es pas un nombre entièr corrèct.",
	'simplemap_zoomnan' => "Lo zoom (z) qu'a per valor « %1 » es pas un nombre entièr corrèct.",
	'simplemap_latnan' => "La latitud (lat) qu'a per valor « %1 » es pas un nombre corrèct.",
	'simplemap_lonnan' => "La longitud (lon) qu'a per valor « %1 » es pas un nombre corrèct.",
	'simplemap_widthbig' => 'La valor de la largor (w) pòt pas excedir 1000',
	'simplemap_widthsmall' => 'La valor de la largor (w) pòt pas èsser inferiora a 100',
	'simplemap_heightbig' => 'La valor de la nautor (h) pòt pas excedir 1000',
	'simplemap_heightsmall' => 'La valor de la nautor (h) pòt pas èsser inferiora a 100',
	'simplemap_latbig' => 'La valor de la latitud (lat) pòt pas excedir 90',
	'simplemap_latsmall' => 'La valor de la latitud (lat) pòt pas èsser inferiora a -90',
	'simplemap_lonbig' => 'La valor de la longitud (lon) pòt pas excedir 180',
	'simplemap_lonsmall' => 'La valor de la longitud (lon) pòt pas èsser inferiora a -180',
	'simplemap_zoomsmall' => 'La valor del zoom (z) pòt pas èsser negativa',
	'simplemap_zoom18' => "La valor del zoom (z) pòt excedir 17. Notatz qu'aqueste croquet d’extension MediaWiki dins lo jaç « osmarender » de OpenStreetMap pòt pas anar de delà del nivèl 17 del zoom. Lo jaç Mapnik disponible sus openstreetmap.org, pòt pas anar de delà del nivèl 18.",
	'simplemap_zoombig' => 'La valor del zoom (z) pòt pas excedir 17.',
	'simplemap_invalidlayer' => 'Valor de « %1 » del « jaç » incorrècta',
	'simplemap_maperror' => 'Error de mapa :',
	'simplemap_osmtext' => 'Vejatz aquesta mapa sus OpenStreetMap.org',
);

/** Polish (Polski)
 * @author Maikking
 */
$messages['pl'] = array(
	'simplemap_maperror' => 'Błąd mapy:',
);

/** Portuguese (Português)
 * @author Lijealso
 * @author Malafaya
 * @author Waldir
 */
$messages['pt'] = array(
	'simplemap_desc' => 'Permite o uso da marca <tt><nowiki>&lt;map&gt;</nowiki></tt> para apresentar um mapa static. Os mapas provêm de [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Faltando o valor lat (para a latitude).',
	'simplemap_lonmissing' => 'Faltando o valor lon (para a longitude).',
	'simplemap_zoommissing' => 'Falta valor z (para o nível de zoom).',
	'simplemap_longdepreciated' => "Por favor, use 'lon' em vez de 'long' (o parâmetro foi renomeado).",
	'simplemap_widthnan' => "o valor de largura (w) '%1' não é um inteiro válido",
	'simplemap_heightnan' => "o valor de altura (h) '%1' não é um inteiro válido",
	'simplemap_zoomnan' => "o valor de zoom (z) '%1' não é um inteiro válido",
	'simplemap_latnan' => "o valor de latitude (lat) '%1' não é um inteiro válido",
	'simplemap_lonnan' => "o valor de longitude (lon) '%1' não é um inteiro válido",
	'simplemap_widthbig' => 'o valor da largura (w) não pode ser maior que 1000',
	'simplemap_widthsmall' => 'o valor da largura (w) não pode ser menor que 100',
	'simplemap_heightbig' => 'o valor da altura (h) não pode ser maior que 1000',
	'simplemap_heightsmall' => 'o valor da altura (h) não pode ser menor que 100',
	'simplemap_latbig' => 'o valor da latitude (lat) não pode ser maior que 90',
	'simplemap_latsmall' => 'o valor da latitude (lat) não pode ser menor que -90',
	'simplemap_lonbig' => 'o valor da longitude (lon) não pode ser maior que 180',
	'simplemap_lonsmall' => 'o valor da longitude (lon) não pode ser menor que -180',
	'simplemap_zoomsmall' => 'o valor do zoom (z) não pode ser menor que zero',
	'simplemap_zoom18' => 'o valor do zoom (z) não pode ser maior de 17. Note que esta extensão MediaWiki liga-se ao visualizador "osmarender" do OpenStreetMap cujo valor de zoom não ultrapassa o nível 17. O visualizador Mapnik disponível no openstreetmap.org, vai até o nivel 18',
	'simplemap_zoombig' => 'O valor de zoom (z) não pode ser maior que 17.',
	'simplemap_invalidlayer' => "Valor '%1' inválido para 'layer'",
	'simplemap_maperror' => 'Erro no mapa:',
	'simplemap_osmtext' => 'Veja este mapa em OpenStreetMap.org',
);

/** Romanian (Română)
 * @author KlaudiuMihaila
 */
$messages['ro'] = array(
	'simplemap_latmissing' => 'Valoarea lat lipsă (pentru latitudine).',
	'simplemap_lonmissing' => 'Valoarea lon lipsă (pentru longitudine).',
);

/** Russian (Русский)
 * @author Ferrer
 */
$messages['ru'] = array(
	'simplemap_maperror' => 'Ошибка карты:',
	'simplemap_button_code' => 'Получить викикод',
);

/** Slovak (Slovenčina)
 * @author Helix84
 */
$messages['sk'] = array(
	'simplemap_desc' => 'Umožňuje použitie značky <tt><nowiki>&lt;map&gt;</nowiki></tt> na zobrazenie posuvnej mapy static. Mapy sú z [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Chýba hodnota lat (rovnobežka).',
	'simplemap_lonmissing' => 'Chýba hodnota lon (poludník).',
	'simplemap_zoommissing' => 'Chýba hodnota z (úroveň priblíženia)',
	'simplemap_longdepreciated' => 'Prosím, použite „lon” namiesto „long” (názov parametra sa zmenil).',
	'simplemap_widthnan' => 'hodnota šírky (w) „%1” nie je platné celé číslo',
	'simplemap_heightnan' => 'hodnota výšky (h) „%1” nie je platné celé číslo',
	'simplemap_zoomnan' => 'hodnota úrovne priblíženia (z) „%1” nie je platné celé číslo',
	'simplemap_latnan' => 'hodnota zemepisnej šírky (lat) „%1” nie je platné celé číslo',
	'simplemap_lonnan' => 'hodnota zemepisnej dĺžky (lon) „%1” nie je platné celé číslo',
	'simplemap_widthbig' => 'hodnota šírky (w) nemôže byť väčšia ako 1000',
	'simplemap_widthsmall' => 'hodnota šírky (w) nemôže byť menšia ako 100',
	'simplemap_heightbig' => 'hodnota výšky (h) nemôže byť väčšia ako 1000',
	'simplemap_heightsmall' => 'hodnota výšky (h) nemôže byť menšia ako 100',
	'simplemap_latbig' => 'hodnota zemepisnej dĺžky (h) nemôže byť väčšia ako 90',
	'simplemap_latsmall' => 'hodnota zemepisnej dĺžky (h) nemôže byť menšia ako -90',
	'simplemap_lonbig' => 'hodnota zemepisnej šírky (lon) nemôže byť väčšia ako 180',
	'simplemap_lonsmall' => 'hodnota zemepisnej dĺžky (lon) nemôže byť menšia ako -180',
	'simplemap_zoomsmall' => 'hodnota úrovne priblíženia (lon) nemôže byť menšia ako nula',
	'simplemap_zoom18' => 'hodnota úrovne priblíženia (lon) nemôže byť väčšia ako 17. Toto rozšírenie MediaWiki využíva vrstvu „osmarender” OpenStreetMap, ktorá umožňuje úroveň priblíženia po 17. Vrstva Mapnik na openstreetmap.org umožňuje priblíženie do úrovne 18.',
	'simplemap_zoombig' => 'hodnota úrovne priblíženia (lon) nemôže byť väčšia ako 17.',
	'simplemap_invalidlayer' => 'Neplatná hodnota „layer” „%1”',
	'simplemap_maperror' => 'Chyba mapy:',
	'simplemap_osmtext' => 'Pozrite si túto mapu na OpenStreetMap.org',
);

/** Swedish (Svenska)
 * @author Boivie
 * @author M.M.S.
 */
$messages['sv'] = array(
	'simplemap_desc' => 'Tillåter användning av taggen <tt>&lt;map&gt;</tt> för att visa map static. Kartorna kommer från [http://openstreetmap.org openstreetmap.org]',
	'simplemap_latmissing' => 'Saknat "lat"-värde (för breddgraden).',
	'simplemap_lonmissing' => 'Saknat "lon"-värde (för längdgraden).',
	'simplemap_zoommissing' => 'Saknat z-värde (för zoom-nivån).',
	'simplemap_longdepreciated' => 'Var god använd "lon"  istället för "long" (parametern fick ett nytt namn).',
	'simplemap_widthnan' => 'breddvärdet (w) "%1" är inte ett giltigt heltal',
	'simplemap_heightnan' => 'höjdvärdet (h) "%1" är inte ett giltigt heltal',
	'simplemap_zoomnan' => 'zoomvärdet (z) "%1" är inte ett giltigt heltal',
	'simplemap_latnan' => 'breddgradsvärdet (lat) "%1" är inte ett giltigt nummer',
	'simplemap_lonnan' => 'längdgradsvärdet (lon) "%1" är inte ett giltigt nummer',
	'simplemap_widthbig' => 'breddvärdet (w) kan inte vara större än 1000',
	'simplemap_widthsmall' => 'breddvärdet (w) kan inte vara mindre än 100',
	'simplemap_heightbig' => 'höjdvärdet (h) kan inte vara större än 1000',
	'simplemap_heightsmall' => 'höjdvärdet (h) kan inte vara mindre än 100',
	'simplemap_latbig' => 'breddgradsvärdet (lat) kan inte vara större än 90',
	'simplemap_latsmall' => 'breddgradsvärdet (lat) kan inte vara mindre än -90',
	'simplemap_lonbig' => 'längdgradsvärdet (lon) kan inte vara större än 180',
	'simplemap_lonsmall' => 'längdgradsvärdet (lon) kan inte vara mindre än -180',
	'simplemap_zoomsmall' => 'zoomvärdet (z) kan inte vara mindre än noll',
	'simplemap_zoom18' => "zoomvärdet (z) kan inte vara högre än 17. Observera att detta programtillägg använder OpenStreetMap-lagret 'osmarender', som inte kan zoomas mer än till nivå 17. Mapnik-lagret på openstreetmap.org zoomar till nivå 18",
	'simplemap_zoombig' => 'zoomvärdet (z) kan inte vara högre än 17.',
	'simplemap_invalidlayer' => "Ogiltigt 'layer'-värde '%1'",
	'simplemap_maperror' => 'Kartfel:',
	'simplemap_osmtext' => 'Se den här kartan på OpenStreetMap.org',
);

/** Telugu (తెలుగు)
 * @author Veeven
 */
$messages['te'] = array(
	'simplemap_maperror' => 'పటపు పొరపాటు:',
);

/** Tagalog (Tagalog)
 * @author AnakngAraw
 */
$messages['tl'] = array(
	'simplemap_desc' => "Nagpapahintulot sa paggamit ng tatak na <tt><nowiki>&lt;map&gt;</nowiki></tt> upang maipakita/mapalitaw ang isang static mapa.  Nanggaling ang mga mapa mula sa [http://openstreetmap.org openstreetmap.org]",
	'simplemap_latmissing' => 'Nawawalang halaga para sa latitud (lat).',
	'simplemap_lonmissing' => 'Nawawalang halaga para sa longhitud (lon).',
	'simplemap_zoommissing' => "Nawawalang halagang 't' (mula sa 'tutok') para sa antas ng paglapit/pagtutok (''zoom'').",
	'simplemap_longdepreciated' => "Pakigamit lamang ang 'lon' sa halip na 'long' (muling pinangalanan ang parametro).",
	'simplemap_widthnan' => "ang halaga ng lapad (l) na '%1' ay hindi isang tanggap na buumbilang (''integer'')",
	'simplemap_heightnan' => "ang halaga ng taas (t) na '%1' ay hindi isang tanggap na buumbilang (''integer'')",
	'simplemap_zoomnan' => "ang halaga ng pagtutok/paglapit ('t' mula sa 'tutok' o ''zoom'') na '%1' ay hindi isang tanggap na buumbilang (''integer'')",
	'simplemap_latnan' => "ang halaga ng latitud (lat) na '%1' ay hindi isang tanggap na buumbilang (''integer'')",
	'simplemap_lonnan' => "ang halaga ng longhitud (lon) na '%1' ay hindi isang tanggap na buumbilang (''integer'')",
	'simplemap_widthbig' => 'hindi maaaring humigit/lumabis kaysa 1000 ang halaga ng lapad (l)',
	'simplemap_widthsmall' => 'hindi maaaring bumaba kaysa 1000 ang halaga ng lapad (l)',
	'simplemap_heightbig' => 'hindi maaaring humigit/lumabis kaysa 1000 ang halaga ng taas (t)',
	'simplemap_heightsmall' => 'hindi maaaring bumaba kaysa 1000 ang halaga ng taas (t)',
	'simplemap_latbig' => 'hindi maaaring humigit/lumabis kaysa 90 ang halaga ng latitud (lat)',
	'simplemap_latsmall' => 'hindi maaaring bumaba kaysa -90 ang halaga ng latitud (lat)',
	'simplemap_lonbig' => 'hindi maaaring humigit/lumabis kaysa 180 ang halaga ng longhitud (lon)',
	'simplemap_lonsmall' => 'hindi maaaring bumaba kaysa -180 ang halaga ng longhitud (lon)',
	'simplemap_zoomsmall' => "hindi maaaring bumaba kaysa wala/sero ang halaga ng pagtutok/paglapit ('t' mula sa 'tutok') o ''zoom''.",
	'simplemap_zoom18' => "hindi maaaring humigit/lumabis kaysa 17 ang halaga ng pagtutok/paglapit ('t' mula sa 'tutok') o ''zoom''.  Tandaan lamang na ang mga karugtong na ito na pang-Mediawiki ay kumakawing/kumakabit patungo sa sapin/patong na 'osmarender' ng OpenStreetMap na hindi lumalagpas mula sa kaantasan ng pagkakatutok na 17.  Ang sapin/patong na Mapnik na makukuha mula sa openstreetmap.org ay umaabot pataas sa kaantasan ng pagkakatutok na 18",
	'simplemap_zoombig' => "hindi maaaring humigit/lumabis kaysa 17 ang halaga ng pagtutok/paglapit ('t' mula sa 'tutok') o ''zoom''.",
	'simplemap_invalidlayer' => "Hindi tanggap ang halaga ng 'patong' o 'sapin' na '%1'",
	'simplemap_maperror' => 'Kamalian sa mapa:',
	'simplemap_osmtext' => 'Tingnan ang mapang ito sa OpenStreetMap.org',
);

/** Vietnamese (Tiếng Việt)
 * @author Minh Nguyen
 */
$messages['vi'] = array(
	'simplemap_desc' => 'Thêm thẻ <tt><nowiki>&lt;map&gt;</nowiki></tt> để nhúng bản đồ. Các bản đồ do [http://openstreetmap.org openstreetmap.org] cung cấp.',
	'simplemap_latmissing' => 'Thiếu giá trị lat (vĩ độ).',
	'simplemap_lonmissing' => 'Thiếu giá trị lon (kinh độ).',
	'simplemap_zoommissing' => 'Thiếu giá trị z (cấp thu phóng).',
	'simplemap_longdepreciated' => 'Xin hãy dùng “lon” thay vì “long” (tham số đã được đổi tên).',
	'simplemap_widthnan' => 'giá trị chiều rộng (w), “%1”, không phải là nguyên số hợp lệ',
	'simplemap_heightnan' => 'giá trị chiều cao (h), “%1”, không phải là nguyên số hợp lệ',
	'simplemap_zoomnan' => 'giá trị cấp thu phóng (z), “%1”, không phải là nguyên số hợp lệ',
	'simplemap_latnan' => 'giá trị vĩ độ (lat), “%1”, không phải là số hợp lệ',
	'simplemap_lonnan' => 'giá trị kinh độ (lon), “%1”, không phải là số hợp lệ',
	'simplemap_widthbig' => 'giá trị chiều rộng (w) tối đa là “1000”',
	'simplemap_widthsmall' => 'giá trị chiều rộng (w) tối thiểu là “100”',
	'simplemap_heightbig' => 'giá trị chiều cao (h) tối đa là “1000”',
	'simplemap_heightsmall' => 'giá trị chiều cao (h) tối thiểu là “100”',
	'simplemap_latbig' => 'giá trị vĩ độ (lat) tối đa là “90”',
	'simplemap_latsmall' => 'giá trị vĩ độ (lat) tối thiểu là “-90”',
	'simplemap_lonbig' => 'giá trị kinh độ (lon) tối đa là “180”',
	'simplemap_lonsmall' => 'giá trị kinh độ (lon) tối thiểu là “-180”',
	'simplemap_zoomsmall' => 'giá trị cấp thu phóng tối thiểu là “0”',
	'simplemap_zoom18' => 'giá trị cấp thu phóng (z) tối đa là 17. Lưu ý rằng phần mở rộng MediaWiki này dựa trên lớp “osmarender” của OpenStreetMap, nó không vẽ rõ hơn cấp 17. Lớp Mapnik tại openstreetmap.org tới được cấp 18.',
	'simplemap_zoombig' => 'giá trị cấp thu phóng (z) tối đa là 17.',
	'simplemap_invalidlayer' => 'Giá trị “layer” không hợp lệ: “%1”.',
	'simplemap_maperror' => 'Lỗi trong bản đồ:',
	'simplemap_osmtext' => 'Xem bản đồ này tại OpenStreetMap.org',
);

/** Volapük (Volapük)
 * @author Smeira
 */
$messages['vo'] = array(
	'simplemap_maperror' => 'Mapapöl:',
);

/** Simplified Chinese (‪中文(简体)‬)
 * @author Gzdavidwong
 */
$messages['zh-hans'] = array(
	'simplemap_widthbig' => '宽度值（w）不能大于1000',
	'simplemap_widthsmall' => '宽度值（w）不能小于100',
	'simplemap_heightbig' => '高度值（h）不能大于1000',
	'simplemap_heightsmall' => '高度值（h）不能小于100',
	'simplemap_latbig' => '纬度值（lat）不能大于90',
	'simplemap_latsmall' => '纬度值（lat）不能小于-90',
	'simplemap_lonbig' => '经度值（lon）不能大于180',
	'simplemap_lonsmall' => '经度值（lon）不能小于-180',
);

/** Traditional Chinese (‪中文(繁體)‬)
 * @author Gzdavidwong
 * @author Wrightbus
 */
$messages['zh-hant'] = array(
	'simplemap_widthbig' => '寬度值(w)不能大於1000',
	'simplemap_widthsmall' => '寬度值(w)不能小於100',
	'simplemap_heightbig' => '高度值(h)不能大於1000',
	'simplemap_heightsmall' => '高度值(h)不能少於100',
	'simplemap_latbig' => '緯度值(lat)不能大於90',
	'simplemap_latsmall' => '緯度值(lat)不能小於-90',
	'simplemap_lonbig' => '經度值(lon)不能大於180',
	'simplemap_lonsmall' => '經度值(lon)不能小於-180',
);

