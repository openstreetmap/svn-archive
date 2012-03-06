/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * A double precision, general, dynamically-resizable,
 * two-dimensional matrix class.  Row and column numbering begins with
 * zero.  The representation is row major.
 */

public class GMatrix implements java.io.Serializable, Cloneable {

    // Compatible with 1.1
    static final long serialVersionUID = 2777097312029690941L;

    int nRow;
    int nCol;

    // double dereference is slow 
    double[][] values;

    /**
     * Constructs an nRow by NCol identity matrix. 
     * Note that because row and column numbering begins with
     * zero, nRow and nCol will be one larger than the maximum
     * possible matrix index values.
     * @param nRow  number of rows in this matrix.
     * @param nCol  number of columns in this matrix.
     */
    public GMatrix(int nRow, int nCol)
    {
        values = new double[nRow][nCol];
	this.nRow = nRow;
	this.nCol = nCol;

	int i, j;
	for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		values[i][j] = 0.0;
	    }
	}

	int l;
	if (nRow < nCol)
	    l = nRow;
        else
	    l = nCol;

	for (i = 0; i < l; i++) {
	    values[i][i] = 1.0;
	}
    }

    /** 
     * Constructs an nRow by nCol matrix initialized to the values 
     * in the matrix array.  The array values are copied in one row at
     * a time in row major fashion.  The array should be at least 
     * nRow*nCol in length.
     * Note that because row and column numbering begins with 
     * zero, nRow and nCol will be one larger than the maximum
     * possible matrix index values.
     * @param nRow  number of rows in this matrix. 
     * @param nCol  number of columns in this matrix. 
     * @param matrix  a 1D array that specifies a matrix in row major fashion
     */ 
    public GMatrix(int nRow, int nCol, double[] matrix)
    {
        values = new double[nRow][nCol];
	this.nRow = nRow;
	this.nCol = nCol;

	int i, j;
	for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		values[i][j] = matrix[i*nCol+j];
	    }
	}
    }

    /**
     * Sets the value of this matrix to the result of multiplying itself
     * with matrix m1 (this = this * m1).  
     * @param m1 the other matrix
     */  
    public final void mul(GMatrix m1)
    {
	int i, j, k;

	if (nCol != m1.nRow ||  nCol != m1.nCol)
	    throw new MismatchedSizeException
		(VecMathI18N.getString("GMatrix0"));

	double [][] tmp = new double[nRow][nCol];

	for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		tmp[i][j] = 0.0;
		for (k = 0; k < nCol; k++) {
		    tmp[i][j] += values[i][k]*m1.values[k][j];	
		}
	    }
	}
       
	values = tmp;
    }

    /**
     * Sets the value of this matrix to the result of multiplying
     * the two argument matrices together (this = m1 * m2).
     * @param m1 the first matrix
     * @param m2 the second matrix
     */
    public final void mul(GMatrix m1, GMatrix m2)
    {
	int i, j, k;

	if (m1.nCol != m2.nRow || nRow != m1.nRow || nCol != m2.nCol)
	    throw new MismatchedSizeException
		(VecMathI18N.getString("GMatrix1"));

	double[][] tmp = new double[nRow][nCol];

	for (i = 0; i < m1.nRow; i++) {
	    for (j = 0; j < m2.nCol; j++) {
		tmp[i][j] = 0.0;
		for (k = 0; k < m1.nCol; k++) {
		    tmp[i][j] += m1.values[i][k]*m2.values[k][j];	
		}
	    }
	}
       
	values = tmp;
    }

    /** 
     * Negates the value of this matrix: this = -this.
     */
    public final void negate() // NO_UCD
    {
	int i, j;
	for (i = 0; i < nRow; i++) {
	    for (j = 0;j < nCol; j++) {
		values[i][j] = -values[i][j];
	    }
	}
    }

    /**
     * Sets this GMatrix to the identity matrix.
     */
    public final void setIdentity() // NO_UCD
    {
        int i, j;
        for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		values[i][j] = 0.0;
	    }
        }

        int l;
        if (nRow < nCol)
	    l = nRow;
        else
	    l = nCol;

        for (i = 0; i < l; i++) {
	    values[i][i] = 1.0;
        }
    }

    /**
     * Sets all the values in this matrix to zero.
     */
    public final void setZero()
    {
	int i, j;
	for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		values[i][j] = 0.0; 
	    }
	}   
    }

   
    /**
     * Inverts this matrix in place.
     */
    public final void invert() // NO_UCD
    {
	invertGeneral(this);
    }


    /**
     * Copies a sub-matrix derived from this matrix into the target matrix.
     * The upper left of the sub-matrix is located at (rowSource, colSource);
     * the lower right of the sub-matrix is located at 
     * (lastRowSource,lastColSource).  The sub-matrix is copied into the
     * the target matrix starting at (rowDest, colDest).
     * @param rowSource   the top-most row of the sub-matrix 
     * @param colSource   the left-most column of the sub-matrix 
     * @param numRow   the number of rows in the sub-matrix
     * @param numCol  the number of columns in the sub-matrix
     * @param rowDest  the top-most row of the position of the copied
     *                 sub-matrix within the target matrix
     * @param colDest  the left-most column of the position of the copied
     *                 sub-matrix within the target matrix
     * @param target  the matrix into which the sub-matrix will be copied
     */
    public final void copySubMatrix(int rowSource, int colSource, 
				    int numRow, int numCol, int rowDest,
				    int colDest, GMatrix target) 
    {
        int i, j;

	if (this != target) {
	    for (i = 0; i < numRow; i++) {
		for (j = 0; j < numCol; j++) {
		    target.values[rowDest+i][colDest+j] =
			values[rowSource+i][colSource+j];
		}
	    }
	} else {
	    double[][] tmp = new double[numRow][numCol];
	    for (i = 0; i < numRow; i++) {
		for (j = 0; j < numCol; j++) {
		    tmp[i][j] = values[rowSource+i][colSource+j];
		}
	    }
	    for (i = 0; i < numRow; i++) {
		for (j = 0; j < numCol; j++) {
		    target.values[rowDest+i][colDest+j] = tmp[i][j];
		}
	    }
	}
    }

    /**
     * Changes the size of this matrix dynamically.  If the size is increased
     * no data values will be lost.  If the size is decreased, only those data
     * values whose matrix positions were eliminated will be lost.
     * @param nRow  number of desired rows in this matrix
     * @param nCol  number of desired columns in this matrix
     */
    public final void setSize(int nRow, int nCol)
    {
	double[][] tmp = new double[nRow][nCol];
	int i, j, maxRow, maxCol;

	if (this.nRow < nRow)
	    maxRow = this.nRow;
	else
	    maxRow = nRow;

	if (this.nCol < nCol)
	    maxCol = this.nCol;
	else
	    maxCol = nCol;

	for (i = 0; i < maxRow; i++) {
	    for (j = 0; j < maxCol; j++) {
		tmp[i][j] = values[i][j];
	    }
	}

	this.nRow = nRow;
	this.nCol = nCol;

	values = tmp;
    }


    /**
     * Returns the number of rows in this matrix.
     * @return  number of rows in this matrix
     */
    public final int getNumRow()
    {
        return(nRow);
    }

    /**
     * Returns the number of colmuns in this matrix.
     * @return  number of columns in this matrix
     */
    public final int getNumCol()
    {
	return(nCol);
    }

    /**
     * Retrieves the value at the specified row and column of this matrix.
     * @param row the row number to be retrieved (zero indexed)
     * @param column the column number to be retrieved (zero indexed)
     * @return the value at the indexed element
     */  
    public final double getElement(int row, int column)
    {
        return(values[row][column]);
    }

 
    /**  
     * Modifies the value at the specified row and column of this matrix.
     * @param row  the row number to be modified (zero indexed) 
     * @param column  the column number to be modified (zero indexed) 
     * @param value  the new matrix element value
     */   
    public final void setElement(int row, int column, double value)
    {
	values[row][column] = value;
    }

    /**
     * Transposes this matrix in place.
     */
    public final void transpose() // NO_UCD
    {
        int i, j;

        if (nRow != nCol) {
	    double[][] tmp;
	    i=nRow;
	    nRow = nCol;
	    nCol = i;
	    tmp = new double[nRow][nCol];
	    for (i = 0; i < nRow; i++) { 
		for (j = 0; j < nCol; j++) {
		    tmp[i][j] = values[j][i]; 
		}  
	    }  
	    values = tmp;
        } else {
	    double swap;
	    for (i = 0; i < nRow; i++) { 
		for (j = 0; j < i; j++) {
		    swap = values[i][j];
		    values[i][j] = values[j][i];
		    values[j][i] = swap;
		}  
	    }  
	}
    }


    /**
     * Returns a string that contains the values of this GMatrix.
     * @return the String representation
     */  
    public String toString() 
    {
	StringBuffer buffer = new StringBuffer(nRow*nCol*8);

	int i, j;

	for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		buffer.append(values[i][j]).append(" ");
	    }
	    buffer.append("\n");
	}

	return buffer.toString();
    }

    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different GMatrix objects with identical data
     * values (i.e., GMatrix.equals returns true) will return the
     * same hash number.  Two GMatrix objects with different data
     * members may return the same hash value, although this is not
     * likely.
     * @return the integer hash code value
     */
    public int hashCode() {
	long bits = 1L;

	bits = 31L * bits + nRow;
	bits = 31L * bits + nCol;

	for (int i = 0; i < nRow; i++) {
	    for (int j = 0; j < nCol; j++) {
		bits = 31L * bits + VecMathUtil.doubleToLongBits(values[i][j]);
	    }
	}

	return (int) (bits ^ (bits >> 32));
    } 


    /**
     * Returns true if all of the data members of GMatrix m1 are
     * equal to the corresponding data members in this GMatrix.
     * @param m1  The matrix with which the comparison is made.
     * @return  true or false
     */  
    public boolean equals(GMatrix m1)
    {
	try { 
	    int i, j;

	    if (nRow != m1.nRow || nCol != m1.nCol)
		return false;

	    for (i = 0;i < nRow; i++) {
		for (j = 0; j < nCol; j++) {
		    if (values[i][j] != m1.values[i][j])
			return false;
		}
	    }
	    return true;
	}  
	catch (NullPointerException e2) {
	    return false;
	}
    }

    /**
     * Returns true if the Object o1 is of type GMatrix and all of the
     * data members of o1 are equal to the corresponding data members in
     * this GMatrix.
     * @param o1  The object with which the comparison is made.
     * @return  true or false
     */  
    public boolean equals(Object o1)
    {
        try { 
	    GMatrix m2 = (GMatrix) o1;
	    int i, j;
	    if (nRow != m2.nRow || nCol != m2.nCol)
		return false;

	    for (i = 0; i < nRow; i++) {
                for (j = 0; j < nCol; j++) {
		    if (values[i][j] != m2.values[i][j])
			return false;
                }
	    }
	    return true;
        }
        catch (ClassCastException e1) {
	    return false;
	}
        catch (NullPointerException e2) {
	    return false;
	}
    }


    /**
     * General invert routine.  Inverts m1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     *
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     */
    final void invertGeneral(GMatrix  m1) {
        int size = m1.nRow*m1.nCol;
	double temp[] = new double[size];
	double result[] = new double[size];
	int row_perm[] = new int[m1.nRow];
	int[] even_row_exchange = new int[1];
	int i, j;

	// Use LU decomposition and backsubstitution code specifically
	// for floating-point nxn matrices.
	if (m1.nRow != m1.nCol) {
	    // Matrix is either under or over determined 
	    throw new MismatchedSizeException
		(VecMathI18N.getString("GMatrix22"));
	}

	// Copy source matrix to temp 
	for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		temp[i*nCol+j] = m1.values[i][j];
	    }
	}

	// Calculate LU decomposition: Is the matrix singular? 
	if (!luDecomposition(m1.nRow, temp, row_perm, even_row_exchange)) {
	    // Matrix has no inverse 
	    throw new SingularMatrixException
		(VecMathI18N.getString("GMatrix21"));
	}

	// Perform back substitution on the identity matrix 
        for (i = 0; i < size; i++)
	    result[i] = 0.0;

        for (i = 0; i < nCol; i++)
	    result[i+i*nCol] = 1.0;

	luBacksubstitution(m1.nRow, temp, row_perm, result);

	for (i = 0; i < nRow; i++) {
	    for (j = 0; j < nCol; j++) {
		values[i][j] =  result[i*nCol+j];
	    }
        }
    }

    /**
     * Given a nxn array "matrix0", this function replaces it with the 
     * LU decomposition of a row-wise permutation of itself.  The input 
     * parameters are "matrix0" and "dim".  The array "matrix0" is also 
     * an output parameter.  The vector "row_perm[]" is an output 
     * parameter that contains the row permutations resulting from partial 
     * pivoting.  The output parameter "even_row_xchg" is 1 when the 
     * number of row exchanges is even, or -1 otherwise.  Assumes data 
     * type is always double.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling, 
    //	      _Numerical_Recipes_in_C_, Cambridge University Press, 
    //	      1988, pp 40-45.
    //
    static boolean luDecomposition(int dim, double[] matrix0,
				   int[] row_perm, int[] even_row_xchg) {

	double row_scale[] = new double[dim];

	// Determine implicit scaling information by looping over rows 
	int i, j;
	int ptr, rs, mtx;
	double big, temp;

	ptr = 0;
	rs = 0;
	even_row_xchg[0] = 1;

	// For each row ... 
	i = dim;
	while (i-- != 0) {
	    big = 0.0;

	    // For each column, find the largest element in the row 
	    j = dim;
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

	// For all columns, execute Crout's method 
	mtx = 0;
	for (j = 0; j < dim; j++) {
	    int imax, k;
	    int target, p1, p2;
	    double sum;

	    // Determine elements of upper diagonal matrix U 
	    for (i = 0; i < j; i++) {
		target = mtx + (dim*i) + j;
		sum = matrix0[target];
		k = i;
		p1 = mtx + (dim*i);
		p2 = mtx + j;
		while (k-- != 0) {
		    sum -= matrix0[p1] * matrix0[p2];
		    p1++;
		    p2 += dim;
		}
		matrix0[target] = sum;
	    }

	    // Search for largest pivot element and calculate
	    // intermediate elements of lower diagonal matrix L.
	    big = 0.0;
	    imax = -1;
	    for (i = j; i < dim; i++) {
		target = mtx + (dim*i) + j;
		sum = matrix0[target];
		k = j;
		p1 = mtx + (dim*i);
		p2 = mtx + j;
		while (k-- != 0) {
		    sum -= matrix0[p1] * matrix0[p2];
		    p1++;
		    p2 += dim;
		}
		matrix0[target] = sum;

		// Is this the best pivot so far? 
		if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
		    big = temp;
		    imax = i;
		}
	    }

	    if (imax < 0) {
		throw new RuntimeException(VecMathI18N.getString("GMatrix24"));
	    }

	    // Is a row exchange necessary? 
	    if (j != imax) {
		// Yes: exchange rows 
		k = dim;
		p1 = mtx + (dim*imax);
		p2 = mtx + (dim*j);
		while (k-- != 0) {
		    temp = matrix0[p1];
		    matrix0[p1++] = matrix0[p2];
		    matrix0[p2++] = temp;
		}

		// Record change in scale factor 
		row_scale[imax] = row_scale[j];
		even_row_xchg[0] = -even_row_xchg[0]; // change exchange parity
	    }

	    // Record row permutation 
	    row_perm[j] = imax;

	    // Is the matrix singular 
	    if (matrix0[(mtx + (dim*j) + j)] == 0.0) {
		return false;
	    }

	    // Divide elements of lower diagonal matrix L by pivot 
	    if (j != (dim-1)) {
		temp = 1.0 / (matrix0[(mtx + (dim*j) + j)]);
		target = mtx + (dim*(j+1)) + j;
		i = (dim-1) - j;
		while (i-- != 0) {
		    matrix0[target] *= temp;
		    target += dim;
		}
	    }

	}

	return true;
    }

    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostion and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a nxn matrix of floating-point values.  The procedure takes each
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
    static void luBacksubstitution(int dim, double[] matrix1,
				   int[] row_perm,
				   double[] matrix2) {

	int i, ii, ip, j, k;
	int rp;
	int cv, rv, ri;
	double tt;
	
	// rp = row_perm;
	rp = 0;

	// For each column vector of matrix2 ... 
	for (k = 0; k < dim; k++) {
	    // cv = &(matrix2[0][k]);
	    cv = k;
	    ii = -1;

	    // Forward substitution 
	    for (i = 0; i < dim; i++) {
		double sum;

		ip = row_perm[rp+i];
		sum = matrix2[cv+dim*ip];
		matrix2[cv+dim*ip] = matrix2[cv+dim*i];
		if (ii >= 0) {
		    // rv = &(matrix1[i][0]);
		    rv = i*dim;
		    for (j = ii; j <= i-1; j++) {
			sum -= matrix1[rv+j] * matrix2[cv+dim*j];
		    }
		}
		else if (sum != 0.0) {
		    ii = i;
		}
		matrix2[cv+dim*i] = sum;
	    }

	    // Backsubstitution 
	    for (i = 0; i < dim; i++) {
		ri = (dim-1-i);
		rv = dim*(ri);
		tt = 0.0;
		for(j=1;j<=i;j++) {
		    tt += matrix1[rv+dim-j] * matrix2[cv+dim*(dim-j)]; 	  
		}
		matrix2[cv+dim*ri]= (matrix2[cv+dim*ri] - tt) / matrix1[rv+ri];
            }
	}
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
	GMatrix m1 = null;
	try {
	    m1 = (GMatrix)super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}

	// Also need to clone array of values
        m1.values = new double[nRow][nCol];
	for (int i = 0; i < nRow; i++) {
	   for(int j = 0; j < nCol; j++) {
	       m1.values[i][j] = values[i][j];
	   }
	}

	return m1;
    }

}
