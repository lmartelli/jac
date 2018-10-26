/*
  Copyright (C) 2002 Renaud Pawlak

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
  USA. */

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.ide.Instance;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.ide.Package;
import org.objectweb.jac.util.Log;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

public class InstanceFigure extends CompositeFigure 
    implements ModelElementFigure 
{
    private static final int BORDER = 3;
    private Rectangle fDisplayBox;
   
    Instance substance;

    /*
      public void refreshView() {
      if( DiagramView.init ) return;
      DiagramView.init = true;
      initInstance();
      layout();
      changed();
      DiagramView.refreshFigure(this);
      //editor().validate();
      DiagramView.init = false;
      }
    */

    public void close() {}

    /**
     * Get the value of substance.
     * @return value of substance.
     */
    public ModelElement getSubstance() {
        return substance;
    }
   
    /**
     * Set the value of substance.
     * @param v  Value to assign to substance.
     */
    public void setSubstance(Instance  v) {
        this.substance = v;
    }

    public String getType() {
        return substance.getType().getName();
    }
   
    public InstanceFigure() {
        initialize();
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
      
    void initInstance() {
        if (substance == null) {
            Log.warning("UNRESOLVED INSTANCE!");
        } else {
            Log.trace("figures","init class "+substance);
            initTitle();
            Wrapping.invokeRoleMethod((Wrappee)substance,"addView",new Object[]{this});
        }
    }

    void initTitle() {
        InstanceNameFigure nf = (InstanceNameFigure)figures().nextFigure(); 
        nf.setSubstance(substance);
        nf.setText(substance.toString());
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
        g.fillRoundRect(r.x, r.y, r.width, r.height,7,7);
      
        g.setColor(Color.black);
        g.drawRoundRect(r.x, r.y, r.width, r.height,7,7);
      
        //Figure f = figureAt(0);
        //Rectangle rf = f.displayBox();
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

    public String getName() {
        return ((TextFigure)figures().nextFigure()).getText();
    }

    void initName() {
        InstanceNameFigure name=new InstanceNameFigure();
        name.setText("NewInstance");
        add(name);
    }

    private void initialize() {
      
        fDisplayBox = new Rectangle(0, 0, 0, 0);
      
        Font f = new Font("Helvetica", Font.PLAIN, 12);
        Font fb = new Font("Helvetica", Font.BOLD, 12);
      
        initName();

    }
   
    public void layout() {
        Point partOrigin = new Point(fDisplayBox.x, fDisplayBox.y);
        partOrigin.translate(BORDER, BORDER);
        Dimension extent = new Dimension(0, 0);


        Dimension partExtent = figureAt(0).size();
        Point corner = new Point(
            partOrigin.x+partExtent.width,
            partOrigin.y+partExtent.height);
        figureAt(0).basicDisplayBox(partOrigin, corner);
         
        extent.width = partExtent.width;
        extent.height = partExtent.height;
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
        /*if (e.getFigure() == figureAt(1)) {
          // duration has changed
          //updateDurations();
          }*/
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

}
