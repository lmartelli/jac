/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
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
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.CommitException;
import org.objectweb.jac.aspects.gui.DialogView;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.util.Strings;

public class ObjectViewDialog extends JDialog 
    implements ActionListener, KeyListener, ContainerListener
{
    static Logger loggerEvents = Logger.getLogger("gui.events");
    static Logger loggerFocus = Logger.getLogger("gui.focus");

    public boolean ok=false;
    JButton okButton;
    JButton cancelButton;
    JButton closeButton;
    View objectView;
    protected DisplayContext context;

    /** Stores context attributes at creation time so they can be
        restored by components when invoking methods */
    Map attributes;

    public ObjectViewDialog(View view,
                            String title, String header, 
                            Frame parent,
                            boolean okay, boolean cancel, boolean close,
                            DisplayContext context) {
        super(parent);
        setModal(parent!=null);
        context.setWindow(this);
        init(view,title,header,okay,cancel,close,context);
    }

    public ObjectViewDialog(View view,
                            String title, String header, 
                            Dialog parent,
                            boolean okay, boolean cancel, boolean close,
                            DisplayContext context) {
        super(parent);
        setModal(parent!=null);
        context.setWindow(this);
        init(view,title,header,okay,cancel,close,context);
    }


    public ObjectViewDialog(View view,
                            String title, String header, 
                            boolean okay, boolean cancel, boolean close,
                            DisplayContext context) {
        setModal(false);
        context.setWindow(this);
        init(view,title,header,okay,cancel,close,context);
    }

    void init(View view,
              String title, String header, 
              boolean okay, boolean cancel, boolean close,
              DisplayContext context) 
    {
        this.context = context;
        setTitle(title);

        if (okay || cancel) {
            //((AbstractSwingDisplay)display).addDialog(this);
            //setModal(true);
        }

        if (header != null) {
            JPanel p=new JPanel();
            p.setBorder(BorderFactory.createEtchedBorder());
            p.add(new JLabel(header,SwingConstants.LEFT));
            getContentPane().add(p,BorderLayout.NORTH);
        }
        if (view != null) {
            getContentPane().add((Component)view,BorderLayout.CENTER);
            objectView = view;
        }

        if (okay || cancel || close) {
            JPanel p2 = new JPanel();
            p2.setBorder(BorderFactory.createEtchedBorder());
            if (okay) {
                okButton = addButton(p2, "Ok");
            }
            if (cancel) {
                cancelButton = addButton(p2, "Cancel");
            }
            if (close) {
                closeButton = addButton(p2,"Close");
            }
            getContentPane().add(p2, BorderLayout.SOUTH);
            if (closeButton!=null) {
                getRootPane().setDefaultButton(closeButton);
            } else if (okButton!=null) {
                getRootPane().setDefaultButton(okButton);
            }
        }
        pack();

        // open the box centered in the screen...
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rect = getBounds();
        rect.width = Math.min(screenDim.width,rect.width);
        rect.height = Math.min(screenDim.height,rect.height);
        double left = (screenDim.getWidth()-rect.getWidth())/2;
        double top = (screenDim.getHeight()-rect.getHeight())/2;
        Rectangle newRect = new Rectangle(
            (int)left,(int)top,
            (int)rect.getWidth(),(int)rect.getHeight());
        setBounds(newRect);

        addWindowListener( new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    loggerEvents.debug("windowClosing: "+e);
                    if (objectView!=null)
                        objectView.close(true);
                }
            }
        );

        java.util.List editors = context.getEditors();
        if (!editors.isEmpty()) {
            loggerFocus.debug("give focus to "+editors.get(0));
            ((Component)editors.get(0)).requestFocus();
        } else {
            loggerFocus.debug("no editor to give focus to");
        }

        addKeyAndContainerListenerRecursively(this);

        attributes = Collaboration.get().getAttributes();   

        setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            Object source = evt.getSource();
            if (source==okButton || source==closeButton) {
                if (objectView!=null) {
                    objectView.close(true);
                }
                ok = true;
                close(true);
                dispose();
            } else if (source==cancelButton) {
                ok = false;
                // ((AbstractSwingDisplay)display).removeDialog(this);
                close(false);
                dispose();
            }
        } catch (CommitException e) {
            context.getDisplay().showModal(e,"Error","",context.getWindow(),false,false,true);
        } catch (Exception e) {
            loggerEvents.error("ObjectViewDialog.actionPerformed failed",e);
        }
    }
   
    public void close(boolean validate) {
        if (objectView!=null)
            objectView.close(validate);
        objectView = null;
    }
   
    public JButton addButton(Container c, String name)  {
        JButton button = new JButton(name);
        button.addActionListener(this);
        c.add(button);
        return button;
    }

    // We should implement DialogView !!!
    public void restoreContext() {
        loggerEvents.debug("Restoring attributes: "+attributes.keySet());
        Collaboration.get().setAttributes(attributes);
    }

    // KeyListener interface
    public void keyPressed(KeyEvent event) {
        int code = event.getKeyCode();
        switch (code) {
            case KeyEvent.VK_ESCAPE:
                ok = false;
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

    public String toString() {
        return Strings.hex(this);
    }
}
