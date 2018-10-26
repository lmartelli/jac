/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>
  
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.objectweb.jac.aspects.gui.Currency;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A Swing editor component for fields values (primitive types).
 */

public class CurrencyEditor extends TextFieldEditor
    implements ActionListener, KeyListener
{   
    JComboBox currencyBox;
    String defaultCurrency;
    String selectedCurrency;

    double realValue;
    boolean invalide = false;

    /**
     * Constructs a new primitive field editor. */

    public CurrencyEditor(Object substance, FieldItem field) {
        super(substance,field);
        textField = new JTextField(20);
        add(textField);
        currencyBox = new JComboBox();
      
        Enumeration currencies = GuiAC.getCurrencies();
        defaultCurrency = GuiAC.getDefaultCurrency();
        while (currencies.hasMoreElements()) {
            String currency = (String)currencies.nextElement();
            currencyBox.addItem(currency);
        }
        currencyBox.setSelectedItem(defaultCurrency);
        currencyBox.addActionListener(this);
        textField.addActionListener(this);
        textField.addKeyListener(this);
        selectedCurrency = defaultCurrency;
        add(currencyBox);
    }

    public void actionPerformed(ActionEvent e) {
        selectCurrency((String)currencyBox.getSelectedItem());
    }

    public void selectCurrency(String currency) {
        if (!textField.getText().trim().equals("")) {
            Currency c1 = GuiAC.getCurrency(currency);
            Currency c2 = GuiAC.getCurrency(selectedCurrency);
            setRealValue(getRealValue()*c1.getRate()/c2.getRate());
            String newValue = new Double(getRealValue()).toString();
            int dot = newValue.indexOf(".");
            if (dot!=-1 && dot+1+c1.getPrecision() <= newValue.length()) {
                textField.setText(newValue.substring(0,dot+1+c1.getPrecision()));
            } else {
                textField.setText(newValue);
            }
        }
        selectedCurrency = currency;
    }

    public void keyTyped(KeyEvent e) {
        invalide = true;
    }

    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}


    protected void setRealValue(double value) {
        realValue = value;
        invalide = false;
    }

    protected double getRealValue() {
        if (invalide) {
            realValue = Double.parseDouble(textField.getText());
        }
        return realValue;
    }
   
    /**
     * Gets the value of the edited field.
     *
     * @return an object for the value */
 
    public Object getValue() {
        selectCurrency(defaultCurrency);
        Class cl = type.getActualClass();
        if ( cl == int.class || cl == Integer.class ) {
            return( new Integer (textField.getText()) );
        } else if ( cl == long.class || cl == Long.class ) {
            return( new Long (textField.getText()) );
        } else if ( cl == float.class || cl == Float.class ) {
            return( new Float (textField.getText()) );
        } else if ( cl == double.class || cl == Double.class ) {
            return( new Double (textField.getText()) );
        } else if ( cl == short.class || cl == Short.class ) {
            return( new Short (textField.getText()) );
        } else if ( cl == byte.class || cl == Byte.class ) {
            return( new Byte (textField.getText()) );
        } else {
            throw new RuntimeException("Unhandled type "+type.getName());
        }         
    }

    /**
     * Sets the value of the edited field.
     *
     * @param value a value 
     */
    public void setValue(Object value) {
        textField.setText(value.toString());
        setRealValue(Double.parseDouble(value.toString()));
    }

    public void onClose() {}
}
