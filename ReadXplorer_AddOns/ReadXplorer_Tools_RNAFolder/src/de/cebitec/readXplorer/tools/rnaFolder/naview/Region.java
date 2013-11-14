package de.cebitec.readXplorer.tools.rnaFolder.naview;

public class Region {
  protected int number;
  protected int start1, end1, start2, end2;

  protected Region(int number, int start1, int end1, int start2, int end2) {
    this.number = number;
    this.start1 = start1;
    this.end1 = end1;
    this.start2 = start2;
    this.end2 = end2;
  }

  public int getStart1() {
    return start1;
  }

  public int getEnd1() {
    return end1;
  }

  public int getStart2() {
    return start2;
  }

  public int getEnd2() {
    return end2;
  }

  public boolean equals(Region region) {
    return(this.start1 == region.start1
           && this.end1 == region.end1 && this.start2 == region.start2
           && this.end2 == region.end2);
  }

  public String toString() {
    return("Region #" + number + ": ("
           + start1 + "-" + end1 + "), (" + start2 + "-" + end2
           + ") Gap: " + (start2 - end1 + 1));
  }
}

