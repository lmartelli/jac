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
  USA. */

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.PolyLineFigure;
import CH.ifa.draw.figures.PolyLineHandle;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Locator;
import CH.ifa.draw.standard.NullHandle;
import org.objectweb.jac.aspects.gui.ObjectUpdate;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.ide.Link;
import org.objectweb.jac.ide.RelationLink;
import org.objectweb.jac.ide.RelationRole;
import org.objectweb.jac.ide.Role;
import org.objectweb.jac.util.Log;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.Utils;

public class LinkFigure extends LineConnection 
    implements ObjectUpdate, ModelElementFigure
{

    public final static int NAME = AttachedTextFigure.NAME;
    public final static int START_CARDINALITY = AttachedTextFigure.START_CARDINALITY;
    public final static int END_CARDINALITY = AttachedTextFigure.END_CARDINALITY;
    public final static int START_ROLE = AttachedTextFigure.START_ROLE;
    public final static int END_ROLE = AttachedTextFigure.END_ROLE;

    List attachedFigures = new Vector();

    org.objectweb.jac.ide.LinkFigure linkFig;

    org.objectweb.jac.ide.LinkFigure getLinkFigure() {
        return linkFig;
    }

    public LinkFigure(org.objectweb.jac.ide.LinkFigure figure) {
        setLinkFigure(figure);
        setDecorations();
        layout();
    }

    public LinkFigure() {
        setDecorations();
    }

    /**
     * Set the decorations at both end of the link
     */
    void setDecorations() {
        setStartDecoration(null);
        setEndDecoration(null);
    }

    protected void layout() {
      
    } 

    public void addAttachedTextFigure(AttachedTextFigure f) {
        attachedFigures.add(f);
    }

    public AttachedTextFigure createAttachedFigure(int type) {
        AttachedTextFigure at = new AttachedTextFigure(linkFig,type);
        at.connect(this);
        at.refresh();
        return at;    
    }

    public AttachedTextFigure createAttachedFigure(int type,String s) {
        AttachedTextFigure at = new AttachedTextFigure(linkFig,type);
        at.setText(s);
        at.connect(this);
        return at;    
    }

    public AttachedTextFigure createName() {
        return createAttachedFigure(NAME);
    }

    public AttachedTextFigure createEndRole() {
        return createAttachedFigure(END_ROLE);
    }

    public AttachedTextFigure createStartRole() {
        return createAttachedFigure(START_ROLE);
    }

    public AttachedTextFigure createStartCardinality() {
        return createAttachedFigure(START_CARDINALITY);
    }

    public AttachedTextFigure createEndCardinality() {
        return createAttachedFigure(END_CARDINALITY);
    }


    public AttachedTextFigure createName(String s) {
        return createAttachedFigure(NAME,s);
    }

    public AttachedTextFigure createEndRole(String s) {
        return createAttachedFigure(END_ROLE,s);
    }

    public AttachedTextFigure createStartRole(String s) {
        return createAttachedFigure(START_ROLE,s);
    }

    public AttachedTextFigure createStartCardinality(String s) {
        return createAttachedFigure(START_CARDINALITY,s);
    }

    public AttachedTextFigure createEndCardinality(String s) {
        return createAttachedFigure(END_CARDINALITY,s);
    }

    Link substance;

    // View interface
    public void setFocus(FieldItem field, Object extraOption) {}

    public void objectUpdated(Object object, Object extra) {
        if (DiagramView.init) 
            return;
        Log.trace("figures.link","objectUpdated("+object+")");
        DiagramView.init = true;
        Link link = null;
        if (object instanceof Link) {
            link = (Link)object;
        } else if (object instanceof Role) {
            link = linkFig.getLink();
        } else {
            link = ((org.objectweb.jac.ide.LinkFigure)object).getLink();
        }
      
        try {
            if (link instanceof RelationLink) {
                RelationLink roledLink = (RelationLink)link;
                Iterator it = attachedFigures.iterator();
                while (it.hasNext()) {
                    AttachedTextFigure cur = (AttachedTextFigure)it.next();
                    cur.refresh();
                }
            }
         
            setDecorations();
            changed();
            /*
              if (substance.getEnd() != ((ModelElementFigure)endFigure()).getSubstance()) {
              Figure newEnd = ((DiagramView)DiagramView.diagramViews.get(0))
              .findElement((ModelElement)substance.getEnd());
              if( newEnd != null ) {
              Log.trace("figures","reconnecting relation to "+newEnd);
              disconnectEnd();
              endPoint(newEnd.center().x,newEnd.center().y);
              connectEnd(newEnd.connectorAt(
              newEnd.center().x,newEnd.center().y));
              updateConnection();
              } else {
              ((DiagramView)DiagramView.diagramViews.get(0)).view().drawing().remove(this);            
              }
              }
              if(DiagramView.diagramViews.size()>0) {
              ((DiagramView)DiagramView.diagramViews.get(0)).view()
              .paint(((DiagramView)DiagramView.diagramViews.get(0))
              .view().getGraphics());
              }
            */
        } finally {
            DiagramView.init = false;
        }
    }

    public void insertPointAt(Point p, int i) {
        super.insertPointAt(p, i);
        if (!DiagramView.init)
            linkFig.addPoint(i,p);
    }

    public void removePointAt(int i) {
        super.removePointAt(i);
        if (!DiagramView.init)
            linkFig.removePoint(i);
    }

    public void close() {
        if (linkFig!=null) {
            Link link = linkFig.getLink();
            Utils.unregisterObject(link,this);
            if (link instanceof RelationLink) {
                RelationLink rel = (RelationLink)link;
                Utils.unregisterObject(rel.getStartRole(),this);
                Utils.unregisterObject(rel.getEndRole(),this);
            }
        }
    }
   
    /**
     * Get the value of substance.
     * @return value of substance.
     */
    public org.objectweb.jac.ide.ModelElement getSubstance() {
        return linkFig.getLink();
    }

    public void setLinkFigure(org.objectweb.jac.ide.LinkFigure linkFig) {
        Link link;
        if (this.linkFig!=null) {
            link = this.linkFig.getLink();
            Utils.unregisterObject(link,this);
            if (link instanceof RelationLink) {
                RelationLink rel = (RelationLink)link;
                Utils.unregisterObject(rel.getStartRole(),this);
                Utils.unregisterObject(rel.getEndRole(),this);
            }
        }
        this.linkFig = linkFig;
        link = linkFig.getLink();
        Utils.registerObject(link,this);
        if (link instanceof RelationLink) {
            RelationLink rel = (RelationLink)link;
            Utils.registerObject(rel.getStartRole(),this);
            Utils.registerObject(rel.getEndRole(),this);
        }
        setDecorations();
    }

    public void handleConnect(Figure start, ModelElementFigure end) {
        if (substance == null) return;
        if (end.getSubstance() != substance.getEnd()) {
            DiagramView.init = true;
            substance.setEnd(end.getSubstance());
            DiagramView.init = false;
        }
    }

    public void handleDisconnect(Figure start, Figure end) {
    } 

    public Vector handles() {
      
        Vector handles = new Vector(fPoints.size());
        for (int i = 0; i < fPoints.size(); i++) {
            handles.addElement(new LinkHandle(this, locator(i), i));
        }
        // don't allow to reconnect the starting figure
        handles.setElementAt(new NullHandle(this, PolyLineFigure.locator(0)), 0);
        return handles;
    }

    AttachedTextLocator locator=new AttachedTextLocator();

    public Locator connectedTextLocator(Figure f) {
        return locator;
    }

    protected void basicMoveBy(int dx, int dy) {
        Log.trace("figures.link","link figure moved "+dx+","+dy);
        for(int i=1;i<linkFig.getPointCount()-1;i++) {
            linkFig.setPoint(i,pointAt(i));
        }     
        super.basicMoveBy(dx,dy);
        Log.trace("figures.link","Moved done");
    }

    /**
     * Changes the coordinates of a point of the line connection
     * @param p the new point
     * @param i  index of the point to modify
     */
    public void setPointAt(Point p, int i) {
        super.setPointAt(p,i);
        linkFig.setPoint(i,p);
    }


    /**
     * Updates the connection.
     */
    public void updateConnection() {
        if (getStartConnector() != null && getEndConnector() != null) {
            Point start = getStartConnector().findStart(this);

            if (start != null) {
                startPoint(start.x, start.y);
            }

            Point end = getEndConnector().findEnd(this);
	  
            if (end != null) {
                endPoint(end.x, end.y);
            }
        }
    }

    /**
     * Initialize associated text figures (cardinality, role name, ...)
     * @param drawing a drawing to add the figures to
     */
    public void load(Drawing drawing) {
        Log.trace("diagram",2,"load link "+this);
        DiagramView.init = true;
        try {
            Iterator points = linkFig.getPoints().iterator();
            int i = 1;
            if (points.hasNext()) {
                points.next();
                while (points.hasNext()) {
                    Point p = (Point)points.next();
                    if (points.hasNext())
                        insertPointAt(p,i++);
                }
            }
            Link subst = (Link)getSubstance();
            Point p;
            if (subst instanceof RelationLink) {
                RelationLink lnk = (RelationLink)subst;
                RelationRole start = lnk.startRole();
                RelationRole end = lnk.endRole();
                AttachedTextFigure text = createName();
                drawing.add(text);
                p = connectedTextLocator(text).locate(this,text);
                text.moveBy((int)(linkFig.getNameCorner().getX()
                                  - p.getX()+text.size().getWidth()/2),
                            (int)(linkFig.getNameCorner().getY()
                                  - p.getY()+text.size().getHeight()/2));
            
                text = createStartRole();
                drawing.add(text);
                p = connectedTextLocator(text).locate(this,text);
                text.moveBy((int)(linkFig.getStartRoleCorner().getX()
                                  - p.getX()+text.size().getWidth()/2),
                            (int)(linkFig.getStartRoleCorner().getY()
                                  - p.getY()+text.size().getHeight()/2));
            
                text = createEndRole();
                drawing.add(text);
                p = connectedTextLocator(text).locate(this,text);
                text.moveBy((int)(linkFig.getEndRoleCorner().getX()
                                  - p.getX()+text.size().getWidth()/2),
                            (int)(linkFig.getEndRoleCorner().getY()
                                  - p.getY()+text.size().getHeight()/2));
            
                text = createStartCardinality();
                drawing.add(text);
                p = connectedTextLocator(text).locate(this,text);
                text.moveBy((int)(linkFig.getStartCardinalityCorner().getX()
                                  - p.getX()+text.size().getWidth()/2),
                            (int)(linkFig.getStartCardinalityCorner().getY()
                                  - p.getY()+text.size().getHeight()/2));
            
                text = createEndCardinality();
                drawing.add(text);
                p = connectedTextLocator(text).locate(this,text);
                text.moveBy((int)(linkFig.getEndCardinalityCorner().getX()
                                  - p.getX()+text.size().getWidth()/2),
                            (int)(linkFig.getEndCardinalityCorner().getY()
                                  - p.getY()+text.size().getHeight()/2));
            }
        } finally {
            DiagramView.init=false;
        }
    }

    public void release() {
        super.release();
        close();
        if(linkFig!=null && linkFig.getDiagram()!=null) {
            linkFig.getDiagram().removeFigure(linkFig);
        }
    }

    // Helper methods
    public void endPoint(Point p) {
        endPoint(p.x,p.y);
    }
    public void startPoint(Point p) {
        startPoint(p.x,p.y);
    }
}

class LinkHandle extends PolyLineHandle {
    public LinkHandle(PolyLineFigure owner, Locator l, int index) {
        super(owner, l,index);
    }

    public void invokeStep(int x, int y, int anchorX, 
                           int anchorY, DrawingView view) {
        super.invokeStep(x,y,anchorX,anchorY,view);
    }
   
}
