OSMEditor.EditPanel = OpenLayers.Class(OpenLayers.Control, {
    features: [],
    outputDiv: 'data',
    layer: null,
    'featureselected': function(e) {
        var feature = e.feature;
        var type = "way"; 
        if (feature.geometry.CLASS_NAME == "OpenLayers.Geometry.Point") {
            type = "node";
        }
        this.draw(type, feature.osm_id, feature.attributes);
        this.drawStartAndEnd(e.feature);
        this.type = type;
        this.id = feature.osm_id;
        this.attributes = feature.attributes;
    },
    'featureunselected': function(e) {
        $(this.outputDiv).innerHTML = "";
        this.removeStartAndEnd();
    },
    'draw': function(type, id, attrs) {
        $(this.outputDiv).innerHTML = "";
        document.getElementById(this.outputDiv).appendChild(this.createHTML(type, id, attrs));
    },
    'createHTML': function(type, id, attrs) {
        window.editFunction = OpenLayers.Function.bind(this.editHTML, this); 
        var s = "<a onclick='editFunction(); return false;' href='/" + type +"/" + id +"'>Edit "+ type + " " + id + "</a>";
        var tags = "<ul>"; 
        for (var key in attrs) {
            if (key.search(":") == -1) {
                tags += "<li><b>" + key + "</b>: " + attrs[key] + "</li>";
            }
        }
        tags += "</ul>";
        var div = $.DIV();
        div.innerHTML = s+tags;
        return div;
    },
    'editHTML': function() {
        var form = $.FORM({'action':'/' + this.type + '/' + this.id + '/', 'method':'POST', 'target': '_blank'});
        form.appendChild($.INPUT({'type':'hidden','name':'type','value': this.type})); 
        form.appendChild($.INPUT({'type':'hidden','name':'id','value': this.id})); 
        form.appendChild($.INPUT({'type':'hidden','name':'timestamp','value': 'nocheck'})); 
        var ul = $.UL();
        for (var key in this.attributes) {
            var li = $.LI({}, $.B({}, key), $.INPUT({'name': 'key_'+key, 'value': this.attributes[key]}), 'Delete?', $.INPUT({'type':'checkbox','name':'delete_key_'+key, value:'delete'}));
            ul.appendChild(li);
        }
        var li = $.LI({}, $.B({}, "New Tag:", $.INPUT({'name':'new_key_1'}), ":", $.INPUT({'name': 'new_value_1'})));
        ul.appendChild(li);
        form.appendChild(ul);
        var div = $.DIV({}, $.INPUT({'type':'submit', 'value':'Change'}), $.INPUT({'type':'submit', 'name':'reverse', 'value':'Change and Reverse'}));
        form.appendChild(div);
        $(this.outputDiv).innerHTML = "";
        $(this.outputDiv).appendChild(form);
    },    
    removeStartAndEnd: function() {
        this.layer.removeFeatures(this.features);
        this.features = [];
    },    
    drawStartAndEnd: function(feature) {
        if (feature.geometry.CLASS_NAME == "OpenLayers.Geometry.LineString") {
            var start = new OpenLayers.Feature.Vector(feature.geometry.components[0], null, {
                fillColor: 'green', pointRadius: 6 
            });
            var end = new OpenLayers.Feature.Vector(
                feature.geometry.components[feature.geometry.components.length-1], null, 
                {fillColor: 'red', pointRadius: 6 });
            
            this.layer.addFeatures([start,end]);
            this.features = [start,end];
        }
    }
});    

