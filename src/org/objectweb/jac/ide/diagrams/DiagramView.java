/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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
*/

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.framework.ViewChangeListener;
import CH.ifa.draw.standard.ChangeAttributeCommand;
import CH.ifa.draw.standard.ChopBoxConnector;
import CH.ifa.draw.standard.StandardDrawing;
import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.CommandChoice;
import CH.ifa.draw.util.Filler;
import CH.ifa.draw.util.PaletteButton;
import CH.ifa.draw.util.PaletteListener;
import CH.ifa.draw.util.StandardVersionControlStrategy;
import CH.ifa.draw.util.VersionControlStrategy;
import CH.ifa.draw.util.VersionManagement;
import CH.ifa.draw.util.VersionRequester;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.objectweb.jac.aspects.gui.CollectionUpdate;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.Transfer;
import org.objectweb.jac.aspects.gui.Utils;
import org.objectweb.jac.aspects.gui.ViewFactory;
import org.objectweb.jac.aspects.gui.swing.AbstractView;
import org.objectweb.jac.aspects.gui.swing.SwingEvents;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.ide.Aspect;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Diagram;
import org.objectweb.jac.ide.InheritanceLink;
import org.objectweb.jac.ide.Link;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.ide.Package;
import org.objectweb.jac.ide.RelationLink;
import org.objectweb.jac.ide.RelationRole;
import org.objectweb.jac.ide.TypedElement;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.util.Strings;

public class DiagramView extends AbstractView
    implements DrawingEditor, PaletteListener, VersionRequester, 
              CollectionUpdate, DropTargetListener
{
    static public boolean init = false;

    protected Diagram diagram;

    public DiagramView(ViewFactory factory, DisplayContext context, 
                       Object diagram) {
        super(factory,context);
        new DropTarget(this, // component
                       DnDConstants.ACTION_COPY_OR_MOVE, // actions
                       this); // DropTargetListener
        this.diagram = (Diagram)diagram;
        init();
        Utils.registerCollection(diagram,"figures",this);
    }

    public void close(boolean validate) {
        Log.trace("diagram","CLOSING DIAGRAM "+this);
        super.close(validate);
        FigureEnumeration figures = fDrawing.figures();
        while (figures.hasMoreElements()) {
            Figure f = figures.nextFigure();
            if (f instanceof ModelElementFigure)
                ((ModelElementFigure)f).close();
        }
        Utils.unregisterCollection(diagram,"figures",this);      
    }

    public Object getSubstance() {
        return diagram;
    }

    public final Diagram getDiagram() {
        return diagram;
    }

    /**
     * Add a figure for a class at a given location
     * @param cl the class to add a figure for
     * @param location where to put the class figure
     */
    public void addClass(Class cl, Point location) {
        Diagram diagram = (Diagram)getSubstance();
        if (diagram.contains(cl)) 
            return;
        ClassFigure cf = null;
        org.objectweb.jac.ide.ClassFigure figure = new org.objectweb.jac.ide.ClassFigure(cl);
        Log.trace("diagram","diagram = "+diagram);
        diagram.addFigure(figure);
        Log.trace("diagram","creating new figure "+figure+","+
                  diagram.getContainer());
        cf = new ClassFigure(figure,diagram.getContainer(),
                             view());
        (view().add(cf)).displayBox(location,location);
    }

    /**
     * Import all relations and inheritance links between a class with
     * other classes on the diagram
     * @param cl the class to import relations for 
     */
    public void importRelations(Class cl) {
        Iterator it = diagram.getMissingRelations(cl).iterator();
        while (it.hasNext()) {
            Link relation = (Link)it.next();
            try {
                if (relation instanceof RelationLink)
                    importRelation((RelationLink)relation);
                else if (relation instanceof InheritanceLink)
                    importInheritance((InheritanceLink)relation);
            } catch (Exception e) {
                 Log.warning(e.getMessage());
            }
        }
    }

    /**
     * Import a relation in the diagram.
     * @param relation the relation to import
     * @throws Exception if both classes of the relation are not on the diagram
     */
    public void importRelation(RelationLink relation) throws Exception {
        ClassFigure endFig = findClass((Class)relation.getEnd());
        if (endFig==null) {
            throw new Exception("Cannot import relation "+GuiAC.toString(relation)+
                                " since "+GuiAC.toString(relation.getEnd())+
                                " is not on the diagram");
        }
        ClassFigure startFig = findClass((Class)relation.getStart());
        if (startFig==null) {
            throw new Exception("Cannot import relation "+GuiAC.toString(relation)+
                                " since "+GuiAC.toString(relation.getStart())+
                                " is not on the diagram");
        }

        org.objectweb.jac.ide.LinkFigure linkFig = new org.objectweb.jac.ide.LinkFigure(relation);
        diagram.addFigure(linkFig);
      
        RelationLinkFigure relf = new RelationLinkFigure();
        relf.setLinkFigure(linkFig);

        relf.startPoint(startFig.center());
        relf.endPoint(endFig.center());
        relf.connectStart(startFig.connectorAt(startFig.center()));
        relf.connectEnd(endFig.connectorAt(endFig.center()));

        relf.updateConnection();
      
        view().add(relf);
        if (relation.getName()!=null && !relation.getName().equals(""))
            view().add(relf.createName());
        if (relation.getEndRole()!=null && !relation.getEndRole().equals(""))
            view().add(relf.createEndRole());
        if (relation.getStartRole()!=null && !relation.getStartRole().equals(""))
            view().add(relf.createStartRole());
        String startCardinality = relation.startRole().getCardinality();
        if (startCardinality!=null && !startCardinality.equals(""))
            view().add(relf.createStartCardinality());
        String endCardinality = relation.endRole().getCardinality();
        if (endCardinality!=null && !endCardinality.equals(""))
            view().add(relf.createEndCardinality());
      
        Utils.registerObject(relation,relf);
    }

    /**
     * Import an inheritance link in the diagram. An exception 
     * @param inheritance the inheritance link to import
     */
    public void importInheritance(InheritanceLink inheritance) throws Exception {
        ClassFigure endFig = findClass((Class)inheritance.getEnd());
        ClassFigure startFig = findClass((Class)inheritance.getStart());
      
        org.objectweb.jac.ide.LinkFigure linkFig = new org.objectweb.jac.ide.LinkFigure(inheritance);
        diagram.addFigure(linkFig);
      
        InheritanceLinkFigure relf = new InheritanceLinkFigure();
        relf.setLinkFigure(linkFig);

        relf.startPoint(startFig.center());
        relf.endPoint(endFig.center());
        relf.connectStart(startFig.connectorAt(startFig.center()));
        relf.connectEnd(endFig.connectorAt(endFig.center()));

        relf.updateConnection();
      
        view().add(relf);
      
        Utils.registerObject(inheritance,relf);
    }

    /**
     * Create a RelationLink between two classes.
     *
     * @param source start class of the link
     * @param target end class of the link
     * @param linkFigure the figure that represents the relation
     * @param isAggregation wether the relation is an aggregation
     */
    public void createRelation(Class source, Class target, 
                               RelationLinkFigure linkFigure,
                               boolean isAggregation) {
        Log.trace("figures","creating a new relation link between "+
                  source+" and "+target);

        RelationLink rel = new RelationLink(source,target);
        rel.setAggregation(isAggregation);
        Log.trace("diagram","1. end="+rel.getEnd()+"===> substance="+target);
        org.objectweb.jac.ide.LinkFigure linkFig = new org.objectweb.jac.ide.LinkFigure(rel);
        linkFigure.setLinkFigure(linkFig);
        view().add(linkFigure.createName());
        view().add(linkFigure.createEndRole());
        view().add(linkFigure.createStartRole());
        view().add(linkFigure.createStartCardinality());
        view().add(linkFigure.createEndCardinality());
      
        Log.trace("diagram","2. end="+rel.getEnd());

        if (source==target) {
            Point c = linkFigure.endFigure().center();
            linkFig.addPoint(1,new Point(c.x+100,c.y));
            linkFig.addPoint(1,new Point(c.x+100,c.y+100));
            linkFig.addPoint(1,new Point(c.x,c.y+100));
        }
      
        diagram.addFigure(linkFig);

        Utils.registerObject(rel,linkFigure);
        Log.trace("diagram","3. end="+rel.getEnd());
    }

    // drop listener interface
    public void drop(DropTargetDropEvent e) {
        try {
            Transferable tr = e.getTransferable();
            List transfered = Transfer.getTransferedWrappees(tr);
            Object o = transfered.get(0);
            Point location = e.getLocation();
            Point offset = scrollPane.getViewport().getViewPosition();
            location.translate((int)offset.getX(),(int)offset.getY());
            Log.trace("gui.dnd","drop event: "+o);
            if (o==null) 
                return;
            if (o instanceof Class) {
                addClass((Class)o,location);
            } else if (o instanceof RelationRole) {
                importRelation((RelationLink)((RelationRole)o).getLink());
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    public void dragEnter(DropTargetDragEvent e) { }
    public void dragExit(DropTargetEvent e) { }
    public void dragOver(DropTargetDragEvent e) { }
    public void dropActionChanged(DropTargetDragEvent e) { }
    // end of drop listener interface

    public void figureSelectionChanged(DrawingView view) {
        if (view.selection().size() > 0) {
            Figure f = (Figure)view.selection().get(0);

            Log.trace("figures","figure "+f+" selected");

            if (f instanceof Selectable) {
                ((Selectable)f).onSelect(getContext());
            }

            if (f instanceof ClassFigure) {
                CollectionItem coll = ClassRepository.get().getClass(Package.class)
                    .getCollection("classes");
                EventHandler.get().onSelection(
                    getContext(),coll,((ClassFigure)f).getSubstance(),null,null);

            } else if (f instanceof InstanceFigure) {
                CollectionItem coll = ClassRepository.get().getClass(Package.class)
                    .getCollection("instances");
                EventHandler.get().onSelection(
                    getContext(),coll,((InstanceFigure)f).getSubstance(),null,null);

            } else if (f instanceof GenericObjectFigure) {

                Log.trace("figures","generic object");
                EventHandler.get().onSelection(
                    getContext(),
                    ((GenericObjectFigure)f).getCollection(),
                    ((GenericObjectFigure)f).getSubstance(),null,null);

            } else if (f instanceof RelationLinkFigure || 
                       f instanceof AttachedTextFigure) {
            
                RelationLink rel = (RelationLink)
                    ((ModelElementFigure)f).getSubstance();
                Class cl = (Class)rel.getStart();
                CollectionItem coll = ClassRepository.get().getClass(Package.class)
                    .getCollection("classes");
                EventHandler.get().onSelection(getContext(),coll,cl,null,null);
                coll = ClassRepository.get().getClass(Class.class)
                    .getCollection("relationRoles");
                EventHandler.get().onSelection(getContext(),coll,
                                               rel.getStartRole(),null,null);
                //((RelationLinkFigure)f).selectAll(view);

            } else if (f instanceof PointcutLinkFigure) {
            
                org.objectweb.jac.ide.PointcutLink rel = (org.objectweb.jac.ide.PointcutLink)
                    ((PointcutLinkFigure)f).getSubstance();
                org.objectweb.jac.ide.Aspect aspect = (org.objectweb.jac.ide.Aspect)rel.getStart();
                CollectionItem coll = ClassRepository.get().getClass(Package.class)
                    .getCollection("aspects");
                EventHandler.get().onSelection(getContext(),coll,aspect,null,null);
                coll = ClassRepository.get().getClass(Aspect.class)
                    .getCollection("pointcutLinks");
                EventHandler.get().onSelection(getContext(),coll,rel,null,null);
                //((PointcutLinkFigure)f).selectAll(view);
            }
        }
        //      update(getGraphics());
    }

    private transient Drawing         fDrawing;
    private transient Tool            fTool;
   
    private transient DrawingView     fView;
    private transient ToolButton      fSelectedToolButton;
   
    static String                     fgUntitled = "untitled";
   
    ToolPalette classPalette;
    ToolPalette aspectPalette;
    ToolPalette instancePalette;
    ToolPalette groupPalette;
    ToolPalette currentPalette;

    /**
     * Find the figure of a given class in the default drawing.
     * @param cl the Class to search for
     * @return a ModelElementFigure that matches cl, or null if none is found.
     */
    public ClassFigure findClass(Class cl) {
        return (ClassFigure)findFigure(view().drawing(),cl);
    }

    /**
     * Find the figure of a given model element
     * @param drawing the Drawing to search the figure into
     * @param cl the Class to search for
     * @return a ModelElementFigure that matches cl, or null if none is found.
     */
    public ClassFigure findClass(Drawing drawing, Class cl) {
        return (ClassFigure)findFigure(drawing,cl);
    }

    /**
     * Find the figure of a given model element.
     * @param drawing the Drawing to search the figure into
     * @param element the ModelElement to search for
     * @return a ModelElementFigure that matches element, or null if none is found.
     */
    public ModelElementFigure findFigure(Drawing drawing, ModelElement element) {
        FigureEnumeration figs = drawing.figures();
        while(figs.hasMoreElements()) {
            Figure fig = figs.nextFigure();
            if( (fig instanceof ModelElementFigure) && 
                ((ModelElementFigure)fig).getSubstance() == element) {
                return (ModelElementFigure)fig;
            }
        }
        return null;
    }

    /**
     * Find the figure of a given TypedElement
     */
    public Figure findElement(TypedElement te) {
        FigureEnumeration figs = view().drawing().figures();
        while(figs.hasMoreElements()) {
            Figure fig = figs.nextFigure();
            if( (fig instanceof ModelElementFigure) && 
                ((ModelElementFigure)fig).getSubstance() == te) {
                return fig;
            }
        }
        return null;
    }

    /**
     * Find the figure of a given ModelElement
     */
    public Figure findElement(ModelElement element) {
        FigureEnumeration figs = view().drawing().figures();
        while(figs.hasMoreElements()) {
            Figure fig = figs.nextFigure();
            if( (fig instanceof ModelElementFigure) && 
                ((ModelElementFigure)fig).getSubstance() == element) {
                return fig;
            }
        }
        return null;
    }
   
    JScrollPane scrollPane;
    /**
     * Initializes the applet and creates its contents.
     */
    public void init() {
        //DiagramView.display = display;
        //diagramViews.add(this);

        getVersionControlStrategy().assertCompatibleVersion();
		
        setLayout(new BorderLayout());

        fView = createDrawingView();

        try {
            buildClassBar();
        } catch (Exception e) {
            e.printStackTrace();
        }

        scrollPane = new JScrollPane((Component)view());
        add("Center", scrollPane);
        JPanel buttonPalette = createButtonPanel();
        createButtons(buttonPalette);
        add("North", buttonPalette);
        setToolBar(buildClassBar());

        initDrawing();
        // JFC should have its own internal double buffering...
        //setBufferedDisplayUpdate();
        //setupAttributes();
        load();
        //((JComponent)drawing()).setMinimumSize(new Dimension(2000,2000));
    }

    public void addViewChangeListener(ViewChangeListener vsl) {
    }

    public void removeViewChangeListener(ViewChangeListener vsl) {
    }


    /**
     * Creates the color choice for the given attribute.
     */
    protected JComboBox createColorChoice(String attribute) {
        CommandChoice choice = new CommandChoice();
        for (int i = 0; i < ColorMap.size(); i++)
            choice.addItem(
                new ChangeAttributeCommand(
                    ColorMap.name(i),
                    attribute,
                    ColorMap.color(i),
                    this
                )
                    );
        return choice;
    }

    /**
     * Creates the buttons panel.
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new PaletteLayout(2, new Point(2,2), false));
        return panel;
    }

    /**
     * Sets the tool bar containing tool buttons.
     */
    void setToolBar(ToolPalette newPalette) {
        if (currentPalette != null) {
            remove(currentPalette);
        }
        add(BorderLayout.WEST, newPalette);
        currentPalette = newPalette;
        validate();
    }

    ToolPalette buildClassBar() {
        Log.trace("diagram","BUILDING CLASS BAR");         
        classPalette = new ToolPalette();

        classPalette.setDefaultToolButton(
            classPalette.addToolButton(this, "icon_selection", "Selection Tool", 
                                       createSelectionTool()));

        classPalette.addToolButton(this, "icon_text", "Text Tool", 
                                   new TextTool(this, new TextFigure()));
        classPalette.addToolButton(this, "icon_class", "Create a new class", 
                                   new NewClassFigureCreationTool(this,getContext()));
        classPalette.addToolButton(this, "icon_import_class", "Add an existing class", 
                                   new ClassFigureCreationTool(this,getContext()));
        try {
            classPalette.addToolButton(this, "icon_see_relation", "Show existing relation", 
                                       new RelationLinkShowTool(this,getContext()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        classPalette.addToolButton(this, "icon_relation", "New relation link", 
                                   new RelationLinkCreationTool(this));
        classPalette.addToolButton(this, "icon_add_point", "Add a point", 
                                   new ConnectionTool(this, new RelationLinkFigure()));
        classPalette.addToolButton(this, "icon_inheritance", "Inheritance link", 
                                   new InheritanceLinkCreationTool(this));
        classPalette.addToolButton(this, "icon_field", "Create new attribute", 
                                   new FieldCreationTool(this,getContext()));
        classPalette.addToolButton(this, "icon_method", "Create new method", 
                                   new MethodCreationTool(this,getContext()));
        return classPalette;
    }

    ToolPalette buildAspectBar() {
        Log.trace("diagram","BUILDING ASPECT BAR");
      
        aspectPalette = new ToolPalette();
      
        aspectPalette.setDefaultToolButton(
            aspectPalette.addToolButton(this, "icon_selection", "Selection Tool", 
                                        createSelectionTool()));
      
        aspectPalette.addToolButton(this, "icon_text", "Text Tool", 
                                    new TextTool(this, new TextFigure()));
        aspectPalette.addToolButton(this, "icon_aspect", "Create a new aspect", 
                                    new NewAspectFigureCreationTool(this,getContext()));
        aspectPalette.addToolButton(this, "icon_import_aspect", "Add an existing aspect", 
                                    new AspectFigureCreationTool(this,getContext()));
        aspectPalette.addToolButton(this, "icon_pointcut", "New pointcut link", 
                                    new PointcutLinkCreationTool(this, new PointcutLinkFigure()));
        aspectPalette.addToolButton(this, "icon_see_pointcut", "Show existing pointcut",
                                    new PointcutLinkShowTool(this,getContext()));
        aspectPalette.addToolButton(this, "icon_add_point", "Add a point", 
                                    new ConnectionTool(this, new RelationLinkFigure()));
        aspectPalette.addToolButton(this, "icon_field", "Create new attribute", 
                                    new FieldCreationTool(this,getContext()));
        aspectPalette.addToolButton(this, "icon_method", "Create new configuration method", 
                                    new MethodCreationTool(this,getContext()));
        aspectPalette.addToolButton(this, "icon_aspect_method","Create new aspect method", 
                                    new MethodCreationTool(this,getContext()));
        return aspectPalette;
    }

    ToolPalette buildInstanceBar() {
        Log.trace("diagram","BUILDING INSTANCE BAR");
         
        instancePalette = new ToolPalette();
      
        instancePalette.setDefaultToolButton(
            instancePalette.addToolButton(this, "icon_selection", "Selection Tool",
                                          createSelectionTool()));
        instancePalette.addToolButton(this, "icon_text", "Text Tool", 
                                      new TextTool(this, new TextFigure()));      
        instancePalette.addToolButton(this, "icon_instance", "Create a new instance", 
                                      new NewInstanceFigureCreationTool(this,getContext()));
        instancePalette.addToolButton(this, "icon_import_instance", "Add an existing instance", 
                                      new InstanceFigureCreationTool(this,getContext()));
        return instancePalette;
    }

    ToolPalette buildGroupBar() {
        Log.trace("diagram","BUILDING GROUP BAR");
         
        groupPalette = new ToolPalette();

        groupPalette.setDefaultToolButton(
            groupPalette.addToolButton(this,"icon_selection", "Selection Tool", 
                                       createSelectionTool()));         
        groupPalette.addToolButton(this,"icon_text", "Text Tool", 
                                   new TextTool(this, new TextFigure()));
        groupPalette.addToolButton(this, "icon_group", "Create a new group", 
                                   new NewInstanceFigureCreationTool(this,getContext()));
        groupPalette.addToolButton(this, "icon_import_group", "Add an existing group", 
                                   new GroupFigureShowTool(this,getContext()));
        return groupPalette;
    }

    /**
     * Creates the buttons shown in the buttons panel. Override to
     * add additional buttons.
     * @param panel the buttons panel.
     */
    protected void createButtons(JPanel panel) {
        panel.add(new Filler(24,20));
        JLabel title = new JLabel(GuiAC.toString(diagram)+" diagram  ");
        panel.add(title);

        panel.add(new Filler(6,20));

        JComboBox combo = new JComboBox(new Object[] {
            "class mode",
            "aspect mode",
            "instance mode",
            "group mode"}
        );

        combo.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        JComboBox cb = (JComboBox)event.getSource();
                        String command = (String)cb.getSelectedItem();
                        if (command.equals("class mode")) {
                            setToolBar(buildClassBar());
                        } else if (command.equals("aspect mode")) {
                            setToolBar(buildAspectBar());
                        } else if (command.equals("instance mode")) {
                            setToolBar(buildInstanceBar());
                        } else if (command.equals("group mode")) {
                            setToolBar(buildGroupBar());
                        }
                        setSelected(currentPalette.getDefaultToolButton());
                    }
                }
        );

        panel.add(combo);
        coord = new JLabel("(--,--)");
        panel.add(coord);
    }

    JLabel coord;
    public void setCoord(int x, int y) {
        coord.setText("("+x+","+y+")");
    }

    /**
     * Creates the tools palette.
     */
    protected JPanel createToolPalette() {
        JPanel palette = new JPanel();
        palette.setLayout(new PaletteLayout(2,new Point(2,2)));
        return palette;
    }

    /**
     * Creates the selection tool used in this editor. Override to use
     * a custom selection tool.
     */
    protected Tool createSelectionTool() {
        return new SelectionTool(this,getContext());
    }

    /**
     * Creates the drawing used in this application.
     * You need to override this method to use a Drawing
     * subclass in your application. By default a standard
     * Drawing is returned.
     */
    protected Drawing createDrawing() {
        return new StandardDrawing();
    }

    /**
     * Creates the drawing view used in this application.
     * You need to override this method to use a DrawingView
     * subclass in your application. By default a standard
     * DrawingView is returned.
     */
    protected DrawingView createDrawingView() {
        IDEDrawingView view = new IDEDrawingView(this, 2000, 2000);
        view.addMouseListener(
            new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        // Show popup menu
                        if (e.isPopupTrigger()) {
                            Figure figure = drawing().findFigure(e.getX(), e.getY());
                            if (figure instanceof ModelElementFigure) {
                                ModelElement element = 
                                    ((ModelElementFigure)figure).getSubstance();
                                if (figure instanceof ClassFigure) {
                                    Figure memberFigure = figure.findFigureInside(e.getX(), e.getY());
                                    if (memberFigure instanceof MemberFigure) {
                                        SwingEvents.showObjectsMenu(
                                            getContext(), 
                                            new Object[] {((ClassFigure)figure).getClassFig(),
                                                          element,
                                                          ((MemberFigure)memberFigure).getSubstance()}, 
                                            e);
                                    } else {
                                        SwingEvents.showObjectsMenu(
                                            getContext(), new Object[] {((ClassFigure)figure).getClassFig(),element}, e);
                                    }
                                } else {
                                    SwingEvents.showObjectMenu(getContext(), element, e);
                                }
                            }
                        }
                    }
                }
        );

        view.addKeyListener(
            new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        int code = e.getKeyCode();
                        if ((code == KeyEvent.VK_BACK_SPACE) || 
                            (code == KeyEvent.VK_DELETE)) 
                        {
                            Log.trace("ide.diagram","DELETE");
                            Vector selection = view().selection();
                            for (int i=0; i<selection.size(); i++) {
                                Figure figure = (Figure)selection.get(i);
                                if (figure instanceof ClassFigure || 
                                    figure instanceof LinkFigure) {
                                    Log.trace("ide.diagram","removing "+figure);
                                    ModelElement element = 
                                        ((ModelElementFigure)figure).getSubstance();
                                    diagram.removeElement(element);
                                }
                                if (e.isControlDown()) {
                                    Log.trace("ide.diagram","DELETE REAL");
                                    if (figure instanceof ClassFigure) {
                                        Class cl = 
                                            (Class)((ModelElementFigure)figure).getSubstance();
                                        cl.getContainer().removeClass(cl);
                                    } else if (figure instanceof RelationLinkFigure) {
                                        RelationLink link = 
                                            (RelationLink)((ModelElementFigure)figure).getSubstance();
                                        link.setStart(null);
                                        link.setEnd(null);
                                    } else if (figure instanceof InheritanceLinkFigure) {
                                        InheritanceLink link = 
                                            (InheritanceLink)((ModelElementFigure)figure).getSubstance();
                                        ((Class)link.getStart()).setSuperClass(null);
                                    }
                                }
                            }
                            view().clearSelection();
                        }
                    }
                }
        );
        return view;
    }

    /**
     * Handles a user selection in the palette.
     * @see PaletteListener
     */
    public void paletteUserSelected(PaletteButton button) {
        ToolButton toolButton = (ToolButton)button;
        Log.trace("palette","paletteUserSelected "+toolButton.getTool());
        setTool(toolButton.getTool(), toolButton.getName());
        setSelected(toolButton);
    }

    /**
     * Handles when the mouse enters or leaves a palette button.
     * @see PaletteListener
     */
    public void paletteUserOver(PaletteButton button, boolean inside) {
        if (inside) {
            showStatus(((ToolButton)button).name());
        } else {
            if(fSelectedToolButton==null) {
                showStatus("");
            } else {
                showStatus(fSelectedToolButton.name());
            }
        }
    }

    /**
     * Gets the current drawing.
     * @see DrawingEditor
     */
    public Drawing drawing() {
        return fDrawing;
    }

    /**
     * Gets the current tool.
     * @see DrawingEditor
     */
    public Tool tool() {
        return fTool;
    }

    /**
     * Gets the current drawing view.
     * @see DrawingEditor
     */
    public DrawingView view() {
        return fView;
    }

    public DrawingView[] views() {
        return new DrawingView[] { view() } ;
    }

    /**
     * Sets the default tool of the editor.
     * @see DrawingEditor
     */
    public void toolDone() {
        if (currentPalette!=null) {
            ToolButton button = currentPalette.getDefaultToolButton();
            if (button!=null) {
                setTool(button.getTool(), button.getName());
                setSelected(button);
            }
        }
    }

    public void viewSelectionChanged(DrawingView oldView, DrawingView newView) {
    }

    private void initDrawing() {
        fDrawing = createDrawing();
        view().setDrawing(fDrawing);
        toolDone();
    }

    private void setTool(Tool t, String name) {
        Log.trace("palette","setTool "+t+" current="+fTool);
        if (fTool != null) {
            fTool.deactivate();
        }
        fTool = t;
        if (fTool != null) {
            showStatus(name);
            fTool.activate();
        }
    }

    private void setSelected(ToolButton button) {
        if (fSelectedToolButton != null) {
            fSelectedToolButton.reset();
        }
        fSelectedToolButton = button;
        if (fSelectedToolButton != null) {
            fSelectedToolButton.select();
        }
    }

    protected VersionControlStrategy getVersionControlStrategy() {
        return new StandardVersionControlStrategy(this);
    }

    /**
     * Subclasses should override this method to specify to which versions of
     * JHotDraw they are compatible. A string array is returned so it is possible
     * to specify several version numbers of JHotDraw to which the application
     * is compatible with.
     *
     * @return all versions number of JHotDraw the application is compatible with.
     */
    public String[] getRequiredVersions() {
        String[] requiredVersions = new String[1];
        // return the version of the package we are in
        requiredVersions[0] = 
            VersionManagement.getPackageVersion(DiagramView.class.getPackage());
        return requiredVersions;
    }

    /**
     * Initialize from the org.objectweb.jac.ide.Diagram
     */
    public void load() {
        toolDone();

        if (diagram == null) {
            Log.error("no diagram");
            return;
        }

        init=true;

        try {
            Log.trace("diagram","initializing drawing");
            fDrawing.release();
            fDrawing = new StandardDrawing();
            Vector links = new Vector();
            Vector ilinks = new Vector();
            Iterator i = getDiagram().getFigures().iterator();
            while (i.hasNext()) {
                Object figure = i.next();
                try {
                    if (figure instanceof org.objectweb.jac.ide.ClassFigure) {
                        if (((org.objectweb.jac.ide.ClassFigure)figure).getCl() 
                            instanceof org.objectweb.jac.ide.Aspect) {
                            fDrawing.add(
                                new AspectFigure((org.objectweb.jac.ide.ClassFigure)figure,
                                                 getDiagram().getContainer(),
                                                 view()));
                        } else {
                            fDrawing.add(
                                new ClassFigure((org.objectweb.jac.ide.ClassFigure)figure,
                                                getDiagram().getContainer(),
                                                view()));
                        }
                    } else if (figure instanceof org.objectweb.jac.ide.LinkFigure) {
                        Figure linkFigure = null;
                        Link link = ((org.objectweb.jac.ide.LinkFigure)figure).getLink();
                        if (link instanceof org.objectweb.jac.ide.RelationLink) {
                            linkFigure = 
                                new RelationLinkFigure((org.objectweb.jac.ide.LinkFigure)figure);
                        } else if (link instanceof org.objectweb.jac.ide.PointcutLink) {
                            linkFigure = 
                                new PointcutLinkFigure((org.objectweb.jac.ide.LinkFigure)figure);
                        } else if (link instanceof org.objectweb.jac.ide.InheritanceLink) {
                            linkFigure = 
                                new InheritanceLinkFigure((org.objectweb.jac.ide.LinkFigure)figure);
                        }
                        links.add(linkFigure);
                    }
                } catch(Exception e) {
                    Log.error("cannot load figure "+figure);
                    e.printStackTrace();
                }
            }

            // connect links
            i = links.iterator();
            while (i.hasNext()) {
                LinkFigure linkFigure = (LinkFigure)i.next();
                Log.trace("diagram","importing link "+linkFigure);
                Link link = (Link)linkFigure.getSubstance();
                ClassFigure start = findClass(fDrawing,(Class)link.getStart());
                Log.trace("diagram","link start "+link.getStart()+" -> "+start);
                if (start!=null) {
                    linkFigure.connectStart(new ChopBoxConnector(start));
                    linkFigure.startPoint(start.getCorner().x,start.getCorner().y);
                }
                ClassFigure end = findClass(fDrawing,(Class)link.getEnd());
                Log.trace("diagram","link end "+link.getEnd()+" -> "+end);
                if (end!=null) {
                    linkFigure.connectEnd(new ChopBoxConnector(end));
                    linkFigure.endPoint(end.getCorner().x,end.getCorner().y);
                }
                if (start!=null && end!=null)
                    linkFigure.updateConnection();
                if (start!=null && end!=null) {
                    fDrawing.add(linkFigure);
                    linkFigure.load(fDrawing);
                } else {
                    Log.warning("diagram",
                                "Bad link: start = "+link.getStart()+" -> "+start+
                                "; end = "+link.getEnd()+" -> "+end);
                }
            }

            // connect ilinks
            /*i = ilinks.iterator();
              while (i.hasNext()) {
              InheritanceLinkFigure linkFigure
              = (InheritanceLinkFigure)i.next();
              Log.trace("diagram","importing link "+linkFigure);
              Link link = (Link)linkFigure.getLinkFigure().getLink();
              ClassFigure start = findClass(fDrawing,(Class)link.getStart());
              Log.trace("diagram","link start "+link.getStart()+" -> "+start);
              if (start!=null) {
              linkFigure.connectStart(new ChopBoxConnector(start));
              linkFigure.startPoint(start.getCorner().x,start.getCorner().y);
              }
              ClassFigure end = findClass(fDrawing,(Class)link.getEnd());
              Log.trace("diagram","link start "+link.getEnd()+" -> "+end);
              if (end!=null) {
              linkFigure.connectEnd(new ChopBoxConnector(end));
              linkFigure.endPoint(end.getCorner().x,end.getCorner().y);
              }
              linkFigure.updateConnection();
              fDrawing.add(linkFigure);
              linkFigure.load(fDrawing);
              }*/

            Log.trace("diagram","sets the drawing"); 
            view().setDrawing(fDrawing);
            toolDone();
        } catch (Exception e) {
            initDrawing();
            e.printStackTrace();
            showStatus("Error:" + e);
        } finally {
            init=false;
        }
    }

    public void showStatus(String msg) {
        if (getContext()!=null) {
            getContext().getCustomizedView().showStatus(msg);
        }
        if (!Strings.isEmpty(msg))
            Log.trace("diagram","Status: "+msg);
    }

    public String toString() {
        return Strings.hex(this);
    }

    // CollectionUpdate interface

    public void onChange(Object substance, 
                         CollectionItem collection, Object value,
                         Object param) {
    }

    public void onAdd(Object substance, 
                      CollectionItem collection, Object value,
                      Object added, Object param) {
    }

    public void onRemove(Object substance, CollectionItem collection, Object value,
                         Object removed, Object param) {
        Log.trace("diagram.remove","onRemove "+removed);
        org.objectweb.jac.ide.Figure removedFigure = (org.objectweb.jac.ide.Figure)removed;
        ModelElementFigure figure = findFigure(fDrawing,removedFigure.getElement());
        Log.trace("diagram.remove","figure = "+figure);
        if (figure!=null) {
            Figure fig = fDrawing.remove((Figure)figure);
            view().removeFromSelection(figure);
            Log.trace("diagram.remove","removed "+fig);
            view().repairDamage();
        }
    }

}
