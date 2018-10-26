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

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.util.Geom;

import org.objectweb.jac.ide.Class;
import org.objectweb.jac.util.Log;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

public  class RelationLinkCreationTool extends AbstractTool {

    /** the anchor point of the interaction */
    Connector   myStartConnector;
    Connector   myEndConnector;
    Connector   myTargetConnector;

    Figure myTarget;

    /** the currently created figure */
    LinkFigure  myConnection;

    public RelationLinkCreationTool(DrawingEditor newDrawingEditor) {
        super(newDrawingEditor);
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
        Log.trace("diagram","mouseDown");
        int ex = e.getX();
        int ey = e.getY();
        myTarget = findConnectionStart(ex, ey, drawing());
        Log.trace("diagram","target figure = "+myTarget);
        if (myTarget != null
            && (myTarget.getClass()==ClassFigure.class ||
                myTarget.getClass()==AspectFigure.class)) {
            myStartConnector = findConnector(ex, ey, myTarget);
            if (myStartConnector != null) {
                Point p = new Point(ex, ey);
                myConnection = createLinkFigure();
                myConnection.startPoint(p.x, p.y);
                myConnection.endPoint(p.x, p.y);
                view().add(myConnection);
            }
        }
    }

    protected LinkFigure createLinkFigure() {
        return new RelationLinkFigure();
    }

    /**
     * Adjust the created connection or split segment.
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
        Log.trace("diagram",2,"mouseDrag "+x+","+y);
        Point p = new Point(e.getX(), e.getY());
        if (myConnection != null) {
            trackConnectors(e, x, y);
            if (myTargetConnector != null) {
                p = Geom.center(myTargetConnector.displayBox());
            }
            myConnection.endPoint(p.x, p.y);
        }
    }

    /**
     * Connects the figures if the mouse is released over another
     * figure.
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        Log.trace("diagram","mouseUp "+x+","+y);
        Log.trace("diagram","  myStartConnector="+myStartConnector);
        ModelElementFigure dest = null;
        if (myStartConnector != null) {
            dest = findTarget(e.getX(), e.getY(), drawing());
            Log.trace("diagram","  dest="+dest);
        }
      
        if (dest instanceof ClassFigure) {
            myEndConnector = findConnector(e.getX(), e.getY(), dest);
            Log.trace("diagram","  myEndConnector="+myEndConnector);
            if (myEndConnector != null) {
                myConnection.connectStart(myStartConnector);
                myConnection.connectEnd(myEndConnector);
                myConnection.updateConnection();
                ClassFigure source = (ClassFigure)myStartConnector.owner();
                createRelation(source.getClassElement(),
                               ((ClassFigure)dest).getClassElement());
            }
        }
        else if (myConnection != null) {
            ((DiagramView)editor()).showStatus(
                "Invalid or empty ending element for relation.");
            view().remove(myConnection);
        }
      
        myConnection = null;
        myStartConnector = null;
        myEndConnector = null;
        editor().toolDone();
    }

    /**
     * Create a RelationLink between two classes.
     * @param source start class of the link
     * @param target end class of the link
     */
    protected void createRelation(Class source, Class target) {
        if (source != null && target != null) {
            diagramView().createRelation(source,target,(RelationLinkFigure)myConnection,false);
            view().clearSelection();
            view().addToSelection(myConnection);
        }
    }

    public void deactivate() {
        super.deactivate();
        if (myTarget != null) {
            myTarget.connectorVisibility(false);
        }
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
    protected ModelElementFigure findTarget(int x, int y, Drawing drawing) {
        ModelElementFigure target = findConnectableFigure(x, y, drawing);
        Figure start = myStartConnector.owner();

        if (target != null
            && myConnection != null
            && target.canConnect()
            && myConnection.canConnect(start, target)) {
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

    protected void trackConnectors(MouseEvent e, int x, int y) {
        Figure c = null;

        if (myStartConnector == null) {
            c = findSource(x, y, drawing());
        }
        else {
            c = findTarget(x, y, drawing());
        }

        // track the figure containing the mouse
        if (c != myTarget) {
            if (myTarget != null) {
                myTarget.connectorVisibility(false);
            }
            myTarget = c;
            if (myTarget != null) {
                myTarget.connectorVisibility(true);
            }
        }

        Connector cc = null;
        if (c != null) {
            cc = findConnector(e.getX(), e.getY(), c);
        }
        if (cc != myTargetConnector) {
            myTargetConnector = cc;
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

    private ModelElementFigure findConnectableFigure(
        int x, int y, Drawing drawing)
    {   
        FigureEnumeration k = drawing.figuresReverse();
        while (k.hasMoreElements()) {
            Figure figure = k.nextFigure();
            if (!figure.includes(myConnection) && figure.canConnect()
                && figure.containsPoint(x, y)) {
                return (ModelElementFigure)figure;
            }
        }
        return null;
    }

}
