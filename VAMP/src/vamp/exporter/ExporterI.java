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

package vamp.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

/**
 *
 * @author jstraube
 */
public interface ExporterI {

    public boolean readyToExport();

    public File writeFile(File tempDir, String name)throws FileNotFoundException, IOException;

    public void addColumn(WritableSheet sheet, String celltype, Object cellvalue,
            int column, int row) throws WriteException;

 // public void fillSheet(WritableSheet sheet, List<Object> object) throws WriteException ;
  
}
