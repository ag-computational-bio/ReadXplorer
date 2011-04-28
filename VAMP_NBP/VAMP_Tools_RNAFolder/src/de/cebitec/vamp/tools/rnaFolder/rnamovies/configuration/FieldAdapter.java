package de.cebitec.vamp.tools.rnaFolder.rnamovies.configuration;

import java.util.logging.Logger;

import java.lang.reflect.Field;
import javax.swing.BoundedRangeModel;

public class FieldAdapter implements ConfigListener {

  private static final Logger log = Logger.getLogger("FieldAdapter");

  private Object configurable;

  public FieldAdapter(Object configurable) {
    this.configurable = configurable;
  }

    @Override
  public void configurationChanged(ConfigChangedEvent e) {
    String name;
    Object value;
    Field field;

    name = e.getKey();
    value = e.getValue();

    try {
      field = configurable.getClass().getDeclaredField(name);
      if(value instanceof Boolean) {
        field.setBoolean(configurable, ((Boolean)value).booleanValue());
      } else if(value instanceof Integer) {
        field.setInt(configurable, ((Integer)value).intValue());
      } else if(value instanceof Float) {
        field.setFloat(configurable, ((Float)value).floatValue());
      } else if(value instanceof BoundedRangeModel) {
        field.setInt(configurable,((BoundedRangeModel)e.getValue()).getValue());
      } else {
        field.set(configurable, value);
      }
    } catch(NoSuchFieldException ex) {
      log.severe("No such field: ".concat(ex.getMessage()));
    } catch(IllegalAccessException ex) {
      log.severe(ex.getMessage());
    }
  }
}
