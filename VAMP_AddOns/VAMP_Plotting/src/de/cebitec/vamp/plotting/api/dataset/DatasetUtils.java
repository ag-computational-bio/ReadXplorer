/*
 * Maui, Maltcms User Interface. 
 * Copyright (C) 2008-2012, The authors of Maui. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maui may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maui, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maui is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package de.cebitec.vamp.plotting.api.dataset;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Nils Hoffmann
 */
public class DatasetUtils {

    public static Numeric1DDataset<Point2D> createDataset() {
        INamedElementProvider<? extends List<Point2D>, ? extends Point2D> nep = new INamedElementProvider<List<Point2D>, Point2D>() {
            private List<Point2D> points = createSamplePoints(500);

            @Override
            public List<Point2D> getSource() {
                return points;
            }

            @Override
            public Comparable getKey() {
                return "Sample dataset";
            }

            @Override
            public void setKey(Comparable key) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int size() {
                return points.size();
            }

            @Override
            public Point2D get(int i) {
                return points.get(i);
            }

            @Override
            public List<Point2D> get(int start, int stop) {
                return points.subList(start, stop);
            }

            @Override
            public void reset() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        List<INamedElementProvider<? extends List<Point2D>, ? extends Point2D>> l = new ArrayList<INamedElementProvider<? extends List<Point2D>, ? extends Point2D>>();
        l.add(nep);
        return new Numeric1DDataset<Point2D>(l);
    }

    public static List<Point2D> createSamplePoints(int n) {
        Random r = new Random(System.nanoTime());
        List<Point2D> points = new ArrayList<Point2D>();
        for (int i = 0; i < n; i++) {
			double random = Math.random();
			if(random>0.95) {
				points.add(new Point2D.Double(r.nextDouble() * 256.0d,Double.POSITIVE_INFINITY));
                                points.add(new Point2D.Double(r.nextDouble() * 256.0d,Double.NEGATIVE_INFINITY));
			}else{
				points.add(new Point2D.Double(2 * i + (r.nextGaussian() - 0.5d), r.nextDouble() * 256.0d));
			}
        }
        points.add(new Point2D.Double(200, 300));
        points.add(new Point2D.Double(100, Double.POSITIVE_INFINITY));
        return points;
    }
    
    private static class SortablePair {

        private final int index;
        private final double value;
        
        public SortablePair(int index, double value) {
            this.index = index;
            this.value = value;
        }
        
        public int getIndex() {
            return this.index;
        }
        
        public double getValue() {
            return this.value;
        }
        
    }
    
    private static class SortablePairComparator implements Comparator<SortablePair> {

        @Override
        public int compare(SortablePair t, SortablePair t1) {
            return Double.compare(t.getValue(), t1.getValue());
        }
        
    }

    public static int[] ranks(double[] domainValues, boolean descending) {
        SortablePair[] sp = new SortablePair[domainValues.length];
        for (int i = 0; i < domainValues.length; i++) {
            SortablePair sortablePair = new SortablePair(i, domainValues[i]);
            sp[i] = sortablePair;
        }
        Comparator<SortablePair> comp = new SortablePairComparator();
        if(descending) {
            Arrays.sort(sp, Collections.reverseOrder(comp));
        }else{
            Arrays.sort(sp, comp);
        }
        final int[] ranks = new int[domainValues.length];
        for (int i = 0; i < sp.length; i++) {
            ranks[i] = sp[i].getIndex();
        }
        return ranks;
    }
}
