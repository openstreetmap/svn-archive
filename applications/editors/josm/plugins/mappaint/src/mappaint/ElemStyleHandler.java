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
    ElemStyle curStyle;
    ElemStyles styles;
    String curWidth, curKey, curValue;
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
                        curWidth = atts.getValue(count);
                    else if (atts.getQName(count).equals("colour"))
                        curColour=ColorHelper.html2color(atts.getValue(count));
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
													URL path = getClass().getResource("/standard/icons/"+atts.getValue(count));
													if (path != null ) {
														curIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(path));
													} else{
														path = getClass().getResource("/standard/icons/amenity.png");
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
            styles.add (curKey, curValue, curStyle);
        }
        else if (inCondition && qName.equals("condition"))
            inCondition = false;
        else if (inLine && qName.equals("line"))
        {
            inLine = false;
            curStyle = new LineElemStyle(Integer.parseInt(curWidth), curColour,
										curMinZoom);
        }
        else if (inIcon && qName.equals("icon"))
        {
            inIcon = false;
            curStyle = new IconElemStyle(curIcon,curAnnotate,curMinZoom);
        }
        else if (inArea && qName.equals("area"))
        {
            inArea = false;
            curStyle = new AreaElemStyle (curColour,curMinZoom);
        }

    }

    @Override public void characters(char ch[], int start, int length)
    {
    }
}
////////////////////////////////////////////////////////////////////////////////



