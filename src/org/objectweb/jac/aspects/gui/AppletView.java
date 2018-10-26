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

package org.objectweb.jac.aspects.gui;
 
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JApplet;
import org.objectweb.jac.aspects.gui.Border;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * This class allows the programmer to insert specialized object views
 * defined as applets.
 *
 * <p>This is useful when views can be used by an Internet browser at
 * client-side.
 *
 * <p>The programmer must subclass this class and overload the
 * <code>init</code> method.
 *
 * @see #init() */

public class AppletView extends JApplet 
    implements View/*, ComponentListener  */
{
    protected String label;
    protected DisplayContext context;
    protected ViewFactory factory;
    protected Object[] parameters;
    protected String type;
    protected Object substance;

    /**
     * The AppletView's constructor.
     * 
     * @param factory
     * @param context
     * @param substance the object that is represented by this view */

    public AppletView(ViewFactory factory, DisplayContext context, Object substance) {
        this.substance = substance;
        this.factory = factory;
        this.context = context;
        init();
        start();
    }

    Border viewBorder;
   
    /**
     * Get the value of viewBorder.
     * @return value of viewBorder.
     */
    public Border getViewBorder() {
        return viewBorder;
    }
   
    /**
     * Set the value of viewBorder.
     * @param v  Value to assign to viewBorder.
     */
    public void setViewBorder(Border  v) {
        this.viewBorder = v;
    }
   
    View parentView;
   
    /**
     * Get the value of parent.
     * @return value of parent.
     */
    public View getParentView() {
        return parentView;
    }
   
    /**
     * Set the value of parent.
     * @param v  Value to assign to parent.
     */
    public void setParentView(View  v) {
        this.parentView = v;
    }

    public View getRootView() {
        if (parentView==null)
            return this;
        return parentView.getRootView();      
    }

    public boolean isDescendantOf(View ancestor) {
        if (this==ancestor)
            return true;
        else if (parentView==null)
            return false;
        else
            return parentView.isDescendantOf(ancestor);
    }

    // style used to change display (css for web)
    String style;

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }


    // View interface

    Border border;
   
    /**
     * Get the value of border.
     * @return value of border.
     */
    public Border getBorder() {
        return border;
    }
   
    /**
     * Set the value of border.
     * @param v  Value to assign to border.
     */
    public void setBorder(Border  v) {
        this.border = v;
    }
   
    String description;
   
    /**
     * Get the value of description.
     * @return value of description.
     */
    public String getDescription() {
        return description;
    }
   
    /**
     * Set the value of description.
     * @param v  Value to assign to description.
     */
    public void setDescription(String  v) {
        this.description = v;
    }
   

    public void setLabel(String label) {
        this.label = label;
    }
   
    public String getLabel() {
        return label;
    }

    MethodItem message;
   
    /**
     * Get the value of message.
     * @return value of message.
     */
    public MethodItem getMessage() {
        return message;
    }
   
    /**
     * Set the value of message.
     * @param v  Value to assign to message.
     */
    public void setMessage(MethodItem  v) {
        this.message = v;
    }

    public void setContext(DisplayContext context) {
        this.context = context;
    }

    public DisplayContext getContext() {
        return context;
    }

    public void setFactory(ViewFactory factory) {
        this.factory = factory;
    }

    public ViewFactory getFactory() {
        return factory;
    }

    public void setWidth(int width) {
    }

    public void setHeight(int heigth) {
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean equalsView(ViewIdentity view) {
        return 
            ( ( type!=null && 
                type.equals(view.getType()) )
              || (type==null && view.getType()==null ) )
            && ( ( parameters!=null && 
                   Arrays.equals(parameters,view.getParameters()) ) 
                 || (parameters==null && view.getParameters()==null) );
    }

    public boolean equalsView(String type, Object[] parameters) {
        return this.type.equals(type)
            && Arrays.equals(this.parameters,parameters);
    }

    public void close(boolean validate) {
        closed = true;
    }

    boolean closed = false;

    public boolean isClosed() {
        return closed;
    }
   
    public Object getSubstance() {
        return substance;
    }
   
    public void setSubstance(Object substance) {
        this.substance = substance;
    }

    public void setFocus(FieldItem field, Object option) {
    }

    /**
     * Initializes the applet view.
     *
     * <p>The programmer must overload this class and start the init
     * method code with <code>super.init()</code> so that the applet's
     * container initialization is done. */

    public void init() {
        setStub(new InternalStub(new InternalContext(),this));
    }

    // The following classes simulate an applet container for the JAC
    // Swing GUI.

    static class InternalStub implements AppletStub {
        InternalContext context;
        public InternalStub(InternalContext context,Applet applet) {
            this.context = context;
            context.setApplet(applet);
        }
        public void appletResize(int width, int height) {
            //Log.trace("gui.applet","resizing applet");
            //applet.setSize(width,height);
        }
        public AppletContext getAppletContext() {
            return context;
        }
        public URL getCodeBase() {
            URL ret = null;
            try {
                ret = new URL("file:/");
            } catch(Exception e) {
                e.printStackTrace();
            }
            return ret;
        }
        public URL getDocumentBase() {
            URL ret = null;
            try {
                ret = new URL("file:/");
            } catch(Exception e) {
                e.printStackTrace();
            }
            return ret;
        }
        public String getParameter(String name) {
            return null;
        }
        public boolean isActive() {
            return true;
        }
    }

    static class InternalContext implements AppletContext {
        public void setApplet(Applet a) {
            v.add(a);
        }
        Vector v=new Vector();
        public Applet getApplet(String name) {
            return (Applet)v.get(0);
        }
        public Enumeration getApplets() {
            return v.elements();
        }
        public AudioClip getAudioClip(URL url) {
            return null;
        }
        public Image getImage(URL url) {
            return null;
        }
        public void showDocument(URL url) {}
        public void showDocument(URL url, String target) {}
        public void showStatus(String status) {}
        // since JDK 1.4
        Vector streamKeys=new Vector();
        public void setStream(String key,
                              InputStream stream)
            throws IOException {}
        public InputStream getStream(String key) {
            return null;
        }
        public Iterator getStreamKeys() {
            return streamKeys.iterator(); 
        }
    }

}
