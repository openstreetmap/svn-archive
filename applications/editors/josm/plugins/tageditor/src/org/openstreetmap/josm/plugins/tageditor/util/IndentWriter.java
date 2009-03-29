package org.openstreetmap.josm.plugins.tageditor.util;

import java.io.IOException;
import java.io.Writer;

public class IndentWriter {
	
	private Writer writer;
	private int level = 0;
	
	public IndentWriter(Writer writer) {
		this.writer = writer;
	}

	public Writer getWriter() {
		return writer;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) { 
		this.level = Math.max(0,level);
	}

	public void incLevel() {
		this.level++;
	}
	
	public void decLevel() {
		this.level = Math.max(0, this.level-1);
	}
	
	public void indent() throws IOException {
		for (int i=1; i <= level;i++) {
			writer.write("  ");
		}
	}
	
	public void write(String s) throws IOException {
		writer.write(s);
	}
	
	/**
	 * writes s prepended by indentation white space and followed by
	 * a newline to the writer
	 * 
	 * @param s the string 
	 * @throws IOException
	 */
	public void writeLine(String s) throws IOException {
		indent();	
		write(s);
		write("\n");
	}
	

	
}
