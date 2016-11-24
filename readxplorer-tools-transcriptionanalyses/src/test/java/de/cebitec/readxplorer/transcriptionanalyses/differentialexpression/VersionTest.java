/*
 * Copyright (C) 2016 Patrick Blumenkamp <patrick.blumenkamp at computational.bio.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;



/**
 *
 * @author Patrick Blumenkamp
 * <patrick.blumenkamp at computational.bio.uni-giessen.de>
 */
public class VersionTest {

    /**
     * Test of splitting the version into parts, of class Version.
     */
    @Test
    public void testVersionParts() {
        Version programA = new Version( "1.0.1" );
        assertEquals( "1.0.1", programA.getMainVersion() );
        assertNull( programA.getPreRealease() );
        assertNull( programA.getMetadata() );
        assertEquals( "1.0.1", programA.getVersion() );
        Version programB = new Version( "1.0.1-alpha.1" );
        assertEquals( "1.0.1", programB.getMainVersion() );
        assertEquals( "alpha.1", programB.getPreRealease() );
        assertNull( programB.getMetadata() );
        assertEquals( "1.0.1-alpha.1", programB.getVersion() );
        Version programC = new Version( "1.0.1+exp.sha.5114f85" );
        assertEquals( "1.0.1", programC.getMainVersion() );
        assertNull( programC.getPreRealease() );
        assertEquals( "exp.sha.5114f85", programC.getMetadata() );
        assertEquals( "1.0.1+exp.sha.5114f85", programC.getVersion() );
        Version programD = new Version( "1.0.0-alpha+001-002" );
        assertEquals( "1.0.0", programD.getMainVersion() );
        assertEquals( "alpha", programD.getPreRealease() );
        assertEquals( "001-002", programD.getMetadata() );
        assertEquals( "1.0.0-alpha+001-002", programD.getVersion() );

        boolean exceptionThrown = false;
        try {
            Version illegalVersion = new Version( "1.a" );
        } catch( IllegalArgumentException ex ) {
            exceptionThrown = true;
        }
        assertTrue( exceptionThrown );
    }


    /**
     * Test of compareTo method, of class Version.
     */
    @Test
    public void testCompareTo() {
        Version programA = new Version( "1.0.1" );
        Version programB = new Version( "1.0.1-alpha.1" );
        Version programC = new Version( "1.0.1+exp.sha.5114f85" );
        Version programD = new Version( "1.0.0-alpha+001-002" );
        Version programE = new Version( "1.0.0-beta+001-002" );
        Version programF = new Version( "1.0.1-rc.1" );
        Version program0 = new Version( "0" );
        Version programMinimal = new Version("0.0.0.1");

        assertEquals( "Only differ in metadata", 0, programA.compareTo( programC ) );
        assertEquals( "Pre-release is older than actual release", 1, programA.compareTo( programB ) );
        assertEquals( "Comparison of simple version number with complex one", -1, programD.compareTo( programA ) );
        assertEquals( "Compare two pre-releases", 1, programF.compareTo( programB ) );
        assertEquals( "Comparison of two complex version numbers", -1, programD.compareTo( programE ) );
        assertEquals( "Version \"0\" should be smaller than any other version", -1, program0.compareTo( programMinimal));
    }


    /**
     * Test of equals method, of class Version.
     */
    @Test
    public void testEquals() {
        Version programA = new Version( "1.0.1" );
        Version programB = new Version( "1.1.1" );
        Version programC = new Version( "1.0.1-alpha.1" );
        Version programD = new Version( "1.0.1-alpha.1" );
        Version programE = new Version( "1.0.1+exp.sha.5114f85" );
        Version programF = new Version( "1.0.1+exp.sha.5354354" );
        assertEquals( programA, programA );
        assertFalse( programA.equals( programB ) );
        assertEquals( programE, programA );
        assertEquals( programD, programC );
        assertEquals( programE, programF );
    }


}
