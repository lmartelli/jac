/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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

import CH.ifa.draw.figures.TextFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.standard.RelativeLocator;
import org.objectweb.jac.aspects.gui.ObjectUpdate;
import org.objectweb.jac.aspects.gui.Utils;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Field;
import org.objectweb.jac.ide.Method;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.ide.Package;
import org.objectweb.jac.ide.Projects;
import org.objectweb.jac.util.Log;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Vector;

public class ClassFigure extends CompositeFigure 
    implements ObjectUpdate, ModelElementFigure
{

    private static final int BORDER = 3;
    //   private Rectangle fDisplayBox;
   
    Vector relationLinkFigures = new Vector();
    Vector inheritanceLinkFigures = new Vector();
    Vector fieldFigures = new Vector();
    Vector methodFigures = new Vector();

    DrawingView view;

    org.objectweb.jac.ide.ClassFigure classFig;
    Dimension dimension = new Dimension();

    public ClassFigure(org.objectweb.jac.ide.ClassFigure figure, Package pack, 
                       DrawingView view) {
        initialize();
        this.classFig = figure;
        this.containerPackage = pack;
        this.view = view;

        ClassNameFigure name=new ClassNameFigure(figure.getCl(),this);
        name.setZValue(1);
        add(name);

        DiagramView.init = true;
        try {
            initFields();
            initMethods();
        } finally {
            DiagramView.init = false;
        }
        Utils.registerObject(classFig.getCl(),this);
        Utils.registerObject(classFig,this);

        layout();
    }

    // ObjectUpdate interface
    public void objectUpdated(Object object, Object extra) {
        Vector fs = new Vector();
        fs.add(this);
        initClass();
        layout();
        changed();
        //editor().validate();
        //      view.draw(view.getGraphics(),new FigureEnumerator(fs));
    }

    public void close() {
        // we should unregister from all objectUpdate events
        Utils.unregisterObject(classFig.getCl(),this);
        Utils.unregisterObject(classFig,this);      
    }

    //Serialization support.
    private static final long serialVersionUID = -7877776240236946511L;
    private int pertFigureSerializedDataVersion = 1;
   

    boolean isInternal() {
        return containerPackage.getClasses().contains(classFig.getCl());
    }

    Package containerPackage;

    /**
     * Get the value of containerPackage.
     * @return value of containerPackage.
     */
    public Package getContainerPackage() {
        return containerPackage;
    }
   
    /**
     * Set the value of containerPackage.
     * @param v  Value to assign to containerPackage.
     */
    public void setContainerPackage(Package  v) {
        this.containerPackage = v;
    }
      
    void initClass() {
        if (classFig.getCl() == null) {
            Log.warning("UNRESOLVED CLASS!");
        } else {
            Log.trace("figures","init class "+classFig.getCl().getName());
            DiagramView.init = true;
            try {
                removeAllMembers();
                initTitle();
                initFields();
                initMethods();
            } finally {
                DiagramView.init = false;
            }
            /*
              Wrapping.invokeRoleMethod((Wrappee)classFig.getCl(),"registerObject",
              new Object[]{this,null});
              Wrapping.invokeRoleMethod((Wrappee)classFig,"registerObject",
              new Object[]{this,null});
            */
        }
    }

    void initTitle() {
        ClassNameFigure nf = (ClassNameFigure)figures().nextFigure(); 
        nf.setSubstance(classFig.getCl());
        nf.setText(classFig.getCl().getName());
    }


    void initFields() {
        if (!classFig.isHideFields()) {
            Iterator it = classFig.getCl().getFields().iterator();
            while(it.hasNext()) {
                Field field = (Field)it.next();
                FieldFigure fieldFigure = new FieldFigure(field,this);
                Log.trace("figures","adding field "+field);
                fieldFigures.add(fieldFigure);
                add(fieldFigure);
            }
        }
    }

    void initMethods() {
        if (!classFig.isHideMethods()) {
            Iterator it = classFig.getCl().getAllMethods().iterator();
            while(it.hasNext()) {
                Method method = (Method)it.next();
                MethodFigure methodFigure = new MethodFigure(method,this);
                Log.trace("figures","adding method "+method);
                add(methodFigure);
                methodFigures.add(methodFigure);
            }
        }
    }

    void removeAllMembers() {
        FigureEnumeration it = figures();
        // skip the title
        it.nextFigure();
        while (it.hasMoreElements()) {
            Figure f = it.nextFigure();
            Log.trace("figures","removing "+f);
            remove(f);
            Wrapping.invokeRoleMethod( (Wrappee) ((ModelElementFigure)f).getSubstance(),
                                       "unregisterObject",new Object[]{this});
        }
        methodFigures.clear();
        fieldFigures.clear();
    }

    protected void basicMoveBy(int dx, int dy) {
        Log.trace("figures",2,this+".basicMoveBy "+dx+","+dy);
        classFig.translate(dx, dy);
        super.basicMoveBy(dx, dy);
    } 

    public Rectangle displayBox() {
        return new Rectangle(classFig.getCorner(),dimension);
    }

    public void basicDisplayBox(Point origin, Point corner) {
        classFig.setCorner(origin);
        dimension.width = corner.x-origin.x;
        dimension.height = corner.y-origin.y;
        layout();
    }

    protected Color getFillColor() {
        if (isInternal())
            return Color.white;
        else
            return Color.lightGray;
    }

    static final Color VERY_LIGHT_GRAY = new Color(220,220,220);

    protected Color getColor() {
        if (classFig.getCl().getContainer()!=null)
            return Color.black;
        else
            return VERY_LIGHT_GRAY;
    }

    protected void drawBorder(Graphics g) {
        Rectangle r = displayBox();
      
        g.setColor(getFillColor());
        g.fillRect(r.x, r.y, r.width, r.height);
      
        g.setColor(getColor());
        g.drawRect(r.x, r.y, r.width, r.height);
      
        Figure f = figureAt(0);
        Rectangle rf = f.displayBox();
      
        g.drawLine(r.x,r.y+rf.height+1,r.x+r.width,r.y+rf.height+1);
      
        if( fieldFigures.size() > 0 ) {
            f = (Figure) fieldFigures.get(0);
            rf = f.displayBox();
            g.drawLine(r.x,rf.y,r.x+r.width,rf.y);
        }
    }

    public void draw(Graphics g) {
        Log.trace("diagram.draw",this+".draw");
        drawBorder(g);
        super.draw(g);
    }

    public Vector handles() {
        Vector handles = new Vector();
        handles.addElement(new NullHandle(this, RelativeLocator.northWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.northEast()));
        handles.addElement(new NullHandle(this, RelativeLocator.southWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.southEast()));
        return handles;
    }

    void addRelationLinkFigure(RelationLinkFigure a) {
        relationLinkFigures.add(a);
    }

    void addInheritanceLinkFigure(InheritanceLinkFigure link) {
        inheritanceLinkFigures.add(link);
    }

    void removeInheritanceLinkFigure(InheritanceLinkFigure link) {
        inheritanceLinkFigures.remove(link);
    }

    void removeRelationLinkFigure(RelationLinkFigure a) {
        relationLinkFigures.remove(a);
    }

    public Vector getRelationLinkFigures() {
        return relationLinkFigures;
    }

    void addFieldFigure() {
        addFieldFigure("newField","void");
    }

    /**
     * Add a new field to the class
     * @param name the name  of the field
     * @param type the type of the field
     */
    void addFieldFigure(String name, String type) {
        Field f = new Field();
        f.setName(name);
        f.setType(Projects.types.resolveType(type));
        classFig.getCl().addField(f); 
    }

    /*
      void addFieldFigure(FieldFigure f) {
      fieldFigures.add(f);
      }
    */

    void addMethodFigure() {
        Method m = new Method();
        m.setName("newMethod");
        m.setType(Projects.types.resolveType("void"));
        classFig.getCl().addMethod(m); 
    }

    void addMethodFigure(MethodFigure f) {
        methodFigures.add(f);
    }

    public String getName() {
        return ((TextFigure)figures().nextFigure()).getText();
    }

    /**
     * Returns all the internal figures of this class (field and
     * method figures)
     */
    Vector getClassFigures() {
        Vector ret = new Vector();
        ret.add(figures().nextFigure());
        ret.addAll(methodFigures);
        ret.addAll(fieldFigures);
        return ret;
    }

    private void initialize() {
        //      fDisplayBox = new Rectangle(0, 0, 0, 0);
        setZValue(2);
    }

    /**
     * Compute the width of the figure, and the position of field and
     * method figures 
     */
    public void layout() {
        Point partOrigin = (Point)classFig.getCorner().clone();
        partOrigin.translate(BORDER, BORDER);
        Dimension extent = new Dimension(0, 0);
      
        Iterator it = getClassFigures().iterator();
        while(it.hasNext()) {
            Figure f = (Figure)it.next();
         
            Dimension partExtent = f.size();
            Point corner = new Point(
                partOrigin.x+partExtent.width,
                partOrigin.y+partExtent.height);
            f.basicDisplayBox(partOrigin, corner);
         
            extent.width = Math.max(extent.width, partExtent.width);
            extent.height += partExtent.height;
            partOrigin.y += partExtent.height;
        }
        dimension.width = extent.width + 2*BORDER;
        dimension.height = extent.height + 2*BORDER;
    }

    private boolean needsLayout() {
        /*		
                Dimension extent = new Dimension(0, 0);
       
                FigureEnumeration k = figures();
                while (k.hasMoreElements()) {
                Figure f = k.nextFigure();
                extent.width = Math.max(extent.width, f.size().width);
                }
                int newExtent = extent.width + 2*BORDER;
                return newExtent != fDisplayBox.width;
        */
        return true;
    }

    public void update(FigureChangeEvent e) {
        Log.trace("figures",this+".update");
        if (needsLayout()) {
            layout();
            changed();
        }
    }

    public void figureChanged(FigureChangeEvent e) {
        update(e);
    }


    public void figureRemoved(FigureChangeEvent e) {
        update(e);
    }
   
    public void release() {
        Log.trace("diagram","release "+this);
        super.release();
        close();
        if (classFig!=null && classFig.getDiagram()!=null)
            classFig.getDiagram().removeFigure(classFig);
    }

    public org.objectweb.jac.ide.ClassFigure getClassFig() {
        return classFig;
    }

    public void setClassFig(org.objectweb.jac.ide.ClassFigure classFig) {
        this.classFig = classFig;
        initClass();
        layout();
    }
   
    public Point getCorner() {
        return classFig.getCorner();
    }

    public ModelElement getSubstance() {
        return classFig.getCl();
    }

    public Class getClassElement() {
        return classFig.getCl();
    }

    // Helper methods
    public Connector connectorAt(Point p) {
        return connectorAt(p.x,p.y);
    }
}

