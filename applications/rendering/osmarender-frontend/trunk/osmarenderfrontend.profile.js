dependencies ={
    layers:  [
        {
        name: "my_dojo.js",
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
		"dojo.parser"
        ]
        },
        {
        name: "osmafrontend.js",
        dependencies: [
		"osmarender_frontend.panels.css.css_rules",
		"osmarender_frontend.widgets.css_editor.css_editor"
        ]
        }

    ],
    prefixes: [
        [ "dijit", "../dijit" ],
        [ "dojox", "../dojox" ],
        [ "cmyk", "../../cmyk" ],
        [ "osmarender_frontend", "../../osmarender_frontend" ]
    ]
};