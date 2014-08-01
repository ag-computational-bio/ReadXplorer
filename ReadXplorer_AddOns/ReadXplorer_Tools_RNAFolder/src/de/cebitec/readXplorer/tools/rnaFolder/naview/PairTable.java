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
package de.cebitec.readXplorer.tools.rnaFolder.naview;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.HashSet;
import java.util.StringTokenizer;

public class PairTable {

  private int[] pairTable;
  private byte[] bondTypes;
  private int length;
  public static final byte NONE = -1;
  public static final byte NORMAL_BOND = 0;
  public static final byte GU_BOND = 1;
  public static final byte KNOT = 2;
  public static final byte KNOT_GU = 3;
  public static final byte PK1 = 4;
  public static final byte PK2 = 5;
  public static final byte PK3 = 6;
  private static final int GU = 'G' + 'U';
  private static final int GT = 'G' + 'T';

  // DCSE input
  public PairTable(String sequence, String structure, String helices) {
    boolean end, paired, gu;
    char c;
    int mate, regions, pos, i, j, h_idx, pk_start, pk_end, sum;
    String tmp, label;
    String[] blubb;
    StringBuffer sb = new StringBuffer();
    List<String> labels = new ArrayList<String>();
    List<Integer> pt_list = new ArrayList<Integer>();
    List<Byte> type_list = new ArrayList<Byte>();
    Stack<Integer> s0 = new Stack<Integer>();
    Stack<Integer> s1 = new Stack<Integer>();
    Stack<Integer> s2 = new Stack<Integer>();
    HashSet<String> pseudoknots = new HashSet<String>();
    HashSet<String> knots = new HashSet<String>();
    StringTokenizer st = null;

    // extract region labels from the helix numbering
    h_idx = helices.indexOf('H');
    if(h_idx < 0)
      throw new IllegalArgumentException("Missing terminating 'H' character in helix numbering.");
    blubb = helices.substring(0, h_idx).split("(\\s*-\\s*)+");
    for(i = 0; i < blubb.length; i++) {
      tmp = blubb[i].trim();
      if(tmp.matches("\\d+('?)"))
        labels.add(tmp);
    }

    // extract pseudoknot labels
    pk_start = structure.lastIndexOf('#');
    pk_end = structure.lastIndexOf('&');
    if(pk_start > 0 && pk_end > 0 && pk_start < pk_end) {
      st = new StringTokenizer(structure.substring(pk_start, pk_end), " ");
      while(st.hasMoreTokens()) {
        tmp = st.nextToken().trim();
        if(tmp.matches("\\d+")) {
          pseudoknots.add(tmp);
          pseudoknots.add(tmp + "'");
        }
      }
    }

    // extract entagled helix (knot) labels
    if(pk_end > 0 && pk_end < structure.length()) {
      st = new StringTokenizer(structure.substring(pk_end, structure.length()), " ");
      while(st.hasMoreTokens()) {
        tmp = st.nextToken().trim();
        if(tmp.matches("\\d+")) {
          knots.add(tmp);
          knots.add(tmp + "'");
        }
      }
    }

    // parse structure
    end = false;
    paired = false;
    pos = regions = 0;
    for(i = 0; i < structure.length() && !end; i++) {
      switch(c = structure.charAt(i)) {
        case '[':
          paired = true;
        case '^':
          regions++;
          break;
        case ']':
        case '{':
          paired = false;
          break;
        case '}':
          paired = true;
          break;
        case ' ':
        case '(':
        case ')':
          break;
        case '|':
          end = true;
          break;
        default:
          if(++pos >= sequence.length())
            throw new IllegalArgumentException("Length of structure exceeds length of sequence!");
          if(paired) {
            if(regions > labels.size())
              throw new IllegalArgumentException("Structure contains more regions than helix numbering.");
            label = labels.get(regions - 1);
            if(label.endsWith("'")) {
              if(pseudoknots.contains(label)) {
                type_list.add(PK1);
                if(s1.empty())
                  throw new IllegalArgumentException("Unbalanced pseudoknot pair(s) in structure.");
                mate = s1.pop().intValue();
              } else {
                if(s0.empty())
                  throw new IllegalArgumentException("Unbalanced base pair(s) in structure.");
                mate = s0.pop().intValue();
                sum =   sequence.toUpperCase().charAt(pos - 1)
                      + sequence.toUpperCase().charAt(mate);
                gu = sum == GU || sum == GT;
                if(knots.contains(label)) {
                  type_list.add(gu ? KNOT_GU : KNOT);
                  type_list.set(mate, gu ? KNOT_GU : KNOT);
                } else {
                  type_list.add(gu ? GU_BOND : NORMAL_BOND);
                  type_list.set(mate, gu ? GU_BOND : NORMAL_BOND);
                }
              }
              pt_list.set(mate, pos - 1);
              pt_list.add(mate);
            } else {
              if(pseudoknots.contains(label)) {
                type_list.add(PK1);
                s1.push(pos - 1);
              } else {
                type_list.add(NORMAL_BOND);
                s0.push(pos - 1);
              }
              pt_list.add(-1);
            }
          } else {
            pt_list.add(-1);
            type_list.add(NONE);
          }
          break;
      }
    }
    if(!(s0.empty() && s1.empty() && s2.empty()))
      throw new IllegalArgumentException("Unbalanced pair(s) in structure.");

    // convert lists to arrays
    if(pt_list.size() != type_list.size())
      throw new IllegalArgumentException("Something went wrong...");

    length = pt_list.size();
    pairTable = new int[length];
    bondTypes = new byte[length];

    for(i = 0; i < length; i++) {
      pairTable[i] = pt_list.get(i).intValue();
      bondTypes[i] = type_list.get(i).byteValue();
    }
  }

  // Vienna (dot-bracket) input
  public PairTable(String sequence, String structure) {
    boolean gu;
    int i, mate, sp0, sp1, sp2, sp3, sum;
    int[] s0, s1, s2, s3;
    char c;

    if(structure.length() > sequence.length())
      throw new IllegalArgumentException("Length of structure exceeds length of sequence!");

    length = structure.length();
    s0 = new int[length];
    s1 = new int[length];
    s2 = new int[length];
    s3 = new int[length];
    pairTable = new int[length];
    bondTypes = new byte[length];

    for (sp0 = sp1 = sp2 = sp3 = 0, i = 0; i < length; i++) {
      switch(c = structure.charAt(i)) {
        case '(':
          s0[sp0++] = i;
          break;
        case '[':
          s1[sp1++] = i;
          break;
        case '{':
          s2[sp2++] = i;
          break;
        case '<':
          s3[sp3++] = i;
          break;
        case ')':
          if (--sp0 < 0)
            throw new IllegalArgumentException("Unbalanced braces in "
                                               +"dot-bracket string.");
          mate = s0[sp0];
          pairTable[i] = mate;
          pairTable[mate] = i;
          sum =   sequence.toUpperCase().charAt(i)
                + sequence.toUpperCase().charAt(mate);
          gu = sum == GU || sum == GT;
          bondTypes[i] = bondTypes[mate] = gu ? GU_BOND : NORMAL_BOND;
          break;
        case ']':
          if (--sp1 < 0)
            throw new IllegalArgumentException("Unbalanced braces in "
                                               +"dot-bracket string.");
          mate = s1[sp1];
          pairTable[i] = mate;
          pairTable[mate] = i;
          bondTypes[i] = bondTypes[mate] = PK1;
          break;
        case '}':
          if (--sp2 < 0)
            throw new IllegalArgumentException("Unbalanced braces in "
                                               +"dot-bracket string.");
          mate = s2[sp2];
          pairTable[i] = mate;
          pairTable[mate] = i;
          bondTypes[i] = bondTypes[mate] = PK2;
          break;
        case '>':
          if (--sp3 < 0)
            throw new IllegalArgumentException("Unbalanced braces in "
                                               +"dot-bracket string.");
          mate = s3[sp3];
          pairTable[i] = mate;
          pairTable[mate] = i;
          bondTypes[i] = bondTypes[mate] = PK3;
          break;
        case ':':
        case '.':
          pairTable[i] = -1;
          bondTypes[i] = NONE;
          break;
        default:
          throw new IllegalArgumentException("Unrecognized token '"+c+"' in "
                                              +"dot-bracket string.");
      }
    }

    s0 = s1 = s2 = s3 = null;

    if(sp0 != 0 || sp1 != 0 || sp2 != 0 || sp3 != 0)
      throw new IllegalArgumentException("Unbalanced braces in "
                                         +"dot-bracket string.");

  }

  public int size() {
    return length;
  }

  public int getMate(int pos) {
    return pairTable[pos];
  }

  public byte getType(int pos) {
    return bondTypes[pos];
  }

}
