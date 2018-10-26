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

package org.objectweb.jac.aspects.gui;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/**
 * A class with static methods to get GUI resources as icons.<p>
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a>
 */

public class ResourceManager {
    static Logger logger = Logger.getLogger("gui.resources");

    /** Store the path where to find resources for the GUI. */ 
    public static final String RESOURCES_PATH = "org/objectweb/jac/aspects/gui/resources/"; 

    static Hashtable icons = new Hashtable();
   
    /** 
     * Creates an icon from a path string.
     *
     * @param path the path of the icon (absolute or accessible through
     * the classpath) */

    public static ImageIcon createIcon(String path) {
        logger.debug("resolving URL for "+path);
        URL url = ResourceManager.class.getClassLoader().getResource(path);
        if (url==null) {
            logger.warn("Could not find resource "+path);
            return null;
        } else {
            logger.debug("URL="+url);
            return new ImageIcon(url);
        }
    }

    static Hashtable resources = new Hashtable();

    /**
     * Define a resource.
     *
     * @param name the short name of the resource
     * @param path the full path to the resource
     * @see #getIcon(String)
     */
    public static void defineResource(String name, String path) {
        resources.put(name,path);
    }

    public static String getResource(String name) {
        return (String)resources.get(name);
    }

    public static InputStream getResourceAsStream(String name) {
        return ResourceManager.class.getClassLoader().getResourceAsStream(
            getResource(name));
    }

    /**
     * Build an icon using a resource as the image. If the resource is
     * null, returns null.
     *
     * @param resource the resource name (full path)
     * @see #getIconResource(String)
     * @see #defineResource(String,String) */

    public static ImageIcon getIcon(String resource) {
        if (resource==null)
            return null;
        ImageIcon result = (ImageIcon)icons.get(resource);
        if (result==null) {
            result = createIcon(resource);
            if (result!=null)
                icons.put(resource,result);
        }
        return result;
    }

    /**
     * Build an icon using a named resource as the image. If name is
     * not a known resource name, returns null.
     *
     * @param name the name of the resource
     *
     * @see #getIcon(String)
     * @see #defineResource(String,String) */
    public static ImageIcon getIconResource(String name) {
        return getIcon(getResource(name));
    }
}
