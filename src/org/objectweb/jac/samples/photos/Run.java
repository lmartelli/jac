/*
  Copyright (C) AOPSYS (http://www.aopsys.com)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.samples.photos;

import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.Actions;

public class Run {

    /**
     * This sample is a photo album, used to stock and visualize photos.
     */
    public static void main(String[] args)
    {
        photos = new PhotoRepository();
        users = new Users();
    }

    static PhotoRepository photos;
    static Users users;

    // Gui methods

    /**
     * View list of photos
     */
    public static void viewPhotos(DisplayContext context, String panelID) {
        Actions.viewObject(context,"photorepository#0",panelID);
    }

    /**
     * View list of Users
     */
    public static void viewUsers(DisplayContext context, String panelID) {
        Actions.viewObject(context,"users#0",panelID);
    }
}
