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
/**
Class Edge:

Contains two endpoints, in order from LEFT to RIGHT on the 2D plane.

*/
public class Edge implements Cloneable{

	EndPoint left;
	EndPoint right;

	public Edge(){ left = new EndPoint(); right = new EndPoint(); }
	
	public Edge(Edge orig){
		left = new EndPoint(orig.getLeft().getX(),orig.getLeft().getY());
		right = new EndPoint(orig.getRight().getX(),orig.getRight().getY());
	}
	
	
	public Edge(EndPoint LEFT, EndPoint RIGHT){
		left = new EndPoint(LEFT);
		right = new EndPoint(RIGHT);
	}
	
	public EndPoint getLeft(){ return left;}
	public EndPoint getRight(){ return right;}
	
	public void setLeft(EndPoint LEFT){ left = new EndPoint(LEFT);}
	public void setRight(EndPoint RIGHT){ right = new EndPoint(RIGHT);}
	
	public String toStr(){ return "{"+left.toString() +"," + right.toString() + "}"; }
	//public String toString(){ return "{"+left.toString() +"," + right.toString() + "}"; }

	public String toString(){ return "{LEFT:"+left.toString() +", RIGHT:" + right.toString() + "}"; }

	
	//public String toString(){ return "("+left.getX()+","+left.getY()+") - (" + right.getX() + "," + right.getY() + ")"; }

	
	//Method: Value,
	//Task: Return the y-value on a line of the form: y = -x + b;
	//(***) Still to do! NEED to consider line-sweep with slope y = x + b; (***)
	//(***) Still to do! ERROR CHECK: make sure the value b IS in the range for this segment! (***)
	
	//Return the Y-value of the edge intersecting a sweep line of the form: y = mx+b
	// where b is the offset and slope is m
	public double Value(double b, int slope){
		if(slope!=-1 && slope!=1){ System.out.println("Invalid Assignment of Slope!"); System.exit(-1); }
		double answer;
		//3 cases: horizontal, vertical or otherwise:
		//Case 1: Horizontal:
		if(left.getY() == right.getY()){
			answer =  left.getY();
		}
		//Case 2: Vertical:
		else if(left.getX() == right.getX()){
			answer = (slope*left.getX() + b);
		}
		else{
			//Need to solve for: y = mx + bother;
			double m = (left.getY() - right.getY())/(left.getX() - right.getX());
			double bother = left.getY() - m*left.getX(); 
			//Now: Need y = slope*x + b & y = mx + bother
			// slope*x + b = mx + bother -> (b - bother) = (m - slope) x
			//SO: if m is NOT -1:
			if(m != slope){
				double x = (b-bother)/(m-slope);
				answer = slope*x + b;
				//answer = (-1*(b - bother)/(m+1) + b);
			}
			//AND if m == -1; then we are parallel to the sweepline and should return the biggest
			//y-value!
			else{
				if(left.getY()>right.getY()){ answer = left.getY();}
				else{ answer = right.getY();}
			}
		}
		
		return answer;
	
	}

	//Given an x-value, return the corresponding y-value;
	public EndPoint getValue(double x){
		EndPoint answer = new EndPoint(-1,-1);
		
		if(left.getX() <= x && right.getX()>=x){
			//Case 1: Horizontal:
			if(left.getY() == right.getY()){ answer = new EndPoint(left.getY(),-1); }
			
			//Case 2: Vertical:
			else if( ((int) left.getX() == (int) right.getX()) && ((int)x == (int)left.getX()) ){
				double min = left.getY(); 
				double max = right.getY();
				if(min>max){ double tmp = min; max = min; min = tmp; }
				answer = new EndPoint(min,max);
			}
	
			//Case 3: Other Slope
			else{
				double m = (left.getY() - right.getY())/(left.getX() - right.getX());
				double bother = left.getY() - m*left.getX();
				double newY = m*x + bother;
				answer = new EndPoint(newY,-1);
			}
		}
				
		return answer;
	}
		
	//Method: getPoint
	//Task: Given a line segment and an appropriate sweepvalue b (how to encode different
	//      sweep lines?) return the point of intersection or the point (-1,-1) if no intersection
	public EndPoint getPoint(double b, int slope){
		EndPoint answer = new EndPoint(-1,-1);
		if(slope!=-1 && slope!=1){ System.out.println("Invalid Assignment of Slope!"); System.exit(-1); }
		//y = slope x + b OR x = (y - b)/slope;
		//3 cases: horizontal, vertical or otherwise:
		//Case 1: Horizontal:
		if(left.getY() == right.getY()){
			answer.setY(left.getY());
			answer.setX((left.getY() - b)/slope);
		}
		//Case 2: Vertical:
		else if(left.getX() == right.getX()){
			answer.setY(slope*left.getX() + b);
			answer.setX(left.getX());
		}
		else{
			//Need to solve for: y = mx + bother;
			double m = (left.getY() - right.getY())/(left.getX() - right.getX());
			double bother = left.getY() - m*left.getX(); 
			//Now: Need y = slope*x + b & y = mx + bother
			// slope*x + b = mx + bother -> (b - bother) = (m - slope) x
			//SO: if m is NOT -1:
			if(m != slope){
				double x = (b-bother)/(m-slope);
				answer.setX(x);
				answer.setY(slope*x + b);
			}
			//AND if m == -1; then we are parallel to the sweepline and should return the biggest
			//y-value!
			else{
				if(left.getY()>right.getY()){ answer.setX(left.getX()); answer.setY(left.getY());}
				else{ answer.setX(right.getX()); answer.setY(right.getY());}
			}
		}
		
		return answer;

	}

	
	public static void main(String[] args) throws CloneNotSupportedException{
		EndPoint e1 = new EndPoint(5,7);
		EndPoint e2 = new EndPoint(1,2);
		Edge E = new Edge(e1,e2);
		Edge E1 = (Edge) E.clone();
	
	
	}
}

