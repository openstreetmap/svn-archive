/*
  Copyright 2007-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.objects;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
  Name tree [PDF:1.6:3.8.5].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.4
*/
public abstract class NameTree<T extends PdfObjectWrapper<?>>
  extends PdfObjectWrapper<PdfDictionary>
  implements Map<PdfString, T>
{
  // <class>
  // <types>
  /**
    Node children.
  */
  private static final class Children
  {
    /** Children's collection */
    public final PdfArray items;
    /** Node's children order (that is maximum number of items allowed). */
    public final int order;
    /** Node's children type. */
    public final PdfName typeName;

    private Children(
      PdfArray items,
      PdfName typeName,
      int order
      )
    {
      this.items = items;
      this.typeName = typeName;
      this.order = order;
    }
  }

  private final class Entry
    implements Map.Entry<PdfString, T>,
      Comparable<Entry>
  {
    // <class>
    // <dynamic>
    // <fields>
    private final PdfString key;
    private final T value;
    // </fields>

    // <constructors>
    private Entry(
      PdfString key,
      T value
      )
    {
      this.key = key;
      this.value = value;
    }
    // </constructors>

    // <interface>
    // <public>
    // <Comparable>
    public int compareTo(
      Entry obj
      )
    {return key.compareTo(obj.getKey());}
    // </Comparable>

    // <Map.Entry>
    public PdfString getKey(
      )
    {return key;}

    public T getValue(
      )
    {return value;}

    public T setValue(
      T value
      )
    {throw new UnsupportedOperationException();}
    // </Map.Entry>
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }

  private interface IFiller<TCollection extends Collection<?>>
  {
    void add(
      PdfArray names,
      int offset,
      PdfIndirectObject container
      );
    TCollection getCollection(
      );
  }
  // </types>

  // <static>
  // <fields>
  /**
    Minimum number of children for each node.
  */
  private static final int NodeMinSize = 5;
  /**
    Maximum number of children for each node.
  */
  private static final int TreeOrder = NodeMinSize * 2;

  /**
    Minimum number of name/value items for each node.
  */
  private static final int NameNodeMinSize = NodeMinSize * 2; // NOTE: Name collections are arrays of name/value pairs.
  /**
    Maximum number of name/value items for each node.
  */
  private static final int NameOrder = NameNodeMinSize * 2;

  private static final int[] ChildrenOrders = new int[]{NameOrder, TreeOrder};
  private static final PdfName[] ChildrenTypeNames = new PdfName[]{PdfName.Names, PdfName.Kids};
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public NameTree(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        { PdfName.Names },
        new PdfDirectObject[]
        { new PdfArray() }
        )
      ); // NOTE: Initial root is by-definition a leaf node.
  }

  public NameTree(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    super(
      baseObject,
      container
      );
  }
  // </constructors>

  // <interface>
  // <public>
  public NameTree<T> clone(
    Document context
    )
  {throw new NotImplementedException();}

  // <Map>
  public void clear(
    )
  {clear(getBaseDataObject());}

  public boolean containsKey(
    Object key
    )
  {
    /*
      NOTE: Here we assume that any named entry has a non-null value.
    */
    return get(key) != null;
  }

  public boolean containsValue(
    Object value
    )
  {throw new NotImplementedException();}

  public Set<Map.Entry<PdfString,T>> entrySet(
    )
  {
    IFiller<Set<Map.Entry<PdfString,T>>> filler = new IFiller<Set<Map.Entry<PdfString,T>>>()
      {
        private Set<Map.Entry<PdfString,T>> entrySet = new HashSet<Map.Entry<PdfString,T>>();
        public void add(
          PdfArray names,
          int offset,
          PdfIndirectObject container
          )
        {
          PdfString key = (PdfString)names.get(offset);
          T value = wrap(
            names.get(offset + 1),
            container,
            key
            );
          entrySet.add(
            new Entry(key,value)
            );
        }
        public Set<Map.Entry<PdfString,T>> getCollection(
          )
        {return entrySet;}
      };
    fill(
      filler,
      (PdfReference)getBaseObject()
      );

    return filler.getCollection();
  }

  public boolean equals(
    Object object
    )
  {throw new NotImplementedException();}

  public T get(
    Object key
    )
  {
    PdfString keyString = (PdfString)key;
    PdfDirectObject containerObject = getBaseObject();
    PdfDictionary parent = getBaseDataObject();
    while(true)
    {
      PdfDirectObject namesObject = parent.get(PdfName.Names);
      if(namesObject == null) // Intermediate node.
      {
        PdfArray kids = (PdfArray)parent.resolve(PdfName.Kids);
        int low = 0, high = kids.size() - 1;
        while(true)
        {
          if(low > high)
            return null;

          int mid = (low + high) / 2;
          PdfDirectObject kidObject = kids.get(mid);
          PdfDictionary kid = (PdfDictionary)File.resolve(kidObject);
          PdfArray limits = (PdfArray)kid.resolve(PdfName.Limits);
          // Compare to the lower limit!
          int comparison = keyString.compareTo(
            (PdfString)limits.get(0)
            );
          if(comparison < 0)
          {high = mid - 1;}
          else
          {
            // Compare to the upper limit!
            comparison = keyString.compareTo(
              (PdfString)limits.get(1)
              );
            if(comparison > 0)
            {low = mid + 1;}
            else
            {
              // Go down one level!
              containerObject = kidObject; // NOTE: Node children MUST be indirectly referenced.
              parent = kid;
              break;
            }
          }
        }
      }
      else // Leaf node.
      {
        if(namesObject instanceof PdfReference)
        {containerObject = namesObject;}

        PdfArray names = (PdfArray)File.resolve(namesObject);
        int low = 0, high = names.size();
        while(true)
        {
          if(low > high)
            return null;

          int mid = (mid = ((low + high) / 2)) - (mid % 2);
          int comparison = keyString.compareTo(
            (PdfString)names.get(mid)
            );
          if(comparison < 0)
          {high = mid - 2;}
          else if(comparison > 0)
          {low = mid + 2;}
          else
          {
            // We got it!
            return wrap(
              names.get(mid + 1),
              ((PdfReference)containerObject).getIndirectObject(),
              (PdfString)names.get(mid)
              );
          }
        }
      }
    }
  }

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {
    PdfDictionary rootNode = getBaseDataObject();
    PdfArray children = (PdfArray)rootNode.resolve(PdfName.Names);
    if(children == null) // Intermediate node.
    {children = (PdfArray)rootNode.resolve(PdfName.Kids);}

    return children == null
      || children.size() == 0;
  }

  public Set<PdfString> keySet(
    )
  {
    IFiller<Set<PdfString>> filler = new IFiller<Set<PdfString>>()
      {
        private Set<PdfString> keySet = new HashSet<PdfString>();
        public void add(
          PdfArray names,
          int offset,
          PdfIndirectObject container
          )
        {
          keySet.add(
            (PdfString)names.get(offset)
            );
        }
        public Set<PdfString> getCollection(
          )
        {return keySet;}
      };
    fill(
      filler,
      (PdfReference)getBaseObject()
      );

    return filler.getCollection();
  }

  public T put(
    PdfString key,
    T value
    )
  {
    // Get the root node!
    PdfReference rootReference = (PdfReference)getBaseObject(); // NOTE: Nodes MUST be indirect objects.
    PdfDictionary root = (PdfDictionary)rootReference.getDataObject();

    // Ensuring the root node isn't full...
    {
      Children rootChildren = getChildren(root);
      if(rootChildren.items.size() >= rootChildren.order) // Root node full.
      {
        // Insert the old root under the new one!
        PdfDataObject oldRootDataObject = rootReference.getDataObject();
        rootReference.setDataObject(
          root = new PdfDictionary(
            new PdfName[]
            { PdfName.Kids },
            new PdfDirectObject[]
            {
              new PdfArray(
                new PdfDirectObject[]{getFile().register(oldRootDataObject)}
                )
            }
            )
          );
        // Split the old root!
        splitFullNode(
          (PdfArray)root.get(PdfName.Kids),
          0, // Old root's position within new root's kids.
          rootChildren.typeName
          );
      }
    }

    // Set the entry under the root node!
    return put(
      key,
      value,
      rootReference
      );
  }

  public void putAll(
    Map<? extends PdfString,? extends T> entries
    )
  {
    for(Map.Entry<? extends PdfString,? extends T> entry : entries.entrySet())
    {put(entry.getKey(),entry.getValue());}
  }

  public T remove(
    Object key
    )
  {throw new NotImplementedException();}

  public int size(
    )
  {return getSize(getBaseDataObject());}

  public Collection<T> values(
    )
  {
    IFiller<Collection<T>> filler = new IFiller<Collection<T>>()
      {
        private Collection<T> values = new ArrayList<T>();
        public void add(
          PdfArray names,
          int offset,
          PdfIndirectObject container
          )
        {
          values.add(
            wrap(
              names.get(offset + 1),
              container,
              (PdfString)names.get(offset)
              )
            );
        }
        public Collection<T> getCollection(
          )
        {return values;}
      };
    fill(
      filler,
      (PdfReference)getBaseObject()
      );

    return filler.getCollection();
  }
  // </Map>
  // </public>

  // <protected>
  /**
    Wraps a base object within its corresponding high-level representation.
  */
  protected abstract T wrap(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    PdfString name
    );
  // </protected>

  // <private>
  /**
    Removes all the given node's children.
    <h3>Remarks</h3>
    <p>Removal affects only tree nodes: referenced objects are preserved
    to avoid inadvertently breaking possible references to them from somewhere else.</p>

    @param node Current node.
  */
  private void clear(
    PdfDictionary node
    )
  {
    Children children = getChildren(node);
    if(children.typeName.equals(PdfName.Kids))
    {
      for(PdfDirectObject child : children.items)
      {
        clear((PdfDictionary)File.resolve(child));
        getFile().unregister((PdfReference)child);
      }
      node.put(PdfName.Names, node.get(children.typeName));
      node.remove(children.typeName);
    }
    children.items.clear();
    node.remove(PdfName.Limits);
  }

  private <TCollection extends Collection<?>> void fill(
    IFiller<TCollection> filler,
    PdfReference nodeReference
    )
  {
    PdfDictionary node = (PdfDictionary)nodeReference.getDataObject();
    File.ResolvedObject<PdfArray> kidsObject = File.resolve(
      node.get(PdfName.Kids),
      nodeReference
      );
    if(kidsObject == null) // Leaf node.
    {
      File.ResolvedObject<PdfArray> namesObject = File.resolve(
        node.get(PdfName.Names),
        nodeReference
        );
      for(
        int index = 0,
          length = namesObject.dataObject.size();
        index < length;
        index += 2
        )
      {
        filler.add(
          namesObject.dataObject,
          index,
          namesObject.container
          );
      }
    }
    else // Intermediate node.
    {
      for(PdfDirectObject kidObject : kidsObject.dataObject)
      {fill(filler,(PdfReference)kidObject);}
    }
  }

  /**
    Gets the given node's children.

    @param node Parent node.
  */
  private Children getChildren(
    PdfDictionary node
    )
  {
    PdfArray children = null;
    PdfName childrenTypeName = null;
    int childrenOrder = 0;
    for(
      int index = 0,
        length = ChildrenTypeNames.length;
      index < length;
      index++
      )
    {
      childrenTypeName = ChildrenTypeNames[index];
      children = (PdfArray)node.resolve(childrenTypeName);
      if(children == null)
        continue;

      childrenOrder = ChildrenOrders[index];
      break;
    }
    return new Children(children, childrenTypeName, childrenOrder);
  }

  /**
    Gets the given node's entries count.

    @param node Current node.
  */
  private int getSize(
    PdfDictionary node
    )
  {
    PdfArray children = (PdfArray)node.resolve(PdfName.Names);
    if(children == null) // Intermediate node.
    {
      children = (PdfArray)node.resolve(PdfName.Kids);

      int size = 0;
      for(PdfDirectObject child : children)
      {size += getSize((PdfDictionary)File.resolve(child));}

      return size;
    }
    else // Leaf node.
    {return (children.size() / 2);}
  }

  /**
    Puts an entry under the given tree node.

    @param key New entry's key.
    @param value New entry's value.
    @param node Current node.
  */
  private T put(
    PdfString key,
    T value,
    PdfReference nodeReference
    )
  {
    PdfDictionary node = (PdfDictionary)nodeReference.getDataObject();
    T oldValue;
    PdfArray children = (PdfArray)node.resolve(PdfName.Names);
    if(children == null) // Intermediate node.
    {
      children = (PdfArray)node.resolve(PdfName.Kids);
      int low = 0, high = children.size() - 1;
      while(true)
      {
        boolean matched = false;
        int mid = (low + high) / 2;
        PdfReference kidReference = (PdfReference)children.get(mid);
        PdfDictionary kid = (PdfDictionary)kidReference.getDataObject();
        PdfArray limits = (PdfArray)kid.resolve(PdfName.Limits);
        if(key.compareTo((PdfString)limits.get(0)) < 0) // Before the lower limit.
        {high = mid - 1;}
        else if(key.compareTo((PdfString)limits.get(1)) > 0) // After the upper limit.
        {low = mid + 1;}
        else // Limit range matched.
        {matched = true;}

        if(matched // Limit range matched.
          || low > high) // No limit range match.
        {
          Children kidChildren = getChildren(kid);
          if(kidChildren.items.size() >= kidChildren.order) // Current node is full.
          {
            // Split the node!
            splitFullNode(
              children,
              mid,
              kidChildren.typeName
              );
            // Is the key before the splitted node?
            if(key.compareTo(((PdfArray)kid.resolve(PdfName.Limits)).get(0)) < 0)
            {
              kidReference = (PdfReference)children.get(mid);
              kid = (PdfDictionary)kidReference.getDataObject();
            }
          }

          oldValue = put(
            key,
            value,
            kidReference
            );
          // Update the key limits!
          updateNodeLimits(
            node,
            children,
            PdfName.Kids
            );
          break;
        }
      }
    }
    else // Leaf node.
    {
      int childrenSize = children.size();
      int low = 0, high = childrenSize;
      while(true)
      {
        int mid = (mid = ((low + high) / 2)) - (mid % 2);
        if(mid >= childrenSize)
        {
          oldValue = null;
          // Append the entry!
          children.add(key);
          children.add(value.getBaseObject());
          break;
        }

        int comparison = key.compareTo(
          (PdfString)children.get(mid)
          );
        if(comparison < 0) // Before.
        {high = mid - 2;}
        else if(comparison > 0) // After.
        {low = mid + 2;}
        else // Matching entry.
        {
          oldValue = wrap(
            children.get(mid + 1),
            nodeReference.getIndirectObject(),
            (PdfString)children.get(mid)
            );
          // Overwrite the entry!
          children.set(mid, key);
          children.set(++mid, value.getBaseObject());
          break;
        }
        if(low > high)
        {
          oldValue = null;
          // Insert the entry!
          children.add(low, key);
          children.add(++low, value.getBaseObject());
          break;
        }
      }

      // Update the key limits!
      updateNodeLimits(
        node,
        children,
        PdfName.Names
        );
    }
    return oldValue;
  }

  /**
    Splits a full node.
    <h3>Remarks</h3>
    <p>A new node is inserted at the full node's position, receiving the lower half of its children.</p>

    @param nodes Parent nodes.
    @param fullNodeIndex Full node's position among the parent nodes.
    @param childrenTypeName Full node's children type.
  */
  private void splitFullNode(
    PdfArray nodes,
    int fullNodeIndex,
    PdfName childrenTypeName
    )
  {
    // Get the full node!
    PdfDictionary fullNode = (PdfDictionary)nodes.resolve(fullNodeIndex);
    PdfArray fullNodeChildren = (PdfArray)fullNode.resolve(childrenTypeName);

    // Create a new (sibling) node!
    PdfDictionary newNode = new PdfDictionary();
    PdfArray newNodeChildren = new PdfArray();
    newNode.put(childrenTypeName, newNodeChildren);
    // Insert the new node just before the full!
    nodes.add(fullNodeIndex,getFile().register(newNode)); // NOTE: Nodes MUST be indirect objects.

    // Transferring exceeding children to the new node...
    {
      int index = 0;
      int length;
      if(childrenTypeName.equals(PdfName.Kids))
      {length = NodeMinSize;}
      else if(childrenTypeName.equals(PdfName.Names))
      {length = NameNodeMinSize;}
      else // NOTE: Should NEVER happen.
      {throw new UnsupportedOperationException(childrenTypeName + " is NOT a supported child type.");}
      while(index++ < length)
      {
        newNodeChildren.add(fullNodeChildren.get(0));
        fullNodeChildren.remove(0);
      }
    }

    // Update the key limits!
    updateNodeLimits(
      newNode,
      newNodeChildren,
      childrenTypeName
      );
    updateNodeLimits(
      fullNode,
      fullNodeChildren,
      childrenTypeName
      );
  }

  /**
    Sets the key limits of the given node.

    @param node Node to update.
    @param children Node children.
    @param childrenTypeName Node's children type.
  */
  private void updateNodeLimits(
    PdfDictionary node,
    PdfArray children,
    PdfName childrenTypeName
    )
  {
    if(childrenTypeName.equals(PdfName.Kids))
    {
      node.put(
        PdfName.Limits,
        new PdfArray(
          new PdfDirectObject[]
          {
            ((PdfArray)((PdfDictionary)children.resolve(0)).resolve(PdfName.Limits)).get(0),
            ((PdfArray)((PdfDictionary)children.resolve(children.size()-1)).resolve(PdfName.Limits)).get(1)
          }
          )
        );
    }
    else if(childrenTypeName.equals(PdfName.Names))
    {
      node.put (
        PdfName.Limits,
        new PdfArray(
          new PdfDirectObject[]
          {
            children.get(0),
            children.get(children.size()-2)
          }
          )
        );
    }
    else // NOTE: Should NEVER happen.
    {throw new UnsupportedOperationException(childrenTypeName + " is NOT a supported child type.");}
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}