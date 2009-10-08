<?php

function RoundUp($val, $step)
{
	/*$remain = fmod((double)$val, $step);
	if($remain >= $step || $remain == 0) return $val;
	$roundDown = $val - $remain;
	return $roundDown + $step;*/

	$out = round($val,-log10($step));
	if($out < $val) $out += $step;
	return $out;
}

function RoundDown($val, $step)
{
	/*$remain = fmod((double)$val, $step);
	echo $val.' '.$step.' '.$remain.'<br/>';
	if($remain >= $step || $remain == 0) {echo 'y'; return $val;}
	return $val - $remain;*/

	$out = round($val,-log10($step));
	if($out > $val) $out -= $step;
	return $out;
}

function ListFiles($dir)
{
	$output = array();
	  if(is_dir($dir))
	  {
	    if($handle = opendir($dir))
	    {
	      while(($file = readdir($handle)) !== false)
	      {
		if($file != "." && $file != ".." && $file != "Thumbs.db"/*pesky windows, images..*/)
		{
		  //echo '<a target="_blank" href="'.$dir.$file.'">'.$file.'</a><br>'."\n";
			array_push($output,$file);
		}
	      }
	      closedir($handle);
	    }
	  }
	return $output;
}

function FilterFiles($fileArray,$whitelistExt)
{
	$wl = split(';',$whitelistExt);
	//print_r($wl);
	foreach ($fileArray as $key => $file)
	{
		$path_parts = pathinfo($file);
		//print_r($path_parts);
		$extOk = 0;
		foreach ($wl as $key2 => $ext)
		{
			if(strcasecmp($ext, $path_parts['extension'])==0) $extOk = 1;
		}
		if($extOk == 0) unset($fileArray[$key]);
	}
	return $fileArray;
}

function GetInputVar($var)
{
	if(isset($_GET[$var])) return $_GET[$var];
	if(isset($_POST[$var])) return $_POST[$var];
	
	//Process $_SERVER['argv']
	$argGroups = array();
	for ($i=1;$i<sizeof($_SERVER['argv']);$i++)
	{
		$argGroups = array_merge($argGroups,split('&',$_SERVER['argv'][$i]));
	}

	$argvData = array();
	foreach ($argGroups as $key => $value)
	{
		
		$strpos = strpos($value,'=');
		if($strpos == FALSE)
			$argvData[$value]=NULL;
		else
		{
			$argvData[substr($value,0,$strpos)]=substr($value,$strpos+1);
		}
	}
	
	if(isset($argvData[$var])) return $argvData[$var];
}

function SetTimeOutFromInputVar()
{
	$timeout = GetInputVar('timeout');
	//print_r($timeout);
	if(isset($timeout))
	{
		//echo 'x';
		set_time_limit($timeout);
		//exit();
	}
	//exit();
}

//http://uk3.php.net/manual/en/function.microtime.php
class PageExecutionTimer {
    private $executionTime;
   
    public function __construct() {
        $this->executionTime = microtime(true);
    }
   
    public function GetTime() {
        return microtime(true)-$this->executionTime;
    }

    public function __destruct() {
        //print_r(chr(10).chr(13).(microtime(true)-$this->executionTime));
    }
}

function PackNumberFormat($input)
{
	$out = '';
	if($input >= 0.0) $out .= '+';
	else $out .= '-';
	
	$input	*= 10.0;
	$input = round(abs($input));
	$input = (string)$input;
	while(strlen($out)+strlen($input) < 5) $out .= '0';
	$out .= $input;
	return $out;
}

function SendImageWithTextMessage($message,$width, $height)
{
	// Set the content-type
	header('Content-type: image/png');

	// Create the image
	$im = imagecreatetruecolor($width, $height);

	// Create some colors
	$white = imagecolorallocate($im, 255, 255, 255);
	$grey = imagecolorallocate($im, 128, 128, 128);
	$black = imagecolorallocate($im, 0, 0, 0);
	//imagefilledrectangle($im, 0, 0, 399, 29, $white);

	// The text to draw
	$text = $message;
	// Replace path by your own font path
	//$font = '/usr/share/fonts/truetype/msttcorefonts/arial.ttf';
	$font = '/usr/share/fonts/truetype/ttf-dejavu/DejaVuSans.ttf';

	// Add the text
	imagettftext($im, 20, 0, 10, 20, $white, $font, $text);

	// Using imagepng() results in clearer text compared with imagejpeg()
	imagepng($im);
	imagedestroy($im);
}

?>
