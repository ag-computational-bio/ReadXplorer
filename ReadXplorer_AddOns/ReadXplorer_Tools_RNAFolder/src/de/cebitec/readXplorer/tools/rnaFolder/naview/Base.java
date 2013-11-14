package de.cebitec.readXplorer.tools.rnaFolder.naview;

import java.awt.geom.Point2D.Double;
import java.awt.geom.Point2D;

public class Base {
  protected int mate;
  protected double x = 9999.0;
  protected double y = 9999.0;
  protected boolean extracted = false;
  protected Region region = null;

  protected Base(int mate) {
    this.mate = mate;
  }

  public int getMate() {
    return mate;
  }

  public Point2D getPosition() {
    return new Point2D.Double(x, y);
  }

  public Region getRegion() {
    return region;
  }
}
