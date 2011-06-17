package org.openstreetmap.osmosis.plugins.mysqlsimple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

public class MysqlSimpleTask implements Sink, EntityProcessor {

	private final BufferedWriter writer;

	SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	/**
	 * Escape a String for use in SQL
	 * 
	 * @param unescaped
	 *            The unescaped String.
	 */
	private String escapeSQL(String unescaped) {
		return unescaped.replaceAll("\\\\","\\\\\\\\").replaceAll("'","\\\\'");
	}

	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	private void write(String data) {
		try {
			writer.write(data);

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}


	/**
	 * Writes a new line in the output file.
	 */
	private void writeNewLine() {
		try {
			writer.newLine();

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}

	/**
	 * Writes a data line to the output file.
	 */
	private void writeln(String data) {
		write(data);
		writeNewLine();
	}

	private void writeTags(String table, long id, Collection<Tag> tags) {
		if (!tags.isEmpty()) {
			write("INSERT INTO "+table+" (id, k, v) VALUES ");
			boolean firstloop = true;
			for (Tag t : tags){
				if (!firstloop) {
					write(", ");
				}
				write("('" + id+"', '"+escapeSQL(t.getKey())+"', '"+escapeSQL(t.getValue())+"')");
				firstloop = false;
			}
			writeln(";");
		}
	}

	public MysqlSimpleTask(BufferedWriter writer) {
		this.writer = writer;
		writeln("set character set utf8;");
		writeln("SET AUTOCOMMIT=0;");
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {

	}


	/**
	 * process all nodes
	 */
	public void process(NodeContainer container) {
		Node n = container.getEntity();
		writeln("INSERT INTO nodes (id, lat, lon, visible, user, timestamp) VALUES ('"
				+n.getId()
				+"', '"+n.getLatitude()
				+"', '"+n.getLongitude()
				+"', NULL, '"+escapeSQL(n.getUser().getName())
				+"', '"+df.format(n.getTimestamp())
				+"');");

		writeTags("node_tags",n.getId(),n.getTags());
	}

	int sequence = 1;
	/**
	 * process all ways
	 */
	public void process(WayContainer container) {
		Way w = container.getEntity();
		writeln("INSERT INTO ways (id, visible, user, timestamp) VALUES ('"
				+w.getId()
				+"', NULL, '"+escapeSQL(w.getUser().getName())
				+"', '"+df.format(w.getTimestamp())
				+"');");

		writeTags("way_tags",w.getId(),w.getTags());

		List<WayNode> nodes = w.getWayNodes();
		if (!nodes.isEmpty()) {
			write("INSERT INTO ways_nodes (wayid, nodeid, sequence) VALUES ");
			boolean firstloop = true;
			for (WayNode wn : nodes){
				if (!firstloop) {
					write(", ");
				}
				write("('" +w.getId()+"', '"+wn.getNodeId()+"', '"+sequence+"')");
				sequence++;
				firstloop = false;
			}
			writeln(";");
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		Relation r = container.getEntity();
		writeln("INSERT INTO relations (id, visible, user, timestamp) VALUES ('"
				+r.getId()
				+"', NULL, '"+escapeSQL(r.getUser().getName())
				+"', '"+df.format(r.getTimestamp())
				+"');");
		writeTags("relation_tags",r.getId(),r.getTags());

		List<RelationMember> members = r.getMembers();
		if (!members.isEmpty()) {
			for (RelationMember rm : members){
				if (rm.getMemberType() == EntityType.Node) {
					writeln("INSERT INTO member_node (nodeid, relid, role) VALUES ('"
							+rm.getMemberId()
							+"', '"+r.getId()
							+"', '"+rm.getMemberRole()
							+"');");
				} else if (rm.getMemberType() == EntityType.Way) {
					writeln("INSERT INTO member_way (wayid, relid, role) VALUES ('"
							+rm.getMemberId()
							+"', '"+r.getId()
							+"', '"+rm.getMemberRole()
							+"');");
				} else if (rm.getMemberType() == EntityType.Relation) {
					writeln("INSERT INTO member_relation (relid2, relid, role) VALUES ('"
							+rm.getMemberId()
							+"', '"+r.getId()
							+"', '"+rm.getMemberRole()
							+"');");
				}
			}
		}
	}


	/**
	 * complete the task
	 */
	public void complete() {
		writeln("COMMIT;");
	}


	/**
	 * {@inheritDoc}
	 */
	public void release() {
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to close writer.", e);
		}
	}

}
