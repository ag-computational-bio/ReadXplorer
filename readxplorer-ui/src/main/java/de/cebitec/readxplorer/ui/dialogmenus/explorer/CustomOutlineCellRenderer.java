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

package de.cebitec.readxplorer.ui.dialogmenus.explorer;


import de.cebitec.readxplorer.utils.OsUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.openide.awt.HtmlRenderer;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;


/**
 * Renderer used to remove the property editor button and the grey appearance of
 * the cells of an outline view. It uses the <@link PropertyTextRenderer/> of
 * the property if any was set. From:
 * http://jnkjava.wordpress.com/2011/11/28/recipe-7-how-do-i-decorate-a-read-only-outlineview/
 * <p>
 * @author Evgeny Anisiforov
 */
public class CustomOutlineCellRenderer extends DefaultOutlineCellRenderer {

    /**
     * Gray Color for the odd lines in the view.
     */
    private static final Color VERY_LIGHT_GRAY = new Color( 236, 236, 236 );
    private static final long serialVersionUID = 1L;
    /**
     * Center the content of the cells displaying text.
     */
    protected boolean centered = !OsUtils.isWindows();
    /**
     * Highlight the non editable cells making the foreground lighter.
     */
    protected boolean lighterEditableFields = false;


    @Override
    @SuppressWarnings( "unchecked" )
    public Component getTableCellRendererComponent( final JTable table,
                                                    final Object value,
                                                    final boolean isSelected,
                                                    final boolean hasFocus,
                                                    final int row,
                                                    final int column ) {
        Component cell = null;
        Object valueToDisplay = value;

        if( value instanceof Property ) {
            try {
                valueToDisplay = ((Property) value).getValue();
            } catch( IllegalAccessException | InvocationTargetException ex ) {
                Exceptions.printStackTrace( ex );
            }
        }
        if( valueToDisplay != null ) {
            TableCellRenderer renderer = table.getDefaultRenderer( valueToDisplay.getClass() );
            if( (renderer != null) ) {
                cell = renderer.getTableCellRendererComponent( table, valueToDisplay, isSelected,
                                                               hasFocus, row, column );
            }
        } else {
            cell = super.getTableCellRendererComponent( table, valueToDisplay, isSelected, hasFocus, row, column );
        }
        if( cell != null ) {
            if( centered ) {
                if( cell instanceof HtmlRenderer.Renderer ) {
                    ((HtmlRenderer.Renderer) cell).setCentered( centered );
                } else if( cell instanceof DefaultTableCellRenderer.UIResource ) {
                    ((JLabel) cell).setHorizontalAlignment( JLabel.CENTER );
                }
            }
            Color foregroundColor = table.getForeground();
            int modelRow = table.convertRowIndexToModel( row );
            int modelColumn = table.convertColumnIndexToModel( column );
            final boolean cellEditable = table.getModel().isCellEditable( modelRow, modelColumn );
            if( lighterEditableFields && cellEditable ) {
                foregroundColor = Color.BLUE;
            }
            cell.setForeground( foregroundColor );
            cell.setBackground( row % 2 == 0 ? Color.WHITE : VERY_LIGHT_GRAY );
            if( isSelected ) {
                if( lighterEditableFields && cellEditable ) {
                    cell.setFont( cell.getFont().deriveFont( Font.BOLD ) );
                }
                cell.setBackground( table.getSelectionBackground() );
            }

        }
        return cell;
    }


    /**
     * @return true if the text rendered in labels is centered.
     */
    public boolean isCentered() {
        return centered;
    }


    /**
     * Center the content of the cells displaying text.
     * <p>
     * @param value true to center, false for default alignment.
     */
    public void setCentered( final boolean value ) {
        this.centered = value;
    }


    /**
     * @return true if non editable cells have a lighter foreground.
     */
    public boolean isLighterEditableFields() {
        return lighterEditableFields;
    }


    /**
     * Highlight the non editable cells making the foreground lighter.
     * <p>
     * @param value true to activate this feature.
     */
    public void setLighterEditableFields( final boolean value ) {
        this.lighterEditableFields = value;
    }


}
