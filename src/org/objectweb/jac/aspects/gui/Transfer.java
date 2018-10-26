/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrappee;

/**
 * This class defines JAC objects data tranfer utilities.
 */

public class Transfer
{
    static final Logger logger = Logger.getLogger("gui.dnd");

    /**
     * Gets the transferable representation of a set of JAC
     * objects. 
     */
    public static Transferable getJACTransfer(Wrappee[] wrappees) {
        String names="";
        for(int i=0;i<wrappees.length;i++) {
            names=names+NameRepository.get().getName(wrappees[i]);
            if(i+1<wrappees.length)
                names=names+",";
        }
        return new StringSelection(names);
    }

    /**
     * Retrieves a list of JAC objects from a transferable
     * representation made by <code>getJACTransfert</code>. 
     */
    public static List getTransferedWrappees(Transferable transferable) {
        Vector ret = new Vector();
        try {
            String names = "";
            StringTokenizer st = new StringTokenizer(
                (String)transferable.getTransferData(DataFlavor.stringFlavor),",");
            while(st.hasMoreTokens()) {
                String s = st.nextToken();
                ret.add(NameRepository.get().getObject(s));
            }
        } catch(Exception e) {
            logger.error("getTransferedWrappees "+transferable,e);
        }
        return ret;
    }
      
}

