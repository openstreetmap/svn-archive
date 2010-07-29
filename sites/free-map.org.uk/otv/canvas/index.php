<html>
<head>
<style type='text/css'>
#photocanvas { background-color: #ffffe0; }
#status { width:1024px; height:200px; overflow: auto }
</style>
<script type='text/javascript' src='../../freemap/javascript/prototype/prototype.js'></script>
<script type='text/javascript' src='CanvController.js'> </script>
<script type='text/javascript' src='cpv.js'> </script>
<script type='text/javascript'>

var pv;

function init()
{
	/*
	pv=new CanvasPanoViewer({
				canvasId: 'photocanvas',
				statusElement: 'status',
				hFovDst : 90,
				hFovSrc : 360,
				showStatus : 0,
				wSlice:10 
			} );
	*/
	pv = new CanvController('status');
	pv.load(<?php echo ($_GET['id'] ? $_GET['id']: 91); ?>);
	//pv.loadImage('/otv/panorama/85',null);
}

</script>
</head>
<body onload='init()'>
<canvas id='photocanvas' width='1024' height='400'></canvas>
<div id='status'>Status</div>
</body>
</html>
