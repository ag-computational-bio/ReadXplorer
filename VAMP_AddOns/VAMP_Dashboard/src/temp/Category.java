/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package temp;

import java.util.List;

/**
 *
 * @author jeff
 */
public class Category {
    
    private String name;
    private List<Object> data;
    
    /** Creates a new instance of Category */
    public Category(String name, List data) {
        //this.setName(name);
        //this.setData(data);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the data
     */
    public List<Object> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<Object> data) {
        this.data = data;
    }
    
}