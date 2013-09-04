/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pdfimport.pdfbox;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.text.PDTextState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextPosition;


/**
 * This will paint a page in a PDF document to a graphics context.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.22 $
 */
public class PageDrawer extends PDFStreamEngine
{

	private GraphicsProcessor graphics;
	private BasicStroke stroke;
	protected PDPage page;

	private final GeneralPath linePath = new GeneralPath();

	/**
	 * Default constructor, loads properties from file.
	 *
	 * @throws IOException If there is an error loading properties from the file.
	 */
	public PageDrawer() throws IOException
	{
		super( ResourceLoader.loadProperties(
				"resources/pdfimport/pdfbox/PageDrawer.properties", true ) );
	}

	/**
	 * This will draw the page to the requested context.
	 *
	 * @param g The graphics context to draw onto.
	 * @param p The page to draw.
	 * @param pageDimension The size of the page to draw.
	 *
	 * @throws IOException If there is an IO error while drawing the page.
	 */
	public void drawPage( GraphicsProcessor g, PDPage p) throws IOException
	{
		graphics = g;
		page = p;
		// Only if there is some content, we have to process it.
		// Otherwise we are done here and we will produce an empty page
		if ( page.getContents() != null)
		{
			PDResources resources = page.findResources();
			processStream( page, resources, page.getContents().getStream() );
		}

		List<?> annotations = page.getAnnotations();
		for( int i=0; i<annotations.size(); i++ )
		{
			PDAnnotation annot = (PDAnnotation)annotations.get( i );
			String appearanceName = annot.getAppearanceStream();
			PDAppearanceDictionary appearDictionary = annot.getAppearance();
			if( appearDictionary != null )
			{
				if( appearanceName == null )
				{
					appearanceName = "default";
				}
				Map<?, ?> appearanceMap = appearDictionary.getNormalAppearance();
				if (appearanceMap != null) {
					PDAppearanceStream appearance =
						(PDAppearanceStream)appearanceMap.get( appearanceName );
					if( appearance != null )
					{
						processSubStream( page, appearance.getResources(), appearance.getStream() );
					}
				}
			}
		}

	}

	/**
	 * You should override this method if you want to perform an action when a
	 * text is being processed.
	 *
	 * @param text The text to process
	 */
	@Override
	protected void processTextPosition( TextPosition text )
	{

		Color color = null;

		try
		{
			switch(this.getGraphicsState().getTextState().getRenderingMode()) {
			case PDTextState.RENDERING_MODE_FILL_TEXT:
				color = this.getGraphicsState().getNonStrokingColor().getJavaColor();
				break;
			case PDTextState.RENDERING_MODE_STROKE_TEXT:
				color = this.getGraphicsState().getStrokingColor().getJavaColor();
				break;
			case PDTextState.RENDERING_MODE_NEITHER_FILL_NOR_STROKE_TEXT:
				//basic support for text rendering mode "invisible"
				Color nsc = this.getGraphicsState().getStrokingColor().getJavaColor();
				float[] components = {Color.black.getRed(),Color.black.getGreen(),Color.black.getBlue()};
				color =  new Color(nsc.getColorSpace(),components,0f);
				break;
			default:
				color = this.getGraphicsState().getNonStrokingColor().getJavaColor();
			}

			Matrix textPos = text.getTextPos().copy();
			float x = textPos.getXPosition();
			float y = textPos.getYPosition();
			graphics.setClip(getGraphicsState().getCurrentClippingPath());
			graphics.drawString(x,y,text.getCharacter(), color);
		}
		catch( IOException io )
		{
			io.printStackTrace();
		}
	}


	/**
	 * Get the page that is currently being drawn.
	 *
	 * @return The page that is being drawn.
	 */
	public PDPage getPage()
	{
		return page;
	}


	/**
	 * Get the current line path to be drawn.
	 *
	 * @return The current line path to be drawn.
	 */
	public GeneralPath getLinePath()
	{
		return linePath;
	}


	/**
	 * This will set the current stroke.
	 *
	 * @param newStroke The current stroke.
	 *
	 */
	public void setStroke(BasicStroke newStroke)
	{
		this.stroke = newStroke;
	}

	public BasicStroke getStroke() {
		return this.stroke;
	}


	public void drawPath(boolean stroke, boolean fill, int windingRule) throws IOException
	{
		graphics.setClip(getGraphicsState().getCurrentClippingPath());
		GeneralPath path = getLinePath();

		Color strokeColor = getGraphicsState().getStrokingColor().getJavaColor();
		Color fillColor = getGraphicsState().getNonStrokingColor().getJavaColor();
		graphics.drawPath(path, stroke ? strokeColor : null, fill ? fillColor : null, windingRule);

		path.reset();
	}


	/**
	 * Draw the AWT image. Called by Invoke.
	 * Moved into PageDrawer so that Invoke doesn't have to reach in here for Graphics as that breaks extensibility.
	 *
	 * @param awtImage The image to draw.
	 * @param at The transformation to use when drawing.
	 *
	 */
	public void drawImage(){
		graphics.setClip(getGraphicsState().getCurrentClippingPath());
		graphics.drawImage( );
	}

	/**
	 * Fill with Shading.  Called by SHFill operator.
	 *
	 * @param ShadingName  The name of the Shading Dictionary to use for this fill instruction.
	 *
	 * @throws IOException If there is an IO error while shade-filling the path/clipping area.
	 */
	public void SHFill(COSName ShadingName) throws IOException
	{
		this.drawPath(false, true, Path2D.WIND_NON_ZERO);
	}



	//This code generalizes the code Jim Lynch wrote for AppendRectangleToPath
	/**
	 * use the current transformation matrix to transform a single point.
	 * @param x x-coordinate of the point to be transform
	 * @param y y-coordinate of the point to be transform
	 * @return the transformed coordinates as Point2D.Double
	 */
	public java.awt.geom.Point2D.Double transformedPoint(double x, double y)
	{
		double[] position = {x,y};
		getGraphicsState().getCurrentTransformationMatrix().createAffineTransform().transform(
				position, 0, position, 0, 1);
		return new Point2D.Double(position[0],position[1]);
	}

	/**
	 * Set the clipping Path.
	 *
	 * @param windingRule The winding rule this path will use.
	 *
	 */
	public void setClippingPath(int windingRule)
	{
		PDGraphicsState graphicsState = getGraphicsState();
		GeneralPath clippingPath = (GeneralPath)getLinePath().clone();
		clippingPath.setWindingRule(windingRule);
		// If there is already set a clipping path, we have to intersect the new with the existing one
		if (graphicsState.getCurrentClippingPath() != null)
		{
			Area currentArea = new Area(getGraphicsState().getCurrentClippingPath());
			Area newArea = new Area(clippingPath);
			currentArea.intersect(newArea);
			graphicsState.setCurrentClippingPath(currentArea);
		}
		else
		{
			graphicsState.setCurrentClippingPath(clippingPath);
		}
		getLinePath().reset();
	}
}
