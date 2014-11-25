/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.importer.excel;

import java.io.File;
import java.io.IOException;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 *
 * @author jritter
 */
public class ExcelImporter {
    
    
    public void readFile(String filelocation) throws IOException  {
    File inputWorkbook = new File(filelocation);
    Workbook w;
    try {
      w = Workbook.getWorkbook(inputWorkbook);
      // Get the first sheet
      Sheet sheet = w.getSheet(0);
      // Loop over first 10 column and lines

      for (int j = 0; j < sheet.getColumns(); j++) {
        for (int i = 0; i < sheet.getRows(); i++) {
          Cell cell = sheet.getCell(j, i);
          CellType type = cell.getType();
          if (type == CellType.LABEL) {
            System.out.println("I got a label "
                + cell.getContents());
          }

          if (type == CellType.NUMBER) {
            System.out.println("I got a number "
                + cell.getContents());
          }

        }
      }
    } catch (BiffException e) {
      e.printStackTrace();
    }
  }
    
}
