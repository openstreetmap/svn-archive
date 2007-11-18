
function Dialog (html, okAction, cancelAction, map) 
{
	html += "<input type='button' id='okbtn' value='Go!' />"+
		"<input type='button' value='Cancel' id='cancelbtn' /> ";
		
	var popup =   new OpenLayers.Popup('fmap_client_popup',
				map.getLonLatFromViewPortPx
				(new OpenLayers.Pixel(50,50)),
				new OpenLayers.Size(480,360), html);

	popup.setBackgroundColor('#ffffc0');
	map.addPopup(popup);
	$('okbtn').onclick = okAction; 
	$('cancelbtn').onclick = cancelAction; 
	return popup;
}
