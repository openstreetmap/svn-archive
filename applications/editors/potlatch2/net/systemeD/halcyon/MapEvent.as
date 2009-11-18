package net.systemeD.halcyon {

    import flash.events.Event;

    public class MapEvent extends Event {

		public static const DOWNLOAD:String = "download";
		public static const RESIZE:String = "resize";
		public static const MOVE:String = "move";
		public static const CLICK:String = "click";

		public var params:Object;

        public function MapEvent(eventname:String, params:Object) {
            super(eventname);
            this.params=params;
        }
    }

}
