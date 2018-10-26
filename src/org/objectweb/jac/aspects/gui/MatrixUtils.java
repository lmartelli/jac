/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

import java.io.IOException;
import java.io.Writer;
import org.jutils.csv.CSVWriter;
import org.objectweb.jac.util.Matrix;

/**
 * Gui related matrix utilities.
 * @see org.objectweb.jac.util.Matrix
 */
public class MatrixUtils {
    /**
     * Export a matrix to a CSV (comma separated values) file.
     * @param matrix the matrix to export
     * @param writer where to write the CSV data
     */
    public static void toCSV(Matrix matrix, Writer writer) throws IOException {
        CSVWriter out = new CSVWriter(writer);
        int rowCount = matrix.getRowCount();
        for (int j=0; j<rowCount; j++) {
            int colCount = matrix.getColumnCount();
            for (int i=0; i<colCount; i++) {
                out.write(GuiAC.toString(matrix.get(i,j)));
            }
            out.writeln();
        }
        writer.flush();
    }
}
