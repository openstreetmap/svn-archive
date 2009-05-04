// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform;

public interface Match {

	public String getMatchID();
	public String getKey(int group);
	public String getValue(int group);
	public int getKeyGroupCount();
	public int getValueGroupCount();
	
}
