/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *   This file is part of ProSE.
 *   Copyright (C) 2007-2010 CeBiTec, Bielefeld University
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 * 
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package vamp.view.dataVisualisation.abstractViewer;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import vamp.ColorProperties;

/**
 *
 * @author jstraube
 */
public class BaseBackground extends JComponent{

    private static final long serialVersionUID = 27956465;
    private String bases = null;

    public BaseBackground(int length, int height, String base){
        super();
         this.setSize(new Dimension(length, height));
        bases = base;
       
    }



    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (bases.equals("a")) {
            graphics.setColor(ColorProperties.BACKGROUND_A);
        } else if (bases.equals("c")) {
            graphics.setColor(ColorProperties.BACKGROUND_C);
        } else if (bases.equals("g")) {
            graphics.setColor(ColorProperties.BACKGROUND_G);
        } else if (bases.equals("t")) {
            graphics.setColor(ColorProperties.BACKGROUND_T);
        } else if (bases.equals("-")) {
            graphics.setColor(ColorProperties.BACKGROUND_READGAP);
        }else if (bases.equals("n")) {
            graphics.setColor(ColorProperties.BACKGROUND_N);
        }else {
            graphics.setColor(ColorProperties.BACKGROUND_BASE_UNDEF);
        }

        graphics.fillRect(0, 0, this.getSize().width-1, this.getSize().height-1);
    }
}
