
function ChangeFeatureDialog (selFeature,routeTypes,upCB,canCB,map)
{
    this.currentSelectedFeature=selFeature;
    this.tempTags = this.currentSelectedFeature.osmitem.tags;
    var t = this.currentSelectedFeature.osmitem.type;
	this.tempType=t;
    var f = (selFeature.osmitem instanceof OpenLayers.OSMWay ) ?
        routeTypes.getTypes('line'): routeTypes.getTypes('point');
    var self = this;
    this.uploadCallback = upCB;
    this.cancelCallback = canCB;
    this.routeTypes = routeTypes;

    this.populateFullTags = function()
    {
        var selectbox = document.getElementById('tagkey');

        while(selectbox.options.length > 0)
            selectbox.options[0] = null;

        for(var tag in self.tempTags)
        {
            if(self.tempTags[tag])
            {
                selectbox.options[selectbox.options.length]= 
                            new Option(tag,tag);
            }
        }
        document.getElementById('tagvalue').value =     
                self.tempTags [document.getElementById('tagkey').value];
    }

    this.typeChange = function()
    {
        var t = (document.getElementById('ftype').value=="--Select--")?
                    "unknown" : document.getElementById('ftype').value;
        var newTags = self.routeTypes.getTags(t);
        self.tempTags = newTags;
        self.tempType=t;
        self.populateFullTags();
    }


    this.tagSelectChange = function()
    {
        document.getElementById('tagvalue').value =     
                self.tempTags [document.getElementById('tagkey').value];
    }

    this.updateSelectedItemTags = function()
    {
        self.tempTags
                [document.getElementById('tagkey').value] = 
                    document.getElementById('tagvalue').value;
        if (document.getElementById('tagkey').value=="name")
        {
            document.getElementById('fname').value = 
                document.getElementById('tagvalue').value;
        }
    }

    this.addTag = function()
    {
        var key = prompt("Tag:");
        var value = prompt("Value:");
        self.tempTags[key] = value;
        self.populateFullTags();
    }

    this.deleteTag = function()
    {
        self.tempTags [document.getElementById('tagkey').value] = null;
        self.populateFullTags();
            
    }
    this.updateNameTag = function()
    {
        self.tempTags['name'] = document.getElementById('fname').value;
        self.populateFullTags();
    }

    this.commitTagsAndUpload = function()
    {
        self.currentSelectedFeature.osmitem.updateTags(self.tempTags);
		self.currentSelectedFeature.osmitem.type=self.tempType;
        self.uploadCallback();
    }

    var html = "<h3>Please enter details</h3>";
    var val = (self.tempTags['name'] ) ?  self.tempTags['name']  : "";

    html+= "<label for='fname'>Name</label><br/>"+
                    "<input id='fname' class='textbox' value=\""+
                            val+"\"/><br/>" +
                    "<label for='ftype'>Type</label><br/>"+
                    "<select id='ftype'>" +
                    "<option>--Select--</option>";

    for(var count=0; count<f.length; count++)
    {
        html += "<option";
        if(f[count]==t)
            html+=" selected='selected'";
        html += ">"+f[count]+"</option>";
    }
    html += "</select><br/>";

    html += "<h4>Full tags:</h4>";
    html += "<p><select id='tagkey'></select>";
    html += "<input id='tagvalue'/>";
    html += "<input type='button' id='ubtn' value='Update!'/>";
    html += "<input type='button' id='delt' value='Delete'/>";
    html += "<br/><input type='button' id='addt' ";
    html += "value='Add tag'/></p>";
    html += "<input type='button' id='uploadBtn' value='Go!'/>" +
                        "<input type='button' value='Cancel' id='cancelBtn'/>";
    var fpopup = new OpenLayers.Popup('fpopup',
                        map.getLonLatFromViewPortPx    
                            (new OpenLayers.Pixel(50,50)),
                        new OpenLayers.Size(480,360),html);
    fpopup.setBackgroundColor('#ffffc0');
    map.addPopup(fpopup);
    document.getElementById('uploadBtn').onclick= this.commitTagsAndUpload;
    document.getElementById('cancelBtn').onclick=this.cancelCallback;
    document.getElementById('fname').onblur=this.updateNameTag;
    document.getElementById('ubtn').onclick= this.updateSelectedItemTags;
    document.getElementById('addt').onclick= this.addTag;
    document.getElementById('delt').onclick= this.deleteTag;
    document.getElementById('tagkey').onchange= this.tagSelectChange;
    document.getElementById('ftype').onchange= this.typeChange;    
    this.populateFullTags();
    return fpopup;
}
