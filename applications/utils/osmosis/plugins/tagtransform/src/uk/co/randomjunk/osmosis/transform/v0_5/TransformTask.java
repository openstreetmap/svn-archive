// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.v0_5;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;
import org.openstreetmap.osmosis.core.domain.common.UnparsedTimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_5.Entity;
import org.openstreetmap.osmosis.core.domain.v0_5.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.task.v0_5.Sink;
import org.openstreetmap.osmosis.core.task.v0_5.SinkSource;
import org.openstreetmap.osmosis.core.xml.common.XmlTimestampFormat;

import uk.co.randomjunk.osmosis.transform.Match;
import uk.co.randomjunk.osmosis.transform.Output;
import uk.co.randomjunk.osmosis.transform.StatsSaveException;
import uk.co.randomjunk.osmosis.transform.TTEntityType;
import uk.co.randomjunk.osmosis.transform.Translation;
import uk.co.randomjunk.osmosis.transform.impl.TransformLoader;

public class TransformTask implements SinkSource {
	private static Logger logger = Logger.getLogger(TransformTask.class.getName());
	
	private Sink sink;
	private List<Translation> translations;
	private String statsFile;
	private String configFile;
	private static TimestampFormat timestampFormat = new XmlTimestampFormat();

	public TransformTask(String configFile, String statsFile) {
		logger.log(Level.FINE, "Transform configured with "+configFile+" and "+statsFile);
		translations = new TransformLoader().load(configFile);
		this.statsFile = statsFile;
		this.configFile = configFile;
	}

	@Override
	public void complete() {
		if ( statsFile != null && !statsFile.isEmpty() ) {
			StringBuilder builder = new StringBuilder();
			builder.append(configFile);
			builder.append("\n\n");
			for ( Translation t : translations )
				t.outputStats(builder, "");
			
			Writer writer = null;
			try {
				writer = new FileWriter(new File(statsFile));
				writer.write(builder.toString());
			} catch (IOException e) {
				throw new StatsSaveException("Failed to save stats: "+e.getLocalizedMessage(), e);
			} finally {
				if ( writer != null )
					try {
						writer.close();
					} catch (IOException e) {}
			}
		}
		sink.complete();
	}

	@Override
	public void process(EntityContainer entityContainer) {
		Entity entity = entityContainer.getEntity();
		EntityType entityType = entity.getType();
		List<Tag> tagList = entity.getTagList();
		Map<String, String> originalTags = new HashMap<String, String>();
		for ( Tag tag : tagList ) {
			originalTags.put(tag.getKey(), tag.getValue());
		}
		
		Map<String, String> tags = new HashMap<String, String>(originalTags);
		for ( Translation translation : translations ) {
			Collection<Match> matches = translation.match(tags, TTEntityType.fromEntityType0_5(entityType));
			if ( matches == null || matches.isEmpty() )
				continue;
			if ( translation.isDropOnMatch() ) {
				return;
			}
			
			Map<String, String> newTags = new HashMap<String, String>();
			for ( Output output : translation.getOutputs() ) {
				output.apply(tags, newTags, matches);
			}
			tags = newTags;
		}
		
		Collection<Tag> newTags = new ArrayList<Tag>();
		for ( Entry<String, String> tag : tags.entrySet() )
			newTags.add(new Tag(tag.getKey(), tag.getValue()));
		
		EntityContainer output = null;
		TimestampContainer timestamp = new UnparsedTimestampContainer(timestampFormat,
				entity.getFormattedTimestamp(timestampFormat));
		switch ( entity.getType() ) {
		case Node:
			Node oldNode = (Node) entityContainer.getEntity();
			output = new NodeContainer(
					new Node(oldNode.getId(), timestamp,
							oldNode.getUser(), oldNode.getLatitude(),
							oldNode.getLongitude()));
			output.getEntity().addTags(newTags);
			break;
			
		case Way:
			Way oldWay = (Way) entityContainer.getEntity();
			Way way = new Way(oldWay.getId(), timestamp, oldWay.getUser());
			way.addTags(newTags);
			way.addWayNodes(oldWay.getWayNodeList());
			output = new WayContainer(way);
			break;

		case Relation:
			Relation oldRelation = (Relation) entityContainer.getEntity();
			Relation relation = new Relation(oldRelation.getId(), timestamp, oldRelation.getUser());
			relation.addTags(newTags);
			relation.addMembers(oldRelation.getMemberList());
			output = new RelationContainer(relation);
			break;

		}
		
		if ( output != null )
			sink.process(output);
	}

	@Override
	public void release() {
		sink.release();
	}

	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}

}
