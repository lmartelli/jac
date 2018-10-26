/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>
  
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

/**
 * This class represents borders in GUI. */

public class Border {
    /** None border constant */
    public static final int NONE = 0;
    /** The label of the border if any is diplayed on the left constant */
    public static final int LEFT = 1;
    /** The label of the border if any is diplayed on the top constant */
    public static final int TOP = 2;
    /** The label of the border if any is diplayed on the right constant */
    public static final int RIGHT = 3;
    /** The label of the border if any is diplayed on the bottom constant */
    public static final int BOTTOM = 4;   
    /** The label of the border if any is diplayed centered constant */
    public static final int CENTER = 5;
    /** The style is a line constant */
    public static final int LINE = 6;
    /** The style is etched constant */
    public static final int ETCHED = 7;
    /** The style is lowered constant */
    public static final int LOWERED = 8;
    /** The style is raised constant */
    public static final int RAISED = 9;

    /**
     * Converts a string representation of the alignment to an int. */ 
   
    public static int a2iAlignment(String alignment) {
        if (alignment.equals("LEFT")) 
            return LEFT;
        else if (alignment.equals("RIGHT")) 
            return RIGHT;
        else if (alignment.equals("CENTER")) 
            return CENTER;
        else
            throw new RuntimeException("Wrong alignment '"+alignment+"'");
    }

    /**
     * Converts a string representation of the style to an int. */ 

    public static int a2iStyle(String style) {
        if (style.equals("LINE")) 
            return LINE;
        else if (style.equals("ETCHED")) 
            return ETCHED;
        else if (style.equals("LOWERED")) 
            return LOWERED;
        else if (style.equals("RAISED")) 
            return RAISED;
        else
            throw new RuntimeException("Wrong style '"+style+"'");
    }

    /**
     * Converts an integer representation of the style to a string. */ 

    public static String i2aStyle(int style) {
        if (style==LINE) 
            return "LINE";
        else if (style==ETCHED) 
            return "ETCHED";
        else if (style==LOWERED) 
            return "LOWERED";
        else if (style==RAISED) 
            return "RAISED";
        else
            throw new RuntimeException("Wrong style '"+style+"'");
    }

    /**
     * Constructs a new border.
     *
     * @param title the border's title
     * @param alignment the title alignment
     * @param style the border's style */

    public Border(String title,int alignment,int style) {
        this.title = title;
        this.alignment = alignment;
        this.style = style;
    }

    /**
     * Returns true if the border has a title. */
    public boolean hasTitle() {
        return title!=null;
    }
   
    int alignment;
   
    /**
     * Get the value of alignment.
     * @return value of alignment.
     */
    public int getAlignment() {
        return alignment;
    }
   
    /**
     * Set the value of alignment.
     * @param v  Value to assign to alignment.
     */
    public void setAlignment(int  v) {
        this.alignment = v;
    }
   
    int style;
   
    /**
     * Get the value of style.
     * @return value of style.
     */
    public int getStyle() {
        return style;
    }
   
    /**
     * Set the value of style.
     * @param v  Value to assign to style.
     */
    public void setStyle(int  v) {
        this.style = v;
    }
   
    String title;
   
    /**
     * Get the value of title.
     * @return value of title.
     */
    public String getTitle() {
        return title;
    }
   
    /**
     * Set the value of title.
     * @param v  Value to assign to title.
     */
    public void setTitle(String  v) {
        this.title = v;
    }

    public String toString() {
        return "Border{title="+title+",style="+style+"}";
    }
}
