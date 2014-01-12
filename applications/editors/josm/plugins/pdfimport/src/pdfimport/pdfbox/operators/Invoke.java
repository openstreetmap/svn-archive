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
package pdfimport.pdfbox.operators;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

import pdfimport.pdfbox.PageDrawer;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class Invoke extends OperatorProcessor
{


	/**
	 * process : Do : Paint the specified XObject (section 4.7).
	 * @param operator The operator that is being executed.
	 * @param arguments List
	 * @throws IOException If there is an error invoking the sub object.
	 */
	@Override
	public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
	{
		PageDrawer drawer = (PageDrawer)context;
		PDPage page = drawer.getPage();
		COSName objectName = (COSName)arguments.get( 0 );
		Map xobjects = drawer.getResources().getXObjects();
		PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );
		if( xobject instanceof PDXObjectImage )
		{
			drawer.drawImage();
		}
		else if(xobject instanceof PDXObjectForm)
		{
			PDXObjectForm form = (PDXObjectForm)xobject;
			COSStream invoke = (COSStream)form.getCOSObject();
			PDResources pdResources = form.getResources();
			if(pdResources == null)
			{
				pdResources = page.findResources();
			}
			// if there is an optional form matrix, we have to
			// map the form space to the user space
			Matrix matrix = form.getMatrix();
			if (matrix != null)
			{
				Matrix xobjectCTM = matrix.multiply( context.getGraphicsState().getCurrentTransformationMatrix());
				context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
			}
			getContext().processSubStream( page, pdResources, invoke );
		}
		else
		{
			//unknown xobject type
		}


		//invoke named object.
	}
}
