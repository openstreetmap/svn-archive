package org.openstreetmap.josm.plugins.JunctionChecker.converting;

import java.util.ArrayList;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.LeadsTo;

/**
 * löscht Pseudochannels, also 2 Channels, die ein Segment repräsentieren, werden zu einem Channel zusammnegefaßt. Dabei übernimmt der 1. Channel den ToNode des Nachfrolger sowie seine LeadsTo-Relationen. Der 2. CHannel wird anschließend gelöscht. Dies iterativ bis das betrachtete Segment nur noch aus einem Channel besteht
 * @author  joerg
 */
public class PseudoChannelRemover {

	private final ChannelDiGraph digraph;
	//private final ArrayList<Channel> pseudochannels = new ArrayList<Channel>();
	private Channel succChannel;
	private Channel tempToChannel;
	private LeadsTo tempLeadsTo;

	public PseudoChannelRemover(ChannelDiGraph digraph) {
		this.digraph = digraph;
	}

	private void mergeChannel(Channel tempchannel) {
		succChannel = tempchannel.getLeadsTo().get(0).getToChannel();
		tempchannel.setToNode(succChannel.getToNode());
		//log.trace("---Folgender Channel ist überflüssig: " + succChannel.getNewid() + "---");
		//log.trace("... und wird durch diesen ersetzt: " + tempchannel.getNewid());
		//VorgängerChannel der nachfolgenden Channels des zu löschenden, überflüssigen Channels neu setzen
		for (int i = 0; i < succChannel.getLeadsTo().size(); i++) {
			for (int j = 0; j < succChannel.getLeadsTo().get(i).getToChannel().getPredChannels().size(); j++) {
				if (succChannel.getLeadsTo().get(i).getToChannel().getPredChannels().get(j).getNewid() == succChannel.getNewid()) {
					succChannel.getLeadsTo().get(i).getToChannel().getPredChannels().remove(j);
					succChannel.getLeadsTo().get(i).getToChannel().addPredChannel(tempchannel);
				}
			}
		}

		//LeadsTo des zu ersetzenden Channels für den neuen Channel neu erzeugen
		tempchannel.removeLeadsTo();
		for (int i = 0; i < succChannel.getLeadsTo().size(); i++) {
			tempToChannel = succChannel.getLeadsTo().get(i).getToChannel();
			//log.trace("tempToChannel: " + tempToChannel.toString());
			tempLeadsTo = new LeadsTo(tempchannel, tempToChannel);
			//log.trace(i + ". Durchlauf: Füge ledasTo hinzu: " + tempLeadsTo.toString());
			digraph.addLeadsTo(tempLeadsTo);
		}

		//TODO: quick'n'dirty
		ArrayList< LeadsTo> tls = succChannel.getLeadsTo();
		for (int i = 0; i < tls.size(); i++) {
			digraph.removeLeadsTo(tls.get(i));
		}
		digraph.removeChannel(succChannel);
		//pseudochannels.add(tempchannel.getSuccChannels().get(0));
	}

	public void removePseudoChannels() {
		Channel tempChannel;
		for (int i = 0; i < digraph.getChannels().size(); i++) {
			tempChannel = digraph.getChannelAtPosition(i);
			while (digraph.isInBBox(tempChannel.getToNode())
					&& (tempChannel.getLeadsTo().size() == 1)
					&& (tempChannel.getLeadsTo().get(0).getToChannel().getPredChannels().size() <= 1)
					&& (tempChannel.getBackChannelID() != tempChannel.getLeadsTo().get(0).getToChannel().getNewid())) {
				// dies if-abfrage verhindert eine endlosschleife, wenn der
				// channel ein kreisverkehr istjava
				if (tempChannel.getLeadsTo().get(0).getToChannel().equals(tempChannel)) {
					break;
				} else {
					mergeChannel(tempChannel);
				}
			}

		}
	}
}
