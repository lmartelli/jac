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

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.AbstractTool;
import CH.ifa.draw.util.Geom;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.PointcutLink;
import org.objectweb.jac.ide.Diagram;
import org.objectweb.jac.util.Log;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

public  class PointcutLinkCreationTool extends AbstractTool {

    /**
    * the anchor point of the interaction
    */
    private Connector   myStartConnector;
    private Connector   myEndConnector;
    private Connector   myTargetConnector;

    private Figure myTarget;

   /**
    * the currently created figure
    */
    private ConnectionFigure  myConnection;

    /**
    * the figure that was actually added
    * Note, this can be a different figure from the one which has been created.
    */
    private Figure myAddedFigure;

    /**
    * the prototypical figure that is used to create new
    * connections.
    */
    private ConnectionFigure  fPrototype;


    public PointcutLinkCreationTool(DrawingEditor newDrawingEditor, ConnectionFigure newPrototype) {
        super(newDrawingEditor);
        fPrototype = newPrototype;
    }

    /**
    * Handles mouse move events in the drawing view.
    */
    public void mouseMove(MouseEvent e, int x, int y) {
        trackConnectors(e, x, y);
    }

    /**
    * Manipulates connections in a context dependent way. If the
    * mouse down hits a figure start a new connection. If the mousedown
    * hits a connection split a segment or join two segments.
    */
    public void mouseDown(MouseEvent e, int x, int y)
    {
        int ex = e.getX();
        int ey = e.getY();
        setTargetFigure(findConnectionStart(ex, ey, drawing()));
        if (getTargetFigure() != null
            && getTargetFigure().getClass()==AspectFigure.class) {
            setStartConnector(findConnector(ex, ey, getTargetFigure()));
            if (getStartConnector() != null) {
                Point p = new Point(ex, ey);
                setConnection(createConnection());
                getConnection().startPoint(p.x, p.y);
                getConnection().endPoint(p.x, p.y);
                setAddedFigure(view().add(getConnection()));
            }
        }
    }

    /**
    * Adjust the created connection or split segment.
    */
    public void mouseDrag(MouseEvent e, int x, int y) {
        Point p = new Point(e.getX(), e.getY());
        if (getConnection() != null) {
            trackConnectors(e, x, y);
            if (getTargetConnector() != null) {
                p = Geom.center(getTargetConnector().displayBox());
            }
            getConnection().endPoint(p.x, p.y);
        }
    }

    /**
    * Connects the figures if the mouse is released over another
    * figure.
    */
    public void mouseUp(MouseEvent e, int x, int y) {
        Figure c = null;
        if (getStartConnector() != null) {
            c = findTarget(e.getX(), e.getY(), drawing());
        }
      
        if (c != null && (c.getClass()==ClassFigure.class ||
                          c.getClass()==InstanceFigure.class)) {
            setEndConnector(findConnector(e.getX(), e.getY(), c));
            if (getEndConnector() != null) {
                getConnection().connectStart(getStartConnector());
                getConnection().connectEnd(getEndConnector());
                getConnection().updateConnection();
            
                /*
                  setUndoActivity(createUndoActivity());
                  getUndoActivity().setAffectedFigures(
                  new SingleFigureEnumerator(getAddedFigure()));
                */
                AspectFigure source = (AspectFigure)getStartConnector().owner();
                ModelElementFigure target = (ModelElementFigure)c;
                Log.trace("figures","creating a new poincut link between "+
                          source.getSubstance()+" and "+target.getSubstance());
            
                createRelation(source.getClassElement(),target.getSubstance());
            
            }
        }
        else if (getConnection() != null) {
            ((DiagramView)editor()).showStatus("Invalid or empty ending element for relation.");
            view().remove(getConnection());
        }
      
        setConnection(null);
        setStartConnector(null);
        setEndConnector(null);
        setAddedFigure(null);
        editor().toolDone();
    }


    protected void createRelation(org.objectweb.jac.ide.Class source, ModelElement target) {

        if(source != null && target != null ) {
            PointcutLink rel = new PointcutLink(source,(Class)target);
            // ***         ((LinkFigure)getConnection()).setSubstance(rel);
            LinkFigure linkFigure = (LinkFigure)getConnection();
            org.objectweb.jac.ide.LinkFigure linkFig = 
                new org.objectweb.jac.ide.LinkFigure(rel);
            linkFigure.setLinkFigure(linkFig);

            view().add(linkFigure.createName("newPointcut"));
            view().add(linkFigure.createEndRole("ALL:ALL"));
            view().add(linkFigure.createStartRole("?"));
            //view().add(((LinkFigure)getConnection()).createStartCardinality("0-1"));
            //view().add(((LinkFigure)getConnection()).createEndCardinality("0-1"));
            ((org.objectweb.jac.ide.Aspect)source).addPointcutLink(rel);

            Diagram diagram = (Diagram)((DiagramView)editor()).getSubstance();

            diagram.addFigure(linkFig);

            Wrapping.invokeRoleMethod((Wrappee)rel,"registerObject",
                                      new Object[]{getConnection(),null});
        }
    }

    public void deactivate() {
        super.deactivate();
        if (getTargetFigure() != null) {
            getTargetFigure().connectorVisibility(false);
        }
    }

    /**
    * Creates the ConnectionFigure. By default the figure prototype is
    * cloned.
    */
    protected ConnectionFigure createConnection() {
        return (ConnectionFigure)fPrototype.clone();
    }
   
    /**
    * Finds a connectable figure target.
    */
    protected Figure findSource(int x, int y, Drawing drawing) {
        return findConnectableFigure(x, y, drawing);
    }
   
    /**
    * Finds a connectable figure target.
    */
    protected Figure findTarget(int x, int y, Drawing drawing) {
        Figure target = findConnectableFigure(x, y, drawing);
        Figure start = getStartConnector().owner();
      
        if (target != null
            && getConnection() != null
            && target.canConnect()
            && !target.includes(start)
            && getConnection().canConnect(start, target)) {
            return target;
        }
        return null;
    }
   
    /**
    * Finds an existing connection figure.
    */
    protected ConnectionFigure findConnection(int x, int y, Drawing drawing) {
        Enumeration k = drawing.figuresReverse();
        while (k.hasMoreElements()) {
            Figure figure = (Figure) k.nextElement();
            figure = figure.findFigureInside(x, y);
            if (figure != null && (figure instanceof ConnectionFigure)) {
                return (ConnectionFigure)figure;
            }
        }
        return null;
    }
   
    private void setConnection(ConnectionFigure newConnection) {
        myConnection = newConnection;
    }
   
    /**
    * Gets the connection which is created by this tool
    */
    protected ConnectionFigure getConnection() {
        return myConnection;
    }
   
    protected void trackConnectors(MouseEvent e, int x, int y) {
        Figure c = null;
      
        if (getStartConnector() == null) {
            c = findSource(x, y, drawing());
        }
        else {
            c = findTarget(x, y, drawing());
        }
      
        // track the figure containing the mouse
        if (c != getTargetFigure()) {
            if (getTargetFigure() != null) {
                getTargetFigure().connectorVisibility(false);
            }
            setTargetFigure(c);
            if (getTargetFigure() != null) {
                getTargetFigure().connectorVisibility(true);
            }
        }
      
        Connector cc = null;
        if (c != null) {
            cc = findConnector(e.getX(), e.getY(), c);
        }
        if (cc != getTargetConnector()) {
            setTargetConnector(cc);
        }
      
        view().checkDamage();
    }
   
    private Connector findConnector(int x, int y, Figure f) {
        return f.connectorAt(x, y);
    }
   
    /**
    * Finds a connection start figure.
    */
    protected Figure findConnectionStart(int x, int y, Drawing drawing) {
        Figure target = findConnectableFigure(x, y, drawing);
        if ((target != null) && target.canConnect()) {
            return target;
        }
        return null;
    }
   
    private Figure findConnectableFigure(int x, int y, Drawing drawing) {
        FigureEnumeration k = drawing.figuresReverse();
        while (k.hasMoreElements()) {
            Figure figure = k.nextFigure();
            if (!figure.includes(getConnection()) && figure.canConnect()
                && figure.containsPoint(x, y)) {
                return figure;
            }
        }
        return null;
    }
   
    private void setStartConnector(Connector newStartConnector) {
        myStartConnector = newStartConnector;
    }
   
    protected Connector getStartConnector() {
        return myStartConnector;
    }
   
    private void setEndConnector(Connector newEndConnector) {
        myEndConnector = newEndConnector;
    }
   
    protected Connector getEndConnector() {
        return myEndConnector;
    }

    private void setTargetConnector(Connector newTargetConnector) {
        myTargetConnector = newTargetConnector;
    }
	
    protected Connector getTargetConnector() {
        return myTargetConnector;
    }
	
    private void setTargetFigure(Figure newTarget) {
        myTarget = newTarget;
    }
	
    protected Figure getTargetFigure() {
        return myTarget;
    }

    /**
    * Gets the figure that was actually added
    * Note, this can be a different figure from the one which has been created.
    */
    protected Figure getAddedFigure() {
        return myAddedFigure;
    }

    private void setAddedFigure(Figure newAddedFigure) {
        myAddedFigure = newAddedFigure;
    }

}
