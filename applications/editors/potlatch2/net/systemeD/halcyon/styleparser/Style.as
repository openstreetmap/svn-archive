package net.systemeD.halcyon.styleparser {

	import flash.utils.ByteArray;
	import flash.net.*;

	public class Style {

		public var merged:Boolean=false;
		public var edited:Boolean=false;		// true once a property has been set from a string
		public var sublayer:uint=5;
		public var evals:Object={};				// compiled SWFs for each eval. We keep it here, not in the property 
												//  | itself, so that we can retain typing for each property

		// Return an exact copy of this object
		// ** this needs some benchmarking - may be quicker to iterate over .properties, copying each one

		public function deepCopy():* {
			registerClassAlias("net.systemeD.halcyon.styleparser.ShapeStyle",ShapeStyle);
			registerClassAlias("net.systemeD.halcyon.styleparser.TextStyle",TextStyle);
			registerClassAlias("net.systemeD.halcyon.styleparser.PointStyle",PointStyle);
			registerClassAlias("net.systemeD.halcyon.styleparser.ShieldStyle",ShieldStyle);
			registerClassAlias("net.systemeD.halcyon.styleparser.InstructionStyle",InstructionStyle);
			var a:ByteArray=new ByteArray();
			a.writeObject(this);
			a.position=0;
			return (a.readObject());
		}

		// Add properties from another object

		public function mergeWith(additional:Style):void {
			for each (var prop:String in properties) {
				if (additional[prop]) {
					this[prop]=additional[prop];
				}
			}
			this.merged=true;
		}

		// Getter (to be overridden) for property list

		public function get properties():Array {
			return [];
		}
		
		// Eval handling
		
		public function hasEvals():Boolean {
			for (var k:String in evals) { return true; }
			return false;
		}
		
		public function runEvals(tags:Object):void {
			for (var k:String in evals) {
				// ** Do we need to do typing here?
				this[k]=evals[k].exec(tags);
			}
		}

		// Set property and cast as correct type (used in stylesheet imports)
		
		public function setPropertyFromString(k:String,v:*):Boolean {
			if (!this.hasOwnProperty(k)) { return false; }
			if (v is Eval) { evals[k]=v; v=1; }
			switch (typeof(this[k])) {
				case "number":	this[k]=Number(v) ; edited=true; return true;
				case "object":	// **for some reason, typeof(string class variables) returns "object".
								// We'll probably need to fix this in future if we have more complex
								// properties
				case "string":	this[k]=String(v) ; edited=true; return true;
				case "boolean":	this[k]=Boolean(v); edited=true; return true;
			}
			return false;
		}
	}
}
