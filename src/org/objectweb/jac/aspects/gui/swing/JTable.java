package org.objectweb.jac.aspects.gui.swing;

import java.awt.Component;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;

/**
 * A table with variable height rows. The height of a row is the
 * maximum preferred height of its cells.
 */
public class JTable extends javax.swing.JTable {
    static Logger logger = Logger.getLogger("gui.swing");

    /**
     * Recompute row height on tableChanged events
     */
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        setPreferredRowHeights(1);
    }

    /**
     * Compute preferred height of all rows
     * @param margin margin in pixels to add at the top and bottom of
     * each row
     */
    public void setPreferredRowHeights(int margin) {
        for (int row=0; row<getRowCount(); row++) {
            setPreferredRowHeight(row,margin);
        }
    }

    /**
     * Compute the preferred height of a row
     * @param row row index
     * @param margin margin in pixels to add at the top and bottom of
     * the row
     */
    public void setPreferredRowHeight(int row, int margin) {
        // Get the current default height for all rows
        int height = getRowHeight();
      
        // Determine highest cell in the row
        for (int col=0; col<getColumnCount(); col++) {
            try {
                TableCellRenderer renderer = getCellRenderer(row,col);
                if (renderer!=null) {
                    Component comp = prepareRenderer(renderer,row,col);
                    if (comp!=null) {
                        comp.validate();
                        int h = comp.getPreferredSize().height + 2*margin;
                        height = Math.max(height,h);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to compute cell height for ("+row+","+col+")",e);
            }
        }
        setRowHeight(row,height);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        try {
            return super.prepareRenderer(renderer,row,column);
        } catch (Exception e) {
            logger.error("Caught exception in prepareRenderer("+row+","+column+")",e);
            return null;
        }
    }
}
