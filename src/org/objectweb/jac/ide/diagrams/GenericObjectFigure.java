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

import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import java.awt.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.util.Log;

public class GenericObjectFigure extends CompositeFigure 
    implements ModelElementFigure
{
    static Logger logger = Logger.getLogger("figures");
    
	public void close() {}
    public static int SHAPE_RECT=0;
    public static int SHAPE_ROUNDRECT=1;

    private static final int BORDER = 3;
    private Rectangle fDisplayBox;

    Vector fieldFigures=new Vector();

    int shape=SHAPE_RECT;
   
    /**
     * Get the value of shape.
     * @return value of shape.
     */
    public int getShape() {
        return shape;
    }
   
    /**
     * Set the value of shape.
     * @param v  Value to assign to shape.
     */
    public void setShape(int  v) {
        this.shape = v;
    }

    DrawingView view;
      
    org.objectweb.jac.ide.GenericFigure genericFigure;
   
    /**
     * Get the value of genericFigure.
     * @return value of genericFigure.
     */
    public org.objectweb.jac.ide.GenericFigure getGenericFigure() {
        return genericFigure;
    }
   
    /**
     * Set the value of genericFigure.
     * @param v  Value to assign to genericFigure.
     */
    public void setGenericFigure(org.objectweb.jac.ide.GenericFigure  v) {
        this.genericFigure = v;
    }
   
    public GenericObjectFigure() {}

    public GenericObjectFigure(org.objectweb.jac.ide.GenericFigure fig, 
                               org.objectweb.jac.ide.Package pack,
                               DrawingView view) {
        this.genericFigure = fig;
        this.containerPackage = pack;
        this.view = view;
        initialize();
    }

    void initFields() {
        if (substance == null) {
            logger.warn("UNRESOLVED OBJECT!");
        } else {
            logger.debug("init generic object figure "+substance);
            FieldItem[] fields=null;
            ClassItem substClass=ClassRepository.get().getClass(substance);
            if(substClass.getAttribute("Gui.attributesOrder")!=null) {
                fields=(FieldItem[])substClass.getAttribute("Gui.attributesOrder");
            } else {
                fields=substClass.getPrimitiveFields();
            }
            for(int i=0;i<fields.length;i++) {
                if(!fields[i].isPrimitive()) continue;
                logger.debug("  adding field "+fields[i]+" subtance="+substance);
                AttributeValueFigure ff=new AttributeValueFigure(fields[i],substance);
                ff.setFont(defaultFont);
                fieldFigures.add(ff);
                add(ff);
            }
            Wrapping.invokeRoleMethod((Wrappee)substance,"addView",new Object[]{this});
        }
    }

    void removeAllMembers() {
        FigureEnumeration it = figures();
        // skip the title
        it.nextFigure();
        while (it.hasMoreElements()) {
            Figure f = it.nextFigure();
            logger.debug("removing "+f);
            remove(f);
        }
        fieldFigures.clear();
    }

    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
        super.basicMoveBy(x, y);
    } 

    public Rectangle displayBox() {
        return new Rectangle(
            fDisplayBox.x,
            fDisplayBox.y,
            fDisplayBox.width,
            fDisplayBox.height);
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin);
        fDisplayBox.add(corner);
        layout();
    }

    protected void drawBorder(Graphics g) {
      
        Rectangle r = displayBox();      
      
        g.setColor(Color.white);
        if(shape==SHAPE_RECT)
            g.fillRect(r.x, r.y, r.width, r.height);
        else if(shape==SHAPE_ROUNDRECT)
            g.fillRoundRect(r.x, r.y, r.width, r.height,7,7);
      
        g.setColor(Color.black);
        if(shape==SHAPE_RECT)
            g.drawRect(r.x, r.y, r.width, r.height);
        else if(shape==SHAPE_ROUNDRECT)
            g.drawRoundRect(r.x, r.y, r.width, r.height,7,7);

        // should be customizable

        /*  Figure f = figureAt(0);
            Rectangle rf = f.displayBox();
      
            g.drawLine(r.x,r.y+rf.height+1,r.x+r.width,r.y+rf.height+1);
      
            if( fieldFigures.size() > 0 ) {
            f = (Figure) fieldFigures.get(0);
            rf = f.displayBox();
            g.drawLine(r.x,rf.y,r.x+r.width,rf.y);
            }*/
      
    }

    public void draw(Graphics g) {
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

    private void initialize() {
        fDisplayBox = new Rectangle(0, 0, 0, 0);
    }

    Font defaultFont=new Font("Helvetica", Font.PLAIN, 10);
   
    /**
     * Get the value of defaultFont.
     * @return value of defaultFont.
     */
    public Font getDefaultFont() {
        return defaultFont;
    }
   
    /**
     * Set the value of defaultFont.
     * @param v  Value to assign to defaultFont.
     */
    public void setDefaultFont(Font  v) {
        this.defaultFont = v;
    }
   
   
    public void layout() {
        Point partOrigin = new Point(fDisplayBox.x, fDisplayBox.y);
        partOrigin.translate(BORDER, BORDER);
        Dimension extent = new Dimension(0, 0);
      
        Iterator it = fieldFigures.iterator();
      
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
        fDisplayBox.width = extent.width + 2*BORDER;
        fDisplayBox.height = extent.height + 2*BORDER;
    }

    private boolean needsLayout() {
        /*		Dimension extent = new Dimension(0, 0);

                FigureEnumeration k = figures();
                while (k.hasMoreElements()) {
                Figure f = k.nextFigure();
                extent.width = Math.max(extent.width, f.size().width);
                }
                int newExtent = extent.width + 2*BORDER;
                return newExtent != fDisplayBox.width;*/
        return true;
    }

    public void update(FigureChangeEvent e) {
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

    Object substance;
   
    // View interface
    /*
      public void refreshViewItem(org.objectweb.jac.core.rtti.FieldItem field, 
      org.objectweb.jac.core.rtti.MethodItem method, Object[] args) {
      refreshView();
      }

      public void setFocus(org.objectweb.jac.core.rtti.FieldItem field, Object extraOption) {}

      public void refreshView() {
      if( DiagramView.init ) return;
      DiagramView.init = true;
      removeAllMembers();
      initFields();
      layout();
      changed();
      DiagramView.refreshFigure(this);
      //editor().validate();
      DiagramView.init = false;
      }

      public void close() {}
    */

    /**
     * Get the value of substance.
     * @return value of substance.
     */
    public ModelElement getSubstance() {
        return (ModelElement)substance;
    }
   
    /**
     * Set the value of substance.
     * @param v  Value to assign to substance.
     */
    public void setSubstance(Object  v) {
        this.substance = v;
    }

    CollectionItem collection;
   
    /**
     * Get the value of collection.
     * @return value of collection.
     */
    public CollectionItem getCollection() {
        return collection;
    }
   
    /**
     * Set the value of collection.
     * @param v  Value to assign to collection.
     */
    public void setCollection(CollectionItem v) {
        this.collection = v;
    }
   
    org.objectweb.jac.ide.Package containerPackage;

    /**
     * Get the value of containerPackage.
     * @return value of containerPackage.
     */
    public org.objectweb.jac.ide.Package getContainerPackage() {
        return containerPackage;
    }
   
    /**
     * Set the value of containerPackage.
     * @param v  Value to assign to containerPackage.
     */
    public void setContainerPackage(org.objectweb.jac.ide.Package  v) {
        this.containerPackage = v;
    }

}
