/*
  Copyright 2009-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)

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

package it.stefanochizzolini.clown.documents.contents.fonts;

import it.stefanochizzolini.clown.bytes.Buffer;
import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.tokens.FileFormatException;
import it.stefanochizzolini.clown.tokens.TokenTypeEnum;
import it.stefanochizzolini.clown.util.ByteArray;
import it.stefanochizzolini.clown.util.ConvertUtils;
import it.stefanochizzolini.clown.util.math.OperationUtils;

import java.io.EOFException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

/**
  CMap parser [PDF:1.6:5.6.4;CMAP].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.8
*/
final class CMapParser
{
/*
TODO:IMPL this parser evaluates a subset of the lexical domain of the token parser (clown.tokens.Parser): it should be better to derive both parsers from a common parsing engine in order to avoid unwieldy duplications.
*/
  // <class>
  // <static>
  // <fields>
  private static final String BeginBaseFontCharOperator = "beginbfchar";
  private static final String BeginBaseFontRangeOperator = "beginbfrange";
  private static final String BeginCIDCharOperator = "begincidchar";
  private static final String BeginCIDRangeOperator = "begincidrange";
   // </fields>

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
  {return c == '(' || c == ')' || c == '<' || c == '>' || c == '[' || c == ']' || c == '/' || c == '%';}

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
  private IInputStream stream;
  private Object token;
  private TokenTypeEnum tokenType;
  // </fields>

  // <constructors>
  public CMapParser(
    java.io.BufferedReader stream
    )
  {this(new Buffer(stream));}

  public CMapParser(
    InputStream stream
    )
  {this(new Buffer(stream));}

  public CMapParser(
    IInputStream stream
    )
  {this.stream = stream;}
  // </constructors>

  // <interface>
  // <public>
  public long getLength(
    )
  {return stream.getLength();}

  public long getPosition(
    )
  {return stream.getPosition();}

  /**
    Gets the stream to be parsed.
  */
  public IInputStream getStream(
    )
  {return stream;}

  /**
    Gets the currently-parsed token.
  */
  public Object getToken(
    )
  {return token;}

  /**
    Gets the currently-parsed token type.
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
      TODO: It'd be interesting to evaluate an alternative regular-expression-based
      implementation...
    */
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
      case '%': // Comment.
        tokenType = TokenTypeEnum.Comment;
        // Skipping comment content...
        try
        {
          do
          {c = stream.readUnsignedByte();}
          while(!isEOL(c));
        }
        catch(EOFException e)
        {/* Let it go. */}
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
        {/* Let it go. */}
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
        case Name:
          token = buffer.toString();
          break;
        case Literal:
          token = buffer.toString();
          break;
        case Hex:
          token = ConvertUtils.hexToByteArray(buffer.toString());
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
    Parses the character-code-to-unicode mapping [PDF:1.6:5.9.1].
  */
  public Map<ByteArray,Integer> parse(
    )
  {
    stream.setPosition(0);
    Hashtable<ByteArray,Integer> codes = new Hashtable<ByteArray,Integer>();
    {
      int itemCount = 0;
      try
      {
        while(moveNext())
        {
          switch(tokenType)
          {
            case Keyword:
            {
              String operator = (String)token;
              if(operator.equals(BeginBaseFontCharOperator)
                || operator.equals(BeginCIDCharOperator))
              {
                /*
                  NOTE: The first element on each line is the input code of the template font;
                  the second element is the code or name of the character.
                */
                for(
                  int itemIndex = 0;
                  itemIndex < itemCount;
                  itemIndex++
                  )
                {
                  // 1. Input code.
                  moveNext();
                  ByteArray inputCode = new ByteArray((byte[])token);
                  // 2. Character...
                  moveNext();
                  switch(tokenType)
                  {
                    case Hex: // ...code (hex).
                      codes.put(inputCode,ConvertUtils.byteArrayToInt((byte[])token));
                      break;
                    case Integer: // ...code (plain).
                      codes.put(inputCode,(Integer)token);
                      break;
                    case Name: // ...name.
                      codes.put(inputCode,GlyphMapping.nameToCode((String)token));
                      break;
                    default:
                      throw new RuntimeException(
                        operator + " section syntax error: hex string, integer or name expected instead of " + tokenType
                        );
                  }
                }
              }
              else if(operator.equals(BeginBaseFontRangeOperator)
                || operator.equals(BeginCIDRangeOperator))
              {
                /*
                  NOTE: The first and second elements in each line are the beginning and
                  ending valid input codes for the template font; the third element is
                  the beginning character code for the range.
                */
                for(
                  int itemIndex = 0;
                  itemIndex < itemCount;
                  itemIndex++
                  )
                {
                  // 1. Beginning input code.
                  moveNext();
                  byte[] beginInputCode = (byte[])token;
                  // 2. Ending input code.
                  moveNext();
                  byte[] endInputCode = (byte[])token;
                  // 3. Character codes.
                  moveNext();
                  switch(tokenType)
                  {
                    case Hex:
                    case Integer:
                    {
                      byte[] inputCode = beginInputCode;
                      int charCode;
                      switch(tokenType)
                      {
                        case Hex:
                          charCode = ConvertUtils.byteArrayToInt((byte[])token);
                          break;
                        case Integer:
                          charCode = (Integer)token;
                          break;
                        default:
                          throw new RuntimeException(
                            operator + " section syntax error: hex string or integer expected instead of " + tokenType
                            );
                      }
                      int endCharCode = charCode + (ConvertUtils.byteArrayToInt(endInputCode) - ConvertUtils.byteArrayToInt(beginInputCode));
                      while(true)
                      {
                        codes.put(new ByteArray(inputCode),charCode);
                        if(charCode == endCharCode)
                          break;

                        OperationUtils.increment(inputCode);
                        charCode++;
                      }
                      break;
                    }
                    case ArrayBegin:
                    {
                      byte[] inputCode = beginInputCode;
                      while(moveNext()
                        && tokenType != TokenTypeEnum.ArrayEnd)
                      {
                        codes.put(new ByteArray(inputCode),GlyphMapping.nameToCode((String)token));
                        OperationUtils.increment(inputCode);
                      }
                      break;
                    }
                    default:
                      throw new RuntimeException(
                        operator + " section syntax error: hex string, integer or name array expected instead of " + tokenType
                        );
                  }
                }
              }
              break;
            }
            case Integer:
            {
              itemCount = (Integer)token;
              break;
            }
          }
        }
      }
      catch(FileFormatException fileFormatException)
      {throw new RuntimeException(fileFormatException);}
    }
    return codes;
  }

  public void seek(
    long position
    )
  {
    if(position < 0)
      throw new IllegalArgumentException("The 'position' argument is lower than acceptable.");
    if(position > stream.getLength())
      throw new IllegalArgumentException("The 'position' argument is higher than acceptable.");

    stream.seek(position);
  }

  public void skip(
    long offset
    )
  {
    long position = stream.getPosition() + offset;
    if(position < 0)
      throw new IllegalArgumentException("The 'offset' argument is lower than acceptable.");
    if(position > stream.getLength())
      throw new IllegalArgumentException("The 'offset' argument is higher than acceptable.");

    stream.skip(position);
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
  // </interface>
  // </dynamic>
  // </class>
}