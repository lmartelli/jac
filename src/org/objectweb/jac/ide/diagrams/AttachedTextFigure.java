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

import CH.ifa.draw.framework.Figure;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.ide.RelationLink;
import org.objectweb.jac.ide.RelationRole;
import org.objectweb.jac.util.Log;

/**
 * Text attached to relations (relation name, cardinalities and role
 * names)
 */

public class AttachedTextFigure extends TextFigure 
    implements ModelElementFigure 
{

    public final static int NAME = 0;
    public final static int START_CARDINALITY = 1;
    public final static int END_CARDINALITY = 2;
    public final static int START_ROLE = 3;
    public final static int END_ROLE = 4;

    public void close() {}

    public static AttachedTextFigure createName(org.objectweb.jac.ide.LinkFigure lf) {
        return new AttachedTextFigure(lf,NAME);
    }

    public static AttachedTextFigure createStartCardinality(org.objectweb.jac.ide.LinkFigure lf) {
        return new AttachedTextFigure(lf,START_CARDINALITY);
    }

    public static AttachedTextFigure createEndCardinality(org.objectweb.jac.ide.LinkFigure lf) {
        return new AttachedTextFigure(lf,END_CARDINALITY);
    }

    public static AttachedTextFigure createStartRole(org.objectweb.jac.ide.LinkFigure lf) {
        return new AttachedTextFigure(lf,START_ROLE);
    }

    public static AttachedTextFigure createEndRole(org.objectweb.jac.ide.LinkFigure lf) {
        return new AttachedTextFigure(lf,END_ROLE);
    }

    org.objectweb.jac.ide.LinkFigure linkFig;

    public ModelElement getSubstance() {
        return linkFig.getElement();
    }

    public AttachedTextFigure() {}

    public AttachedTextFigure(org.objectweb.jac.ide.LinkFigure linkFig,int type) {
        this.linkFig = linkFig;
        this.type = type;
    }

    public void setText(String s) {
        if (s==null) 
            return;
        super.setText(s);
        if (DiagramView.init) 
            return;
        RelationLink substance = (RelationLink)linkFig.getLink();
        Log.trace("diagram","setText "+s+", type="+type+
                  ", substance="+substance);
        if (substance != null) {
            RelationRole start = substance.startRole();
            RelationRole end = substance.endRole();
            switch (type) {
                case NAME:
                    substance.setName(s);
                    break;
                case END_ROLE:
                    end.setName(s);
                    break;
                case END_CARDINALITY:
                    end.setCardinality(s);
                    break;
                case START_ROLE:
                    start.setName(s);
                    break;
                case START_CARDINALITY:
                    start.setCardinality(s);
                    break;
            }
        }
    }

    public void refresh() {
        RelationLink roledLink = (RelationLink)linkFig.getLink();
        if (roledLink != null) {
            RelationRole start = roledLink.startRole();
            RelationRole end = roledLink.endRole();
            int orientation = roledLink.getOrientation();
            switch (type) {
                case NAME:
                    if (roledLink.getName()!=null)
                        super.setText(roledLink.getName());
                    break;
                case END_ROLE:
                    if (end.getName()!=null &&
                        orientation!=RelationLink.ORIENTATION_STRAIGHT)
                        super.setText(end.getName());
                    else
                        super.setText("");
                    break;
                case END_CARDINALITY:
                    if (end.getCardinality()!=null &&
                        orientation!=RelationLink.ORIENTATION_STRAIGHT)
                        super.setText(end.getCardinality());
                    else
                        super.setText("");
                    break;
                case START_ROLE:
                    if (start.getName()!=null &&
                        orientation!=RelationLink.ORIENTATION_REVERSE)
                        super.setText(start.getName());
                    else
                        super.setText("");
                    break;
                case START_CARDINALITY:
                    if (start.getCardinality()!=null && 
                        orientation!=RelationLink.ORIENTATION_REVERSE)
                        super.setText(start.getCardinality());
                    else
                        super.setText("");
                    break;
            }
        }
    }

    protected void basicMoveBy(int dx, int dy) {
        if(!DiagramView.init) {
            Log.trace("diagram",2,this+".basicMoveBy "+dx+","+dy);
            //new Exception().printStackTrace();
            switch(type) {
                case NAME:
                    linkFig.translateName(dx,dy);
                    break;
                case START_ROLE:
                    linkFig.translateStartRole(dx,dy);
                    break;
                case START_CARDINALITY:
                    linkFig.translateStartCardinality(dx,dy);
                    break;
                case END_ROLE:
                    linkFig.translateEndRole(dx,dy);
                    break;
                case END_CARDINALITY:
                    linkFig.translateEndCardinality(dx,dy);
                    break;
            }
        }
        super.basicMoveBy(dx, dy);
    } 

    int type = -1;
    
    /**
     * Get the value of type.
     * @return value of type.
     */
    public int getType() {
        return type;
    }
   
    /**
     * Set the value of type.
     * @param v  Value to assign to type.
     */
    public void setType(int  v) {
        this.type = v;
    }

    LinkFigure connectedLink;
   
    /**
     * Get the value of connectedLink.
     * @return value of connectedLink.
     */
    public LinkFigure getConnectedLink() {
        return connectedLink;
    }

    /**
     * Set the value of connectedLink.
     * @param v  Value to assign to connectedLink.
     */
    public void setConnectedLink(LinkFigure  v) {
        this.connectedLink = v;
    }

    public void connect(Figure f) {
        Log.trace("diagram","connecting text figure, type= "+type);
        super.connect(f);
        ((LinkFigure)f).addAttachedTextFigure(this);
        connectedLink = (LinkFigure)f;
    }

}
