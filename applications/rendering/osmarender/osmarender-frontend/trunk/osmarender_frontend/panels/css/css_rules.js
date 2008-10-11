dojo.provide("osmarender_frontend.panels.css.css_rules");

dojo.require("dijit._Widget");
dojo.require("dojox.dtl._HtmlTemplated");
dojo.require("dojox.dtl.Context");

dojo.declare("osmarender_frontend.panels.css.css_rules", [dijit._Widget, dojox.dtl._HtmlTemplated], {
	templateString: '{% load dojox.dtl.contrib.data %}{% bind_data items to store as items %}{% include templateFile %}',
	templateFile: dojo.moduleUrl("osmarender_frontend.panels.css", "css_rules.html"),
	store: "",

	constructor: function(mystore) {
		this.store=mystore;
	},

 postCreate: function(){
        console.log("this.domNode = ",this.domNode);
        this.store.fetch({
          query: {
            type: "continent"
          },
          onComplete: dojo.hitch(this, function(items){
            this.items = items;
            this.render();
          })
        });
      }
});

