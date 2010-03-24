
function Dialog (html, okAction, cancelAction, map, colour, size) 
{
	size = (size) ? size:new OpenLayers.Size(480,360);
	colour = (colour) ? colour:'#ffffc0';

	html += "<input type='button' id='okbtn' value='OK' />";
	if(cancelAction)
		html+="<input type='button' value='Cancel' id='cancelbtn' />";
		
	var popup =   new OpenLayers.Popup('fmap_client_popup',
				map.getLonLatFromViewPortPx
				(new OpenLayers.Pixel(50,50)), size, html);

	popup.setBackgroundColor(colour);
	map.addPopup(popup);
	$('okbtn').onclick = okAction; 
	if(cancelAction)
		$('cancelbtn').onclick = cancelAction; 
	return popup;
}
