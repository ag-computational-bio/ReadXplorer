package de.cebitec.vamp.tools.rnaFolder.rnamovies.configuration;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Stack;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ConfigXMLHandler implements ContentHandler{

 /**
  * The 3 different states the parser can be in
  */
  private static final Integer MODE_CATEGORY = new Integer(0);
  private static final Integer MODE_VALUE = new Integer(1);
  private static final Integer MODE_CONSTRUCTOR = new Integer(2);

  private static final Logger log = Logger.getLogger("ConfigXMLHandler");

  private Map<String, Category> cats;
  private Map<String, TypeWrapper> vals;

  private Stack<Attributes> lastAtts;
  private Stack<StringBuffer> lastChars;
  private Stack<Integer> lastMode;
  private Stack<List<Class>> lastCons;
  private Stack<List<Object>> lastArgs;

  private Object value = null;

  private int mode = MODE_CATEGORY;

  public ConfigXMLHandler(Map<String, Category> cats) {

    this.cats = cats;

    // init Stacks
    lastChars = new Stack<StringBuffer>();
    lastAtts = new Stack<Attributes>();
    lastMode = new Stack<Integer>();
    lastCons = new Stack<List<Class>>();
    lastArgs = new Stack<List<Object>>();
  }

    @Override
  public void startElement(String uri,
                           String localName,
                           String qName,
                           Attributes atts)
  throws SAXException {

    lastAtts.push(new AttributesImpl(atts));
    lastChars.push(new StringBuffer());

    if(qName.equalsIgnoreCase("category")) {
      lastMode.push(mode);
      mode = MODE_CATEGORY;
      vals = new LinkedHashMap<String, TypeWrapper>();
    } else if(qName.equalsIgnoreCase("value")) {
      lastMode.push(mode);
      mode = MODE_VALUE;
      value = null;
    } else if(qName.equalsIgnoreCase("object")) {
      lastMode.push(mode);
      mode = MODE_CONSTRUCTOR;
      lastCons.push(new ArrayList<Class>(5));
      lastArgs.push(new ArrayList<Object>(5));
    }
  }

    @Override
  public void endElement(String uri, String localName, String qName)
  throws SAXException {
    int i;
    String key, text;
    int id;
    Attributes atts;
    TypeWrapper tw;
    Class[] paramTypes;
    Object[] initArgs;
    Class class_ = null;
    Constructor cons_ = null;
    Object obj_ = null;

    atts = lastAtts.pop();
    text = lastChars.pop().toString();

    if(qName.equalsIgnoreCase("category")) {
      mode = lastMode.pop();

      key = atts.getValue("key");
      key = key == null ? "unnamed" : key;
      try {
        text = atts.getValue("id");
        id = text == null ? -1 : Integer.parseInt(text);
        id = id < -1 ? -1 : id;
      } catch(NumberFormatException e) {
        log.warning("Cannot convert "+e.getMessage()+" to java.lang.Integer.");
        id = -1;
      }

      //log.info("Adding new category: "+key+", with "+vals.size()+" values");
      cats.put(key, new Category(id, key, vals));
    } else if(qName.equalsIgnoreCase("value")) {
      mode = lastMode.pop();
      key = atts.getValue("key");

      if(value != null) {
        tw = new TypeWrapper(value);

        for(i = 0; i < atts.getLength(); i++)
          tw.putAttribute(atts.getQName(i), atts.getValue(i));

        //log.info("Adding new value: " + key);
        vals.put(key == null ? "unnamed" : key, tw);
      }

    } else if(qName.equalsIgnoreCase("object")) {
      mode = lastMode.pop();

      paramTypes = lastCons.pop().toArray(new Class[]{});
      initArgs = lastArgs.pop().toArray(new Object[]{});

      try {
        class_ = Class.forName(atts.getValue("class"));
        cons_ = class_.getConstructor(paramTypes);
        obj_ = cons_.newInstance(initArgs);

        //log.info("Loaded "+obj_.getClass().getName()+": "+obj_.toString());

        if(mode == MODE_CONSTRUCTOR) {
          if(lastCons.empty() || lastArgs.empty())
            return;

          lastCons.peek().add(class_);
          lastArgs.peek().add(obj_);
        } else if(mode == MODE_VALUE) {
          value = obj_;
        }
      } catch(NoSuchMethodException e) {
        log.warning("Could not find Constructor: " + e.getMessage());
      } catch(InstantiationException e) {
        log.warning("Could not instantiate: " + e.getMessage());
      } catch(IllegalAccessException e) {
        log.warning(e.getMessage());
      } catch(java.lang.reflect.InvocationTargetException e) {
        log.warning(e.getMessage());
      } catch(ClassNotFoundException e) {
        log.warning("Could not find class: " + e.getMessage());
      }
    } else if(qName.equalsIgnoreCase("string")) {
      if(mode == MODE_CONSTRUCTOR) {
        lastCons.peek().add(text.getClass());
        lastArgs.peek().add(text);
      } else if(mode == MODE_VALUE) {
        value = text;
      }
    } else if(qName.equalsIgnoreCase("int")) {
      Integer ival;
      try {
        ival = new Integer(text);
        if(mode == MODE_CONSTRUCTOR) {
          lastCons.peek().add(Integer.TYPE);
          lastArgs.peek().add(ival);
        } else if(mode == MODE_VALUE) {
          value = ival;
        }
      } catch(NumberFormatException e) {
        log.warning("Cannot convert "+e.getMessage()+" to java.lang.Integer.");
      }
    } else if(qName.equalsIgnoreCase("float")) {
      Float fval;
      try {
        fval = new Float(text);
        if(mode == MODE_CONSTRUCTOR) {
          lastCons.peek().add(Float.TYPE);
          lastArgs.peek().add(fval);
        } else if(mode == MODE_VALUE) {
          value = fval;
        }
      } catch(NumberFormatException e) {
        log.warning("Cannot convert "+e.getMessage()+" to java.lang.Float.");
      }
    } else if(qName.equalsIgnoreCase("boolean")) {
      Boolean bval;
      bval = Boolean.valueOf(text);
      if(mode == MODE_CONSTRUCTOR) {
        lastCons.peek().add(Boolean.TYPE);
        lastArgs.peek().add(bval);
      } else if(mode == MODE_VALUE) {
        value = bval;
      }
    }
  }

    @Override
  public void setDocumentLocator(Locator locator){
  }

    @Override
  public void startDocument()
  throws SAXException {
    log.info("Loading configuration...");
  }

    @Override
  public void endDocument()
  throws SAXException {
    log.info("Configuration successfully loaded.");
  }

    @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    lastChars.peek().append(ch, start, length);
  }

    @Override
  public void ignorableWhitespace(char[] ch, int start, int length)
  throws SAXException {
  }

    @Override
  public void processingInstruction(String target, String data)
  throws SAXException {
  }

    @Override
  public void skippedEntity(String name)
  throws SAXException {
  }

    @Override
  public void startPrefixMapping(String prefix, String uri)
  throws SAXException {
  }

    @Override
  public void endPrefixMapping(String prefix)
  throws SAXException {
  }

}
