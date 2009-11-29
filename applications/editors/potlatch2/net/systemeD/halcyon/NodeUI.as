package net.systemeD.halcyon {

	import flash.display.*;
	import flash.events.*;
	import flash.text.AntiAliasType;
	import flash.text.GridFitType;
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flash.geom.Matrix;
	import flash.geom.Point;
    import net.systemeD.halcyon.connection.Node;
    import net.systemeD.halcyon.connection.Connection;
	import net.systemeD.halcyon.styleparser.*;
	import net.systemeD.halcyon.Globals;
	
	public class NodeUI extends EntityUI {
		
        private var node:Node;
		public var loaded:Boolean=false;
		private var iconname:String='';				// name of icon
		private var heading:Number=0;				// heading within way
		private var rotation:Number=0;				// rotation applied to this POI

		public function NodeUI(node:Node, map:Map, heading:Number=0) {
			super();
			this.map = map;
			this.node = node;
			this.heading = heading;
			node.addEventListener(Connection.NODE_MOVED, nodeMoved);
		}
		
		public function nodeMoved(event:Event):void {
		    updatePosition();
		}
		
		public function redraw(sl:StyleList=null,forceDraw:Boolean=false):Boolean {
			// *** forcedraw can be removed
			var tags:Object = node.getTagsCopy();

			// special tags
			if (!node.hasParentWays) { tags[':poi']='yes'; }
            for (var stateKey:String in stateClasses) {
                tags[":"+stateKey] = 'yes';
            }

			if (!sl) { sl=map.ruleset.getStyles(this.node,tags); }

			var inWay:Boolean=node.hasParentWays;
			var hasStyles:Boolean=sl.hasStyles();
			
			removeSprites(); iconname='';
			return renderFromStyle(sl,tags);
		}

		private function renderFromStyle(sl:StyleList,tags:Object):Boolean {
			var r:Boolean=false;	// ** rendered
			var w:Number;
			var icon:Sprite;
			layer=10;
			for (var sublayer:int=10; sublayer>=0; sublayer--) {

				if (sl.pointStyles[sublayer]) {
					var s:PointStyle=sl.pointStyles[sublayer];
					r=true;
					if (s.rotation) { rotation=s.rotation; }

					if (s.icon_image!=iconname) {
						if (s.icon_image=='square') {
							// draw square
							icon=new Sprite();
							addToLayer(icon,STROKESPRITE,sublayer);
							w=styleIcon(icon,sl,sublayer);
							icon.graphics.drawRect(0,0,w,w);
							addHitSprite(w);
							updatePosition();
							iconname='_square';

						} else if (s.icon_image=='circle') {
							// draw circle
							icon=new Sprite();
							addToLayer(icon,STROKESPRITE,sublayer);
							w=styleIcon(icon,sl,sublayer);
							icon.graphics.drawCircle(w,w,w);
							addHitSprite(w);
							updatePosition();
							iconname='_circle';

						} else if (map.ruleset.images[s.icon_image]) {
							// 'load' icon (actually just from library)
							var loader:Loader = new Loader();
							loader.contentLoaderInfo.addEventListener(Event.COMPLETE, function(e:Event):void { loadedIcon(e,sublayer); } );
							loader.loadBytes(map.ruleset.images[s.icon_image]);
							iconname=s.icon_image;
						}
					} else {
						// already loaded, so just reposition
						updatePosition();
					}
				}

				// name sprite
				var a:String, t:TextStyle;
				if (sl.textStyles[sublayer]) {
					t=sl.textStyles[sublayer];
					a=tags[t.text];
				}

				if (a) { 
					var name:Sprite=new Sprite();
					addToLayer(name,NAMESPRITE);
					t.writeNameLabel(name,a,map.lon2coord(node.lon),map.latp2coord(node.latp));
				}
			}
			return r;
		}


		private function styleIcon(icon:Sprite, sl:StyleList, sublayer:uint):Number {
			loaded=true;

			// get colours
			if (sl.shapeStyles[sublayer]) {
				var s:ShapeStyle=sl.shapeStyles[sublayer];
				if (s.color) { icon.graphics.beginFill(s.color); }
				if (s.casing_width || s.casing_color!=false) {
					icon.graphics.lineStyle(s.casing_width ? s.casing_width : 1,
											s.casing_color ? s.casing_color : 0,
											s.casing_opacity ? s.casing_opacity : 1);
					// ** this appears to give casing to things that shouldn't have it
					// Globals.vars.root.addDebug("casing: "+(s.casing_width ? s.casing_width : 1)+","+(s.casing_color ? s.casing_color : 0)+","+(s.casing_opacity ? s.casing_opacity : 1)); 
				}
			}

			// return width
			return sl.pointStyles[sublayer].icon_width ? sl.pointStyles[sublayer].icon_width : 4;
		}

		private function addHitSprite(w:uint):void {
            var hitzone:Sprite = new Sprite();
            hitzone.graphics.lineStyle(4, 0x000000, 1, false, "normal", CapsStyle.ROUND, JointStyle.ROUND);
			hitzone.graphics.beginFill(0);
			hitzone.graphics.drawRect(0,0,w,w);
            addToLayer(hitzone, CLICKSPRITE);
            hitzone.visible = false;
			createListenSprite(hitzone);
		}

		private function loadedIcon(event:Event,sublayer:uint):void {
			var icon:Sprite=new Sprite();
			addToLayer(icon,STROKESPRITE,sublayer);
			icon.addChild(Bitmap(event.target.content));
			addHitSprite(icon.width);
			loaded=true;
			updatePosition();
		}

        override protected function mouseEvent(event:MouseEvent):void {
			map.entityMouseEvent(event, node);
        }

		private function updatePosition():void {
			if (!loaded) { return; }

			// ** this won't work with text objects. They have a different .x and .y
			//    and (obviously) don't need to be rotated. Needs fixing
			for (var i:uint=0; i<sprites.length; i++) {
				var d:DisplayObject=sprites[i];
				d.x=0; d.y=0; d.rotation=0;

				var m:Matrix=new Matrix();
				m.translate(-d.width/2,-d.height/2);
				m.rotate(rotation);
				m.translate(map.lon2coord(node.lon),map.latp2coord(node.latp));
				d.transform.matrix=m;
			}
		}
	}
}
