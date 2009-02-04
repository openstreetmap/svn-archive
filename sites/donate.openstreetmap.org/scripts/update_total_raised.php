<?
/* REALLY UGLY */
$goal = 10000;
$_DB_H = mysql_pconnect('localhost','osm_donate','password');
mysql_select_db('osm_donate', $_DB_H);
mysql_query('SET NAMES \'utf8\'', $_DB_H);
$sql_query_total_raised = 'SELECT SUM( `amount_gbp` ) AS `total` FROM `donations` WHERE `processed` =1';
$sql_result = mysql_query($sql_query_total_raised, $_DB_H) OR die('FAIL UPDATING: '.$sql_query_total_raised);
if ($sql_result AND mysql_num_rows($sql_result)==1) {
	$total = mysql_fetch_array($sql_result ,MYSQL_ASSOC);
	$fp = fopen('raised.inc.html', 'w');
	$raised_string	= number_format($total['total']);
	$raised_percent	= number_format( ($total['total']/$goal*100),2);
	fwrite($fp, '£ '.$raised_string.' ('.$raised_percent.'%)');
	fclose($fp);
}
?>