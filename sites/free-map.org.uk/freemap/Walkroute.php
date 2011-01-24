<?php

require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
require_once('Way.php');
require_once('../common/defines.php');

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
    var $pts, $ways, $route;


    function __construct($in,$deserialise=false)
    {
        $this->pts=array();
        $this->ways=array();
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
        $prevPt=null;
        for($i=0; $i<count($points); $i++)
        {
            $prevdist=1000;
            $curPt = array();
            $curPt['way']= 0;
            list($curPt['x'],$curPt['y'])=explode(" ", $points[$i]);

            // Prevent double clicks adding same point twice
            if($prevPt!==null && dist($prevPt['x'],$prevPt['y'],
                                    $curPt['x'],$curPt['y']) < 10)
                continue;

            $ways=get_ways_by_point($curPt['x'],$curPt['y'],100,0);
            if(count($ways)>0)
            {
                $waytoadd=null;
                // Add ways that are found in 2 consecutive points
                for($j=0; $j<count($ways); $j++)
                {
                    for($k=0; $k<count($prevways); $k++)
                    {
                        if($ways[$j]['osm_id']==$prevways[$k]['osm_id'] &&
                        $ways[$j]['dist']+$prevways[$k]['dist'] < $prevdist)
                        {
                            $waytoadd=$ways[$j];
                            $prevdist=$ways[$j]['dist']+$prevways[$k]['dist'];
                        }
                    }
                }

                $curPt['way']=($waytoadd===null ) ?  0 : $waytoadd['osm_id'];
                if($waytoadd!==null&&!isset($waystoadd[$waytoadd['osm_id']]))
                    $waystoadd[$waytoadd['osm_id']] = $waytoadd; 

                if($curPt['way']>0)
                {
                    $waystoadd[$curPt['way']]['end'] = 
                        $waytoadd['posn'];
                }
                if($curPt['way']>0 && $curPt['way']!=$prevPt['way'])
                {
                    for($j=0; $j<count($prevways); $j++)
                    {
                        if($prevways[$j]['osm_id']==$curPt['way'])
                        {
                            $waystoadd[$curPt['way']]['start'] = 
                                $prevways[$j]['posn'];
                            break;
                        }
                    }
                }

                if($i>0 && $prevPt['way']==0 && $curPt['way']==0)
                {
                    $this->route[] = new Point($prevPt['x'],$prevPt['y']);
                }
            } // end if count > 0
            if($i>0 && $prevPt['way']>0 && $curPt['way'] != $prevPt['way'])
            {
                $w=new Way
                        ($waystoadd[$prevPt['way']],true,
                        $waystoadd[$prevPt['way']]['start'],
                                $waystoadd[$prevPt['way']]['end']);
                $this->route[] = $w;
            }
            if($i>0 && count($ways)==0 && $prevPt['way']==0)
            {
                $this->route[] = new Point($prevPt['x'],$prevPt['y']);
            }
            $prevPt = $curPt;
            $prevways=$ways;
        } // end for
        if($prevPt['way']>0)
        {
            $w=new Way
                        ($waystoadd[$prevPt['way']],true,
                        $waystoadd[$prevPt['way']]['start'],
                                $waystoadd[$prevPt['way']]['end']);
            $this->route[] = $w;
        }
		else
		{
			$this->route[] = new Point($prevPt['x'],$prevPt['y']);
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
        foreach($this->route as $r)
        {
            $r->to_xml();
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
            echo "<li>$line</li>\n";
        }
        echo "</ol>\n";
    }

    function to_pdf()
    {
        $pts=array();
        foreach ($this->route as $r)
        {
            $pts=array_merge($pts,$r->getCoords());
        }
        $text=$this->to_text();
        $pdf = new PDF();
        $pdf->SetFont('Arial','B',10);
        $pdf->title="Your walkroute";
        $pdf->SetLeftMargin(10);
        $pdf->AddPage();
        list($w,$h)=get_required_dimensions(get_bounds($pts,14),14);
        $w+=32;
        $h+=32;

        // convert to mm
        // 72dpi (GD resolution) = 2.835 pixels/mm
        $w /= 2.835;
        $h /= 2.835;

        if($w>$h)
        {
            $pageW=($w>190) ? 190:$w;
            $pageH=($w>190) ? 190*($h/$w) : $h;
            $pdf->secondColStart=$pdf->bodystart + $pageH;
        }
        else
        {
            $pageW=($w>90) ? 90:$w;
            $pageH=($w>90) ? 90*($h/$w) : $h;
            $pdf->secondColStart=$pdf->bodystart;
        }


        $pdf->Image(FREEMAP_ROOT."/freemap/route.php".
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
            $pdf->MultiCell(80,5,$line);
            $pdf->Ln();
        }

        $pdf->Output();
    }

    function to_png($w=null,$h=null,$wrpadding=16)
    {
        $pts=array();
        foreach ($this->route as $r)
        {
            $pts=array_merge($pts,$r->getCoords());
        }
        $bounds=get_bounds($pts);
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
        for($i=0;$i<count($pts); $i++)
        {
            $px=(metresToPixel($pts[$i]['x'],14) - 
                metresToPixel($bounds[0],14))+
                $wrpadding;
            $py=(metresToPixel(-$pts[$i]['y'],14)-
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
        //print_r($coords);
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
        for($i=0; $i<count($this->route)-1; $i++)
        {
            $text=array_merge($text,
                $this->route[$i]->toTextFull($this->route[$i+1]));
        }
        
        return array_merge($text,$this->route[$i]->to_text());
    }

    function keypoint_coords()
    {
        $coords=array();
        for($i=0; $i<count($this->route); $i++)
        {
            $coords = array_merge($coords,
                $this->route[$i]->get_key_coords($this->route[$i+1]));
        }
        return $coords;
    }

    function serialise()
    {
        $str="";
        for($i=0; $i<count($this->route); $i++)
        {
            if($i>0)
                $str.=",";
            $str.=$this->route[$i]->serialise();
        }
        return $str;
    }

    function deserialise($str)
    {
        $this->route=array();
        $features=explode(",", $str);
        foreach($features as $f)
        {
            if($f[0]=='P')
            {
                list(,$x,$y) = explode(" ",$f);
                $p =new Point($x,$y);
                $this->route[] = $p;
            }
            elseif($f[0]=='W')
            {
                list(,$osm_id,$start,$end) = explode(" ",$f);
                $this->route[] = new Way((int)$osm_id,true,$start,$end);
            }
        }
    }
}
?>
