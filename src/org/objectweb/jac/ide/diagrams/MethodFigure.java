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

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.ObjectUpdate;
import org.objectweb.jac.aspects.gui.Utils;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Method;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.util.Log;

public class MethodFigure extends MemberFigure 
    implements ModelElementFigure, ObjectUpdate, Selectable
{
    Method method;

    public MethodFigure(Method method, ClassFigure parentFigure) {
        super(parentFigure);
        this.method  = method;
        setText();
        Utils.registerObject(method,this);
    }

    public void close() {
        Utils.unregisterObject(method,this);
    }

    protected void setText() {
        super.setText(method.getName()+"("+method.getParameterNames()+")");
    }
   
    /**
     * Get the value of substance.
     * @return value of substance.
     */
    public ModelElement getSubstance() {
        return method;
    }
   
    public String getPrototype() {
        String text = getText();
        int sep = text.indexOf(':');
        if (sep == -1) {
            return "void "+text;
        } else {
            return text.substring(sep+1)+" "+text.substring(0,sep);
        }      
    }
   
    public String getName() {
        String name = super.getName();
        int sep = name.indexOf('(');
        if (sep == -1) {
            return name;
        } else {
            return name.substring(0,sep).trim();
        }      
    }

    public Vector getArgs() {
        Vector result = new Vector();
        String text = getText();
        String args = text.substring(text.indexOf('(')+1);
        args = args.substring(0,args.indexOf(')'));
        StringTokenizer st = new StringTokenizer(args,",");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            s=s.trim();
            String type = s.substring(0,s.indexOf(' '));
            String name = s.substring(s.indexOf(' ')+1);
            Log.trace("diagram","parameter: "+type+" "+name);
            result.add(new String[]{name,type});
        }
        return result;
    }

    boolean updating = false;

    public synchronized void setText(String s) {
        super.setText(s);
        Log.trace("diagram","settext("+s+")");
        Log.trace("diagram","gettext()="+getText());
        if (method != null && !DiagramView.init) {
            updating=true;
            Log.trace("diagram","gettext()="+getText());
            Log.trace("diagram","name: "+getName());
            method.setName(getName());
            Log.trace("diagram","gettext()="+getText());
            Log.trace("diagram","type: "+getType());
            method.setType(org.objectweb.jac.ide.Projects.types.resolveType(getType()));
            Iterator it = new Vector(method.getParameters()).iterator();
            while(it.hasNext()) {
                method.getParameters().remove(it.next());
            }
            it = getArgs().iterator();
            while(it.hasNext()) {
                String[] param = (String[])it.next();
                org.objectweb.jac.ide.Parameter parameter = new org.objectweb.jac.ide.Parameter();
                parameter.setName(param[0]);
                parameter.setType(org.objectweb.jac.ide.Projects.types.resolveType(param[1]));
                method.addParameter(parameter);
            }
            updating=false;
        }
    }

    // ObjectUpdate interface
    public void objectUpdated(Object object, Object extra) {
        Log.trace("diagram","objectUpdated()"+object);      
        if (!updating) {
            setText();
        }
    }

    // Selectable interface

    public void onSelect(DisplayContext context) {
        CollectionItem coll = ClassRepository.get().getClass(Class.class)
            .getCollection("methods");
        EventHandler.get().onSelection(
            context,coll,getSubstance(),null,null);
    }
}
