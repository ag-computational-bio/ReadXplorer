package de.cebitec.readXplorer.tools.rnaFolder.rnamovies.configuration;

import java.io.InputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;

public class Configuration {

  private Map<String, Category> categories = Collections.synchronizedMap(new LinkedHashMap<String, Category>());

  public Configuration(InputStream in) throws IOException, SAXException {
    XMLReader parser;
    ContentHandler handler;

    parser = XMLReaderFactory.createXMLReader();
    handler = new ConfigXMLHandler(categories);

    parser.setContentHandler(handler);
    parser.parse(new InputSource(in));
  }

  public void initAll() {
    Iterator<Category> cats;
    for(cats = categories.values().iterator(); cats.hasNext();)
      cats.next().init();
  }

  public void addConfigListener(ConfigListener listener) {
    Iterator<Category> cats;
    for(cats = categories.values().iterator(); cats.hasNext();)
      cats.next().addConfigListener(listener);
  }

  public Map<String, Category> getCategories() {
    return categories;
  }

  public Category getCategory(String name) {
    if(!categories.containsKey(name))
      throw new NoSuchElementException("No such configuration category " + name + ".");

    return categories.get(name);
  }

}
