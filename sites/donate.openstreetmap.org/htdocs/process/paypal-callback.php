<?
//Extensively based on Very_Horrible Paypal sample code.

// read the post from PayPal system and add 'cmd'
$req = 'cmd=_notify-validate';
foreach ($_POST as $key => $value) {
	$value = get_magic_quotes_gpc() ? stripslashes($value) : $value;
	$req .= '&'.$key.'='.urlencode($value);
}

// post back to PayPal system to validate
$header .= "POST /cgi-bin/webscr HTTP/1.0\r\n";
$header .= "Content-Type: application/x-www-form-urlencoded\r\n";
$header .= 'Content-Length: ' . strlen($req) . "\r\n\r\n";
$fp = fsockopen ('ssl://www.paypal.com', 443, $errno, $errstr, 30);

// assign posted variables to local variables
$item_name			= get_magic_quotes_gpc() ? stripslashes($_POST['item_name'])		: $_POST['item_name'] ;
$item_number		= get_magic_quotes_gpc() ? stripslashes($_POST['item_number'])		: $_POST['item_number'];
$payment_status		= get_magic_quotes_gpc() ? stripslashes($_POST['payment_status'])	: $_POST['payment_status'];
$payment_amount		= get_magic_quotes_gpc() ? stripslashes($_POST['mc_gross'])			: $_POST['mc_gross'];
$payment_currency	= get_magic_quotes_gpc() ? stripslashes($_POST['mc_currency'])		: $_POST['mc_currency'];
$txn_id				= get_magic_quotes_gpc() ? stripslashes($_POST['txn_id'])			: $_POST['txn_id'];
$receiver_email		= get_magic_quotes_gpc() ? stripslashes($_POST['receiver_email'])	: $_POST['receiver_email'];
$payer_email		= get_magic_quotes_gpc() ? stripslashes($_POST['payer_email'])		: $_POST['payer_email'];
$business			= get_magic_quotes_gpc() ? stripslashes($_POST['business'])			: $_POST['business'];
$option_selection1	= get_magic_quotes_gpc() ? stripslashes($_POST['option_selection1']) : $_POST['option_selection1'];
$option_name1		= get_magic_quotes_gpc() ? stripslashes($_POST['option_name1'])		 : $_POST['option_name1'];

if (!$fp) {
	// HTTP ERROR
	error_log('Verify Failed Callback: '.var_export($_POST, TRUE));
} else {
	fputs ($fp, $header . $req);
	while (!feof($fp)) {
		$res = fgets ($fp, 1024);
		if (strcmp ($res, 'VERIFIED') == 0) {
			// check the payment_status is Completed
			// check that txn_id has not been previously processed
			// check that receiver_email is your Primary PayPal email
			// check that payment_amount/payment_currency are correct
			// process payment
			//CONNECT to DB
			
			$_DB_H = mysql_pconnect('localhost','osm_donate','password');
			mysql_select_db('osm_donate', $_DB_H);
			mysql_query('SET NAMES \'utf8\'', $_DB_H);
			if ($payment_status == 'Completed' AND $option_name1=='contribution_tracking_id' AND $business == 'treasurer@openstreetmap.org') {
				$sql_update_donation = 'UPDATE `donations` SET `processed` = 1 WHERE `uid`="'.mysql_real_escape_string($option_selection1, $_DB_H).'" LIMIT 1';
				mysql_query($sql_update_donation, $_DB_H) OR error_log('SQL FAIL: '.$sql_update_donation);
			}

			$sql_insert_callback = 'INSERT INTO `paypal_callbacks` (`amount`, `currency` , `donation_id`, `callback`) VALUES (\''.
									mysql_real_escape_string($payment_amount, $_DB_H).'\',\''.
									mysql_real_escape_string($payment_currency, $_DB_H).'\',\''.
									mysql_real_escape_string($option_selection1, $_DB_H).'\',\''.
									mysql_real_escape_string(serialize($_POST), $_DB_H).
									'\')';
			mysql_query($sql_insert_callback, $_DB_H) OR error_log('SQL FAIL: '.$sql_insert_callback);
		} else if (strcmp ($res, 'INVALID') == 0) {
			// log for manual investigation
			error_log('Invalid Callback: '.var_export($_POST, TRUE));
		}
	}
	fclose ($fp);
}
?>