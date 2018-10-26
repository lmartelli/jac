/*
 * @(#)ToolButton.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.framework.ToolListener;
import CH.ifa.draw.util.PaletteButton;
import CH.ifa.draw.util.PaletteListener;
import java.util.EventObject;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * A PaletteButton that is associated with a tool.
 *
 * @see Tool
 *
 * @version <$CURRENT_VERSION$>
 */
public class ToolButton extends PaletteButton implements ToolListener {

   private String          name;
   private Tool            tool;

   public ToolButton(PaletteListener listener, ImageIcon icon, String name, Tool tool) {
      super(listener);
      tool.addToolListener(this);
      setEnabled(tool.isUsable());

      this.tool = tool;
      this.name = name;
                
      setIcon(icon);
      setToolTipText(name);
      setEnabled(true);
      setBorder(normalBorder);
   }

   Border normalBorder = 
      BorderFactory.createCompoundBorder(
         BorderFactory.createBevelBorder(BevelBorder.RAISED),
         BorderFactory.createEmptyBorder(2,2,2,2));
   Border pressedBorder =
      BorderFactory.createCompoundBorder(
         BorderFactory.createBevelBorder(BevelBorder.LOWERED),
         BorderFactory.createEmptyBorder(2,2,2,2));

   public Tool getTool() {
      return tool;
   }

   public String getName() {
      return name;
   }

   public Object attributeValue() {
      return getTool();
   }

   public void toolUsable(EventObject toolEvent) {
      setEnabled(true);
   }

   public void toolUnusable(EventObject toolEvent) {
      setEnabled(false);
      setSelected(false);
   }

   public void toolActivated(EventObject toolEvent) {
      setBorder(pressedBorder);
   }

   public void toolDeactivated(EventObject toolEvent) {
      setBorder(normalBorder);
   }

   public void toolEnabled(EventObject toolEvent) {
      setEnabled(true);
   }

   public void toolDisabled(EventObject toolEvent) {
      setEnabled(false);
   }
}
