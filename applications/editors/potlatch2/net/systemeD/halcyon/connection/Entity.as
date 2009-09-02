package net.systemeD.halcyon.connection {

    import flash.events.EventDispatcher;
    import flash.utils.Dictionary;

    public class Entity extends EventDispatcher {
        private var _id:Number;
        private var _version:uint;
        private var tags:Object = {};
        private var modified:Boolean = false;
		private var parents:Dictionary = new Dictionary();

        public function Entity(id:Number, version:uint, tags:Object) {
            this._id = id;
            this._version = version;
            this.tags = tags;
            modified = id < 0;
        }

        public function get id():Number {
            return _id;
        }

        public function get version():uint {
            return _version;
        }

		// Tag-handling methods

        public function hasTags():Boolean {
            for (var key:String in tags)
                return true;
            return false;
        }

        public function getTag(key:String):String {
            return tags[key];
        }

        public function setTag(key:String, value:String):void {
            var old:String = tags[key];
            if ( old != value ) {
                if ( value == null || value == "" )
                    delete tags[key];
                else
                    tags[key] = value;
                markDirty();
                dispatchEvent(new TagEvent(Connection.TAG_CHANGE, this, key, key, old, value));
            }
        }

        public function renameTag(oldKey:String, newKey:String):void {
            var value:String = tags[oldKey];
            if ( oldKey != newKey ) {
                delete tags[oldKey];
                tags[newKey] = value;
                markDirty();
                dispatchEvent(new TagEvent(Connection.TAG_CHANGE, this, oldKey, newKey, value, value));
            }
        }

        public function getTagList():TagList {
            return new TagList(tags);
        }

        public function getTagsCopy():Object {
            var copy:Object = {};
            for (var key:String in tags )
                copy[key] = tags[key];
            return copy;
        }

		public function getTagsHash():Object {
			// hm, not sure we should be doing this, but for read-only purposes
			// it's faster than using getTagsCopy
			return tags;
		}

        public function getTagArray():Array {
            var copy:Array = [];
            for (var key:String in tags )
                copy.push(new Tag(this, key, tags[key]));
            return copy;
        }

		// Clean/dirty methods

        public function get isDirty():Boolean {
            return modified;
        }

        public function markClean(newID:Number, newVersion:uint):void {
            this._id = newID;
            this._version = newVersion;
            modified = false;
        }

        protected function markDirty():void {
            modified = true;
        }

		// Parent handling
		
		public function addParent(parent:Entity):void {
			parents[parent]=true;
		}

		public function removeParent(parent:Entity):void {
			delete parents[parent];
		}
		
		public function get parentWays():Array {
			var a:Array=[];
			for (var o:Object in parents) {
				if (o is Way) { a.push(o); }
			}
			return a;
		}
		
		public function get parentRelations():Array {
			var a:Array=[];
			for (var o:Object in parents) {
				if (o is Relation) { a.push(o); }
			}
			return a;
		}
		
		public function get parentObjects():Array {
			var a:Array=[];
			for (var o:Object in parents) { a.push(o); }
			return a;
		}

		// To be overridden

        public function getType():String {
            return '';
        }

    }

}
