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

package vamp.view.dataVisualisation.readPosition;

/**
 *
 * @author jstraube
 */
public class Read {
    private String readname;
    private int position;
    private int errors;
    private int isBestMapping;

    public Read(String readname, int position, int errors, int isBestMapping){
        this.readname = readname;
        this.position = position;
        this.errors = errors;
        this.isBestMapping = isBestMapping;
    }

    public String getReadname() {
        return readname;
    }

    public int getErrors() {
        return errors;
    }

    public int getPosition() {
        return position;
    }

    public int getisBestMapping() {
        return isBestMapping;
    }

    @Override
    public String toString(){
        return "read: "+readname+"\tposition: "+position+"\terrors: "+errors+"%\tis best mapping.: "+isBestMapping;
    }

}
