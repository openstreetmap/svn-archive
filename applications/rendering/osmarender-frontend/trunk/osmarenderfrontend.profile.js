dependencies ={
    layers:  [
        {
        name: "osmafrontend.js",
        dependencies: [
		"dojo.dnd.move",
		"dojo.fx",
		"dijit.Menu",
		"dijit.form.Button",
		"dijit.form.CheckBox",
		"dijit.form.FilteringSelect",
		"dijit.form.TextBox",
		"dijit.ProgressBar",
		"dijit.Toolbar",
		"dijit.Tooltip",
		"dijit.Tree",
		"dijit.layout.BorderContainer",
		"dijit.layout.ContentPane",
		"dijit.layout.TabContainer",
		"dojox.layout.ResizeHandle",
		"dijit.form.NumberSpinner",
		"dojox.widget.ColorPicker",
		"dojo.data.ItemFileReadStore",
		"dojox.dtl.Context",
		"dojo.i18n",
		"dojo.parser",
		"osmarender_frontend.panels.css.css_rules"
        ]
        },
        {
        name: "juice.js",
        dependencies: [
		"dojo.18n",
		"juice.juice"
        ]
        }
    ],
    prefixes: [
        [ "dijit", "../dijit" ],
        [ "dojox", "../dojox" ],
        [ "cmyk", "../../cmyk" ],
        [ "juice", "../../juice" ],
        [ "osmarender_frontend", "../../osmarender_frontend" ]
    ]
};