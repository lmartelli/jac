package org.objectweb.jac.aspects.gui;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.MemberItem;


/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel) 
 * and itself implements TableModel. TableSorter does not store or copy 
 * the data in the TableModel, instead it maintains an array of 
 * integers which it keeps the same size as the number of rows in its 
 * model. When the model changes it notifies the sorter that something 
 * has changed eg. "rowsAdded" so that its internal array of integers 
 * can be reallocated. As requests are made of the sorter (like 
 * getValueAt(row, col) it redirects them to its model via the mapping 
 * array. That way the TableSorter appears to hold another copy of the table 
 * with the rows in a different order. The sorting algorthm used is stable 
 * which means that it does not move around rows when its comparison 
 * function returns 0 to denote that they are equivalent. 
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */

public class TableSorter extends TableMap  {
    static Logger logger = Logger.getLogger("gui.sort");

    int indexes[];
    /** list of SortCriteria */
    Vector sortingColumns = new Vector();
    int compares;
   
    public TableSorter() {
        indexes = new int[0]; // for consistency
    }
   
    public TableSorter(ExtendedTableModel model) {
        setModel(model);
    }

    /**
     * Gets the sort criteria for a column.
     * @return the SortCriteria of the given column, or null.
     */
    public SortCriteria getSortCriteria(int column) {
        for (int i=0; i<sortingColumns.size(); i++) {
            if (((SortCriteria)sortingColumns.get(i)).column==column) {
                return (SortCriteria)sortingColumns.get(i);
            }
        }
        return null;
    }

    public void setModel(ExtendedTableModel model) {
        super.setModel(model); 
        reallocateIndexes(); 
        defaultSortOrder();
    }

    /**
     * Sets the sort column for the collection from the context or from
     * RTTI configuration. 
     */
    public void defaultSortOrder() {
        // First see if there's a config in the context
        HashMap map = (HashMap)Collaboration.get().getAttribute(GuiAC.SORT_COLUMN);
        if (map != null) {
            SortCriteria criteria = (SortCriteria)map.get(getCollection());
            if (criteria != null) {
                logger.debug("Using sort criteria from context: "+criteria);
                sortByColumn(criteria.column,criteria.ascending);
                return;
            }
        }

        // Then see if the collection has a default sort order
        String column = GuiAC.getDefaultSortedColumn(getCollection());
        if (column!=null) {
            logger.debug("Using default sort order : "+column);
            boolean ascending = true;
            if (column.startsWith("-")) {
                ascending = false;
                column = column.substring(1);
            } else if (column.startsWith("+")) {
                ascending = true;
                column = column.substring(1);
            }
            // find the column with that name
            MemberItem[] members = getMembers();
            for (int i=0; i<members.length; i++) {
                if (members[i].getName().equals(column)) {
                    sortByColumn(i,ascending);
                    return;
                }
            }
        }
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        ExtendedTableModel data = model;

        // Check for nulls.
      
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column); 
            
        // If both values are null, return 0.
        if (o1 == null && o2 == null) {
            return 0; 
        } else if (o1 == null) { // Define null less than everything. 
            return -1; 
        } else if (o2 == null) { 
            return 1; 
        }

        /*
         * We copy all returned values from the getValue call in case
         * an optimised model is reusing one object to return many
         * values.  The Number subclasses in the JDK are immutable and
         * so will not be used in this way but other subclasses of
         * Number might want to do this to save space and avoid
         * unnecessary heap allocation.
         */
      
        if (type.getSuperclass() == java.lang.Number.class) {
            double d1 = ((Number)o1).doubleValue();
            double d2 = ((Number)o2).doubleValue();
         
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == java.util.Date.class) {
            long n1 = ((Date)o1).getTime();
            long n2 = ((Date)o2).getTime();
         
            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == String.class) {
            return ((String)o1).compareToIgnoreCase((String)o2);
        } else if (type == Boolean.class) {
            boolean b1 = ((Boolean)o1).booleanValue();
            boolean b2 = ((Boolean)o2).booleanValue();
         
            if (b1 == b2) {
                return 0;
            } else if (b1) { // Define false < true
                return 1;
            } else {
                return -1;
            }
        } else {
            return GuiAC.toString(data.getValueAt(row1, column)).
                compareToIgnoreCase(GuiAC.toString(data.getValueAt(row2, column)));
        }
    }
   
    public int compare(int row1, int row2) {
        compares++;
        for (int level=0; level<sortingColumns.size(); level++) {
            SortCriteria criteria = (SortCriteria)sortingColumns.get(level);
            int result = compareRowsByColumn(row1, row2, criteria.column);
            if (result != 0) {
                return criteria.ascending ? result : -result;
            }
        }
        return 0;
    }
   
    /**
     * Reset to default unsorted order of the model.
     */
    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
      
        logger.debug(this+".reallocateIndexes "+rowCount);

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];
      
        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public int getActualIndex(int row) {
        return indexes[row];
    }
   
    public void tableChanged(TableModelEvent e) {
        if (e.getType()==TableModelEvent.INSERT || 
            e.getType()==TableModelEvent.DELETE) {
            logger.debug(this+".tableChanged "+
                      (e.getType()==TableModelEvent.DELETE?"DELETE":"INSERT")+
                      " "+e.getFirstRow()+"-"+e.getLastRow());
            reallocateIndexes();
            sort(this);
            super.tableChanged(e);
        } else {
            logger.debug(this+".tableChanged UPDATE "+
                      e.getFirstRow()+"-"+e.getLastRow());
            reallocateIndexes();
            sort(this);
            super.tableChanged(e);
        }
    }
   
    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
            logger.warn(this+" not informed of a change in model for collection "+
                        getCollection().getName()+": "+indexes.length+"!="+model.getRowCount());
        }
    }
   
    public void sort(Object sender) {
        if (sortingColumns.size()>0) {
            logger.debug("sorting "+getCollection()+" by "+sortingColumns);
            checkModel();
         
            compares = 0;
            // n2sort();
            // qsort(0, indexes.length-1);
            shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
            //System.out.println("Compares: "+compares);
        } else {
            reallocateIndexes();
        }
    }

    public void n2sort() {
        for (int i=0; i<getRowCount(); i++) {
            for (int j=i+1; j<getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) < 0) {
                    swap(i, j);
                }
            }
        }
    }
   
    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    public void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high)/2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
      
        int p = low;
        int q = middle;
      
        /* This is an optional short-cut; at each recursive call,
           check to see if the elements in this subset are already
           ordered.  If so, no further comparisons are needed; the
           sub-array can just be copied.  The array must be copied rather
           than assigned otherwise sister calls in the recursion might
           get out of sinc.  When the number of elements is three they
           are partitioned so that the first set, [low, mid), has one
           element and and the second, [mid, high), has two. We skip the
           optimisation when the number of elements is three or less as
           the first compare in the normal merge will produce the same
           sequence of steps. This optimisation seems to be worthwhile
           for partially ordered lists but some analysis is needed to
           find out how the performance drops to Nlog(N) as the initial
           order diminishes - it may drop very quickly.  */
      
        if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
      
        // A normal merge. 
      
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            }
            else {
                to[i] = from[q++];
            }
        }
    }
   
    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }
   
    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".
   
    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        if(indexes.length>aRow) {
            logger.debug("getValueAt("+aRow+","+aColumn+") -> "+
                      model.getValueAt(indexes[aRow], aColumn));
            return model.getValueAt(indexes[aRow], aColumn);
        } else {
            return null;
        }
    }
   
    public Object getObject(int row) {
        checkModel();
        return model.getObject(indexes[row]);
    }

    public int indexOf(Object object) {
        checkModel();
        return indexes[model.indexOf(object)];
    }

    public Object getObject(int row, int column) {
        checkModel();
        return model.getObject(indexes[row],column);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }
   
    /**
     * Sorts using values of a column
     * @param column index of column to sort by
     */
    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    /**
     * Sorts using values of a column. Reverse the order if the table
     * was already sorted by this column.
     * @param column index of column to sort by 
     */
    public void toggleSortByColumn(int column) {
        SortCriteria criteria = getSortCriteria(column);
        SortCriteria savedCriteria;
        if (criteria!=null) {
            criteria.toggleAscending();
            savedCriteria = new SortCriteria(column,criteria.isAscending());
        } else {
            sortingColumns.clear();
            sortingColumns.add(new SortCriteria(column,true));
            savedCriteria = new SortCriteria(column,true);
        }
        sort(this);
        saveSortCriteria(savedCriteria);
        super.tableChanged(new TableModelEvent(this)); 
    }

    public void sortByColumn(int column, boolean ascending) {
        logger.debug("sortByColumn "+column+
                     "("+(ascending?"":"-")+(column>=0?getHeaders()[column]:"none")+")");
        sortingColumns.clear();

        if (column>=0) {
            sortingColumns.add(new SortCriteria(column,ascending));
        }
        sort(this);

        saveSortCriteria(column>=0?new SortCriteria(column,ascending):null);

        super.tableChanged(new TableModelEvent(this)); 
    }

    /**
     * Save the sort criteria in the context
     * @param criteria the sort criteria to save
     */
    protected void saveSortCriteria(SortCriteria criteria) {
        //      new Exception().printStackTrace();
        HashMap map = (HashMap)Collaboration.get().getAttribute(GuiAC.SORT_COLUMN);
        if (map == null) {
            map = new HashMap();
            Collaboration.get().addAttribute(GuiAC.SORT_COLUMN, map);
        }
        map.put(getCollection(), criteria);
    }
}
