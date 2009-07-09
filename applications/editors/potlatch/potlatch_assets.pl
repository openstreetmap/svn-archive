
	# **********************************************
	# Line width scaling factor
	# For current Ming, set this to 1/20.
	# For older Ming (e.g. 0.3), set this to 1.

	$cw=1/20;

	# **********************************************

	# ----- Export symbols

	#		Empty movie-clips for prototypes
	
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"way");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"relation");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"keyvalue");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"relmember");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"propwindow");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"presetmenu");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"menu");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"checkbox");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"radio");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"auto");
	$ec=new SWF::MovieClip(); $ec->nextFrame(); $m->addExport($ec,"modal");

	#		POI icons
if (0==1) {	
	for ($i=0; $i<8; $i++) {
		for ($j=0; $j<8; $j++) {
			$ec=new SWF::MovieClip();
			$di=$ec->add(new SWF::Bitmap("icons/icon$i$j.dbl"));
			$di->moveTo(-12,-12);
			$ec->nextFrame();
			$m->addExport($ec,"poi_$i$j");
		}
	}
}
	#		Radio buttons
	
	$ec=new SWF::MovieClip(); $ch=new SWF::Shape();
	$ch->setRightFill(0xBB,0xBB,0xBB); $ch->movePenTo(5,5); $ch->setLine(20*$cw,0,0,0); $ch->drawCircle(6);
	$ec->add($ch); $ec->nextFrame(); $m->addExport($ec,"radio_off");

	$ec=new SWF::MovieClip(); $ch=new SWF::Shape();
	$ch->setRightFill(0xBB,0xBB,0xBB); $ch->movePenTo(5,5); $ch->setLine(20*$cw,0,0,0); $ch->drawCircle(6);
	$ch->setRightFill(0   ,0   ,0   ); $ch->movePenTo(5,5); $ch->setLine(20*$cw,0,0,0); $ch->drawCircle(3);
	$ec->add($ch); $ec->nextFrame(); $m->addExport($ec,"radio_on");

	#		Whirling 'in progress' animation
	
	$a=3.1415926/6;
	$ec=new SWF::MovieClip();
	for ($i=0; $i<12; $i++) {
		$ch=new SWF::Shape();
		for ($j=0; $j<12; $j++) {
			$t=$i-$j; if ($t<0) { $t+=12; }
			$t=$t*15.5; $ch->setLine(50*$cw,$t,$t,$t);
			$ch->movePenTo(cos($j*$a)*5,sin($j*$a)*5);
			$ch->drawLineTo(cos($j*$a)*10,sin($j*$a)*10);
		}
		$ec->add($ch);
		$ec->nextFrame(); $ec->nextFrame();
		$ec->nextFrame(); $ec->nextFrame();
	}
	$m->addExport($ec,"whirl");

	#		Photo
	
	$ec=new SWF::MovieClip();
	$ch=new SWF::Shape();

	$ch->setRightFill(127,255,127);
	$ch->setLine(20*$cw,127,255,127);
	$ch->movePenTo(-6,-6);
	$ch->drawLineTo(6,-6); $ch->drawLineTo(6,6);
	$ch->drawLineTo(-6,6); $ch->drawLineTo(-6,-6);
	$ec->add($ch);

	$ch=new SWF::Shape();
	$ch->setRightFill(0,0,0);
	$ch->movePenTo(-6,-6);
	$ch->drawLineTo(6,-6); $ch->drawLineTo(6,6);
	$ch->drawLineTo(-6,6); $ch->drawLineTo(-6,-6);
	
	for ($i=-5.5; $i<=3.5; $i+=3) {
		for ($j=-5; $j<=3.5; $j+=8) {
			$ch->setRightFill(255,255,255);
			$ch->movePenTo($j,$i);
			$ch->drawLineTo($j,$i+2);
			$ch->drawLineTo($j+2,$i+2);
			$ch->drawLineTo($j+2,$i);
			$ch->drawLineTo($j,$i);
		}
	}
	for ($i=-5; $i<3.5; $i+=6) {
		$ch->setRightFill(255,255,255);
		$ch->movePenTo(-2,$i);
		$ch->drawLineTo(-2,$i+4);
		$ch->drawLineTo(-2+4,$i+4);
		$ch->drawLineTo(-2+4,$i);
		$ch->drawLineTo(-2,$i);
	}
	$ec->add($ch);
	$ec->nextFrame();
	$m->addExport($ec,"photo");

	#		POI
	
	$ec=new SWF::MovieClip();
	$ch=new SWF::Shape();
	$ch->setRightFill(0,155,0);
	$ch->setLine(20*$cw,0,0,0);
	$ch->drawCircle(4);
	$ec->add($ch);
	$ec->nextFrame();
	$m->addExport($ec,"poi");

	#		POI in way
	
	$ec=new SWF::MovieClip();
	$ch=new SWF::Shape();
	$ch->setRightFill(0,0,0);
	$ch->setLine(20*$cw,0,0,0);
	$ch->drawCircle(3);
	$ec->add($ch); $ec->nextFrame();
	$m->addExport($ec,"poiinway");

	#		Anchor (selected)

	$ec=new SWF::MovieClip();
	$ch=new SWF::Shape();
	$ch->setRightFill(255,0,0); $ch->movePenTo(-2,-2);
	$ch->drawLine( 4,0); $ch->drawLine(0, 4);
	$ch->drawLine(-4,0); $ch->drawLine(0,-4);
	$ec->add($ch);
	$ec->nextFrame();
	$m->addExport($ec,"anchor");

	$ec=new SWF::MovieClip();
	$c2=new SWF::Shape();
	$c2->movePenTo(-3,-3);
	$c2->setLine(20*$cw,0,0,0);
	$c2->drawLine( 6,0); $c2->drawLine(0, 6);
	$c2->drawLine(-6,0); $c2->drawLine(0,-6);
	$ec->add($ch); $ec->add($c2);
	$ec->nextFrame();
	$m->addExport($ec,"anchor_junction");

	#		Anchor (mouseover)

	$ec=new SWF::MovieClip();
	$ch=new SWF::Shape();
	$ch->setRightFill(0,0,255); $ch->movePenTo(-2,-2);
	$ch->drawLine( 4,0); $ch->drawLine(0, 4);
	$ch->drawLine(-4,0); $ch->drawLine(0,-4);
	$ec->add($ch); $ec->nextFrame();
	$m->addExport($ec,"anchorhint");

	$ec=new SWF::MovieClip();
	$ec->add($ch); $ec->add($c2);
	$ec->nextFrame();
	$m->addExport($ec,"anchorhint_junction");

	#		Zoom in

	$ec=new SWF::MovieClip();
	$bq=new SWF::Shape();
	$bq->setRightFill($bq->addFill(0,0,0x8b));
	$bq->movePenTo(0,20);
	$bq->drawLineTo(0,10);
	$bq->drawCurveTo(0,0,10,0);
	$bq->drawCurveTo(20,0,20,10);
	$bq->drawLineTo(20,20);
	$bq->drawLineTo(0,20);
	$bq->setLine(50*$cw,255,255,255);
	$bq->movePenTo(5,9); $bq->drawLineTo(15,9);
	$bq->drawLineTo(10,9); $bq->drawLineTo(10,4); $bq->drawLineTo(10,14);
	$bq->drawLineTo(10,9); $bq->drawLineTo(5,9);
	$ec->add($bq); $ec->nextFrame();
	$m->addExport($ec,"zoomin");
	
	#		Zoom out

	$ec=new SWF::MovieClip();
	$bq=new SWF::Shape();
	$bq->setRightFill($bq->addFill(0,0,0x8b));
	$bq->drawLineTo(0,10);
	$bq->drawCurveTo(0,20,10,20);
	$bq->drawCurveTo(20,20,20,10);
	$bq->drawLineTo(20,0);
	$bq->drawLineTo(0,0);
	$bq->setLine(50*$cw,255,255,255);
	$bq->movePenTo(6,9); $bq->drawLineTo(14,9);
	$ec->add($bq); $ec->nextFrame();
	$m->addExport($ec,"zoomout");

	# ------ padlock sprite
	
	$ec=new SWF::MovieClip();
	$s=new SWF::Shape();
	$s->movePenTo(3,-2); $s->setLine(35*$cw,0,0,0); $s->drawCircle(2);
	$s->movePenTo(0,0); $s->setRightFill($s->addFill(0,0,0));
	$s->drawLineTo(6,0); $s->drawLineTo(6,6);
	$s->drawLineTo(0,6); $s->drawLineTo(0,0); 
	$ec->add($s);
	$ec->nextFrame();
	$m->addExport($ec,"padlock");
	

	# ------ exclamation sprite
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(255,0,0);
	$s->movePenTo(20.50,19.73);
	$s->drawCurveTo(15.70,11.43,9.91,1.39);
	$s->drawCurveTo(8.92,-0.32,7.70,0.00);
	$s->drawCurveTo(7.01,0.18,5.98,1.96);
	$s->drawCurveTo(3.21,6.76,-0.40,13.01);
	$s->drawCurveTo(-2.56,16.76,-3.35,18.13);
	$s->drawCurveTo(-3.36,18.14,-3.85,18.92);
	$s->drawCurveTo(-4.48,19.92,-4.50,20.53);
	$s->drawCurveTo(-4.54,22.24,-2.43,22.24);
	$s->drawCurveTo(-2.43,22.24,2.00,22.24);
	$s->drawCurveTo(2.00,22.24,15.52,22.24);
	$s->drawCurveTo(15.52,22.24,18.97,22.24);
	$s->drawCurveTo(20.66,22.24,20.72,20.60);
	$s->drawCurveTo(20.73,20.15,20.50,19.73);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(18.09,20.01);
	$s->drawCurveTo(18.09,20.01,-1.84,20.01);
	$s->drawCurveTo(-1.84,20.01,-1.86,20.01);
	$s->drawCurveTo(-1.79,19.88,5.59,7.10);
	$s->drawCurveTo(6.98,4.68,8.10,2.75);
	$s->drawCurveTo(8.10,2.75,8.11,2.73);
	$s->drawCurveTo(14.90,14.50,18.07,19.99);
	$s->drawCurveTo(18.08,19.99,18.09,20.01);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(0,0,0);
	$s->movePenTo(9.60,17.78);
	$s->drawCurveTo(9.58,16.10,7.84,16.37);
	$s->drawCurveTo(6.62,16.56,6.62,17.78);
	$s->drawCurveTo(6.62,19.46,8.37,19.20);
	$s->drawCurveTo(9.58,19.00,9.60,17.78);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(0,0,0);
	$s->movePenTo(9.99,9.04);
	$s->drawCurveTo(10.34,7.48,8.88,6.80);
	$s->drawCurveTo(8.18,6.49,7.45,6.75);
	$s->drawCurveTo(5.86,7.33,6.41,9.72);
	$s->drawCurveTo(6.44,9.82,6.46,9.93);
	$s->drawCurveTo(6.47,9.95,7.21,12.89);
	$s->drawCurveTo(7.61,14.45,7.88,15.52);
	$s->drawCurveTo(7.93,15.71,8.29,15.55);
	$s->drawCurveTo(8.34,15.52,8.35,15.49);
	$s->drawCurveTo(8.35,15.48,8.54,14.74);
	$s->drawCurveTo(8.56,14.64,8.59,14.56);
	$s->drawCurveTo(9.02,12.85,9.43,11.25);
	$s->drawCurveTo(9.84,9.61,9.99,9.04);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"exclamation");

	# ------ potlatch_rotation sprite
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(5.68,0.63);
	$s->drawLineTo(6.39,-6.49);
	$s->drawLineTo(-0.72,-5.78);
	$s->drawLineTo(5.68,0.63);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-4.22,6.41);
	$s->drawLineTo(-6.41,4.22);
	$s->drawLineTo(2.80,-5.00);
	$s->drawLineTo(5.00,-2.80);
	$s->drawLineTo(-4.22,6.41);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"rotation");
	
	# ------ clockwise and anticlockwise
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(2.90,-4.60);
	$s->drawCurveTo(5.43,-3.12,5.43,-0.20);
	$s->drawCurveTo(5.43,0.84,5.03,1.79);
	$s->drawLineTo(6.70,2.49);
	$s->drawCurveTo(7.24,1.21,7.24,-0.20);
	$s->drawCurveTo(7.24,-4.16,3.81,-6.16);
	$s->drawLineTo(2.90,-4.60);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(3.79,0.45);
	$s->drawLineTo(4.42,5.53);
	$s->drawLineTo(8.51,2.44);
	$s->drawLineTo(3.79,0.45);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-3.75,-5.92);
	$s->drawCurveTo(-6.58,-4.28,-7.11,-1.05);
	$s->drawLineTo(-5.33,-0.76);
	$s->drawCurveTo(-4.94,-3.15,-2.85,-4.35);
	$s->drawCurveTo(-1.95,-4.87,-0.93,-5.00);
	$s->drawLineTo(-1.15,-6.79);
	$s->drawCurveTo(-2.54,-6.62,-3.75,-5.92);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-1.47,-3.25);
	$s->drawLineTo(2.62,-6.34);
	$s->drawLineTo(-2.10,-8.34);
	$s->drawLineTo(-1.47,-3.25);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-5.37,4.25);
	$s->drawCurveTo(-4.53,5.36,-3.31,6.07);
	$s->drawCurveTo(-0.07,7.94,3.27,6.24);
	$s->drawLineTo(2.46,4.63);
	$s->drawCurveTo(-0.01,5.89,-2.41,4.50);
	$s->drawCurveTo(-3.31,3.98,-3.93,3.17);
	$s->drawLineTo(-5.37,4.25);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-2.14,2.76);
	$s->drawLineTo(-6.86,0.76);
	$s->drawLineTo(-6.23,5.85);
	$s->drawLineTo(-2.14,2.76);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"clockwise");

	#

	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
	$ec->add($s);

	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-2.71,-4.60);
	$s->drawCurveTo(-5.24,-3.12,-5.24,-0.20);
	$s->drawCurveTo(-5.24,0.84,-4.84,1.79);
	$s->drawLineTo(-6.50,2.49);
	$s->drawCurveTo(-7.04,1.21,-7.04,-0.20);
	$s->drawCurveTo(-7.04,-4.16,-3.62,-6.16);
	$s->drawLineTo(-2.71,-4.60);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-3.59,0.45);
	$s->drawLineTo(-4.22,5.53);
	$s->drawLineTo(-8.31,2.44);
	$s->drawLineTo(-3.59,0.45);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(3.95,-5.92);
	$s->drawCurveTo(6.78,-4.28,7.31,-1.05);
	$s->drawLineTo(5.53,-0.76);
	$s->drawCurveTo(5.14,-3.15,3.05,-4.35);
	$s->drawCurveTo(2.15,-4.87,1.13,-5.00);
	$s->drawLineTo(1.35,-6.79);
	$s->drawCurveTo(2.73,-6.62,3.95,-5.92);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(1.67,-3.25);
	$s->drawLineTo(-2.42,-6.34);
	$s->drawLineTo(2.30,-8.34);
	$s->drawLineTo(1.67,-3.25);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(5.56,4.25);
	$s->drawCurveTo(4.72,5.36,3.51,6.07);
	$s->drawCurveTo(0.27,7.94,-3.08,6.24);
	$s->drawLineTo(-2.26,4.63);
	$s->drawCurveTo(0.21,5.89,2.61,4.50);
	$s->drawCurveTo(3.51,3.98,4.12,3.17);
	$s->drawLineTo(5.56,4.25);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(2.34,2.76);
	$s->drawLineTo(7.06,0.76);
	$s->drawLineTo(6.43,5.85);
	$s->drawLineTo(2.34,2.76);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"anticlockwise");

	
	#		Scissors (auto-generated from AI-to-Ming script)

	$ec=new SWF::MovieClip();

	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(127,127,127));
	drawLargeCircle();
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(255,255,255));
	$s->movePenTo(0.93,-1.62);
	$s->drawLineTo(0.29,-2.66);
	$s->drawLineTo(0.62,-3.22);
	$s->drawCurveTo(1.07,-3.37,1.45,-4.24);
	$s->drawLineTo(2.13,-5.78);
	$s->drawCurveTo(2.89,-7.53,4.03,-7.53);
	$s->drawCurveTo(4.63,-7.53,5.05,-7.08);
	$s->drawCurveTo(5.46,-6.63,5.46,-5.99);
	$s->drawCurveTo(5.46,-5.07,4.84,-4.38);
	$s->drawCurveTo(4.23,-3.69,3.41,-3.69);
	$s->drawCurveTo(3.34,-3.69,3.00,-3.73);
	$s->drawLineTo(2.83,-3.74);
	$s->drawCurveTo(2.65,-3.76,2.58,-3.76);
	$s->drawCurveTo(2.04,-3.76,1.66,-2.91);
	$s->drawCurveTo(1.59,-2.77,1.50,-2.62);
	$s->drawLineTo(0.93,-1.62);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(255,255,255));
	$s->movePenTo(-0.45,-3.15);
	$s->drawLineTo(4.77,5.25);
	$s->drawCurveTo(4.80,5.46,4.80,5.66);
	$s->drawCurveTo(4.80,6.68,4.39,7.68);
	$s->drawLineTo(-0.78,-1.23);
	$s->drawCurveTo(-1.03,-1.65,-1.09,-1.80);
	$s->drawLineTo(-1.15,-1.97);
	$s->drawCurveTo(-1.39,-2.60,-2.00,-2.60);
	$s->drawLineTo(-2.07,-2.60);
	$s->drawLineTo(-2.25,-2.59);
	$s->drawLineTo(-2.62,-2.57);
	$s->drawCurveTo(-3.63,-2.52,-4.58,-3.66);
	$s->drawCurveTo(-5.54,-4.81,-5.54,-6.07);
	$s->drawCurveTo(-5.54,-6.71,-5.21,-7.12);
	$s->drawCurveTo(-4.89,-7.53,-4.39,-7.53);
	$s->drawCurveTo(-3.74,-7.53,-2.94,-6.26);
	$s->drawLineTo(-1.47,-3.90);
	$s->drawCurveTo(-0.99,-3.13,-0.60,-3.13);
	$s->drawCurveTo(-0.56,-3.13,-0.45,-3.15);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(255,255,255));
	$s->movePenTo(-0.92,-0.73);
	$s->drawLineTo(-0.24,0.41);
	$s->drawLineTo(-4.42,7.68);
	$s->drawCurveTo(-4.79,6.77,-4.79,5.94);
	$s->drawCurveTo(-4.79,5.60,-4.73,5.22);
	$s->drawLineTo(-0.92,-0.73);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(127,127,127));
	$s->movePenTo(2.87,-5.99);
	$s->drawLineTo(2.69,-5.63);
	$s->drawCurveTo(2.46,-5.13,2.46,-4.83);
	$s->drawCurveTo(2.46,-4.23,3.29,-4.23);
	$s->drawCurveTo(3.91,-4.23,4.42,-4.79);
	$s->drawCurveTo(4.94,-5.34,4.94,-6.00);
	$s->drawCurveTo(4.94,-6.39,4.66,-6.68);
	$s->drawCurveTo(4.39,-6.97,4.00,-6.97);
	$s->drawCurveTo(3.37,-6.97,2.87,-5.99);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(127,127,127));
	$s->movePenTo(-2.47,-4.49);
	$s->drawLineTo(-3.53,-6.19);
	$s->drawCurveTo(-4.03,-6.97,-4.39,-6.97);
	$s->drawCurveTo(-4.69,-6.97,-4.84,-6.73);
	$s->drawCurveTo(-4.99,-6.49,-4.99,-6.02);
	$s->drawCurveTo(-4.99,-5.03,-4.23,-4.09);
	$s->drawCurveTo(-3.46,-3.15,-2.66,-3.15);
	$s->drawCurveTo(-2.12,-3.15,-2.12,-3.62);
	$s->drawCurveTo(-2.12,-3.90,-2.47,-4.49);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(127,127,127));
	$s->movePenTo(-0.38,-1.25);
	$s->drawCurveTo(-0.38,-0.89,-0.04,-0.89);
	$s->drawCurveTo(0.30,-0.89,0.30,-1.25);
	$s->drawCurveTo(0.30,-1.60,-0.04,-1.60);
	$s->drawCurveTo(-0.38,-1.60,-0.38,-1.25);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$ec->nextFrame();
	$m->addExport($ec,"scissors");

	#		Undo
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->movePenTo(-10.00,0.00);
	$s->drawCurveTo(-10.00,-6.72,-3.75,-9.27);
	$s->drawCurveTo(-1.96,-10.00,0.00,-10.00);
	$s->drawCurveTo(7.41,-10.00,9.58,-2.89);
	$s->drawCurveTo(10.00,-1.48,10.00,0.00);
	$s->drawCurveTo(10.00,6.72,3.75,9.27);
	$s->drawCurveTo(1.96,10.00,0.00,10.00);
	$s->drawCurveTo(-7.41,10.00,-9.58,2.89);
	$s->drawCurveTo(-10.00,1.48,-10.00,0.00);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->movePenTo(-0.38,-1.25);
	$s->drawCurveTo(-0.38,-0.89,-0.04,-0.89);
	$s->drawCurveTo(0.30,-0.89,0.30,-1.25);
	$s->drawCurveTo(0.30,-1.60,-0.04,-1.60);
	$s->drawCurveTo(-0.38,-1.60,-0.38,-1.25);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-0.65,-1.60);
	$s->drawLineTo(0.04,0.80);
	$s->drawCurveTo(-3.26,-2.07,-6.53,-3.28);
	$s->drawCurveTo(-2.66,-4.72,0.04,-7.07);
	$s->drawLineTo(-0.57,-4.82);
	$s->drawCurveTo(7.85,-2.68,5.36,1.99);
	$s->drawCurveTo(3.92,4.65,0.15,6.85);
	$s->drawCurveTo(-0.41,7.18,-0.55,6.60);
	$s->drawCurveTo(-0.56,6.55,-0.56,6.50);
	$s->drawCurveTo(-0.56,6.49,-0.56,6.49);
	$s->drawCurveTo(-0.56,6.49,-0.56,6.49);
	$s->drawCurveTo(-0.55,6.48,-0.54,6.47);
	$s->drawCurveTo(-0.38,6.34,-0.19,6.17);
	$s->drawCurveTo(1.83,4.33,2.51,2.66);
	$s->drawCurveTo(3.80,-0.52,-0.65,-1.60);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$ec->nextFrame(); $m->addExport($ec,"undo");

	#		Photo-mapping
	#		(ok, so this is just the tourism icon reshaped a bit...)
	
	$ec=new SWF::MovieClip();

	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
    $d=$ec->add($s);
    $d->move(5,5);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(11.74,5.18);
$s->drawLineTo(-4.19,5.18);
$s->drawCurveTo(-4.23,5.17,-4.27,5.16);
$s->drawCurveTo(-4.70,4.99,-4.72,4.49);
$s->drawCurveTo(-4.77,3.61,-4.77,1.27);
$s->drawCurveTo(-4.77,-1.67,-4.63,-2.52);
$s->drawCurveTo(-4.45,-3.62,-4.34,-4.16);
$s->drawCurveTo(-4.33,-4.21,-4.32,-4.24);
$s->drawCurveTo(-4.31,-4.27,-4.29,-4.30);
$s->drawCurveTo(-4.14,-4.66,-3.82,-4.74);
$s->drawCurveTo(-3.81,-4.74,-3.67,-4.78);
$s->drawCurveTo(-2.37,-5.11,-1.61,-4.93);
$s->drawCurveTo(-1.50,-4.90,-1.41,-4.87);
$s->drawCurveTo(-1.43,-4.89,-1.41,-5.11);
$s->drawCurveTo(-1.41,-5.13,-1.41,-5.15);
$s->drawCurveTo(-1.38,-5.17,-1.33,-5.21);
$s->drawCurveTo(-0.87,-5.52,0.03,-5.52);
$s->drawCurveTo(0.93,-5.52,1.58,-5.21);
$s->drawCurveTo(1.64,-5.18,1.69,-5.15);
$s->drawCurveTo(1.69,-5.15,1.69,-5.15);
$s->drawLineTo(1.72,-4.71);
$s->drawCurveTo(1.77,-4.70,1.82,-4.69);
$s->drawCurveTo(2.36,-4.63,2.73,-5.37);
$s->drawCurveTo(3.09,-6.10,3.76,-7.13);
$s->drawCurveTo(3.83,-7.22,3.87,-7.30);
$s->drawCurveTo(3.88,-7.30,3.88,-7.31);
$s->drawCurveTo(3.88,-7.31,3.89,-7.31);
$s->drawCurveTo(3.94,-7.34,4.02,-7.37);
$s->drawCurveTo(4.86,-7.75,5.79,-7.75);
$s->drawCurveTo(6.72,-7.75,7.80,-7.40);
$s->drawCurveTo(7.90,-7.37,7.97,-7.34);
$s->drawCurveTo(7.98,-7.34,7.98,-7.34);
$s->drawLineTo(9.36,-5.05);
$s->drawCurveTo(9.38,-5.01,9.41,-4.97);
$s->drawCurveTo(9.74,-4.48,10.24,-4.43);
$s->drawCurveTo(11.79,-4.27,12.21,-3.04);
$s->drawCurveTo(12.30,-2.79,12.33,-2.49);
$s->drawCurveTo(12.44,-1.56,12.41,1.16);
$s->drawCurveTo(12.39,2.61,12.34,4.21);
$s->drawCurveTo(12.34,4.36,12.33,4.47);
$s->drawCurveTo(12.33,4.48,12.33,4.49);
$s->drawCurveTo(12.33,4.49,12.33,4.50);
$s->drawCurveTo(12.34,4.54,12.33,4.60);
$s->drawCurveTo(12.29,5.18,11.74,5.18);
$d=$ec->add($s); $d->move(2,4.5); $d->scale(0.9); $d->rotate(-15);

$s=new SWF::Shape();
$s->setRightFill(150,150,150);
$s->movePenTo(1.75,0.33);
$s->drawCurveTo(1.75,-3.83,5.92,-3.83);
$s->drawCurveTo(10.08,-3.83,10.08,0.33);
$s->drawCurveTo(10.08,4.49,5.92,4.49);
$s->drawCurveTo(1.75,4.49,1.75,0.33);
$d=$ec->add($s); $d->move(2,4.5); $d->scale(0.9); $d->rotate(-15);

$s=new SWF::Shape();
$s->setLine(1.34,127,127,127);
$s->movePenTo(-0.56,4.59);
$s->drawCurveTo(-0.56,4.56,-0.56,4.53);
$s->drawCurveTo(-0.60,4.17,-0.63,3.73);
$s->drawCurveTo(-0.97,-0.97,-0.03,-3.02);
$d=$ec->add($s); $d->move(2,4.5); $d->scale(0.9); $d->rotate(-15);

$s=new SWF::Shape();
$s->setLine(0.67,127,127,127);
$s->movePenTo(3.60,-5.37);
$s->drawLineTo(8.17,-5.37);
$d=$ec->add($s); $d->move(2,4.5); $d->scale(0.9); $d->rotate(-15);


	$ec->nextFrame();
    $m->addExport($ec,"camera");

	#		Align

	$ec=new SWF::MovieClip();

	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
    $d=$ec->add($s);

	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(0,0); $s->drawLineTo(4,0); $s->drawLineTo(4,4);
	$s->drawLineTo(0,4); $s->drawLineTo(0,0);
	$d=$ec->add($s); $d->move(-6,2); 
	$d=$ec->add($s); $d->move(-2,-2); 
	$d=$ec->add($s); $d->move(2,-6); 
	
	$ec->nextFrame();
    $m->addExport($ec,"tidy");

	#		GPS

	$ec=new SWF::MovieClip();

	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(1.47,8.21);
	$s->drawCurveTo(1.27,8.74,0.74,8.54);
	$s->drawLineTo(-5.96,6.06);
	$s->drawCurveTo(-6.49,5.86,-6.29,5.32);
	$s->drawLineTo(-1.22,-8.34);
	$s->drawCurveTo(-1.02,-8.87,-0.48,-8.67);
	$s->drawLineTo(6.21,-6.18);
	$s->drawCurveTo(6.75,-5.98,6.55,-5.45);
	$s->drawLineTo(1.47,8.21);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->movePenTo(3.86,-0.62);
	$s->drawCurveTo(4.06,-1.15,3.52,-1.35);
	$s->drawLineTo(-1.57,-3.24);
	$s->drawCurveTo(-2.10,-3.44,-2.30,-2.91);
	$s->drawLineTo(-5.11,4.66);
	$s->drawCurveTo(-5.31,5.20,-4.78,5.39);
	$s->drawLineTo(0.31,7.29);
	$s->drawCurveTo(0.85,7.48,1.04,6.95);
	$s->drawLineTo(3.86,-0.62);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->movePenTo(4.09,-4.36);
	$s->drawCurveTo(3.94,-3.95,4.32,-3.81);
	$s->drawCurveTo(4.70,-3.67,4.85,-4.07);
	$s->drawCurveTo(5.00,-4.47,4.62,-4.61);
	$s->drawCurveTo(4.24,-4.75,4.09,-4.36);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->movePenTo(2.77,-3.40);
	$s->drawCurveTo(2.62,-3.00,3.00,-2.86);
	$s->drawCurveTo(3.39,-2.71,3.54,-3.12);
	$s->drawCurveTo(3.68,-3.52,3.30,-3.66);
	$s->drawCurveTo(2.92,-3.80,2.77,-3.40);
    $d=$ec->add($s);
    $d->move(5,5);

	$ec->nextFrame();
    $m->addExport($ec,"gps");
	
	#		Prefs

	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(4.29,-2.58);
	$s->drawLineTo(4.29,5.32);
	$s->drawLineTo(-4.76,5.32);
	$s->drawLineTo(-4.76,-3.72);
	$s->drawLineTo(1.30,-3.72);
	$s->drawLineTo(1.93,-4.80);
	$s->drawLineTo(-5.84,-4.80);
	$s->drawLineTo(-5.84,6.40);
	$s->drawLineTo(5.37,6.40);
	$s->drawLineTo(5.37,-3.89);
	$s->drawLineTo(4.29,-2.58);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(6.80,-6.73);
	$s->drawCurveTo(4.78,-4.47,3.03,-2.08);
	$s->drawCurveTo(1.27,0.31,-0.21,2.84);
	$s->drawCurveTo(-0.31,3.02,-0.48,3.36);
	$s->drawCurveTo(-0.99,4.37,-1.92,4.37);
	$s->drawCurveTo(-2.31,4.37,-2.57,4.26);
	$s->drawCurveTo(-2.83,4.14,-3.05,3.85);
	$s->drawCurveTo(-3.22,3.61,-3.38,3.20);
	$s->drawCurveTo(-3.53,2.81,-3.75,2.03);
	$s->drawCurveTo(-3.76,1.98,-3.78,1.91);
	$s->drawCurveTo(-4.02,1.05,-4.02,0.83);
	$s->drawCurveTo(-4.02,0.39,-3.50,0.02);
	$s->drawCurveTo(-2.98,-0.35,-2.48,-0.35);
	$s->drawCurveTo(-2.32,-0.35,-2.21,-0.25);
	$s->drawCurveTo(-2.11,-0.16,-1.98,0.16);
	$s->drawCurveTo(-1.90,0.36,-1.79,0.69);
	$s->drawCurveTo(-1.52,1.44,-1.34,1.44);
	$s->drawCurveTo(-1.19,1.44,-0.12,-0.27);
	$s->drawCurveTo(0.94,-1.94,1.65,-2.99);
	$s->drawCurveTo(2.68,-4.53,3.22,-5.23);
	$s->drawCurveTo(3.76,-5.92,4.20,-6.27);
	$s->drawCurveTo(4.57,-6.58,5.18,-6.76);
	$s->drawCurveTo(5.78,-6.94,6.69,-7.00);
	$s->drawLineTo(6.80,-6.73);
    $d=$ec->add($s);
    $d->move(5,5);
	
	$ec->nextFrame(); $m->addExport($ec,"prefs");



	#Ê=====	Menu icons
		
# ------ potlatch_iplace sprite

# ------ potlatch_iskyline sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-7.71,4.70);
$s->drawLineTo(-7.71,-3.43);
$s->drawLineTo(-4.37,-3.43);
$s->drawLineTo(-4.37,4.69);
$s->drawLineTo(-7.71,4.70);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-5.18,-4.65);
$s->drawLineTo(-2.72,-7.11);
$s->drawLineTo(-0.27,-4.67);
$s->drawLineTo(-0.27,4.77);
$s->drawLineTo(-3.31,4.77);
$s->drawLineTo(-3.31,-4.61);
$s->drawLineTo(-5.18,-4.65);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(4.42,-1.42);
$s->drawLineTo(4.42,-7.21);
$s->drawLineTo(0.69,-7.21);
$s->drawLineTo(0.69,4.70);
$s->drawLineTo(3.14,4.70);
$s->drawLineTo(3.19,-1.42);
$s->drawLineTo(4.42,-1.42);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(12.32,4.64);
$s->drawLineTo(4.03,4.64);
$s->drawLineTo(4.03,-0.70);
$s->drawLineTo(12.32,-0.70);
$s->drawLineTo(12.32,4.64);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(7.37,-4.09);
$s->drawLineTo(5.48,-4.09);
$s->drawLineTo(5.48,-1.53);
$s->drawLineTo(7.37,-1.53);
$s->drawLineTo(7.37,-4.09);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(12.21,-7.77);
$s->drawLineTo(9.82,-7.77);
$s->drawLineTo(9.82,-1.59);
$s->drawLineTo(12.21,-1.59);
$s->drawLineTo(12.21,-7.77);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(16.60,-6.32);
$s->drawLineTo(14.71,-8.04);
$s->drawLineTo(12.99,-6.32);
$s->drawLineTo(12.99,4.64);
$s->drawLineTo(16.60,4.64);
$s->drawLineTo(16.60,-6.32);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_place");

# ------ potlatch_itourism sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(11.74,5.18);
$s->drawLineTo(-4.19,5.18);
$s->drawCurveTo(-4.23,5.17,-4.27,5.16);
$s->drawCurveTo(-4.70,4.99,-4.72,4.49);
$s->drawCurveTo(-4.77,3.61,-4.77,1.27);
$s->drawCurveTo(-4.77,-1.67,-4.63,-2.52);
$s->drawCurveTo(-4.45,-3.62,-4.34,-4.16);
$s->drawCurveTo(-4.33,-4.21,-4.32,-4.24);
$s->drawCurveTo(-4.31,-4.27,-4.29,-4.30);
$s->drawCurveTo(-4.14,-4.66,-3.82,-4.74);
$s->drawCurveTo(-3.81,-4.74,-3.67,-4.78);
$s->drawCurveTo(-2.37,-5.11,-1.61,-4.93);
$s->drawCurveTo(-1.50,-4.90,-1.41,-4.87);
$s->drawCurveTo(-1.43,-4.89,-1.41,-5.11);
$s->drawCurveTo(-1.41,-5.13,-1.41,-5.15);
$s->drawCurveTo(-1.38,-5.17,-1.33,-5.21);
$s->drawCurveTo(-0.87,-5.52,0.03,-5.52);
$s->drawCurveTo(0.93,-5.52,1.58,-5.21);
$s->drawCurveTo(1.64,-5.18,1.69,-5.15);
$s->drawCurveTo(1.69,-5.15,1.69,-5.15);
$s->drawLineTo(1.72,-4.71);
$s->drawCurveTo(1.77,-4.70,1.82,-4.69);
$s->drawCurveTo(2.36,-4.63,2.73,-5.37);
$s->drawCurveTo(3.09,-6.10,3.76,-7.13);
$s->drawCurveTo(3.83,-7.22,3.87,-7.30);
$s->drawCurveTo(3.88,-7.30,3.88,-7.31);
$s->drawCurveTo(3.88,-7.31,3.89,-7.31);
$s->drawCurveTo(3.94,-7.34,4.02,-7.37);
$s->drawCurveTo(4.86,-7.75,5.79,-7.75);
$s->drawCurveTo(6.72,-7.75,7.80,-7.40);
$s->drawCurveTo(7.90,-7.37,7.97,-7.34);
$s->drawCurveTo(7.98,-7.34,7.98,-7.34);
$s->drawLineTo(9.36,-5.05);
$s->drawCurveTo(9.38,-5.01,9.41,-4.97);
$s->drawCurveTo(9.74,-4.48,10.24,-4.43);
$s->drawCurveTo(11.79,-4.27,12.21,-3.04);
$s->drawCurveTo(12.30,-2.79,12.33,-2.49);
$s->drawCurveTo(12.44,-1.56,12.41,1.16);
$s->drawCurveTo(12.39,2.61,12.34,4.21);
$s->drawCurveTo(12.34,4.36,12.33,4.47);
$s->drawCurveTo(12.33,4.48,12.33,4.49);
$s->drawCurveTo(12.33,4.49,12.33,4.50);
$s->drawCurveTo(12.34,4.54,12.33,4.60);
$s->drawCurveTo(12.29,5.18,11.74,5.18);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(1.75,0.33);
$s->drawCurveTo(1.75,-3.83,5.92,-3.83);
$s->drawCurveTo(10.08,-3.83,10.08,0.33);
$s->drawCurveTo(10.08,4.49,5.92,4.49);
$s->drawCurveTo(1.75,4.49,1.75,0.33);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(1.34,190,190,190);
$s->movePenTo(-0.56,4.59);
$s->drawCurveTo(-0.56,4.56,-0.56,4.53);
$s->drawCurveTo(-0.60,4.17,-0.63,3.73);
$s->drawCurveTo(-0.97,-0.97,-0.03,-3.02);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.67,190,190,190);
$s->movePenTo(3.60,-5.37);
$s->drawLineTo(8.17,-5.37);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_tourism");

# ------ potlatch_inatural sprite

# ------ potlatch_itrees sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-6.54,0.95);
$s->drawLineTo(-2.87,-7.50);
$s->drawLineTo(0.02,0.62);
$s->drawLineTo(-1.87,0.84);
$s->drawLineTo(-2.09,5.07);
$s->drawLineTo(-4.54,5.07);
$s->drawLineTo(-4.43,1.07);
$s->drawLineTo(-6.54,0.95);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(8.70,1.18);
$s->drawLineTo(12.82,-7.50);
$s->drawLineTo(16.55,1.51);
$s->drawLineTo(13.49,2.85);
$s->drawLineTo(13.60,5.07);
$s->drawLineTo(11.15,5.07);
$s->drawLineTo(11.26,2.40);
$s->drawLineTo(8.70,1.18);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(2.19,2.12);
$s->drawLineTo(4.95,-3.33);
$s->drawLineTo(7.65,2.35);
$s->drawLineTo(5.64,3.01);
$s->drawLineTo(5.75,5.24);
$s->drawLineTo(3.97,5.24);
$s->drawLineTo(4.08,2.57);
$s->drawLineTo(2.19,2.12);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.67,255,255,255);
$s->movePenTo(-0.31,-8.50);
$s->drawCurveTo(-0.30,-8.50,-0.29,-8.50);
$s->drawCurveTo(-0.16,-8.50,0.00,-8.48);
$s->drawCurveTo(1.72,-8.22,1.86,-6.67);
$s->drawCurveTo(1.87,-6.68,1.87,-6.68);
$s->drawCurveTo(1.94,-6.78,2.03,-6.88);
$s->drawCurveTo(3.07,-8.01,4.20,-7.17);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_natural");

	# ----- potlatch_iboat sprite
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-10.00,7.17);
	$s->drawLineTo(19.00,7.17);
	$s->drawLineTo(19.00,-10.00);
	$s->drawLineTo(-10.00,-10.00);
	$s->drawLineTo(-10.00,7.17);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-7.61,1.78);
	$s->drawCurveTo(-3.99,-0.47,-0.35,1.17);
	$s->drawCurveTo(0.41,1.52,1.06,1.74);
	$s->drawCurveTo(3.05,2.40,4.79,2.05);
	$s->drawCurveTo(5.51,1.91,7.34,1.11);
	$s->drawCurveTo(8.58,0.58,9.96,0.50);
	$s->drawCurveTo(12.31,0.38,14.52,1.99);
	$s->drawLineTo(14.66,1.83);
	$s->drawCurveTo(14.80,1.91,17.13,-2.69);
	$s->drawCurveTo(17.34,-3.12,17.52,-3.47);
	$s->drawCurveTo(17.54,-3.50,17.55,-3.53);
	$s->drawCurveTo(17.55,-3.53,17.44,-3.53);
	$s->drawCurveTo(17.08,-3.52,16.86,-3.51);
	$s->drawCurveTo(13.20,-3.44,13.20,-3.65);
	$s->drawCurveTo(13.20,-3.65,13.20,-5.08);
	$s->drawLineTo(11.42,-6.73);
	$s->drawCurveTo(0.47,-6.73,0.47,-6.73);
	$s->drawLineTo(0.41,-3.48);
	$s->drawLineTo(-6.43,-3.53);
	$s->drawLineTo(-6.43,-1.55);
	$s->drawLineTo(-7.71,-1.55);
	$s->drawLineTo(-7.61,1.78);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(14.25,3.39);
	$s->drawCurveTo(12.08,1.85,9.78,2.08);
	$s->drawCurveTo(9.78,2.08,5.71,3.40);
	$s->drawCurveTo(5.35,3.51,5.02,3.59);
	$s->drawCurveTo(2.64,4.13,0.33,3.13);
	$s->drawCurveTo(-1.61,2.30,-2.00,2.22);
	$s->drawCurveTo(-3.34,1.95,-4.43,2.14);
	$s->drawCurveTo(-6.07,2.42,-7.59,3.48);
	$s->drawCurveTo(-7.30,3.94,-7.16,4.18);
	$s->drawCurveTo(-6.97,4.49,-6.68,4.29);
	$s->drawCurveTo(-6.20,3.98,-5.58,3.74);
	$s->drawCurveTo(-4.39,3.29,-3.15,3.35);
	$s->drawCurveTo(-3.11,3.35,0.98,4.67);
	$s->drawCurveTo(1.32,4.78,1.63,4.84);
	$s->drawCurveTo(4.15,5.38,6.59,4.43);
	$s->drawCurveTo(6.60,4.43,7.83,3.88);
	$s->drawCurveTo(8.46,3.60,8.92,3.50);
	$s->drawCurveTo(10.24,3.22,11.36,3.44);
	$s->drawCurveTo(12.26,3.62,13.07,4.04);
	$s->drawCurveTo(13.07,4.04,13.51,4.30);
	$s->drawCurveTo(13.81,4.49,13.82,4.48);
	$s->drawCurveTo(13.83,4.47,14.13,3.65);
	$s->drawCurveTo(14.18,3.50,14.25,3.39);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(7.56,-5.46);
	$s->drawLineTo(7.56,-3.53);
	$s->drawLineTo(11.61,-3.53);
	$s->drawLineTo(11.61,-5.46);
	$s->drawLineTo(7.56,-5.46);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(1.91,-5.46);
	$s->drawLineTo(1.91,-3.53);
	$s->drawLineTo(5.96,-3.53);
	$s->drawLineTo(5.96,-5.46);
	$s->drawLineTo(1.91,-5.46);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-0.80,-8.58);
	$s->drawLineTo(1.71,-6.08);
	$s->drawLineTo(2.18,-6.55);
	$s->drawLineTo(-0.33,-9.05);
	$s->drawLineTo(-0.80,-8.58);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"preset_waterway");
	
	# ------ potlatch_icar sprite
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-10.00,7.17);
	$s->drawLineTo(19.00,7.17);
	$s->drawLineTo(19.00,-10.00);
	$s->drawLineTo(-10.00,-10.00);
	$s->drawLineTo(-10.00,7.17);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(10.82,3.30);
	$s->drawCurveTo(10.86,2.02,11.77,1.31);
	$s->drawCurveTo(12.75,0.54,13.93,0.79);
	$s->drawCurveTo(17.07,1.47,15.72,4.37);
	$s->drawCurveTo(14.37,7.25,11.84,5.36);
	$s->drawCurveTo(10.86,4.63,10.82,3.30);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(12.02,3.30);
	$s->drawCurveTo(12.09,1.88,13.48,1.95);
	$s->drawCurveTo(14.96,2.03,14.73,3.47);
	$s->drawCurveTo(14.53,4.72,13.28,4.66);
	$s->drawCurveTo(12.09,4.60,12.02,3.30);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-7.14,3.37);
	$s->drawCurveTo(-7.10,2.08,-6.18,1.37);
	$s->drawCurveTo(-5.20,0.59,-4.03,0.86);
	$s->drawCurveTo(-0.92,1.56,-2.24,4.43);
	$s->drawCurveTo(-3.57,7.33,-6.11,5.42);
	$s->drawCurveTo(-7.10,4.68,-7.14,3.37);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-5.93,3.37);
	$s->drawCurveTo(-5.86,1.94,-4.47,2.01);
	$s->drawCurveTo(-3.00,2.09,-3.23,3.53);
	$s->drawCurveTo(-3.42,4.78,-4.67,4.72);
	$s->drawCurveTo(-5.87,4.66,-5.93,3.37);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-7.22,-2.70);
	$s->drawCurveTo(-7.21,-2.73,-7.19,-2.77);
	$s->drawCurveTo(-7.06,-3.15,-6.84,-3.60);
	$s->drawCurveTo(-4.50,-8.53,0.05,-8.53);
	$s->drawCurveTo(4.48,-8.53,8.98,-3.62);
	$s->drawCurveTo(9.57,-2.97,9.75,-2.83);
	$s->drawCurveTo(13.58,-2.33,15.54,-1.77);
	$s->drawCurveTo(17.62,-1.16,17.64,1.82);
	$s->drawCurveTo(17.64,1.87,17.64,1.96);
	$s->drawCurveTo(17.64,2.04,17.64,2.05);
	$s->drawCurveTo(17.64,3.26,16.63,3.43);
	$s->drawCurveTo(16.74,1.87,15.89,0.90);
	$s->drawCurveTo(15.02,-0.10,13.48,-0.15);
	$s->drawCurveTo(11.90,-0.20,10.94,0.83);
	$s->drawCurveTo(10.02,1.81,10.13,3.43);
	$s->drawLineTo(-1.16,3.43);
	$s->drawCurveTo(-1.05,1.88,-1.90,0.90);
	$s->drawCurveTo(-2.76,-0.10,-4.30,-0.15);
	$s->drawCurveTo(-5.91,-0.20,-6.85,0.83);
	$s->drawCurveTo(-7.53,1.58,-7.78,3.07);
	$s->drawLineTo(-8.76,2.80);
	$s->drawCurveTo(-9.11,3.60,-7.80,-0.85);
	$s->drawCurveTo(-7.46,-2.02,-7.22,-2.70);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(8.25,-2.81);
	$s->drawCurveTo(8.23,-2.83,8.21,-2.85);
	$s->drawCurveTo(7.96,-3.10,7.65,-3.40);
	$s->drawCurveTo(4.31,-6.61,2.57,-7.16);
	$s->drawCurveTo(2.15,-7.29,1.44,-7.39);
	$s->drawCurveTo(1.38,-7.40,1.33,-7.40);
	$s->drawCurveTo(1.32,-7.41,1.32,-7.41);
	$s->drawLineTo(1.37,-3.31);
	$s->drawLineTo(8.25,-2.81);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(0.40,-7.53);
	$s->drawCurveTo(0.38,-7.53,0.35,-7.53);
	$s->drawCurveTo(0.09,-7.54,-0.25,-7.50);
	$s->drawCurveTo(-3.83,-7.08,-5.57,-3.43);
	$s->drawLineTo(0.31,-3.27);
	$s->drawLineTo(0.40,-7.53);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(1.55,-1.77);
	$s->drawLineTo(2.89,-1.68);
	$s->drawLineTo(2.92,-2.18);
	$s->drawLineTo(1.59,-2.27);
	$s->drawLineTo(1.55,-1.77);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-1.07,-1.89);
	$s->drawLineTo(0.26,-1.81);
	$s->drawLineTo(0.29,-2.31);
	$s->drawLineTo(-1.04,-2.39);
	$s->drawLineTo(-1.07,-1.89);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"preset_road");
	
	# ------ potlatch_icycle sprite
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-10.00,7.17);
	$s->drawLineTo(19.00,7.17);
	$s->drawLineTo(19.00,-10.00);
	$s->drawLineTo(-10.00,-10.00);
	$s->drawLineTo(-10.00,7.17);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(9.43,-6.21);
	$s->drawCurveTo(10.17,-3.43,10.35,-2.77);
	$s->drawCurveTo(12.09,-3.33,13.45,-2.73);
	$s->drawCurveTo(15.07,-2.01,15.70,-0.59);
	$s->drawCurveTo(16.25,0.64,16.06,1.88);
	$s->drawCurveTo(16.00,2.21,15.90,2.53);
	$s->drawCurveTo(15.34,4.17,13.91,4.94);
	$s->drawCurveTo(10.39,6.83,8.26,3.51);
	$s->drawCurveTo(6.84,1.30,8.24,-1.01);
	$s->drawCurveTo(8.67,-1.72,9.33,-2.21);
	$s->drawCurveTo(9.40,-2.26,9.35,-2.45);
	$s->drawCurveTo(9.34,-2.49,9.26,-2.71);
	$s->drawCurveTo(9.25,-2.75,9.25,-2.77);
	$s->drawCurveTo(8.10,-1.97,6.39,-0.77);
	$s->drawCurveTo(5.01,0.19,4.70,0.41);
	$s->drawCurveTo(4.70,0.42,4.41,0.61);
	$s->drawCurveTo(4.37,0.65,4.33,0.67);
	$s->drawCurveTo(4.32,0.68,4.38,1.21);
	$s->drawCurveTo(4.39,1.29,4.39,1.31);
	$s->drawCurveTo(4.39,2.37,3.41,2.80);
	$s->drawCurveTo(2.91,3.03,2.46,2.98);
	$s->drawCurveTo(2.46,2.98,2.28,2.94);
	$s->drawCurveTo(2.11,2.90,2.07,2.98);
	$s->drawCurveTo(1.98,3.18,1.84,3.49);
	$s->drawCurveTo(1.84,3.49,2.27,3.49);
	$s->drawCurveTo(2.35,3.49,2.33,3.74);
	$s->drawCurveTo(2.32,3.80,2.32,3.82);
	$s->drawCurveTo(2.32,3.83,2.33,3.94);
	$s->drawCurveTo(2.34,4.06,2.24,4.06);
	$s->drawCurveTo(2.24,4.06,2.08,4.05);
	$s->drawCurveTo(1.86,4.03,1.81,4.10);
	$s->drawCurveTo(1.52,4.45,1.16,4.19);
	$s->drawCurveTo(0.98,4.06,0.90,4.06);
	$s->drawCurveTo(0.90,4.06,0.77,4.07);
	$s->drawCurveTo(0.54,4.08,0.54,4.01);
	$s->drawCurveTo(0.54,4.01,0.54,3.53);
	$s->drawCurveTo(0.54,3.46,0.81,3.48);
	$s->drawCurveTo(0.83,3.49,0.84,3.49);
	$s->drawCurveTo(0.84,3.49,1.29,3.49);
	$s->drawCurveTo(1.31,3.49,1.43,3.19);
	$s->drawCurveTo(1.45,3.15,1.45,3.14);
	$s->drawCurveTo(1.58,2.87,1.65,2.72);
	$s->drawCurveTo(1.65,2.71,1.41,2.51);
	$s->drawCurveTo(1.38,2.48,1.37,2.47);
	$s->drawCurveTo(1.05,2.16,0.95,1.81);
	$s->drawCurveTo(0.89,1.61,0.71,1.61);
	$s->drawCurveTo(0.47,1.61,0.45,1.80);
	$s->drawCurveTo(0.41,2.12,0.26,2.60);
	$s->drawCurveTo(0.06,3.30,-0.49,3.93);
	$s->drawCurveTo(-1.60,5.23,-3.23,5.44);
	$s->drawCurveTo(-7.10,5.93,-7.94,2.04);
	$s->drawCurveTo(-8.18,0.94,-7.77,-0.21);
	$s->drawCurveTo(-7.62,-0.63,-7.38,-1.03);
	$s->drawCurveTo(-6.59,-2.35,-4.89,-2.89);
	$s->drawCurveTo(-4.16,-3.12,-3.37,-3.02);
	$s->drawCurveTo(-2.87,-2.95,-2.54,-2.86);
	$s->drawCurveTo(-2.10,-2.73,-2.07,-2.79);
	$s->drawCurveTo(-2.06,-2.80,-1.45,-3.86);
	$s->drawCurveTo(-1.21,-4.27,-1.04,-4.57);
	$s->drawCurveTo(-1.04,-4.58,-0.62,-5.30);
	$s->drawCurveTo(-0.58,-5.36,-0.55,-5.42);
	$s->drawCurveTo(-0.44,-5.61,-0.47,-5.72);
	$s->drawCurveTo(-0.48,-5.75,-0.51,-5.83);
	$s->drawCurveTo(-0.51,-5.83,-0.65,-6.22);
	$s->drawCurveTo(-0.68,-6.29,-0.70,-6.35);
	$s->drawCurveTo(-0.76,-6.52,-0.94,-6.55);
	$s->drawCurveTo(-1.27,-6.60,-1.59,-6.93);
	$s->drawCurveTo(-1.86,-7.19,-1.90,-7.46);
	$s->drawCurveTo(-1.91,-7.53,-1.89,-7.58);
	$s->drawCurveTo(-1.84,-7.67,-1.50,-7.65);
	$s->drawCurveTo(-1.48,-7.65,-1.47,-7.65);
	$s->drawCurveTo(-1.47,-7.65,0.73,-7.65);
	$s->drawCurveTo(0.73,-7.65,1.68,-7.65);
	$s->drawCurveTo(2.04,-7.65,2.15,-7.56);
	$s->drawCurveTo(2.44,-7.30,2.15,-7.04);
	$s->drawCurveTo(2.09,-6.98,1.72,-6.90);
	$s->drawCurveTo(1.72,-6.90,0.34,-6.59);
	$s->drawCurveTo(0.63,-5.80,0.67,-5.71);
	$s->drawCurveTo(0.73,-5.53,0.84,-5.53);
	$s->drawCurveTo(0.84,-5.53,2.25,-5.53);
	$s->drawCurveTo(2.25,-5.53,6.95,-5.53);
	$s->drawCurveTo(6.95,-5.53,8.51,-5.53);
	$s->drawCurveTo(8.25,-6.48,8.39,-6.82);
	$s->drawCurveTo(8.43,-6.92,8.50,-7.02);
	$s->drawCurveTo(8.50,-7.02,8.73,-7.22);
	$s->drawCurveTo(8.94,-7.41,8.83,-7.55);
	$s->drawCurveTo(8.75,-7.65,8.20,-7.65);
	$s->drawCurveTo(8.20,-7.65,7.11,-7.65);
	$s->drawCurveTo(7.02,-7.65,7.02,-7.72);
	$s->drawCurveTo(7.02,-7.72,7.02,-8.29);
	$s->drawCurveTo(7.02,-8.29,7.02,-8.71);
	$s->drawCurveTo(7.02,-8.73,7.17,-8.72);
	$s->drawCurveTo(7.19,-8.72,7.20,-8.72);
	$s->drawCurveTo(7.20,-8.72,8.31,-8.72);
	$s->drawCurveTo(9.12,-8.72,9.34,-8.67);
	$s->drawCurveTo(9.80,-8.57,10.07,-7.98);
	$s->drawCurveTo(10.27,-7.56,9.98,-7.00);
	$s->drawCurveTo(9.97,-6.98,9.58,-6.60);
	$s->drawCurveTo(9.40,-6.42,9.43,-6.21);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(9.99,-0.01);
	$s->drawCurveTo(9.99,-0.01,9.80,-0.72);
	$s->drawCurveTo(9.77,-0.82,9.75,-0.90);
	$s->drawCurveTo(8.45,0.55,9.00,2.09);
	$s->drawCurveTo(9.65,3.88,11.31,4.15);
	$s->drawCurveTo(13.20,4.44,14.20,3.04);
	$s->drawCurveTo(15.05,1.86,14.75,0.55);
	$s->drawCurveTo(14.68,0.24,14.54,-0.05);
	$s->drawCurveTo(13.76,-1.66,11.94,-1.76);
	$s->drawCurveTo(11.53,-1.78,11.17,-1.68);
	$s->drawCurveTo(10.96,-1.63,10.75,-1.55);
	$s->drawCurveTo(10.66,-1.52,10.71,-1.38);
	$s->drawCurveTo(10.71,-1.38,10.74,-1.29);
	$s->drawCurveTo(10.75,-1.25,10.76,-1.23);
	$s->drawCurveTo(11.03,-0.20,11.42,0.26);
	$s->drawCurveTo(11.55,0.43,11.83,0.68);
	$s->drawCurveTo(11.83,0.68,12.05,0.84);
	$s->drawCurveTo(12.20,0.94,12.23,1.05);
	$s->drawCurveTo(12.33,1.33,12.12,1.54);
	$s->drawCurveTo(11.98,1.69,11.57,1.59);
	$s->drawCurveTo(10.43,1.31,9.99,-0.01);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(4.70,-1.62);
	$s->drawLineTo(4.70,-1.05);
	$s->drawLineTo(4.02,-1.05);
	$s->drawLineTo(3.60,-0.26);
	$s->drawLineTo(3.71,-0.19);
	$s->drawLineTo(8.86,-3.80);
	$s->drawLineTo(8.69,-4.46);
	$s->drawLineTo(1.12,-4.46);
	$s->drawLineTo(2.54,-0.56);
	$s->drawLineTo(3.10,-0.49);
	$s->drawLineTo(3.41,-1.05);
	$s->drawLineTo(2.98,-1.05);
	$s->drawLineTo(2.98,-1.62);
	$s->drawCurveTo(3.01,-1.62,3.04,-1.62);
	$s->drawCurveTo(3.38,-1.58,3.42,-1.64);
	$s->drawCurveTo(3.82,-2.13,4.24,-1.62);
	$s->drawLineTo(4.70,-1.62);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-0.81,0.83);
	$s->drawCurveTo(-0.81,0.83,-2.92,0.83);
	$s->drawCurveTo(-2.20,-0.43,-1.84,-1.06);
	$s->drawCurveTo(-1.00,-0.27,-0.81,0.83);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(0.04,-4.31);
	$s->drawCurveTo(0.04,-4.30,-0.78,-2.90);
	$s->drawCurveTo(-0.84,-2.80,-0.89,-2.70);
	$s->drawCurveTo(-0.89,-2.70,-0.94,-2.63);
	$s->drawCurveTo(-1.21,-2.19,-1.19,-2.17);
	$s->drawCurveTo(-0.80,-1.86,-0.60,-1.62);
	$s->drawCurveTo(-0.07,-1.00,0.24,-0.22);
	$s->drawCurveTo(0.38,0.14,0.43,0.50);
	$s->drawCurveTo(0.43,0.51,0.44,0.64);
	$s->drawCurveTo(0.45,0.83,0.53,0.83);
	$s->drawCurveTo(0.89,0.83,0.91,0.75);
	$s->drawCurveTo(1.01,0.41,1.20,0.16);
	$s->drawCurveTo(1.20,0.16,1.42,-0.07);
	$s->drawCurveTo(1.55,-0.20,1.54,-0.21);
	$s->drawCurveTo(1.54,-0.22,1.28,-0.92);
	$s->drawCurveTo(1.18,-1.19,1.11,-1.39);
	$s->drawCurveTo(0.42,-3.29,0.04,-4.31);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(1.49,1.22);
	$s->drawCurveTo(1.56,0.28,2.31,0.13);
	$s->drawCurveTo(3.15,-0.05,3.61,0.66);
	$s->drawCurveTo(4.97,2.75,2.43,2.34);
	$s->drawCurveTo(2.24,2.31,2.07,2.21);
	$s->drawCurveTo(1.54,1.88,1.49,1.22);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-4.27,1.02);
	$s->drawCurveTo(-4.57,1.66,-3.23,1.61);
	$s->drawCurveTo(-3.13,1.61,-3.09,1.61);
	$s->drawCurveTo(-3.09,1.61,-0.81,1.61);
	$s->drawCurveTo(-1.16,3.50,-2.74,4.01);
	$s->drawCurveTo(-4.55,4.60,-5.77,3.42);
	$s->drawCurveTo(-7.13,2.11,-6.64,0.44);
	$s->drawCurveTo(-6.12,-1.38,-4.37,-1.70);
	$s->drawCurveTo(-3.61,-1.84,-2.76,-1.59);
	$s->drawCurveTo(-3.78,0.18,-4.27,1.02);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"preset_cycleway");
	
	# ------ potlatch_itrain sprite
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-10.00,7.17);
	$s->drawLineTo(19.00,7.17);
	$s->drawLineTo(19.00,-10.00);
	$s->drawLineTo(-10.00,-10.00);
	$s->drawLineTo(-10.00,7.17);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(2.12,4.24);
	$s->drawCurveTo(2.16,3.21,2.88,2.64);
	$s->drawCurveTo(3.66,2.02,4.61,2.23);
	$s->drawCurveTo(7.12,2.77,6.04,5.09);
	$s->drawCurveTo(4.96,7.40,2.94,5.88);
	$s->drawCurveTo(2.16,5.29,2.12,4.24);
	$ec->add($s);
	
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(7.13,4.24);
	$s->drawCurveTo(7.16,3.21,7.89,2.64);
	$s->drawCurveTo(8.67,2.02,9.62,2.23);
	$s->drawCurveTo(12.13,2.77,11.05,5.09);
	$s->drawCurveTo(9.97,7.40,7.95,5.88);
	$s->drawCurveTo(7.17,5.29,7.13,4.24);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(15.27,-1.30);
	$s->drawCurveTo(15.76,-0.83,16.02,-0.52);
	$s->drawCurveTo(16.04,-0.49,16.06,-0.47);
	$s->drawCurveTo(16.06,-0.47,16.07,-0.46);
	$s->drawCurveTo(16.12,-0.42,16.17,-0.35);
	$s->drawCurveTo(16.71,0.33,16.15,1.11);
	$s->drawCurveTo(15.60,1.89,13.73,3.35);
	$s->drawCurveTo(13.55,3.49,13.41,3.59);
	$s->drawCurveTo(13.40,3.60,13.39,3.61);
	$s->drawCurveTo(13.34,3.64,13.28,3.67);
	$s->drawCurveTo(12.68,4.01,12.20,4.01);
	$s->drawCurveTo(11.99,4.01,-5.15,4.08);
	$s->drawCurveTo(-6.72,4.09,-8.04,4.09);
	$s->drawCurveTo(-8.16,4.09,-8.25,4.09);
	$s->drawCurveTo(-8.26,4.09,-8.26,4.09);
	$s->drawLineTo(-8.25,-7.49);
	$s->drawLineTo(6.94,-7.49);
	$s->drawCurveTo(6.94,-7.49,6.95,-7.49);
	$s->drawCurveTo(7.00,-7.49,7.08,-7.48);
	$s->drawCurveTo(7.96,-7.39,9.44,-6.39);
	$s->drawCurveTo(9.63,-6.26,10.18,-5.81);
	$s->drawCurveTo(10.23,-5.77,10.27,-5.74);
	$s->drawCurveTo(10.27,-5.73,10.28,-5.73);
	$s->drawLineTo(8.18,-5.73);
	$s->drawLineTo(12.52,-1.30);
	$s->drawLineTo(15.27,-1.30);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(6.62,-3.06);
	$s->drawLineTo(9.50,-3.06);
	$s->drawLineTo(11.29,-1.32);
	$s->drawLineTo(6.62,-1.32);
	$s->drawLineTo(6.62,-3.06);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(1.27,-1.32);
	$s->drawLineTo(-8.45,-1.32);
	$s->drawLineTo(-8.45,-3.06);
	$s->drawLineTo(1.27,-3.06);
	$s->drawLineTo(1.27,-1.32);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setLine(0.50,255,255,255);
	$s->setRightFill(190,190,190);
	$s->movePenTo(1.74,-6.13);
	$s->drawLineTo(6.22,-6.13);
	$s->drawLineTo(6.22,1.29);
	$s->drawLineTo(1.74,1.29);
	$s->drawLineTo(1.74,-6.13);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(13.00,4.20);
	$s->drawLineTo(-8.45,4.20);
	$s->drawLineTo(-8.45,3.87);
	$s->drawLineTo(13.00,3.87);
	$s->drawLineTo(13.00,4.20);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"preset_railway");
	
	# ------ potlatch_iwalking sprite
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(190,190,190);
	$s->movePenTo(-10.00,7.17);
	$s->drawLineTo(19.00,7.17);
	$s->drawLineTo(19.00,-10.00);
	$s->drawLineTo(-10.00,-10.00);
	$s->drawLineTo(-10.00,7.17);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-0.14,-0.20);
	$s->drawCurveTo(-0.80,-1.72,-0.28,-3.26);
	$s->drawCurveTo(0.44,-1.75,2.08,-1.21);
	$s->drawCurveTo(2.08,-1.21,2.27,-1.15);
	$s->drawCurveTo(3.15,-0.84,3.27,-1.13);
	$s->drawCurveTo(3.40,-1.42,3.44,-1.59);
	$s->drawCurveTo(3.48,-1.79,3.44,-1.88);
	$s->drawCurveTo(3.44,-1.88,2.99,-1.97);
	$s->drawCurveTo(2.98,-1.97,2.97,-1.97);
	$s->drawCurveTo(1.97,-2.32,1.34,-3.14);
	$s->drawCurveTo(1.34,-3.15,0.70,-4.57);
	$s->drawCurveTo(0.64,-4.68,0.59,-4.78);
	$s->drawCurveTo(0.40,-5.12,0.12,-5.46);
	$s->drawCurveTo(0.12,-5.46,-0.18,-5.73);
	$s->drawCurveTo(-0.40,-5.93,-0.40,-6.02);
	$s->drawCurveTo(-0.41,-6.06,-0.05,-6.36);
	$s->drawCurveTo(0.22,-6.58,0.32,-6.92);
	$s->drawCurveTo(1.09,-9.39,-1.26,-8.25);
	$s->drawCurveTo(-1.93,-7.93,-1.90,-7.18);
	$s->drawCurveTo(-1.90,-7.18,-1.68,-6.62);
	$s->drawCurveTo(-1.55,-6.27,-1.83,-6.25);
	$s->drawCurveTo(-3.73,-6.14,-4.82,-3.89);
	$s->drawCurveTo(-5.28,-2.97,-5.41,-1.99);
	$s->drawCurveTo(-5.45,-1.67,-5.62,-1.46);
	$s->drawCurveTo(-5.74,-1.32,-5.66,-1.11);
	$s->drawCurveTo(-5.35,-0.25,-4.52,-0.61);
	$s->drawCurveTo(-4.34,-0.69,-4.34,-0.92);
	$s->drawCurveTo(-4.34,-0.93,-4.42,-1.67);
	$s->drawCurveTo(-4.45,-1.89,-4.43,-2.06);
	$s->drawCurveTo(-4.42,-2.12,-4.41,-2.19);
	$s->drawCurveTo(-4.27,-2.81,-3.23,-3.71);
	$s->drawCurveTo(-3.07,-0.08,-6.07,3.03);
	$s->drawCurveTo(-6.07,3.03,-6.46,3.35);
	$s->drawCurveTo(-6.75,3.59,-6.77,3.69);
	$s->drawCurveTo(-6.81,3.82,-6.25,4.65);
	$s->drawCurveTo(-6.20,4.74,-6.17,4.78);
	$s->drawCurveTo(-5.95,5.20,-5.04,5.31);
	$s->drawCurveTo(-4.63,5.36,-4.96,5.08);
	$s->drawCurveTo(-4.96,5.08,-4.97,5.07);
	$s->drawCurveTo(-5.24,4.87,-5.44,4.04);
	$s->drawCurveTo(-5.47,3.90,-4.29,2.88);
	$s->drawCurveTo(-4.16,2.76,-4.11,2.71);
	$s->drawCurveTo(-2.93,1.45,-2.18,-0.20);
	$s->drawCurveTo(-0.12,2.55,0.61,5.94);
	$s->drawCurveTo(1.36,5.62,1.75,5.46);
	$s->drawCurveTo(1.75,5.45,2.16,5.32);
	$s->drawCurveTo(2.78,5.12,2.83,4.98);
	$s->drawCurveTo(2.91,4.74,2.26,4.72);
	$s->drawCurveTo(2.21,4.72,2.18,4.72);
	$s->drawCurveTo(2.18,4.72,1.62,4.66);
	$s->drawCurveTo(1.51,4.65,1.43,4.64);
	$s->drawCurveTo(1.35,4.63,1.33,4.20);
	$s->drawCurveTo(1.33,4.18,1.33,4.16);
	$s->drawCurveTo(1.21,3.05,0.86,1.91);
	$s->drawCurveTo(0.81,1.75,-0.14,-0.20);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"preset_footway");
	
	




	#		add new attribute
	
	$ec=new SWF::MovieClip();

	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawSmallCircle();
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-1.62,1.83);
	$s->drawLineTo(-3.38,1.83);
	$s->drawLineTo(-3.38,-6.83);
	$s->drawLineTo(-1.62,-6.83);
	$s->drawLineTo(-1.62,1.83);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(1.83,-3.38);
	$s->drawLineTo(1.83,-1.62);
	$s->drawLineTo(-6.83,-1.62);
	$s->drawLineTo(-6.83,-3.38);
	$s->drawLineTo(1.83,-3.38);
	$ec->add($s);
	
	$ec->nextFrame();
	$m->addExport($ec,"newattr");
	
	#		close cross
	
	$ec=new SWF::MovieClip();

	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->drawCircle(6);
	$ec->add($s);

	$s=new SWF::Shape();
	$s->setLine(40*$cw,255,255,255);
	$s->movePenTo(-2.5,-2.5); $s->drawLineTo(2.5, 2.5);
	$s->movePenTo(-2.5, 2.5); $s->drawLineTo(2.5,-2.5);
	$ec->add($s);
	$ec->nextFrame();
	$m->addExport($ec,"closecross");

	#		add new relation
	
	$ec=new SWF::MovieClip();

	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawSmallCircle();
	$ec->add($s);

	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-3.97,0.28);
	$s->drawLineTo(-7.68,0.26);
	$s->drawLineTo(-7.72,2.56);
	$s->drawLineTo(-3.70,2.56);
	$s->drawCurveTo(-2.14,2.56,-1.08,1.41);
	$s->drawLineTo(-2.22,1.42);
	$s->drawLineTo(-3.04,1.07);
	$s->drawLineTo(-3.97,0.28);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-2.91,-2.50);
	$s->drawLineTo(-4.68,-2.40);
	$s->drawCurveTo(-4.16,0.55,-1.16,0.55);
	$s->drawLineTo(2.72,0.55);
	$s->drawLineTo(2.75,-1.48);
	$s->drawLineTo(-1.13,-1.48);
	$s->drawCurveTo(-2.01,-1.49,-2.78,-2.34);
	$s->drawCurveTo(-2.85,-2.42,-2.90,-2.49);
	$s->drawCurveTo(-2.90,-2.49,-2.90,-2.49);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-3.84,-6.35);
	$s->drawCurveTo(-2.77,-7.51,-1.21,-7.51);
	$s->drawLineTo(2.70,-7.51);
	$s->drawLineTo(2.81,-5.48);
	$s->drawLineTo(-1.10,-5.48);
	$s->drawLineTo(-2.03,-6.11);
	$s->drawLineTo(-2.77,-6.28);
	$s->drawLineTo(-3.84,-6.35);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-7.81,-3.51);
	$s->drawLineTo(-3.83,-3.51);
	$s->drawCurveTo(-2.65,-3.51,-2.20,-2.88);
	$s->drawCurveTo(-2.11,-2.75,-1.56,-2.55);
	$s->drawCurveTo(-1.51,-2.53,-1.47,-2.52);
	$s->drawCurveTo(-1.46,-2.52,-1.46,-2.51);
	$s->drawLineTo(-0.01,-2.72);
	$s->drawCurveTo(-0.59,-5.53,-3.79,-5.53);
	$s->drawLineTo(-7.77,-5.53);
	$s->drawLineTo(-7.81,-3.51);
	$ec->add($s);
		
	$ec->nextFrame();
	$m->addExport($ec,"newrel");
	
	#		repeat last attributes
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawSmallCircle();
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-6.03,1.04);
	$s->drawCurveTo(-2.57,4.50,0.89,1.04);
	$s->drawLineTo(-0.45,-0.30);
	$s->drawCurveTo(-2.56,1.80,-4.69,-0.30);
	$s->drawCurveTo(-4.69,-0.30,-4.69,-0.30);
	$s->drawCurveTo(-6.30,-1.91,-5.19,-3.89);
	$s->drawCurveTo(-4.99,-4.25,-4.69,-4.55);
	$s->drawCurveTo(-2.57,-6.67,-0.45,-4.55);
	$s->drawLineTo(0.89,-5.89);
	$s->drawCurveTo(-2.57,-9.35,-6.03,-5.89);
	$s->drawCurveTo(-8.36,-3.56,-7.08,-0.51);
	$s->drawCurveTo(-6.71,0.36,-6.03,1.04);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-2.14,-3.35);
	$s->drawLineTo(2.31,-2.90);
	$s->drawLineTo(1.86,-7.35);
	$s->drawLineTo(-2.14,-3.35);
	$ec->add($s);
	
	$ec->nextFrame();
	$m->addExport($ec,"repeatattr");
	
	#		next page of attributes
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawSmallCircle();
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-6.35,1.09);
	$s->drawLineTo(-1.78,-2.64);
	$s->drawLineTo(-6.35,-6.38);
	$s->drawLineTo(-6.35,1.09);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-1.69,1.09);
	$s->drawLineTo(2.87,-2.64);
	$s->drawLineTo(-1.69,-6.38);
	$s->drawLineTo(-1.69,1.09);
	$ec->add($s);
	
	$ec->nextFrame();
	$m->addExport($ec,"nextattr");
	
	#		pointers
	#		compasses

	# -----	Set up screen layout

	#		Properties window

if (1==0) {
	$ch=new SWF::Shape();
	$ch->setLine(1,0xCC,0xCC,0xCC);
	$ch->setRightFill(0xF3,0xF3,0xF3);
	$ch->movePenTo(0,500); $ch->drawLine( 699,0);
	$ch->drawLine (0,99 ); $ch->drawLine(-699,0);
	$ch->drawLine (0,-99); $m->add($ch);
	$m->add($ch);

	$ch=new SWF::Shape();
	$ch->setLine(1,0xCC,0xCC,0xCC);
	$ch->movePen(100,500); $ch->drawLine (0,99 );
	$i=$m->add($ch);
}

	#		Map background

	#		..mask

	$maskSprite=new SWF::MovieClip();
	$maskShape =new SWF::Shape();
	$maskShape->setLine(1,0,0,0);
	$maskShape->setRightFill($maskShape->addFill(0xE0,0xE0,0xFF));
	$maskShape->movePenTo(0,0);
	$maskShape->drawLine( 3000,0); $maskShape->drawLine(0, 3000);
	$maskShape->drawLine(-3000,0); $maskShape->drawLine(0,-3000);
	$maskSprite->add($maskShape);
	$maskSprite->nextFrame();
	$i=$m->add($maskSprite);
	$i->setName("masksquare");

#	$maskSprite=new SWF::MovieClip();
#	$maskShape =new SWF::Shape();
#	$maskShape->setRightFill($maskShape->addFill(0xF3,0xF3,0xF3));
#	$maskShape->movePenTo(0,500);
#	$maskShape->drawLine( 700,0); $maskShape->drawLine(0,200);
#	$maskShape->drawLine(-700,0); $maskShape->drawLine(0,-200);
#	$maskSprite->add($maskShape);
#	$maskSprite->nextFrame();
#	$i=$m->add($maskSprite);
#	$i->setName("masksquare2");

	# ====== pointers
	
	# ------ hand pointer
	
	$ec=new SWF::MovieClip();
	
	$s=new SWF::Shape();
	$s->setLine(0.94,0,0,0);
	$s->setRightFill(255,255,255);
	$s->movePenTo(6.00,14.57);
	$s->drawLineTo(6.00,14.21);
	$s->drawCurveTo(5.98,14.04,8.06,10.29);
	$s->drawCurveTo(9.24,8.17,9.50,5.56);
	$s->drawCurveTo(9.79,2.68,8.06,2.68);
	$s->drawCurveTo(7.02,2.68,6.67,4.94);
	$s->drawCurveTo(6.64,5.15,6.62,5.32);
	$s->drawCurveTo(6.62,5.34,6.62,5.35);
	$s->drawCurveTo(6.62,5.35,6.62,2.88);
	$s->drawCurveTo(6.62,1.30,5.18,0.82);
	$s->drawCurveTo(4.13,0.48,3.96,4.60);
	$s->drawCurveTo(3.94,4.98,3.94,5.30);
	$s->drawCurveTo(3.94,5.33,3.94,5.35);
	$s->drawCurveTo(3.94,5.35,3.94,1.85);
	$s->drawCurveTo(3.94,0.00,2.29,0.00);
	$s->drawCurveTo(0.91,0.00,1.02,4.33);
	$s->drawCurveTo(1.03,4.74,1.05,5.09);
	$s->drawCurveTo(1.06,5.12,1.06,5.14);
	$s->drawCurveTo(1.06,5.13,1.06,5.13);
	$s->drawCurveTo(1.04,5.03,1.02,4.90);
	$s->drawCurveTo(0.79,3.54,0.44,2.68);
	$s->drawCurveTo(-0.45,0.45,-2.03,1.24);
	$s->drawCurveTo(-3.46,1.95,-1.78,5.47);
	$s->drawCurveTo(-1.31,6.47,-0.70,7.45);
	$s->drawCurveTo(-0.64,7.54,-0.60,7.61);
	$s->drawCurveTo(-0.59,7.61,-0.59,7.62);
	$s->drawCurveTo(-0.59,7.61,-0.59,7.61);
	$s->drawCurveTo(-0.62,7.55,-0.67,7.47);
	$s->drawCurveTo(-1.16,6.66,-2.03,5.97);
	$s->drawCurveTo(-3.26,4.98,-4.50,5.97);
	$s->drawCurveTo(-5.76,6.98,-2.24,10.50);
	$s->drawCurveTo(-1.02,11.72,-0.29,14.01);
	$s->drawCurveTo(-0.23,14.22,-0.18,14.39);
	$s->drawCurveTo(-0.18,14.40,-0.18,14.41);
	$s->drawLineTo(-0.18,14.66);
	$s->drawLineTo(6.00,14.57);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"hand");

	# ------ pen pointer
	
	$ec=new SWF::MovieClip();
	drawPen();
	$ec->nextFrame(); $m->addExport($ec,"pen");
	
	# ------ penx pointer
	
	$ec=new SWF::MovieClip();
	drawPen();
	$s=new SWF::Shape();
	$s->setLine(3,0,0,0);
	$s->movePenTo(5,18);
	$s->drawLine(5,-5); $s->movePen(-5,0); $s->drawLine(5,5);
	$ec->add($s);
	$ec->nextFrame(); $m->addExport($ec,"penx");
	
	# ------ penplus pointer
	
	$ec=new SWF::MovieClip();
	drawPen();
	$s=new SWF::Shape();
	$s->setLine(3,0,0,0);
	$s->movePenTo(6,14);
	$s->drawLine(0,5); $s->movePen(-2,-3); $s->drawLine(5,0);
	$ec->add($s);
	$ec->nextFrame(); $m->addExport($ec,"penplus");
	
	# ------ peno pointer
	
	$ec=new SWF::MovieClip();
	drawPen();
	$s=new SWF::Shape();
	$s->setLine(2,0,0,0);
	$s->setRightFill(255,255,255);
	$s->movePenTo(7,16); $s->drawCircle(2);
	$ec->add($s);
	$ec->nextFrame(); $m->addExport($ec,"peno");
	
	# ------ penso pointer (solid o)
	
	$ec=new SWF::MovieClip();
	drawPen();
	$s=new SWF::Shape();
	$s->setLine(2,0,0,0);
	$s->setRightFill(0,0,0);
	$s->movePenTo(7,16); $s->drawCircle(2);
	$ec->add($s);
	$ec->nextFrame(); $m->addExport($ec,"penso");
	



	# -----	repeated drawing instructions

	sub drawLargeCircle {
		$s->movePenTo(-10.00,0.00);
		$s->drawCurveTo(-10.00,-6.72,-3.75,-9.27);
		$s->drawCurveTo(-1.96,-10.00,0.00,-10.00);
		$s->drawCurveTo(7.41,-10.00,9.58,-2.89);
		$s->drawCurveTo(10.00,-1.48,10.00,0.00);
		$s->drawCurveTo(10.00,6.72,3.75,9.27);
		$s->drawCurveTo(1.96,10.00,0.00,10.00);
		$s->drawCurveTo(-7.41,10.00,-9.58,2.89);
		$s->drawCurveTo(-10.00,1.48,-10.00,0.00);
	}

	
	sub drawSmallCircle {
		$s->movePenTo(-10.00,-2.50);
		$s->drawCurveTo(-10.00,-8.58,-4.03,-9.84);
		$s->drawCurveTo(-3.28,-10.00,-2.50,-10.00);
		$s->drawCurveTo(3.90,-10.00,4.91,-3.67);
		$s->drawCurveTo(5.00,-3.09,5.00,-2.50);
		$s->drawCurveTo(5.00,3.58,-0.97,4.84);
		$s->drawCurveTo(-1.72,5.00,-2.50,5.00);
		$s->drawCurveTo(-8.90,5.00,-9.91,-1.33);
		$s->drawCurveTo(-10.00,-1.91,-10.00,-2.50);
	}

	sub drawPen {
		$s=new SWF::Shape();
		$s->setLine(1.08,0,0,0);
		$s->setRightFill(255,255,255);
		$s->movePenTo(-2.13,13.97);
		$s->drawLineTo(-4.50,8.76);
		$s->drawLineTo(-0.00,0.00);
		$s->drawLineTo(4.50,8.76);
		$s->drawLineTo(1.89,13.97);
		$s->drawLineTo(-2.13,13.97);
		$ec->add($s);
		
		$s=new SWF::Shape();
		$s->setLine(1.08,130,130,130);
		$s->movePenTo(-0.00,8.53);
		$s->drawLineTo(-0.00,1.90);
		$s->drawLineTo(-0.20,1.90);
		$s->drawLineTo(-0.20,8.53);
		$s->drawLineTo(-0.00,8.53);
		$ec->add($s);
		
		$s=new SWF::Shape();
		$s->setLine(1.08,0,0,0);
		$s->movePenTo(-3.79,13.97);
		$s->drawLineTo(4.03,13.97);
		$ec->add($s);
		
		$s=new SWF::Shape();
		$s->setRightFill(0,0,0);
		$s->movePenTo(2.60,14.21);
		$s->drawLineTo(-2.61,14.21);
		$s->drawLineTo(-2.61,17.53);
		$s->drawLineTo(2.60,17.53);
		$s->drawLineTo(2.60,14.21);
		$ec->add($s);
	}










# ------ potlatch_iaddress sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(1.34,255,255,255);
$s->movePenTo(-6.94,0.38);
$s->drawCurveTo(-7.81,-4.67,-2.76,-5.53);
$s->drawCurveTo(2.28,-6.40,3.15,-1.35);
$s->drawCurveTo(4.01,3.69,-1.03,4.56);
$s->drawCurveTo(-6.08,5.42,-6.94,0.38);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.67,255,255,255);
$s->movePenTo(3.26,-5.32);
$s->drawCurveTo(3.27,-5.34,3.27,-5.35);
$s->drawCurveTo(3.36,-5.49,3.48,-5.65);
$s->drawCurveTo(4.75,-7.29,6.33,-5.85);
$s->drawCurveTo(8.35,-4.01,9.40,-6.38);
$s->drawCurveTo(10.52,-8.89,12.87,-6.91);
$s->drawCurveTo(12.88,-6.90,12.89,-6.90);
$s->drawCurveTo(13.00,-6.79,13.16,-6.69);
$s->drawCurveTo(14.77,-5.64,15.92,-7.55);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.67,255,255,255);
$s->movePenTo(5.15,-3.84);
$s->drawCurveTo(5.95,-3.98,6.78,-3.21);
$s->drawCurveTo(8.80,-1.37,9.85,-3.74);
$s->drawCurveTo(10.97,-6.25,13.32,-4.28);
$s->drawCurveTo(13.33,-4.27,13.34,-4.26);
$s->drawCurveTo(13.45,-4.16,13.60,-4.06);
$s->drawCurveTo(15.22,-3.00,16.37,-4.91);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.67,255,255,255);
$s->movePenTo(5.66,-1.21);
$s->drawCurveTo(6.43,-1.31,7.23,-0.58);
$s->drawCurveTo(9.25,1.26,10.30,-1.11);
$s->drawCurveTo(11.42,-3.62,13.77,-1.64);
$s->drawCurveTo(13.78,-1.63,13.79,-1.63);
$s->drawCurveTo(13.90,-1.53,14.05,-1.43);
$s->drawCurveTo(15.67,-0.37,16.82,-2.28);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.67,255,255,255);
$s->movePenTo(4.61,2.58);
$s->drawCurveTo(4.62,2.57,4.63,2.56);
$s->drawCurveTo(4.71,2.41,4.83,2.25);
$s->drawCurveTo(6.11,0.62,7.69,2.06);
$s->drawCurveTo(9.70,3.90,10.76,1.53);
$s->drawCurveTo(11.87,-0.98,14.22,0.99);
$s->drawCurveTo(14.23,1.00,14.24,1.01);
$s->drawCurveTo(14.36,1.11,14.51,1.21);
$s->drawCurveTo(16.13,2.26,17.27,0.36);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-0.14,0.31);
$s->drawCurveTo(-0.03,1.01,0.11,1.45);
$s->drawLineTo(-0.67,1.57);
$s->drawLineTo(-0.85,0.98);
$s->drawLineTo(-0.87,0.99);
$s->drawCurveTo(-1.25,1.78,-2.23,1.93);
$s->drawCurveTo(-3.69,2.16,-3.91,0.78);
$s->drawCurveTo(-4.19,-1.02,-1.30,-1.45);
$s->drawLineTo(-1.32,-1.55);
$s->drawCurveTo(-1.50,-2.67,-2.58,-2.49);
$s->drawCurveTo(-3.31,-2.37,-3.78,-1.94);
$s->drawLineTo(-4.07,-2.49);
$s->drawCurveTo(-3.47,-3.01,-2.55,-3.15);
$s->drawCurveTo(-0.72,-3.44,-0.42,-1.48);
$s->drawLineTo(-0.14,0.31);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-1.19,-0.85);
$s->drawCurveTo(-3.23,-0.58,-3.05,0.55);
$s->drawCurveTo(-2.92,1.38,-2.11,1.25);
$s->drawCurveTo(-1.21,1.11,-1.06,0.26);
$s->drawCurveTo(-1.04,0.14,-1.06,-0.02);
$s->drawLineTo(-1.19,-0.85);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_address");

# ------ potlatch_ilanduse sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(3.38,4.72);
$s->drawLineTo(-6.53,4.72);
$s->drawLineTo(-6.53,-6.80);
$s->drawLineTo(3.38,-6.80);
$s->drawLineTo(3.38,4.72);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(15.67,-0.20);
$s->drawLineTo(4.91,-0.20);
$s->drawLineTo(4.91,-6.80);
$s->drawLineTo(15.67,-6.80);
$s->drawLineTo(15.67,-0.20);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(15.67,4.69);
$s->drawLineTo(4.91,4.69);
$s->drawLineTo(4.91,1.69);
$s->drawLineTo(15.67,1.69);
$s->drawLineTo(15.67,4.69);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_landuse");

# ------ potlatch_itrack sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(13.00,7.21);
$s->drawCurveTo(12.93,4.45,12.91,4.25);
$s->drawCurveTo(12.77,2.60,8.65,-1.01);
$s->drawCurveTo(8.49,-1.16,8.37,-1.27);
$s->drawCurveTo(5.66,-3.78,5.79,-4.92);
$s->drawCurveTo(5.86,-5.59,8.36,-6.76);
$s->drawCurveTo(8.59,-6.87,8.78,-6.95);
$s->drawCurveTo(8.80,-6.96,8.81,-6.96);
$s->drawLineTo(7.92,-7.05);
$s->drawCurveTo(7.91,-7.05,7.90,-7.04);
$s->drawCurveTo(7.72,-6.99,7.50,-6.91);
$s->drawCurveTo(5.16,-6.08,3.74,-5.18);
$s->drawCurveTo(2.20,-4.21,3.69,-1.91);
$s->drawCurveTo(3.89,-1.60,4.10,-1.36);
$s->drawCurveTo(4.79,-0.52,5.46,0.77);
$s->drawCurveTo(6.36,2.49,6.41,3.81);
$s->drawCurveTo(6.45,4.86,3.24,7.16);
$s->drawLineTo(13.00,7.21);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.67,255,255,255);
$s->movePenTo(-7.43,-6.05);
$s->drawCurveTo(-7.42,-6.06,-7.41,-6.07);
$s->drawCurveTo(-7.28,-6.17,-7.11,-6.29);
$s->drawCurveTo(-5.25,-7.56,-3.65,-7.27);
$s->drawCurveTo(-3.24,-7.20,-1.36,-6.24);
$s->drawCurveTo(-0.77,-5.94,0.02,-5.94);
$s->drawCurveTo(0.14,-5.94,3.46,-7.10);
$s->drawCurveTo(4.34,-7.41,5.03,-7.27);
$s->drawCurveTo(7.62,-6.76,8.37,-7.16);
$s->drawCurveTo(10.77,-8.47,12.15,-7.61);
$s->drawCurveTo(15.00,-5.83,16.83,-5.83);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_track");

# ------ potlatch_ilighthouse sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-7.66,-4.74);
$s->drawLineTo(-1.65,-8.52);
$s->drawLineTo(4.36,-4.74);
$s->drawLineTo(-3.54,-4.74);
$s->drawLineTo(-3.54,1.35);
$s->drawLineTo(2.64,1.35);
$s->drawLineTo(4.70,1.93);
$s->drawLineTo(4.64,2.86);
$s->drawLineTo(-7.54,2.86);
$s->drawLineTo(-7.54,1.47);
$s->drawLineTo(-4.98,1.47);
$s->drawLineTo(-4.98,-4.74);
$s->drawLineTo(-7.66,-4.74);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(16.04,-8.02);
$s->drawLineTo(1.80,-2.26);
$s->drawLineTo(16.84,-5.78);
$s->drawLineTo(16.84,-8.01);
$s->drawLineTo(16.04,-8.02);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(16.38,2.57);
$s->drawLineTo(1.86,-0.31);
$s->drawLineTo(15.26,4.37);
$s->drawLineTo(16.38,4.37);
$s->drawLineTo(16.38,2.57);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(0.88,-3.12);
$s->drawCurveTo(0.59,-3.22,0.29,-3.22);
$s->drawCurveTo(-2.31,-3.22,-2.31,-1.48);
$s->drawCurveTo(-2.31,0.25,0.29,0.25);
$s->drawCurveTo(0.68,0.25,1.03,0.09);
$s->drawCurveTo(1.02,0.08,1.01,0.08);
$s->drawCurveTo(0.90,0.03,0.78,-0.04);
$s->drawCurveTo(-0.53,-0.78,-0.53,-1.47);
$s->drawCurveTo(-0.53,-2.16,0.66,-2.98);
$s->drawCurveTo(0.77,-3.05,0.86,-3.11);
$s->drawCurveTo(0.87,-3.11,0.87,-3.11);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(2.47,5.78);
$s->drawLineTo(-5.76,5.78);
$s->drawLineTo(-5.76,1.74);
$s->drawLineTo(2.47,1.74);
$s->drawLineTo(2.47,5.78);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_landmark");

# ------ potlatch_ishopping sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-0.18,-4.92);
$s->drawLineTo(9.35,-4.92);
$s->drawCurveTo(9.38,-4.91,9.42,-4.90);
$s->drawCurveTo(9.79,-4.76,9.79,-4.34);
$s->drawCurveTo(9.79,-4.34,9.79,3.45);
$s->drawCurveTo(9.79,3.46,9.79,3.47);
$s->drawCurveTo(9.74,3.61,9.68,3.79);
$s->drawCurveTo(8.97,5.58,7.66,5.58);
$s->drawCurveTo(7.66,5.58,1.47,5.58);
$s->drawCurveTo(1.46,5.58,1.45,5.57);
$s->drawCurveTo(1.31,5.53,1.15,5.45);
$s->drawCurveTo(-0.62,4.66,-0.62,3.49);
$s->drawCurveTo(-0.62,3.49,-0.62,-4.43);
$s->drawCurveTo(-0.62,-4.47,-0.60,-4.51);
$s->drawCurveTo(-0.48,-4.92,-0.18,-4.92);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(1.34,255,255,255);
$s->movePenTo(2.63,-4.97);
$s->drawLineTo(2.63,-5.77);
$s->drawCurveTo(2.63,-5.78,2.63,-5.79);
$s->drawCurveTo(2.62,-5.92,2.63,-6.08);
$s->drawCurveTo(2.79,-7.77,4.63,-7.77);
$s->drawCurveTo(6.47,-7.77,6.59,-6.04);
$s->drawCurveTo(6.60,-5.88,6.59,-5.75);
$s->drawCurveTo(6.59,-5.73,6.59,-5.73);
$s->drawLineTo(6.59,-4.97);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(8.37,-5.38);
$s->drawCurveTo(8.37,-5.37,8.37,-5.36);
$s->drawCurveTo(8.34,-5.25,8.31,-5.10);
$s->drawCurveTo(7.97,-3.58,7.67,-3.32);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(7.94,-2.99);
$s->drawCurveTo(8.11,-2.79,7.91,-2.61);
$s->drawLineTo(6.27,-1.23);
$s->drawCurveTo(6.07,-1.06,5.90,-1.27);
$s->drawLineTo(5.35,-1.91);
$s->drawCurveTo(5.18,-2.12,5.38,-2.29);
$s->drawLineTo(7.02,-3.67);
$s->drawCurveTo(7.22,-3.84,7.39,-3.64);
$s->drawLineTo(7.94,-2.99);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_shop");

# ------ potlatch_ipostbox sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-2.13,5.78);
$s->drawLineTo(-2.13,-1.45);
$s->drawLineTo(-4.05,-4.46);
$s->drawLineTo(-4.38,-4.50);
$s->drawLineTo(-4.42,-6.29);
$s->drawCurveTo(-4.41,-6.30,-4.40,-6.31);
$s->drawCurveTo(-4.25,-6.42,-4.01,-6.56);
$s->drawCurveTo(-1.40,-8.01,4.09,-8.01);
$s->drawCurveTo(9.58,-8.01,12.90,-6.52);
$s->drawCurveTo(13.21,-6.39,13.41,-6.27);
$s->drawCurveTo(13.43,-6.26,13.44,-6.25);
$s->drawLineTo(13.48,-4.54);
$s->drawLineTo(12.90,-4.54);
$s->drawLineTo(11.44,-1.54);
$s->drawLineTo(11.44,5.86);
$s->drawLineTo(-2.13,5.78);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(8.01,3.05);
$s->drawLineTo(0.79,3.05);
$s->drawLineTo(0.79,0.38);
$s->drawLineTo(8.01,0.38);
$s->drawLineTo(8.01,3.05);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(1.00,190,190,190);
$s->movePenTo(-1.71,-1.75);
$s->drawLineTo(10.77,-1.75);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(1.00,190,190,190);
$s->movePenTo(-3.38,-4.54);
$s->drawLineTo(12.23,-4.54);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(5.87,4.85);
$s->drawLineTo(2.86,4.85);
$s->drawLineTo(2.86,6.30);
$s->drawLineTo(5.87,6.30);
$s->drawLineTo(5.87,4.85);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_utility");

# ------ potlatch_ifootball sprite

$ec=new SWF::MovieClip();

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-10.00,7.17);
$s->drawLineTo(19.00,7.17);
$s->drawLineTo(19.00,-10.00);
$s->drawLineTo(-10.00,-10.00);
$s->drawLineTo(-10.00,7.17);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(255,255,255);
$s->movePenTo(-3.56,-1.26);
$s->drawCurveTo(-3.56,-8.85,4.03,-8.85);
$s->drawCurveTo(11.62,-8.85,11.62,-1.26);
$s->drawCurveTo(11.62,6.34,4.03,6.34);
$s->drawCurveTo(-3.56,6.34,-3.56,-1.26);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(0.97,-6.34);
$s->drawCurveTo(0.97,-6.34,0.98,-6.35);
$s->drawCurveTo(1.05,-6.39,1.14,-6.44);
$s->drawCurveTo(2.07,-6.96,2.52,-7.10);
$s->drawCurveTo(2.98,-7.24,4.09,-7.50);
$s->drawCurveTo(4.19,-7.52,4.28,-7.54);
$s->drawCurveTo(4.28,-7.54,4.29,-7.54);
$s->drawCurveTo(4.29,-7.54,4.42,-7.45);
$s->drawCurveTo(5.18,-6.93,5.49,-6.62);
$s->drawCurveTo(5.79,-6.33,6.50,-5.56);
$s->drawCurveTo(6.56,-5.49,6.61,-5.43);
$s->drawCurveTo(6.62,-5.43,6.62,-5.42);
$s->drawLineTo(4.74,-2.66);
$s->drawLineTo(1.11,-3.32);
$s->drawCurveTo(1.11,-3.33,1.11,-3.34);
$s->drawCurveTo(1.09,-3.43,1.08,-3.54);
$s->drawCurveTo(0.94,-4.71,0.94,-4.95);
$s->drawCurveTo(0.94,-5.19,0.96,-6.16);
$s->drawCurveTo(0.96,-6.25,0.97,-6.33);
$s->drawCurveTo(0.97,-6.33,0.97,-6.34);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(-1.13,-0.54);
$s->drawLineTo(-3.04,-1.16);
$s->drawCurveTo(-3.04,-1.15,-3.04,-1.14);
$s->drawCurveTo(-3.05,-1.01,-3.07,-0.85);
$s->drawCurveTo(-3.22,0.88,-3.16,1.20);
$s->drawCurveTo(-3.00,2.04,-1.81,3.27);
$s->drawCurveTo(-1.44,3.65,-1.11,3.81);
$s->drawCurveTo(-1.09,3.81,-0.05,2.94);
$s->drawCurveTo(0.05,2.86,0.13,2.79);
$s->drawCurveTo(0.13,2.79,0.14,2.78);
$s->drawCurveTo(0.14,2.78,0.11,2.72);
$s->drawCurveTo(0.02,2.53,-0.05,2.39);
$s->drawCurveTo(-1.03,0.26,-1.13,-0.54);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(3.72,3.56);
$s->drawCurveTo(3.73,3.54,3.74,3.54);
$s->drawCurveTo(3.86,3.42,4.00,3.28);
$s->drawCurveTo(5.51,1.77,5.96,1.03);
$s->drawCurveTo(5.96,1.02,8.48,1.37);
$s->drawCurveTo(8.72,1.40,8.92,1.43);
$s->drawCurveTo(8.94,1.43,8.95,1.44);
$s->drawCurveTo(8.95,1.44,8.95,1.45);
$s->drawCurveTo(8.97,1.56,8.97,1.72);
$s->drawCurveTo(9.05,3.31,8.55,4.05);
$s->drawCurveTo(8.55,4.05,8.34,4.22);
$s->drawCurveTo(7.17,5.11,5.87,5.32);
$s->drawCurveTo(5.60,5.36,5.33,5.37);
$s->drawCurveTo(5.25,5.37,4.58,4.67);
$s->drawCurveTo(3.89,3.96,3.75,3.60);
$ec->add($s);

$s=new SWF::Shape();
$s->setRightFill(190,190,190);
$s->movePenTo(9.64,-4.60);
$s->drawCurveTo(9.64,-4.60,9.64,-4.61);
$s->drawCurveTo(9.67,-4.68,9.70,-4.76);
$s->drawCurveTo(10.01,-5.64,9.97,-5.94);
$s->drawCurveTo(9.99,-5.94,10.14,-5.76);
$s->drawCurveTo(11.07,-4.65,11.33,-3.37);
$s->drawCurveTo(11.59,-2.12,11.62,-1.23);
$s->drawCurveTo(11.62,-1.15,11.62,-1.10);
$s->drawCurveTo(11.62,-1.09,11.62,-1.09);
$s->drawCurveTo(11.61,-1.09,11.61,-1.09);
$s->drawCurveTo(11.56,-1.11,11.50,-1.13);
$s->drawCurveTo(10.89,-1.37,10.68,-1.37);
$s->drawCurveTo(10.68,-1.37,10.67,-1.40);
$s->drawCurveTo(10.65,-1.58,10.62,-1.77);
$s->drawCurveTo(10.29,-3.99,9.64,-4.60);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(4.63,-3.39);
$s->drawLineTo(6.09,1.43);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(-1.27,-0.38);
$s->drawLineTo(1.50,-3.89);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(1.37,-6.10);
$s->drawLineTo(-1.85,-6.83);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(4.27,-7.43);
$s->drawCurveTo(4.27,-7.43,4.27,-7.44);
$s->drawCurveTo(4.31,-7.52,4.37,-7.61);
$s->drawCurveTo(4.95,-8.65,5.54,-9.13);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(5.82,-5.46);
$s->drawCurveTo(5.84,-5.45,5.86,-5.45);
$s->drawCurveTo(6.10,-5.42,6.40,-5.38);
$s->drawCurveTo(9.61,-4.96,10.23,-4.45);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(8.77,1.72);
$s->drawCurveTo(8.78,1.70,8.79,1.69);
$s->drawCurveTo(8.92,1.51,9.09,1.29);
$s->drawCurveTo(10.86,-1.14,10.96,-2.06);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(9.81,4.46);
$s->drawCurveTo(9.81,4.46,9.80,4.46);
$s->drawCurveTo(9.72,4.43,9.62,4.39);
$s->drawCurveTo(8.54,3.99,7.89,3.91);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(3.98,3.53);
$s->drawCurveTo(3.97,3.53,3.95,3.53);
$s->drawCurveTo(3.77,3.52,3.54,3.49);
$s->drawCurveTo(1.10,3.24,-0.16,2.71);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(3.48,6.80);
$s->drawCurveTo(3.49,6.80,3.50,6.79);
$s->drawCurveTo(3.61,6.74,3.74,6.66);
$s->drawCurveTo(5.18,5.85,5.54,5.13);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(-0.24,5.84);
$s->drawCurveTo(-0.25,5.83,-0.25,5.82);
$s->drawCurveTo(-0.30,5.70,-0.37,5.56);
$s->drawCurveTo(-1.07,4.03,-1.23,3.44);
$ec->add($s);

$s=new SWF::Shape();
$s->setLine(0.73,190,190,190);
$s->movePenTo(-2.72,-0.45);
$s->drawCurveTo(-2.72,-0.45,-2.72,-0.46);
$s->drawCurveTo(-2.73,-0.50,-2.75,-0.57);
$s->drawCurveTo(-2.94,-1.25,-3.08,-1.93);
$s->drawCurveTo(-3.47,-3.89,-3.18,-4.49);
$ec->add($s);

$ec->nextFrame(); $m->addExport($ec,"preset_recreation");




    # ------ editlive sprite

    $ec=new SWF::MovieClip();

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->setRightFill(255,255,255);
    $s->movePenTo(12.56,7.80);
    $s->drawLineTo(19.00,11.73);
    $s->drawCurveTo(19.00,11.74,18.99,11.74);
    $s->drawCurveTo(18.94,11.80,18.87,11.89);
    $s->drawCurveTo(18.10,12.76,17.17,13.83);
    $s->drawCurveTo(7.05,25.41,6.29,26.77);
    $s->drawCurveTo(5.53,28.13,4.65,28.81);
    $s->drawCurveTo(4.57,28.87,4.51,28.91);
    $s->drawCurveTo(4.50,28.92,4.50,28.92);
    $s->drawCurveTo(4.47,28.91,4.44,28.90);
    $s->drawCurveTo(4.11,28.76,3.68,28.59);
    $s->drawCurveTo(-1.04,26.67,-3.91,25.16);
    $s->drawCurveTo(-6.78,23.65,-9.08,22.03);
    $s->drawCurveTo(-9.29,21.89,-9.44,21.78);
    $s->drawCurveTo(-9.45,21.77,-9.46,21.76);
    $s->drawLineTo(-7.14,19.07);
    $s->drawLineTo(5.57,5.83);
    $s->drawLineTo(12.56,7.80);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(0.56,27.49);
    $s->drawLineTo(16.67,10.48);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(8.62,14.24);
    $s->drawCurveTo(8.64,14.22,8.67,14.19);
    $s->drawCurveTo(8.97,13.92,9.35,13.59);
    $s->drawCurveTo(13.44,9.95,14.35,9.05);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(15.78,14.96);
    $s->drawLineTo(10.23,11.55);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setRightFill(169,0,0);
    $s->movePenTo(13.76,2.47);
    $s->drawLineTo(12.69,7.57);
    $s->drawLineTo(10.54,6.50);
    $s->drawLineTo(12.42,14.29);
    $s->drawLineTo(18.87,8.65);
    $s->drawLineTo(16.18,8.11);
    $s->drawLineTo(18.60,-3.97);
		$s->drawLineTo(13.76,2.47);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->setRightFill(255,255,255);
    $s->movePenTo(-10.00,18.72);
    $s->drawCurveTo(-10.00,18.71,-9.99,18.71);
    $s->drawCurveTo(-9.95,18.65,-9.89,18.58);
    $s->drawCurveTo(-9.23,17.77,-8.43,16.79);
    $s->drawCurveTo(0.28,6.11,1.64,4.75);
    $s->drawCurveTo(3.00,3.39,3.98,2.11);
    $s->drawCurveTo(4.07,1.99,4.13,1.90);
    $s->drawCurveTo(4.14,1.90,4.14,1.89);
    $s->drawCurveTo(4.14,1.89,4.16,1.89);
    $s->drawCurveTo(4.31,1.88,4.56,1.87);
    $s->drawCurveTo(6.98,1.82,8.80,2.42);
    $s->drawCurveTo(10.61,3.03,13.94,3.89);
    $s->drawCurveTo(14.25,3.97,14.49,4.03);
    $s->drawCurveTo(14.51,4.03,14.52,4.04);
    $s->drawCurveTo(14.52,4.04,14.07,4.86);
    $s->drawCurveTo(11.41,9.65,9.69,12.63);
    $s->drawCurveTo(8.03,15.50,4.88,21.28);
    $s->drawCurveTo(4.58,21.81,4.36,22.23);
    $s->drawCurveTo(4.34,22.26,4.32,22.29);
    $s->drawCurveTo(4.32,22.29,4.25,22.26);
    $s->drawCurveTo(3.99,22.14,3.80,22.06);
    $s->drawCurveTo(0.85,20.74,-1.23,20.15);
    $s->drawCurveTo(-3.34,19.54,-9.00,18.84);
    $s->drawCurveTo(-9.52,18.77,-9.93,18.72);
    $s->drawCurveTo(-9.97,18.72,-10.00,18.72);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(-4.27,19.43);
    $s->drawCurveTo(-4.25,19.39,-4.22,19.34);
    $s->drawCurveTo(-3.95,18.81,-3.61,18.15);
    $s->drawCurveTo(0.13,10.98,1.64,8.87);
    $s->drawCurveTo(3.15,6.75,6.12,2.62);
    $s->drawCurveTo(6.40,2.23,6.61,1.94);
    $s->drawCurveTo(6.63,1.91,6.65,1.89);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(10.05,2.78);
    $s->drawCurveTo(10.03,2.81,10.01,2.85);
    $s->drawCurveTo(9.79,3.23,9.51,3.71);
    $s->drawCurveTo(6.57,8.82,5.22,11.38);
    $s->drawCurveTo(3.86,13.94,1.36,19.67);
    $s->drawCurveTo(1.13,20.20,0.95,20.62);
    $s->drawCurveTo(0.93,20.65,0.92,20.68);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(-5.17,12.45);
    $s->drawCurveTo(-5.12,12.46,-5.07,12.47);
    $s->drawCurveTo(-4.52,12.58,-3.81,12.73);
    $s->drawCurveTo(3.89,14.37,7.36,16.03);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(-0.51,7.08);
    $s->drawCurveTo(-0.47,7.09,-0.41,7.10);
    $s->drawCurveTo(0.16,7.21,0.87,7.36);
    $s->drawCurveTo(8.36,8.95,10.77,10.30);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(-7.85,18.89);
    $s->drawCurveTo(-7.84,18.87,-7.82,18.83);
    $s->drawCurveTo(-7.63,18.45,-7.39,17.98);
    $s->drawCurveTo(-4.82,12.89,-3.91,11.38);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(-2.84,25.52);
    $s->drawLineTo(-0.15,22.47);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(-6.60,23.37);
    $s->drawLineTo(-5.52,22.30);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(8.26,23.73);
    $s->drawLineTo(4.86,21.40);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->movePenTo(12.02,19.43);
    $s->drawLineTo(7.36,16.75);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setRightFill(169,0,0);
    $s->movePenTo(18.60,-3.97);
    $s->drawLineTo(12.84,-1.68);
    $s->drawLineTo(16.37,-8.57);
    $s->drawLineTo(13.27,-7.85);
    $s->drawLineTo(11.36,-10.00);
    $s->drawLineTo(6.25,4.89);
    $s->drawLineTo(13.76,2.47);
    $s->drawLineTo(18.60,-3.97);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.27,0,0,0);
    $s->setRightFill(0,0,0);
    $s->movePenTo(4.32,22.30);
    $s->drawCurveTo(4.27,22.30,4.22,22.30);
    $s->drawCurveTo(3.59,22.35,2.79,22.39);
    $s->drawCurveTo(-5.84,22.82,-9.46,21.76);
    $s->drawLineTo(-7.14,19.07);
    $s->drawCurveTo(-1.45,19.62,3.49,21.89);
    $s->drawCurveTo(3.95,22.10,4.27,22.27);
    $s->drawCurveTo(4.30,22.28,4.32,22.29);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.72,255,255,255);
    $s->movePenTo(17.04,10.24);
    $s->drawLineTo(18.87,8.65);
    $s->drawLineTo(16.18,8.11);
    $s->drawLineTo(18.60,-3.97);
    $s->drawLineTo(12.84,-1.68);
    $s->drawLineTo(16.37,-8.57);
    $s->drawLineTo(13.27,-7.85);
    $s->drawLineTo(11.36,-10.00);
    $s->drawLineTo(6.25,4.89);
    $s->drawLineTo(13.76,2.47);
    $s->drawLineTo(13.55,3.47);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.72,255,255,255);
    $s->movePenTo(11.43,10.19);
    $s->drawLineTo(12.42,14.29);
    $s->drawLineTo(17.04,10.24);
    $ec->add($s);

    $ec->nextFrame(); $m->addExport($ec,"editlive");

    # ------ editwithsave sprite

    $ec=new SWF::MovieClip();

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->setRightFill(255,255,255);
    $s->movePenTo(7.05,2.03);
    $s->drawLineTo(11.92,5.01);
    $s->drawCurveTo(11.92,5.01,11.91,5.01);
    $s->drawCurveTo(11.87,5.06,11.82,5.12);
    $s->drawCurveTo(11.22,5.80,10.49,6.63);
    $s->drawCurveTo(2.88,15.35,2.31,16.37);
    $s->drawCurveTo(1.74,17.40,1.07,17.91);
    $s->drawCurveTo(1.01,17.96,0.97,17.99);
    $s->drawCurveTo(0.96,17.99,0.96,18.00);
    $s->drawCurveTo(0.94,17.99,0.92,17.98);
    $s->drawCurveTo(0.66,17.88,0.34,17.75);
    $s->drawCurveTo(-3.23,16.30,-5.40,15.15);
    $s->drawCurveTo(-7.57,14.01,-9.30,12.79);
    $s->drawCurveTo(-9.46,12.68,-9.58,12.60);
    $s->drawCurveTo(-9.59,12.59,-9.59,12.58);
    $s->drawLineTo(-7.84,10.55);
    $s->drawLineTo(1.77,0.54);
    $s->drawLineTo(7.05,2.03);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->setRightFill(0,0,0);
    $s->movePenTo(0.82,12.99);
    $s->drawCurveTo(0.79,12.99,0.75,12.99);
    $s->drawCurveTo(0.27,13.03,-0.33,13.06);
    $s->drawCurveTo(-6.85,13.38,-9.59,12.58);
    $s->drawLineTo(-7.84,10.55);
    $s->drawCurveTo(-3.54,10.96,0.20,12.68);
    $s->drawCurveTo(0.54,12.84,0.78,12.97);
    $s->drawCurveTo(0.81,12.98,0.82,12.99);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(-2.02,16.91);
    $s->drawLineTo(10.16,4.06);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(-4.59,15.42);
    $s->drawLineTo(-2.56,13.12);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(4.07,6.90);
    $s->drawCurveTo(4.09,6.88,4.11,6.86);
    $s->drawCurveTo(4.35,6.65,4.64,6.39);
    $s->drawCurveTo(7.72,3.66,8.40,2.98);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(-7.43,13.80);
    $s->drawLineTo(-6.62,12.99);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(3.80,14.07);
    $s->drawLineTo(1.23,12.31);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(6.64,10.82);
    $s->drawLineTo(3.12,8.79);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(9.48,7.44);
    $s->drawLineTo(5.29,4.87);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.54,255,255,255);
    $s->setRightFill(0,164,0);
    $s->movePenTo(10.93,-1.90);
    $s->drawCurveTo(10.93,4.11,3.95,2.05);
    $s->drawCurveTo(2.06,1.49,3.70,2.61);
    $s->drawCurveTo(9.82,6.78,12.82,3.87);
    $s->drawCurveTo(15.29,1.47,15.35,-1.47);
    $s->drawCurveTo(15.35,-1.74,15.33,-1.94);
    $s->drawCurveTo(15.33,-1.96,15.32,-1.97);
    $s->drawLineTo(19.00,-1.14);
    $s->drawLineTo(13.41,-10.00);
    $s->drawLineTo(7.63,-1.34);
    $s->drawLineTo(10.93,-1.90);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->setRightFill(255,255,255);
    $s->movePenTo(-10.00,10.28);
    $s->drawCurveTo(-10.00,10.28,-9.99,10.28);
    $s->drawCurveTo(-9.96,10.24,-9.92,10.18);
    $s->drawCurveTo(-9.42,9.57,-8.81,8.83);
    $s->drawCurveTo(-2.23,0.76,-1.21,-0.27);
    $s->drawCurveTo(-0.18,-1.30,0.57,-2.27);
    $s->drawCurveTo(0.63,-2.36,0.68,-2.42);
    $s->drawCurveTo(0.68,-2.43,0.69,-2.43);
    $s->drawCurveTo(0.69,-2.43,0.70,-2.43);
    $s->drawCurveTo(0.82,-2.44,1.00,-2.45);
    $s->drawCurveTo(2.83,-2.49,4.21,-2.03);
    $s->drawCurveTo(5.58,-1.57,8.09,-0.92);
    $s->drawCurveTo(8.33,-0.86,8.51,-0.82);
    $s->drawCurveTo(8.52,-0.82,8.53,-0.81);
    $s->drawCurveTo(8.54,-0.81,8.19,-0.19);
    $s->drawCurveTo(6.19,3.43,4.88,5.68);
    $s->drawCurveTo(3.63,7.84,1.26,12.20);
    $s->drawCurveTo(1.03,12.61,0.85,12.93);
    $s->drawCurveTo(0.84,12.96,0.83,12.99);
    $s->drawCurveTo(0.83,12.99,0.77,12.96);
    $s->drawCurveTo(0.57,12.87,0.43,12.81);
    $s->drawCurveTo(-1.80,11.81,-3.37,11.37);
    $s->drawCurveTo(-4.97,10.91,-9.24,10.37);
    $s->drawCurveTo(-9.64,10.33,-9.95,10.29);
    $s->drawCurveTo(-9.98,10.29,-10.00,10.28);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(-5.67,10.82);
    $s->drawCurveTo(-5.65,10.79,-5.64,10.76);
    $s->drawCurveTo(-5.43,10.36,-5.17,9.86);
    $s->drawCurveTo(-2.35,4.44,-1.21,2.84);
    $s->drawCurveTo(-0.06,1.24,2.19,-1.89);
    $s->drawCurveTo(2.39,-2.17,2.56,-2.40);
    $s->drawCurveTo(2.57,-2.42,2.58,-2.43);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(5.15,-1.76);
    $s->drawCurveTo(5.14,-1.74,5.13,-1.71);
    $s->drawCurveTo(4.97,-1.43,4.76,-1.08);
    $s->drawCurveTo(2.53,2.79,1.50,4.74);
    $s->drawCurveTo(0.48,6.67,-1.41,10.98);
    $s->drawCurveTo(-1.58,11.39,-1.72,11.72);
    $s->drawCurveTo(-1.74,11.75,-1.75,11.77);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(-6.35,5.55);
    $s->drawCurveTo(-6.31,5.55,-6.27,5.56);
    $s->drawCurveTo(-5.84,5.65,-5.29,5.76);
    $s->drawCurveTo(0.51,7.00,3.12,8.25);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(-2.83,1.49);
    $s->drawCurveTo(-2.80,1.49,-2.76,1.50);
    $s->drawCurveTo(-2.34,1.58,-1.82,1.69);
    $s->drawCurveTo(3.87,2.90,5.69,3.92);
    $ec->add($s);

    $s=new SWF::Shape();
    $s->setLine(0.20,0,0,0);
    $s->movePenTo(-8.38,10.42);
    $s->drawCurveTo(-8.37,10.40,-8.35,10.37);
    $s->drawCurveTo(-8.21,10.09,-8.03,9.73);
    $s->drawCurveTo(-6.08,5.88,-5.40,4.74);
    $ec->add($s);

    $ec->nextFrame(); $m->addExport($ec,"editwithsave");





	$m->writeExports();

	1;
