package org.openstreetmap.josm.plugins.tageditor.preset.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.tageditor.preset.Presets;
import org.openstreetmap.josm.plugins.tageditor.preset.io.Parser;
import org.openstreetmap.josm.plugins.tageditor.preset.io.PresetIOException;
import org.openstreetmap.josm.plugins.tageditor.util.IndentWriter;

import junit.framework.TestCase;



public class ParserTest extends TestCase {

	public void test_Parser() {
		Parser parser = new Parser();
	}
	
	public void test_Parser_2() {
		try {
			Parser parser = new Parser(null);
			assert false : "IllegalArgumentException expected because reader is null";
		} catch(IllegalArgumentException e) {
			// OK
		}
		Reader reader = new StringReader("test");
		Parser parser = new Parser(reader);
		assert parser.getReader() == reader; 
	}
	
	public void test_set_get_reader() {
		Reader reader = new StringReader("test");
		Parser parser = new Parser();
		parser.setReader(reader);
		assert parser.getReader() == reader; 
	}
	
	
	public void test_parse() throws PresetIOException, IOException {
		InputStream in = Main.class.getResourceAsStream("/presets/presets.xml");
		assert in != null : "failed to open preset file";
		Reader reader = null;
		try {
			reader = new InputStreamReader(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			reader = new InputStreamReader(in);
		}
		
		Parser parser = new Parser(reader);
		parser.parse();
		Presets presets = parser.getPresets(); 
		IndentWriter writer = new IndentWriter(new PrintWriter(System.out));
		presets.dump(writer);
		
		
	}
	
}
