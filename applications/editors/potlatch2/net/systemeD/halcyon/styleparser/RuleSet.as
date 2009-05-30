package net.systemeD.halcyon.styleparser {

	import org.as3yaml.*;
	import flash.events.*;
	import flash.net.*;
	import net.systemeD.halcyon.Globals;

	public class RuleSet {

		public var rules:Array=new Array();		// list of rules
		public var images:Object=new Object();	// loaded images

		// variables for name, author etc.

		// returns array of ShapeStyle,PointStyle,TextStyle,ShieldStyle
		public function getStyle(isPoint:Boolean,tags:Object,scale:uint):Array {
			var ss:ShapeStyle;
			var ps:PointStyle;
			var ts:TextStyle;
			var hs:ShieldStyle;
			for each (var rule:* in rules) {
				if ( isPoint && rule is ShapeRule) { continue; }
				if (!isPoint && rule is PointRule) { continue; }
				if (scale>rule.minScale && !isPoint) { continue; }
				if (scale<rule.maxScale && !isPoint) { continue; }
				if (rule.test(tags)) {
					if (rule is ShapeRule && rule.shapeStyle)  { ss=rule.shapeStyle; }
					if (rule is PointRule && rule.pointStyle)  { ps=rule.pointStyle; }
					if (                     rule.textStyle )  { ts=rule.textStyle; }
					if (rule is ShapeRule && rule.shieldStyle) { hs=rule.shieldStyle; }
					if (rule.breaker)     { break; }
				}
			}
			return new Array(ss,ps,ts,hs);
		}

		// Save and load rulesets

		public function save(url:String,name:String):void {

			var request:URLRequest=new URLRequest(url);
			var requestVars:URLVariables=new URLVariables();
			var loader:URLLoader=new URLLoader();
			
			// send to server
			requestVars['name']=name;
			requestVars['data']=YAML.encode(rules);
			request.data=requestVars;
			request.method = URLRequestMethod.POST;  
			loader.dataFormat = URLLoaderDataFormat.TEXT;
			loader.addEventListener(Event.COMPLETE, 					savedRuleSet,			false, 0, true);
			loader.addEventListener(HTTPStatusEvent.HTTP_STATUS,		httpStatusHandler,		false, 0, true);
			loader.addEventListener(SecurityErrorEvent.SECURITY_ERROR,	securityErrorHandler,	false, 0, true);
			loader.addEventListener(IOErrorEvent.IO_ERROR,				ioErrorHandler,			false, 0, true);
			loader.load(request);
		}

		public function load(url:String):void {

			var request:URLRequest=new URLRequest(url);
			var loader:URLLoader=new URLLoader();

			request.method=URLRequestMethod.GET;
			loader.dataFormat = URLLoaderDataFormat.TEXT;
			loader.addEventListener(Event.COMPLETE, 					loadedRuleSet,			false, 0, true);
			loader.addEventListener(HTTPStatusEvent.HTTP_STATUS,		httpStatusHandler,		false, 0, true);
			loader.addEventListener(SecurityErrorEvent.SECURITY_ERROR,	securityErrorHandler,	false, 0, true);
			loader.addEventListener(IOErrorEvent.IO_ERROR,				ioErrorHandler,			false, 0, true);
			loader.load(request);
		}

		// data handlers
		private function savedRuleSet(event:Event):void {
			var loader:URLLoader = URLLoader(event.target);  
			// do something with loader.data
		}
		
		private function loadedRuleSet(event:Event):void {
			var loader:URLLoader = URLLoader(event.target);  
			rules=YAML.decode(event.target.data) as Array;
			// ** fire some event or other to tell map to redraw
			loadImages();
		}

		private function httpStatusHandler( event:HTTPStatusEvent ):void { }
		private function securityErrorHandler( event:SecurityErrorEvent ):void { }
		private function ioErrorHandler( event:IOErrorEvent ):void { }
		
		// serialise/deserialise methods


		// ------------------------------------------------------------------------------------------------
		// Load all referenced images
		// ** currently only looks in PointRules
		// ** will duplicate if referenced twice, shouldn't
		
		public function loadImages():void {
			var ps:PointStyle;
			for each (var rule:* in rules) {
				if (!(rule is PointRule)) { continue; }
				if (!(rule.pointStyle)) { continue; }
				if (!(rule.pointStyle.icon)) { continue; }
				
				var request:URLRequest=new URLRequest(rule.pointStyle.icon);
				var loader:ImageLoader=new ImageLoader();
				loader.dataFormat=URLLoaderDataFormat.BINARY;
				loader.filename=rule.pointStyle.icon;
				loader.addEventListener(Event.COMPLETE, 					loadedImage,			false, 0, true);
				loader.addEventListener(HTTPStatusEvent.HTTP_STATUS,		httpStatusHandler,		false, 0, true);
				loader.addEventListener(SecurityErrorEvent.SECURITY_ERROR,	securityErrorHandler,	false, 0, true);
				loader.addEventListener(IOErrorEvent.IO_ERROR,				ioErrorHandler,			false, 0, true);
				loader.load(request);
			}
		}

		// data handler

		private function loadedImage(event:Event):void {
			Globals.vars.debug.appendText("Target is "+event.target+", name"+event.target.filename+"\n");
			images[event.target.filename]=event.target.data;
		}

	}
}
