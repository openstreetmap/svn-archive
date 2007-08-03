
	# updated 2.8.2007 - peno works correctly

	# ----- Export symbols

	#		Empty movie-clip for ways
	
	$ec=new SWF::Sprite();
	$ec->nextFrame();
	$m->addExport($ec,"way");

	#		Empty movie-clip for key/value pairs
	
	$ec=new SWF::Sprite();
	$ec->nextFrame();
	$m->addExport($ec,"keyvalue");

	#		Empty movie-clip for UI components

	$ec=new SWF::Sprite(); $ec->nextFrame(); $m->addExport($ec,"menu");
	$ec=new SWF::Sprite(); $ec->nextFrame(); $m->addExport($ec,"checkbox");

	#		Anchor (selected)

	$ec=new SWF::Sprite();
	$ch=new SWF::Shape();
	$ch->setRightFill(255,0,0); $ch->movePenTo(-2,-2);
	$ch->drawLine( 4,0); $ch->drawLine(0, 4);
	$ch->drawLine(-4,0); $ch->drawLine(0,-4);
	$ec->add($ch); $ec->nextFrame();
	$m->addExport($ec,"anchor");

	#		Anchor (mouseover)

	$ec=new SWF::Sprite();
	$ch=new SWF::Shape();
	$ch->setRightFill(0,0,255); $ch->movePenTo(-2,-2);
	$ch->drawLine( 4,0); $ch->drawLine(0, 4);
	$ch->drawLine(-4,0); $ch->drawLine(0,-4);
	$ec->add($ch); $ec->nextFrame();
	$m->addExport($ec,"anchorhint");

	#		Zoom in

	$ec=new SWF::Sprite();
	$bq=new SWF::Shape();
	$bq->setRightFill($bq->addFill(0,0,0x8b));
	$bq->movePenTo(0,20);
	$bq->drawLineTo(0,10);
	$bq->drawCurveTo(0,0,10,0);
	$bq->drawCurveTo(20,0,20,10);
	$bq->drawLineTo(20,20);
	$bq->drawLineTo(0,20);
	$bq->setLine(50,255,255,255);
	$bq->movePenTo(5,9); $bq->drawLineTo(15,9);
	$bq->drawLineTo(10,9); $bq->drawLineTo(10,4); $bq->drawLineTo(10,14);
	$bq->drawLineTo(10,9); $bq->drawLineTo(5,9);
	$ec->add($bq); $ec->nextFrame();
	$m->addExport($ec,"zoomin");
	
	#		Zoom out

	$ec=new SWF::Sprite();
	$bq=new SWF::Shape();
	$bq->setRightFill($bq->addFill(0,0,0x8b));
	$bq->drawLineTo(0,10);
	$bq->drawCurveTo(0,20,10,20);
	$bq->drawCurveTo(20,20,20,10);
	$bq->drawLineTo(20,0);
	$bq->drawLineTo(0,0);
	$bq->setLine(50,255,255,255);
	$bq->movePenTo(6,9); $bq->drawLineTo(14,9);
	$ec->add($bq); $ec->nextFrame();
	$m->addExport($ec,"zoomout");

	# ------ exclamation sprite
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();
	
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
	
	# ------ potlatch_roundabout sprite
	
	$ec=new SWF::Sprite();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(-7.44,-0.00);
	$s->drawLineTo(-6.04,-1.34);
	$s->drawCurveTo(-6.04,-1.33,-6.03,-1.33);
	$s->drawCurveTo(-5.94,-1.26,-5.84,-1.18);
	$s->drawCurveTo(-4.73,-0.35,-4.71,-0.48);
	$s->drawCurveTo(-4.28,-4.74,0.00,-4.74);
	$s->drawCurveTo(0.53,-4.74,1.04,-4.62);
	$s->drawLineTo(2.60,-5.36);
	$s->drawLineTo(2.17,-7.12);
	$s->drawCurveTo(1.12,-7.44,0.00,-7.44);
	$s->drawCurveTo(-5.96,-7.44,-7.27,-1.61);
	$s->drawCurveTo(-7.44,-0.82,-7.44,-0.00);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(3.68,6.42);
	$s->drawLineTo(1.82,5.88);
	$s->drawLineTo(2.33,4.08);
	$s->drawCurveTo(-1.77,6.45,-4.14,2.34);
	$s->drawCurveTo(-4.47,1.77,-4.63,1.14);
	$s->drawCurveTo(-4.66,1.02,-5.98,0.41);
	$s->drawCurveTo(-6.11,0.35,-6.21,0.31);
	$s->drawCurveTo(-6.22,0.30,-6.23,0.30);
	$s->drawCurveTo(-6.23,0.30,-6.23,0.30);
	$s->drawCurveTo(-6.29,0.35,-6.40,0.47);
	$s->drawCurveTo(-7.34,1.42,-7.32,1.54);
	$s->drawCurveTo(-7.07,2.68,-6.48,3.70);
	$s->drawCurveTo(-3.31,9.20,2.64,6.92);
	$s->drawCurveTo(3.18,6.71,3.68,6.42);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(3.73,-6.48);
	$s->drawLineTo(4.19,-4.60);
	$s->drawCurveTo(4.18,-4.60,4.17,-4.60);
	$s->drawCurveTo(4.07,-4.55,3.95,-4.50);
	$s->drawCurveTo(2.66,-3.96,2.77,-3.88);
	$s->drawCurveTo(5.52,-1.91,4.54,1.34);
	$s->drawCurveTo(4.38,1.86,4.11,2.33);
	$s->drawCurveTo(3.97,2.57,3.81,2.79);
	$s->drawCurveTo(3.71,2.92,3.46,4.63);
	$s->drawCurveTo(3.43,4.79,3.42,4.92);
	$s->drawCurveTo(3.41,4.93,3.41,4.94);
	$s->drawCurveTo(3.41,4.94,3.42,4.94);
	$s->drawCurveTo(3.50,4.97,3.66,5.02);
	$s->drawCurveTo(5.04,5.45,5.17,5.33);
	$s->drawCurveTo(5.92,4.60,6.45,3.68);
	$s->drawCurveTo(7.79,1.37,7.34,-1.29);
	$s->drawCurveTo(6.75,-4.74,3.73,-6.48);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"roundabout");

	#		Scissors (auto-generated from AI-to-Ming script)

	$ec=new SWF::Sprite();

	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(127,127,127));
	drawLargeCircle();
	$ec->add($s);
	
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
	$ec->add($s);
	
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
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(255,255,255));
	$s->movePenTo(-0.92,-0.73);
	$s->drawLineTo(-0.24,0.41);
	$s->drawLineTo(-4.42,7.68);
	$s->drawCurveTo(-4.79,6.77,-4.79,5.94);
	$s->drawCurveTo(-4.79,5.60,-4.73,5.22);
	$s->drawLineTo(-0.92,-0.73);
	$ec->add($s);
	
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
	$ec->add($s);
	
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
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill($s->addFill(127,127,127));
	$s->movePenTo(-0.38,-1.25);
	$s->drawCurveTo(-0.38,-0.89,-0.04,-0.89);
	$s->drawCurveTo(0.30,-0.89,0.30,-1.25);
	$s->drawCurveTo(0.30,-1.60,-0.04,-1.60);
	$s->drawCurveTo(-0.38,-1.60,-0.38,-1.25);
	$ec->add($s);
	
	$ec->nextFrame();
	$m->addExport($ec,"scissors");

	#		GPS

	$ec=new SWF::Sprite();

	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
	$ec->add($s);
	
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
	$ec->add($s);
	
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
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->movePenTo(4.09,-4.36);
	$s->drawCurveTo(3.94,-3.95,4.32,-3.81);
	$s->drawCurveTo(4.70,-3.67,4.85,-4.07);
	$s->drawCurveTo(5.00,-4.47,4.62,-4.61);
	$s->drawCurveTo(4.24,-4.75,4.09,-4.36);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	$s->movePenTo(2.77,-3.40);
	$s->drawCurveTo(2.62,-3.00,3.00,-2.86);
	$s->drawCurveTo(3.39,-2.71,3.54,-3.12);
	$s->drawCurveTo(3.68,-3.52,3.30,-3.66);
	$s->drawCurveTo(2.92,-3.80,2.77,-3.40);
	$ec->add($s);

	$ec->nextFrame();
	$m->addExport($ec,"gps");
	
	#		Prefs

	$ec=new SWF::Sprite();
	
	$s=new SWF::Shape();
	$s->setRightFill(127,127,127);
	drawLargeCircle();
	$ec->add($s);
	
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
	$ec->add($s);
	
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
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"prefs");



	#Ê=====	Menu icons
		
	# ------ potlatch_inatural sprite
	
	$ec=new SWF::Sprite();
	
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
	$s->movePenTo(-2.98,-8.43);
	$s->drawLineTo(-8.37,2.55);
	$s->drawLineTo(-4.82,3.05);
	$s->drawLineTo(-4.73,5.64);
	$s->drawLineTo(-2.52,5.72);
	$s->drawLineTo(-2.40,3.01);
	$s->drawLineTo(1.15,2.76);
	$s->drawLineTo(-2.98,-8.43);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(4.86,-2.63);
	$s->drawLineTo(1.63,3.95);
	$s->drawLineTo(3.76,4.25);
	$s->drawLineTo(3.81,5.80);
	$s->drawLineTo(5.14,5.85);
	$s->drawLineTo(5.21,4.23);
	$s->drawLineTo(7.34,4.08);
	$s->drawLineTo(4.86,-2.63);
	$ec->add($s);
	
	$s=new SWF::Shape();
	$s->setRightFill(255,255,255);
	$s->movePenTo(12.96,-8.22);
	$s->drawLineTo(7.58,2.76);
	$s->drawLineTo(11.12,3.26);
	$s->drawLineTo(11.21,5.85);
	$s->drawLineTo(13.42,5.93);
	$s->drawLineTo(13.54,3.22);
	$s->drawLineTo(17.09,2.97);
	$s->drawLineTo(12.96,-8.22);
	$ec->add($s);
	
	$ec->nextFrame(); $m->addExport($ec,"preset_natural");

	# ----- potlatch_iboat sprite
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();

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
	
	#		repeat last attributes
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();
	
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

	$m->writeExports();

	# -----	Set up screen layout

	#		Properties window

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

#	#		Centre crosshair
	
#	$ch=new SWF::Shape();
#	$ch->setLine(1,0xFF,0xFF,0xFF);
#	$ch->movePenTo(-2,0); $ch->drawLine(-10,0);
#	$ch->movePenTo( 2,0); $ch->drawLine( 10,0);
#	$ch->movePenTo(0,-2); $ch->drawLine(0,-10);
#	$ch->movePenTo(0, 2); $ch->drawLine(0, 10);
#	$i=$m->add($ch); $i->moveTo(350,250); $i->setDepth(5002);
	
	#		Map background

	#		..mask

	$maskSprite=new SWF::MovieClip();
	$maskShape =new SWF::Shape();
	$maskShape->setLine(1,0,0,0);
	$maskShape->setRightFill($maskShape->addFill(0xE0,0xE0,0xFF));
	$maskShape->movePenTo(0,0);
	$maskShape->drawLine( 700,0); $maskShape->drawLine(0,500);
	$maskShape->drawLine(-700,0); $maskShape->drawLine(0,-500);
	$maskSprite->add($maskShape);
	$maskSprite->nextFrame();
	$i=$m->add($maskSprite);
	$i->setName("masksquare");

	$maskSprite=new SWF::MovieClip();
	$maskShape =new SWF::Shape();
	$maskShape->setRightFill($maskShape->addFill(0xF3,0xF3,0xF3));
	$maskShape->movePenTo(0,500);
	$maskShape->drawLine( 700,0); $maskShape->drawLine(0,200);
	$maskShape->drawLine(-700,0); $maskShape->drawLine(0,-200);
	$maskSprite->add($maskShape);
	$maskSprite->nextFrame();
	$i=$m->add($maskSprite);
	$i->setName("masksquare2");


	# ====== pointers
	
	# ------ hand pointer
	
	$ec=new SWF::Sprite();
	
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
	
	$ec=new SWF::Sprite();
	drawPen();
	$ec->nextFrame(); $m->addExport($ec,"pen");
	
	# ------ penx pointer
	
	$ec=new SWF::Sprite();
	drawPen();
	$s=new SWF::Shape();
	$s->setLine(3,0,0,0);
	$s->movePenTo(5,18);
	$s->drawLine(5,-5); $s->movePen(-5,0); $s->drawLine(5,5);
	$ec->add($s);
	$ec->nextFrame(); $m->addExport($ec,"penx");
	
	# ------ penplus pointer
	
	$ec=new SWF::Sprite();
	drawPen();
	$s=new SWF::Shape();
	$s->setLine(3,0,0,0);
	$s->movePenTo(6,14);
	$s->drawLine(0,5); $s->movePen(-2,-3); $s->drawLine(5,0);
	$ec->add($s);
	$ec->nextFrame(); $m->addExport($ec,"penplus");
	
	# ------ peno pointer
	
	$ec=new SWF::Sprite();
	$s=new SWF::Shape();
	drawPen();
	$s->setLine(2,0,0,0);
	$s->setRightFill(255,255,255);
	$s->movePenTo(7,16); $s->drawCircle(2);
	$ec->nextFrame(); $m->addExport($ec,"peno");
	
	# ------ penso pointer (solid o)
	
	$ec=new SWF::Sprite();
	$s=new SWF::Shape();
	drawPen();
	$s->setLine(2,0,0,0);
	$s->movePenTo(7,16); $s->drawCircle(2);
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

	1;
