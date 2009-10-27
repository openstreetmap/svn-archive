var WizardPages = { PoiSelection:1, NaviSelection:2, Finish:3 };

function LoadNextPage(divout,divin){
	$("." + divout).fadeOut("fast", function () {
		$("." + divin).fadeIn("fast", function() {
			if (divin == WizardPages.Finish)
			{
				RefreshFinishPage();
			}
		});
	});
}
function UpdateButtons(stage){
	var selection;
	switch (stage)
	{
		case WizardPages.PoiSelection:
			selection = $("#poitype :selected").text();
			if (selection == "")
			{
				$("#next1").attr("disabled","disabled");
			} else {
				$("#next1").removeAttr("disabled");
			}
			break;

		case WizardPages.NaviSelection:
			selection = $("#navitype :selected").text();
			if (selection == "")
			{
				$("#next2").attr("disabled","disabled");
			} else {
				$("#next2").removeAttr("disabled");
			}
			break;
	}
}
function RefreshFinishPage()
{
	$("#action").html("<p>Poi type : " + $("#poitype :selected").text() + "</p><p>Apparaat type : " + $("#navitype :selected").text() + "</p>");
}
function DownloadFile() {
	var Filename = $("#poitype :selected").text().replace(" ", "_").toLowerCase(); 
	var PoiType = $("#poitype :selected").val();
	var NaviType = $("#navitype :selected").val();
	var kv = PoiType.split(":"); // get key value pair
	var DownloadUrl = "download.php?" + "k=" + kv[0] + "&v=" + kv[1] + "&output=" +  NaviType + "&filename=" + Filename;
	window.location.href = DownloadUrl;  // goto download url
}
$(document).ready(function(){
	$("." + WizardPages.PoiSelection).css('display', 'block'); // display first page
});
