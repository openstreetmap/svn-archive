<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
session_start();

require_once('functions.php');

$vars = array ("lat"=>50.9,"lon"=>-1.4,"zoom"=>12);

foreach ($vars as $var=>$default)
{
	if(wholly_numeric($_REQUEST[$var]))
		$_SESSION[$var] = $_REQUEST[$var];
	elseif(!isset($_SESSION[$var]))
			$_SESSION[$var] = $default; 
}

$trk = (isset($_SESSION['ngatekeeper'])) ? 1: 0;
?>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <script type="text/javascript" src="main.js"></script>
  <script type="text/javascript" src="tile.js"></script>
  <script type="text/javascript" src="latlong.js"></script>
  <link rel="stylesheet" type="text/css" href="css/osmtest.css" />
  <title>OSM map tester</title>
</head>
<?php
echo "<body onload='init($_SESSION[lat],$_SESSION[lon],$_SESSION[zoom],$trk)'>";
?>
 
<div id="freemap">


<div id="vpcontainer">
		
	<div id="drag" style='width:400px; height: 320px'></div>
	<canvas id="canvas1" width="400" height="320">
	</canvas>
	
</div>

<div id="mapdiv3">
	
		<div class="panel">
		<form id='form1' method='post' action='geocoder.php'>
		<h1>Search</h1>
		<label for="place">Name</label>
		<input name="place" id="place" class="inputelement"/>
		<label for="country">Country</label>
<select name="country" id="country" style="width:100px">
<option  value="af">Afghanistan</option>
<option  value="al">Albania</option>
<option  value="ag">Algeria</option>
<option  value="an">Andorra</option>
<option  value="ao">Angola</option>
<option  value="av">Anguilla</option>
<option  value="ac">Antigua And Barbuda</option>
<option  value="ar">Argentina</option>
<option  value="am">Armenia</option>
<option  value="aa">Aruba</option>
<option  value="at">Ashmore And Cartier Islands</option>
<option  value="as">Australia</option>
<option  value="au">Austria</option>
<option  value="aj">Azerbaijan</option>
<option  value="bf">Bahamas</option>
<option  value="ba">Bahrain</option>
<option  value="bg">Bangladesh</option>
<option  value="bb">Barbados</option>
<option  value="bs">Bassas Da India</option>
<option  value="bo">Belarus</option>
<option  value="be">Belgium</option>
<option  value="bh">Belize</option>
<option  value="bn">Benin</option>
<option  value="bd">Bermuda</option>
<option  value="bt">Bhutan</option>
<option  value="bl">Bolivia</option>
<option  value="bk">Bosnia And Herzegovina</option>
<option  value="bc">Botswana</option>
<option  value="bv">Bouvet Island</option>
<option  value="br">Brazil</option>
<option  value="io">British Indian Ocean Territory</option>
<option  value="vi">British Virgin Islands</option>
<option  value="bx">Brunei</option>
<option  value="bu">Bulgaria</option>
<option  value="uv">Burkina Faso</option>
<option  value="bm">Burma</option>
<option  value="by">Burundi</option>
<option  value="iv">Cote d'Ivoire</option>
<option  value="cb">Cambodia</option>
<option  value="cm">Cameroon</option>
<option  value="ca">Canada</option>
<option  value="cv">Cape Verde</option>
<option  value="cj">Cayman Islands</option>
<option  value="ct">Central African Republic</option>
<option  value="cd">Chad</option>
<option  value="ci">Chile</option>
<option  value="ch">China</option>
<option  value="kt">Christmas Island</option>
<option  value="ip">Clipperton Island</option>
<option  value="ck">Cocos (Keeling) Islands</option>
<option  value="co">Colombia</option>
<option  value="cn">Comoros</option>
<option  value="cf">Congo</option>
<option  value="cg">Congo (Demo. Republic)</option>
<option  value="cw">Cook Islands</option>
<option  value="cr">Coral Sea Islands</option>
<option  value="cs">Costa Rica</option>
<option  value="hr">Croatia</option>
<option  value="cu">Cuba</option>
<option  value="cy">Cyprus</option>
<option  value="ez">Czech Republic</option>
<option  value="da">Denmark</option>
<option  value="dj">Djibouti</option>
<option  value="do">Dominica</option>
<option  value="dr">Dominican Republic</option>
<option  value="tt">East Timor</option>
<option  value="ec">Ecuador</option>
<option  value="eg">Egypt</option>
<option  value="es">El Salvador</option>
<option  value="ek">Equatorial Guinea</option>
<option  value="er">Eritrea</option>
<option  value="en">Estonia</option>
<option  value="et">Ethiopia</option>
<option  value="eu">Europa Island</option>
<option  value="fk">Falkland Islands (Islas Malvinas)</option>
<option  value="fo">Faroe Islands</option>
<option  value="fj">Fiji</option>
<option  value="fi">Finland</option>
<option  value="fr">France</option>
<option  value="fg">French Guiana</option>
<option  value="fp">French Polynesia</option>
<option  value="fs">French Southern/Antarctic Lands</option>
<option  value="gb">Gabon</option>
<option  value="ga">Gambia</option>
<option  value="gz">Gaza Strip</option>
<option  value="gg">Georgia</option>
<option  value="gm">Germany</option>
<option  value="gh">Ghana</option>
<option  value="gi">Gibraltar</option>
<option  value="go">Glorioso Islands</option>
<option  value="gr">Greece</option>
<option  value="gl">Greenland</option>
<option  value="gj">Grenada</option>
<option  value="gp">Guadeloupe</option>
<option  value="gt">Guatemala</option>
<option  value="gk">Guernsey</option>
<option  value="gv">Guinea</option>
<option  value="pu">Guinea-Bissau</option>
<option  value="gy">Guyana</option>
<option  value="ha">Haiti</option>
<option  value="hm">Heard Island/McDonald Islands</option>
<option  value="ho">Honduras</option>
<option  value="hk">Hong Kong</option>
<option  value="hu">Hungary</option>
<option  value="ic">Iceland</option>
<option  value="in">India</option>
<option  value="id">Indonesia</option>
<option  value="ir">Iran</option>
<option  value="iz">Iraq</option>
<option  value="ei">Ireland</option>
<option  value="im">Isle Of Man</option>
<option  value="is">Israel</option>
<option  value="it">Italy</option>
<option  value="jm">Jamaica</option>
<option  value="jn">Jan Mayen</option>
<option  value="ja">Japan</option>
<option  value="je">Jersey</option>
<option  value="jo">Jordan</option>
<option  value="ju">Juan De Nova Island</option>
<option  value="kz">Kazakhstan</option>
<option  value="ke">Kenya</option>
<option  value="kr">Kiribati</option>
<option  value="ku">Kuwait</option>
<option  value="kg">Kyrgyzstan</option>
<option  value="la">Laos</option>
<option  value="lg">Latvia</option>
<option  value="le">Lebanon</option>
<option  value="lt">Lesotho</option>
<option  value="li">Liberia</option>
<option  value="ly">Libya</option>
<option  value="ls">Liechtenstein</option>
<option  value="lh">Lithuania</option>
<option  value="lu">Luxembourg</option>
<option  value="mc">Macau</option>
<option  value="mk">Macedonia</option>
<option  value="ma">Madagascar</option>
<option  value="mi">Malawi</option>
<option  value="my">Malaysia</option>
<option  value="mv">Maldives</option>
<option  value="ml">Mali</option>
<option  value="mt">Malta</option>
<option  value="rm">Marshall Islands</option>
<option  value="mb">Martinique</option>
<option  value="mr">Mauritania</option>
<option  value="mp">Mauritius</option>
<option  value="mf">Mayotte</option>
<option  value="mx">Mexico</option>
<option  value="fm">Micronesia</option>
<option  value="md">Moldova</option>
<option  value="mn">Monaco</option>
<option  value="mg">Mongolia</option>
<option  value="mh">Montserrat</option>
<option  value="mo">Morocco</option>
<option  value="mz">Mozambique</option>
<option  value="wa">Namibia</option>
<option  value="nr">Nauru</option>
<option  value="np">Nepal</option>
<option  value="nl">Netherlands</option>
<option  value="nt">Netherlands Antilles</option>
<option  value="nc">New Caledonia</option>
<option  value="nz">New Zealand</option>
<option  value="nu">Nicaragua</option>
<option  value="ng">Niger</option>
<option  value="ni">Nigeria</option>
<option  value="ne">Niue</option>
<option  value="nm">No Man's Land</option>
<option  value="nf">Norfolk Island</option>
<option  value="kn">North Korea</option>
<option  value="no">Norway</option>
<option  value="os">Oceans</option>
<option  value="mu">Oman</option>
<option  value="pk">Pakistan</option>
<option  value="ps">Palau</option>
<option  value="pm">Panama</option>
<option  value="pp">Papua New Guinea</option>
<option  value="pf">Paracel Islands</option>
<option  value="pa">Paraguay</option>
<option  value="pe">Peru</option>
<option  value="rp">Philippines</option>
<option  value="pc">Pitcairn Islands</option>
<option  value="pl">Poland</option>
<option  value="po">Portugal</option>
<option  value="qa">Qatar</option>
<option  value="re">Reunion</option>
<option  value="ro">Romania</option>
<option  value="rs">Russia</option>
<option  value="rw">Rwanda</option>
<option  value="sh">Saint Helena</option>
<option  value="sc">Saint Kitts And Nevis</option>
<option  value="st">Saint Lucia</option>
<option  value="sb">Saint Pierre And Miquelon</option>
<option  value="vc">Saint Vincent And The Grenadines</option>
<option  value="ws">Samoa</option>
<option  value="sm">San Marino</option>
<option  value="tp">Sao Tome And Principe</option>
<option  value="sa">Saudi Arabia</option>
<option  value="sg">Senegal</option>
<option  value="yi">Serbia And Montenegro</option>
<option  value="se">Seychelles</option>
<option  value="sl">Sierra Leone</option>
<option  value="sn">Singapore</option>
<option  value="lo">Slovakia</option>
<option  value="si">Slovenia</option>
<option  value="bp">Solomon Islands</option>
<option  value="so">Somalia</option>
<option  value="sf">South Africa</option>
<option  value="sx">South Georgia/Sandwich Islands</option>
<option  value="ks">South Korea</option>
<option  value="sp">Spain</option>
<option  value="pg">Spratly Islands</option>
<option  value="ce">Sri Lanka</option>
<option  value="su">Sudan</option>
<option  value="ns">Suriname</option>
<option  value="wz">Swaziland</option>
<option  value="sw">Sweden</option>
<option  value="sz">Switzerland</option>
<option  value="sy">Syria</option>
<option  value="tw">Taiwan</option>
<option  value="ti">Tajikistan</option>
<option  value="tz">Tanzania</option>
<option  value="th">Thailand</option>
<option  value="to">Togo</option>
<option  value="tl">Tokelau</option>
<option  value="tn">Tonga</option>
<option  value="td">Trinidad And Tobago</option>
<option  value="te">Tromelin Island</option>
<option  value="ts">Tunisia</option>
<option  value="tu">Turkey</option>
<option  value="tx">Turkmenistan</option>
<option  value="tk">Turks And Caicos Islands</option>
<option  value="tv">Tuvalu</option>
<option  value="ug">Uganda</option>
<option  value="up">Ukraine</option>
<option  value="uf">Undersea Features</option>
<option  value="ae">United Arab Emirates</option>
<option  value="uk" selected="selected">United Kingdom</option>
<option  value="us">United States</option>
<option  value="uy">Uruguay</option>
<option  value="uz">Uzbekistan</option>
<option  value="nh">Vanuatu</option>
<option  value="vt">Vatican City</option>
<option  value="ve">Venezuela</option>
<option  value="vm">Vietnam</option>
<option  value="wf">Wallis And Futuna</option>
<option  value="we">West Bank</option>
<option  value="wi">Western Sahara</option>
<option  value="ym">Yemen</option>
<option  value="za">Zambia</option>
<option  value="zi">Zimbabwe</option>
</select>
<br/>
		<input class="submit" type="submit" 
				id="searchsubmit" value="Go"/>
		</form>
		</div>


		<?php showScaleControl(); ?>
</div>
<div id="mapdiv4">
		<div class="panel">
		<h1>Go to</h1>
			<div>

			<label for="txtLat">Latitude:</label>
			<input name="txtLat" id="txtLat" class="inputelement"/>
			<label for="txtLon">Longitude:</label>
			<input name="txtLon" id="txtLon" class="inputelement"/>
			<input type="button" value="Go!" id="btnLL"/>
			</div>
		</div>

		<div class="panel">

		<h1>Options</h1>

		<label for="view">View</label>
		<select id='view' class="inputelement">
		<option value="0" selected='selected'>normal</option>
		<option value="1">tracks</option>
		</select>


		<label for="action">Action</label>
		<select id='action' class="inputelement">
		<option selected='selected'>drag</option>
		<option>feature</option>
		<option>featuredel</option>
		<option>featureupdate</option>
		<option>featurequery</option>
		<option>distance</option>
		</select>
		</div>
</div>

<div id="bottomarea">
<div id='status'>
<h3>OSM drawing client testing</h3>
<p> This page is a testing page for new OSM map-drawing code.</p>
</div>
</div>


<div id="promptbox">
<h3>Enter details</h3>
<label for="featurename">Name</label>
<input id="featurename"/>
<label for="featuredesc">Description</label>
<textarea id="featuredesc"></textarea>
<input type='button' id="featurego" value="go"/>
</div>

</div>

</body>
</html>

<?php
function showScaleControl()
{
	echo "<div class='panel'>\n";

			echo '<img class="scaleimg" id="magnify" '.
			'src="images/magnify.png" alt="Increase scale 2x" />';

			echo '<img class="scaleimg" id="shrink" '.
			'src="images/shrink.png" alt="Increase scale 2x" />';
	echo "</div>\n";
}

?>
