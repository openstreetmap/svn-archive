<?php
///\file contains the central configuration data for the t@h server installation

/// root directory (no ending slash)
$conf['BASEDIR'] = '/var/www/osm';

/// log directory (no ending slash)
$conf['LOGDIR'] = $conf['BASEDIR'].'/Log/Data';

/// tiles are stored in
$conf['TILEDIR'] = '/mnt/agami/openstreetmap/tah/Tiles';
?>
