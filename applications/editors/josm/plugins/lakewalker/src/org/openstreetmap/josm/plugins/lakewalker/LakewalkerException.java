package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

class LakewalkerException extends Exception {
    public LakewalkerException(){
    	super(tr("An unknown error has occurred"));
    }

    public LakewalkerException(String err){
        super(err);
    }
}
