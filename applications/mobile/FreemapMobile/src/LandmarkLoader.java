// A class for loading landmarks from a URL.
// We need to specify the parser we are going to be using to parse landmarks
// from this URL, i.e. the XML delivered from the URL can be in any format as
// long as we have a parser for it.

import javax.microedition.location.LandmarkStore;
import java.util.Enumeration;
import javax.microedition.location.Landmark;
import javax.microedition.io.*;
import java.io.DataInputStream;
import javax.xml.parsers.*;
import java.io.*;

public class LandmarkLoader
{

  LandmarkStore store;
  double w,s,e,n;
  String url;
  LandmarkSourceParserHandler parserHandler;
  
  public LandmarkLoader(LandmarkStore store,double w,double s,double e,double n,
                          String url,LandmarkSourceParserHandler parserHandler)
  {
    this.store=store;
    this.w=w;
    this.s=s;
    this.e=e;
    this.n=n;
    this.url=url;
    this.parserHandler=parserHandler;
  }

    public void load() throws Exception
    {
		Enumeration en=null;
		try
		{
        en = store.getLandmarks (null,s,n,w,e);
		}
		catch(IllegalArgumentException e)
		{
			throw new FreemapMobileException
			("IllegalArgument at getLandmarks "+ e.getMessage()+
        "wsen="+w+" "+s+" "+e+" " +n,"");
		}
		try
		{
		if(en!=null) // getLandmarks() returns null if none could be found
		{
        	while(en.hasMoreElements())
        	{
              store.deleteLandmark((Landmark)en.nextElement());
        	}
		}
		}
		catch(IllegalArgumentException e)
		{
		  throw new FreemapMobileException("IllegalArgument deleting landmarks:"+
          e.getMessage(),"");
    }
          SAXParser parser=null; 
          DataInputStream dis=null;
           HttpConnection conn=null; 
         try
         {   
                
        conn = (HttpConnection)
                                        Connector.open(url);
        }
        catch(IllegalArgumentException e)
        {
        throw new FreemapMobileException("Error opening http connection:"+
              "url="+url,"");
        }
        System.out.println
                                    ("Creating DataInputStream...");
        InputStream is=null;
        try
        {
          is=conn.openInputStream();
        }
        catch(IllegalArgumentException e)
        {
        throw new FreemapMobileException("Error opening input stream:"+
            e.getMessage(),"");
        }
        try
        {
          dis= new DataInputStream(is);
        }
        catch(IllegalArgumentException e)
        {
        throw new FreemapMobileException("Error creating data input stream:"+
            e.getMessage(),"");
        }
        try
        {
          parser =     
                                    SAXParserFactory.newInstance().
                                    newSAXParser();
        }
      catch(IllegalArgumentException e)
        {
        throw new FreemapMobileException("Error creating sax parser:" +
                e.getMessage(),"");
        }     
        parserHandler.setLandmarkStore(store);
      
                           
		try
		{
        	parser.parse(dis,parserHandler);    
		}
		catch(IllegalArgumentException e)
		{
			throw new FreemapMobileException
			("IllegalArgument at parser.parse "+ e.getMessage(),"");
		}
    }
   
}
