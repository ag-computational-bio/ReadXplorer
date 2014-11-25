/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.util;

import java.io.File;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jeff
 */
public class FileUtilsTest {
    
    public FileUtilsTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetFilePathWithoutExtension() {
        assertEquals("/var/lib/samfile", FileUtils.getFilePathWithoutExtension(new File("/var/lib/samfile.sam")));
        assertEquals("/var/lib/samfile.redo", FileUtils.getFilePathWithoutExtension(new File("/var/lib/samfile.redo.sam")));
    }
}
