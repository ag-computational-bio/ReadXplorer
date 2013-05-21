package org.jfree.chart.axis;

import java.awt.geom.Rectangle2D;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;

public class InfNumberAxis extends NumberAxis {

    public InfNumberAxis(String yaxis) {
        super(yaxis);
    }

    /**
     * Converts a data value to a coordinate in Java2D space, assuming that the
     * axis runs along one edge of the specified dataArea.
     * <p>
     * Note that it is possible for the coordinate to fall outside the plotArea.
     *
     * @param value the data value.
     * @param area the area for plotting the data.
     * @param edge the axis location.
     *
     * @return The Java2D coordinate.
     *
     * @see #java2DToValue(double, Rectangle2D, RectangleEdge)
     */
    @Override
    public double valueToJava2D(double value, Rectangle2D area,
            RectangleEdge edge) {
        double ret;
        Range range = getRange();
        double axisMin = range.getLowerBound();
        double axisMax = range.getUpperBound();

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getMaxX();
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            max = area.getMinY();
            min = area.getMaxY();
        }
        if (Double.isInfinite(value)) {
            if (isInverted()) {
                if (value == Double.POSITIVE_INFINITY) {
                    ret = min - 4;
                } else {
                    ret = max + 4;
                }
            } else {
                if (value == Double.POSITIVE_INFINITY) {
                    ret = max + 4;
                } else {
                    ret = min - 4;
                }
            }
        } else {
            if (isInverted()) {
                ret = max - ((value - axisMin) / (axisMax - axisMin)) * (max - min);
            } else {
                ret = min + ((value - axisMin) / (axisMax - axisMin)) * (max - min);
            }
        }
        return ret;
    }

    /**
     * Rescales the axis to ensure that all data is visible.
     */
    @Override
    protected void autoAdjustRange() {
//
//        Plot plot = getPlot();
//        if (plot == null) {
//            return;  // no plot, no data
//        }
//                
//
//        if (plot instanceof ValueAxisPlot) {
//            ValueAxisPlot vap = (ValueAxisPlot) plot;
//            Range r = vap.getDataRange(this);
//            if (r == null) {
//                r = getDefaultAutoRange();
//            }
//
//            double upper = r.getUpperBound();
//            double lower = r.getLowerBound();
//            if (getRangeType() == RangeType.POSITIVE) {
//                lower = Math.max(0.0, lower);
//                upper = Math.max(0.0, upper);
//            } else if (getRangeType() == RangeType.NEGATIVE) {
//                lower = Math.min(0.0, lower);
//                upper = Math.min(0.0, upper);
//            }
//
//            if (getAutoRangeIncludesZero()) {
//                lower = Math.min(lower, 0.0);
//                upper = Math.max(upper, 0.0);
//            }
//            double range = upper - lower;
//
//            // if fixed auto range, then derive lower bound...
//            double fixedAutoRange = getFixedAutoRange();
//            if (fixedAutoRange > 0.0) {
//                lower = upper - fixedAutoRange;
//            } else {
//                // ensure the autorange is at least <minRange> in size...
//                double minRange = getAutoRangeMinimumSize();
//                if (range < minRange) {
//                    double expand = (minRange - range) / 2;
//                    upper = upper + expand;
//                    lower = lower - expand;
//                    if (lower == upper) { // see bug report 1549218
//                        double adjust = Math.abs(lower) / 10.0;
//                        lower = lower - adjust;
//                        upper = upper + adjust;
//                    }
//                    if (getRangeType() == RangeType.POSITIVE) {
//                        if (lower < 0.0) {
//                            upper = upper - lower;
//                            lower = 0.0;
//                        }
//                    } else if (getRangeType() == RangeType.NEGATIVE) {
//                        if (upper > 0.0) {
//                            lower = lower - upper;
//                            upper = 0.0;
//                        }
//                    }
//                }
//
//                if (getAutoRangeStickyZero()) {
//                    if (upper <= 0.0) {
//                        upper = Math.min(0.0, upper + getUpperMargin() * range);
//                    } else {
//                        upper = upper + getUpperMargin() * range;
//                    }
//                    if (lower >= 0.0) {
//                        lower = Math.max(0.0, lower - getLowerMargin() * range);
//                    } else {
//                        lower = lower - getLowerMargin() * range;
//                    }
//                } else {
//                    upper = upper + getUpperMargin() * range;
//                    lower = lower - getLowerMargin() * range;
//                }
//            }
//
//            setRange(new Range(lower, upper), false, false);
//        }
    }
}
