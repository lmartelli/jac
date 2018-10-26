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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.lib;

import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.util.MimeTypes;

/**
 * This class represents a file attachment. The data field is the
 * content of the file. 
 */
public class Attachment {
    /**
     * Creates a new Attachement object. If the mimeType is null, it
     * will be initialized from the name's extension.
     */
    public Attachment(byte[] data, String mimeType, String name) {
        this.data = data;
        this.mimeType = mimeType;
        this.name = name;
        if (mimeType==null) {
            guessMimeType();
        }
    }

    static MimeTypes mimeTypes;

    public void guessMimeType() {
        try {
            if (mimeTypes==null) {
                mimeTypes = new MimeTypes();
                mimeTypes.readDefaults();
            }
            this.mimeType = mimeTypes.getMimeType(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    byte[] data;
    /**
     * Returns the content of the attachment.
     * @return the file's content.
     * @see #setData(byte[])
     */
    public byte[] getData() {
        return data;
    }
    /**
     * Set the content of the file
     * @param data the content of the file
     * @see #getData()
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    String mimeType;
    /**
     * @return the mime type of the file (text/plain, text/html,
     * application/msword, ...)
     * @see #setMimeType(String)
     */
    public String getMimeType() {
        return mimeType;
    }
    /**
     * Sets the mime type of the file.
     * @param mimeType the mime type of the file.
     * @see #getMimeType()
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    String name;
    /**
     * @return the name of the file
     * @see #setName(String)
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name of the file
     * @see #getName()
     */
    public void setName(String name) {
        this.name = name;
    }

    public static Object getType(FieldItem field, Attachment attachment) {
        String type = attachment.getMimeType();
        if (type!=null) {
            if (type.startsWith("text/")) {
                return "text";
            } else if (type.startsWith("image/")) {
                return "image";
            }
        }
        return field.getTypeItem();
    }
}
