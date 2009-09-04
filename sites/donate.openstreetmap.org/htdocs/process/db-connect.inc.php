<?

$_DB_H = mysql_connect('localhost','osm_donate','password');
mysql_select_db('osm_donate', $_DB_H);
mysql_query('SET NAMES \'utf8\'', $_DB_H);

?>