package net.systemeD.halcyon.connection {

    public class Node extends Entity {
        private var _lat:Number;
        private var _latproj:Number;
        private var _lon:Number;
        private var _ways:Array = new Array();

        public function Node(id:Number, version:uint, tags:Object, lat:Number, lon:Number) {
            super(id, version, tags);
            this.lat = lat;
            this.lon = lon;
        }

        public function get lat():Number {
            return _lat;
        }

        public function get latp():Number {
            return _latproj;
        }

        public function get lon():Number {
            return _lon;
        }

        public function set lat(lat:Number):void {
            var oldLat:Number = this._lat;
            this._lat = lat;
            this._latproj = lat2latp(lat);
            markDirty();
            dispatchEvent(new NodeMovedEvent(Connection.NODE_MOVED, this, oldLat, _lon));
        }

        public function set latp(latproj:Number):void {
            this._latproj = latproj;
            this._lat = latp2lat(lat);
        }

        public function set lon(lon:Number):void {
            this._lon = lon;
        }

        public override function toString():String {
            return "Node("+id+"@"+version+"): "+lat+","+lon+" "+getTagList();
        }

        public static function lat2latp(lat:Number):Number {
            return 180/Math.PI * Math.log(Math.tan(Math.PI/4+lat*(Math.PI/180)/2));
        }

		public function latp2lat(a:Number):Number {
		    return 180/Math.PI * (2 * Math.atan(Math.exp(a*Math.PI/180)) - Math.PI/2);
		}
		
		public function get ways():Array {
		    return _ways;
		}

        public function registerAddedToWay(way:Way):void {
            if ( _ways.indexOf(way) < 0 )
                _ways.push(way);
        }
        
        public function registerRemovedFromWay(way:Way):void {
            var i:int = _ways.indexOf(way);
            if ( i >= 0 )
                _ways.splice(i, 1);
        }
        
		public override function getType():String {
			return 'node';
		}
    }

}
