/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>

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
  USA */

package org.objectweb.jac.ide;

// Look and feels

import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.cache.CacheAC;
import org.objectweb.jac.aspects.gui.CustomizedDisplay;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.lib.Attachment;
import org.objectweb.jac.util.File;
import org.objectweb.jac.util.Predicate;
import org.objectweb.jac.util.Streams;
import org.objectweb.jac.aspects.gui.GuiAC;

/**
 * Gui methods for the menus.
 */
public class Gui {
    static final Logger logger = Logger.getLogger("umlaf.gui");

    protected static void setLookAndFeel(LookAndFeel look) 
        throws UnsupportedLookAndFeelException 
    {
        Object d = Collaboration.get().getAttribute("Gui.display");
        if (d instanceof CustomizedDisplay) {
            CustomizedDisplay display = (CustomizedDisplay)d;
            UIManager.setLookAndFeel(look);
            Iterator i = display.getCustomizedViews().iterator();
            while (i.hasNext()) {
                Object frame = i.next();
                if (frame instanceof Component) {
                    SwingUtilities.updateComponentTreeUI((Component)frame);
                }
            }
        }
      
    }

    /**
     * Gets the project an element belongs to
     * @param element the element whose projet to return
     */
    protected static Project getProject(ModelElement element) {
        Project project = null;
        if (element instanceof Member) {
            project = ((Member)element).getProject();
        } else if (element instanceof Parameter) {
            Method method = ((Parameter)element).getMethod();
            if (method!=null)
                project = method.getProject();
        } else if (element instanceof Class) {
            return ((Class)element).getProject();
        }
        return project;
    }

    /**
     * Returns all types which belong to the same project as a
     * given member.
     * @param element the element
     * @see #getAvailableClasses(ModelElement)
     * @see #getMatchingTypes(ModelElement,String)
     */
    public static Collection getAvailableTypes(ModelElement element) {
        Vector result = new Vector();
        Project project = getProject(element);
        Collection types = ObjectRepository.getObjects(
            ClassRepository.get().getClass("org.objectweb.jac.ide.Type"));
        Iterator it = types.iterator();
        while (it.hasNext()) {
            Type type = (Type)it.next();
            if (((element instanceof Field) || (element instanceof Parameter)) && 
                type==Projects.types.resolveType("void"))
                continue;
            if (!(type instanceof Class && project!=null &&
                  ((Class)type).getProject()!=project)) {
                result.add(type);
            }
        }
        return result;
    }

    /**
     * Returns types with a given short name
     */
    public static Collection getMatchingTypes(ModelElement element, final String search) {
        Vector result = new Vector();
        new Predicate() {
                public boolean apply(Object o) {
                    return ((ModelElement)o).getName().compareToIgnoreCase(search)==0;
                }
            }.filter(getAvailableTypes(element),result);
        return result;
    }

    /**
     * Returns all classes which belong to the same project as a
     * given member.
     * @param element the element
     * @see #getAvailableTypes(ModelElement)
     * @see #getMatchingTypes(ModelElement,String)
     */
    public static Collection getAvailableClasses(ModelElement element) {
        Project project = getProject(element);
        return project.getClasses();
    }

    public static void invalidateCache() {
        CacheAC cacheAspect = (CacheAC)ACManager.getACM().getACFromFullName("ide.cache");
        if (cacheAspect!=null)
            cacheAspect.invalidateCache();
        else
            throw new RuntimeException("Could not find cache aspect \"ide.cache\"");
    }

    public static void edit(Attachment attachment, DisplayContext context) 
        throws IOException 
    {
        String editor = Projects.prefs.getExternalEditor();
        if (editor!=null) {
            new ExternalEditorThread(context,editor,attachment).start();
        } else {
            throw new RuntimeException(
                "You must specify an external for your projects");
        }
    }

    public static void editWith(Attachment attachment, 
                                DisplayContext context, String editor) 
        throws IOException 
    {
        new ExternalEditorThread(context,editor,attachment).start();
    }

    static class ExternalEditorThread extends Thread {
        public ExternalEditorThread(
            DisplayContext context,
            String editor, Attachment attachment) 
        {
            this.context = context;
            this.editor = editor;
            this.attachment = attachment;
        }
        
        DisplayContext context;
        String editor;
        Attachment attachment;

        public void run() 
        {
            try {
                java.io.File tmpFile = 
                    File.createTempFile("UMLAF", 
                                        attachment.getName());
                FileOutputStream out =
                    new FileOutputStream(tmpFile);
                out.write(attachment.getData());
                out.close();
                logger.info(
                    "Editing resource "+attachment.getName()+
                    " with "+editor);
                Process proc = 
                    Runtime.getRuntime().exec(
                        new String[] {
                            editor,
                            tmpFile.getPath()
                        });
                int status = proc.waitFor();
                if (status!=0)
                    throw new Exception(
                        "Editor process for "+editor+" failed with status "+status);
                attachment.setData(
                    Streams.readStream(new FileInputStream(tmpFile)));
                logger.info(
                    "Resource "+attachment.getName()+" edited ("+status+")");
                tmpFile.delete();
            } catch (Exception e) {
                logger.error(
                    "Failed to edit resource "+attachment.getName(),e);
                Collaboration.get().addAttribute(GuiAC.DISPLAY_CONTEXT,context);
                context.getDisplay().showError(
                    "Failed to edit resource "+attachment.getName(),
                    e.toString());
            }
        }
    }

    public static Object getType(FieldItem field, Attachment attachment) {
        String type = attachment.getMimeType();
        ClassRepository cr = ClassRepository.get();
        if ("text/x-java".equals(type)) {
            return "javaCode";
        }
        return Attachment.getType(field,attachment);
    }

    public static String getAttachmentIcon(Attachment attachment) {
        String type = attachment.getMimeType();
        ClassRepository cr = ClassRepository.get();
        if ("text/x-java".equals(type)) {
            return ResourceManager.getResource("icon_class");
        } else {
            return null;
        }
    }    
}

