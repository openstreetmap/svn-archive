package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

/**
 * @author  joerg
 */
public class LeadsTo {

	private Channel fromChannel;
	private Channel toChannel;

	//für den JunctionCheck
	private boolean isForwardEdge;

	public LeadsTo() {
	}

	public LeadsTo(Channel fromcChannel, Channel toChannel) {
		this.fromChannel = fromcChannel;
		this.toChannel = toChannel;
	}

	public Channel getFromChannel() {
		return fromChannel;
	}

	public void setFromChannel(Channel fromChannel) {
		this.fromChannel = fromChannel;
	}

	public Channel getToChannel() {
		return toChannel;
	}

	public void setToChannel(Channel toChannel) {
		this.toChannel = toChannel;
	}

	public boolean isForwardEdge() {
		return isForwardEdge;
	}

	public void setForwardEdge(boolean isForwardEdge) {
		this.isForwardEdge = isForwardEdge;
	}

	@Override
	public String toString() {
		return "fromChannel:::" + fromChannel.getNewid()  + ", toChannel:::" + toChannel.getNewid() + " über Node " + toChannel.getFromNode().getId();
	}


}
