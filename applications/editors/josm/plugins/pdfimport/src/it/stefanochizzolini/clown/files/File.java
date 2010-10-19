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

package it.stefanochizzolini.clown.files;

import it.stefanochizzolini.clown.bytes.FileInputStream;
import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.bytes.OutputStream;
import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.IPdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObject;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.tokens.FileFormatException;
import it.stefanochizzolini.clown.tokens.Reader;
import it.stefanochizzolini.clown.tokens.Writer;
import it.stefanochizzolini.clown.tokens.XRefEntry;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

/**
  PDF file representation.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.0
  @version 0.0.8
*/
public class File
  implements Closeable
{
  // <class>
  // <classes>
  public static final class ResolvedObject<T extends PdfDataObject>
  {
    public final T dataObject;
    public final PdfIndirectObject container;

    private ResolvedObject(
      T dataObject,
      PdfIndirectObject container
      )
    {
      this.dataObject = dataObject;
      this.container = container;
    }
  }
  // </classes>

  // <static>
  // <fields>
  private static Random hashCodeGenerator = new Random();
  // </fields>

  // <interface>
  // <public>
  /**
    Forces a generic object to be expressed as its corresponding data object.
  */
  public static PdfDataObject resolve(
    PdfObject object
    )
  {
    if(object instanceof IPdfIndirectObject)
      return ((IPdfIndirectObject)object).getDataObject();
    else
      return (PdfDataObject)object;
  }

  /**
    Resolves a generic object.
  */
  @SuppressWarnings("unchecked")
  public static <T extends PdfDataObject> ResolvedObject<T> resolve(
    PdfObject object,
    IPdfIndirectObject container
    )
  {
    if(object == null)
      return null;
    else if(object instanceof IPdfIndirectObject)
    {
      IPdfIndirectObject indirectObject = (IPdfIndirectObject)object;
      return new ResolvedObject<T>((T)indirectObject.getDataObject(),indirectObject.getIndirectObject());
    }
    else
      return new ResolvedObject<T>((T)object,container.getIndirectObject());
  }

  /**
    Forces a direct object to be updated (whether possible).
  */
  public static boolean update(
    PdfDirectObject object
    )
  {
    /*
      NOTE: Only PDF references are able to be updated. Other direct types
      are dependent on their respective containers for update.
    */
    if(object instanceof PdfReference)
    {
      ((PdfReference)object).getIndirectObject().update();
      return true;
    }
    else
      return false;
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <fields>
  private Document document;
  private int hashCode = File.hashCodeGenerator.nextInt();
  private IndirectObjects indirectObjects;
  private Reader reader;
  private PdfDictionary trailer;
  private String version;
  private XRefEntry[] xrefEntries;

  private String path;
  // </fields>

  // <constructors>
  public File(
    )
  {
    this.version = "1.6";
    this.trailer = new PdfDictionary();
    this.indirectObjects = new IndirectObjects(this,null);
    this.document = new Document(this);
  }

  public File(
    String path
    ) throws FileFormatException,
      java.io.FileNotFoundException
  {
    this(
      new FileInputStream(
        new java.io.RandomAccessFile(path,"r")
        )
      );
    this.path = path;
  }

  public File(
    IInputStream stream
    ) throws FileFormatException
  {
    this.reader = new Reader(stream,this);

    this.version = reader.readVersion();
    this.trailer = reader.readTrailer();

    // Is this file encrypted?
    if(trailer.containsKey(PdfName.Encrypt))
      throw new NotImplementedException("Encrypted files are currently not supported.");

    this.xrefEntries = reader.readXRefTable(trailer);
    this.indirectObjects = new IndirectObjects(this,xrefEntries);
    this.document = new Document(trailer.get(PdfName.Root));
  }
  // </constructors>

  // <interface>
  // <public>
  public Document getDocument(
    )
  {return document;}

  public IndirectObjects getIndirectObjects(
    )
  {return indirectObjects;}

  public Reader getReader(
    )
  {return reader;}

  public PdfDictionary getTrailer(
    )
  {return trailer;}

  /**
    Gets the file header version [PDF:1.6:3.4.1].
    <h3>Remarks</h3>
    <p>This property represents just the original file version; to get the actual version,
    use the {@link it.stefanochizzolini.clown.documents.Document#getVersion() Document.getVersion} method.</p>
  */
  public String getVersion(
    )
  {return version;}

  //TODO:IMPL avoid direct exposure of array (corruptible!)!!!
  public XRefEntry[] getXRefEntries(
    )
  {return xrefEntries;}

  public int hashCode(
    )
  {return hashCode;}

  /**
    Registers an <b>internal data object</b>.
    @since 0.0.4
  */
  public PdfReference register(
    PdfDataObject object
    )
  {return indirectObjects.add(object).getReference();}

  /**
    Serializes the file to the current file-system path using the standard serialization mode.
  */
  public void save(
    ) throws IOException
  {save(SerializationModeEnum.Standard);}

  /**
    Serializes the file to the current file-system path.
    @param mode Serialization mode.
  */
  public void save(
    SerializationModeEnum mode
    ) throws IOException
  {
    if(!new java.io.File(path).exists())
      throw new FileNotFoundException("No valid source path available: use writeTo instead.");

    /*
      NOTE: The document file cannot be directly overwritten
      as it's locked for reading by the open stream;
      its update is therefore delayed to its disposal,
      when the temporary file will overwrite it (see Dispose method).
    */
    writeTo(getTempPath(),mode);
  }

  /**
    Unregisters an <b>internal object</b>.
    @since 0.0.5
  */
  public void unregister(
    PdfReference reference
    )
  {indirectObjects.remove(reference.getObjectNumber());}

  /**
    Serializes the file to the specified file-system path.
    @param path Target path.
    @param mode Serialization mode.
  */
  public void writeTo(
    String path,
    SerializationModeEnum mode
    ) throws IOException
  {
    writeTo(
      new java.io.File(path),
      mode
      );
  }

  /**
    Serializes the file to the specified file-system file.
    @param file Target file.
    @param mode Serialization mode.
  */
  public void writeTo(
    java.io.File file,
    SerializationModeEnum mode
    ) throws IOException
  {
    OutputStream outputStream;
    java.io.BufferedOutputStream baseOutputStream;
    try
    {
      file.createNewFile();
      baseOutputStream = new java.io.BufferedOutputStream(
        new java.io.FileOutputStream(file)
        );
      outputStream = new OutputStream(baseOutputStream);
    }
    catch(Exception e)
    {throw new IOException(file.getPath() + " file couldn't be created.",e);}

    try
    {
      writeTo(
        outputStream,
        mode
        );

      baseOutputStream.flush();
      baseOutputStream.close();
    }
    catch(Exception e)
    {throw new IOException(file.getPath() + " file writing has failed.",e);}
  }

  /**
    Serializes the file to the specified stream.
    <h3>Remarks</h3>
    <p>It's caller responsibility to close the stream after this method ends.</p>
    @param stream Target stream.
    @param mode Serialization mode.
  */
  public void writeTo(
    IOutputStream stream,
    SerializationModeEnum mode
    )
  {
    Writer writer = new Writer(stream,this);

    switch(mode)
    {
      case Incremental:
        if(reader != null)
        {
          writer.writeIncremental();
          break;
        }
        // If reader IS null, fall through to Standard!
      case Standard:
        writer.writeStandard();
        break;
      case Linearized:
        throw new NotImplementedException();
    }
  }

  // <Closeable>
  public void close(
    ) throws IOException
  {
    if(reader != null)
    {
      reader.close();
      reader = null;

      /*
        NOTE: If the temporary file exists (see Save method),
        overwrite the document file.
      */
      java.io.File sourceFile = new java.io.File(getTempPath());
      if(sourceFile.exists())
      {
        java.io.File targetFile = new java.io.File(path);
        targetFile.delete();
        sourceFile.renameTo(targetFile);
      }
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

  // <private>
  private String getTempPath(
    )
  {return (path == null ? null : path + ".tmp");}
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}