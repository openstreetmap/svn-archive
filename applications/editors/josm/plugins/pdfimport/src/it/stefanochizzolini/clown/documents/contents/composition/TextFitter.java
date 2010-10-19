/*
  Copyright 2007-2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents.composition;

import it.stefanochizzolini.clown.documents.contents.fonts.Font;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Text fitter.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.3
*/
final class TextFitter
{
  // <class>
  // <dynamic>
  // <fields>
  private Font font;
  private double fontSize;
  private boolean hyphenation;
  private String text;
  private double width;

  private int beginIndex = 0;
  private int endIndex = -1;
  private String fittedText;
  private double fittedWidth;
  // </fields>

  // <constructors>
  TextFitter(
    String text,
    double width,
    Font font,
    double fontSize,
    boolean hyphenation
    )
  {
    this.text = text;
    this.width = width;
    this.font = font;
    this.fontSize = fontSize;
    this.hyphenation = hyphenation;
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Fits the text inside the specified width.
    @return Whether the operation was successful.
  */
  public boolean fit(
    )
  {
    return fit(
      endIndex + 1,
      width
      );
    }

  /**
    Fits the text inside the specified width.
    @param index Beginning index, inclusive.
    @param width Available width.
    @return Whether the operation was successful.
    @version 0.0.4
  */
  public boolean fit(
    int index,
    double width
    )
  {
    beginIndex = index;
    this.width = width;

    fittedText = null;
    fittedWidth = 0;

    String hyphen = "";

fitting:
    // Fitting the text within the available width...
    {
      Pattern pattern = Pattern.compile("(\\s*)(\\S*)");
      Matcher matcher = pattern.matcher(text);
      matcher.region(beginIndex,text.length());
      while(matcher.find())
      {
        // Scanning for the presence of a line break...
        /*
          NOTE: This text fitting algorithm returns everytime it finds a line break character,
          as it's intended to evaluate the width of just a single line of text at a time.
        */
        for(
          int spaceIndex = matcher.start(1),
            spaceEnd = matcher.end(1);
          spaceIndex < spaceEnd;
          spaceIndex++
          )
        {
          switch(text.charAt(spaceIndex))
          {
            case '\n':
            case '\r':
              index = spaceIndex;
              break fitting;
          }
        }

        // Get the limit of the current word!
        int wordEndIndex = matcher.end(0);
        // Add the current word!
        double wordWidth = font.getKernedWidth(
          matcher.group(0),
          fontSize
          ); // Current word's width.
        fittedWidth += wordWidth;
        // Does the fitted text's width exceed the available width?
        if(fittedWidth > width)
        {
          // Remove the current (unfitting) word!
          fittedWidth -= wordWidth;
          wordEndIndex = index;
          if(wordEndIndex == 0 // Fitted text is empty.
            || !hyphenation) // No hyphenation.
            break fitting;

          /*
            NOTE: We need to hyphenate the current (unfitting) word.
          */
          /*
            TODO: This hyphenation algorithm is quite primitive (to improve!).
          */
hyphenating:
          while(true)
          {
            // Add the current character!
            char textChar = text.charAt(wordEndIndex); // Current character.
            wordWidth = (font.getKerning(text.charAt(wordEndIndex - 1),textChar) + font.getWidth(textChar)) * Font.getScalingFactor(fontSize); // Current character's width.
            wordEndIndex++;
            fittedWidth += wordWidth;
            // Does fitted text's width exceed the available width?
            if(fittedWidth > width)
            {
              // Remove the current character!
              fittedWidth -= wordWidth;
              wordEndIndex--;
              // Is hyphenation to be applied?
              if(wordEndIndex > index + 4) // Long-enough word chunk.
              {
                // Make room for the hyphen character!
                wordEndIndex--;
                index = wordEndIndex;
                textChar = text.charAt(wordEndIndex);
                fittedWidth -= (font.getKerning(text.charAt(wordEndIndex - 1),textChar) + font.getWidth(textChar)) * Font.getScalingFactor(fontSize);

                // Add the hyphen character!
                textChar = '-'; // hyphen.
                fittedWidth += (font.getKerning(text.charAt(wordEndIndex - 1),textChar) + font.getWidth(textChar)) * Font.getScalingFactor(fontSize);

                hyphen = String.valueOf(textChar);
              }
              else // No hyphenation.
              {
                // Removing the current word chunk...
                while(wordEndIndex > index)
                {
                  wordEndIndex--;
                  textChar = text.charAt(wordEndIndex);
                  fittedWidth -= (font.getKerning(text.charAt(wordEndIndex - 1),textChar) + font.getWidth(textChar)) * Font.getScalingFactor(fontSize);
                }
              }
              break hyphenating;
            }
          }
          break fitting;
        }
        index = wordEndIndex;
      }
    }
    fittedText = text.substring(beginIndex,index) + hyphen;
    endIndex = index;

    return (fittedWidth > 0);
  }

  /**
    Gets the begin index of the fitted text inside the available text.
  */
  public int getBeginIndex(
    )
  {return beginIndex;}

  /**
    Gets the end index of the fitted text inside the available text.
  */
  public int getEndIndex(
    )
  {return endIndex;}

  /**
    Gets the fitted text.
  */
  public String getFittedText(
    )
  {return fittedText;}

  /**
    Gets the fitted text's width.
  */
  public double getFittedWidth(
    )
  {return fittedWidth;}

  /**
    Gets the font used to fit the text.
  */
  public Font getFont(
    )
  {return font;}

  /**
    Gets the size of the font used to fit the text.
  */
  public double getFontSize(
    )
  {return fontSize;}

  /**
    Gets the available text.
  */
  public String getText(
    )
  {return text;}

  /**
    Gets the available width.
  */
  public double getWidth(
    )
  {return width;}

  /**
    Gets whether the hyphenation algorithm has to be applied.
  */
  public boolean isHyphenation(
    )
  {return hyphenation;}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}