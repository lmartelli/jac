/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>
                          Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Semaphore;
import org.objectweb.jac.util.Strings;

/**
 * This dialog is used to ask the parameters values when a method is
 * called on a viewed JAC object.<p>
 *
 * @see View
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a>
 */
public class Dialog extends JDialog 
    implements ActionListener, KeyListener, ContainerListener, DialogView
{
    static Logger loggerEvents = Logger.getLogger("gui.events");

    String label;
    DisplayContext context;
    Length width;
    Length height;
    ViewFactory factory;
    Object[] parameters;
    String type;

    boolean ok = false;

    private JButton okButton;
    private JButton cancelButton;

    Semaphore semaphore = new Semaphore();
    String description;
    View contentView;

   /**
    * Construct a dialog window.
    *
    * @param content the content of the dialog
    * @param parent the parent window of the dialog
    * @param description a text describing the dialog to the user
    */
    public Dialog(View content, Object parent,
                  String title, String description) {
        this.description = description;
        this.contentView = content;
        setModal(true);
        setTitle(title);

        Container contentPane = getContentPane();

        addWindowListener( new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            }
        );

        contentPane.add((Component)content, BorderLayout.CENTER);
        content.setParentView(this);

        if (description != null)
        {
            JEditorPane descr = new JEditorPane("text/plain",description);
            descr.setEditable(false);
            descr.setBackground(null);
            contentPane.add(descr,BorderLayout.NORTH);
        }

        // Buttons panel
        JPanel p2 = new JPanel();
        p2.setBorder(BorderFactory.createEtchedBorder());
        okButton = addButton(p2,"Ok");
        cancelButton = addButton(p2,"Cancel");
        getRootPane().setDefaultButton(okButton);
        contentPane.add(p2,BorderLayout.SOUTH);
        pack();

        // open the box centerd in the screen...
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rect = getBounds();
        double left = (screenDim.getWidth()-rect.getWidth())/2;
        double top = (screenDim.getHeight()-rect.getHeight())/2;
        Rectangle newRect = new Rectangle(
            (int)left,(int)top,
            (int)rect.getWidth(),(int)rect.getHeight());
        setBounds(newRect);
      
        addKeyAndContainerListenerRecursively(this);

        attributes = Collaboration.get().getAttributes();   

        setVisible (true);
        // Do not place anything after this, since this a blocking call
    }

    /** Stores context attributes at creation time so they can be
        restored by components when invoking methods */
    Map attributes;

    // View interface

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
   
    // style used to change display (css for web)
    String style;

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
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
   
    View parentView;
   
   /**
    * Get the value of parentView.
    * @return value of parentView.
    */
    public View getParentView() {
        return parentView;
    }
   
    /**
    * Set the value of parentView.
    * @param v  Value to assign to parentView.
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

    public void setLabel(String label) {
        this.label = label;
        setTitle(label);
    }

    public String getLabel() {
        return label;
    }

    public void setSize(Length width, Length height) {
        this.width = width;
        this.height = height;
        //SwingUtils.setSize(this,width,height);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
   
    public Object[] getParameters() {
        return parameters;
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
        contentView.close(validate);
        closed = true;
        dispose();
    }

    boolean closed = false;

    public boolean isClosed() {
        return closed;
    }

    public void setFocus(FieldItem field, Object option) {
    }

    // DialogView interface

    public boolean waitForClose() {
        loggerEvents.debug("waiting for "+Strings.hex(this)+" to be closed");
        semaphore.acquire();
        loggerEvents.debug("closed "+Strings.hex(this)+" -> "+ok);
        return ok;
    }

    public View getContentView() {
        return contentView;
    }

    public void restoreContext() {
        loggerEvents.debug("Restoring attributes: "+attributes.keySet());
        Collaboration.get().setAttributes(attributes);
    }

    /**
     * For internal use.<p>
     */
    JButton addButton(Container c, String name)  {
        JButton button = new JButton(name);
        button.addActionListener(this);
        c.add(button);
        return button;
    }

    /**
     * Implements what is done when a button is pressed (may be either
     * OK or CANCEL).<p>
     *
     * @param evt tell what button was pressed 
     */
    public void actionPerformed(ActionEvent evt) {
        try {
            Object source = evt.getSource();
            if (source==okButton) {
                ok = true;
                semaphore.release();
            } else if (source == cancelButton) {
                ok = false;
                semaphore.release();
            }
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // KeyListener interface
    public void keyPressed(KeyEvent event) {
        int code = event.getKeyCode();
        switch (code) {
            case KeyEvent.VK_ESCAPE:
                ok = false;
                semaphore.release();
                dispose();
                break;
            default:
        }
    }
    public void keyTyped(KeyEvent event) {}
    public void keyReleased(KeyEvent event) {}

    // ContainerListener interface
    // Copied from http://www.javaworld.com/javaworld/javatips/jw-javatip69.html

    public void componentAdded(ContainerEvent event) {
        addKeyAndContainerListenerRecursively(event.getChild());
    }

    /**
     * Register as a KeyListener and ContainerListener on the component
     * and its children recursively.
     * @param c the component
     */
    protected void addKeyAndContainerListenerRecursively(Component c) {
        c.addKeyListener(this);
        if (c instanceof Container) {
            Container cont = (Container)c;
            cont.addContainerListener(this);
            Component[] children = cont.getComponents();
            for(int i=0; i<children.length; i++){
                addKeyAndContainerListenerRecursively(children[i]);
            }
        }
    }

    public void componentRemoved(ContainerEvent event) {
        removeKeyAndContainerListenerRecursively(event.getChild());
    }

    /**
     * Unregister as a KeyListener and ContainerListener on the
     * component and its children recursively.
     * @param c the component 
     */
    protected void removeKeyAndContainerListenerRecursively(Component c) {
        c.removeKeyListener(this);
        if (c instanceof Container) {
            Container cont = (Container)c;
            cont.removeContainerListener(this);
            Component[] children = cont.getComponents();
            for(int i=0; i<children.length; i++){
                removeKeyAndContainerListenerRecursively(children[i]);
            }
        }
    }
}
