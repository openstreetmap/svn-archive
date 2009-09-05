<?
/* REALLY UGLY */
$goal = 800;

//CONNECT to DB
include('../htdocs/process/db-connect.inc.php');

$sql_query_total_raised = 'SELECT SUM( `amount_gbp` ) AS `total` FROM `donations` WHERE `processed` =1 AND `target`="domain"';
$sql_result = mysql_query($sql_query_total_raised, $_DB_H) OR die('FAIL UPDATING: '.$sql_query_total_raised);
if ($sql_result AND mysql_num_rows($sql_result)==1) {
	$total = mysql_fetch_array($sql_result ,MYSQL_ASSOC);
	$fp = fopen('../htdocs/raised-domain.inc.html', 'w');
	$raised_string	= number_format($total['total']);
	$raised_percent	= number_format( ($total['total']/$goal*100),2);
	$raised_percent_css = $raised_percent;
	if ($raised_percent_css > 100) $raised_percent_css=100;
	if ($raised_percent_css < 5) $raised_percent_css=5;
	
	fwrite($fp, '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
  "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head><title>Amount Raised</title><link rel="stylesheet" href="bar.css" type="text/css"/><meta http-equiv="refresh" content="60"></head>
<body>
<div class="progress-container">          
    <div style="width: '.ceil($raised_percent_css).'%"><span>'.$raised_percent.'%</span></div>
</div>
</body>');
	fclose($fp);
}
?>