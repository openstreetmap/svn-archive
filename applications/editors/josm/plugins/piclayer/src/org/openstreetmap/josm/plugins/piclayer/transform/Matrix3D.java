package org.openstreetmap.josm.plugins.piclayer.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;


class Matrix3D {
	double[][] a;
	
	public Matrix3D() {
		a = new double[3][]; a[0] = new double[3]; a[1] = new double[3]; a[2] = new double[3];
	}
	
	public Matrix3D(PictureTransform pictureTransform, double b11, double b12, double b13, double b21, double b22, double b23, double b31, double b32, double b33) {
		this();
		
		a[0][0] = b11; a[0][1] = b12; a[0][2] = b13;
		a[1][0] = b21; a[1][1] = b22; a[1][2] = b23;
		a[2][0] = b31; a[2][1] = b32; a[2][2] = b33;
	}
	
	public Matrix3D(List<? extends Point2D> list) {
		this();
		
		for(int i=0; i<3; i++) {
			Point2D p = list.get(i);
			a[0][i] = p.getX();
			a[1][i] = p.getY();
			a[2][i] = 1;
		}
	}

	public Matrix3D multiply(Matrix3D m) {
		Matrix3D result = new Matrix3D();
		for(int i=0; i<3; i++) 
			for (int j=0; j<3; j++) {
				double sum = 0;
				for (int k=0; k<3; k++)
					sum += a[i][k]*m.a[k][j];
				result.a[i][j] = sum;
			}				
		return result;
	}
	
	private double determinant() {
		return a[0][0]*(a[1][1]*a[2][2]-a[1][2]*a[2][1])-a[0][1]*(a[1][0]*a[2][2]-a[1][2]*a[2][0])
		   	+a[0][2]*(a[1][0]*a[2][1]-a[1][1]*a[2][0]);
	}
	
	public Matrix3D inverse() throws NoSolutionException {
		Matrix3D invert = new Matrix3D();
		double det = determinant();
		if (Math.abs(det) <= Double.MIN_VALUE)
			throw new NoSolutionException("Determinant = 0");
		
		double s = 1/det;
		
	    invert.a[0][0] = (s) * (a[1][1] * a[2][2] - a[1][2] * a[2][1]);
	    invert.a[1][0] = (s) * (a[1][2] * a[2][0] - a[1][0] * a[2][2]);
	    invert.a[2][0] = (s) * (a[1][0] * a[2][1] - a[1][1] * a[2][0]);
	    invert.a[0][1] = (s) * (a[0][2] * a[2][1] - a[0][1] * a[2][2]);
	    invert.a[1][1] = (s) * (a[0][0] * a[2][2] - a[0][2] * a[2][0]);
	    invert.a[2][1] = (s) * (a[0][1] * a[2][0] - a[0][0] * a[2][1]);
	    invert.a[0][2] = (s) * (a[0][1] * a[1][2] - a[0][2] * a[1][1]);
	    invert.a[1][2] = (s) * (a[0][2] * a[1][0] - a[0][0] * a[1][2]);
	    invert.a[2][2] = (s) * (a[0][0] * a[1][1] - a[0][1] * a[1][0]);
	    
	    return invert;
	}
	
	public AffineTransform toAffineTransform() throws NoSolutionException {
		if (!(Math.abs(a[2][0]) <= 1e-2 && Math.abs(a[2][1]) <= 1e-2 && Math.abs(a[2][2]-1) <= 1e-2))
			throw new NoSolutionException("Resulted matrix is not AF");
		return new AffineTransform(a[0][0], a[1][0], a[0][1], a[1][1], a[0][2], a[1][2]);
	}
}