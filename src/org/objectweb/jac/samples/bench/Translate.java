package org.objectweb.jac.samples.bench;

import org.objectweb.jac.core.JacLoader;

public class Translate {

   /**
    * This sample shows jac's performances in class loading
    */

   public static void main(String args[]) throws Exception {
      String classes[] = new String[] {
         "org.objectweb.jac.core.dist.Topology",
         "org.objectweb.jac.util.Repository",
         "org.objectweb.jac.core.NameRepository",
         "org.objectweb.jac.core.ApplicationRepository",
         "org.objectweb.jac.core.ACManager",
         "org.objectweb.jac.core.dist.rmi.RMIRemoteContainerStub",
         "org.objectweb.jac.core.Application",
         "org.objectweb.jac.aspects.gui.WrappableMap",
         "org.objectweb.jac.lib.java.util.Vector",
         "org.objectweb.jac.core.ParserImpl",
         "org.objectweb.jac.core.rtti.RttiAC",
         "org.objectweb.jac.ide.Method",
         "org.objectweb.jac.ide.Project",
         "org.objectweb.jac.ide.Application",
         "org.objectweb.jac.ide.AspectConfiguration",
         "org.objectweb.jac.ide.ModelElement",
         "org.objectweb.jac.ide.Link",
         "org.objectweb.jac.ide.Diagram",
         "org.objectweb.jac.ide.Package",
         "org.objectweb.jac.aspects.gui.GuiAC",
         "org.objectweb.jac.aspects.gui.GenericFactory",
         "org.objectweb.jac.aspects.gui.swing.SwingCustomized",
         "org.objectweb.jac.aspects.gui.swing.ReferenceView",
         "org.objectweb.jac.aspects.gui.swing.ObjectChooser",
         "org.objectweb.jac.aspects.gui.swing.Tree",
         "org.objectweb.jac.aspects.gui.swing.SwingPanelView",
         "org.objectweb.jac.aspects.gui.swing.SwingLabel",
         "org.objectweb.jac.aspects.gui.swing.SwingTabbedView",
         "org.objectweb.jac.aspects.gui.swing.SwingEditorContainer",
         "org.objectweb.jac.aspects.gui.swing.SwingContainerView",
         "org.objectweb.jac.aspects.gui.swing.SingleSlotContainer",
         "org.objectweb.jac.aspects.gui.swing.DesktopView",
         "org.objectweb.jac.aspects.gui.swing.SwingMethodView",
         "org.objectweb.jac.aspects.gui.swing.SwingFieldView",
         "org.objectweb.jac.aspects.gui.swing.SwingTableView",
         "org.objectweb.jac.aspects.gui.swing.List",
         "org.objectweb.jac.aspects.gui.swing.MenuBar",
         "org.objectweb.jac.aspects.gui.swing.Menu",
         "org.objectweb.jac.aspects.gui.swing.ToolBar",
         "org.objectweb.jac.aspects.gui.swing.SwingEmptyView",
         "org.objectweb.jac.aspects.gui.swing.DateViewer",
         "org.objectweb.jac.aspects.gui.swing.ImageURLViewer",
         "org.objectweb.jac.aspects.gui.swing.TextViewer",
         "org.objectweb.jac.aspects.gui.swing.BooleanEditor",
         "org.objectweb.jac.aspects.gui.swing.FileEditor",
         "org.objectweb.jac.aspects.gui.swing.URLEditor",
         "org.objectweb.jac.aspects.gui.swing.DateEditor",
         "org.objectweb.jac.aspects.gui.swing.TextEditor",
         "org.objectweb.jac.aspects.gui.swing.PrimitiveFieldEditor",
         "org.objectweb.jac.aspects.gui.web.Empty",
         "org.objectweb.jac.aspects.gui.web.Customized",
         "org.objectweb.jac.aspects.gui.web.Panel",
         "org.objectweb.jac.aspects.gui.web.Container",
         "org.objectweb.jac.aspects.gui.web.SingleSlotContainer",
         "org.objectweb.jac.aspects.gui.web.Label",
         "org.objectweb.jac.aspects.gui.web.ReferenceView",
         "org.objectweb.jac.aspects.gui.web.PrimitiveField",
         "org.objectweb.jac.aspects.gui.web.Table",
         "org.objectweb.jac.aspects.gui.web.List",
         "org.objectweb.jac.aspects.gui.web.Tree",
         "org.objectweb.jac.aspects.gui.web.Tabs",
         "org.objectweb.jac.aspects.gui.web.Method",
         "org.objectweb.jac.aspects.gui.web.Page",
         "org.objectweb.jac.aspects.gui.web.Dialog",
         "org.objectweb.jac.aspects.gui.web.MenuBar",
         "org.objectweb.jac.aspects.gui.web.ToolBar",
         "org.objectweb.jac.aspects.gui.web.Menu",
         "org.objectweb.jac.aspects.gui.web.DateViewer",
         "org.objectweb.jac.aspects.gui.web.ImageURLViewer",
         "org.objectweb.jac.aspects.gui.web.EditorContainer",
         "org.objectweb.jac.aspects.gui.web.PrimitiveFieldEditor",
         "org.objectweb.jac.aspects.gui.web.ObjectChooser",
         "org.objectweb.jac.aspects.gui.web.DateEditor",
         "org.objectweb.jac.aspects.gui.web.URLEditor",
         "org.objectweb.jac.aspects.gui.web.BooleanEditor",
         "org.objectweb.jac.aspects.gui.CollectionWrapper",
         "java.util.Date",
         "java.util.Hashtable$Entry",
         "org.objectweb.jac.ide.Projects",
         "org.objectweb.jac.ide.Aspect",
         "org.objectweb.jac.ide.Class",
         "org.objectweb.jac.ide.RelationLink",
         "org.objectweb.jac.ide.PointcutLink",
         "org.objectweb.jac.ide.TypedElement",
         "org.objectweb.jac.ide.Field",
         "org.objectweb.jac.ide.Type",
         "org.objectweb.jac.ide.Instance",
         "org.objectweb.jac.ide.Group",
         "org.objectweb.jac.ide.AspectMethod",
         "org.objectweb.jac.ide.Parameter",
         "org.objectweb.jac.ide.TypeRepository",
         "org.objectweb.jac.ide.diagrams.ClassFigureCreationTool",
         "org.objectweb.jac.ide.diagrams.PointcutLinkShowTool",
         "org.objectweb.jac.ide.diagrams.RelationLinkShowTool",
         "org.objectweb.jac.ide.Errors",
         "org.objectweb.jac.ide.Error",
         "org.objectweb.jac.ide.diagrams.DiagramView",
         "org.objectweb.jac.aspects.persistence.PersistenceAC",
         "org.objectweb.jac.aspects.persistence.FSStorage",
         "org.objectweb.jac.lib.java.util.HashSet"};
      JacLoader loader;
      long start; 

      loader = new JacLoader(false,false);
      loader.setWrappeeTranslator(null);
      start = System.currentTimeMillis();
      for (int i=0; i<classes.length; i++) {
         loader.loadClass(classes[i]);
      }
      System.out.println("without translator "+(System.currentTimeMillis()-start)+"ms");

      loader = new JacLoader(false,false);
      start = System.currentTimeMillis();
      for (int i=0; i<classes.length; i++) {
         loader.loadClass(classes[i]);
      }
      System.out.println("with translator "+(System.currentTimeMillis()-start)+"ms");

   }
}
