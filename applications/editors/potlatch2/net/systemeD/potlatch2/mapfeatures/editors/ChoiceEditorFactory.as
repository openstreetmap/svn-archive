package net.systemeD.potlatch2.mapfeatures.editors {

    import net.systemeD.halcyon.connection.*;
    import net.systemeD.potlatch2.mapfeatures.*;
    import flash.display.*;

	public class ChoiceEditorFactory extends SingleTagEditorFactory {
	    public var choices:Array;
        
        public function ChoiceEditorFactory(inputXML:XML) {
            super(inputXML);
            
            choices = [];
            for each( var choiceXML:XML in inputXML.choice ) {
                var choice:Object = {};
                choice["value"] = String(choiceXML.@value);
                choice["description"] = String(choiceXML.@description);
                choice["label"] = String(choiceXML.@text);
                choice["icon"] = choiceXML.hasOwnProperty("@icon") ? String(choiceXML.@icon) : null;
                choices.push(choice);
            }
        }
        
        override protected function createSingleTagEditor():SingleTagEditor {
            return new ChoiceEditor();
        }
    }

}


