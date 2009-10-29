<?php
session_start();
include('include/functions.php');

//Check if the language is set with the ?l=<2lettercode> GET parameter
if(isset($_GET["l"])) {
	$LANG = $_GET["l"];
	$_SESSION['LANG'] = $LANG;
}

//Initialize localization
i18n();

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title><? msg('OpenStreetMap POI export');?> - <? msg('download free point of interest'); ?></title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta name="Description" content="<? msg('OpenStreetMap POI Export'); ?>" />
        <meta name="Keywords" content="openstreetmap,poi,waypoint,export,free,tomtom,garmin,google earth,oziexplorer,ov2,csv,gpx,wpt" />
        <meta name="robots" content="index, follow" />
        <meta http-equiv="Content-Language" content="<? echo $_SESSION['LANG']; ?>" />
        <link rel="shortcut icon" href="favicon.ico" />
        <link type="text/css" href="style.css" rel="stylesheet" />
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script>
        <script type="text/javascript" src="ui.js"></script>
    </head>

    <body>
        <div class="wizardwrapper">
            <div class="wizardpanel 1">
                <ul class="mainNav threeStep">
                    <li class="current"><a title=""><em><? msg('1: POI type'); ?></em><span><? msg('Select the POI type') ?></span></a></li>
                    <li><a title=""><em><? msg('2: Device'); ?></em><span><? msg('Select the device or file type you want to download to'); ?></span></a></li>
                    <li class="mainNavNoBg"><a title=""><em><? msg('3: Export'); ?></em><span><? msg('Download POI file'); ?></span></a></li>
                </ul>
                <div class="wizardcontent">
                    <img src="images/pushpin.png" alt="" />
                    <div class="explanation"><? msg('Select the type of POI you would like to download. Currently only one type can be selected.') ?></div>
                    <select name="select" multiple size="10" id="poitype" onDblclick="UpdateButtons(1);LoadNextPage(1,2);" onchange="UpdateButtons(1)">
                        <option value="amenity:fuel"><? msg('Fuel'); ?></option>
                        <option value="amenity:atm"><? msg('ATM'); ?></option>
                        <option value="amenity:speed_camera"><? msg('Speed camera'); ?></option>
                        <option value="highway:bus_stop"><? msg('Bus stop'); ?></option>
                        <option value="amenity:parking"><? msg('Parking'); ?></option>
                        <option value="amenity:bicycle_parking"><? msg('Bicycle parking'); ?></option>
                        <option value="amenity:place_of_worship"><? msg('Place of worship'); ?></option>
                        <option value="amenity:hospital"><? msg('Hospital'); ?></option>
                        <option value="shop:supermarket"><? msg('Supermarket'); ?></option>
                        <option value="amenity:theatre"><? msg('Theatre'); ?></option>
                        <option value="amenity:police"><? msg('Police'); ?></option>
                        <option value="amenity:fire_station"><? msg('Fire station'); ?></option>
                        <option value="amenity:post_box"><? msg('Post box'); ?></option>
                        <option value="amenity:post_office"><? msg('Post office'); ?></option>
                        <option value="amenity:recycling"><? msg('Recycling'); ?></option>
                        <option value="amenity:restaurant"><? msg('Restaurant'); ?></option>
                        <option value="amenity:fast_food"><? msg('Fast food'); ?></option>
                        <option value="amenity:toilets"><? msg('Toilets'); ?></option>
                        <option value="amenity:pub"><? msg('Pub'); ?></option>
                        <option value="amenity:waste_basket"><? msg('Waste basket'); ?></option>
						<option value="barrier:cattle_grid"><? msg('Cattle grid'); ?></option>
                        <option value="tourism:camp_site"><? msg('Camp site'); ?></option>
                        <option value="tourism:hotel"><? msg('Hotel'); ?></option>
                        <option value="tourism:museum"><? msg('Museum'); ?></option>
                        <option value="tourism:zoo"><? msg('Zoo'); ?></option>
                        <option value="historic:castle"><? msg('Castle'); ?></option>
                        <option value="man_made:windmill"><? msg('Windmill'); ?></option>
                        <option value="man_made:lighthouse"><? msg('Lighthouse'); ?></option>
                        <option value="man_made:watermill"><? msg('Watermill'); ?></option>
                        <option value="man_made:water_tower"><? msg('Water tower'); ?></option>
                        <option value="amenity:nightclub"><? msg('Nightclub'); ?></option>
                        <option value="amenity:stripclub"><? msg('Stripclub'); ?></option>
                    </select>
                </div>
                <div class="buttons">
                    <button type="submit" id="next1" class="next" disabled="disabled" onclick="LoadNextPage(1,2);"><? msg('Next'); ?>&nbsp;&gt;&gt;</button>
                </div>
                <div style="clear:both"></div>
            </div>

            <div class="wizardpanel 2">
                <ul class="mainNav threeStep">
                    <li class="lastDone"><a href="#" title="" onclick="LoadNextPage(2,1);"><em><? msg('1: POI type'); ?></em><span><? msg('Select the POI type') ?></span></a></li>
                    <li class="current"><a title=""><em><? msg('2: Device'); ?></em><span><? msg('Select the device or file type you want to download to'); ?></span></a></li>
                    <li class="mainNavNoBg"><a title=""><em><? msg('3: Export'); ?></em><span><? msg('Download POI file'); ?></span></a></li>
                </ul>
                <div class="wizardcontent">
                    <img src="images/device.png" alt="" />
                    <div class="explanation"><? msg('Select the device or file type you want to download the POI in'); ?></div>
                    <select id="navitype" size="10"  onDblclick="UpdateButtons(2);LoadNextPage(2,3);" onchange="UpdateButtons(2)">
                        <option value="ov2"><? msg('TomTom overlay (ov2)'); ?></option>
                        <option value="csv"><? msg('Garmin (csv)'); ?></option>
                        <option value="gpx"><? msg('GPS Exchange format (gpx)'); ?></option>
                        <option value="kml"><? msg('Google Earth (kml)'); ?></option>
						<option value="wpt"><? msg('OziExplorer (wpt)'); ?></option>
                        <!--<option value="osm"><? msg('OpenStreetMap (osm)'); ?></option>-->
                    </select>
                </div>
                <div class="buttons">
                    <button type="submit" id="back2" class="previous" onclick="LoadNextPage(2,1);">&lt;&lt;&nbsp;<? msg('Previous'); ?></button>
                    <button type="submit" id="next2" class="next" disabled="disabled" onclick="LoadNextPage(2,3);"><? msg('Next'); ?>&nbsp;&gt;&gt;</button>
                </div>

                <div style="clear:both"></div>
            </div>


            <div class="wizardpanel 3">
                <ul class="mainNav threeStep">
                    <li class="done"><a href="#" title="" onclick="LoadNextPage(3,1);"><em><? msg('1: POI type'); ?></em><span><? msg('Select the POI type') ?></span></a></li>
                    <li class="lastDone"><a href="#" onclick="LoadNextPage(3,2);" title=""><em><? msg('2: Device'); ?></em><span><? msg('Select the device or file type you want to download to'); ?></span></a></li>
                    <li class="mainNavNoBg current"><a title=""><em><? msg('3: Export'); ?></em><span><? msg('Download POI file'); ?></span></a></li>
                </ul>
                <div class="wizardcontent">
                    <img src="images/download.png" alt="" />
                    <div class="explanation"><? msg('The file is ready for download.') ?></div>
                    <div id="action">
                        <p><? msg('Poi type') ?> : - </p>
                        <p><? msg('Device type') ?> : -</p>
                    </div>
                </div>
                <div class="buttons">
                    <button type="submit" id="back3" class="previous" onclick="LoadNextPage(3,2);">&lt;&lt;&nbsp;<? msg('Previous'); ?></button>
                    <button type="submit" id="next3" class="next"  onclick="DownloadFile();"><? msg('Download'); ?></button>
                </div>
                <div style="clear:both"></div>
            </div>
            <div class="footer"><? msg('Poi Export'); ?> - <? echo $VERSION; ?>&nbsp;
                <? msg('data'); ?> <a href="http://creativecommons.org/licenses/by-sa/2.0/">cc-by-sa</a>&nbsp;
                <a href="http://www.openstreetmap.nl/"><? msg('OpenStreetMap'); ?></a>&nbsp;
                <? msg('community') ?>.&nbsp;-&nbsp;
                <? msg('Created by'); ?>&nbsp;<a href="http://www.openstreetmap.org/user/rullzer">rullzer</a>&nbsp;<? msg('and'); ?>&nbsp;<a href="http://www.openstreetmap.org/user/Rubke">rubke</a>
           </div>
        </div>
    </body>
</html>
