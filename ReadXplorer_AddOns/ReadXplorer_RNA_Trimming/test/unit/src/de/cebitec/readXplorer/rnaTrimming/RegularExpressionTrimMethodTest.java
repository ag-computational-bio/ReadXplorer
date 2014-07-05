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
package de.cebitec.readXplorer.rnaTrimming;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jeff
 */
public class RegularExpressionTrimMethodTest {
    
    public RegularExpressionTrimMethodTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testTrim() {
        RegularExpressionTrimMethod method;
        String sequence = "AAAGGGCTTGCTAAAAA";  
        TrimMethodResult r;
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.FIXED_LEFT);
        method.setMaximumTrimLength(3);
        r = method.trim(sequence);
        assertEquals("GGGCTTGCTAAAAA", r.getSequence());
        assertEquals("AAA@", r.getOsField());
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.FIXED_RIGHT);
        method.setMaximumTrimLength(6);
        r = method.trim(sequence);
        assertEquals("AAAGGGCTTGC", r.getSequence());
        assertEquals("@TAAAAA", r.getOsField());
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.FIXED_BOTH);
        method.setMaximumTrimLength(4);
        assertEquals("AGGGCTTGCTAAA", method.trim(sequence).getSequence());
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.VARIABLE_LEFT);
        method.setMaximumTrimLength(3);
        assertEquals("GGGCTTGCTAAAAA", method.trim(sequence).getSequence());
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.VARIABLE_RIGHT);
        method.setMaximumTrimLength(6);
        assertEquals("AAAGGGCTTGCT", method.trim(sequence).getSequence());
        
        method = RegularExpressionTrimMethod.createNewInstance(RegularExpressionTrimMethod.Type.VARIABLE_BOTH);
        method.setMaximumTrimLength(6);
        r = method.trim(sequence);
        assertEquals("GGGCTTGCTAA", r.getSequence());
        assertEquals("AAA@AAA", r.getOsField());
        assertEquals(3, r.getTrimmedCharsFromLeft());
        assertEquals(3, r.getTrimmedCharsFromRight());
        
    }
}
