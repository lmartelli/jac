package org.objectweb.jac.aspects.gui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/* numberPanel is a JPanel used by JAVAEditorPane to display linenumbers.
 */
 
public class NumberPanel extends JComponent implements Runnable
{
   BufferedImage buffer;
   Graphics2D gc;
   String text;
   FontMetrics fm;
   
   boolean painting = false;
   boolean needRepaint = false;
   
   int lineToMark = -1;
   
   JScrollPane sp;
   
   Thread paint = null;
   
   NumberPanel()
   {
      setDoubleBuffered(false);
      setFont(new Font("MonoSpaced", Font.PLAIN, 12));
      
   }
   
   public void setJScrollPane(JScrollPane s)
   {
      sp = s;
   }
   
   public void run()
   {
      repaint();
   }
      
   public void paint(Graphics g)
   {
      /*buffer = createImage(getSize().width, getSize().height);
      Graphics gc = buffer.getGraphics();*/
      
      if (buffer == null || buffer.getWidth(null) < getSize().width || buffer.getHeight(null) < getSize().height)
      {
         buffer = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_3BYTE_BGR);
         gc = buffer.createGraphics();
         
         gc.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
         fm = gc.getFontMetrics();
      }
      
      //g.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
      gc.setColor(getBackground());
      gc.fillRect(0, 0, getSize().width, getSize().height);
      gc.setColor(new Color(200, 0, 0));
      
      fm = g.getFontMetrics();
      /*if(needRepaint)
      {/*
         int lines = 1;
         for(int i = 0; i < text.length(); i ++)
         {
            if(text.charAt(i) == '\n')lines ++;
         }
         
         int posY = fm.getHeight();
         
         for(int i = 1; i <= lines; i ++)
         {
            gc.drawString("" + i, 55 - fm.charsWidth(("" + i).toCharArray(), 0, ("" + i).length()), posY);
            if (lineToMark == i)
            {
               gc.setColor(Color.green);
               gc.fillPolygon(new int[]
               {
                  58, 63, 58
               }, new int[]
               {
                  posY - (int)(fm.getHeight() / 1.5d), posY - (int)(fm.getHeight() / 3d), posY
               },
               3);
               
               gc.setColor(new Color(200, 0, 0));      
            }
            posY += fm.getHeight();
         }*/
         
         int posY = fm.getHeight();
        
         for(int i = 0; posY < getSize().height; i ++)
         {
            gc.drawString("" + i, 55 - fm.charsWidth(("" + i).toCharArray(), 0, ("" + i).length()), posY);
            if (lineToMark == i)
            {
               gc.setColor(Color.green);
               gc.fillPolygon(new int[]
               {
                  58, 63, 58
               }, new int[]
               {
                  posY - (int)(fm.getHeight() / 1.5d), posY - (int)(fm.getHeight() / 3d), posY
               },
               3);
               
               gc.setColor(new Color(200, 0, 0));      
            }
            posY += fm.getHeight();
         }
      //}
      //needRepaint = false;
      ((Graphics2D)g).drawImage(buffer, 0, 0, null);
   }
   
   public void scrollToLine(int l)
   {
      lineToMark = l;
      if(fm != null)
      {
         int lines = 1;
         for(int i = 0; i < text.length(); i ++)
         {
            if(text.charAt(i) == '\n')lines ++;
         }
         
         int posY = fm.getHeight();
         
         for(int i = 1; i <= lines && i != l; i ++)
         {
            posY += fm.getHeight();
         }
         
         if (sp != null)
         {
            System.out.println("Line: " + l);

            if(posY - fm.getHeight() < sp.getVerticalScrollBar().getValue() || posY - fm.getHeight() > sp.getVerticalScrollBar().getValue() + sp.getVerticalScrollBar().getVisibleAmount()) sp.getVerticalScrollBar().setValue(posY - fm.getHeight());
         }
      }
      repaint();
   }
   
   public void update(String t, FontMetrics f)
   {
      //fm = f;
      if (!text.equals(t))
      {
         text = t;
         needRepaint = true;
         repaint();
      }
      /*if (paint == null)
      {
         paint = new Thread(this);
         paint.start();
      }*/
   }
   
   public void update(Graphics g)
   {
      paint(g);
   }
}
