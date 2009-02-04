<?
/*
	Update Currency Exchange Rate Table

	UGLY - Quick Dirty Hack - Needs to be replaced ASAP.
	PHP Pear Services_ExchangeRates is dead
*/

require_once 'Services/ExchangeRates.php';
$rates = new Services_ExchangeRates(array(	'thousandsSeparator'=>'',
											'decimalCharacter'=>'.'));
$rateProvider     = $rates->factory('Rates_ECB');
$currencyProvider = $rates->factory('Currencies_UN');
$rates->fetch($rateProvider, $currencyProvider);

//BRAINDEAD CHECK
$GBPEUR = ( (float) $rates->convert('GBP','EUR', 10000));
$EURGBP = ( (float) $rates->convert('EUR','GBP', $GBPEUR));
if ($EURGBP<9980 or $EURGBP>10020) die('BAD Exchange Rate COnversion');


//$_CURRENCIES = array('AUD','CAD','CHF','CZK','DKK','EUR','GBP','HKD','HUF','JPY','NZD','NOK','PLN','SGD','SEK','USD');
$_DB_H = mysql_pconnect('localhost','osm_donate','password');
mysql_select_db('osm_donate', $_DB_H);
mysql_query('SET NAMES \'utf8\'', $_DB_H);


foreach($rates->getRates('GBP') AS $currency => $rate) {
	$sql_replace= 'REPLACE INTO `currency_rates` (`currency`, `rate`) VALUES ("'.$currency.'","'.$rate.'")';
	mysql_query($sql_replace, $_DB_H) OR die('FAIL UPDATING: '.$sql_replace);
}
?>