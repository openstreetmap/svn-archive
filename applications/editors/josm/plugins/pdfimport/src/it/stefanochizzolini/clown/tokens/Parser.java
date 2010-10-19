/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)
    * Haakan Aakerberg (bugfix contributor):
      - [FIX:0.0.4:1]
      - [FIX:0.0.4:4]

  This file should be part of the source code distribution of "PDF Clown library"
  (the Program): see the accompanying README files for more info.

  This Program is free software; you can redistribute it and/or modify it under the terms
  of the GNU Lesser General Public License as published by the Free Software Foundation;
  either version 3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  either expressed or implied; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this
  Program (see README files); if not, go to the GNU website (http://www.gnu.org/licenses/).

  Redistribution and use, with or without modification, are permitted provided that such
  redistributions retain the above copyright notice, license and disclaimer, along with
  this list of conditions.
*/

package it.stefanochizzolini.clown.tokens;

import it.stefanochizzolini.clown.bytes.Buffer;
import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfBoolean;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDate;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.objects.PdfTextString;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.Date;

/**
  Token parser.
  <h3>Contract</h3>
  <ul>
    <li>Preconditions:
    <ol>
      <li>(none).</li>
    </ol>
    </li>
    <li>Postconditions:
    <ol>
      <li>(none).</li>
    </ol>
    </li>
    <li>Invariants:
    <ol>
      <li>Stream data IS kept untouched.</li>
    </ol>
    </li>
    <li>Side-effects:
    <ol>
      <li>(none).</li>
    </ol>
    </li>
  </ul>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8.2
*/
public class Parser
  implements Closeable
{
  // <class>
  // <classes>
  public class Reference
  {
    // <class>
    // <fields>
    private final int generationNumber;
    private final int objectNumber;
    // </fields>

    // <constructors>
    private Reference(
      int objectNumber,
      int generationNumber
      )
    {
      this.objectNumber = objectNumber;
      this.generationNumber = generationNumber;
    }
    // </constructors>

    // <interface>
    // <public>
    public int getGenerationNumber(
      )
    {return generationNumber;}

    public int getObjectNumber(
      )
    {return objectNumber;}
    // </public>
    // </interface>
    // </class>
  }
  // </classes>

  // <static>
  // <fields>
  private static final String PdfHeader = "%PDF-";
  // </fields>

  // <interface>
  // <protected>
  /**
    Evaluates whether a character is a delimiter [PDF:1.6:3.1.1].
  */
  protected static boolean isDelimiter(
    int c
    )
  {return (c == '(' || c == ')' || c == '<' || c == '>' || c == '[' || c == ']' || c == '/' || c == '%');}

  /**
    Evaluates whether a character is an EOL marker [PDF:1.6:3.1.1].
  */
  protected static boolean isEOL(
    int c
    )
  {return c == 10 || c == 13;}

  /**
    Evaluates whether a character is a white-space [PDF:1.6:3.1.1].
  */
  protected static boolean isWhitespace(
    int c
    )
  {return c == 32 || isEOL(c) || c == 0 || c == 9 || c == 12;}
  // </protected>
  // </interface>
  // </static>

  // <dynamic>
  // <fields>
  private File file;
  private IInputStream stream;
  private Object token;
  private TokenTypeEnum tokenType;

  private boolean multipleTokenParsing;
  // </fields>

  // <constructors>
  Parser(
    IInputStream stream,
    File file
    )
  {
    this.stream = stream;
    this.file = file;
  }
  // </constructors>

  // <interface>
  // <public>
  public long getLength(
    )
  {return stream.getLength();}

  public long getPosition(
    )
  {return stream.getPosition();}

  public IInputStream getStream(
    )
  {return stream;}

  /**
    Gets the currently-parsed token.
    @return The current token.
  */
  public Object getToken(
    )
  {return token;}

  /**
    Gets the currently-parsed token type.
    @return The current token type.
  */
  public TokenTypeEnum getTokenType(
    )
  {return tokenType;}

  public int hashCode(
    )
  {return stream.hashCode();}

  /**
    @param offset Number of tokens to be skipped before reaching the intended one.
  */
  public boolean moveNext(
    int offset
    ) throws FileFormatException
  {
    for(
      int index = 0;
      index < offset;
      index++
      )
    {
      if(!moveNext())
        return false;
    }

    return true;
  }

  /**
    Parse the next token [PDF:1.6:3.1].
    <h3>Contract</h3>
    <ul>
     <li>Preconditions:
      <ol>
       <li>To properly parse the current token, the pointer MUST be just before its starting (leading whitespaces are ignored).</li>
      </ol>
     </li>
     <li>Postconditions:
      <ol>
       <li id="moveNext_contract_post[0]">When this method terminates, the pointer IS at the last byte of the current token.</li>
      </ol>
     </li>
     <li>Invariants:
      <ol>
       <li>The byte-level position of the pointer IS anytime (during token parsing) at the end of the current token (whereas the 'current token' represents the token-level position of the pointer).</li>
      </ol>
     </li>
     <li>Side-effects:
      <ol>
       <li>See <a href="#moveNext_contract_post[0]">Postconditions</a>.</li>
      </ol>
     </li>
    </ul>
    @return Whether a new token was found.
  */
  public boolean moveNext(
    ) throws FileFormatException
  {
    /*
      NOTE: It'd be interesting to evaluate an alternative regular-expression-based
      implementation...
    */
    StringBuilder buffer = null;
    token = null;
    int c = 0;

    // Skip leading white-space characters [PDF:1.6:3.1.1].
    try
    {
      do
      {
        c = stream.readUnsignedByte();
      } while(isWhitespace(c)); // Keep goin' till there's a white-space character...
    }
    catch(EOFException e)
    {return false;}

    // Which character is it?
    switch(c)
    {
      case '/': // Name [PDF:1.6:3.2.4].
        tokenType = TokenTypeEnum.Name;

        /*
          NOTE: As name objects are atomic symbols uniquely defined by sequences of characters,
          the bytes making up the name are never treated as text, so here they are just
          passed through without unescaping.
        */
        buffer = new StringBuilder();
        try
        {
          while(true)
          {
            c = stream.readUnsignedByte();
            if(isDelimiter(c) || isWhitespace(c))
              break;

            buffer.append((char)c);
          }
        }
        catch(EOFException e)
        {throw new FileFormatException("Unexpected EOF (malformed name object).",e,stream.getPosition());}

        stream.skip(-1); // Recover the first byte after the current token.
        break;
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
      case '.':
      case '-':
      case '+': // Number [PDF:1.6:3.2.2] | Indirect reference.
        switch(c)
        {
          case '.': // Decimal point.
            tokenType = TokenTypeEnum.Real;
            break;
          case '-':
          case '+': // Signum.
            tokenType = TokenTypeEnum.Integer; // By default (it may be real).
            break;
          default: // Digit.
            if(multipleTokenParsing) // Plain number (multiple token parsing -- see indirect reference search).
            {
              tokenType = TokenTypeEnum.Integer; // By default (it may be real).
            }
            else // Maybe an indirect reference (postfix notation [PDF:1.6:3.2.9]).
            {
              /*
                NOTE: We need to identify this pattern:
                ref :=  { int int 'R' }
              */
              // Enable multiple token parsing!
              // NOTE: This state MUST be disabled before returning.
              multipleTokenParsing = true;

              // 1. Object number.
              // Try the possible object number!
              stream.skip(-1); moveNext();
              // Isn't it a valid object number?
              if(tokenType != TokenTypeEnum.Integer)
              {
                // Disable multiple token parsing!
                multipleTokenParsing = false;
                return true;
              }
              // Assign object number!
              int objectNumber = (Integer)token;
              // Backup the recovery position!
              long oldOffset = stream.getPosition();

              // 2. Generation number.
              // Try the possible generation number!
              moveNext();
              // Isn't it a valid generation number?
              if(tokenType != TokenTypeEnum.Integer)
              {
                // Rollback!
                stream.seek(oldOffset);
                token = objectNumber; tokenType = TokenTypeEnum.Integer;
                // Disable multiple token parsing!
                multipleTokenParsing = false;
                return true;
              }
              // Assign generation number!
              int generationNumber = (Integer)token;

              // 3. Reference keyword.
              // Try the possible reference keyword!
              moveNext();
              // Isn't it a valid reference keyword?
              if(tokenType != TokenTypeEnum.Reference)
              {
                // Rollback!
                stream.seek(oldOffset);
                token = objectNumber; tokenType = TokenTypeEnum.Integer;
                // Disable multiple token parsing!
                multipleTokenParsing = false;
                return true;
              }
              token = new Reference(objectNumber,generationNumber);
              // Disable multiple token parsing!
              multipleTokenParsing = false;
              return true;
            }
            break;
        }

        // Building the number...
        buffer = new StringBuilder();
        try
        {
          do
          {
            buffer.append((char)c);
            c = stream.readUnsignedByte();
            if(c == '.')
              tokenType = TokenTypeEnum.Real;
            else if(c < '0' || c > '9')
              break;
          } while(true);
        }
        catch(EOFException e)
        {throw new FileFormatException("Unexpected EOF (malformed number object).",e,stream.getPosition());}

        stream.skip(-1); // Recover the first byte after the current token.
        break;
      case '[': // Array (begin).
        tokenType = TokenTypeEnum.ArrayBegin;
        break;
      case ']': // Array (end).
        tokenType = TokenTypeEnum.ArrayEnd;
        break;
      case '<': // Dictionary (begin) | Hexadecimal string.
        try
        {c = stream.readUnsignedByte();}
        catch(EOFException e)
        {throw new FileFormatException("Unexpected EOF (isolated opening angle-bracket character).",e,stream.getPosition());}
        // Is it a dictionary (2nd angle bracket [PDF:1.6:3.2.6])?
        if(c == '<')
        {
          tokenType = TokenTypeEnum.DictionaryBegin;
          break;
        }

        // Hexadecimal string (single angle bracket [PDF:1.6:3.2.3]).
        tokenType = TokenTypeEnum.Hex;

        // [FIX:0.0.4:4] It skipped after the first hexadecimal character, missing it.
        buffer = new StringBuilder();
        try
        {
          while(c != '>') // NOT string end.
          {
            buffer.append((char)c);

            c = stream.readUnsignedByte();
          }
        }
        catch(EOFException e)
        {throw new FileFormatException("Unexpected EOF (malformed hex string).",e,stream.getPosition());}

        break;
      case '>': // Dictionary (end).
        try
        {c = stream.readUnsignedByte();}
        catch(EOFException e)
        {throw new FileFormatException("Unexpected EOF (malformed dictionary).",e,stream.getPosition());}
        if(c != '>')
          throw new FileFormatException("Malformed dictionary.",stream.getPosition());

        tokenType = TokenTypeEnum.DictionaryEnd;

        break;
      case '(': // Literal string [PDF:1.6:3.2.3].
        tokenType = TokenTypeEnum.Literal;

        /*
          NOTE: As literal objects are textual, their characters are unescaped when deserialized.
        */
        buffer = new StringBuilder();
        int level = 0;
        try
        {
          while(true)
          {
            c = stream.readUnsignedByte();
            if(c == '(')
              level++;
            else if(c == ')')
              level--;
            else if(c == '\\')
            {
              boolean lineBreak = false;
              c = stream.readUnsignedByte();
              switch(c)
              {
                case 'n':
                  c = '\n';
                  break;
                case 'r':
                  c = '\r';
                  break;
                case 't':
                  c = '\t';
                  break;
                case 'b':
                  c = '\b';
                  break;
                case 'f':
                  c = '\f';
                  break;
                case '(':
                case ')':
                case '\\':
                  break;
                case '\r':
                  lineBreak = true;
                  c = stream.readUnsignedByte();
                  if(c != '\n')
                    stream.skip(-1);
                  break;
                case '\n':
                  lineBreak = true;
                  break;
                default:
                {
                  // Is it outside the octal encoding?
                  if(c < '0' || c > '7')
                    break;

                  // Octal.
                  int octal = c - '0';
                  c = stream.readUnsignedByte();
                  // Octal end?
                  if(c < '0' || c > '7')
                  {c = octal; stream.skip(-1); break;}
                  octal = (octal << 3) + c - '0';
                  c = stream.readUnsignedByte();
                  // Octal end?
                  if(c < '0' || c > '7')
                  {c = octal; stream.skip(-1); break;}
                  octal = (octal << 3) + c - '0';
                  c = octal & 0xff;
                  break;
                }
              }
              if(lineBreak)
                continue;
            }
            else if(c == '\r')
            {
              c = stream.readUnsignedByte();
              if(c != '\n')
              {c = '\n'; stream.skip(-1);}
            }
            if(level == -1)
              break;

            buffer.append((char)c);
          }
        }
        catch(EOFException e)
        {throw new FileFormatException("Unexpected EOF (malformed literal string).",e,stream.getPosition());}

        break;
      case 'R': // Indirect reference.
        tokenType = TokenTypeEnum.Reference;

        break;
      case '%': // Comment [PDF:1.6:3.1.2].
        tokenType = TokenTypeEnum.Comment;
        
        buffer = new StringBuilder();
        try
        {
          while(true)
          {
          	c = stream.readUnsignedByte();
          	if(isEOL(c))
          		break;
          	
          	buffer.append((char)c);
          }
        }
        catch(EOFException e)
        {/* NOOP */}

        break;
      default: // Keyword object.
        tokenType = TokenTypeEnum.Keyword;

        buffer = new StringBuilder();
        try
        {
          do
          {
            buffer.append((char)c);
            c = stream.readUnsignedByte();
          } while(!isDelimiter(c) && !isWhitespace(c));
        }
        catch(EOFException e)
        {/* NOOP */}
        stream.skip(-1); // Recover the first byte after the current token.

        break;
    }

    if(buffer != null)
    {
      /*
        Here we prepare the current token state.
      */
      // Which token type?
      switch(tokenType)
      {
        case Keyword:
          token = buffer.toString();
          // Late recognition.
          if(((String)token).equals("false")
            || ((String)token).equals("true")) // Boolean.
          {
            tokenType = TokenTypeEnum.Boolean;
            token = Boolean.parseBoolean((String)token);
          }
          else if(((String)token).equals("null")) // Null.
          {
            tokenType = TokenTypeEnum.Null;
            token = null;
          }
          break;
        case Comment:
        case Hex:
        case Name:
          token = buffer.toString();
          break;
        case Literal:
          token = buffer.toString();
          // Late recognition.
          if(((String)token).startsWith("D:")) // Date.
          {
            tokenType = TokenTypeEnum.Date;
            token = PdfDate.toDate((String)token);
          }
          break;
        case Integer:
          token = Integer.parseInt(buffer.toString());
          break;
        case Real:
          token = Float.parseFloat(buffer.toString());
          break;
      }
    }

    return true;
  }

  /**
    Parse the current PDF object [PDF:1.6:3.2].
    <h3>Contract</h3>
    <ul>
     <li>Preconditions:
      <ol>
       <li>When this method is invoked, the pointer MUST be at the first
       token of the requested object.</li>
      </ol>
     </li>
     <li>Postconditions:
      <ol>
       <li id="parsePdfObject_contract_post[0]">When this method terminates,
       the pointer IS at the last token of the requested object.</li>
      </ol>
     </li>
     <li>Invariants:
      <ol>
       <li>(none).</li>
      </ol>
     </li>
     <li>Side-effects:
      <ol>
       <li>See <a href="#parsePdfObject_contract_post[0]">Postconditions</a>.</li>
      </ol>
     </li>
    </ul>
  */
  public PdfDataObject parsePdfObject(
    ) throws FileFormatException
  {
    /*
      NOTE: Object parsing is intrinsically a sequential operation tied to the stream pointer.
      Calls bound towards other classes are potentially disruptive for the predictability of
      the position of the stream pointer, so we are forced to carefully keep track of our
      current position in order to recover its proper state after any outbound call.
    */
  	do
  	{
	    // Which token type?
	    switch(tokenType)
	    {
	      case Integer:
	        return new PdfInteger((Integer)token);
	      case Name:
	        return new PdfName((String)token,true);
	      case Reference:
	        /*
	          NOTE: Curiously, PDF references are the only primitive objects that require
	          a file reference. That's because they deal with indirect objects, which are strongly
	          coupled with the current state of the file: so, PDF references are the fundamental
	          bridge between the token layer and the file layer.
	        */
	        return new PdfReference(
	          (Reference)token,
	          file
	          );
	      case Literal:
	        try
	        {
	          return new PdfTextString(
	            ((String)token).getBytes("ISO-8859-1")
	            );
	        }
	        catch(Exception e)
	        {throw new RuntimeException(e);}
	      case DictionaryBegin:
	        PdfDictionary dictionary = new PdfDictionary();
	        // Populate the dictionary.
	        while(true)
	        {
	          // Key.
	          moveNext();
	          if(tokenType == TokenTypeEnum.DictionaryEnd)
	            break;
	          PdfName key = (PdfName)parsePdfObject();
	
	          // Value.
	          moveNext();
	          PdfDirectObject value = (PdfDirectObject)parsePdfObject();
	
	          // Add the current entry to the dictionary!
	          dictionary.put(key,value);
	        }
	
	        int oldOffset = (int)stream.getPosition();
	        moveNext();
	        // Is this dictionary the header of a stream object [PDF:1.6:3.2.7]?
	        if((tokenType == TokenTypeEnum.Keyword)
	          && token.equals("stream")) // Stream.
	        {
	          // Keep track of current position!
	          long position = stream.getPosition();
	
	          // Get the stream length!
	          /*
	            NOTE: Indirect reference resolution is an outbound call (stream pointer hazard!),
	            so we need to recover our current position after it returns.
	          */
	          int length = ((PdfInteger)File.resolve(dictionary.get(PdfName.Length))).getRawValue();
	
	          // Come back to current position!
	          stream.seek(position);
	
	          skipWhitespace();
	
	          // Copy the stream data to the instance!
	          byte[] data = new byte[length];
	          try
	          {stream.read(data);}
	          catch(EOFException e)
	          {throw new FileFormatException("Unexpected EOF (malformed stream object).",e,stream.getPosition());}
	
	          moveNext(); // Postcondition (last token should be 'endstream' keyword).
	
	          return new PdfStream(
	            dictionary,
	            new Buffer(data)
	            );
	        }
	        else // Simple dictionary.
	        {
	          stream.seek(oldOffset); // Restore postcondition (last token should be the dictionary end).
	
	          return dictionary;
	        }
	      case ArrayBegin:
	        PdfArray array = new PdfArray();
	        // Populate the array.
	        while(true)
	        {
	          // Value.
	          moveNext();
	          if(tokenType == TokenTypeEnum.ArrayEnd)
	            break;
	
	          // Add the current item to the array!
	          array.add((PdfDirectObject)parsePdfObject());
	        }
	        return array;
	      case Real:
	        return new PdfReal((Float)token);
	      case Boolean:
	        return new PdfBoolean((Boolean)token);
	      case Date:
	        return new PdfDate((Date)token);
	      case Hex:
	        try
	        {
	          return new PdfString(
	            (String)token,
	            PdfString.SerializationModeEnum.Hex
	            );
	        }
	        catch(Exception e)
	        {throw new RuntimeException(e);}
	      case Null:
	        return null;
	      case Comment:
	      	// NOOP: Comments are simply ignored and skipped.
	      	break;
	      default:
	        throw new RuntimeException("Unhandled type: " + tokenType);
	    }
  	} while(moveNext());

  	return null;
  }

  /**
    Retrieves the PDF version of the file [PDF:1.6:3.4.1].
    <h3>Contract</h3>
    <ul>
     <li>Preconditions:
      <ol>
       <li>(none).</li>
      </ol>
     </li>
     <li>Postconditions:
      <ol>
       <li>(none).</li>
      </ol>
     </li>
     <li>Invariants:
      <ol>
       <li>(none).</li>
      </ol>
     </li>
     <li>Side-effects:
      <ol>
       <li>The pointer is released at an undefined location.</li>
      </ol>
     </li>
    </ul>
  */
  public String retrieveVersion(
    ) throws FileFormatException
  {
    stream.seek(0);
    String header;
    try{header = stream.readString(10);}
    catch(EOFException e){throw new FileFormatException("Unexpected EOF (malformed version data).",e,stream.getPosition());}
    if(!header.startsWith(PdfHeader))
      throw new FileFormatException("PDF header not found.",stream.getPosition());

    return header.substring(PdfHeader.length(),PdfHeader.length() + 3);
  }

  /**
    Retrieves the starting position of the last xref-table section.
    @see #retrieveXRefOffset(long)
  */
  public long retrieveXRefOffset(
    ) throws FileFormatException
  {return retrieveXRefOffset(stream.getLength());}

  /**
    Retrieves the starting position of an xref-table section [PDF:1.6:3.4.4].
    <h3>Contract</h3>
    <ul>
     <li>Preconditions:
      <ol>
       <li>(none).</li>
      </ol>
     </li>
     <li>Postconditions:
      <ol>
       <li>(none).</li>
      </ol>
     </li>
     <li>Invariants:
      <ol>
       <li>(none).</li>
      </ol>
     </li>
     <li>Side-effects:
      <ol>
       <li>The pointer is released at an undefined location.</li>
      </ol>
     </li>
    </ul>
    @param offset Position of the EOF marker related to the section intended to be parsed.
  */
  public long retrieveXRefOffset(
    long offset
    ) throws FileFormatException
  {
    final int chunkSize = 1024; // [PDF:1.6:H.3.18].

    // Move back before 'startxref' keyword!
    long position = offset - chunkSize;
    if (position < 0)
    {position = 0;} // [FIX:0.0.4:1] It failed to deal with less-than-1024-byte-long PDF files.
    stream.seek(position);

    // Get 'startxref' keyword position!
    int index;
    try{index = stream.readString(chunkSize).lastIndexOf("startxref");}
    catch(EOFException e){throw new FileFormatException("Unexpected EOF (malformed 'startxref' tag).",e,stream.getPosition());}
    if(index < 0)
      throw new FileFormatException("PDF startxref not found.",stream.getPosition());
    // Go past the 'startxref' keyword!
    stream.seek(position + index); moveNext();

    // Get the xref offset!
    moveNext();
    if(tokenType != TokenTypeEnum.Integer)
      throw new FileFormatException("PDF startxref malformed.",stream.getPosition());

    return (Integer)token;
  }

  public void seek(
    long position
    )
  {stream.seek(position);}

  public void skip(
    long offset
    )
  {stream.skip(offset);}

  /**
    Moves to the last whitespace after the current position in order to let read
    the first non-whitespace.
  */
  public boolean skipWhitespace(
    )
  {
    int b;
    try
    {
      do
      {b = stream.readUnsignedByte();} while(isWhitespace(b)); // Keep goin' till there's a white-space character...
    }
    catch(EOFException e)
    {return false;}
    stream.skip(-1); // Recover the last whitespace position.

    return true;
  }

  // <Closeable>
  public void close(
    ) throws IOException
  {
    if(stream != null)
    {
      stream.close();
      stream = null;
    }
  }
  // </Closeable>
  // </public>

  // <protected>
  @Override
  protected void finalize(
    ) throws Throwable
  {
    try
    {close();}
    finally
    {super.finalize();}
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}