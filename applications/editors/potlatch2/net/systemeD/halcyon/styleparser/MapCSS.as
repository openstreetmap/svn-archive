package net.systemeD.halcyon.styleparser {

	/*
		MapCSS parser
		creates a RuleSet from a .mapcss file

	*/

	import flash.events.*;
	import flash.net.*;
	import net.systemeD.halcyon.Globals;
	import net.systemeD.halcyon.Map;

	// ** also needs to support @import rules

	public class MapCSS {
		private var map:Map;
		private var choosers:Array;

		private static const WHITESPACE:RegExp	=/^ \s+ /sx;
		private static const COMMENT:RegExp		=/^ \/* .+? *\/ \s* /sx;	/* */
		private static const CLASS:RegExp		=/^ ([\.:]\w+) \s* /sx;
		private static const ZOOM:RegExp		=/^ \| \s* z([\d\-]+) \s* /isx;
		private static const GROUP:RegExp		=/^ , \s* /isx;
		private static const CONDITION:RegExp	=/^ \[(.+?)\] \s* /sx;
		private static const OBJECT:RegExp		=/^ (\w+) \s* /sx;
		private static const DECLARATION:RegExp	=/^ \{(.+?)\} \s* /sx;
		private static const UNKNOWN:RegExp		=/^ (\S+) \s* /sx;

		private static const ZOOM_MINMAX:RegExp	=/^ (\d+)\-(\d+) $/sx;
		private static const ZOOM_MIN:RegExp	=/^ (\d+)\-      $/sx;
		private static const ZOOM_MAX:RegExp	=/^      \-(\d+) $/sx;
		private static const ZOOM_SINGLE:RegExp	=/^        (\d+) $/sx;

		private static const CONDITION_TRUE:RegExp	=/^ \s* (\w+) \s* = \s* yes \s*  $/isx;
		private static const CONDITION_FALSE:RegExp	=/^ \s* (\w+) \s* = \s* no  \s*  $/isx;
		private static const CONDITION_SET:RegExp	=/^ \s* (\w+) \s* $/sx;
		private static const CONDITION_UNSET:RegExp	=/^ \s* !(\w+) \s* $/sx;
		private static const CONDITION_EQ:RegExp	=/^ \s* (\w+) \s* =  \s* (.+) \s* $/sx;
		private static const CONDITION_NE:RegExp	=/^ \s* (\w+) \s* != \s* (.+) \s* $/sx;
		private static const CONDITION_GT:RegExp	=/^ \s* (\w+) \s* >  \s* (.+) \s* $/sx;
		private static const CONDITION_GE:RegExp	=/^ \s* (\w+) \s* >= \s* (.+) \s* $/sx;
		private static const CONDITION_LT:RegExp	=/^ \s* (\w+) \s* <  \s* (.+) \s* $/sx;
		private static const CONDITION_LE:RegExp	=/^ \s* (\w+) \s* <= \s* (.+) \s* $/sx;
		private static const CONDITION_REGEX:RegExp	=/^ \s* (\w+) \s* =~\/ \s* (.+) \/ \s* $/sx;

		private static const ASSIGNMENT:RegExp		=/^ \s* (\S+) \s* \: \s* (.+?) \s* $/sx;
		private static const SET_TAG:RegExp			=/^ \s* set \s+(\S+)\s* = \s* (.+?) \s* $/sx;
		private static const SET_TAG_TRUE:RegExp	=/^ \s* set \s+(\S+)\s* $/sx;
		private static const EXIT:RegExp			=/^ \s* exit \s* $/isx;

		private static const oZOOM:uint=2;
		private static const oGROUP:uint=3;
		private static const oCONDITION:uint=4;
		private static const oOBJECT:uint=5;
		private static const oDECLARATION:uint=6;

		private static const DASH:RegExp=/\-/g;
		private static const COLOR:RegExp=/color$/;
		private static const BOLD:RegExp=/^bold$/i;
		private static const ITALIC:RegExp=/^italic|oblique$/i;
		private static const CAPS:RegExp=/^uppercase$/i;
		private static const LINE:RegExp=/^line$/i;

		private static const CSSCOLORS:Object = {
			aliceblue:0xf0f8ff,
			antiquewhite:0xfaebd7,
			aqua:0x00ffff,
			aquamarine:0x7fffd4,
			azure:0xf0ffff,
			beige:0xf5f5dc,
			bisque:0xffe4c4,
			black:0x000000,
			blanchedalmond:0xffebcd,
			blue:0x0000ff,
			blueviolet:0x8a2be2,
			brown:0xa52a2a,
			burlywood:0xdeb887,
			cadetblue:0x5f9ea0,
			chartreuse:0x7fff00,
			chocolate:0xd2691e,
			coral:0xff7f50,
			cornflowerblue:0x6495ed,
			cornsilk:0xfff8dc,
			crimson:0xdc143c,
			cyan:0x00ffff,
			darkblue:0x00008b,
			darkcyan:0x008b8b,
			darkgoldenrod:0xb8860b,
			darkgray:0xa9a9a9,
			darkgreen:0x006400,
			darkkhaki:0xbdb76b,
			darkmagenta:0x8b008b,
			darkolivegreen:0x556b2f,
			darkorange:0xff8c00,
			darkorchid:0x9932cc,
			darkred:0x8b0000,
			darksalmon:0xe9967a,
			darkseagreen:0x8fbc8f,
			darkslateblue:0x483d8b,
			darkslategray:0x2f4f4f,
			darkturquoise:0x00ced1,
			darkviolet:0x9400d3,
			deeppink:0xff1493,
			deepskyblue:0x00bfff,
			dimgray:0x696969,
			dodgerblue:0x1e90ff,
			firebrick:0xb22222,
			floralwhite:0xfffaf0,
			forestgreen:0x228b22,
			fuchsia:0xff00ff,
			gainsboro:0xdcdcdc,
			ghostwhite:0xf8f8ff,
			gold:0xffd700,
			goldenrod:0xdaa520,
			gray:0x808080,
			green:0x008000,
			greenyellow:0xadff2f,
			honeydew:0xf0fff0,
			hotpink:0xff69b4,
			indianred :0xcd5c5c,
			indigo :0x4b0082,
			ivory:0xfffff0,
			khaki:0xf0e68c,
			lavender:0xe6e6fa,
			lavenderblush:0xfff0f5,
			lawngreen:0x7cfc00,
			lemonchiffon:0xfffacd,
			lightblue:0xadd8e6,
			lightcoral:0xf08080,
			lightcyan:0xe0ffff,
			lightgoldenrodyellow:0xfafad2,
			lightgrey:0xd3d3d3,
			lightgreen:0x90ee90,
			lightpink:0xffb6c1,
			lightsalmon:0xffa07a,
			lightseagreen:0x20b2aa,
			lightskyblue:0x87cefa,
			lightslategray:0x778899,
			lightsteelblue:0xb0c4de,
			lightyellow:0xffffe0,
			lime:0x00ff00,
			limegreen:0x32cd32,
			linen:0xfaf0e6,
			magenta:0xff00ff,
			maroon:0x800000,
			mediumaquamarine:0x66cdaa,
			mediumblue:0x0000cd,
			mediumorchid:0xba55d3,
			mediumpurple:0x9370d8,
			mediumseagreen:0x3cb371,
			mediumslateblue:0x7b68ee,
			mediumspringgreen:0x00fa9a,
			mediumturquoise:0x48d1cc,
			mediumvioletred:0xc71585,
			midnightblue:0x191970,
			mintcream:0xf5fffa,
			mistyrose:0xffe4e1,
			moccasin:0xffe4b5,
			navajowhite:0xffdead,
			navy:0x000080,
			oldlace:0xfdf5e6,
			olive:0x808000,
			olivedrab:0x6b8e23,
			orange:0xffa500,
			orangered:0xff4500,
			orchid:0xda70d6,
			palegoldenrod:0xeee8aa,
			palegreen:0x98fb98,
			paleturquoise:0xafeeee,
			palevioletred:0xd87093,
			papayawhip:0xffefd5,
			peachpuff:0xffdab9,
			peru:0xcd853f,
			pink:0xffc0cb,
			plum:0xdda0dd,
			powderblue:0xb0e0e6,
			purple:0x800080,
			red:0xff0000,
			rosybrown:0xbc8f8f,
			royalblue:0x4169e1,
			saddlebrown:0x8b4513,
			salmon:0xfa8072,
			sandybrown:0xf4a460,
			seagreen:0x2e8b57,
			seashell:0xfff5ee,
			sienna:0xa0522d,
			silver:0xc0c0c0,
			skyblue:0x87ceeb,
			slateblue:0x6a5acd,
			slategray:0x708090,
			snow:0xfffafa,
			springgreen:0x00ff7f,
			steelblue:0x4682b4,
			tan:0xd2b48c,
			teal:0x008080,
			thistle:0xd8bfd8,
			tomato:0xff6347,
			turquoise:0x40e0d0,
			violet:0xee82ee,
			wheat:0xf5deb3,
			white:0xffffff,
			whitesmoke:0xf5f5f5,
			yellow:0xffff00,
			yellowgreen:0x9acd32 };


		public function MapCSS(m:Map) {
			map=m;
		}
		
		public function parse(css:String):Array {
			var previous:uint=0;					// what was the previous CSS word?
			var sc:StyleChooser=new StyleChooser();	// currently being assembled
			choosers=new Array();

			var o:Object=new Object();
			while (css.length>0) {

				// CSS comment
				if ((o=COMMENT.exec(css))) {
					css=css.replace(COMMENT,'');

				// Whitespace (probably only at beginning of file)
				} else if ((o=WHITESPACE.exec(css))) {
					css=css.replace(WHITESPACE,'');

				// Class - .motorway, .builtup, :hover
				} else if ((o=CLASS.exec(css))) {
					if (previous==oDECLARATION) { saveChooser(sc); sc=new StyleChooser(); }

					css=css.replace(CLASS,'');
					sc.addCondition(new Condition('set',o[1]));
					previous=oCONDITION;
					Globals.vars.root.addDebug("class: "+o[1]);

				// Zoom
				} else if ((o=ZOOM.exec(css))) {
					if (previous!=oOBJECT) { sc.newObject(); }

					css=css.replace(ZOOM,'');
					var z:Array=parseZoom(o[1]);
					sc.addZoom(z[0],z[1]);
					Globals.vars.root.addDebug("zoom: "+o[1]+"->"+parseZoom(o[1]));
					previous=oZOOM;

				// Grouping - just a comma
				} else if ((o=GROUP.exec(css))) {
					css=css.replace(GROUP,'');
					sc.newGroup();
					Globals.vars.root.addDebug("group");
					previous=oGROUP;

				// Condition - [highway=primary]
				} else if ((o=CONDITION.exec(css))) {
					if (previous==oDECLARATION) { saveChooser(sc); sc=new StyleChooser(); }
					if (previous!=oOBJECT && previous!=oZOOM) { sc.newObject(); }

					css=css.replace(CONDITION,'');
					sc.addCondition(parseCondition(o[1]) as Condition);
					Globals.vars.root.addDebug("condition: "+o[1]+'->'+parseCondition(o[1]));
					previous=oCONDITION;

				// Object - way, node, relation
				} else if ((o=OBJECT.exec(css))) {
					if (previous==oDECLARATION) { saveChooser(sc); sc=new StyleChooser(); }

					css=css.replace(OBJECT,'');
					sc.newObject(o[1]);
					Globals.vars.root.addDebug("object: "+o[1]);
					previous=oOBJECT;

				// Declaration - {...}
				} else if ((o=DECLARATION.exec(css))) {
					css=css.replace(DECLARATION,'');
					Globals.vars.root.addDebug("declaration: "+o[1]);
					sc.addStyles(parseDeclaration(o[1]));
					previous=oDECLARATION;
				
				// Unknown pattern
				} else if ((o=UNKNOWN.exec(css))) {
					css=css.replace(UNKNOWN,'');
					Globals.vars.root.addDebug("unknown: "+o[1]);
					// ** do some debugging with o[1]

				} else {
					Globals.vars.root.addDebug("choked on "+css);
					return choosers;
				}
			}
			if (previous==oDECLARATION) { saveChooser(sc); sc=new StyleChooser(); }
			return choosers;
		}
		
		private function saveChooser(sc:StyleChooser):void {
Globals.vars.root.addDebug("+ saveChooser [rc="+sc.ruleChains[0][0]+"]");
			choosers.push(sc);
		};

		// Parse declaration string into list of styles

		private function parseDeclaration(s:String):Array {
			Globals.vars.root.addDebug("entering parseDeclaration with "+s); 
			var styles:Array=[];
			var t:Object=new Object();
			var o:Object=new Object();
			var a:String;

			// Create styles
			var ss:ShapeStyle =new ShapeStyle() ;
			var ps:PointStyle =new PointStyle() ; 
			var ts:TextStyle  =new TextStyle()  ; 
			var hs:ShieldStyle=new ShieldStyle(); 
			var xs:InstructionStyle=new InstructionStyle(); 

			for each (a in s.split(';')) {
				if ((o=ASSIGNMENT.exec(a))) { t[o[1].replace(DASH,'_')]=o[2]; }
				else if ((o=SET_TAG.exec(a))) { xs.addSetTag(o[1],o[2]); }
				else if ((o=SET_TAG_TRUE.exec(a))) { xs.addSetTag(o[1],true); }
				else if ((o=EXIT.exec(a))) { xs.setPropertyFromString('breaker',true); }
			}
if (xs.edited) { Globals.vars.root.addDebug("xs.set_tags is *"+xs.set_tags+"*");  }

			// Find sublayer
			var sub:uint=5;
			if (t['z_index']) { sub=Number(t['z_index']); delete t['z_index']; }
			ss.sublayer=ps.sublayer=ts.sublayer=hs.sublayer=sub;
			xs.sublayer=10;

			// Munge special values
			if (t['font_weight']   ) { if (t['font_weight'].match(BOLD)   ) { t['font_bold']=true;   } else { t['font_bold']=false;   } }
			if (t['font_style']    ) { if (t['font_style'].match(ITALIC)  ) { t['font_italic']=true; } else { t['font_italic']=false; } }
			if (t['text_transform']) { if (t['text_transform'].match(CAPS)) { t['font_caps']=true;   } else { t['font_caps']=false;   } }
			if (t['text_position'] ) { if (t['text_position'].match(LINE) ) { t['text_onpath']=true; } else { t['text_onpath']=false; } }

			// ** Do compound settings (e.g. line: 5px dotted blue;)

			// Assign each property to the appropriate style
			for (a in t) {
				// Parse properties
				// ** also do units, e.g. px/pt
				// ** convert # colours to 0x numbers
Globals.vars.root.addDebug("looking at property *"+a+"*"); 
				if (a.match(COLOR) && CSSCOLORS[t[a].toLowerCase()]) { t[a]=CSSCOLORS[t[a].toLowerCase()]; }
				
				// Set in styles
				if      (ss.hasOwnProperty(a)) { ss.setPropertyFromString(a,t[a]); Globals.vars.root.addDebug("added "+a+" to ShapeStyle"); }
				else if (ps.hasOwnProperty(a)) { ps.setPropertyFromString(a,t[a]); Globals.vars.root.addDebug("added "+a+" to PointStyle"); }
				else if (ts.hasOwnProperty(a)) { ts.setPropertyFromString(a,t[a]); Globals.vars.root.addDebug("added "+a+" to TextStyle"); }
				else if (hs.hasOwnProperty(a)) { hs.setPropertyFromString(a,t[a]); Globals.vars.root.addDebug("added "+a+" to ShieldStyle"); }
			}

			// Add each style to list
			if (ss.edited) { styles.push(ss); Globals.vars.root.addDebug("added ShapeStyle"); }
			if (ps.edited) { styles.push(ps); Globals.vars.root.addDebug("added PointStyle"); }
			if (ts.edited) { styles.push(ts); }
			if (hs.edited) { styles.push(hs); }
			if (xs.edited) { styles.push(xs); }
			return styles;
		}
		
		private function parseZoom(s:String):Array {
			var o:Object=new Object();
			if ((o=ZOOM_MINMAX.exec(s))) { return [o[1],o[2]]; }
			else if ((o=ZOOM_MIN.exec(s))) { return [o[1],map.MAXSCALE]; }
			else if ((o=ZOOM_MAX.exec(s))) { return [map.MINSCALE,o[1]]; }
			else if ((o=ZOOM_SINGLE.exec(s))) { return [o[1],o[1]]; }
			return null;
		}

		private function parseCondition(s:String):Object {
			var o:Object=new Object();
			if      ((o=CONDITION_TRUE.exec(s)))  { return new Condition('true'	,o[1]); }
			else if ((o=CONDITION_FALSE.exec(s))) { return new Condition('false',o[1]); }
			else if ((o=CONDITION_SET.exec(s)))   { return new Condition('set'	,o[1]); }
			else if ((o=CONDITION_UNSET.exec(s))) { return new Condition('unset',o[1]); }
			else if ((o=CONDITION_NE.exec(s)))    { return new Condition('ne'	,o[1],o[2]); }
			else if ((o=CONDITION_GT.exec(s)))    { return new Condition('>'	,o[1],o[2]); }
			else if ((o=CONDITION_GE.exec(s)))    { return new Condition('>='	,o[1],o[2]); }
			else if ((o=CONDITION_LT.exec(s)))    { return new Condition('<'	,o[1],o[2]); }
			else if ((o=CONDITION_LE.exec(s)))    { return new Condition('<='	,o[1],o[2]); }
			else if ((o=CONDITION_REGEX.exec(s))) { return new Condition('regex',o[1],o[2]); }
			else if ((o=CONDITION_EQ.exec(s)))    { return new Condition('eq'	,o[1],o[2]); }
			return null;
		}

	}
}
