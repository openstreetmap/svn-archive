<?
/* REALLY UGLY */
$goal = 10000;

//CONNECT to DB
include('../htdocs/process/db-connect.inc.php');


$sql_query_total_raised = 'SELECT SUM( `amount_gbp` ) AS `total` FROM `donations` WHERE `processed` =1';
$sql_result = mysql_query($sql_query_total_raised, $_DB_H) OR die('FAIL UPDATING: '.$sql_query_total_raised);
if ($sql_result AND mysql_num_rows($sql_result)==1) {
	$total = mysql_fetch_array($sql_result ,MYSQL_ASSOC);
	$fp = fopen('../htdocs/raised.inc.html', 'w');
	$raised_string	= number_format($total['total']);
	$raised_percent	= number_format( ($total['total']/$goal*100),2);
	fwrite($fp, '&pound; '.$raised_string.' ('.$raised_percent.'%)');
	fclose($fp);
}
?>