/*
  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli
  
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

package org.objectweb.jac.aspects.gui.swing;



import java.awt.Dimension;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JTextPane;
import org.objectweb.jac.aspects.gui.Length;

/**
 * A Swing editor component for array values.
 */

public class ArrayEditor extends JTextPane 
//   implements ValueEditor 
{
   
    /**
    * Constructs a new array editor. */

    Class type;
    public ArrayEditor(Class type) {
        setPreferredSize(new Dimension( 200, 200 ));
        this.type = type;
    }

    /**
    * Gets the value of the edited array.
    *
    * @return an array object */
 
    public Object getValue() {
        String s = getText();
        Collection c = null;
        if ( type.isArray() ) {
            c = new Vector();
        } else {
            try {
                c = (Collection) type.newInstance();
            } catch ( Exception e ) { e.printStackTrace(); }
        }
        StringTokenizer st = new StringTokenizer ( s, "\n" );
        while ( st.hasMoreTokens() ) {
            c.add( st.nextToken() );
        }
        if ( type.isArray() ) {
            Object[] a = c.toArray();
            Object array = Array.newInstance( type.getComponentType(), c.size() );
            for ( int j = 0; j < c.size(); j++ ) {
                Array.set( array, j, a[j] );
            }
            return array;
        } else {
            return c;
        }
    }

    /**
    * Sets the value of the edited array
    **/

    public void setValue(Object value) {}

    public void setSize(Length width, Length height) {
        SwingUtils.setSize(this, width, height);
    }

    public void onClose() {}
}
