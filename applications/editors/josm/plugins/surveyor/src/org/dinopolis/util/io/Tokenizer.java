/***********************************************************************
 * @(#)$RCSfile: Tokenizer.java,v $   $Revision: 1.6 $$Date: 2006/04/21 14:14:56 $
 *
 * Copyright (c) Christof Dallermassl
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL)
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 ***********************************************************************/

package org.dinopolis.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//----------------------------------------------------------------------
/**

 * This tokenizer merges the benefits of the java.lang.StringTokenizer
 * class and the java.io.StreamTokenizer class. It provides a low
 * level and a high level interface to the tokenizer. The low level
 * interface consists of the method pair nextToken() and getWord(),
 * where the first returns the type of token in the parsing process,
 * and the latter returns the String element itself.
 * <p>
 * The high level interface consists of the methods hasNextLine() and
 * nextLine(). They use the low level interface to parse the data line
 * by line and create a list of strings from it.
 * <p>
 * It is unsure, if it is wise to mix the usage of the high and
 * the low level interface. For normal usage, the high level interface
 * should be more comfortable to use and does not provide any
 * drawbacks.
 * <p>

 * An example for the high level interface:
 * <pre>
 *    try
 *    {
 *          // simple example, tokenizing string, no escape, but quoted
 *          // works:
 *      System.out.println("example 1");
 *      Tokenizer tokenizer = new Tokenizer("text,,,\"another,text\"");
 *      List tokens;
 *      while(tokenizer.hasNextLine())
 *      {
 *        tokens = tokenizer.nextLine();
 *        System.out.println(tokens.get(0)); // prints 'text'
 *        System.out.println(tokens.get(1)); // prints ''
 *        System.out.println(tokens.get(2)); // prints ''
 *        System.out.println(tokens.get(3)); // prints 'another,text'
 *      }
 *
 *      System.out.println("example 2");
 *          // simple example, tokenizing string, using escape char and
 *          // quoted strings:
 *      tokenizer = new Tokenizer("text,text with\\,comma,,\"another,text\"");
 *      tokenizer.respectEscapedCharacters(true);
 *      while(tokenizer.hasNextLine())
 *      {
 *        tokens = tokenizer.nextLine();
 *        System.out.println(tokens.get(0)); // prints 'text'
 *        System.out.println(tokens.get(1)); // prints 'text with, comma'
 *        System.out.println(tokens.get(2)); // prints ''
 *        System.out.println(tokens.get(3)); // prints 'another,text'
 *      }
 *    }
 *    catch(Exception ioe)
 *    {
 *      ioe.printStackTrace();
 *    }
 * </pre>
 * <p>
 * The advantages compared to the StreamTokenizer class are: Unlike
 * the StreamTokenizer, this Tokenizer class returns the delimiters as
 * tokens and therefore may be used to tokenize e.g. comma separated
 * files with empty fields (the StreamTokenizer handles multiple
 * delimiters in a row like one delimiter).
 * <p>
 * The tokenizer respect quoted words, so the delimiter is ignored if
 * inside quotes. And it may handle escaped characters (like an
 * escaped quote character, or an escaped new line). So the line
 * <code>eric,"he said, \"great!\""</code> returns <code>eric</code>
 * and <code>he said, "great!"</code> as words.
 * <p>
 * Low level interface: The design of the Tokenizer allows to get
 * empty columns as well as treat multiple delimiters in a row as one
 * delimiter. For the first approach trigger the values on every
 * DELIMITER and EOF token whereas for the second, trigger only on
 * WORD tokens.
 * <p>
 * If one wants to be informed about empty words as well, use the
 * Tokenizer like in the following code fragment:
 *  <pre>
 *   Tokenizer tokenizer = new Tokenizer("text,,,another text");
 *   String word = "";
 *   int token;
 *   while((token = tokenizer.nextToken()) != Tokenizer.EOF)
 *   {
 *     switch(token)
 *     {
 *     case Tokenizer.EOL:
 *       System.out.println("word: "+word);
 *       word = "";
 *       System.out.println("-------------");
 *       break;
 *     case Tokenizer.WORD:
 *       word = tokenizer.getWord();
 *       break;
 *     case Tokenizer.QUOTED_WORD:
 *       word = tokenizer.getWord() + " (quoted)";
 *       break;
 *     case Tokenizer.DELIMITER:
 *       System.out.println("word: "+word);
 *       word = "";
 *       break;
 *     default:
 *       System.err.println("Unknown Token: "+token);
 *     }
 *   }
 * </pre>
 * In this example, if the delimiter is set to a comma, a line like
 * <code>column1,,,"column4,partofcolumn4"</code> would be treated correctly.
 * <p>
 * This tokenizer uses the LF character as end of line characters. It
 * ignores any CR characters, so it can be used in windows
 * environments as well.
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.6 $
 */

public class Tokenizer
{
  /** the reader to read from */
  protected PushbackReader reader_;
  /** the buffer to create the tokens */
  protected StringBuffer buffer_;
  /** all characters in this string are used as delimiters */
  protected String delimiters_ = ",";
  /** the escape character */
  protected int escapeChar_ = '\\';
  /** the quote character */
  protected int quoteChar_ = '"';

  /** if true, characters are treated as escaped */
  protected boolean escapeMode_ = false;

  /** if true, end of line is respected */
  protected boolean eolIsSignificant_ = true;
  /** if true, escape characters are respected */
  protected boolean respectEscapedChars_ = false;
  /** if true, quoted words are respected */
  protected boolean respectQuotedWords_ = true;

  /** line count */
  protected int lineCount_ = 1;

  /** end of file marker */
  protected boolean eofReached_ = false;

  /** the last token that was found */
  protected int lastToken_ = NOT_STARTED;

  /** end of file token */
  public static final int EOF = -1;
  /** end of line token */
  public static final int EOL = 0;
  /** word token */
  public static final int WORD = 1;
  /** quoted word token */
  public static final int QUOTED_WORD = 2;
  /** delimiter token */
  public static final int DELIMITER = 3;
  /** error token */
  public static final int ERROR = 4;
  /** not started token */
  public static final int NOT_STARTED = 5;


//----------------------------------------------------------------------
/**
 * Creates a tokenizer that reads from the given string. It uses the
 * comma as delimiter, does not respect escape characters but respects
 * quoted words.
 *
 * @param string the string to read from.
 */
  public Tokenizer(String string)
  {
    this(new StringReader(string));
  }

//----------------------------------------------------------------------
/**
 * Creates a tokenizer that reads from the given string. All
 * characters in the given delimiters string are used as
 * delimiter. The tokenizer does not respect escape characters but
 * respects quoted words.
 *
 * @param string the string to read from.
 * @param delimiters the delimiters to use.
 */
  public Tokenizer(String string, String delimiters)
  {
    this(new StringReader(string));
    setDelimiters(delimiters);
  }

//----------------------------------------------------------------------
/**
 * Creates a tokenizer that reads from the given string. It uses the
 * comma as delimiter, does not respect escape characters but respects
 * quoted words.
 *
 * @param inStream the stream to read from.
 */
  public Tokenizer(InputStream inStream)
  {
    this(new InputStreamReader(inStream));
  }

//----------------------------------------------------------------------
/**
 * Creates a tokenizer that reads from the given reader. It uses the
 * comma as delimiter, does not respect escape characters but respects
 * quoted words.
 *
 * @param reader the reader to read from.
 */
  public Tokenizer(Reader reader)
  {
    reader_ = new PushbackReader(reader,2);
    buffer_ = new StringBuffer();
  }

//----------------------------------------------------------------------
/**
 * Set the delimiter character. The default is the comma.
 *
 * @param delimiterChar the delimiter character.
 */
  public void setDelimiter(int delimiterChar)
  {
    delimiters_ = new String(new char[]{(char)delimiterChar});
  }

//----------------------------------------------------------------------
/**
 * Get the first delimiter character.
 *
 * @return the delimiter character.
 * @deprecated use the getDelimiters() method now
 */
  public int getDelimiter()
  {
    return(delimiters_.charAt(0));
  }

//----------------------------------------------------------------------
/**
 * Set the delimiter characters. All characters in the delimiters are
 * used as delimiter.
 *
 * @param delimiters the delimiter characters.
 */
  public void setDelimiters(String delimiters)
  {
    delimiters_ = delimiters;
  }

//----------------------------------------------------------------------
/**
 * Get the delimiter character.
 *
 * @return the delimiter character.
 */
  public String getDelimiters()
  {
    return(delimiters_);
  }

//----------------------------------------------------------------------
/**
 * Set the escape character. The default is the backslash.
 *
 * @param escapeChar the escape character.
 */
  public void setEscapeChar(int escapeChar)
  {
    escapeChar_ = escapeChar;
  }

//----------------------------------------------------------------------
/**
 * Get the escape character.
 *
 * @return the escape character.
 */
  public int getEscapeChar()
  {
    return(escapeChar_);
  }

//----------------------------------------------------------------------
/**
 * If escape characters should be respected, set the param to
 * <code>true</code>. The default is to ignore escape characters.
 *
 * @param respectEscaped If escape characters should be respected,
 * set the param to <code>true</code>.
 */
  public void respectEscapedCharacters(boolean respectEscaped)
  {
    respectEscapedChars_ = respectEscaped;
  }

//----------------------------------------------------------------------
/**
 * Returns <code>true</code>, if escape character is respected.
 *
 * @return <code>true</code>, if escape character is respected.
 */
  public boolean respectEscapedCharacters()
  {
    return(respectEscapedChars_);
  }

//----------------------------------------------------------------------
/**
 * Get the quote character.
 *
 * @return the quote character.
 */
  public int getQuoteChar()
  {
    return (quoteChar_);
  }

//----------------------------------------------------------------------
/**
 * Set the quote character. The default is the double quote.
 *
 * @param quoteChar the quote character.
 */
  public void setQuoteChar(int quoteChar)
  {
    quoteChar_ = quoteChar;
  }

//----------------------------------------------------------------------
/**
 * If quoted words should be respected, set the param to
 * <code>true</code>. The default is to respect quoted words.
 *
 * @param respectQuotes If quoted words should be respected,
 * set the param to <code>true</code>.
 */
  public void respectQuotedWords(boolean respectQuotes)
  {
    respectQuotedWords_ = respectQuotes;
  }

//----------------------------------------------------------------------
/**
 * Returns <code>true</code>, if quoted words are respected.
 *
 * @return <code>true</code>, if quoted words are respected.
 */
  public boolean respectQuotedWords()
  {
    return(respectQuotedWords_);
  }

//----------------------------------------------------------------------
/**
 * If set to <code>true</code> the end of line is signaled by the EOL
 * token.  If set to <code>false</code> end of line is treated as a
 * normal delimiter. The default value is true;
 *
 * @param significant if the end of line is treated as a special token
 * or as a delimiter.
 */
  public void eolIsSignificant(boolean significant)
  {
    eolIsSignificant_ = significant;
  }

//----------------------------------------------------------------------
/**
 * Returns <code>true</code>, if in case of an end of line detected,
 * an EOL token is returned. If <code>false</code>, the end of line is
 * treated as a normal delimiter.
 *
 * @return <code>true</code>, if in case of an end of line detected,
 * an EOL token is returned. If <code>false</code>, the end of line is
 * treated as a normal delimiter.
 */
  public boolean isEolSignificant()
  {
    return(eolIsSignificant_);
  }


//----------------------------------------------------------------------
/**
 * Returns the current line number of the reader.
 *
 * @return the current line number of the reader.
 */
  public int getLineNumber()
  {
    return(lineCount_);
  }

//----------------------------------------------------------------------
/**
 * Returns the value of the token. If the token was of the type WORD,
 * the word is returned.
 *
 * @return the value of the token.
 */
  public String getWord()
  {
    return(buffer_.toString());
  }

//----------------------------------------------------------------------
/**
 * Returns the last token that was returned from the nextToken() method.
 *
 * @return the last token.
 */
  public int getLastToken()
  {
    return(lastToken_);
  }

//----------------------------------------------------------------------
/**
 * Returns true, if the given character is seen as a delimiter. This
 * method respects escape_mode, so if the escape character was found
 * before, it has to act accordingly (usually, return false, even if
 * the character is a delimiter).
 *
 * @param character the character to check for delimiter
 * @return true, if the given character is seen as a delimiter.
 */
  protected boolean isDelimiter(int character)
  {
        // check for escape mode:
    if(escapeMode_)
      return(false);

    return(delimiters_.indexOf(character) >= 0);
  }

//----------------------------------------------------------------------
/**
 * Returns true, if the given character is seen as a quote
 * character. This method respects escape_mode, so if the escape
 * character was found before, it has to act accordingly (usually,
 * return false, even if the character is a quote character).
 *
 * @param character the character to check for quote.
 * @return true, if the given character is seen as a quote character.
 */
  protected boolean isQuoteChar(int character)
  {
    if(!respectQuotedWords_)
      return(false);

        // check for escape mode:
    if(escapeMode_)
      return(false);

    return(character == quoteChar_);
  }

//----------------------------------------------------------------------
/**
 * Returns true, if the given character is seen as a escape
 * character. This method respects escape_mode, so if the escape
 * character was found before, it has to act accordingly (usually,
 * return false, even if the character is a escape character).
 * @param character the character to check for escape character.
 * @return true, if the given character is seen as a escape character.
 */
  protected boolean isEscapeChar(int character)
  {
    if(!respectEscapedChars_)
      return(false);

        // check for escape mode:
    if(escapeMode_)
      return(false);

    return(character == escapeChar_);
  }

//----------------------------------------------------------------------
/**
 * Returns true, if the given character is seen as a end of line
 * character. This method respects end of line_mode, so if the end of
 * line character was found before, it has to act accordingly
 * (usually, return false, even if the character is a end of line
 * character).
 * @param character the character to check for end of line.
 * @return true, if the given character is seen as a end of line
 * character.
 */
  protected boolean isEndOfLine(int character)
  {
        // check for escape mode:
    if(escapeMode_)
    {
      if(character == '\n')   // add line count, even if in escape mode!
        lineCount_++;
      return(false);
    }
    if(character == -1)
      eofReached_ = true;

    return((character=='\n') || (character=='\r') || (character == -1));
  }

//----------------------------------------------------------------------
/**
 * Closes the tokenizer (and the reader is uses internally).
 *
 * @exception IOException if an error occurred.
 */
  public void close()
    throws IOException
  {
    reader_.close();
  }

//----------------------------------------------------------------------
/**
 * Reads and returns the next character from the reader and checks for
 * the escape character. If an escape character is read, a flag is set
 * and the next character is read. A newline following the escape
 * character is ignored.
 *
 * @return the next character.
 * @exception IOException if an error occurred.
 */
  protected int readNextChar()
    throws IOException
  {
    int next_char = reader_.read();
    if(escapeMode_)
    {
      escapeMode_ = false;
    }
    else
    {
      if(isEscapeChar(next_char))
      {
            // ignore escape char itself:
        next_char = reader_.read();

            // check for newline and ignore it:
        if(isEndOfLine(next_char))
        {
          lineCount_++;
          next_char = reader_.read();
              // ignore CR:
          if(next_char == '\r')
          {
            next_char = readNextChar();
          }
        }
        escapeMode_ = true;
      }
    }
        // ignore CR:
    if(next_char == '\r')
    {
      next_char = readNextChar();
    }
    return(next_char);
  }

//----------------------------------------------------------------------
/**
 * Returns the next token from the reader. The token's value may be
 * WORD, QUOTED_WORD, EOF, EOL, or DELIMITER. In the case or WORD or
 * QUOTED_WORD the actual word can be obtained by the use of the
 * getWord method.
 *
 * @return the next token.
 * @exception IOException if an error occurred.
 */
  public int nextToken()
    throws IOException
  {
    buffer_.setLength(0);

    int next_char;
    next_char = readNextChar();

        // handle EOF:
    if(eofReached_)
    {
      lastToken_ = EOF;
      return(EOF);
    }

        // handle EOL:
    if(isEndOfLine(next_char))
    {
      lineCount_++;
      if(eolIsSignificant_)
      {
        lastToken_ = EOL;
        return(EOL);
      }
      else
      {
        lastToken_ = DELIMITER;
        return(DELIMITER);
      }
    }

        // handle DELIMITER
    if(isDelimiter(next_char))
    {
      lastToken_ = DELIMITER;
      return(DELIMITER);
    }

        // handle quoted words:
    if(isQuoteChar(next_char))
    {
      while(true)
      {
        next_char = readNextChar();
        if(isEndOfLine(next_char))
        {
          lastToken_ = ERROR;
          return(ERROR);
        }
        else
        {
          if(isQuoteChar(next_char))
          {
            lastToken_ = QUOTED_WORD;
            return(QUOTED_WORD);
          }

              // no special char, then append to buffer:
          buffer_.append((char)next_char);
        }
      }
    }

        // handle 'normal' words:
    while(true)
    {
      buffer_.append((char)next_char);
      next_char = readNextChar();
      if(isDelimiter(next_char) || isEndOfLine(next_char))
      {
        reader_.unread(next_char);
        lastToken_ = WORD;
        return(WORD);
      }
    }
  }

//----------------------------------------------------------------------
/**
 * Returns true, if the tokenizer can return another line.
 *
 * @return true, if the tokenizer can return another line.
 * @exception IOException if an error occurred.
 */
  public boolean hasNextLine()
    throws IOException
  {
    if(lastToken_ == EOF)
      return(false);

    if((lastToken_ == EOL) || (lastToken_ == NOT_STARTED))
    {
      int next_char = readNextChar();
      if(next_char == -1)
        return(false);

      reader_.unread(next_char);
    }
    return(true);
  }


//----------------------------------------------------------------------
/**
 * Returns a list of elements (Strings) from the next line of the
 * tokenizer. If there are multiple delimiters without any values in
 * between, empty (zero length) strings are added to the list. They
 * may be removed by the use of the {@link
 * #removeZeroLengthElements(List)} method.
 *
 * @return a list of elements (Strings) from the next line of the
 * tokenizer.
 * @exception IOException if an error occurred.
 */
  public List<String> nextLine()
    throws IOException
  {
    int token = nextToken();
    List<String> list = new ArrayList<String>();
    String word = "";
//    while(token != Tokenizer.EOF)
    while(true)
    {
      switch(token)
      {
        case Tokenizer.WORD:
          word = getWord();
          break;
        case Tokenizer.QUOTED_WORD:
          word = getWord();
          break;
        case Tokenizer.DELIMITER:
          list.add(word);
          word = "";
          break;
        case Tokenizer.EOL:
        case Tokenizer.EOF:
          list.add(word);
          return(list);
        default:
          System.err.println("Unknown Token: "+token);
      }
      token = nextToken();
    }
//    return(list);
  }

//----------------------------------------------------------------------
/**
 * This helper method removes all zero length elements from the given
 * list and returns it. The given list is not changed!
 *
 * @param list the list of String objects to remove the zero elements from.
 * @return a copy of the given list where all zero length elements are removed.
 */
  public static List<String> removeZeroLengthElements(List<String> list)
  {
    return removeZeroLengthElements(list, false);
  }

//----------------------------------------------------------------------
  /**
   * This helper method trims all elements and removes all zero length
   * (length is taken after trimming leading and trailing spaces) elements from the given
   * list and returns it. This method copies the (trimmed and) non-zero elements to a
   * new list.
   *
   * @param list the list of String objects to remove the zero elements from.
   * @param trim if set to <code>true</code>, all leading and trailing spaces are removed from
   * the elements. This is done, before the length is compared to zero (and the element
   * may be removed if the length is zero). If set to <code>true</code>, elements
   * that only consist of spaces are removed as well!
   * @return the list where all zero length elements are remove.
   */
    public static List<String> removeZeroLengthElements(List<String> list, boolean trim)
    {
      Iterator<String> iterator = list.iterator();
      String value;
      List<String> new_list = new ArrayList<String>();
      while(iterator.hasNext())
      {
        value = iterator.next();
        if (trim)
          value = value.trim();
        if(value.length() != 0)
          new_list.add(value);
      }
      return(new_list);
    }

//  /**
//   * Demonstrates the low level interface.
//   * @param args command line arguments.
//   */
//  protected static void testLowLevel(String[] args)
//  {
//    try
//    {
//      String filename;
//      if(args.length > 0)
//        filename = args[0];
//      else
//        filename = "/filer/cdaller/tmp/test.csv";
//
//      Tokenizer tokenizer = new Tokenizer(new BufferedReader(new FileReader(filename)));
////      Tokenizer tokenizer = new Tokenizer("column1,\"quoted column2\",column3\\, with quoted comma");
//      tokenizer.setDelimiter(',');
////      tokenizer.eolIsSignificant(false);
//      tokenizer.respectEscapedCharacters(true);
//      tokenizer.respectQuotedWords(true);
//
//      int token;
//      while((token = tokenizer.nextToken()) != Tokenizer.EOF)
//      {
//        switch(token)
//        {
//        case Tokenizer.EOL:
//          System.out.println("------------- ");
//          break;
//        case Tokenizer.WORD:
//          System.out.println("line" +tokenizer.getLineNumber() +" word: "+tokenizer.getWord());
//          break;
//        case Tokenizer.QUOTED_WORD:
//          System.out.println("line" +tokenizer.getLineNumber() +" quoted word: "+tokenizer.getWord());
//          break;
//        case Tokenizer.DELIMITER:
//          System.out.println("delimiter");
//          break;
//        default:
//          System.err.println("Unknown Token: "+token);
//        }
//      }
//      tokenizer.close();
//    }
//    catch(Exception ioe)
//    {
//      ioe.printStackTrace();
//    }
//  }
//
//
//  /**
//   * Demonstration of the high level interface.
//   * @param args command line arguments.
//   */
//  protected static void testHighLevel(String[] args)
//  {
//    try
//    {
//      String filename;
//      if(args.length > 0)
//        filename = args[0];
//      else
//        filename = "/filer/cdaller/tmp/test.csv";
//
//      Tokenizer tokenizer = new Tokenizer(new BufferedReader(new FileReader(filename)));
////      Tokenizer tokenizer = new Tokenizer("column1,\"quoted column2\",column3\\, with quoted comma");
//      tokenizer.setDelimiter(',');
////      tokenizer.eolIsSignificant(false);
//      tokenizer.respectEscapedCharacters(true);
//      tokenizer.respectQuotedWords(true);
//
//      List list;
//      while(tokenizer.hasNextLine())
//      {
//        list = tokenizer.nextLine();
//        System.out.println("List: "+list);
//        System.out.println("List w/o zero length elements: "+removeZeroLengthElements(list));
//        System.out.println("--");
//      }
//
//    }
//    catch(Exception ioe)
//    {
//      ioe.printStackTrace();
//    }
//  }
//
//   /**
//   *  Demo code for the high level interface.
//   */
//  protected static void testHighLevelExample()
//  {
//    try
//    {
//          // simple example, tokenizing string, no escape, but quoted
//          // works:
//      System.out.println("example 1");
//      Tokenizer tokenizer = new Tokenizer("text,,,\"another,text\"");
//      List tokens;
//      while(tokenizer.hasNextLine())
//      {
//        tokens = tokenizer.nextLine();
//        System.out.println(tokens.get(0)); // prints 'text'
//        System.out.println(tokens.get(1)); // prints ''
//        System.out.println(tokens.get(2)); // prints ''
//        System.out.println(tokens.get(3)); // prints 'another,text'
//      }
//
//      System.out.println("example 2");
//          // simple example, tokenizing string, using escape char and
//          // quoted strings:
//      tokenizer = new Tokenizer("text,text with\\,comma,,\"another,text\"");
//      tokenizer.respectEscapedCharacters(true);
//      while(tokenizer.hasNextLine())
//      {
//        tokens = tokenizer.nextLine();
//        System.out.println(tokens.get(0)); // prints 'text'
//        System.out.println(tokens.get(1)); // prints 'text with, comma'
//        System.out.println(tokens.get(2)); // prints ''
//        System.out.println(tokens.get(3)); // prints 'another,text'
//      }
//    }
//    catch(Exception ioe)
//    {
//      ioe.printStackTrace();
//    }
//  }
//
//  public static void main(String[] args)
//  {
////    testLowLevel(args);
////    testHighLevel(args);
////    testGeonetUTF8(args);
//    testHighLevelExample();
//  }
}


