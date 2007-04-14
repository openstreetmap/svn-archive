<?php
class Map
{
	var $bottomleft, 
		$topright,
		$nscale, // pixels per nitude unit
		$escale, // pixels per eitude unit
		$width, // pixels
		$height; // pixels

	function Map ($w, $s, $e, $n, $width, $height)
	{
		$this->bottomleft["e"] = $w;
		$this->bottomleft["n"] = $s;
		$this->escale = $width/($e-$w);
		$this->nscale = $height/($n-$s);
		$this->width = $width; 
		$this->height = $height; 
	}

	function is_valid()
	{
		return $this->width>0 && $this->height>0;
	}

	function get_x($e)
	{
		return round(($e - $this->bottomleft['e']) * $this->escale);
	}
	
	function get_y($n)
	{
		return $this->height-
				round(($n-$this->bottomleft['n']) * $this->nscale);
	}

	function get_point($ll)
	{
		return array ("x" => $this->get_x($ll["e"]), 
						"y" => $this->get_y($ll["n"]) );
	}

	function get_en($pt)
	{
		$e = $this->bottomleft['e'] +$pt['x']/$this->escale;
		$n = $this->bottomleft['n'] + 
			($this->height-$pt['y'])/$this->nscale;
		return array ('e' => $e, 'n' => $n); 
	}

	function get_centre()
	{
		return $this->get_en
				(array('x'=>$this->width/2,'y'=>$this->height/2));
	}

	function within_map($en)
	{
		$pt['x'] = round($this->get_x($en['e']));
		$pt['y'] = round($this->get_y($en['n']));
		return $this->pt_within_map($pt);
	}

	function pt_within_map($pt)
	{
		return $pt['x']>=0 && $pt['y']>=0 && 
				$pt['x']<$this->width && $pt['y']<$this->height;
	}

	function pt_right_of_map ($pt)
	{
		return $pt['x'] >= $this->width;
	}

	function pt_below_map ($pt)
	{
		return $pt['y'] >= $this->height;
	}

	function set_scale($newescale, $newnscale)
	{
		$this->escale = $newescale;
		$this->nscale = $newnscale;
	}

	// Return the n-e of the bottom left coordinate at a new scale
	// while keeping the centre constant
	function get_new_bottom_left($newescale, $newnscale)
	{
		// Get the centre as a n-e
		$centre=$this->get_centre();
			// Coordinates of the new bottom left with respect to the centre
			// as the origin
			$pt['x'] = -$this->width/2; 
			$pt['y'] = $this->height/2;

			// Convert these to a n-e and return
			$new_bottom_left['e']=
				$centre['e']+round($pt['x']/($newescale/1000));
			$new_bottom_left['n']=$centre['n']-
				round($pt['y']/($newnscale/1000));
		return $new_bottom_left; 
	}

	function centreToBottomLeft()
	{
		$pt['x'] = -$this->width/2; 
		$pt['y'] = 3*($this->height/2);

		// Convert these to a n-e and return
		return $this->get_en($pt);
	}

	function get_top_right()
	{
		return $this->get_en(array("x"=>$this->width,"y"=>0));
	}

	function npeSuitable()
	{
		//return $this->width==125 && $this->height==125;
		return true;
	}
}
?>
