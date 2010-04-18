<?php

// dashes in gd
require_once('defines.php');

class Painter 
{

}

class GDPainter extends Painter
{
	var $colours, $im, $backcol;

	function GDPainter()
	{

	}

	function createImage($w,$h,$r,$g,$b)
	{
		$this->im = ImageCreateTrueColor($w,$h);
		$this->backcol =  ImageColorAllocate($this->im,$r,$g,$b);
		ImageFill($this->im,$w/2,$h/2,$this->backcol);
		ImageColorTransparent($this->im,$this->backcol);
		return $this->backcol;
	}

	function createFromFile($filename)
	{
		//Detect type
		$path_parts = pathinfo($filename);
		$this->im = NULL;
		if(strcasecmp($path_parts['extension'],"jpeg")==0 ||
			strcasecmp($path_parts['extension'],"jpg")==0)
			$this->im = imagecreatefromjpeg($filename);
		if(strcasecmp($path_parts['extension'],"gif")==0)
			$this->im = imagecreatefromgif($filename);
		if(strcasecmp($path_parts['extension'],"png")==0)
			$this->im = imagecreatefrompng($filename);
		if($this->im == NULL) throw new Exception('Image extension '.$path_parts['PATHINFO_EXTENSION'].' not recognised.');

		$this->backcol =  ImageColorAllocate($this->im,$r,$g,$b);
		return $this->backcol;
	}

	function getColour($r, $g, $b)
	{
		return ImageColorAllocate($this->im,$r,$g,$b);
	}

	function drawText($x,$y,$fontsize,$text, $colour)
	{
		ImageTTFText($this->im, $fontsize, 0, $x, $y,
								$colour, 
							TRUETYPE_FONT, $text);
	}

	function imageCopyResized($src_image, $dst_x, $dst_y, $src_x, $src_y, $dst_w, $dst_h, $src_w, $src_h)
	{
		if(USE_RESAMPLING)
			imagecopyresized($this->im,$src_image->im, $dst_x, $dst_y, $src_x, $src_y, $dst_w, $dst_h, $src_w, $src_h);
		else
			imagecopyresampled($this->im,$src_image->im, $dst_x, $dst_y, $src_x, $src_y, $dst_w, $dst_h, $src_w, $src_h);
	}

	function getImageHeight()
	{
		return imagesy($this->im);
	}

	function getImageWidth()
	{
		return imagesx($this->im);
	}

	function drawImage($x,$y,$imgfile, $format)
	{
		$imgsize=getimagesize($imgfile);
		$w = $imgsize[0];
		$h = $imgsize[1];
		if($format=="jpeg")
			$img = ImageCreateFromJPEG($imgfile);
		else
			$img = ImageCreateFromPNG($imgfile);
		ImageCopy($this->im,$img, $x, $y, 0, 0, $w, $h);
		ImageDestroy($img);
	}

	function drawLine($x1,$y1,$x2,$y2,$colour,$thickness)
	{
		ImageSetThickness($this->im, $thickness);
		ImageLine($this->im,$x1,$y1,$x2,$y2, $colour);
	}

	function drawPolygon ($p, $colour)
	{
		ImageFilledPolygon($this->im,$p,count($p)/2,$colour);
	}

	// $dash is the dash pattern in the XML e.g. 2,2
	function drawDashedLine($x1,$y1,$x2,$y2,$dash, $colour, $thickness)
	{
		$dashpattern = $this->makeDashPattern($dash,$colour);
		ImageSetThickness($this->im, $thickness);
		ImageSetStyle($this->im,  $dashpattern);
		ImageLine($this->im,$x1,$y1,$x2,$y2,IMG_COLOR_STYLED);
	}

	function makeDashPattern($dash, $colour)
	{
		if($dash && $colour)
		{
			list($on,$off)=explode(",",$dash);
			$dashpattern=array();
			for($count2=0; $count2<$on; $count2++)
				$dashpattern[$count2] = $colour;
			for($count2=0; $count2<$off;$count2++)
				$dashpattern[$on+$count2] = IMG_COLOR_TRANSPARENT;
			return $dashpattern;
		}
		return null;
	}

	# 16/11/04 new version for truetype fonts.
	function drawMultiword($x,$y,$multiword,$fontsize,$colour)
	{
		
		$multiword_arr = explode(' ',$multiword);
		
		for($count=0; $count<count($multiword_arr)-1; $count++)
		{
			ImageTTFText($this->im, $fontsize, 0, $x, $y, 
							$colour, 
							TRUETYPE_FONT,
							$multiword_arr[$count]);

			// Get the height of the next word, so we know how far down to
			// draw it.	
			$bbox = ImageTTFBBox
			($fontsize,0,TRUETYPE_FONT,$multiword_arr[$count+1]);
			$y += ($bbox[1]-$bbox[7])+FONT_MARGIN;
		}
			
		// Finally draw the last word
		@ImageTTFText($this->im, $fontsize, 0, $x, $y, $colour,
							TRUETYPE_FONT, $multiword_arr[$count]);
	} 

	function angleText ($x1,$y1,$x2,$y2, $fontsize, $colour, $text)
	{
		$angle=slope_angle($x1, $y1, $x2, $y2);
		$ix = $x2 > $x1 ? $x1:$x2;
		$iy= $x2 > $x1 ? $y1:$y2;
		ImageTTFText($this->im, $fontsize, -$angle, $ix, $iy,
						$colour, TRUETYPE_FONT, $text);
		return $i;
	}

	function drawFilledRectangle($x1,$y1,$x2,$y2,$colour)
	{
		ImageFilledRectangle($this->im,$x1,$y1,$x2,$y2,$colour);
	}

	function getTextDimensions($fontsize,$text)
	{
		$bbox = ImageTTFBBox($fontsize,0,TRUETYPE_FONT,$text);
		return array 
			( line_length($bbox[6],$bbox[7],$bbox[4],$bbox[5]),
			  line_length($bbox[6],$boox[7],$bbox[0],$bbox[1]) );
	}

	function renderImage()
	{
		ImagePNG($this->im);
	}
	function renderImageJpeg()
	{
		imagejpeg($this->im);
	}

	function crop($x,$y,$w,$h)
	{
		$newim = ImageCreateTrueColor($w,$h);
		$backcol =  $this->backcol; 
		ImageFill($newim,$w/2,$h/2,$backcol);
		ImageColorTransparent($newim,$backcol);
		ImageCopyMerge($newim,$this->im,0,0,$x,$y,$w,$h,100);
		ImageDestroy($this->im);
		$this->im = $newim;
	}

	function saveToFile($filename)
	{
		imagejpeg  ( $this->im ,$filename);
	}

}

class MagickPainter extends Painter
{
	var $wand, $dwand, $bgcolour;

	function MagickPainter()
	{
		$this->wand = NewMagickWand();
	}

	function createImage($w,$h,$r,$g,$b)
	{
		MagickNewImage($this->wand,$w,$h,htmlcolour($r,$g,$b));
		MagickSetImageFormat($this->wand,"png");
		$this->dwand = NewDrawingWand();
		DrawSetStrokeLineCap($this->dwand,MW_RoundCap);
		DrawSetFont($this->dwand,TRUETYPE_FONT);
		$this->bgcolour = htmlcolour($r,$g,$b);
	}

	function getColour($r, $g, $b)
	{
		return NewPixelWand(htmlcolour($r,$g,$b));
	}

	function drawText($x,$y,$fontsize,$text, $pwand)
	{
		if($text!="") 
		{
			DrawSetFontSize($this->dwand,$fontsize);
			DrawSetStrokeColor($this->dwand,$pwand); 
			DrawSetFontWeight($this->dwand,100);
			DrawAnnotation($this->dwand,$x,$y,$text); 
		}
	}

	function drawImage($x,$y,$imgfile, $format)
	{
		$imgwand = NewMagickWand();
		MagickReadImage($imgwand,$imgfile);
		$w = MagickGetImageWidth($imgwand);
		$h = MagickGetImageHeight($imgwand);

		/*
		DrawComposite($this->dwand, MW_CopyCompositeOp,
					$x, $y, $w, $h, $imgwand);
		*/
		MagickCompositeImage($this->wand,$imgwand,MW_CopyCompositeOp,$x,$y);
	}

	function drawLine($x1,$y1,$x2,$y2,$pwand,$thickness)
	{
		DrawSetStrokeColor($this->dwand,$pwand);
		DrawSetStrokeWidth($this->dwand,$thickness);
		DrawLine($this->dwand,$x1,$y1,$x2,$y2);
	}

	function drawPolygon ($p, $pwand)
	{
		DrawSetFillColor($this->dwand, $pwand);
		DrawPolygon($this->dwand,$p);
	}

	function drawDashedLine($x1,$y1,$x2,$y2,$dash, $pwand, $thickness)
	{
		//$dashpattern = explode(",",$dash);
		//DrawSetStrokeDashArray($this->dwand, $dashpattern);
		$this->drawLine($x1,$y1,$x2,$y2,$pwand,$thickness);
		//DrawSetStrokeDashArray($this->dwand); // remove the dash pattern
	}

	# 16/11/04 new version for truetype fonts.
	function drawMultiword($x,$y,$multiword,$fontsize,$pwand)
	{
		
		$multiword_arr = explode(' ',$multiword);
		
		for($count=0; $count<count($multiword_arr); $count++)
		{
			if($multiword_arr[$count]!="") 
			{
				// draw text	
				$this->drawText($x,$y,$fontsize,$multiword_arr[$count],$pwand);

				// Get the height of the next word, so we know how far down to
				// draw it.	

				$y += MagickGetStringHeight($this->wand,$this->dwand,
										$multiword_arr[$count]); 
			}
		}
	} 

	function angleText ($x1, $y1, $x2, $y2, $fontsize, $pwand, $text)
	{
		$angle=slope_angle($x1, $y1, $x2, $y2);
		$ix = $x2 > $x1 ? $x1:$x2;
		$iy= $x2 > $x1 ? $y1:$y2;
		DrawTranslate($this->dwand,$ix,$iy);
		DrawRotate($this->dwand,$angle);
		$this->drawText(0,0,$fontsize,$text,$pwand);
		DrawRotate($this->dwand,-$angle);
		DrawTranslate($this->dwand,-$ix,-$iy);
		return $i;
	}

	function drawFilledRectangle($x1,$y1,$x2,$y2,$colour)
	{
		DrawSetFillColor($this->dwand,$colour);
		DrawRectangle($this->dwand,$x1,$y1,$x2,$y2);
	}

	function getTextDimensions($fontsize,$text)
	{
		// Fatal errors should be restricted to exceptional conditions,
		// surely???
		if($text!="") 
		{
			$f = DrawGetFontSize($this->dwand);
			DrawSetFontSize($this->dwand,$fontsize);
			// I presume this uses the current font size
			return array 
				( MagickGetStringWidth($this->wand,$this->dwand,$text),
			 	MagickGetStringHeight($this->wand,$this->dwand,$text ) );
			DrawSetFontSize($this->dwand,$f);
		}
		return null;
	}

	function renderImage()
	{
		MagickEchoImageBlob($this->wand);
		DestroyDrawingWand($this->dwand);
		DestroyMagickWand($this->wand);
	}

	function crop($x,$y,$w,$h)
	{
		MagickDrawImage($this->wand,$this->dwand);
		MagickCropImage($this->wand,$w,$h,$x,$y);
	}

	function saveToFile($filename)
	{
		
	}
}

function htmlcolour($r,$g,$b)
{
	return sprintf("#%02x%02x%02x", $r, $g, $b);
}

?>
