package pdfimport;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.ContentScanner;
import it.stefanochizzolini.clown.documents.contents.ContentScanner.GraphicsState;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.ColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceGrayColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceRGBColorSpace;
import it.stefanochizzolini.clown.documents.contents.objects.BeginSubpath;
import it.stefanochizzolini.clown.documents.contents.objects.CloseSubpath;
import it.stefanochizzolini.clown.documents.contents.objects.ContainerObject;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.objects.DrawCurve;
import it.stefanochizzolini.clown.documents.contents.objects.DrawLine;
import it.stefanochizzolini.clown.documents.contents.objects.DrawRectangle;
import it.stefanochizzolini.clown.documents.contents.objects.EndPathNoOp;
import it.stefanochizzolini.clown.documents.contents.objects.FillEvenOdd;
import it.stefanochizzolini.clown.documents.contents.objects.FillStrokeEvenOdd;
import it.stefanochizzolini.clown.documents.contents.objects.GenericOperation;
import it.stefanochizzolini.clown.documents.contents.objects.Path;
import it.stefanochizzolini.clown.documents.contents.objects.Stroke;
import it.stefanochizzolini.clown.documents.contents.objects.Text;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfReal;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFStreamProcessor {

	private final LayerInfo info;
	public Rectangle2D bounds;
	int pathNo = 0;

	PathOptimizer optimizer = new PathOptimizer();
	Map<LayerInfo, LayerInfo> multipathLayers = new HashMap<LayerInfo, LayerInfo>();
	private GraphicsState state;
	private DeviceRGBColorSpace rgbSpace;
	private DeviceGrayColorSpace graySpace;

	public PDFStreamProcessor(Document doc) {

		this.rgbSpace = new DeviceRGBColorSpace(doc);
		this.graySpace = new DeviceGrayColorSpace(doc);

		this.info = new LayerInfo();
	}

	public void finish() {
		this.rgbSpace = null;
		this.graySpace = null;
		this.state = null;
		this.optimizer.optimize();
	}

	public List<LayerContents> getResult() {
		return this.optimizer.getLayers();
	}

	public void process(ContentScanner level) {
		if(level == null)
			return;

		while(level.moveNext()) {
			ContentObject object = level.getCurrent();
			if(object instanceof ContainerObject) {
				// Scan the inner level!
				process(level.getChildLevel());
			}
			else {
				addObject(level);
			}
		}
	}

	public void addObject(ContentScanner level){

		ContentObject obj = level.getCurrent();

		if (obj instanceof Path)
		{
			this.state = level.getChildLevel().getState();
			this.parsePath((Path)obj);
		}
		else if (obj instanceof Text){
			//maybe something here
		}
		else if (obj instanceof EndPathNoOp){
			//nothing here
			this.info.divider ++;
		}
		else if (obj instanceof GenericOperation) {
			this.state = level.getState();
			//operations PDF clown cannot handle
			parseGO((GenericOperation) obj, false);
		}
		else {
			int a = 10;
			a++;
		}
	}

	private void parseGO(GenericOperation go, boolean setDivider) {
		String op = go.getOperator();
		boolean parsed = true;
		//FIXME - currently mapping ICC colors (SCN) to device RGB.

		if (op.equals("RG") || op.equals("SCN")) {
			this.state.strokeColorSpace = this.rgbSpace;
			this.state.strokeColor = this.rgbSpace.getColor(go.getOperands().toArray(new PdfDirectObject[3]));
		}
		else if (op.equals("G")) {
			this.state.strokeColorSpace = this.graySpace;
			this.state.strokeColor = this.graySpace.getColor(go.getOperands().toArray(new PdfDirectObject[3]));
		}
		else if (op.equals("rg") || op.equals("scn")) {
			this.state.fillColorSpace = this.rgbSpace;
			this.state.fillColor = this.rgbSpace.getColor(go.getOperands().toArray(new PdfDirectObject[3]));
		}
		else if (op.equals("g")) {
			this.state.fillColorSpace = this.graySpace;
			this.state.fillColor = this.graySpace.getColor(go.getOperands().toArray(new PdfDirectObject[3]));
		}
		else {
			parsed = false;
			//nothing here
			int a = 10;
			a++;
			a++;
		}

		if (parsed && setDivider) {
			this.info.divider ++;
		}
	}

	private void parsePath(Path path) {

		List<PdfPath> paths = this.getPathNodes(path);
		this.updateInfoFromState();

		for (PdfPath p: paths){
			p.nr = pathNo;
		}

		pathNo ++;

		if (paths.size() > 1) {
			this.optimizer.addMultiPath(this.info, paths);
		}
		else if (paths.size() == 1) {
			this.optimizer.addPath(this.info, paths.get(0));
		}
	}

	private List<PdfPath> getPathNodes(Path path) {
		List<PdfPath> result = new ArrayList<PdfPath>(2);
		List<Point2D> points = new ArrayList<Point2D>(2);
		this.info.fill = false;
		this.info.stroke = true;

		for (ContentObject obj:path.getObjects()) {
			Point2D point = null;

			if (obj instanceof BeginSubpath) {
				if (points.size() >= 2) {
					result.add(new PdfPath(points));
					points = new ArrayList<Point2D>(2);
				}

				BeginSubpath b = (BeginSubpath)obj;
				point = b.getPoint();
			} else if (obj instanceof DrawLine) {
				DrawLine l = (DrawLine)obj;
				point = l.getPoint();
			}
			else if (obj instanceof DrawCurve) {

				DrawCurve c = (DrawCurve) obj;
				point = c.getPoint();
			}
			else if (obj instanceof Stroke) {
				//draw line mode..
			}
			else if (obj instanceof EndPathNoOp) {
				if (points.size() >= 2) {
					result.add(new PdfPath(points));
					points = new ArrayList<Point2D>(2);
				}
			}
			else if (obj instanceof CloseSubpath) {
				if (points.size() >= 2) {
					result.add(new PdfPath(points));
					points = new ArrayList<Point2D>(2);
				}
			}
			else if (obj instanceof FillEvenOdd) {
				this.info.fill = true;
				this.info.stroke = false;
			}
			else if (obj instanceof FillStrokeEvenOdd){
				this.info.fill = true;
				this.info.stroke = true;
			}
			else if (obj instanceof GenericOperation) {
				this.parseGO((GenericOperation)obj, false);
			}
			else if (obj instanceof DrawRectangle) {
				if (points.size() >= 2)
				{
					result.add(new PdfPath(points));
					points = new ArrayList<Point2D>(2);
				}

				DrawRectangle r = (DrawRectangle) obj;

				points.add(this.parsePoint(new Point2D.Double(r.getX(), r.getY())));
				points.add(this.parsePoint(new Point2D.Double(r.getX()+r.getWidth(), r.getY())));
				points.add(this.parsePoint(new Point2D.Double(r.getX()+r.getWidth(), r.getY()+r.getHeight())));
				points.add(this.parsePoint(new Point2D.Double(r.getX(), r.getY()+r.getHeight())));
				points.add(points.get(0));
				result.add(new PdfPath(points));
				points = new ArrayList<Point2D>(2);
			}
			else {
				int a = 10;
				a++;
			}

			//add point
			if (point != null)
			{
				boolean sameAsPrevPoint = (points.size() > 0) &&points.get(points.size() - 1).equals(point);
				if (!sameAsPrevPoint) {
					points.add(this.parsePoint(point));
				}
			}
		}

		if (points.size() >= 2)
		{
			result.add(new PdfPath(points));
		}

		return result;
	}

	private Point2D parsePoint(Point2D point) {
		point = this.state.userToDeviceSpace(point);
		return this.optimizer.getUniquePoint(point);
	}



	private void updateInfoFromState() {
		this.info.color = getColor(this.state.strokeColor);
		this.info.fillColor = getColor(this.state.fillColor);
		this.info.width = this.state.lineWidth;
	}

	private Color getColor(
			it.stefanochizzolini.clown.documents.contents.colorSpaces.Color col) {
		if (col == null) {
			return Color.BLACK;
		}

		ColorSpace space = col.getColorSpace();

		if (space instanceof DeviceRGBColorSpace) {
			return new Color(
					(float)col.getComponents()[0],
					(float)col.getComponents()[1],
					(float)col.getComponents()[2]);
		}
		else if (space instanceof DeviceGrayColorSpace) {
			return new Color(
					(float)col.getComponents()[0],
					(float)col.getComponents()[0],
					(float)col.getComponents()[0]);
		}
		else {
			throw new RuntimeException("Unexpected colour space: "+space.toString());
		}
	}

	private Color addColor(GenericOperation go) {
		List<PdfDirectObject> operands = go.getOperands();
		PdfDirectObject o1 = operands.get(0);
		PdfDirectObject o2 = operands.get(1);
		PdfDirectObject o3 = operands.get(2);
		Color c =new Color(parseFloat(o1), parseFloat(o2), parseFloat(o3));
		return c;
	}


	private Color addGrayColor(GenericOperation go) {
		List<PdfDirectObject> operands = go.getOperands();
		PdfDirectObject o1 = operands.get(0);
		Color c =new Color(parseFloat(o1), parseFloat(o1), parseFloat(o1));
		return c;
	}


	private float parseFloat(PdfDirectObject obj) {
		if (obj instanceof PdfReal) {
			return (float)((PdfReal)obj).getNumberValue();
		}
		else if (obj instanceof PdfInteger) {
			return (float)((PdfInteger)obj).getNumberValue();
		}
		else {
			return 0.0f;
		}
	}

}
