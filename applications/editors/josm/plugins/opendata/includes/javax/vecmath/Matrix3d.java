/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 127 $
 * $Date: 2008-02-28 21:18:51 +0100 (jeu., 28 févr. 2008) $
 * $State$
 */

package javax.vecmath;


/**
 * A double precision floating point 3 by 3 matrix.
 * Primarily to support 3D rotations.
 *
 */
public class Matrix3d implements java.io.Serializable, Cloneable {

    // Compatible with 1.1
    static final long serialVersionUID = 6837536777072402710L;

    /**
     * The first matrix element in the first row.
     */
    public	double	m00;

    /**
     * The second matrix element in the first row.
     */
    public	double	m01;

    /**
     * The third matrix element in the first row.
     */
    public	double	m02;

    /**
     * The first matrix element in the second row.
     */
    public	double	m10;

    /**
     * The second matrix element in the second row.
     */
    public	double	m11;

    /**
     * The third matrix element in the second row.
     */
    public	double	m12;

    /**
     * The first matrix element in the third row.
     */
    public	double	m20;

    /**
     * The second matrix element in the third row.
     */
    public	double	m21;

    /**
     * The third matrix element in the third row.
     */
    public	double	m22;

    /**
     * Constructs and initializes a Matrix3d to all zeros.
     */
    public Matrix3d()
    {
	this.m00 = 0.0;
	this.m01 = 0.0;
	this.m02 = 0.0;

	this.m10 = 0.0;
	this.m11 = 0.0;
	this.m12 = 0.0;

	this.m20 = 0.0;
	this.m21 = 0.0;
	this.m22 = 0.0;

    }

   /**
     * Returns a string that contains the values of this Matrix3d.
     * @return the String representation
     */
    public String toString() {
      return
	this.m00 + ", " + this.m01 + ", " + this.m02 + "\n" +
	this.m10 + ", " + this.m11 + ", " + this.m12 + "\n" +
	this.m20 + ", " + this.m21 + ", " + this.m22 + "\n";
    }

    /**
     * Sets this Matrix3d to identity.
     */
    public final void setIdentity()
    {
	this.m00 = 1.0;
	this.m01 = 0.0;
	this.m02 = 0.0;

	this.m10 = 0.0;
	this.m11 = 1.0;
	this.m12 = 0.0;

	this.m20 = 0.0;
	this.m21 = 0.0;
	this.m22 = 1.0;
    }


    /**
     * Sets the specified element of this matrix3f to the value provided.
     * @param row the row number to be modified (zero indexed)
     * @param column the column number to be modified (zero indexed)
     * @param value the new value
     */
    public final void setElement(int row, int column, double value)
    {
	switch (row)
	  {
	  case 0:
	    switch(column)
	      {
	      case 0:
		this.m00 = value;
		break;
	      case 1:
		this.m01 = value;
		break;
	      case 2:
		this.m02 = value;
		break;
	      default:
		throw new ArrayIndexOutOfBoundsException(VecMathI18N.getString("Matrix3d0"));
	      }
	    break;

	  case 1:
	    switch(column)
	      {
	      case 0:
		this.m10 = value;
		break;
	      case 1:
		this.m11 = value;
		break;
	      case 2:
		this.m12 = value;
		break;
	      default:
		throw new ArrayIndexOutOfBoundsException(VecMathI18N.getString("Matrix3d0"));
	      }
	    break;


	  case 2:
	    switch(column)
	      {
	      case 0:
		this.m20 = value;
		break;
	      case 1:
		this.m21 = value;
		break;
	      case 2:
		this.m22 = value;
		break;
	      default:
		throw new ArrayIndexOutOfBoundsException(VecMathI18N.getString("Matrix3d0"));
	      }
	    break;

	  default:
		throw new ArrayIndexOutOfBoundsException(VecMathI18N.getString("Matrix3d0"));
	  }
    }

    /**
     * Retrieves the value at the specified row and column of the specified
     * matrix.
     * @param row the row number to be retrieved (zero indexed)
     * @param column the column number to be retrieved (zero indexed)
     * @return the value at the indexed element.
     */
    public final double getElement(int row, int column)
    {
	switch (row)
	  {
	  case 0:
	    switch(column)
	      {
	      case 0:
		return(this.m00);
	      case 1:
		return(this.m01);
	      case 2:
		return(this.m02);
	      default:
		break;
	      }
	    break;
	  case 1:
	    switch(column)
	      {
	      case 0:
		return(this.m10);
	      case 1:
		return(this.m11);
	      case 2:
		return(this.m12);
	      default:
		break;
	      }
	    break;

	  case 2:
	    switch(column)
	      {
	      case 0:
		return(this.m20);
	      case 1:
		return(this.m21);
	      case 2:
		return(this.m22);
	      default:
		break;
	      }
	    break;

	  default:
	    break;
	  }

	throw new ArrayIndexOutOfBoundsException(VecMathI18N.getString("Matrix3d1"));
    }

    /**
     * Sets the value of this matrix to its transpose.
     */
    public final void transpose() // NO_UCD
    {
	double temp;

	temp = this.m10;
	this.m10 = this.m01;
	this.m01 = temp;

	temp = this.m20;
	this.m20 = this.m02;
	this.m02 = temp;

	temp = this.m21;
	this.m21 = this.m12;
	this.m12 = temp;
    }

    /**
     * Inverts this matrix in place.
     */
    public final void invert() // NO_UCD
    {
         invertGeneral( this );
    }

    /**
     * General invert routine.  Inverts m1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     *
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     */
    private final void invertGeneral(Matrix3d  m1) {
	double result[] = new double[9];
	int row_perm[] = new int[3];
	int i;
	double[]    tmp = new double[9];  // scratch matrix

	// Use LU decomposition and backsubstitution code specifically
	// for floating-point 3x3 matrices.

	// Copy source matrix to t1tmp
        tmp[0] = m1.m00;
        tmp[1] = m1.m01;
        tmp[2] = m1.m02;

        tmp[3] = m1.m10;
        tmp[4] = m1.m11;
        tmp[5] = m1.m12;

        tmp[6] = m1.m20;
        tmp[7] = m1.m21;
        tmp[8] = m1.m22;


	// Calculate LU decomposition: Is the matrix singular?
	if (!luDecomposition(tmp, row_perm)) {
	    // Matrix has no inverse
	    throw new SingularMatrixException(VecMathI18N.getString("Matrix3d12"));
	}

	// Perform back substitution on the identity matrix
        for(i=0;i<9;i++) result[i] = 0.0;
        result[0] = 1.0; result[4] = 1.0; result[8] = 1.0;
	luBacksubstitution(tmp, row_perm, result);

        this.m00 = result[0];
        this.m01 = result[1];
        this.m02 = result[2];

        this.m10 = result[3];
        this.m11 = result[4];
        this.m12 = result[5];

        this.m20 = result[6];
        this.m21 = result[7];
        this.m22 = result[8];

    }

    /**
     * Given a 3x3 array "matrix0", this function replaces it with the
     * LU decomposition of a row-wise permutation of itself.  The input
     * parameters are "matrix0" and "dimen".  The array "matrix0" is also
     * an output parameter.  The vector "row_perm[3]" is an output
     * parameter that contains the row permutations resulting from partial
     * pivoting.  The output parameter "even_row_xchg" is 1 when the
     * number of row exchanges is even, or -1 otherwise.  Assumes data
     * type is always double.
     *
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 3x3 matrices.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 40-45.
    //
    static boolean luDecomposition(double[] matrix0,
				   int[] row_perm) {

	double row_scale[] = new double[3];

	// Determine implicit scaling information by looping over rows
	{
	    int i, j;
	    int ptr, rs;
	    double big, temp;

	    ptr = 0;
	    rs = 0;

	    // For each row ...
	    i = 3;
	    while (i-- != 0) {
		big = 0.0;

		// For each column, find the largest element in the row
		j = 3;
		while (j-- != 0) {
		    temp = matrix0[ptr++];
		    temp = Math.abs(temp);
		    if (temp > big) {
			big = temp;
		    }
		}

		// Is the matrix singular?
		if (big == 0.0) {
		    return false;
		}
		row_scale[rs++] = 1.0 / big;
	    }
	}

	{
	    int j;
	    int mtx;

	    mtx = 0;

	    // For all columns, execute Crout's method
	    for (j = 0; j < 3; j++) {
		int i, imax, k;
		int target, p1, p2;
		double sum, big, temp;

		// Determine elements of upper diagonal matrix U
		for (i = 0; i < j; i++) {
		    target = mtx + (3*i) + j;
		    sum = matrix0[target];
		    k = i;
		    p1 = mtx + (3*i);
		    p2 = mtx + j;
		    while (k-- != 0) {
			sum -= matrix0[p1] * matrix0[p2];
			p1++;
			p2 += 3;
		    }
		    matrix0[target] = sum;
		}

		// Search for largest pivot element and calculate
		// intermediate elements of lower diagonal matrix L.
		big = 0.0;
		imax = -1;
		for (i = j; i < 3; i++) {
		    target = mtx + (3*i) + j;
		    sum = matrix0[target];
		    k = j;
		    p1 = mtx + (3*i);
		    p2 = mtx + j;
		    while (k-- != 0) {
			sum -= matrix0[p1] * matrix0[p2];
			p1++;
			p2 += 3;
		    }
		    matrix0[target] = sum;

		    // Is this the best pivot so far?
		    if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
			big = temp;
			imax = i;
		    }
		}

		if (imax < 0) {
		    throw new RuntimeException(VecMathI18N.getString("Matrix3d13"));
		}

		// Is a row exchange necessary?
		if (j != imax) {
		    // Yes: exchange rows
		    k = 3;
		    p1 = mtx + (3*imax);
		    p2 = mtx + (3*j);
		    while (k-- != 0) {
			temp = matrix0[p1];
			matrix0[p1++] = matrix0[p2];
			matrix0[p2++] = temp;
		    }

		    // Record change in scale factor
		    row_scale[imax] = row_scale[j];
		}

		// Record row permutation
		row_perm[j] = imax;

		// Is the matrix singular
		if (matrix0[(mtx + (3*j) + j)] == 0.0) {
		    return false;
		}

		// Divide elements of lower diagonal matrix L by pivot
		if (j != (3-1)) {
		    temp = 1.0 / (matrix0[(mtx + (3*j) + j)]);
		    target = mtx + (3*(j+1)) + j;
		    i = 2 - j;
		    while (i-- != 0) {
			matrix0[target] *= temp;
			target += 3;
		    }
		}
	    }
	}

	return true;
    }

    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD3x3 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 3x3 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     *
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 44-45.
    //
    static void luBacksubstitution(double[] matrix1,
				   int[] row_perm,
				   double[] matrix2) {

	int i, ii, ip, j, k;
	int rp;
	int cv, rv;

	//	rp = row_perm;
	rp = 0;

	// For each column vector of matrix2 ...
	for (k = 0; k < 3; k++) {
	    //	    cv = &(matrix2[0][k]);
	    cv = k;
	    ii = -1;

	    // Forward substitution
	    for (i = 0; i < 3; i++) {
		double sum;

		ip = row_perm[rp+i];
		sum = matrix2[cv+3*ip];
		matrix2[cv+3*ip] = matrix2[cv+3*i];
		if (ii >= 0) {
		    //		    rv = &(matrix1[i][0]);
		    rv = i*3;
		    for (j = ii; j <= i-1; j++) {
			sum -= matrix1[rv+j] * matrix2[cv+3*j];
		    }
		}
		else if (sum != 0.0) {
		    ii = i;
		}
		matrix2[cv+3*i] = sum;
	    }

	    // Backsubstitution
	    //	    rv = &(matrix1[3][0]);
	    rv = 2*3;
	    matrix2[cv+3*2] /= matrix1[rv+2];

	    rv -= 3;
	    matrix2[cv+3*1] = (matrix2[cv+3*1] -
			    matrix1[rv+2] * matrix2[cv+3*2]) / matrix1[rv+1];

	    rv -= 3;
	    matrix2[cv+4*0] = (matrix2[cv+3*0] -
			    matrix1[rv+1] * matrix2[cv+3*1] -
			    matrix1[rv+2] * matrix2[cv+3*2]) / matrix1[rv+0];

	}
    }

   /**
     * Sets the value of this matrix to the result of multiplying itself
     * with matrix m1.
     * @param m1 the other matrix
     */
    public final void mul(Matrix3d m1)
    {
            double      m00, m01, m02,
                        m10, m11, m12,
                        m20, m21, m22;

            m00 = this.m00*m1.m00 + this.m01*m1.m10 + this.m02*m1.m20;
            m01 = this.m00*m1.m01 + this.m01*m1.m11 + this.m02*m1.m21;
            m02 = this.m00*m1.m02 + this.m01*m1.m12 + this.m02*m1.m22;

            m10 = this.m10*m1.m00 + this.m11*m1.m10 + this.m12*m1.m20;
            m11 = this.m10*m1.m01 + this.m11*m1.m11 + this.m12*m1.m21;
            m12 = this.m10*m1.m02 + this.m11*m1.m12 + this.m12*m1.m22;

            m20 = this.m20*m1.m00 + this.m21*m1.m10 + this.m22*m1.m20;
            m21 = this.m20*m1.m01 + this.m21*m1.m11 + this.m22*m1.m21;
            m22 = this.m20*m1.m02 + this.m21*m1.m12 + this.m22*m1.m22;

            this.m00 = m00; this.m01 = m01; this.m02 = m02;
            this.m10 = m10; this.m11 = m11; this.m12 = m12;
            this.m20 = m20; this.m21 = m21; this.m22 = m22;
    }

   /**
     * Returns true if all of the data members of Matrix3d m1 are
     * equal to the corresponding data members in this Matrix3d.
     * @param m1  the matrix with which the comparison is made
     * @return  true or false
     */
    public boolean equals(Matrix3d m1)
    {
      try {
         return(this.m00 == m1.m00 && this.m01 == m1.m01 && this.m02 == m1.m02
            && this.m10 == m1.m10 && this.m11 == m1.m11 && this.m12 == m1.m12
            && this.m20 == m1.m20 && this.m21 == m1.m21 && this.m22 == m1.m22);
      }
      catch (NullPointerException e2) { return false; }

    }

   /**
     * Returns true if the Object t1 is of type Matrix3d and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Matrix3d.
     * @param t1  the matrix with which the comparison is made
     * @return  true or false
     */
    public boolean equals(Object t1)
    {
        try {
           Matrix3d m2 = (Matrix3d) t1;
           return(this.m00 == m2.m00 && this.m01 == m2.m01 && this.m02 == m2.m02
             && this.m10 == m2.m10 && this.m11 == m2.m11 && this.m12 == m2.m12
             && this.m20 == m2.m20 && this.m21 == m2.m21 && this.m22 == m2.m22);
        }
        catch (ClassCastException   e1) { return false; }
        catch (NullPointerException e2) { return false; }

    }


    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Matrix3d objects with identical data values
     * (i.e., Matrix3d.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     * @return the integer hash code value
     */
    public int hashCode() {
	long bits = 1L;
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m00);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m01);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m02);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m10);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m11);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m12);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m20);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m21);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(m22);
	return (int) (bits ^ (bits >> 32));
    }


  /**
    *  Sets this matrix to all zeros.
    */
   public final void setZero() // NO_UCD
   {
        m00 = 0.0;
        m01 = 0.0;
        m02 = 0.0;

        m10 = 0.0;
        m11 = 0.0;
        m12 = 0.0;

        m20 = 0.0;
        m21 = 0.0;
        m22 = 0.0;

   }

   /**
    * Negates the value of this matrix: this = -this.
    */
   public final void negate() // NO_UCD
   {
       this.m00 = -this.m00;
       this.m01 = -this.m01;
       this.m02 = -this.m02;

       this.m10 = -this.m10;
       this.m11 = -this.m11;
       this.m12 = -this.m12;

       this.m20 = -this.m20;
       this.m21 = -this.m21;
       this.m22 = -this.m22;

   }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return a clone of this instance.
     * @exception OutOfMemoryError if there is not enough memory.
     * @see java.lang.Cloneable
     * @since vecmath 1.3
     */
    public Object clone() {
	Matrix3d m1 = null;
	try {
	    m1 = (Matrix3d)super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}

	// Also need to create new tmp arrays (no need to actually clone them)
	return m1;
    }

}
