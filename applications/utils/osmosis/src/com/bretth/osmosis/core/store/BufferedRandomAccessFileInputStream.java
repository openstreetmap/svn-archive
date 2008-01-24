package com.bretth.osmosis.core.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


/**
 * Wraps a random access file adding buffered input stream capabilities. This
 * allows a file to be randomly accessed while providing performance
 * improvements over the non-buffered random access file implementation.
 * 
 * @author Brett Henderson
 */
public class BufferedRandomAccessFileInputStream extends InputStream {
	
	private static final int DEFAULT_INITIAL_BUFFER_SIZE = 100;
	private static final int DEFAULT_MAXIMUM_BUFFER_SIZE = 65536;
	private static final float DEFAULT_BUFFER_INCREASE_FACTOR = 2;
	
	
	private RandomAccessFile randomFile;
	
	private int initialBufferSize;
	private int maxBufferSize;
	private float bufferIncreaseFactor;
	
	private byte[] buffer;
	
	private int currentBufferSize;
	private int currentBufferByteCount;
	private int currentBufferOffset;
	private boolean endOfStream;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to be read.
	 * @throws FileNotFoundException
	 *             if the file cannot be opened.
	 */
	public BufferedRandomAccessFileInputStream(File file) throws FileNotFoundException {
		this(file, DEFAULT_INITIAL_BUFFER_SIZE, DEFAULT_MAXIMUM_BUFFER_SIZE, DEFAULT_BUFFER_INCREASE_FACTOR);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to be read.
	 * @param initialBufferSize
	 *            After a seek, this is the number of bytes that will be
	 *            initially read.
	 * @param maxBufferSize
	 *            This is the maximum number of bytes that will ever be read
	 *            from the underlying file at a time.
	 * @param bufferIncreaseFactor
	 *            During sequential reads, if the buffer is exhausted the next
	 *            read will be longer than the previous read according to this
	 *            factor. A value of 1 means the buffer never gets larger. The
	 *            buffer will never get larger than maxBufferSize.
	 * @throws FileNotFoundException
	 *             if the file cannot be opened.
	 */
	public BufferedRandomAccessFileInputStream(File file, int initialBufferSize, int maxBufferSize, float bufferIncreaseFactor) throws FileNotFoundException {
		this.initialBufferSize = initialBufferSize;
		this.maxBufferSize = maxBufferSize;
		this.bufferIncreaseFactor = bufferIncreaseFactor;
		
		buffer = new byte[maxBufferSize];
		
		randomFile = new RandomAccessFile(file, "r");
		
		currentBufferSize = 0;
		currentBufferByteCount = 0;
		currentBufferOffset = 0;
		endOfStream = false;
	}
	
	
	/**
	 * Ensures data is available in the buffer.
	 * 
	 * @return True if data is available. False indicates that the end of stream
	 *         has been reached.
	 */
	private boolean populateBuffer() throws IOException {
		if (endOfStream) {
			return false;
		}
		
		if (currentBufferOffset >= currentBufferByteCount) {
			currentBufferOffset = 0;
			
			if (currentBufferSize == 0) {
				currentBufferSize = initialBufferSize;
			} else {
				currentBufferSize = (int) (currentBufferSize * bufferIncreaseFactor);
				if (currentBufferSize > maxBufferSize) {
					currentBufferSize = maxBufferSize;
				}
			}
			
			currentBufferByteCount = randomFile.read(buffer, 0, currentBufferSize);
			
			if (currentBufferByteCount < 0) {
				endOfStream = true;
			}
		}
		
		return !endOfStream;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		if (populateBuffer()) {
			return buffer[currentBufferOffset++] & 0xff;
		} else {
			return -1;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b) throws IOException {
		if (populateBuffer()) {
			int readLength;
			
			// Determine how many bytes to read from the current buffer.
			readLength = currentBufferByteCount - currentBufferOffset;
			if (readLength > b.length) {
				readLength = b.length;
			}
			
			// Copy the bytes into the output buffer and update the current buffer position.
			System.arraycopy(buffer, currentBufferOffset, b, 0, readLength);
			currentBufferOffset += readLength;
			
			return readLength;
			
		} else {
			return -1;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (populateBuffer()) {
			int readLength;
			
			// Determine how many bytes to read from the current buffer.
			readLength = currentBufferByteCount - currentBufferOffset;
			if (readLength > len) {
				readLength = len;
			}
			
			// Copy the bytes into the output buffer and update the current buffer position.
			System.arraycopy(buffer, currentBufferOffset, b, off, readLength);
			currentBufferOffset += readLength;
			
			return readLength;
			
		} else {
			return -1;
		}
	}
	
	
	/**
	 * Seeks to the specified position in the file.
	 * 
	 * @param pos
	 *            The position within the file to seek to.
	 * @throws IOException
	 *             if an error occurs during seeking.
	 */
	public void seek(long pos) throws IOException {
		randomFile.seek(pos);
		
		// Clear all buffered data.
		currentBufferSize = 0;
		currentBufferByteCount = 0;
		currentBufferOffset = 0;
		endOfStream = false;
	}
	
	
	/**
	 * Returns the length of the data file.
	 * 
	 * @return The file length in bytes.
	 * @throws IOException
	 *             if an error occurs during the length operation.
	 */
	public long length() throws IOException {
		return randomFile.length();
	}
	
	
	/**
	 * Returns the current read position in the data file.
	 * 
	 * @return The current file offset in bytes.
	 * @throws IOException
	 *             if an error occurs during the position operation.
	 */
	public long position() throws IOException {
		return randomFile.getFilePointer() - currentBufferByteCount + currentBufferOffset;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		randomFile.close();
	}
}
