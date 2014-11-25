package de.cebitec.readXplorer.tools.rnaFolder.rnamovies.configuration;

import java.util.EventObject;

public class ConfigChangedEvent extends EventObject {

  private int id;

  private String key;

  private Object value;

  public ConfigChangedEvent(Object source, int id, String key, Object value) {
    super(source);
    this.id = id;
    this.key = key;
    this.value = value;
  }

  public int getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

}
