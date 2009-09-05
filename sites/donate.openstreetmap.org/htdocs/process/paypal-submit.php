<?
/* Paypal Submission 
//FIXME add paypal language support
lc = AU, DE FR IT GB ES US
*/

ob_start();

//CONNECT to DB
include('db-connect.inc.php');

$data = array();
$data['comment']		= (get_magic_quotes_gpc() ? stripslashes($_POST['comment'])			: $_POST['comment']			);
$data['comment-option']	= (get_magic_quotes_gpc() ? stripslashes($_POST['comment-option'])	: $_POST['comment-option']	);
$data['anonymous'] 		= ($data['comment-option'] == 'comment' ? '0' : '1');
$data['amount']			= (get_magic_quotes_gpc() ? stripslashes($_POST['amountGiven'])		: $_POST['amountGiven']		);
$data['currency']		= (get_magic_quotes_gpc() ? stripslashes($_POST['currency_code'])	: $_POST['currency_code']	);

$data['target']		= (get_magic_quotes_gpc() ? stripslashes($_POST['target'])	: $_POST['target']	);
if (empty($data['target'])) $data['target'] = 'default';


$sql_insert =	'INSERT INTO `donations` (`amount`,`currency`,`anonymous`,`comment`,`target`) VALUES (\''.
					mysql_real_escape_string($data['amount'],		$_DB_H).'\',\''.
					mysql_real_escape_string($data['currency'],		$_DB_H).'\',\''.
					mysql_real_escape_string($data['anonymous'],	$_DB_H).'\',\''.
					mysql_real_escape_string($data['comment'],		$_DB_H).'\',\''.
					mysql_real_escape_string($data['target'],		$_DB_H).
				'\')';
$sql_insert_result = mysql_query($sql_insert, $_DB_H) OR error_log('SQL FAIL: '.$sql_insert);
$sql_insert_id = mysql_insert_id($_DB_H);

$_PAYPAL_URL = 'https://www.paypal.com/cgi-bin/webscr';

$paypal_fields = array();
$paypal_fields['on0']			= 'contribution_tracking_id';
$paypal_fields['os0']			= $sql_insert_id;
$paypal_fields['business']		= 'treasurer@openstreetmap.org';
$paypal_fields['item_name']		= 'One-time donation';
$paypal_fields['item_number']	= 'DONATE-Domain-2009';
$paypal_fields['cmd']			= '_xclick';
$paypal_fields['no_note']		= '0';
$paypal_fields['notify_url']	= 'http://donate.openstreetmap.org/process/paypal-callback.php';
$paypal_fields['return']		= 'http://wiki.openstreetmap.org/wiki/DonationThankYou';
$paypal_fields['currency_code']	= (empty($data['currency']) ?  'GBP' : $data['currency']);
$paypal_fields['amount']		= (double) $data['amount'];
$paypal_fields['charset']		= 'utf-8';
$pp_url_part=array();
foreach($paypal_fields AS $pp_key => $pp_value) {
	$pp_url_part[] = $pp_key.'='.urlencode($pp_value);
}
header('Location: '.$_PAYPAL_URL.'?'.implode('&',$pp_url_part));
exit();
?>