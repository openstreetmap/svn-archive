package org.openstreetmap.josm.plugins.utilsplugin2.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openstreetmap.josm.data.osm.Tag;

public class TextTagParser {

    int start = 0;
    boolean keyFound = false;
    boolean quotesStarted = false;
    boolean esc = false;
    StringBuilder s = new StringBuilder(200);
    int pos;
    String data;
    int n;
    boolean notFound;

    public TextTagParser(String text) {
        pos = 0;
        data = text;
        n = data.length();
    }
    
    /**
     * Read tags from format, tag1\t val1 \n tag2 \t vat2 
     * if possible
     */
    Collection<Tag> getFormattedTags() {
         String lines[] = data.split("\n");
         
         Pattern p = Pattern.compile("(.*?)\t(.*?)");
         List<Tag> tags = new ArrayList<Tag>();
         String k=null, v=null;
         for (String  line: lines) {
            if (line.trim().isEmpty()) continue; // skiip empty lines
            Matcher m = p.matcher(line);
            if (m.matches()) {
                 k=m.group(1).trim(); v=m.group(2).trim();
                 tags.add(new Tag(k,v));
            } else {
                tags.clear();
                break;
            }
         }
         if (!tags.isEmpty()) {
            return tags;
        }  else {
            return null;
        }
    }

    /**
     * Read tags from "Free format"
     */
    Collection<Tag> getParsedTags() {
        String k, v;
        List<Tag> tags = new ArrayList<Tag>();

        while (true) {
            skipEmpty();
            if (pos == n) { 
               break; 
            }
            k = parseString(true);
            if (pos == n) { 
               tags.clear();
               break; 
            }
            skipSign();
            if (pos == n) { 
                tags.clear();
                break; 
            }
            v = parseString(false);
            tags.add(new Tag(k, v));
        }
        return tags;
    }

    private String parseString(boolean stopOnEquals) {
        char c;
        while (pos < n) {
            c = data.charAt(pos);
            if (esc) {
                esc = false;
                s.append(c); //  \" \\
            } else if (c == '\\') {
                esc = true;
            } else if (c == '\"' && !quotesStarted) { // opening "
                if (s.toString().trim().length()>0) { // we had   ||some text"||
                    s.append(c); // just add ", not open
                } else {
                    s.delete(0, s.length()); // forget that empty characthers and start reading "....
                    quotesStarted = true;
                }
            } else if (c == '\"' && quotesStarted) {  // closing "
                quotesStarted = false;
                pos++;
                break;
            } else if (!quotesStarted && (c=='\n'|| c=='\t'|| c==' ' || c=='\r'
                  || (c=='=' && stopOnEquals))) {  // stop-symbols
                pos++;
                break;
            } else {
                if(c>=32) s.append(c);
            }
            pos++;
        }

        String res = s.toString();
        s.delete(0, s.length());
        return res.trim();
    }

    private void skipSign() {
        char c;
        boolean signFound = false;;
        while (pos < n) {
            c = data.charAt(pos);
            if (c == '\t' || c == '\n'  || c == ' ') {
                pos++;
            } else if (c== '=') {
                if (signFound) break; // a  =  =qwerty means "a"="=qwerty"
                signFound = true;
                pos++;
            } else {
                break;
            }
        }
    }

    private void skipEmpty() {
        char c;
        while (pos < n) {
            c = data.charAt(pos);
            if (c == '\t' || c == '\n' || c == '\r' || c == ' ' ) {
                pos++;
            } else {
                break;
            }
        }
    }

    public static Collection<Tag> readTagsFromText(String buf) {
        TextTagParser parser = new TextTagParser(buf);
        Collection<Tag> tags;
        tags = parser.getFormattedTags(); // try "tag\tvalue\n" format
        if (tags == null) {
            tags = parser.getParsedTags();
        }
        return tags;

    }


    
}
