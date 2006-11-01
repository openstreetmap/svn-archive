
/* File: mapTiles.js
*/

// from npemap

var sizeGridX = 6;   // Size of visible grid (table) in each dimension
var sizeGridY = 4;   // Size of visible grid (table) in each dimension

var tileWidth = 125;
var tileHeight = 125;


var zoomLevel = 1;


// end from npemap

function init() {
	setUpGrid();
	refreshGrid();
}

// All this is taken from the npemap code. Some is tweaked slightly

function setUpGrid() {
  mapElement = document.getElementById('map');

	/*
	var e = (1+parseInt(trimNumber(offsetGridX-Math.round(sizeGridX/2))))*1000;
	var n = (1+parseInt(trimNumber(offsetGridY-Math.round(sizeGridY/2))))*1000;
	*/

  page = ''
  for(var y=sizeGridY; y>=1; y--) {
    for(var x=1; x<=sizeGridX; x++) {
      page += '<img width=' + tileWidth + ' height=' + tileHeight + 
	  		' id=element' + x + '.' + y + '>';
     }
     page += '<br/>';
  }
  
	/*
	page += "<img id='fmap' src='freemap.php?bbox="+e+","+n+","+(e+6000)+","+(n+4000)+"&width=750&height=500' />";
	*/
    mapElement.innerHTML = page
}


// Need to use substring for IE, and it doesn't take negative numbers.
function trimNumber(string) {
  str = '000' + string
	return str.substring(str.length -3)
}

function refreshGrid() {
  
	for(var x=1; x<=sizeGridX; x++) {
		for(var y=1; y<=sizeGridY; y++) {
            // Update the table using the array data from position 0,0.
            tileX = trimNumber(x+offsetGridX-Math.round(sizeGridX/2));
            tileY = trimNumber(y+offsetGridY-Math.round(sizeGridY/2));
            document.getElementById('element' + x + '.' + y).src = 
					tileURL(tileX, tileY);
        }
    }

	/*
	var e = (1+parseInt(trimNumber(offsetGridX-Math.round(sizeGridX/2))))*1000;
	var n = (1+parseInt(trimNumber(offsetGridY-Math.round(sizeGridY/2))))*1000;
	*/

	/*
	document.getElementById('fmap').src=
	"freemap.php?bbox="+e+","+n+","+(e+6000)+","+(n+4000)+
		"&width=750&height=500";
	*/
}

function tileURL(tileX, tileY) {
		x1=tileX*1000;
		y1=tileY*1000;
		
		return "freemap.php?bbox="+x1+','+ y1+
				","+(x1+1000)+","+(y1+1000)+
				"&width="+tileWidth+"&height="+tileHeight;
				
		//return "http://ustile.npemap.org.uk/scaled1/"+tileX+'/'+tileY +'.jpg';
}

function updateGrid(theDirection) {
    if ( theDirection == 'right' ) {
            offsetGridX += 2;
    }
    else if ( theDirection == 'left' ) {
            offsetGridX -= 2;
    }

    else if ( theDirection == 'up' ) {
            offsetGridY += 2;
    }
    else if ( theDirection == 'down' ) {
            offsetGridY -= 2;
    }

	refreshGrid();
}

// end of code taken from npemap

function zoomin() { 
	if(tileWidth<250 && tileHeight<250) {
		tileWidth *= 2;
		tileHeight *= 2;
		sizeGridX = Math.round(6 * (125/tileWidth));
		sizeGridY = Math.round(4 * (125/tileWidth));
		sizeGridX = (sizeGridX<2) ? 2:sizeGridX;
		sizeGridY = (sizeGridY<2) ? 2:sizeGridY;
		setUpGrid();
		refreshGrid();
	} else {
		alert('Cannot zoom in any further!');
	}
}
function zoomout() { 
	if(tileWidth>125 && tileHeight>125) {
		tileWidth /= 2;
		tileHeight /= 2;
		sizeGridX = Math.round(6 * (125/tileWidth));
		sizeGridY = Math.round(4 * (125/tileWidth));
		sizeGridX = (sizeGridX<2) ? 2:sizeGridX;
		sizeGridY = (sizeGridY<2) ? 2:sizeGridY;
		setUpGrid();
		refreshGrid();
	} else {
		alert('Cannot zoom out any further!');
	}
}
