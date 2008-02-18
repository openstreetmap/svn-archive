package org.openstreetmap.josm.plugins.lakewalker;

class LakewalkerException extends Exception {
	String error;
	
	public LakewalkerException(){
		super();
		this.error = "An unknown error has occured";
	}
	
	public LakewalkerException(String err){
		super();
		this.error = err;
	}
	
	public String getError(){
	  return this.error;
	}
}
