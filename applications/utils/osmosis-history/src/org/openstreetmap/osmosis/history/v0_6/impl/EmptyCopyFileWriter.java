package org.openstreetmap.osmosis.history.v0_6.impl;

import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;
import org.postgis.Geometry;
import org.postgresql.util.PGobject;

public class EmptyCopyFileWriter extends CopyFileWriter {

	public EmptyCopyFileWriter() {
		super(null);
	}

	@Override
	public void writeField(boolean data) {
		// NOP
	}

	@Override
	public void writeField(int data) {
		// NOP
	}

	@Override
	public void writeField(long data) {
		// NOP
	}

	@Override
	public void writeField(String data) {
		// NOP
	}

	@Override
	public void writeField(Date data) {
		// NOP
	}

	@Override
	public void writeField(Geometry data) {
		// NOP
	}

	@Override
	public void writeField(PGobject data) {
		// NOP
	}

	@Override
	public void writeField(List<Long> data) {
		// NOP
	}

	@Override
	public void endRecord() {
		// NOP
	}

	@Override
	public void complete() {
		// NOP
	}

	@Override
	public void release() {
		// NOP
	}
	
	

}
