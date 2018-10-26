/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.swing;

import java.util.List;

/**
 * This class implements a default completion engines for editors. It
 * is non contextual (i.e. the proposed completions are always the
 * same and are the ones defined by <code>addBaseWord</code>. */

public class DefaultCompletionEngine extends CompletionEngine {

   /**
    * In the default completion engine, this method only returns base
    * words (i.e. non-contextual words).
    * 
    * @param text the editor's full text
    * @param position the cursor position
    * @param writtenText the already written text */

   public List getContextualChoices(String text, int position, 
                                    String writtenText) {
      return baseWords;
   }

   /**
    * This method always returns false (no automatic completion is
    * supported). */

   public boolean isAutomaticCompletionChar(char c) {
      return false;
   }
   
   /**
    * Do nothing (anyway it is never called because
    * <code>isAutomaticCompletionChar</code> always returns
    * false). */
   
   public void runAutomaticCompletion(SHEditor editor,
                                      String text, 
                                      int position,
                                      char c) {}
   
}
