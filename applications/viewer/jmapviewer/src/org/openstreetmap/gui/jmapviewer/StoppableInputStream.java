package org.openstreetmap.gui.jmapviewer;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link FilterInputStream} implementation that offers a {@link #stop()}
 * method. The difference between {@link #stop()} and {@link #close()} is that
 * {@link #stop()} guarantees
 * 
 * @author Jan Peter Stotz
 */
public class StoppableInputStream extends FilterInputStream {

	boolean stopped;

	public StoppableInputStream(InputStream in) {
		super(in);
		stopped = false;
	}

	public void stop() {
		stopped = true;
	}

	@Override
	public int read() throws IOException {
		if (stopped)
			return -1;
		return super.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (stopped)
			throw new EOFException();
		return super.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		if (stopped)
			throw new EOFException();
		return super.read(b);
	}

	public boolean isStopped() {
		return stopped;
	}

}
