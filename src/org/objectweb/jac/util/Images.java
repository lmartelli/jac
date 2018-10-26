/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.util;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 * Contains various methods related to image files
 */

public class Images {
    static Logger logger  = Logger.getLogger("images");

    protected static final byte[] 
	    PNG_SIG = new byte[] {-119, 'P', 'N', 'G', 13, 10, 26, 10};
    protected static final byte[] 
	    GIF87A_SIG = new byte[] {'G', 'I', 'F', '8', '7', 'a' };
    protected static final byte[] 
	    GIF89A_SIG = new byte[] {'G', 'I', 'F', '8', '9', 'a' };
    protected static final byte[] 
	    JFIF_SIG = new byte[] {-1, -40, -1, -32, 0, 0, 'J', 'F', 'I', 'F'};


    /**
     * Tells the size of an image. Supported formats are GIF and PNG.
     *
     * @param img the file of the image
     * @return The size of the image, null if the image format is unhandled 
     * 
     * @see #getImageSize(InputStream)
     */
    public static Dimension getImageFileSize(File img) 
        throws IOException 
    {
        return getImageSize(
            Images.class.getClassLoader().getResourceAsStream(
                img.getPath()));
    }

    /**
     * Tells the size of an image. Supported formats are GIF and PNG.
     *
     * @param img the data of the image
     * @return The size of the image, null if the image format is unhandled 
     * 
     * @see #getImageFileSize(File)
     */
    public static Dimension getImageSize(InputStream img) 
        throws IOException 
    {

        int read;
        byte [] buf = new byte[24];
        if ((read=img.read(buf,0,24))>=10) {
            if (ExtArrays.equals(buf, 0, GIF87A_SIG, 0, 6) ||
                ExtArrays.equals(buf, 0, GIF89A_SIG, 0, 6)) {
                return new Dimension(
                    buf[7]*(2^8)+buf[6],
                    buf[9]*(2^8)+buf[8]);
            } else if (ExtArrays.equals(buf,0,PNG_SIG,0,8)) {
                if (read==24) {
                    return new Dimension(
                        buf[16]*2^24+buf[17]*2^16+buf[18]*2^8+buf[19],
                        buf[20]*2^24+buf[21]*2^16+buf[22]*2^8+buf[23]);
                } else {
                    throw new RuntimeException(
                        "getImageSize: not enough data");
                }
            } else if (ExtArrays.equals(buf,0,JFIF_SIG,0,4) &&
                       ExtArrays.equals(buf,6,JFIF_SIG,6,4)) {
                logger.debug("getImageSize: JPEG is not supported yet");
                return null;
            }
            logger.warn("Unrecognized signature"+ExtArrays.asList(buf));
            return null;
        } else {
            throw new RuntimeException("getImageSize: not enough data");
        }
    }
}
