/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents;

import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
  Document pages collection.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class Pages
  extends PdfObjectWrapper<PdfDictionary>
  implements List<Page>
{
  /*
    TODO:IMPL A B-tree algorithm should be implemented to optimize the inner layout
    of the page tree (better insertion/deletion performance). In this case, it would
    be necessary to keep track of the modified tree nodes for incremental update.
  */
  // <class>
  // <dynamic>
  // <constructors>
  Pages(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        {
          PdfName.Type,
          PdfName.Kids,
          PdfName.Count
        },
        new PdfDirectObject[]
        {
          PdfName.Pages,
          new PdfArray(),
          new PdfInteger(0)
        }
        )
      );
  }

  Pages(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container (page tree root node MUST be an indirect object [PDF:1.6:3.6.1]).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Pages clone(
    Document context
    )
  {throw new NotImplementedException();}

  // <List>
  public void add(
    int index,
    Page page
    )
  {commonAddAll(index,Arrays.asList(page));}

  public boolean addAll(
    int index,
    Collection<? extends Page> pages
    )
  {return commonAddAll(index,pages);}

  public Page get(
    int index
    )
  {
    /*
      NOTE: As stated in [PDF:1.6:3.6.2], to retrieve pages is a matter of diving
      inside a B-tree. To keep it as efficient as possible, this implementation
      does NOT adopt recursion to deepen its search, opting for an iterative strategy
      instead.
    */
    int pageOffset = 0;
    PdfDictionary parent = getBaseDataObject();
    PdfArray kids = (PdfArray)File.resolve(parent.get(PdfName.Kids));
    for(
      int i = 0;
      i < kids.size();
      i++
      )
    {
      PdfReference kidReference = (PdfReference)kids.get(i);
      PdfDictionary kid = (PdfDictionary)File.resolve(kidReference);
      // Is current kid a page object?
      if(kid.get(PdfName.Type).equals(PdfName.Page)) // Page object.
      {
        // Did we reach the searched position?
        if(pageOffset == index) // Vertical scan (we finished).
        {
          // We got it!
          return new Page(kidReference);
        }
        else // Horizontal scan (go past).
        {
          // Cumulate current page object count!
          pageOffset++;
        }
      }
      else // Page tree node.
      {
        // Does the current subtree contain the searched page?
        if(((PdfInteger)kid.get(PdfName.Count)).getRawValue() + pageOffset > index) // Vertical scan (deepen the search).
        {
          // Go down one level!
          parent = kid;
          kids = (PdfArray)File.resolve(parent.get(PdfName.Kids));
          i = -1;
        }
        else // Horizontal scan (go past).
        {
          // Cumulate current subtree count!
          pageOffset += ((PdfInteger)kid.get(PdfName.Count)).getRawValue();
        }
      }
    }

    return null;
  }

  public int indexOf(
    Object page
    )
  {return ((Page)page).getIndex();}

  public int lastIndexOf(
    Object page
    )
  {
    /*
      NOTE: Each page object should NOT appear more than once inside the same document.
    */
    return indexOf(page);
  }

  public ListIterator<Page> listIterator(
    )
  {throw new NotImplementedException();}

  public ListIterator<Page> listIterator(
    int index
    )
  {throw new NotImplementedException();}

  public Page remove(
    int index
    )
  {
    Page page = get(index);
    remove(page);

    return page;
  }

  public Page set(
    int index,
    Page page
    )
  {
    Page old = remove(index);
    add(index,page);

    return old;
  }

  public List<Page> subList(
    int fromIndex,
    int toIndex
    )
  {
  /*
  TODO:IMPL this implementation is incoherent with the subList contract --> move to another location!
  */
    ArrayList<Page> pages = new ArrayList<Page>(toIndex - fromIndex);
    int i = fromIndex;
    while(i < toIndex)
    {pages.add(get(i++));}

    return pages;
  }

  // <Collection>
  public boolean add(
    Page page
    )
  {return commonAddAll(-1,Arrays.asList(page));}

  public boolean addAll(
    Collection<? extends Page> pages
    )
  {return commonAddAll(-1,pages);}

  public void clear(
    )
  {throw new NotImplementedException();}

  public boolean contains(
    Object page
    )
  {throw new NotImplementedException();}

  public boolean containsAll(
    Collection<?> pages
    )
  {throw new NotImplementedException();}

  public boolean equals(
    Object object
    )
  {throw new NotImplementedException();}

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {throw new NotImplementedException();}

  public boolean remove(
    Object page
    )
  {
    Page pageObj = (Page)page;
    PdfDictionary pageData = pageObj.getBaseDataObject();
    // Get the parent tree node!
    PdfDirectObject parent = pageData.get(PdfName.Parent);
    PdfDictionary parentData = (PdfDictionary)File.resolve(parent);
    // Get the parent's page collection!
    PdfDirectObject kids = parentData.get(PdfName.Kids);
    PdfArray kidsData = (PdfArray)File.resolve(kids);
    // Remove the page!
    kidsData.remove(pageObj.getBaseObject());
    boolean updateParent = !File.update(kids); // Try to update the page collection.
    // Unbind the page from its parent!
    pageData.put(PdfName.Parent,null);
    pageObj.update();
    // Decrementing the pages counters...
    do
    {
      // Get the page collection counter!
      PdfDirectObject count = parentData.get(PdfName.Count);
      IPdfNumber countData = (IPdfNumber)File.resolve(count);
      // Decrement the counter at the current level!
      countData.translateNumberValue(-1);
      updateParent |= !File.update(count); // Try to update the counter.
      // Is the parent tree node to be updated?
      /*
        NOTE: It avoids to update the parent tree node if its modified fields are all
        indirect objects which perform independent updates.
      */
      if(updateParent)
      {
        File.update(parent);
        updateParent = false; // Reset.
      }

      // Iterate upward!
      parent = parentData.get(PdfName.Parent);
      parentData = (PdfDictionary)File.resolve(parent);
    } while(parent != null);

    return true;
  }

  public boolean removeAll(
    Collection<?> pages
    )
  {
    /*
      NOTE: The interface contract doesn't prescribe any relation among the removing-collection's
      items, so we cannot adopt the optimized approach of the add*(...) methods family,
      where adding-collection's items are explicitly ordered.
    */
    boolean changed = false;
    for(Object page : pages)
    {changed |= remove(page);}

    return changed;
  }

  public boolean retainAll(
    Collection<?> pages
    )
  {throw new NotImplementedException();}

  public int size(
    )
  {return ((PdfInteger)getBaseDataObject().get(PdfName.Count)).getRawValue();}

  public Page[] toArray(
    )
  {throw new NotImplementedException();}

  public <T> T[] toArray(
    T[] pages
    )
  {throw new NotImplementedException();}

  // <Iterable>
  public Iterator<Page> iterator(
    )
  {
    return new Iterator<Page>()
    {
      // <class>
      // <dynamic>
      // <fields>
      /**
        Index of the next item.
      */
      private int index = 0;
      /**
        Collection size.
      */
      private int size = size();

      /**
        Current level index.
      */
      private int levelIndex = 0;
      /**
        Stacked level indexes.
      */
      private Stack<Integer> levelIndexes = new Stack<Integer>();
      /**
        Current parent tree node.
      */
      private PdfDictionary parent = getBaseDataObject();
      /**
        Current child tree nodes.
      */
      private PdfArray kids = (PdfArray)File.resolve(parent.get(PdfName.Kids));
      // </fields>

      // <interface>
      // <public>
      // <Iterator>
      public boolean hasNext(
        )
      {return (index < size);}

      public Page next(
        )
      {
        if(!hasNext())
          throw new NoSuchElementException();

        return getNext();
      }

      public void remove(
        )
      {throw new UnsupportedOperationException();}
      // </Iterator>
      // </public>

      // <private>
      private Page getNext(
        )
      {
        /*
          NOTE: As stated in [PDF:1.6:3.6.2], to retrieve pages is a matter of diving
          inside a B-tree.
          This is a special adaptation of the get() algorithm necessary to keep
          a low overhead throughout the page tree scan (using the get() method
          would have implied a nonlinear computational cost).
        */
        /*
          NOTE: Algorithm:
          1. [Vertical, down] We have to go downward the page tree till we reach
          a page (leaf node).
          2. [Horizontal] Then we iterate across the page collection it belongs to,
          repeating step 1 whenever we find a subtree.
          3. [Vertical, up] When leaf-nodes scan is complete, we go upward solving
          parent nodes, repeating step 2.
        */
        while(true)
        {
          // Did we complete current page-tree-branch level?
          if(kids.size() == levelIndex) // Page subtree complete.
          {
            // 3. Go upward one level.
            // Restore node index at the current level!
            levelIndex = levelIndexes.pop() + 1; // Next node (partially scanned level).
            // Move upward!
            parent = (PdfDictionary)File.resolve(parent.get(PdfName.Parent));
            kids = (PdfArray)File.resolve(parent.get(PdfName.Kids));
          }
          else // Page subtree incomplete.
          {
            PdfReference kidReference = (PdfReference)kids.get(levelIndex);
            PdfDictionary kid = (PdfDictionary)File.resolve(kidReference);
            // Is current kid a page object?
            if(kid.get(PdfName.Type).equals(PdfName.Page)) // Page object.
            {
              // 2. Page found.
              index++; // Absolute page index.
              levelIndex++; // Current level node index.

              return new Page(kidReference);
            }
            else // Page tree node.
            {
              // 1. Go downward one level.
              // Save node index at the current level!
              levelIndexes.push(levelIndex);
              // Move downward!
              parent = kid;
              kids = (PdfArray)File.resolve(parent.get(PdfName.Kids));
              levelIndex = 0; // First node (new level).
            }
          }
        }
      }
      // </private>
      // </interface>
      // </dynamic>
      // </class>
    };
  }
  // </Iterable>
  // </Collection>
  // </List>
  // </public>

  // <private>
  /**
    Add a collection of pages at the specified position.
    @param index Addition position. To append, use value -1.
    @param pages Collection of pages to add.
  */
  private boolean commonAddAll(
    int index,
    Collection<? extends Page> pages
    )
  {
    PdfDirectObject parent;
    PdfDictionary parentData;
    PdfDirectObject kids;
    PdfArray kidsData;
    int offset;
    // Append operation?
    if(index == -1) // Append operation.
    {
      // Get the parent tree node!
      parent = getBaseObject();
      parentData = getBaseDataObject();
      // Get the parent's page collection!
      kids = parentData.get(PdfName.Kids);
      kidsData = (PdfArray)File.resolve(kids);
      offset = 0; // Not used.
    }
    else // Insert operation.
    {
      // Get the page currently at the specified position!
      Page pivotPage = get(index);
      // Get the parent tree node!
      parent = pivotPage.getBaseDataObject().get(PdfName.Parent);
      parentData = (PdfDictionary)File.resolve(parent);
      // Get the parent's page collection!
      kids = parentData.get(PdfName.Kids);
      kidsData = (PdfArray)File.resolve(kids);
      // Get the insertion's relative position within the parent's page collection!
      offset = kidsData.indexOf(pivotPage.getBaseObject());
    }

    // Adding the pages...
    for(Page page : pages)
    {
      // Append?
      if(index == -1) // Append.
      {
        // Append the page to the collection!
        kidsData.add(page.getBaseObject());
      }
      else // Insert.
      {
        // Insert the page into the collection!
        kidsData.add(
          offset++,
          page.getBaseObject()
          );
      }
      // Bind the page to the collection!
      page.getBaseDataObject().put(PdfName.Parent,parent);
      page.update();
    }
    boolean updateParent = !File.update(kids); // Try to update the page collection.

    // Incrementing the pages counters...
    do
    {
      // Get the page collection counter!
      PdfDirectObject count = parentData.get(PdfName.Count);
      IPdfNumber countData = (IPdfNumber)File.resolve(count);
      // Increment the counter at the current level!
      countData.translateNumberValue(pages.size());
      updateParent |= !File.update(count); // Try to update the page counter.
      // Is the parent tree node to be updated?
      /*
        NOTE: It avoids to update the parent tree node if its modified fields are all
        indirect objects which perform independent updates.
      */
      if(updateParent)
      {
        File.update(parent);
        updateParent = false; // Reset.
      }

      // Iterate upward!
      parent = parentData.get(PdfName.Parent);
      parentData = (PdfDictionary)File.resolve(parent);
    } while(parent != null);

    return true;
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}