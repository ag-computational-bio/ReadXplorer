/**
 * Copyright 2010 Benjamin Raphael, Suzanne Sindi, Hsin-Ta Wu, Anna Ritz, Luke Peng
 *
 *  This file is part of gasv.
 * 
 *  gasv is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  gasv is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with gasv.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package gasv.main;
import java.lang.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class  EndPoint implements Cloneable{
	double x;
	double y;
	
	public EndPoint(){x = -1; y = -1;}
	
	public EndPoint(EndPoint orig){ x = orig.getX(); y = orig.getY(); }

	public EndPoint(double X, double Y){
		x = X; y = Y;
	}
	
	public void setX(double X){ x = X;}
	public double getX(){ return x;}

	public void setY(double Y){ y = Y;}
	public double getY(){ return y;}
	
	public String toString(){
		//return "(" + x + "," + y + ")";
		return "{" + Math.round(x) + "," + Math.round(y) + "}";
	}
	
	public int equals(EndPoint other){
		if(getX() == other.getX() && getY() == other.getY()){ return 1; }
		else{return 0;}
	}
	
	public static void main(String[] args) throws CloneNotSupportedException{
		EndPoint e1 = new EndPoint(4,7);
		EndPoint e2 = (EndPoint) e1.clone();
		
		System.out.println(e1.toString());
		System.out.println(e2.toString());
		e2.setX(5);
		
		System.out.println(e1.toString());
		System.out.println(e2.toString());
	
		
	
	}

}
