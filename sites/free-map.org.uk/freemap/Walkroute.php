<?php

require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
require_once('Way.php');

include ('fpdf16/fpdf.php');

class PDF extends FPDF
{

    var $col=0, $title, $secondColStart;

    function Header()
    {
        $this->Cell(80);
        $this->SetFont('Arial','B',18);
        $this->Cell(30,10,$this->title);
        $this->SetFont('Arial','B',10);
        $this->Ln();
        $this->bodystart=$this->GetY();
    }


    function Footer()
    {
    }

    function AcceptPageBreak()
    {
        if($this->col==0)
        {
            $this->col=1;
            $this->SetY(($this->PageNo()==1) ? 
                            $this->secondColStart:$this->bodystart);
            $this->SetX(110);
            return false;
        }
        else
        {
            $this->col=0;
            $this->SetX(0);
            $this->SetY($this->bodystart);
        }
        return true;
    }

    function ShowPicture($image)
    {
        list ($imgw,$imgh,$imgtype, , , , )= getimagesize($image);
        $format= ($imgtype==IMAGETYPE_JPEG) ? "jpg" : "png";
        $w=100;
        $h=100*($imgh/$imgw);
    

        if($this->GetY()+$h > 280)
        {
            $y=$this->bodystart;
            $this->col = ($this->col==1) ? 0:1;
        }
        else
        {
            $y=$this->GetY();
        }
        $x = 10+($this->col*100);
        
        
        $this->Image($image,$x,$y,$w,$h,$format);
        $this->SetX($x);
        $this->SetY($y+$h);    
    }
}

class Walkroute
{
    var $pts, $ways;


    function __construct($in,$deserialise=false)
    {
        if(is_int($in))
        {
            $p=$this->get_route($in);
        }
        elseif(is_string($in))
        {
            if($deserialise===true)
            {
                $this->deserialise($in);
                return;
            }
            else
            {
                $p=$in;
            }
        }
        else
        {
            die("Invalid parameter to Walkroute constructor");
        }

        $points=explode(",",$p);
        $prevways=array();
        $waystoadd=array();
        $this->pts = array();
        for($i=0; $i<count($points); $i++)
        {
            $prevdist=100;
            $this->pts[$i] = array();
            $this->pts[$i]['way']= 0;
            list($this->pts[$i]['x'],$this->pts[$i]['y'])=    
                explode(" ", $points[$i]);
            $ways=get_ways_by_point($this->pts[$i]['x'],
                $this->pts[$i]['y'],100,0);
            if(count($ways)>0)
            {
                $waytoadd=null;
                // Add ways that are found in 2 consecutive points
                for($j=0; $j<count($ways); $j++)
                {
                    for($k=0; $k<count($prevways); $k++)
                    {
                        if($ways[$j]['osm_id']==$prevways[$k]['osm_id'] &&
                        $ways[$j]['dist'] < $prevdist)
                        {
                            $waytoadd=$ways[$j];
                            $prevdist=$ways[$j]['dist'];
                        }
                    }
                }

                $this->pts[$i]['way']=($waytoadd===null ) ? 
                    0 : $waytoadd['osm_id'];
                if($waytoadd!==null&&!isset($waystoadd[$waytoadd['osm_id']]))
                {
                    $waystoadd[$waytoadd['osm_id']] = $waytoadd; 
                    $lastway=$waytoadd['osm_id'];
                }
                if($this->pts[$i-1]['way']>0 && 
                    $this->pts[$i]['way']!=$this->pts[$i-1]['way'])
                {
                    $waystoadd[$this->pts[$i-1]['way']]['end'] = $prevposn;
                }
                if($this->pts[$i]['way']>0 && 
                    $this->pts[$i]['way']!=$this->pts[$i-1]['way'])
                {
                    for($j=0; $j<count($prevways); $j++)
                    {
                        if($prevways[$j]['osm_id']==$this->pts[$i]['way'])
                        {
                            $waystoadd[$this->pts[$i]['way']]['start'] = 
                                $prevways[$j]['posn'];
                            break;
                        }
                    }
                }
                $prevways=$ways;
                if($waytoadd!==null)
                {
                    $prevposn=$waytoadd['posn'];
                } 
            }
            if(isset($waystoadd[$lastway]))
                $waystoadd[$lastway]['end'] = $prevposn;
        }
        foreach($waystoadd as $w)
        {
            $this->ways[$w['osm_id']] = 
                new Way($w,true,$w['start'],$w['end']);
        }
    }

    function get_route($id)
    {
        $result=pg_query("SELECT AsText(route) FROM routes WHERE id=$id");
        $row=pg_fetch_array($result,null,PGSQL_ASSOC);
        if($row)
        {
            $m=array();
            preg_match("/LINESTRING\((.+)\)/",$row['astext'],$m);
            return $m[1];
        }
        return null; 
    }

    function to_xml()
    {
        echo "<route>\n";
        for($i=0; $i<count($this->pts); $i++)
        {
            echo "<pt wayid='".$this->pts[$i][way]."' />\n";
            if($this->pts[$i]['way']==0 && 
                    ($i==count($this->pts)-1 || $this->pts[$i+1]['way']==0))
            {
                echo "<point>".$this->pts[$i][x]." ".
                    $this->pts[$i][y]."</point>\n";
            }

            if($i < count($this->pts)-1 && $this->pts[$i]['way'] != 
                $this->pts[$i+1]['way'] && $this->pts[$i+1]['way']>0)
            {
                $w1=$this->ways[$this->pts[$i+1]['way']];
                $w1->annotated_way_to_xml();
            }
        }
        echo "</route>\n";
    }
    
    function to_html()
    {
		echo "<p><img src='/freemap/route.php?action=get&serialised=".
			urlencode($this->serialise())."&format=png' ".
			"alt='your walk route' /></p><br />";
        $text = $this->to_text();
        echo "<ol>\n";
        foreach($text as $line)
        {
            if(is_array($line))
            {
                echo "<li>$line[general]</li>\n";
                foreach($line['annotations'] as $ann)
                {
                    echo "<li>$ann</li>\n";
                }
            }
            else
            {
                echo "<li>$line</li>\n";
            }
        }
    	echo "</ol>\n";
    }

    function to_pdf()
    {
        $text=$this->to_text();
        $pdf = new PDF();
        $pdf->SetFont('Arial','B',10);
        $pdf->title="Your walkroute";
        $pdf->SetLeftMargin(10);
        $pdf->AddPage();
        list($w,$h)=get_required_dimensions(get_bounds($this->pts,14),14);
        $w+=32;
        $h+=32;
        if($w>$h)
        {
            $pageW=($w>190) ? 190:0;
            $pageH=($w>190) ? 190*($h/$w) : 0;
            $pdf->secondColStart=$pdf->bodystart + $pageH;
        }
        else
        {
            $pageW=($w>90) ? 90:0;
            $pageH=($w>90) ? 90*($h/$w) : 0;
            $pdf->secondColStart=$pdf->bodystart;
        }
        $pdf->Image("http://www.free-map.org.uk/freemap/route.php".
            "?format=png&action=get&serialised=".
            urlencode($this->serialise()), 10,20,$pageW,$pageH,"png");
        $pdf->SetXY(10,$pdf->bodystart+$pageH);
        $pdf->SetFont('Arial','B',10);
        $pdf->MultiCell(100,5,"Maps copyright OpenStreetMap contributors ".
                "and licenced under CC-by-SA 2.0");
        $pdf->SetFont('Arial','B',16);
        $pdf->MultiCell(100,10,"Notes");
        $pdf->SetFont('Arial','',10);
        $count=1;
        foreach($text as $line)
        {
            $pdf->SetX($pdf->col==1 ? 110: 10);
            $pdf->SetDrawColor(0,0,255);
            $pdf->SetFillColor(0,0,255);
            $pdf->SetTextColor(255,255,255);
            $pdf->Cell($pdf->GetStringWidth($count)*2,5,$count++,1,0,'L',true);
            $pdf->SetDrawColor(0,0,0);
            $pdf->SetFillColor(255,255,255);
            $pdf->SetTextColor(0,0,0);

            if(is_array($line))
            {
                $pdf->MultiCell(80,5,$line['general']);
                $pdf->Ln();
                foreach($line['annotations'] as $ann)
                {
                    $pdf->SetX($pdf->col==1 ? 110: 10);
                    $pdf->SetDrawColor(0,0,255);
                    $pdf->SetFillColor(0,0,255);
                    $pdf->SetTextColor(255,255,255);
                    $pdf->Cell
                        ($pdf->GetStringWidth($count)*2,
                        5,$count++,1,0,'L',true);
                    $pdf->SetDrawColor(0,0,0);
                    $pdf->SetFillColor(255,255,255);
                    $pdf->SetTextColor(0,0,0);
                    $pdf->MultiCell(80,5,$ann);
                    $pdf->Ln();
                }
            }
            else
            {
                $pdf->MultiCell(80,5,$line);
            }
        }

        $pdf->Output();
    }

    function to_png($w=null,$h=null,$wrpadding=16)
    {
        $bounds=get_bounds($this->pts);
        $mpx=($bounds[0]+$bounds[2]) / 2;
        $mpy=($bounds[1]+$bounds[3]) / 2;
        if($w===null || $h===null)
            list($w,$h)=get_required_dimensions($bounds,14);
        $w+=$wrpadding*2;
        $h+=$wrpadding*2;
        $im=ImageCreateTrueColor($w,$h);
        $backg=ImageColorAllocate($im,220,220,220);
        $linecol=ImageColorAllocate($im,255,255,0);
        $anncol=ImageColorAllocate($im,0,0,255);
        $textcol=ImageColorAllocate($im,255,255,255);
        render_tiles($im,$mpx,$mpy,14,$w,$h);
        for($i=0;$i<count($this->pts); $i++)
        {
            $px=(metresToPixel($this->pts[$i]['x'],14) - 
                metresToPixel($bounds[0],14))+
                $wrpadding;
            $py=(metresToPixel(-$this->pts[$i]['y'],14)-
                metresToPixel(-$bounds[3],14))+
                $wrpadding;
            if(isset($prevpx) && isset($prevpy))
            {
                ImageSetThickness($im,3);
                ImageLine($im,$prevpx,$prevpy,$px,$py,$linecol);
            }
            $prevpx=$px;
            $prevpy=$py;
        }
        $coords=$this->keypoint_coords();
        $count=1;
        foreach($coords as $coord)
        {
            $px=(metresToPixel($coord['x'],14) - metresToPixel($bounds[0],14))+
                $wrpadding;
            $py=(metresToPixel(-$coord['y'],14)-metresToPixel(-$bounds[3],14))+
            $wrpadding;
            ImageFilledEllipse($im,$px,$py,10,10,$anncol);
            ImageString($im,2,$px-5,$py-5,$count++,$textcol);
        }
        ImagePNG($im);
        ImageDestroy($im);
    }

    function to_text()
    {
        $text=array();
        for($i=0; $i<count($this->pts); $i++)
        {
            if($this->pts[$i]['way']==0 && $i>0) 
            {
                    $dist=round
                    (dist($this->pts[$i-1]['x'],$this->pts[$i-1]['y'],
                        $this->pts[$i]['x'],$this->pts[$i]['y']) / 1000, 2);
                    $dir=compass_direction
                    (get_bearing($this->pts[$i]['x']-$this->pts[$i-1]['x'],
                              $this->pts[$i]['y']-$this->pts[$i-1]['y']));
                    $text[] = "Continue $dir for $dist km";
            }

            if($i < count($this->pts)-1 && $this->pts[$i]['way'] 
                != $this->pts[$i+1]['way'] &&
                $this->pts[$i+1]['way']>0)
            {
                $w1=$this->ways[$this->pts[$i+1]['way']];
                $text[] = $w1->annotated_way_to_text( );
            }
        }
        return $text;
    }

    function keypoint_coords()
    {
        $coords=array();
        for($i=0; $i<count($this->pts); $i++)
        {
            if ($i<count($this->pts)-1 && $this->pts[$i+1]['way']==0)
            {
                $coords[] = $this->pts[$i];
            }
            elseif ($i<count($this->pts)-1 && 
                $this->pts[$i]['way'] != $this->pts[$i+1]['way'] &&
                    $this->pts[$i+1]['way'] > 0)
            {
                $coords[] = $this->pts[$i];
                $w=$this->ways[$this->pts[$i+1]['way']];
                $coords=array_merge($coords, $w->get_annotation_coords());
            }
        }
        return $coords;
    }

    function serialise()
    {
        $str="";
        for($i=0; $i<count($this->pts); $i++)
        {
            if($i>0)
                $str.=",";
            $str .= round($this->pts[$i]['x'],0) ." ".
                    round($this->pts[$i]['y'],0)." ".
                    $this->pts[$i]['way'];
        }
        $str.=";";
        $first=true;
        foreach($this->ways as $id=>$w)
        {
            if(!$first)
                $str.=",";
            else
                $first=false;
            $str.=$id." ".round($w->getStart(),2)." ".  round($w->getEnd(),2);
        }
        return $str;
    }

    function deserialise($str)
    {
        $this->pts=array();
        $this->ways=array();
        list($ptinfo,$wayinfo) = explode(";", $str);
        $pts=explode(",",$ptinfo);
        $wys=explode(",",$wayinfo);
        for($i=0; $i<count($pts); $i++)
        {
            $this->pts[$i]=array();
            list($this->pts[$i]['x'],$this->pts[$i]['y'],
                    $this->pts[$i]['way']) = explode(" ",$pts[$i]);
        }
        for($i=0; $i<count($wys); $i++)
        {
            list($id,$start,$end) = explode(" ", $wys[$i]);
            $this->ways[$id]=new Way((int)$id,true,$start,$end);
        }
    }
}
?>
