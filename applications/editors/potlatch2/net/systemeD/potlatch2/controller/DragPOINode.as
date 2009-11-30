package net.systemeD.potlatch2.controller {
	import flash.events.*;
    import net.systemeD.potlatch2.EditController;
    import net.systemeD.halcyon.connection.*;
	import net.systemeD.halcyon.Globals;

    public class DragPOINode extends ControllerState {
        private var selectedNode:Node;
        private var isDraggingStarted:Boolean = false;
		private var isNew:Boolean = false;

        private var downX:Number;
        private var downY:Number;
		private var dragstate:uint=NOT_MOVED;
		private const NOT_DRAGGING:uint=0;
		private const NOT_MOVED:uint=1;
		private const DRAGGING:uint=2;
        
        public function DragPOINode(node:Node, event:MouseEvent, newNode:Boolean) {
            selectedNode = node;
            downX = event.localX;
            downY = event.localY;
			isNew = newNode;
        }
 
       override public function processMouseEvent(event:MouseEvent, entity:Entity):ControllerState {

            if (event.type==MouseEvent.MOUSE_UP) {
               	return new SelectedPOINode(selectedNode);

			} else if ( event.type == MouseEvent.MOUSE_MOVE) {
				// dragging
				if (dragstate==NOT_DRAGGING) {
					return this;
				} else if (dragstate==NOT_MOVED && Math.abs(downX - event.localX) < 3 && Math.abs(downY - event.localY) < 3) {
					return this;
				}
				dragstate=DRAGGING;
                return dragTo(event);

			} else {
				// event not handled
                return this;
			}
        }

        private function dragTo(event:MouseEvent):ControllerState {
            selectedNode.lat = controller.map.coord2lat(event.localY);
            selectedNode.lon = controller.map.coord2lon(event.localX);
            return this;
        }
        
		public function forceDragStart():void {
			dragstate=NOT_MOVED;
		}

        override public function enterState():void {
            controller.map.setHighlight(selectedNode, { highlight: true } );
			Globals.vars.root.addDebug("**** -> "+this);
        }
        override public function exitState():void {
            controller.map.setHighlight(selectedNode, { highlight: false } );
			Globals.vars.root.addDebug("**** <- "+this);
        }
        override public function toString():String {
            return "DragPOINode";
        }
    }
}
