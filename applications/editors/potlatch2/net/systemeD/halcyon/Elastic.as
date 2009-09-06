package net.systemeD.halcyon {

	import flash.display.*;
	import flash.geom.Matrix;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	import flash.text.AntiAliasType;
	import flash.text.GridFitType;
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flash.events.*;
	import net.systemeD.halcyon.styleparser.*;
    import net.systemeD.halcyon.connection.*;

	public class Elastic {

		public var map:Map;							// reference to parent map
		public var sprites:Array=new Array();		// instances in display list
        private var _start:Point;
        private var _end:Point;

		public function Elastic(map:Map, start:Point, end:Point) {
			this.map = map;
			this._start = start;
			this._end = end;
			redraw();
		}
		
		public function set start(start:Point):void {
		    this._start = start;
		    redraw();
		}

		public function set end(end:Point):void {
		    this._end = end;
		    redraw();
		}
		
		public function get start():Point {
		    return _start;
		}
		
		public function get end():Point {
		    return _end;
		}
		
		public function removeSprites():void {
			// Remove all currently existing sprites
			while (sprites.length>0) {
				var d:DisplayObject=sprites.pop(); d.parent.removeChild(d);
			}
        }
        
		public function redraw():void {
		    removeSprites();

			// Iterate through each sublayer, drawing any styles on that layer
			var p0:Point = start;
			var p1:Point = end;

			// Create stroke object
			var stroke:Shape = new Shape();
            stroke.graphics.lineStyle(1, 0xff0000, 1, false, "normal", CapsStyle.ROUND, JointStyle.ROUND);
			addToLayer(stroke,2);
			dashedLine(stroke.graphics, [2,2]);
			
			var nodes:Sprite = new Sprite();
            drawNodes(nodes.graphics);
            addToLayer(nodes, 2);

		}
		
		// ------------------------------------------------------------------------------------------
		// Drawing support functions

		private function drawNodes(g:Graphics):void {
            g.lineStyle(1, 0xff0000, 1, false, "normal", CapsStyle.ROUND, JointStyle.ROUND);
			for (var i:uint = 0; i < 1; i++) {
                var p:Point = i == 0 ? start : end;
                var x:Number = map.lon2coord(p.x);
                var y:Number = map.latp2coord(p.y);
                g.moveTo(x-2, y-2);
                g.lineTo(x+2, y-2);
                g.lineTo(x+2, y+2);
                g.lineTo(x-2, y+2);
                g.lineTo(x-2, y-2);
			}
		}

		// Draw dashed polyline
		
		private function dashedLine(g:Graphics,dashes:Array):void {
			var draw:Boolean=false, dashleft:Number=0, dc:Array=new Array();
			var a:Number, xc:Number, yc:Number;
			var curx:Number, cury:Number;
			var dx:Number, dy:Number, segleft:Number=0;
 			var i:int=0;

            var p0:Point = start;
            var p1:Point = end;
 			g.moveTo(map.lon2coord(p0.x), map.latp2coord(p0.y));
			while (i < 1 || segleft>0) {
				if (dashleft<=0) {	// should be ==0
					if (dc.length==0) { dc=dashes.slice(0); }
					dashleft=dc.shift();
					draw=!draw;
				}
				if (segleft<=0) {	// should be ==0
					curx=map.lon2coord(p0.x);
                    dx=map.lon2coord(p1.x)-curx;
					cury=map.latp2coord(p0.y);
                    dy=map.latp2coord(p1.y)-cury;
					a=Math.atan2(dy,dx); xc=Math.cos(a); yc=Math.sin(a);
					segleft=Math.sqrt(dx*dx+dy*dy);
					i++;
				}

				if (segleft<=dashleft) {
					// the path segment is shorter than the dash
		 			curx+=dx; cury+=dy;
					moveLine(g,curx,cury,draw);
					dashleft-=segleft; segleft=0;
				} else {
					// the path segment is longer than the dash
					curx+=dashleft*xc; dx-=dashleft*xc;
					cury+=dashleft*yc; dy-=dashleft*yc;
					moveLine(g,curx,cury,draw);
					segleft-=dashleft; dashleft=0;
				}
			}
		}

		private function moveLine(g:Graphics,x:Number,y:Number,draw:Boolean):void {
			if (draw) { g.lineTo(x,y); }
				 else { g.moveTo(x,y); }
		}

		
		// Add object (stroke/fill/roadname) to layer sprite
		
		private function addToLayer(s:DisplayObject,t:uint,sublayer:int=-1):void {
			var l:DisplayObject=Map(map).getChildAt(5);
			var o:DisplayObject=Sprite(l).getChildAt(t);
			if (sublayer!=-1) { o=Sprite(o).getChildAt(sublayer); }
			Sprite(o).addChild(s);
			sprites.push(s);
            if ( s is Sprite ) {
                Sprite(s).mouseEnabled = false;
                Sprite(s).mouseChildren = false;
            }
		}
	}
}
