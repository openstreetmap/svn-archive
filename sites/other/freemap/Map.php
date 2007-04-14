<?php
class Map
{
	var $bottomleft, 
		$topright,
		$latscale, // pixels per latitude unit
		$lonscale, // pixels per longitude unit
		$width, // pixels
		$height; // pixels

	function Map ($w, $s, $e, $n, $width, $height)
	{
		$this->bottomleft["long"] = $w;
		$this->bottomleft["lat"] = $s;
		$this->lonscale = $width/($e-$w);
		$this->latscale = $height/($n-$s);
		$this->width = $width; 
		$this->height = $height; 
	}

	function is_valid()
	{
		return $this->width>0 && $this->height>0;
	}

	function get_x($e)
	{
		return round(($e - $this->bottomleft['long']) * $this->lonscale);
	}
	
	function get_y($n)
	{
		return $this->height-
				round(($n-$this->bottomleft['lat']) * $this->latscale);
	}

	function get_point($ll)
	{
		return array ("x" => $this->get_x($ll["long"]), 
						"y" => $this->get_y($ll["lat"]) );
	}

	function get_latlon($pt)
	{
		$lon = $this->bottomleft['long'] +$pt['x']/$this->lonscale;
		$lat = $this->bottomleft['lat'] + 
			($this->height-$pt['y'])/$this->latscale;
		return array ('long' => $lon, 'lat' => $lat); 
	}

	function get_centre()
	{
		return $this->get_latlon
				(array('x'=>$this->width/2,'y'=>$this->height/2));
	}

	function within_map($latlon)
	{
		$pt['x'] = round($this->get_x($latlon['long']));
		$pt['y'] = round($this->get_y($latlon['lat']));
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

	function set_scale($newlonscale, $newlatscale)
	{
		$this->lonscale = $newlonscale;
		$this->latscale = $newlatscale;
	}

	// Return the lat-lon of the bottom left coordinate at a new scale
	// while keeping the centre constant
	function get_new_bottom_left($newlonscale, $newlatscale)
	{
		// Get the centre as a lat-lon
		$centre=$this->get_centre();
			// Coordinates of the new bottom left with respect to the centre
			// as the origin
			$pt['x'] = -$this->width/2; 
			$pt['y'] = $this->height/2;

			// Convert these to a lat-lon and return
			$new_bottom_left['long']=
				$centre['long']+round($pt['x']/($newlonscale/1000));
			$new_bottom_left['lat']=$centre['lat']-
				round($pt['y']/($newlatscale/1000));
		return $new_bottom_left; 
	}

	function centreToBottomLeft()
	{
		$pt['x'] = -$this->width/2; 
		$pt['y'] = 3*($this->height/2);

		// Convert these to a lat-lon and return
		return $this->get_latlon($pt);
	}

	function get_top_right()
	{
		return $this->get_latlon(array("x"=>$this->width,"y"=>0));
	}
}
?>
