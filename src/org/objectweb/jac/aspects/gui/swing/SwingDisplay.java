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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.CommitException;
import org.objectweb.jac.aspects.gui.CustomizedDisplay;
import org.objectweb.jac.aspects.gui.CustomizedGUI;
import org.objectweb.jac.aspects.gui.CustomizedView;
import org.objectweb.jac.aspects.gui.DialogView;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EditorContainer;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GenericFactory;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.aspects.gui.ViewFactory;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Strings;

public class SwingDisplay implements CustomizedDisplay {
    static Logger logger = Logger.getLogger("display");

    /* Generic family -> Swing family */
    static Map fontFamilies = new Hashtable();
    static {
        fontFamilies.put("serif","Serif");
        fontFamilies.put("sans-serif","SansSerif");
        fontFamilies.put("monospace","Monospaced");
    }

    ViewFactory factory;
    // customizedID -> customized
    Hashtable frames = new Hashtable();

    public SwingDisplay(ViewFactory factory) {
        this.factory = factory;
        initFonts();
    }

    protected void initFonts() {
        // Fonts settings
        Map fontAttributes = GuiAC.getFontAttributes();
        if (fontAttributes.size()>0) {
            int type = 0;

            String weight = (String)fontAttributes.get("weight"); 
            if (weight!=null) {
                if (weight.compareToIgnoreCase("bold")==0) {
                    type |= Font.BOLD;
                } else if (weight.compareToIgnoreCase("normal")==0) {
                    // nothing
                } else {
                    logger.warn("Unknown font weight "+weight);
                }
            }

            String style = (String)fontAttributes.get("style"); 
            if (style!=null) {
                if (style.compareToIgnoreCase("italic")==0) {
                    type |= Font.ITALIC;
                } else if (style.compareToIgnoreCase("normal")==0) {
                    // nothing
                } else {
                    logger.warn("Unknown font style "+style);
                }
            }

            String size = (String)fontAttributes.get("size");
            String family = (String)fontAttributes.get("family");

            Font font = new Font( 
                family!=null ? family : "SansSerif", 
                type, 
                size!=null ? Integer.parseInt(size) : 12 );

            UIManager.put("Label.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("Menu.font", font);
            UIManager.put("MenuBar.font", font);
            UIManager.put("Panel.font", font);
            UIManager.put("Border.font", font);
            UIManager.put("MenuItem.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("RadioButton.font", font);
            UIManager.put("RadioButtonMenuItem.font", font);
            UIManager.put("CheckBox.font", font);
            UIManager.put("CheckBoxMenuItem.font", font);
            UIManager.put("TabbedPane.font", font);
        }
        boldifyFont("TableHeader.font");
    }

    public static void boldifyFont(String resourceName) {
        Font font = UIManager.getFont(resourceName);
        if (font!=null)
            UIManager.put(resourceName,boldifyFont(font));
    }

    public static Font boldifyFont(Font font) {
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.WEIGHT,TextAttribute.WEIGHT_BOLD);
        return new Font(attributes);
    }

    // Strings
    HashSet timedoutDialogs = new HashSet();
    public void addTimedoutDialog(DialogView dialog) {
        timedoutDialogs.add(dialog);
    }

    public void closeWindow(View window, boolean validate) {
        window.close(validate);
        if (window instanceof Window)
            ((Window)window).dispose();
    }

    public void fullRefresh() {
        Iterator it = frames.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            View frame = (View)entry.getValue();
            CustomizedGUI customized = ((CustomizedView)frame).getCustomizedGUI();
            frame.close(true);
            ((Window)frame).dispose();
            View newframe = factory.createView(
                customized.getTitle(),"Customized",
                new Object[] {customized},
                new DisplayContext(this,null));
            logger.debug("frame created "+newframe);
            frames.put(entry.getKey(),newframe);
            ((Component)newframe).setVisible(true);
        }
    }

    public void showCustomized(String id, Object object, Map panels) {
    }

    public void showCustomized(final String id, final Object object) {
        logger.debug("showCustomized("+id+","+object+")");
        try {
            final CustomizedGUI customized = (CustomizedGUI)object;
            final Component frame = (Component)frames.get(id);
            if (frame!=null) {
                if (frame instanceof Window) {
                    logger.debug("showing window "+id);
                    ((Window)frame).show();
                    ((Window)frame).toFront();
                }
            } else {
                SwingUtilities.invokeAndWait(
                    new Runnable() {
                            public void run() {
                                try {
                                    Component frame = (Component)factory.createView(
                                        customized.getTitle(),"Customized",
                                        new Object[] {customized},
                                        new DisplayContext(SwingDisplay.this,null));
                                    frames.put(id,frame);
                                    frame.setVisible(true);
                                // do this after show because otherwise, percentage
                                // positions goes wrong.
                                    ((SwingCustomized)frame).setSplitters();
                                } catch(Exception e) {
                                    logger.error("showCustomized("+id+","+object+")",e);
                                }
                            }
                        });
            }
        } catch(Exception e) {
            logger.error("showCustomized("+id+","+object+")",e);
        }
    }

    public CustomizedView getCustomizedView(String customizedID) {
        return (CustomizedView)frames.get(customizedID);
    }

    public Collection getCustomizedViews() {
        return frames.values();
    }

    public ViewFactory getFactory() {
        return factory;
    }

    // implements the display interface

    public void show(Object object) {
        show(object,
             "Object",new String[] {GuiAC.DEFAULT_VIEW});
    }

    public void show(Object object,
                     String viewType, Object[] viewParams) {
        logger.debug("show("+Strings.hex(object)+")");
        if (object==null) {
            return;
            /*
              } else if (object instanceof Throwable) {
              Log.trace("exception",(Throwable)object);
              String message = ((Throwable)object).getMessage();
              if ("".equals(message) || message==null)
              showMessage(object.getClass().getName(),"Error",false,false,true);
              else
              showMessage(message,"Error",false,false,true);
            */
        } else if (object instanceof Reader) {
            saveStreamToFile((Reader)object);
        } else if (object.getClass().isArray()) {
            // create a customized gui to show the array
            /*
              SwingCustomizedGUI gui = new SwingCustomizedGUI(true);
              gui.setSubPanesGeometry(2, Constants.HORIZONTAL, new boolean[] {false,true});
              CollectionWrapper c = new CollectionWrapper(Arrays.asList((Object[])object));
              gui.setObjectForPane(NameRepository.get().getName(c),0);
              gui.addReferenceToPane("org.objectweb.jac.aspects.gui.CollectionWrapper","collection",1);
              gui.applicationStarted();
              gui.setPosition(0,0,60,60);
              gui.show();
            */
        } else {
            String name = NameRepository.get().getName(object);
            logger.debug("name of "+object.getClass().getName()+" is "+name);
            addViewFor(object,viewType,viewParams);
        }
    }

    public boolean showModal(Object object, 
                             String viewType, Object[] viewParams,
                             String title, String header, 
                             Object parent,
                             boolean okButton, 
                             boolean cancelButton, 
                             boolean closeButton) 
    {
        logger.debug("showModal("+
                  (object!=null?object.getClass().getName():"null")+
                  viewType+Arrays.asList(viewParams)+
                  ","+title+",parent="+parent+")");
        if (object==null) {
            return addViewFor( 
                null,viewType,viewParams,
                title, header, parent,
                okButton, cancelButton, closeButton);
        } else if (object instanceof CommitException) {
            CommitException e = (CommitException)object;
            showError(
                "Commit error",
                "Failed to set value of "+e.getField()+
                " on "+GuiAC.toString(e.getObject())+
                ": "+e.getNested().getMessage());
            return true;
        } else if (object.getClass().isArray()) {
            return addViewFor(
                Arrays.asList((Object[])object),viewType,viewParams,
                title, header, parent,
                okButton, cancelButton, closeButton);
        } else {
            return addViewFor(
                object,viewType,viewParams,
                title, header, parent,
                okButton, cancelButton, closeButton);
        }
    }

    public boolean showModal(Object object, String title, String header, 
                             Object parent,
                             boolean okButton, 
                             boolean cancelButton, 
                             boolean closeButton) 
    {
        return showModal(object,
                         "Object",new String[] {GuiAC.DEFAULT_VIEW},
                         title,header,
                         parent,
                         okButton,cancelButton,closeButton);
    }

    public void openView(Object object) {
        logger.debug("openView("+object.getClass().getName()+")");
        show(object);
    }

    public boolean showInput(Object substance, AbstractMethodItem method, 
                             Object[] parameters) 
    {
        logger.debug("showInput("+method.getName()+
                  ","+Arrays.asList(parameters)+")");
        DisplayContext dc = (DisplayContext)Collaboration.get()
            .getAttribute(GuiAC.DISPLAY_CONTEXT);
        if (dc==null) {
            dc = new DisplayContext(this,null);
        }

        DialogView page = GenericFactory.createInputDialog(substance,
                                                           method,parameters,dc);
        if (page.waitForClose()) {
            EditorContainer inputView = (EditorContainer)page.getContentView();
            Iterator it = inputView.getEditors().iterator();
            int i=0;
            while (it.hasNext()) {
                if (method.getParameterTypes()[i] != DisplayContext.class) {
                    FieldEditor editor = (FieldEditor)it.next();
                    method.setParameter(parameters,i,editor.getValue());
                }
                i++;
            }
            return true;
        } else {
            return false;
        }
    }

    String displayID;

    public String getDisplayID() {
        return displayID;
    }

    public void setDisplayID(String displayID) {
        this.displayID = displayID;
    }

    public boolean showMessage(String message, String title,
                               boolean okButton, 
                               boolean cancelButton, 
                               boolean closeButton ) 
    {
        DisplayContext context = 
            (DisplayContext)Collaboration.get().getAttribute(
                GuiAC.DISPLAY_CONTEXT);
        return showModal(null,title,message,context.getWindow(),
                         okButton,cancelButton,closeButton);
    }

    public void showMessage(String message, String title) {
        showMessage(message,title,false,false,true);
    }

    public Object showRefreshMessage(String message, String title) {
        View page;
        try {
            logger.debug("showMessage("+title+","+message+")");
            DisplayContext context = new DisplayContext(this,null);
            View label = factory.createView(message,"Label",
                                            new Object[] {},context);
            page = factory.createView("Object view","Window",
                                      new Object[] {label},context);
        } finally {
            refresh();
        }
        return page;
    }

    public void showError(String message, String title) {
        showMessage(title,message);      
    }

    public void refresh() {}

    public void applicationStarted() {
    }
   
    public void close() {
        // close all customized guis
        Iterator i = frames.values().iterator();
        while(i.hasNext()) {
            View view = (View)i.next();
            view.close(true);
        }
    }   

    /** show a save dialog and save the stream into the selected file 
    * @param reader the stream to save
    */
    public void saveStreamToFile(Reader reader) 
    {
        JFileChooser chooser = new JFileChooser();
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {
            try {
                File file = chooser.getSelectedFile();
                logger.debug("saving stream to "+file.getAbsolutePath());
                OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file),"UTF-8");
                int b = reader.read();
                while (b != -1) {
                    writer.write(b);
                    b = reader.read();
                }
                writer.close();
            } catch (IOException e) {
                logger.error("saveStreamToFile() failed",e);
            }
        }
    }

    public boolean addViewFor(final Object substance) {
        return addViewFor(substance, null, null, null,
                          false, false, true);
    }

    public boolean addViewFor(final Object substance,
                              String viewType, Object[] viewParams) {
        return addViewFor(substance,
                          viewType,viewParams,
                          null,null,null,
                          false,false,true);
    }

    public boolean addViewFor(Object substance,
                              String title, String header, 
                              Object parent,
                              boolean okButton, boolean cancelButton,
                              boolean closeButton) 
    {
        return addViewFor(substance,
                          "Object",new String[] {GuiAC.DEFAULT_VIEW},
                          title,header,parent,
                          okButton,cancelButton,closeButton);
    }
    /**
     * Adds a view for a given Jac object.<p>
     *
     * @param substance the object to add a view for 
     * @return true if the OK button or the close button were clicked.
     */
    public boolean addViewFor(Object substance,
                              String viewType, Object[] viewParams,
                              String title, String header, 
                              Object parent,
                              boolean okButton, boolean cancelButton,
                              boolean closeButton) 
    {
        logger.debug("addViewFor: parent="+parent);
        if (title == null) {
            if (substance!=null) {
                Class substance_type = substance.getClass();
                if(substance_type==String.class) {
                    title = "Message" + " -" +
                        org.objectweb.jac.core.dist.Distd.getLocalContainerName() + "-";
                } else {
                    String tn = substance_type.getName();            
                    title = tn.substring( tn.lastIndexOf('.') + 1) + " " +
                        GuiAC.toString( substance ) + " -" +
                        org.objectweb.jac.core.dist.Distd.getLocalContainerName() + "-";
                }
            } else {
                if (title == null) {
                    title= "<null> -" +
                        org.objectweb.jac.core.dist.Distd.getLocalContainerName() + "-";
                }
            }
        }

        ObjectViewDialog view = null;
        try {
            DisplayContext context = new DisplayContext(this,null);
            View objectView = 
                substance==null ? null : factory.createView(
                    "object",viewType,ExtArrays.add(substance,viewParams),context);

            if (parent==null)
                view = new ObjectViewDialog(
                    objectView,title,header,okButton,cancelButton,closeButton,context);
            else if (parent instanceof Dialog)
                view = new ObjectViewDialog(
                    objectView,title,header,(Dialog)parent,
                    okButton,cancelButton,closeButton,context);
            else if (parent instanceof Frame)
                view = new ObjectViewDialog(
                    objectView,title,header,(Frame)parent,
                    okButton,cancelButton,closeButton,context);

            context.setWindow(view);
            return view.ok;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean fillParameters(AbstractMethodItem method, Object[] parameters) {
        return false;
    }

    public void onInvocationReturn(Object substance, AbstractMethodItem method) {
    }
}
