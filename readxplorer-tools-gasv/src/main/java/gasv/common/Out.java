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
package gasv.common;

/**
  * This simple class is just a wrapper around System.out to facilitate printing at different
  * debug levels
  */
public class Out {
	/**
	 * If DEBUG_LEVEL is set to MAX_LVL then all possible messages will be output.
	 * This includes print(), print1(), print2(), etc messages.
	 */
	public static final int MAX_LVL = 10;

	/**
	 * If DEBUG_LEVEL is set to VERBOSE_LVL then only print() and print1() 
	 * messages will be output.
	 */
	public static final int VERBOSE_LVL = 1;

	/**
	 * At level 0, only print() calls will have an effect.
         * At level 1, print() and print1() calls will take effect
         * At level 2, print(), print1(), and print2() calls will take effect
	 * ...this trend can be continued ad infinitum
	 */
	private static int DEBUG_LEVEL = 0;

	public static void setDebugLevel(int newDebugLvl) {
		DEBUG_LEVEL = newDebugLvl;
	}

	public static int getDebugLevel() {
		return DEBUG_LEVEL;
	}
	/**
	  * Will always print no matter the DEBUG_LEVEL
	  */
	public static void print(String output) {
		System.out.println(output);
	}

	/**
	  * Will always print as long as the DEBUG_LEVEL is greater than 0.
	  * Use this for --verbose mode messages, i.e. those messages that are
	  * important for a user to understand program execution in more detail 
	  */
	public static void print1(String output) {
		if (DEBUG_LEVEL > 0) {
			System.out.println(output);
		}
	}	

	/**
	  * Will only print if DEBUG_LEVEL is 2 or higher.
	  * Use this for important debugging messages (that users won't need to see).
	  */
	public static void print2(String output) {
		if (DEBUG_LEVEL > 1) {
			System.out.println(output);
		}
	}	

	/**
	  * Will only print if DEBUG_LEVEL is 3 or higher.
	  * Use this for less important debugging messages (that debuggers don't even need to see all the time).
	  */
	public static void print3(String output) {
		if (DEBUG_LEVEL > 1) {
			System.out.println(output);
		}
	}	


}
