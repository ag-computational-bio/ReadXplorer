package de.cebitec.vamp.tools.rnaFolder.rnamovies.configuration;

import java.util.HashMap;
import java.util.Map;

public class TypeWrapper {

  private Object obj;
  private Map<String, String> attributes;

  public TypeWrapper(Object obj) {
    this.obj = obj;
    attributes = new HashMap<String, String>();
  }

  public Object getObject() {
    return obj;
  }

  public void setObject(Object obj) {
    this.obj = obj;
  }

  public void putAttribute(String key, String value) {
    attributes.put(key, value);
  }

  public String getAttribute(String key) {
    return attributes.get(key);
  }

  public boolean contains(String key) {
    return attributes.containsKey(key);
  }

}
