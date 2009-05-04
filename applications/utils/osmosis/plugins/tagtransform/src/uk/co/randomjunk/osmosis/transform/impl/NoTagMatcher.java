// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import uk.co.randomjunk.osmosis.transform.Match;
import uk.co.randomjunk.osmosis.transform.Matcher;
import uk.co.randomjunk.osmosis.transform.TTEntityType;

public class NoTagMatcher implements Matcher {

	private Pattern keyPattern;
	private Pattern valuePattern;
	private long matchHits;

	public NoTagMatcher(String keyPattern, String valuePattern) {
		this.keyPattern = Pattern.compile(keyPattern);
		this.valuePattern = Pattern.compile(valuePattern);
	}

	@Override
	public Collection<Match> match(Map<String, String> tags, TTEntityType type) {
		// loop through the tags to find matches
		for ( Entry<String, String> tag : tags.entrySet() ) {
			java.util.regex.Matcher keyMatch = keyPattern.matcher(tag.getKey());
			java.util.regex.Matcher valueMatch = valuePattern.matcher(tag.getValue());
			if ( keyMatch.matches() && valueMatch.matches() ) {
				return null;
			}
		}
		
		matchHits += 1;
		return Collections.singleton(NULL_MATCH);
	}
	
	@Override
	public void outputStats(StringBuilder output, String indent) {
		output.append(indent);
		output.append("NoTag[");
		output.append(keyPattern.pattern());
		output.append(",");
		output.append(valuePattern.pattern());
		output.append("]: ");
		output.append(matchHits);
		output.append('\n');
	}

	private static final Match NULL_MATCH = new Match() {
		@Override
		public int getValueGroupCount() {
			return 0;
		}
	
		@Override
		public String getValue(int group) {
			return null;
		}
	
		@Override
		public String getMatchID() {
			return null;
		}
	
		@Override
		public int getKeyGroupCount() {
			return 0;
		}
	
		@Override
		public String getKey(int group) {
			return null;
		}
	};
}
