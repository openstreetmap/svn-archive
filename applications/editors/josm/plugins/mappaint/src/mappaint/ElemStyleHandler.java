package mappaint;

import java.io.File;
import java.awt.Color;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.plugins.Plugin;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ElemStyleHandler extends DefaultHandler
{
    boolean inDoc, inRule, inCondition, inElemStyle, inLine, inIcon, inArea;
    ElemStyle curLineStyle=null;
    ElemStyle curIconStyle=null;
    ElemStyle curAreaStyle=null;
    ElemStyles styles;
    String curKey, curValue ;
		int curWidth = 1, curRealWidth = 0;
	int curMinZoom;
    ImageIcon curIcon;
    Color curColour;
    boolean curAnnotate;

    public ElemStyleHandler(  )
    {
        inDoc=inRule=inCondition=inElemStyle=inLine=inIcon=inArea=false;
        styles = new ElemStyles();
    }

    public void setElemStyles(ElemStyles styles)
    {
        this.styles = styles;
    }

    /*
    ElemStyles getElemStyles()
    {
        return styles;
    }
    */

    @Override public void startDocument()
    {
        inDoc = true;
    }

    @Override public void endDocument()
    {
        inDoc = false;
    }

    @Override public void startElement(String uri,String name, String qName, 
                                    Attributes atts)    
    {
        if(inDoc==true)
        {
            if(qName.equals("rule"))
            {
                inRule=true;
            }
            else if (qName.equals("condition") && inRule)
            {
                inCondition=true;
                for(int count=0; count<atts.getLength(); count++)
                {
                    if(atts.getQName(count).equals("k"))
                        curKey = atts.getValue(count);        
                    else if(atts.getQName(count).equals("v"))
                        curValue = atts.getValue(count);        
                }
            }
            else if (qName.equals("line"))
            {
                inLine = true;
                for(int count=0; count<atts.getLength(); count++)
                {
                    if(atts.getQName(count).equals("width"))
                        curWidth = Integer.parseInt(atts.getValue(count));
                    else if (atts.getQName(count).equals("colour"))
                        curColour=ColorHelper.html2color(atts.getValue(count));
										else if (atts.getQName(count).equals("realwidth"))
												curRealWidth=Integer.parseInt(atts.getValue(count));
                }
            }
			else if (qName.equals("zoom"))
			{
				curMinZoom = 0;
                for(int count=0; count<atts.getLength(); count++)
                {
                    if(atts.getQName(count).equals("min"))
                        curMinZoom = Integer.parseInt(atts.getValue(count));
                }
			}	
            else if (qName.equals("icon"))
            {
                inIcon = true;
                for(int count=0; count<atts.getLength(); count++)
                {
                    if(atts.getQName(count).equals("src")) {
												String imageFile = MapPaintPlugin.getStyleDir()+"icons/"+atts.getValue(count); 
												File f = new File(imageFile);
												if (f.exists()){
													//open icon from user directory
                        	curIcon = new ImageIcon(imageFile);
												} else {
													try {
														URL path = getClass().getResource("/standard/icons/"+atts.getValue(count));
														curIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(path));
													}
													catch (Exception e){
														URL path = getClass().getResource("/standard/icons/amenity.png");
														curIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(path));

													}
												}
                    } else if (atts.getQName(count).equals("annotate"))
                        curAnnotate = Boolean.parseBoolean
                                        (atts.getValue(count));
                }
            }
            else if (qName.equals("area"))
            {
                inArea = true;
                for(int count=0; count<atts.getLength(); count++)
                {
                    if (atts.getQName(count).equals("colour"))
                        curColour=ColorHelper.html2color(atts.getValue(count));
                }
            }
        }
    }


    @Override public void endElement(String uri,String name, String qName)
    {
        if(inRule && qName.equals("rule"))
        {
            inRule = false;
			if(curLineStyle != null)
			{
            	styles.add (curKey, curValue, curLineStyle);
				curLineStyle = null;
			}
			if(curIconStyle != null)
			{
            	styles.add (curKey, curValue, curIconStyle);
				curIconStyle = null;
			}
			if(curAreaStyle != null)
			{
            	styles.add (curKey, curValue, curAreaStyle);
				curAreaStyle = null;
			}
        }
        else if (inCondition && qName.equals("condition"))
            inCondition = false;
        else if (inLine && qName.equals("line"))
        {
            inLine = false;
            curLineStyle = new LineElemStyle(curWidth,curRealWidth, curColour,
										curMinZoom);
						curWidth=1;
						curRealWidth = 0;
        }
        else if (inIcon && qName.equals("icon"))
        {
            inIcon = false;
            curIconStyle = new IconElemStyle(curIcon,curAnnotate,curMinZoom);
        }
        else if (inArea && qName.equals("area"))
        {
            inArea = false;
            curAreaStyle = new AreaElemStyle (curColour,curMinZoom);
        }

    }

    @Override public void characters(char ch[], int start, int length)
    {
    }
}
////////////////////////////////////////////////////////////////////////////////



