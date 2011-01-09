<?php
    session_start();
?>
<html>
<head>
    <title>OpenTrailView - Submit your panoramas</title>

    <script type='text/javascript' src='js/fileuploader.js'></script>
    <link rel='stylesheet' type='text/css' href='css/osv.css' />
    <link rel='stylesheet' type='text/css' href='css/fileuploader.css' />
</head>
<body>
<?php
    include('otv_funcs.php');
    if(isset($_SESSION['gatekeeper']))
    {
        //$_SESSION['photosession'] = newsession();
        ?>
        <h1>OpenTrailView</h1>
        <h2>Submit a series of panoramas</h2>
        <p>
        Upload each panorama in your trip (on Firefox or Chrome,
		you can now select
        more than one at once, thanks to Andrew Valums' multi-file
        uploader, see <a href='http://valums.com/ajax-upload'>here</a>).
        Then, if you also recorded a GPX trace
        while out, you can select "Auto-position with GPX", below, to 
        automatically position each panorama. </p>
        <p>You can then:
            <ul>
            <li>use the <a href='photomgr.php'>photo management
            page</a> to manage your photos (position any not auto-located
            using your GPX trace)</li>
            <li>orientate a panorama on the main page by rotating its camera
            icon.</li>
            </ul>
        </p>
        <p>Note that photos will need to be authorised before they become
        visible to the public; however you can still manage them on the
        <a href='photomgr.php'>photo management page</a> and orientate
        them on the main page.</p>
        


        <div id='div1'>this is div1</div>

        <script type='text/javascript'>
        function uploadFiles()
        {
            var uploader = new qq.FileUploader( {
            element: document.getElementById('div1'),
            action: '/photoupload.php',
            sizeLimit: 3*1024*1024,
            onComplete: function(id,fileName,responseJSON)
            {
                var str="";
                for(x in responseJSON)
                    str +=  x+"="+responseJSON[x]+":";
				//alert(str);            
            }
            } );
            //init();
        }

        window.onload = uploadFiles;
        </script>
        <p>
        <a href='gpxupload.php'>
        Auto-position uploaded photos with GPX
        </a> |
        <a href='index.php'>Back to map</a>
        </p>
        <?php
    }
	else
	{
		?>
		<p>
		You need to <a href='/common/user.php?action=login&redirect=/psbmt.php'>
			log in</a> to contribute photos.
		</p>
		<?php
	}
?>
</body>
</html>
