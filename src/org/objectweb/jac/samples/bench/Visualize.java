package org.objectweb.jac.samples.bench;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;


public class Visualize implements ListSelectionListener {
   public static void main(String args[]) throws FileNotFoundException,IOException {
      Visualize v = new Visualize();
      v.init(args[0]);
   }

   DefaultTreeModel treeModel;
   Model tableModel;
   TableSorter sorter;
   ListSelectionModel selectionModel;
   MethodNode root = new MethodNode("<root>");
   JFrame window;
   JDialog searchDialog;
   JTable table;
   int totalTime = 0;

   /**
    * Build the window
    */
   void init(String filename) throws FileNotFoundException, IOException {
      window = new JFrame("profiler data : "+filename);
      searchDialog = new SearchDialog();

      LineNumberReader reader = new LineNumberReader(new FileReader(filename));
      String line = reader.readLine(); // skip first line
      tableModel = new Model();
      buildTree(reader);
      treeModel = new DefaultTreeModel(root);

      sorter = new TableSorter(tableModel);
      table = new JTable(sorter);
      sorter.addMouseListenerToHeaderInTable(table); 

      JTree tree = new JTree(treeModel);
      tree.setRootVisible(false);
      tree.setCellRenderer(new TreeNodeRenderer());

      selectionModel = table.getSelectionModel();
      selectionModel.addListSelectionListener(this);
      table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

      JTabbedPane tabs = new JTabbedPane();
      tabs.addTab("Methods",new JScrollPane(table));
      tabs.addTab("Tree",new JScrollPane(tree));

      JToolBar toolBar = new JToolBar();
      JButton searchButton = new JButton("Search");
      searchButton.addActionListener(
         new ActionListener() {
               public void actionPerformed(ActionEvent event) {
                  searchDialog.show();
                  searchDialog.toFront();
               }
            }
      );
      toolBar.add(searchButton);
      

      window.getContentPane().add(toolBar,BorderLayout.NORTH);

      window.getContentPane().add(tabs);
      window.addWindowListener ( new WindowAdapter () {
               public void windowClosing( WindowEvent e ) {
                  System.out.println("Bye bye.");
                  System.exit(0);
               }
            }
         );
      window.pack();
      window.setBounds(new Rectangle(0,0,500,500));
      window.show();
   }

   void buildTree(LineNumberReader reader) 
      throws IOException 
   {
      String line;
      // build a a node for each method, and compute its children and
      // its total time and count
      while ((line=reader.readLine())!=null) {
         Entry entry = parseLine(line);
         MethodNode calleeNode = getNode(entry.callee);
         calleeNode.count += entry.count;
         calleeNode.time += entry.time;
         totalTime += entry.time;
         if (entry.caller!=null) {
            MethodNode callerNode = getNode(entry.caller);
            callerNode.add(new MethodNode(entry.callee,entry.count,entry.time));
         }
      }

      // for each child node previously build, compute its children
      // and sort them according to their time.
      Iterator i = nodes.values().iterator();
      while (i.hasNext()) {
         MethodNode parent = (MethodNode)i.next();
         parent.sortChildren();
         Iterator children = parent.getChildren().iterator();
         while (children.hasNext()) {
            MethodNode child = (MethodNode)children.next();
            MethodNode node = getNode(child.methodName);
            child.setChildren(node.getChildren());
         }
      }

   }

   Hashtable nodes = new Hashtable();

   /**
    * Returns a node with a given name
    */
   MethodNode getNode(String name) {
      MethodNode result = (MethodNode)nodes.get(name);
      if (result==null) {
         result = new MethodNode(name);
         tableModel.addEntry(result);
         nodes.put(name,result);
      }
      return result;
   }

   static Entry parseLine(String line) {
      try {
         Entry result = new Entry();
         int a=0,b;
         b = line.indexOf(" ");
         result.count = Integer.parseInt(line.substring(a,b));
         
         a=b+1;
         b = line.indexOf(" ",a);
         result.callee = line.substring(a,b);
         
         a=b+1;
         if (line.charAt(a)=='<') {
            b = line.indexOf(">",a);
            result.caller = null;
            b++;
         } else {
            b = line.indexOf(" ",a);
         result.caller = line.substring(a,b);
         }
         
         a=b+1;
         result.time = Integer.parseInt(line.substring(a));
         
         return result;
      } catch (Exception e) {
         System.err.println("Could not parse line: "+line);
         e.printStackTrace();
         return null;
      }
   }


   // ListSelectionListener

   public void valueChanged(ListSelectionEvent event) {
      if (event.getValueIsAdjusting())
         return;

      ListSelectionModel lsm = (ListSelectionModel)event.getSource();
      
      boolean isAdjusting = event.getValueIsAdjusting();
      
      if (!isAdjusting) {
         root = new MethodNode("<root>");
         if (!lsm.isSelectionEmpty()) {
            for (int i=lsm.getMinSelectionIndex(); i<=lsm.getMaxSelectionIndex(); i++) {
               root.add(sorter.getEntry(i));
            }
         }
         treeModel.setRoot(root);
      }
   }

   /**
    * Search dialog
    */

   class SearchDialog extends JDialog {
      JTextField text = new JTextField();
      int current = -1;
      public SearchDialog() {
         super(window,"Search");
         Container content = getContentPane();
         content.add(text);
         JPanel buttonPanel = new JPanel();
         JButton nextButton = new JButton("Next");
         nextButton.addActionListener(
            new ActionListener() {
                  public void actionPerformed(ActionEvent event) {
                     current++;
                     while (current<sorter.getRowCount()) {
                        MethodNode entry = sorter.getEntry(current);
                        if (entry.methodName.indexOf(text.getText())!=-1) {
                           selectionModel.setSelectionInterval(current,current);
                           Rectangle rect = table.getCellRect(current,1,true);
                           table.scrollRectToVisible(rect);
                           return;
                        }
                        current++;
                     }
                     current = -1;
                  }
               }
         );
         buttonPanel.add(nextButton);

         JButton closeButton = new JButton("Close");
         closeButton.addActionListener(
            new ActionListener() {
                  public void actionPerformed(ActionEvent event) {
                     searchDialog.hide();
                  }
               }
         );
         buttonPanel.add(closeButton);
         
         content.add(buttonPanel,BorderLayout.SOUTH);
         pack();
      }
   }
   
   class Model extends AbstractTableModel implements TableModel {
      Vector entries = new Vector();
   
      public void addEntry(MethodNode entry) {
         entries.add(entry);
      }

      public MethodNode getEntry(int row) {
         return (MethodNode)entries.get(row);
      }

      // implementation of javax.swing.table.TableModel interface
      public int getRowCount()
      {
         return entries.size();
      }

      public int getColumnCount()
      {
         return 3;
      }

      public String getColumnName(int col)
      {
         return new String[] {"Method","Count","Time", "%"} [col];
      }

      public Class getColumnClass(int col)
      {
         return new Class[] {String.class,Integer.class,Integer.class,Integer.class} [col];
      }

      public boolean isCellEditable(int param1, int param2)
      {
         return false;
      }

      public Object getValueAt(int row, int col)
      {
         MethodNode entry = getEntry(row);
         switch(col) {
            case 0: 
               return entry.methodName;
            case 1: 
               return new Integer(entry.count);
            case 2: 
               return new Integer(entry.time);
            case 3: 
               return new Integer(100*entry.time/totalTime);
            default:
               return null;
         }
      }

      public void setValueAt(Object param1, int row, int col)
      {
         // TODO: implement this javax.swing.table.TableModel method
      }

   }

}

class Entry {
   public String callee;
   public String caller;
   public int count;
   public int time;
}

class MethodNode implements TreeNode {
   public String methodName;
   public int count = 0;
   public int time = 0;
   public int percentage = 0;

   Vector children = new Vector();
   TreeNode parent;

   public MethodNode(String name) {
      methodName = name;
   }
   public MethodNode(String name, int count, int time) {
      methodName = name;
      this.count = count;
      this.time = time;
   }

   public Enumeration children() {
      return children.elements();
   }
   public Vector getChildren() {
      return children;
   }
   public void setChildren(Vector children) {
      this.children = children;
   }
   public void sortChildren() {
      Object[] array = children.toArray();
      Arrays.sort(array, new NodeComparator());
      for (int i=0; i<array.length; i++) {
         MethodNode child = (MethodNode)array[i];
         if (time!=0) {
            child.percentage = 100*child.time/time;
         } else {
            child.percentage = 0;
         }
         children.set(i,child);
      }
   }
   public boolean getAllowsChildren() {
      return true;
   }
   public TreeNode getChildAt(int i) {
      return (TreeNode)children.get(i);
   }
   public int getChildCount() {
      return children.size();
   }
   public int getIndex(TreeNode node) {
      return children.indexOf(node);
   }
   public TreeNode getParent() {
      return parent;
   }
   public boolean isLeaf() {
      return getChildCount()==0;
   }
   public void add(TreeNode node) {
      children.add(node);
   }
}

class TreeNodeRenderer extends DefaultTreeCellRenderer {

   public TreeNodeRenderer() {}
   
   public Component getTreeCellRendererComponent( JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      this.hasFocus = hasFocus;
      MethodNode node=(MethodNode)value;
      setText(
         "["+node.time+"/"+node.percentage+"%/"+node.count+"] "+
         node.methodName.substring(0,node.methodName.indexOf(")")+1));
      
      if(sel)
         setForeground(getTextSelectionColor());
      else
         setForeground(getTextNonSelectionColor());
      // There needs to be a way to specify disabled icons.
      if (!tree.isEnabled()) {
         setEnabled(false);
         if (leaf) {
            setDisabledIcon(getLeafIcon());
         } else if (expanded) {
            setDisabledIcon(getOpenIcon());
         } else {
            setDisabledIcon(getClosedIcon());
         }
      }
      else {
         setEnabled(true);
      }
      setComponentOrientation(tree.getComponentOrientation());	    
      selected = sel;

      return this;
   }
}

class NodeComparator implements Comparator {
   public int compare(Object a, Object b) {
      return ((MethodNode)b).time - ((MethodNode)a).time;
   }
   public boolean equals(Object comparator) {
      return comparator instanceof NodeComparator;
   }
}

class TableMap extends AbstractTableModel 
   implements TableModelListener 
{
    protected TableModel model; 

    public TableModel getModel() {
        return model;
    }

    public void setModel(TableModel model) {
        this.model = model; 
        model.addTableModelListener(this); 
    }

    // By default, implement TableModel by forwarding all messages 
    // to the model. 

    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn); 
    }
        
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn); 
    }

    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount(); 
    }

    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount(); 
    }
        
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn); 
    }

    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn); 
    }
        
    public boolean isCellEditable(int row, int column) { 
         return model.isCellEditable(row, column); 
    }
//
// Implementation of the TableModelListener interface, 
//
    // By default forward all events to all the listeners. 
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}

class TableSorter extends TableMap {
    int             indexes[];
    Vector          sortingColumns = new Vector();
    boolean         ascending = true;
    int compares;

    public TableSorter() {
        indexes = new int[0]; // for consistency
    }

    public TableSorter(TableModel model) {
        setModel(model);
    }

    public void setModel(TableModel model) {
        super.setModel(model); 
        reallocateIndexes(); 
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        TableModel data = model;

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
            Number n1 = (Number)data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number)data.getValueAt(row2, column);
            double d2 = n2.doubleValue();

            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == java.util.Date.class) {
            Date d1 = (Date)data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date)data.getValueAt(row2, column);
            long n2 = d2.getTime();

            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == String.class) {
            String s1 = (String)data.getValueAt(row1, column);
            String s2    = (String)data.getValueAt(row2, column);
            int result = s1.compareTo(s2);

            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean)data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean)data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();

            if (b1 == b2) {
                return 0;
            } else if (b1) { // Define false < true
                return 1;
            } else {
                return -1;
            }
        } else {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);

            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
        	return 0;
            }
        }
    }

    public int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++) {
            Integer column = (Integer)sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                return ascending ? result : -result;
            }
        }
        return 0;
    }

    public void reallocateIndexes() {
        int rowCount = model.getRowCount();

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public void tableChanged(TableModelEvent e) {
        //System.out.println("Sorter: tableChanged"); 
        reallocateIndexes();

        super.tableChanged(e);
    }

    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

    public void sort(Object sender) {
        checkModel();

        compares = 0;
        // n2sort();
        // qsort(0, indexes.length-1);
        shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
        //System.out.println("Compares: "+compares);
    }

    public void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i+1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
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
        return model.getValueAt(indexes[aRow], aColumn);
    }

   public MethodNode getEntry(int row) {
      return ((Visualize.Model)model).getEntry(indexes[row]);
   }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) {
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort(this);
        super.tableChanged(new TableModelEvent(this)); 
    }

    // There is no-where else to put this. 
    // Add a mouse listener to the Table to trigger a table sort 
    // when a column heading is clicked in the JTable. 
    public void addMouseListenerToHeaderInTable(JTable table) { 
        final TableSorter sorter = this; 
        final JTable tableView = table; 
        tableView.setColumnSelectionAllowed(false); 
        MouseAdapter listMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
                int column = tableView.convertColumnIndexToModel(viewColumn); 
                if (e.getClickCount() == 1 && column != -1) {
                    //System.out.println("Sorting ..."); 
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK; 
                    boolean ascending = (shiftPressed == 0); 
                    sorter.sortByColumn(column, ascending); 
                }
            }
        };
        JTableHeader th = tableView.getTableHeader(); 
        th.addMouseListener(listMouseListener); 
    }
}
