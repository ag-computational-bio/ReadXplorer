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
package de.cebitec.readXplorer.dashboard;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jeff
 */
public class DashboardTest {
    
    public DashboardTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testEquals() {
        PersistantReference r1 = new PersistantReference(1, "", "", new Timestamp(111111111), new File(""));
        PersistantReference r2 = new PersistantReference(1, "", "", new Timestamp(111111111), new File(""));
        PersistantReference r3 = new PersistantReference(2, "", "", new Timestamp(111111111), new File(""));
        assertTrue(r1.equals(r2));
        assertFalse(r1.equals(r3));
        
        Object o1 = (Object) r1;
        Object o2 = (Object) r2;
        Object o3 = (Object) r3;
        assertTrue(o1.equals(o2));
        assertFalse(o1.equals(o3));
    }
    
    @Test
    public void testMap() {
        HashMap<Value,Integer> map = new HashMap<>();
        Value v1 = new Value();
        Value v2 = new Value();
        map.put(v1, 1);
        assertNotNull(map.get(v2));
    }
    
    @Test
    public void testMap2() {
        HashMap<PersistantReference, List<PersistantTrack>> tracks_by_reference = new HashMap<>();
        PersistantReference r1 = new PersistantReference(1, "", "", new Timestamp(111111111), new File(""));
        PersistantReference r2 = new PersistantReference(1, "", "", new Timestamp(111111111), new File(""));
        List<PersistantTrack> list = new ArrayList<>();
        tracks_by_reference.put(r1, list);
        assertNotNull(tracks_by_reference.get(r2));
        
    }
    
    @Test 
    public void zip() throws Exception {
        String data = "djkfdskajkj";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        GZIPOutputStream gz;
        ObjectOutputStream    oos; 
        
        gz = new GZIPOutputStream(baos);
        oos = new ObjectOutputStream( gz );
        oos.writeObject( data ); 
        oos.close(); 
        byte[] array = baos.toByteArray();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "created byte representation ({0} bytes)", array.length);
        
        ByteArrayInputStream bais;
        GZIPInputStream gzi;
        ObjectInputStream ins;
        bais = new ByteArrayInputStream(array);
        
        gzi = new GZIPInputStream(bais);
        ins = new ObjectInputStream(gzi);
        Object data2 = ins.readObject();
        
        assertEquals(data, data2);
    }
    
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    private class Value {
        @Override
        public int hashCode() {
            return 1;
        }
        
        @Override
        public boolean equals(Object o) {
            return true;
        }
        

        
    }
}
