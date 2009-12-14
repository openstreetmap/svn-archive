<?php

	@define('CONST_Debug', false);
	@define('CONST_ClosedForIndexing', false);
	@define('CONST_ClosedForIndexingExceptionIPs', ',192.168.1.137,77.100.156.176,87.194.178.147,');
	@define('CONST_BlockedIPs', ',85.22.48.42,');
	@define('CONST_Search_AreaPolygons', true);
	@define('CONST_Website_BaseURL', 'http://nominatim.openstreetmap.org/');

//	@define('CONST_Database_DSN', 'pgsql://www-data@/gazetteer');
	@define('CONST_Database_DSN', 'pgsql://www-data@/gazetteerworld');

	@define('CONST_Default_Language', 'en');
	@define('CONST_Default_Lat', 20.0);
	@define('CONST_Default_Lon', 0.0);
	@define('CONST_Default_Zoom', 2);

