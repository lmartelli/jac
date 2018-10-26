/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * This is the default implementation for the aspect-configuration files.
 *
 * <p>For the moment, it supports XML and ACC formats.
 */

public class ParserImpl implements Parser 
{
    static Logger logger = Logger.getLogger("parser");

    public List parse(String filePath, String targetClass, 
                      Set blockKeywords) throws IOException
    {
        InputStream inputStream = null;
        try {
            logger.debug("FileInputStream("+filePath+")");
            inputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            try {
                logger.debug("getResourceAsStream("+filePath+")");
                inputStream = 
                    getClass().getClassLoader().getResourceAsStream(filePath);
                if (inputStream==null) {
                    logger.warn("Resource "+filePath+" not found");
                }
            } catch (Exception e2) {
                logger.debug("caught exception "+e2);
                // uncaught MalformedURLException
                logger.debug("new URL("+filePath+")");
                try {
                    URL url = new URL(filePath);
                    inputStream = url.openStream();
                } catch (MalformedURLException e3) {
                    e3.printStackTrace();
                    return null;
                }
            }
        }
        if (inputStream!=null) {
            return parse(new BufferedInputStream(inputStream),
                         filePath,targetClass,blockKeywords);
        } else {
            return new Vector();
        }
    }

    public List parse(InputStream inputStream, String filePath, 
                      String targetClass, Set blockKeywords) {
        List methods = null;
        if (filePath.endsWith(".xml")) {
            try {
                InputStreamParser parser = 
                    (InputStreamParser)(Class.forName(
                        "org.objectweb.jac.core.parsers.xml.JacXmlParser").newInstance());
                methods = parser.parse(inputStream, filePath, 
                                       targetClass, blockKeywords);
            } catch (Exception e) {
                logger.error("Exception during xml parsing",e);
            }
        } else if (filePath.endsWith(".acc")) {
            try {
                InputStreamParser parser = 
                    (InputStreamParser)(Class.forName(
                        "org.objectweb.jac.core.parsers.acc.AccParserWrapper").newInstance());
                methods = parser.parse(inputStream, filePath, 
                                       targetClass, blockKeywords);
            } catch (Exception e) {
                logger.error("Exception during acc parsing",e);
            }
        } else {
            logger.warn("No parser found for "+filePath);
            methods = new Vector();
        }
        return methods;
    }
}
