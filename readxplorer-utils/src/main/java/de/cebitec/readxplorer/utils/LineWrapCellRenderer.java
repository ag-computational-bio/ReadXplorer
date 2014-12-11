/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.utils;


import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;


/**
 * A cell renderer with line wrap.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class LineWrapCellRenderer extends JTextArea implements TableCellRenderer {

    private static final long serialVersionUID = 1L;


    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected,
                                                    boolean hasFocus, int row, int column ) {
        this.setText( String.valueOf( value ) );
        this.setWrapStyleWord( true );
        this.setLineWrap( true );

        //Edit by jritter
//                if(isSelected) {
//                    this.setBackground(Color.blue);
//                }
        // ========================

        int fontHeight = this.getFontMetrics( this.getFont() ).getHeight();
        String[] splittedText = this.getText().split( "\n" );
//                int textLength = this.getText().length();
//                int lines = textLength / this.getColumns() +1;//+1, cause we need at least 1 row.

        int height = (fontHeight + 2) * splittedText.length;
        height = height < 1 ? 1 : height;
        table.setRowHeight( row, height );

        return this;
    }


}
