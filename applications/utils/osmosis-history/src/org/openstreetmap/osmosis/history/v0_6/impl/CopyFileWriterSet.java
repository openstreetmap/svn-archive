package org.openstreetmap.osmosis.history.v0_6.impl;

import org.openstreetmap.osmosis.core.lifecycle.Completable;
import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.CopyFileset;

public class CopyFileWriterSet implements Completable {

	private CopyFileWriter userWriter;
	private CopyFileWriter nodeWriter;
	private CopyFileWriter wayWriter;
	private CopyFileWriter wayNodeWriter;
	private CopyFileWriter relationWriter;
	private CopyFileWriter relationMemberWriter;

	public CopyFileWriterSet(CopyFileWriter userWriter,
			CopyFileWriter nodeWriter, CopyFileWriter wayWriter,
			CopyFileWriter wayNodeWriter, CopyFileWriter relationWriter,
			CopyFileWriter relationMemberWriter) {
		super();
		this.userWriter = userWriter;
		this.nodeWriter = nodeWriter;
		this.wayWriter = wayWriter;
		this.wayNodeWriter = wayNodeWriter;
		this.relationWriter = relationWriter;
		this.relationMemberWriter = relationMemberWriter;
	}

	public CopyFileWriter getUserWriter() {
		return userWriter;
	}

	public void setUserWriter(CopyFileWriter userWriter) {
		this.userWriter = userWriter;
	}

	public CopyFileWriter getNodeWriter() {
		return nodeWriter;
	}

	public void setNodeWriter(CopyFileWriter nodeWriter) {
		this.nodeWriter = nodeWriter;
	}

	public CopyFileWriter getWayWriter() {
		return wayWriter;
	}

	public void setWayWriter(CopyFileWriter wayWriter) {
		this.wayWriter = wayWriter;
	}

	public CopyFileWriter getWayNodeWriter() {
		return wayNodeWriter;
	}

	public void setWayNodeWriter(CopyFileWriter wayNodeWriter) {
		this.wayNodeWriter = wayNodeWriter;
	}

	public CopyFileWriter getRelationWriter() {
		return relationWriter;
	}

	public void setRelationWriter(CopyFileWriter relationWriter) {
		this.relationWriter = relationWriter;
	}

	public CopyFileWriter getRelationMemberWriter() {
		return relationMemberWriter;
	}

	public void setRelationMemberWriter(CopyFileWriter relationMemberWriter) {
		this.relationMemberWriter = relationMemberWriter;
	}

	public static CopyFileWriterSet createFromFileset(CopyFileset copyFileset) {
		return new CopyFileWriterSet(
				new CopyFileWriter(copyFileset.getUserFile()), 
				new CopyFileWriter(copyFileset.getNodeFile()), 
				new CopyFileWriter(copyFileset.getWayFile()), 
				new CopyFileWriter(copyFileset.getWayNodeFile()), 
				new CopyFileWriter(copyFileset.getRelationFile()), 
				new CopyFileWriter(copyFileset.getRelationMemberFile())
		);
	}
	
	public static CopyFileWriterSet createEmpty() {
		CopyFileWriter emptyWriter = new EmptyCopyFileWriter();
		return new CopyFileWriterSet(emptyWriter, emptyWriter, emptyWriter, emptyWriter, emptyWriter, emptyWriter);
	}

	@Override
	public void release() {
		userWriter.release();
		nodeWriter.release();
		wayWriter.release();
		wayNodeWriter.release();
		relationWriter.release();
		relationMemberWriter.release();
	}

	@Override
	public void complete() {
		userWriter.complete();
		nodeWriter.complete();
		wayWriter.complete();
		wayNodeWriter.complete();
		relationWriter.complete();
		relationMemberWriter.complete();
	}
}
