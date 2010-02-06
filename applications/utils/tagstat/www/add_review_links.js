// Copyright Jean-Baptiste Rouquier

// The license of this file is the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// See http://www.gnu.org/licenses/

////////////////////////////////////////////////////////////////////////////////
// Configuration

// If a tag appears more than [maxNbUses] times,
// do not offer to download and parse the .osm xml file,
// as it would be slow and would blow up the HTML page.
// JOSM might be better suited for opening mid-sized .osm xml files.
var maxNbUses=50;

var nbUsesIndex       = 2; // index of the column containing the number of uses of this key/value pair.
var nbElementsIndices = {'node':3, 'way':4, 'relation':5};
var tableIndex    = 0;     // index of the table we will edit.
var minRowIndex   = 1;     // nb of table header rows we have to skip
var xapiLinkBaseURL = '';  // prefix of the URL to data.osm
// URL to request data.osm. JS can only read local files (security to prevent cross-site scripting), so we may need a proxy.
var xapiURL = function (key,value) {return './osm_xapi_proxy.php?key='+ encodeURIComponent(key) +'&value='+ encodeURIComponent(value);};
var requestDuration = 'about 10s';


////////////////////////////////////////////////////////////////////////////////
// main code
var tags = {1: 'node', 2: 'way', 3: 'relation'};

String.prototype.entityify = function () {
	return this.replace(/&/g, "&amp;").replace(/</g,"&lt;").replace(/>/g, "&gt;");
};

// We will overwrite table cells in the DOM. This global variable is used to store the original content,
// and to restore it (link "remove links").
var originalProperties = new Object();

function addReviewLinks(){ // called once the page is loaded. Add a column with "review" links.
	var table=document.getElementsByTagName('table')[tableIndex];
	var rows=table.rows;

	for (var i=minRowIndex;i<rows.length;i++) {
		var row = rows[i];
		var cells = row.cells;
		var xapiLink=xapiLinkBaseURL + cells[cells.length-1].getElementsByTagName('a')[0].href;
		var nbUses=cells[nbUsesIndex].innerHTML;
		if (nbUses <= maxNbUses) {
			var cell = row.insertCell(-1);
			cell.id='review_link_'+i;
			cell.innerHTML='<a href="javascript:void(0)" onclick=addReviewLink("'+xapiLink+'","'+cell.id+'")>review</a>';
		};
	};
	return 0;
};

// add the links to one row, pointing to the /browse/ pages on openstreetmap.org
function addReviewLink (xapiLink,cellId) {
	var cell=document.getElementById(cellId);

	// Extract the key/value pair from [xapiLink]. Here are two example URLs:
	// http://tagwatch.stoecker.eu/osmxapi/*%5Bbridge=1%5D
	// http://www.informationfreeway.org/api/0.6/*[highway=traffic_signals;crossing]
	var regexp = /^http:\/\/.*\/\*(%5B|\[)([^\/]*)=([^\/]*)(%5D|\])$/;
	matches=regexp.exec(xapiLink);
	var key   = matches[2];
	var value = matches[3];
	key   = decodeURIComponent(key);
	value = decodeURIComponent(value);

	// save [cell.innerHTML], then overwrite:
	if (! originalProperties[cellId]) originalProperties[cellId] = new Object();
	if (! originalProperties[cellId].addedCell) originalProperties[cellId].addedCell = new Object();
	originalProperties[cellId].addedCell.innerHTML = cell.innerHTML;
	cell.innerHTML = 'Requesting data.osm...  (This may take '+requestDuration+'.)';

	// request data.osm
	var req = new XMLHttpRequest(); 	// For internet explorer, this should probably read var req = new ActiveXObject("Msxml2.XMLHTTP")
	req.open('GET', xapiURL(key,value), false);
	req.send(null);
	if(req.status != 200) {
		cell.innerHTML=('Could not get osm data.');
		return 0;};
		cell.innerHTML = 'received data.osm, processing...';

		// parse data.osm
		var osmdata = null;
		try {
			var xmlstring=String(req.responseText).replace(/<\?xml version=.*?\?\>/,"");
			osmdata = new XML( xmlstring );
			// see bugs 270553 and 336551 at the end of https://developer.mozilla.org/en/E4X
		}
		catch (e) {
			cell.innerHTML = (
					'Could not parse osm data.<br>\n' + e +
					'<br>\nBeginning of xml data is:<br>\n' +
					xmlstring.substring(0,1000).entityify() );
			return 0;}

			// modify the DOM.
			var firstTag = 1; // Is it the first non empty tag among [tags] ?
			for each (var tagname in tags) {
				var firstId=1;
				var xmlnodes=osmdata[tagname];

				var existingCell= cell.parentNode.cells[nbElementsIndices[tagname]];
				// save the cell we will overwrite:
				if (! originalProperties[cellId]) originalProperties[cellId] = new Object();
				if (! originalProperties[cellId][tagname]) originalProperties[cellId][tagname] = new Object ();
				var originalExistingCell = originalProperties[cellId][tagname];
				originalExistingCell.innerHTML = existingCell.innerHTML;
				if (! originalExistingCell.style) originalExistingCell.style = new Object ();
				originalExistingCell.style.textAlign = existingCell.style.textAlign;

				for (i=0;i<xmlnodes.length();i++) {
					var xmlnode=xmlnodes[i];
					if (xmlnode.tag.(@k == key && @v == value).length() > 0) {// see https://developer.mozilla.org/en/E4X_Tutorial/Descendants_and_Filters
						if (firstId) {
							firstId=0;
							existingCell.innerHTML += '<br>' ;
							existingCell.style.textAlign = 'left';
							firstTag = 0;
						}
						id = xmlnode.@id;
						link = '<a href="http://www.openstreetmap.org/browse/'+tagname+'/'+id+'">'+id+'</a> ';
						existingCell.innerHTML += link;
					}
				}
			};
			cell.innerHTML='<a href="javascript:void(0)" onclick=removeReviewLink("'+cell.id+'")>remove links</a>';
			return 0;
};

// restore the cells of one row (the row of [cellId]), using [originalProperties].
function removeReviewLink (cellId) {
	var cell=document.getElementById(cellId);
	cell.innerHTML = originalProperties[cellId].addedCell.innerHTML;
	for each (var tagname in tags) {
		var existingCell= cell.parentNode.cells[nbElementsIndices[tagname]];
		var originalExistingCell = originalProperties[cellId][tagname];
		existingCell.innerHTML = originalExistingCell.innerHTML;
		existingCell.style.textAlign = originalExistingCell.style.textAlign;
	};
	return 0;
}
