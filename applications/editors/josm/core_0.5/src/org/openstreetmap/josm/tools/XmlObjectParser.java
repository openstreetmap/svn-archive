// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.tools;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.co.wilson.xml.MinML2;

/**
 * An helper class that reads from a XML stream into specific objects.
 *
 * @author Imi
 */
public class XmlObjectParser implements Iterable<Object> {

	public static class Uniform<T> implements Iterable<T>{
		private Iterator<Object> iterator;
		/**
		 * @param klass This has to be specified since generics are ereased from
		 * class files so the JVM cannot deduce T itself.
		 */
		public Uniform(Reader input, String tagname, Class<T> klass) {
			XmlObjectParser parser = new XmlObjectParser();
			parser.map(tagname, klass);
			parser.start(input);
			iterator = parser.iterator();
		}
		public Iterator<T> iterator() {
			return new Iterator<T>(){
				public boolean hasNext() {return iterator.hasNext();}
				@SuppressWarnings("unchecked") public T next() {return (T)iterator.next();}
				public void remove() {iterator.remove();}
			};
		}
	}

	private class Parser extends MinML2 {
		Stack<Object> current = new Stack<Object>();
		String characters = "";
		@Override public void startElement(String ns, String lname, String qname, Attributes a) throws SAXException {
			if (mapping.containsKey(qname)) {
				Class<?> klass = mapping.get(qname).klass;
				try {
					current.push(klass.newInstance());
				} catch (Exception e) {
					throw new SAXException(e);
				}
				for (int i = 0; i < a.getLength(); ++i)
					setValue(a.getQName(i), a.getValue(i));
				if (mapping.get(qname).onStart)
					report();
			}
		}
		@Override public void endElement(String ns, String lname, String qname) throws SAXException {
			if (mapping.containsKey(qname) && !mapping.get(qname).onStart)
				report();
			else if (characters != null && !current.isEmpty()) {
				setValue(qname, characters);
				characters = "";
			}
		}
		@Override public void characters(char[] ch, int start, int length) {
			String s = new String(ch, start, length);
			characters += s;
		}

		private void report() {
			try {
				queue.put(current.pop());
			} catch (InterruptedException e) {
			}
			characters = "";
		}

		private Object getValueForClass(Class<?> klass, String value) {
			if (klass == Boolean.TYPE)
				return parseBoolean(value);
			else if (klass == Integer.TYPE || klass == Long.TYPE)
				return Long.parseLong(value);
			else if (klass == Float.TYPE || klass == Double.TYPE)
				return Double.parseDouble(value);
			return value;
		}
		
		private void setValue(String fieldName, String value) throws SAXException {
			if (fieldName.equals("class") || fieldName.equals("default") || fieldName.equals("throw") || fieldName.equals("new") || fieldName.equals("null"))
				fieldName += "_";
			try {
				Object c = current.peek();
				Field f = null;
				try {
	                f = c.getClass().getField(fieldName);
                } catch (NoSuchFieldException e) {
                }
				if (f != null && Modifier.isPublic(f.getModifiers()))
					f.set(c, getValueForClass(f.getType(), value));
				else {
					fieldName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
					Method[] methods = c.getClass().getDeclaredMethods();
					for (Method m : methods) {
						if (m.getName().equals(fieldName) && m.getParameterTypes().length == 1) {
							m.invoke(c, new Object[]{getValueForClass(m.getParameterTypes()[0], value)});
							return;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace(); // SAXException does not dump inner exceptions.
				throw new SAXException(e);
			}
		}
		private boolean parseBoolean(String s) {
			return s != null && 
				!s.equals("0") && 
				!s.startsWith("off") && 
				!s.startsWith("false") &&
				!s.startsWith("no");
		}
	}

	private static class Entry {
		Class<?> klass;
		boolean onStart;
		public Entry(Class<?> klass, boolean onStart) {
			super();
			this.klass = klass;
			this.onStart = onStart;
		}
	}

	private Map<String, Entry> mapping = new HashMap<String, Entry>();
	private Parser parser;
	
	/**
	 * The queue of already parsed items from the parsing thread.
	 */
	private BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(10);

	/**
	 * This stores one item retrieved from the queue to give hasNext a chance.
	 * So this is also the object that will be returned on the next call to next().
	 */
	private Object lookAhead = null;
	
	/**
	 * This object represent the end of the stream (null is not allowed as
	 * member in class Queue).
	 */
	private Object EOS = new Object();

	public XmlObjectParser() {
		parser = new Parser();
	}

	public Iterable<Object> start(final Reader in) {
		new Thread(){
			@Override public void run() {
				try {
					parser.parse(in);
				} catch (Exception e) {
					try {
						queue.put(e);
					} catch (InterruptedException e1) {
					}
				}
				parser = null;
				try {
					queue.put(EOS);
				} catch (InterruptedException e) {
				}
			}
		}.start();
		return this;
	}

	public void map(String tagName, Class<?> klass) {
		mapping.put(tagName, new Entry(klass,false));
	}

	public void mapOnStart(String tagName, Class<?> klass) {
		mapping.put(tagName, new Entry(klass,true));
	}

	/**
	 * @return The next object from the xml stream or <code>null</code>,
	 * if no more objects.
	 */
	public Object next() throws SAXException {
		fillLookAhead();
		if (lookAhead == EOS)
			throw new NoSuchElementException();
		Object o = lookAhead;
		lookAhead = null;
		return o;
	}

	private void fillLookAhead() throws SAXException {
		if (lookAhead != null)
			return;
	    try {
			lookAhead = queue.take();
			if (lookAhead instanceof SAXException)
				throw (SAXException)lookAhead;
			else if (lookAhead instanceof RuntimeException)
				throw (RuntimeException)lookAhead;
			else if (lookAhead instanceof Exception)
				throw new SAXException((Exception)lookAhead);
		} catch (InterruptedException e) {
        	throw new RuntimeException("XmlObjectParser must not be interrupted.", e);
		}
    }

	public boolean hasNext() throws SAXException {
		fillLookAhead();
        return lookAhead != EOS;
	}

	public Iterator<Object> iterator() {
		return new Iterator<Object>(){
			public boolean hasNext() {
				try {
					return XmlObjectParser.this.hasNext();
				} catch (SAXException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			public Object next() {
				try {
					return XmlObjectParser.this.next();
				} catch (SAXException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
