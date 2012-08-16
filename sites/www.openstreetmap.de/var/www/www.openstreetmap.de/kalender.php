<?php
//	include the calendar file
$the_file="ac/ac-includes/cal.inc.php";
if(!file_exists($the_file)) die("<b>".$the_file."</b> not found");
else		require_once($the_file);
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
       "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>OpenStreetMap Deutschland: GPS-Verleih: Kalender</title>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<meta name="description" content="" />
	<meta name="keywords" content="" />
	<meta name="language" content="de" />
	<meta name="robots" content="index,nofollow" />
	<meta name="author" content="" />
	<meta name="revisit-After" content="7 days" />
	<meta name="distribution" content="global" />
	<link rel="icon" href="img/favicon.png" type="image/png">
	<link rel="Shortcut Icon" href="img/favicon.png" type="image/png">
	
	<link rel="stylesheet" href="css/bootstrap.css" type="text/css" media="all" />
	<link rel="stylesheet" href="css/mystyle.css" type="text/css" media="all" />
	<link rel="stylesheet" href="<?php echo AC_DIR_CSS; ?>avail-calendar.css">
	
	<style type="text/css">
	table.cal {
    	border-collapse: collapse;
	}
	table.cal th {
    	padding: 2px 10px;
    	background-color: #bfc0d8;
    	color: #404590;
	}
	table.cal td {
    	padding: 2px 10px;
    	border-bottom: 1px solid #bfc0d8;
	}
	.frei {
		background-color: lightgreen;
	}
	.verliehen {
		background-color: red;
	}
	.tverliehen {
		background-color: orange;
	}
    </style>
        
</head>
 
<div class="container">

	<div class="mtop">
	    <div class="mnav lnav">
	        <ul>
	           	<li><a href="index.html">Startseite</a></li>
	            <li><a href="faq.html">FAQ</a></li>
	            <li><a href="karte.html">Karte</a></li>
	            <li><a href="community.html">Community</a></li>
	            <li><a target="_blank" href="http://blog.openstreetmap.de">Blog/News</a></li>
	            <li><a href="spenden.html">Spenden</a></li>
	            <li><a href="impressum.html">Kontakt/Impressum</a></li>
	        </ul>
	    </div>
	</div>

	<div id="logo">
	    <a href="index.html"><img id="osm_logo" src="img/osm_logo.png" alt="logo"/></a>
	</div>

	<h1 class="osm_heading">OpenStreetMap - Deutschland</h1>
	
	<div class="page-header">
    	<h1>GPS-Verleih<small> Kalender</small></h1>
  	</div>
  	
  	<p>An den folgenden Terminen sind die GPS-Geräte schon gebucht:</p>

		<table class="cal">
			<tr>
				<th>Datum</th>
				<th>Anzahl</th>
				<th>Aktion</th>
			</tr>
		<!-- START1 -->

			<tr>
				<td>2010-01-18 - 2010-01-20</td>
				<td>10</td>
				<td>Projektwoche August-Hermann-Francke-Schule Hamburg/Uhlenhorst</td>
			<tr>
				<td>2010-01-22 - 2010-01-29</td>
				<td>10</td>

				<td>Mapping in und um Troisdorf</td>
			<tr>
				<td>2010-02-22 - 2010-02-28</td>
				<td>10</td>
				<td>Jugendförderung Walluf: Mapping von Nieder- und Oberwalluf</td>
			<tr>
				<td>2010-03-08 - 2010-03-12</td>

				<td>10</td>
				<td>Projektwoche St. Jakobus-Schule Breckerfeld</td>
			<tr>
				<td>2010-04-22</td>
				<td>5</td>
				<td>Mapping in Bedburg-Hau, bei Kleve am Niederrhein</td>

			<tr>
				<td>2010-04-23 - 2010-04-27</td>
				<td>5</td>
				<td>Schul-Mapping in Damgarten</td>
			<tr>
				<td>2010-05-01 - 2010-05-19</td>
				<td>5</td>

				<td>Mapping-Wettbewerb Thierbach</td>
			<tr>
				<td>2010-05-05 - 2010-05-09</td>
				<td>5</td>
				<td>NaturFreunde Marl e.V.: Mapping in und um Marl</td>
			<tr>
				<td>2010-05-13 - 2010-05-16</td>

				<td>5</td>
				<td>Studiengang Technische Redaktion (Hochschule Aalen): 88178 Heimenkirch</td>
			<tr>
				<td>2010-05-22 - 2010-05-30</td>
				<td>10</td>
				<td>Mapping in Montabaur (Westerwald)</td>

			<tr>
				<td>2010-06-04</td>
				<td>5</td>
				<td>Industriegebiet Ahrensburg</td>
			<tr>
				<td>2010-06-14 - 2010-06-28</td>
				<td>10</td>

				<td>Mapping in Köln und Troisdorf</td>
			<tr>
				<td>2010-07-01 - 2010-07-08</td>
				<td>10</td>
				<td>Ribnitz-Damgarten</td>
			<tr>
				<td>2010-08-21 - 2010-08-22</td>

				<td>10</td>
				<td>FrOSCon</td>
			<tr>
				<td>2010-09-05 - 2010-09-11</td>
				<td>10</td>
				<td><i>Angefragt:</i> Kursfahrt des Gymnasium Oedeme</td>

			<tr>
				<td>2010-09-27 - 2010-09-30</td>
				<td>10</td>
				<td>Mapping in und um Schönau, Langenried, Gestratz</td>
			<tr>
				<td>2010-10-04 - 2010-10-08</td>
				<td>10</td>

				<td>Schulmapping in Nidda</td>
			<tr>
				<td>2010-10-13 - 2010-10-15</td>
				<td>10</td>
				<td>Projekt am Kreisgymnasium Hochschwarzwald</td>
			<tr>
				<td>2010-12-05 - 2010-12-12</td>

				<td>10</td>
				<td>Comenius-Schulprojekt zum Thema OSM</td>
			<tr>
				<td>2011-01-15 - 2011-01-28</td>
				<td>10</td>
				<td>Rhein-Maas-Gymnasium: Touristische Infrastruktur in &Ouml;sterreich</td>

			<tr>
				<td>2011-01-31 - 2011-02-04</td>
				<td>10</td>
				<td>Projektwoche der WHS</td>
			<tr>
				<td>2011-06-24 - 2011-06-30</td>
				<td>10</td>

				<td>Sch&uuml;lerprojekt Gymnasium Fridericianum: Schwerin und Umgebung</td>
			<tr>
				<td>2011-07-17</td>
				<td>10</td>
				<td>Mapping im Rahmen des Ferienpass der Gemeinde Loxstedt</td>
			<tr>

				<td>2011-07-30 - 2011-08-07</td>
				<td>10</td>
				<td>Cde Sommer-Akademie</td>
			<tr>
				<td>2011-11-03 - 2011-11-07</td>
				<td>10</td>

				<td>VCP Alt Duvenstedt</td>
		<!-- END1 -->
                </table>

                <p>Es sind jeweils nur die Termine des eigentlichen Events aufgelistet. Vorher und
                nachher sind noch 1-3 Tage für die Postlaufzeiten zu berücksichtigen.</p>

		<div id="the_months">
			<?php echo $calendar_months; ?>
		</div>
		<div id="key_wrapper">
			<?php echo $calendar_states; ?>
			<div id="footer_data" style="clear:both;">
				<?php echo $lang["last_update"].': '.get_cal_update_date(ID_ITEM); ?>
			</div>
		</div>
  	
</div>
	<script type="text/javascript">		
	//	define vars
	var url_ajax_cal 		= '<?php echo AC_DIR_AJAX; ?>calendar.ajax.php'; // ajax file for loading calendar via ajax
	var img_loading_day		= '<?php echo AC_DIR_IMAGES; ?>ajax-loader-day.gif'; // animated gif for loading	
	var img_loading_month	= '<?php echo AC_DIR_IMAGES; ?>ajax-loader-month.gif'; // animated gif for loading	
	//	don't change these values
	var id_item 			= '<?php echo ID_ITEM; ?>'; // id of item to be modified (via ajax)
	var lang 				= '<?php echo AC_LANG; ?>'; // language
	var months_to_show		= <?php echo AC_NUM_MONTHS; ?>; // number of months to show
	var clickable_past		= '<?php echo AC_ACTIVE_PAST_DATES; ?>'; // previous dates
	</script> 
	<script type="text/javascript" src="<?php echo AC_DIR_JS; ?>mootools-core-1.3.2-full-compat-yc.js"></script>
	<script type="text/javascript" src="<?php echo AC_DIR_JS; ?>mootools-cal-public.js"></script>
</body>
</html>
