package net.systemeD.halcyon.connection {
    import flash.geom.Point;

    public class Way extends Entity {
        private var nodes:Array;
		public static var entity_type:String = 'way';

        public function Way(id:Number, version:uint, tags:Object, loaded:Boolean, nodes:Array) {
            super(id, version, tags, loaded);
            this.nodes = nodes;
			for each (var node:Node in nodes) { node.addParent(this); }
        }

		public function update(version:uint, tags:Object, loaded:Boolean, nodes:Array):void {
			var node:Node;
			for each (node in this.nodes) { node.removeParent(this); }
			updateEntityProperties(version,tags,loaded); this.nodes=nodes;
			for each (node in nodes) { node.addParent(this); }
		}

        public function get length():uint {
            return nodes.length;
        }
        
        public function getNode(index:uint):Node {
            return nodes[index];
        }

        public function insertNode(index:uint, node:Node):void {
			node.addParent(this);
            nodes.splice(index, 0, node);
            markDirty();
            dispatchEvent(new WayNodeEvent(Connection.WAY_NODE_ADDED, node, this, index));
        }

        public function appendNode(node:Node):uint {
			node.addParent(this);
            nodes.push(node);
            markDirty();
            dispatchEvent(new WayNodeEvent(Connection.WAY_NODE_ADDED, node, this, nodes.length - 1));
            return nodes.length;
        }

        public function removeNode(index:uint):void {
            var removed:Array=nodes.splice(index, 1);
			removed[0].removeParent(this);
			markDirty();
            dispatchEvent(new WayNodeEvent(Connection.WAY_NODE_REMOVED, removed[0], this, index));
        }

        /**
         * Finds the 1st way segment which intersects the projected
         * coordinate and adds the node to that segment. If snap is
         * specified then the node is moved to exactly bisect the
         * segment.
         */
        public function insertNodeAtClosestPosition(newNode:Node, isSnap:Boolean):int {
            var closestProportion:Number = 1;
            var newIndex:uint = 0;
            var nP:Point = new Point(newNode.lon, newNode.latp);
            var snapped:Point = null;
            
            for ( var i:uint; i < length - 1; i++ ) {
                var node1:Node = getNode(i);
                var node2:Node = getNode(i+1);
                var p1:Point = new Point(node1.lon, node1.latp);
                var p2:Point = new Point(node2.lon, node2.latp);
                
                var directDist:Number = Point.distance(p1, p2);
                var viaNewDist:Number = Point.distance(p1, nP) + Point.distance(nP, p2);
                        
                var proportion:Number = Math.abs(viaNewDist/directDist - 1);
                if ( proportion < closestProportion ) {
                    newIndex = i+1;
                    closestProportion = proportion;
                    snapped = calculateSnappedPoint(p1, p2, nP);
                }
            }
            
            // splice in new node
            if ( isSnap ) {
                newNode.latp = snapped.y;
                newNode.lon = snapped.x;
            }
            insertNode(newIndex, newNode);
            return newIndex;
        }
        
        private function calculateSnappedPoint(p1:Point, p2:Point, nP:Point):Point {
            var w:Number = p2.x - p1.x;
            var h:Number = p2.y - p1.y;
            var u:Number = ((nP.x-p1.x) * w + (nP.y-p1.y) * h) / (w*w + h*h);
            return new Point(p1.x + u*w, p1.y+u*h);
        }
        
        public override function toString():String {
            return "Way("+id+"@"+version+"): "+getTagList()+
                     " "+nodes.map(function(item:Node,index:int, arr:Array):String {return item.id.toString();}).join(",");
        }

		public function isArea():Boolean {
			return (nodes[0].id==nodes[nodes.length-1].id && nodes.length>2);
		}

		public override function getType():String {
			return 'way';
		}
    }

}
