package net.systemeD.halcyon.mapfeatures {

    import flash.events.EventDispatcher;
    import flash.events.Event;
    import flash.net.URLLoader;
    import flash.net.URLRequest;

	import flash.system.Security;
	import flash.net.*;

    import net.systemeD.halcyon.connection.*;


	public class MapFeatures extends EventDispatcher {
        private static var instance:MapFeatures;

        public static function getInstance():MapFeatures {
            if ( instance == null ) {
                instance = new MapFeatures();
                instance.loadFeatures();
            }
            return instance;
        }



        private var xml:XML = null;
        private var _features:Array = null;
        private var _categories:Array = null;

        protected function loadFeatures():void {
            var request:URLRequest = new URLRequest("map_features.xml");
            var loader:URLLoader = new URLLoader();
            loader.addEventListener(Event.COMPLETE, onFeatureLoad);
            loader.load(request);
        }

        private function onFeatureLoad(event:Event):void {
            xml = new XML(URLLoader(event.target).data);
            
            _features = new Array();
            for each(var feature:XML in xml.feature) {
                _features.push(new Feature(feature));
            }            
            _categories = new Array();
            for each(var catXML:XML in xml.category) {
                if ( catXML.child("category").length() == 0 )
                  _categories.push(new Category(this, catXML.@name, catXML.@id));
            }
            dispatchEvent(new Event("featuresLoaded"));
        }

        public function hasLoaded():Boolean {
            return xml != null;
        }

        public function findMatchingFeature(entity:Entity):Feature {
            if ( xml == null )
                return null;

            for each(var feature:Feature in features) {
                // check for matching tags
                var match:Boolean = true;
                for each(var tag:Object in feature.tags) {
                    var entityTag:String = entity.getTag(tag.k);
                    match = entityTag == tag.v || (entityTag != null && tag.v == "*");
                    if ( !match ) break;
                }
                if ( match )
                    return feature;
            }
            return null;
        }
        
        [Bindable(event="featuresLoaded")]
        public function get categories():Array {
            if ( xml == null )
                return null;            
            return _categories;
        }

        [Bindable(event="featuresLoaded")]
        public function get features():Array {
            if ( xml == null )
                return null;            
            return _features;
        }

    }

}


