
var PI = 3.14159265358979323846;

// Class to handle converting between lat/lon and other custom coordinate
// systems e.g. Google or OSGB. Useful if you want to overlay lat/lon markers
// (e.g. from GeoRSS) on base maps using another coordinate system

// type can be:
//     GOOG - Google coord system (as used in Mapnik OSM)
//     OSGB - UK grid refs (requires jscoord from www.jstott.me.uk/jscoord)

function converter(type)
{
	this.type = type;
	this.customToNorm = customToNorm;
	this.normToCustom = normToCustom;
	this.customToNormBounds = customToNormBounds;
	this.normToCustomBounds = normToCustomBounds;
	this.phi2 = phi2;
}

function phi2 (ts,e)
{
	var eccnth = 0.5*e;
	var Phi = (PI/2) - 2.0*Math.atan(ts);
	var dphi;
	var i=15;
	do
	{
		var con = e*Math.sin(Phi);
		dphi = (PI/2) - 2.0*Math.atan(ts*Math.pow((1.0-con)/(1.0+con),eccnth))
			- Phi;
		Phi += dphi;
	}
	while(Math.abs(dphi) > 0.0000000001 && --i);
	return Phi;
}

// convert between different coordinate schemes (e.g. google and osgb) and
// lat/lon
// partially borrowed from the main openstreetmap site
function customToNorm (custom)
{
	var lat_deg, lon_deg;
	switch(this.type)
	{
		case "GOOG": 
           lat_deg = (custom.lat / 20037508.34) * 180;
           lon_deg = (custom.lon / 20037508.34) * 180;
           lat_deg = 180/PI * 
		   		(2 * Math.atan(Math.exp(lat_deg * PI / 180)) - PI / 2);
		   break;

		case "OSGB": 
			var ll = new OSRef(custom.lon,custom.lat).toLatLng();		
			ll.OSGB36ToWGS84();
			lat_deg = ll.lat;
			lon_deg = ll.lng;
			break;

		case "Mercator":
			var a = 6378137.0;
			var b = 6356752.3142;
			var k0 = 1.0;
			var t = 1.0 - b/a;
			var es = 2*t - t*t;
			var e = Math.sqrt(es);
			lon_deg = custom.lon;
			lat_deg = custom.lat;
			lon_deg /= a;
			lat_deg /= a;
			lon_deg /= k0;
			lat_deg = phi2(Math.exp(-lat_deg/k0), e);
			lon_deg *= (180/PI);
			lat_deg *= (180/PI);
			break;

		default:
			lon_deg = custom.lon;
			lat_deg = custom.lat;
	}
	return new OpenLayers.LonLat(lon_deg,lat_deg);
}

function normToCustom(norm)
{
	var custLon, custLat; 

	switch(this.type)
	{
		case "GOOG":
         var a = 
			 Math.log(Math.tan((90+parseFloat(norm.lat))*PI / 360))/(PI / 180);
 		custLat = a * 20037508.34 / 180;
		custLon=norm.lon;
 		custLon = custLon * 20037508.34 / 180;
		break;

		case "OSGB":
			var ll = new LatLng(norm.lat,norm.lon);
			ll.WGS84ToOSGB36();
			var osgb = ll.toOSRef();
			custLon = osgb.easting;
			custLat = osgb.northing;
			break;

		case "Mercator":
			var lat2  = norm.lat * (PI/180);
			var a = 6378137;
			var b = 6356752.3142;
			var f = (a-b)/a;
			var e = Math.sqrt(2*f-Math.pow(f,2));
			custLat=a*Math.log(Math.tan(PI/4+lat2/2)*
				Math.pow(( (1-e*Math.sin(lat2)) / (1+e*Math.sin(lat2))),e/2));
			custLon = norm.lon * (PI/180) * 6378137;
			break;

		default:
			custLon = norm.lon;
			custLat = norm.lat;
	}
	return new OpenLayers.LonLat(custLon,custLat);
}

function customToNormBounds(bounds)
{
	var bl = this.customToNorm
				(new OpenLayers.LonLat(bounds.left,bounds.bottom));
	var tr = this.customToNorm
				(new OpenLayers.LonLat(bounds.right,bounds.top));
	var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
	return newBounds;
}

function normToCustomBounds(bounds)
{
	var bl = this.normToCustom
				(new OpenLayers.LonLat(bounds.left,bounds.bottom));
	var tr = this.normToCustom
				(new OpenLayers.LonLat(bounds.right,bounds.top));
	var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
	return newBounds;
}
