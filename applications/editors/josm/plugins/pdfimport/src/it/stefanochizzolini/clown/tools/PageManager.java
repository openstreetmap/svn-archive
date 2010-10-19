/*
  Copyright 2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.tools;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.documents.Pages;
import it.stefanochizzolini.clown.files.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
  Tool for page management.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
  @since 0.0.6
*/
public class PageManager
{
  // <class>
  // <dynamic>
  // <fields>
  private Document document;
  private Pages pages;
  // </fields>

  // <constructors>
  public PageManager(
    )
  {this(null);}

  public PageManager(
    Document document
    )
  {setDocument(document);}
  // </constructors>

  // <interface>
  // <public>
  /**
    Appends a document to the end of the document.

    @param document The document to add.
  */
  public void add(
    Document document
    )
  {add((Collection<Page>)document.getPages());}

  /**
    Appends a collection of pages to the end of the document.

    @param pages The pages to add.
  */
  @SuppressWarnings("unchecked")
  public void add(
    Collection<Page> pages
    )
  {
    /*
      NOTE: To be added to an alien document,
      pages MUST be firstly contextualized into it,
      then added to the target pages collection.
    */
    this.pages.addAll(
      (Collection<Page>)this.document.contextualize(pages)
      );
    this.pages.update(); // NOTE: Update is fundamental to override original page collection.
  }

  /**
    Inserts a document at the specified position in the document.

    @param index Position at which the document is to be inserted.
    @param document The document to be inserted.
  */
  public void add(
    int index,
    Document document
    )
  {add(index,(Collection<Page>)document.getPages());}

  /**
    Inserts a collection of pages at the specified position in the document.

    @param index Position at which the pages are to be inserted.
    @param pages The pages to be inserted.
  */
  @SuppressWarnings("unchecked")
  public void add(
    int index,
    Collection<Page> pages
    )
  {
    /*
      NOTE: To be added to an alien document,
      pages MUST be firstly contextualized into it,
      then added to the target pages collection.
    */
    // Add the source pages to the document (contextualize)!
    /* NOTE: Deep addition. */
    Collection<Page> addingPages = (Collection<Page>)document.contextualize(pages);
    // Add the source pages to the pages collection!
    /* NOTE: Shallow addition. */
    if(index >= this.pages.size())
    {this.pages.addAll(addingPages);}
    else
    {this.pages.addAll(index,addingPages);}
    this.pages.update(); // NOTE: Update is fundamental to override original page collection.
  }

  /**
    Extracts a page range from the document.

    @param beginIndex The beginning index, inclusive.
    @param endIndex The ending index, exclusive.
    @return Extracted page range.
  */
  @SuppressWarnings("unchecked")
  public Document extract(
    int beginIndex,
    int endIndex
    )
  {
    Document extractedDocument = new File().getDocument();

    // Add the pages to the target file!
    /*
      NOTE: To be added to an alien document,
      pages MUST be contextualized within it first,
      then added to the target pages collection.
    */
    extractedDocument.getPages().addAll(
      (Collection<Page>)extractedDocument.contextualize(
        pages.subList(beginIndex,endIndex)
        )
      );

    return extractedDocument;
  }

  /**
    Moves a page range to a target position within the document.

    @param beginIndex The beginning index, inclusive.
    @param endIndex The ending index, exclusive.
    @param targetIndex The target index.
  */
  public void move(
    int beginIndex,
    int endIndex,
    int targetIndex
    )
  {
    int pageCount = pages.size();

    List<Page> movingPages = pages.subList(beginIndex,endIndex);

    // Temporarily remove the pages from the pages collection!
    /* NOTE: Shallow removal. */
    pages.removeAll(movingPages);

    // Adjust indexes!
    pageCount -= movingPages.size();
    if(targetIndex > beginIndex)
    {targetIndex -= movingPages.size(); /* Adjusts the target position due to shifting for temporary page removal. */}

    // Reinsert the pages at the target position!
    /* NOTE: Shallow addition. */
    if(targetIndex >= pageCount)
    {pages.addAll(movingPages);}
    else
    {pages.addAll(targetIndex,movingPages);}
    pages.update(); // NOTE: Update is fundamental to override original page collection.
  }

  /**
    Gets the document being manipulated.
  */
  public Document getDocument(
    )
  {return document;}

  /**
    Removes a page range from the document.

    @param beginIndex The beginning index, inclusive.
    @param endIndex The ending index, exclusive.
  */
  public void remove(
    int beginIndex,
    int endIndex
    )
  {
    List<Page> removingPages = pages.subList(beginIndex,endIndex);

    // Remove the pages from the pages collection!
    /* NOTE: Shallow removal. */
    pages.removeAll(removingPages); pages.update();

    // Remove the pages from the document (decontextualize)!
    /* NOTE: Deep removal. */
    document.decontextualize(removingPages);
  }

  /**
    Sets the document to manipulate.
  */
  public void setDocument(
    Document value
    )
  {
    document = value;
    pages = document.getPages();
  }

  /**
    Splits the document into multiple single-paged documents.

    @return A list of single-paged documents.
  */
  public List<Document> split(
    )
  {
    List<Document> documents = new ArrayList<Document>();
    for(Page page : pages)
    {
      Document pageDocument = new File().getDocument();
      pageDocument.getPages().add(
        (Page)page.clone(pageDocument)
        );
      documents.add(pageDocument);
    }

    return documents;
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}