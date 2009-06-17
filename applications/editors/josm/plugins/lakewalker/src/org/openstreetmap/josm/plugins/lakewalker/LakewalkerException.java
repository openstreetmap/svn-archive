package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

class LakewalkerException extends Exception {
    String error;

    public LakewalkerException(){
        super();
        this.error = tr("An unknown error has occurred");
    }

    public LakewalkerException(String err){
        super();
        this.error = err;
    }

    public String getError(){
      return this.error;
    }
}
