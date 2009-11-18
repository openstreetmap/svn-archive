package net.systemeD.potlatch2.controller {
	import flash.events.*;
    import net.systemeD.halcyon.Map;
    import net.systemeD.halcyon.connection.*;
    import net.systemeD.potlatch2.EditController;

    public class ControllerState {

        protected var controller:EditController;
        protected var previousState:ControllerState;

        public function ControllerState() {}
 
        public function setController(controller:EditController):void {
            this.controller = controller;
        }

        public function setPreviousState(previousState:ControllerState):void {
            if ( this.previousState == null )
                this.previousState = previousState;
        }
   
        public function processMouseEvent(event:MouseEvent, entity:Entity):ControllerState {
            return this;
        }
        
        public function processKeyboardEvent(event:KeyboardEvent):ControllerState {
            return this;
        }

		public function get map():Map {
			return controller.map;
		}

        public function enterState():void {}
        public function exitState():void {}

		public function toString():String {
			return "(No state)";
		}

    }
}
