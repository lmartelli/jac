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

import CH.ifa.draw.figures.ArrowTip;
import org.objectweb.jac.ide.RelationLink;
import java.awt.Color;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.aspects.gui.EventHandler;


public class RelationLinkFigure extends LinkFigure {

   public RelationLinkFigure(org.objectweb.jac.ide.LinkFigure figure) {
      super(figure);
   }
   public RelationLinkFigure() {
   }

   void setDecorations() {
      if (linkFig==null) {
         setStartDecoration(null);
         setEndDecoration(null);
         return;
      }
      RelationLink link = (RelationLink)linkFig.getLink();
      switch (link.getOrientation()) {
         case RelationLink.ORIENTATION_STRAIGHT:
            setStartDecoration(null);
            setEndDecoration(new ArrowTip());
            break;
         case RelationLink.ORIENTATION_REVERSE:
            setStartDecoration(new ArrowTip());
            setEndDecoration(null);
            break;
         default:
            setStartDecoration(null);
            setEndDecoration(null);
      }
      if (link.isAggregation()) {
         ArrowTip arrow = new ArrowTip(0.50, 15, 28);
         arrow.setFillColor(Color.white);
         setStartDecoration(arrow);
      }
   }

   // Selectable interface
   public void onSelect(DisplayContext context) {
      CollectionItem coll = ClassRepository.get().getClass(Class.class)
         .getCollection("links");
      EventHandler.get().onSelection(
         context,coll,getSubstance(),null,null);
   }
}
