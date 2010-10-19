/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)
    * Haakan Aakerberg (bugfix contributor):
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

package it.stefanochizzolini.clown.documents.contents.tokens;

import it.stefanochizzolini.clown.bytes.Buffer;
import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.documents.contents.objects.BeginInlineImage;
import it.stefanochizzolini.clown.documents.contents.objects.BeginMarkedContent;
import it.stefanochizzolini.clown.documents.contents.objects.BeginSubpath;
import it.stefanochizzolini.clown.documents.contents.objects.BeginText;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.objects.DrawRectangle;
import it.stefanochizzolini.clown.documents.contents.objects.EndInlineImage;
import it.stefanochizzolini.clown.documents.contents.objects.EndMarkedContent;
import it.stefanochizzolini.clown.documents.contents.objects.EndText;
import it.stefanochizzolini.clown.documents.contents.objects.InlineImage;
import it.stefanochizzolini.clown.documents.contents.objects.InlineImageBody;
import it.stefanochizzolini.clown.documents.contents.objects.InlineImageHeader;
import it.stefanochizzolini.clown.documents.contents.objects.LocalGraphicsState;
import it.stefanochizzolini.clown.documents.contents.objects.MarkedContent;
import it.stefanochizzolini.clown.documents.contents.objects.Operation;
import it.stefanochizzolini.clown.documents.contents.objects.PaintPath;
import it.stefanochizzolini.clown.documents.contents.objects.PaintShading;
import it.stefanochizzolini.clown.documents.contents.objects.PaintXObject;
import it.stefanochizzolini.clown.documents.contents.objects.Path;
import it.stefanochizzolini.clown.documents.contents.objects.RestoreGraphicsState;
import it.stefanochizzolini.clown.documents.contents.objects.SaveGraphicsState;
import it.stefanochizzolini.clown.documents.contents.objects.Shading;
import it.stefanochizzolini.clown.documents.contents.objects.Text;
import it.stefanochizzolini.clown.documents.contents.objects.XObject;
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
import it.stefanochizzolini.clown.tokens.FileFormatException;
import it.stefanochizzolini.clown.tokens.TokenTypeEnum;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
  Content stream parser [PDF:1.6:3.7.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8.2
*/
public class Parser
{
/*
TODO:IMPL this parser evaluates a subset of the lexical domain of the token parser (clown.tokens.Parser): it should be better to derive both parsers from a common parsing engine in order to avoid unwieldy duplications.
*/
  // <class>
  // <static>
  // <interface>
  // <protected>
  protected static int getHex(
    int c
    )
  {
    if(c >= '0' && c <= '9')
      return (c - '0');
    else if(c >= 'A' && c <= 'F')
      return (c - 'A' + 10);
    else if(c >= 'a' && c <= 'f')
      return (c - 'a' + 10);
    else
      return -1;
  }

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
  private final PdfDataObject contentStream;

  private long basePosition;
  private IInputStream stream;
  private int streamIndex = -1;
  private Object token;
  private TokenTypeEnum tokenType;
  // </fields>

  // <constructors>
  /**
    For internal use only.
  */
  public Parser(
    PdfDataObject contentStream
    )
  {
    this.contentStream = contentStream;

    moveNextStream();
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the content stream on which parsing is done.
    <h3>Remarks</h3>
    <p>A content stream may be made up of either a single stream or an array of streams.</p>
  */
  public PdfDataObject getContentStream(
    )
  {return contentStream;}

  public long getLength(
    )
  {
    if(contentStream instanceof PdfStream) // Single stream.
      return ((PdfStream)contentStream).getBody().getLength();
    else // Array of streams.
    {
      int length = 0;
      for(PdfDirectObject stream : (PdfArray)contentStream)
      {length += ((PdfStream)((PdfReference)stream).getDataObject()).getBody().getLength();}
      return length;
    }
  }

  public long getPosition(
    )
  {return basePosition + stream.getPosition();}

  /**
    Gets the current stream.
  */
  public IInputStream getStream(
    )
  {return stream;}

  /**
    Gets the current stream index.
  */
  public int getStreamIndex(
    )
  {return streamIndex;}

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
    int c = 0;

    // Skip leading white-space characters [PDF:1.6:3.1.1].
    while(true)
    {
      try
      {
        do
        {
          c = stream.readUnsignedByte();
        } while(isWhitespace(c)); // Keep goin' till there's a white-space character...
        break;
      }
      catch(EOFException e)
      {
        /* NOTE: Current stream has finished. */
        // Move to the next stream!
        moveNextStream();
      }
      catch(Exception e)
      {
        // No more streams?
        if(stream == null)
          return false;
      }
    }

    StringBuilder buffer = null;
    token = null;
    // Which character is it?
    switch(c)
    {
      case '/': // Name.
        tokenType = TokenTypeEnum.Name;

        buffer = new StringBuilder();
        try
        {
          while(true)
          {
            c = stream.readUnsignedByte();
            if(isDelimiter(c) || isWhitespace(c))
              break;
            // Is it an hexadecimal code [PDF:1.6:3.2.4]?
            if(c == '#')
            {
              try
              {c = (getHex(stream.readUnsignedByte()) << 4) + getHex(stream.readUnsignedByte());}
              catch(EOFException e)
              {throw new FileFormatException("Unexpected EOF (malformed hexadecimal code in name object).",e,stream.getPosition());}
            }

            buffer.append((char)c);
          }
        }
        catch(EOFException e)
        {/* NOOP */}

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
          default: // Digit or signum.
            tokenType = TokenTypeEnum.Integer; // By default (it may be real).
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
        {/* NOOP */}

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
      case '(': // Literal string.
        tokenType = TokenTypeEnum.Literal;

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

                  // Octal [PDF:1.6:3.2.3].
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
      default: // Keyword.
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
    Parses the next content object [PDF:1.6:4.1], may it be a single operation or a graphics object.

    @since 0.0.4
  */
  public ContentObject parseContentObject(
    ) throws FileFormatException
  {
    final Operation operation = parseOperation();
    if(operation instanceof PaintXObject) // External object.
      return new XObject((PaintXObject)operation);
    else if(operation instanceof PaintShading) // Shading.
      return new Shading((PaintShading)operation);
    else if(operation instanceof BeginSubpath
      || operation instanceof DrawRectangle) // Path.
      return parsePath(operation);
    else if(operation instanceof BeginText) // Text.
      return new Text(
        parseContentObjects()
        );
    else if(operation instanceof SaveGraphicsState) // Local graphics state.
      return new LocalGraphicsState(
        parseContentObjects()
        );
    else if(operation instanceof BeginMarkedContent) // Marked-content sequence.
      return new MarkedContent(
        (BeginMarkedContent)operation,
        parseContentObjects()
        );
    else if(operation instanceof BeginInlineImage) // Inline image.
      return parseInlineImage();
    else // Single operation.
      return operation;
  }

  public List<ContentObject> parseContentObjects(
    ) throws FileFormatException
  {
    final List<ContentObject> contentObjects = new ArrayList<ContentObject>(2);
    while(moveNext())
    {
      ContentObject contentObject = parseContentObject();
      // Multiple-operation graphics object end?
      if(contentObject instanceof EndText // Text.
        || contentObject instanceof RestoreGraphicsState // Local graphics state.
        || contentObject instanceof EndMarkedContent // End marked-content sequence.
        || contentObject instanceof EndInlineImage) // Inline image.
        return contentObjects;

      contentObjects.add(contentObject);
    }
    return contentObjects;
  }

  public Operation parseOperation(
    ) throws FileFormatException
  {
    String operator = null;
    final List<PdfDirectObject> operands = new ArrayList<PdfDirectObject>(2);
    // Parsing the operation parts...
    while(true)
    {
      // Did we reach the operator keyword?
      if(tokenType == TokenTypeEnum.Keyword)
      {
        operator = (String)token;
        break;
      }

      operands.add(parsePdfObject()); moveNext();
    }

    return Operation.get(operator,operands);
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
  protected PdfDirectObject parsePdfObject(
    ) throws FileFormatException
  {
  	do
  	{
	    switch(tokenType)
	    {
	      case Integer:
	        return new PdfInteger((Integer)token);
	      case Name:
	        return new PdfName((String)token,true);
	      case Literal:
	        try
	        {
	          return new PdfString(
	            ((String)token).getBytes("ISO-8859-1"),
	            PdfString.SerializationModeEnum.Literal
	            );
	        }
	        catch(Exception e)
	        {throw new RuntimeException(e);}
	      case DictionaryBegin:
	        {
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
	          return dictionary;
	        }
	      case ArrayBegin:
	        {
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
	        }
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
	        throw new RuntimeException("Unknown type: " + tokenType);
	    }
  	} while(moveNext());

  	return null;
  }

  public void seek(
    long position
    )
  {
    while(true)
    {
      if(position < basePosition) //Before current stream.
      {
        if(!movePreviousStream())
          throw new IllegalArgumentException("The 'position' argument is lower than acceptable.");
      }
      else if(position > basePosition + stream.getLength()) // After current stream.
      {
        if(!moveNextStream())
          throw new IllegalArgumentException("The 'position' argument is higher than acceptable.");
      }
      else // At current stream.
      {
        stream.seek(position - basePosition);
        break;
      }
    }
  }

  public void skip(
    long offset
    )
  {
    while(true)
    {
      long position = stream.getPosition() + offset;
      if(position < 0) //Before current stream.
      {
        offset += stream.getPosition();
        if(!movePreviousStream())
          throw new IllegalArgumentException("The 'offset' argument is lower than acceptable.");

        stream.setPosition(stream.getLength());
      }
      else if(position > stream.getLength()) // After current stream.
      {
        offset -= (stream.getLength() - stream.getPosition());
        if(!moveNextStream())
          throw new IllegalArgumentException("The 'offset' argument is higher than acceptable.");
      }
      else // At current stream.
      {
        stream.skip(position);
        break;
      }
    }
  }

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
  // </public>

  // <private>
  private boolean moveNextStream(
    )
  {
    /* NOTE: A content stream may be made up of multiple streams [PDF:1.6:3.6.2]. */
    // Is the content stream just a single stream?
    if(contentStream instanceof PdfStream) // Single stream.
    {
      if(streamIndex == 0)
      {
        streamIndex++;
        basePosition += stream.getLength();
        stream = null;
      }
      if(streamIndex == 1)
        return false;

      streamIndex++;
      basePosition = 0;
      stream = ((PdfStream)contentStream).getBody();
    }
    else // Array of streams.
    {
      PdfArray streams = (PdfArray)contentStream;
      if(streamIndex == (streams.size() -1))
      {
        streamIndex++;
        basePosition += stream.getLength();
        stream = null;
      }
      if(streamIndex == streams.size())
        return false;

      streamIndex++;
      if(streamIndex == 0)
      {basePosition = 0;}
      else
      {basePosition += stream.getLength();}
      stream = ((PdfStream)((PdfReference)streams.get(streamIndex)).getDataObject()).getBody();
    }
    return true;
  }

  private boolean movePreviousStream(
    )
  {
    if(streamIndex == 0)
    {
      streamIndex--;
      stream = null;
    }
    if(streamIndex == -1)
      return false;

    streamIndex--;
    /* NOTE: A content stream may be made up of multiple streams [PDF:1.6:3.6.2]. */
    // Is the content stream just a single stream?
    if(contentStream instanceof PdfStream) // Single stream.
    {
      stream = ((PdfStream)contentStream).getBody();
      basePosition = 0;
    }
    else // Array of streams.
    {
      PdfArray streams = (PdfArray)contentStream;

      stream = ((PdfStream)((PdfReference)streams.get(streamIndex)).getDataObject()).getBody();
      basePosition -= stream.getLength();
    }

    return true;
  }

  private InlineImage parseInlineImage(
    ) throws FileFormatException
  {
    /*
      NOTE: Inline images use a peculiar syntax that's an exception to the usual rule
      that the data in a content stream is interpreted according to the standard PDF syntax
      for objects.
    */
    InlineImageHeader header;
    {
      final List<PdfDirectObject> operands = new ArrayList<PdfDirectObject>(2);
      // Parsing the image entries...
      while(moveNext()
        && tokenType != TokenTypeEnum.Keyword) // Not keyword (i.e. end at image data beginning (ID operator)).
      {operands.add(parsePdfObject());}
      header = new InlineImageHeader(operands);
    }

    InlineImageBody body;
    {
      moveNext();
      Buffer data = new Buffer();
      byte c1 = 0, c2 = 0;
      do
      {
        try
        {
          while(true)
          {
            c1 = stream.readByte();
            c2 = stream.readByte();
            if(c1 == 'E' && c2 == 'I')
              break;

            data.append(c1);
            data.append(c2);
          } break;
        }
        catch(EOFException e)
        {
          /* NOTE: Current stream has finished. */
          // Move to the next stream!
          moveNextStream();
        }
      } while(stream != null);
      body = new InlineImageBody(data);
    }

    return new InlineImage(
      header,
      body
      );
  }

  /**
    @since 0.0.7
  */
  private Path parsePath(
    Operation beginOperation
    ) throws FileFormatException
  {
    /*
      NOTE: Paths do not have an explicit end operation, so we must infer it
      looking for the first non-painting operation.
    */
    final List<ContentObject> operations = new ArrayList<ContentObject>(2);
    {
      operations.add(beginOperation);
      long position = getPosition();
      boolean closeable = false;
      while(moveNext())
      {
        Operation operation = parseOperation();
        // Multiple-operation graphics object closeable?
        if(operation instanceof PaintPath) // Painting operation.
        {closeable = true;}
        else if(closeable) // Past end (first non-painting operation).
        {
          seek(position); // Rolls back to the last path-related operation.
          break;
        }

        operations.add(operation);
        position = getPosition();
      }
    }
    return new Path(operations);
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}