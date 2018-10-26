/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.util;

import java.util.Vector;
import org.apache.log4j.Logger;
import java.util.List;

/**
 * This class represents a wrappable matrix of object
 *
 * <p>The implementation is simple and not optimized.
 */
public class Matrix {
    Logger logger = Logger.getLogger("matrix");

    public Matrix() {
        rowCount = 0;
        columnCount = 0;
    }

    public Matrix(int cCount, int rCount) {
        allocate(cCount,rCount);
    }

    /**
     * Sets the number of columns and the number of rows. 
     *
     * <p>Old data is discarded and all cells are set to null</p>
     * @param cCount number of columns
     * @param rCount number of rows
     */
    public void allocate(int cCount, int rCount) {
        logger.debug("allocate("+cCount+","+rCount+")");
        this.columnCount = cCount;
        this.rowCount = rCount;
        Vector cols = new Vector(cCount);
        cols.setSize(cCount);
        for (int i=0; i<cCount; i++) {
            Vector col = new Vector(rCount);
            col.setSize(rCount);
            cols.set(i, col);
        }
        this.cols = cols;
    }

    /**
     * Inserts an element in the matrix.
     * 
     * @param i the row of insertion 
     * @param j the columm of insertion
     * @param value the object to insert in the matrix
     */
    public void set(int i, int j, Object value)
        throws IndexOutOfBoundsException 
    {
        checkBounds(i, j);
        ((Vector)cols.get(i)).set(j,value);
    }

    /** 
     *  Gets an element in the matrix.
     * 
     * @param i the row
     * @param j the columm
     * @return value the element at the given place
     */
    public Object get(int i, int j) throws IndexOutOfBoundsException {
        checkBounds(i, j);
        return ((Vector) cols.get(i)).get(j);
    }

    void checkBounds(int i, int j) throws IndexOutOfBoundsException {
        if (i >= columnCount) {
            throw new IndexOutOfBoundsException(
                "Column index out of bound for "+this+": "+
                i + " >= " + columnCount);
        }
        if (j >= rowCount) {
            throw new IndexOutOfBoundsException(
                "Row index out of bound for "+this+": "+
                j + " >= " + rowCount);
        }
    }

    List cols;

    int rowCount;

    /**
     * Gets the row count.
     */
    public int getRowCount() {
        return rowCount;
    }

    int columnCount;

    /**
     * Gets column count.
     */
    public int getColumnCount() {
        return columnCount;
    }
}
