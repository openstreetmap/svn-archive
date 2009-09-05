<?
/* REALLY UGLY */

//CONNECT to DB
include('../htdocs/process/db-connect.inc.php');

$sql_query_comments = 'SELECT * FROM `donations` WHERE `processed` =1 ORDER BY `timestamp` DESC';
$sql_result = mysql_query($sql_query_comments, $_DB_H) OR die('FAIL UPDATING: '.$sql_query_comments);
if ($sql_result AND mysql_num_rows($sql_result)>0) {
	$fp = fopen('../htdocs/comments/constributors.inc.html', 'w');
	$count=0;
	while($contrib = mysql_fetch_array($sql_result ,MYSQL_ASSOC)) {	
		$count++;
		fwrite($fp, '<tr'.($count % 2 ? ' class="alt"' : '').'><td class="left"><strong>'.
					($contrib['anonymous']  ? 'Anonymous' : htmlentities($contrib['name'],ENT_QUOTES,'UTF-8')).'</strong>'.
					($contrib['comment'] ? '<br />'.htmlentities($contrib['comment'],ENT_QUOTES,'UTF-8') : '').
					'</td><td class="left">'.
					$contrib['timestamp'].
					'</td><td class="right">'.htmlentities($contrib['currency'],ENT_QUOTES,'UTF-8').' '.number_format($contrib['amount'],2).'</td>'.
					'</tr>'."\n");
	}
	fclose($fp);
}
?>