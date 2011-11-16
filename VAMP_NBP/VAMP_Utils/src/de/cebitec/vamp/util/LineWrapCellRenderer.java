package de.cebitec.vamp.util;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;


/**
 * @author rhilker
 * 
 * A cell renderer with line wrap.
 */
public class LineWrapCellRenderer  extends JTextArea implements TableCellRenderer {

    
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                this.setText(String.valueOf(value));
                this.setWrapStyleWord(true);                    
                this.setLineWrap(true);   
                
                int fontHeight = this.getFontMetrics(this.getFont()).getHeight();
                String[] splittedText = this.getText().split("\n");
//                int textLength = this.getText().length();
//                int lines = textLength / this.getColumns() +1;//+1, cause we need at least 1 row. 
                
                int height = (fontHeight+2) * splittedText.length;                        
                table.setRowHeight(row, height);   
                
                return this;
    }
        
        

}