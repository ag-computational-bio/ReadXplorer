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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies.util;


import de.cebitec.readxplorer.tools.rnafolder.naview.PairTable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


public class ShapeOps {

    public Stroke backboneStroke = new BasicStroke( 2f,
                                                    BasicStroke.CAP_ROUND,
                                                    BasicStroke.JOIN_ROUND );
    public Stroke bondStroke = new BasicStroke( 1.5f );
    public Stroke labelStroke = new BasicStroke( 1.0f );
    public Color backboneColor = Color.BLACK;
    public Color bondColor = Color.BLACK;
    public Color textColor = Color.BLACK;
    public Color background = Color.WHITE;
    public Color aColor = Color.BLACK;
    public Color cColor = Color.BLACK;
    public Color gColor = Color.BLACK;
    public Color uColor = Color.BLACK;
    public Color pseudoknotColor = Color.RED;
    public Color labelColor = new Color( 204, 204, 204 );
    public boolean showBases = false;
    public boolean showBackbone = true;
    public boolean showBonds = true;
    public boolean baseColors = false;
    public boolean show5End = true;
    public boolean show3End = true;
    public boolean showNumberLabels = true;
    public boolean showPseudoknots = true;
    public double rotAngle = 0;
    public int distance = 25;
    public int period = 10;


    public static Point2D interpolate( Point2D a,
                                       Point2D b,
                                       int step,
                                       int numstep ) {
        double f;

        if( numstep <= 0 || step < 0 || step > numstep ) {
            throw new IllegalArgumentException( "Illegal argument(s) passed " +
                     "to interpolate " +
                     ((numstep <= 0) ? "(numstep[" + numstep + "] <= 0) " : "") +
                     ((step < 0) ? "(step[" + step + "] < 0) " : "") +
                     ((step > numstep) ? "(step[" + step + "] > numstep[" + numstep + "])" : "") );
        }

        if( a.equals( b ) || step == 0 ) {
            return a;
        }

        if( step == numstep ) {
            return b;
        }

        f = 1 - (numstep - step) / (double) numstep;

        return new Point2D.Double( a.getX() + f * (b.getX() - a.getX()),
                                   a.getY() + f * (b.getY() - a.getY()) );

    }


    public static Line2D resizeLine( Point2D p1, Point2D p2, double dl ) {
        return resizeLine( p1, dl, p2, dl );
    }


    public static Point2D midPoint( Point2D p1, Point2D p2 ) {
        return new Point2D.Double( p1.getX() + 0.5 * (p2.getX() - p1.getX()),
                                   p1.getY() + 0.5 * (p2.getY() - p1.getY()) );
    }


    public static Line2D resizeLine( Point2D p1,
                                     double dl1,
                                     Point2D p2,
                                     double dl2 ) {
        double l, f1, f2;

        l = p1.distance( p2 );

        if( l + (dl1 + dl2) <= 0 ) {
            return new Line2D.Double( p1.getX() + 0.5 * (p2.getX() - p1.getX()),
                                      p1.getY() + 0.5 * (p2.getY() - p1.getY()),
                                      p1.getX() + 0.5 * (p2.getX() - p1.getX()),
                                      p1.getY() + 0.5 * (p2.getY() - p1.getY()) );
        }

        f1 = (l + dl1) / l;
        f2 = (l + dl2) / l;

        return new Line2D.Double( p1.getX() + f2 * (p2.getX() - p1.getX()),
                                  p1.getY() + f2 * (p2.getY() - p1.getY()),
                                  p2.getX() + f1 * (p1.getX() - p2.getX()),
                                  p2.getY() + f1 * (p1.getY() - p2.getY()) );
    }


    public static Point2D transRot( Point2D p, Point2D c, double theta ) {
        double sinTheta, cosTheta;
        double x, y, x_rt, y_rt;

        sinTheta = Math.sin( theta );
        cosTheta = Math.cos( theta );

        x = p.getX() - c.getX();
        y = p.getY() - c.getY();
        x_rt = cosTheta * x - sinTheta * y + c.getX();
        y_rt = sinTheta * x + cosTheta * y + c.getY();

        return new Point2D.Double( x_rt, y_rt );
    }


    public void getBounds( Point2D[] structure,
                           Point2D center,
                           Dimension d,
                           Point2D origin ) {
        int i;
        double xmin, ymin, xmax, ymax;
        Point2D p;

        p = transRot( structure[0], center, rotAngle );

        xmin = xmax = p.getX();
        ymin = ymax = p.getY();

        for( i = 1; i < structure.length; i++ ) {
            p = transRot( structure[i], center, rotAngle );
            xmin = p.getX() < xmin ? p.getX() : xmin;
            ymin = p.getY() < ymin ? p.getY() : ymin;
            xmax = p.getX() > xmax ? p.getX() : xmax;
            ymax = p.getY() > ymax ? p.getY() : ymax;
        }

        d.setSize( xmax - xmin + 10, ymax - ymin + 10 );
        origin.setLocation( xmin - 5.0, ymin - 5.0 );
    }


    public void draw( Graphics2D gc,
                      String chars,
                      Point2D[] structure,
                      Point2D center,
                      PairTable pt ) {
        drawInterpolate( gc, chars, structure, structure, center, pt, 1, 1 );
    }


    private static double signedDist( double x1,
                                      double y1,
                                      double x2,
                                      double y2,
                                      double x,
                                      double y ) {
        double a, b, c;

        a = y2 - y1;
        b = x1 - x2;
        c = x2 * y1 - x1 * y2;

        return (a * x + b * y + c) / Math.sqrt( a * a + b * b );
    }


    private static double angle( double x1, double y1,
                                 double x2, double y2,
                                 double x3, double y3 ) {
        double x1x2, y1y2, x3x2, y3y2;
        double dot, l, cosalpha;

        x1x2 = x1 - x2;
        y1y2 = y1 - y2;
        x3x2 = x3 - x2;
        y3y2 = y3 - y2;

        dot = x1x2 * x3x2 + y1y2 * y3y2;
        l = Math.sqrt( x1x2 * x1x2 + y1y2 * y1y2 ) * Math.sqrt( x3x2 * x3x2 + y3y2 * y3y2 );
        cosalpha = dot / l;

        if( Math.abs( cosalpha ) >= 1d ) {
            return Math.PI;
        }

        return Math.acos( cosalpha );
    }


    private void drawLabel( Graphics2D gc,
                            Point2D d,
                            Point2D h,
                            Point2D e,
                            String label ) {
        double f, l, l2, sgn, a, aabs, dl;
        double rx, ry, rw, rh;
        Point2D g, m;
        Rectangle2D r;
        Font font;
        FontMetrics metrics;

        //calculate angle between d and e
        a = angle( d.getX(),
                   d.getY(),
                   h.getX(),
                   h.getY(),
                   e.getX(),
                   e.getY() );

        //determine wether the angle is > pi
        l2 = signedDist( d.getX(), d.getY(),
                         e.getX(), e.getY(),
                         h.getX(), h.getY() );

        sgn = Math.abs( l2 ) > 0.0 ? Math.abs( l2 ) / l2 : 1.0;

        //construct the angle bisector
        a *= 0.5 * sgn;
        a += 2 * Math.PI;
        m = transRot( e, h, a );
        l = m.distance( h );

        //calculate distance to center from string and its absolute angle
        metrics = gc.getFontMetrics();
        r = metrics.getStringBounds( label, gc );

        aabs = Math.atan2( m.getY() - h.getY(), m.getX() - h.getX() );
        rw = r.getWidth();
        rh = r.getHeight() - metrics.getLeading();
        rx = r.getX();
        ry = r.getY();

        dl = 5 + (0.5 * rw > 3 ? Math.abs( Math.cos( aabs ) ) * (0.5 * rw - 3) : 0);

        f = sgn * ((float) distance + dl + sgn * l) / l;

        g = new Point2D.Double( m.getX() + f * (h.getX() - m.getX()),
                                m.getY() + f * (h.getY() - m.getY()) );

        gc.setColor( labelColor );
        gc.setStroke( labelStroke );
        gc.draw( resizeLine( h, showBases ? -5 : 0, g, -dl ) );
        gc.drawString( label,
                       (float) (g.getX() - (rx + 0.5 * rw)),
                       (float) (g.getY() - (ry + 0.5 * rh)) );
    }


    public void drawInterpolate( Graphics2D gc,
                                 String chars,
                                 Point2D[] curr,
                                 Point2D[] next,
                                 Point2D center,
                                 PairTable pt,
                                 int step,
                                 int numstep ) {
        int i, mate, length;
        Point2D a = null;
        Point2D b = null;
        Point2D c = null;
        Point2D d, m;
        Point2D e = null;
        Ellipse2D el;

        length = Math.min( curr.length, next.length );
        if( chars.length() < length ) {
            throw new IllegalArgumentException( "Illegal argument(s) passed " +
                     "to drawInterpolate." );
        }
        if( pt.size() < length ) {
            throw new IllegalArgumentException( "Illegal argument(s) passed " +
                     "to drawInterpolate." );
        }

        if( length > 2 ) {
            e = interpolate( curr[length - 1], next[length - 1], step, numstep );
            e = transRot( e, center, rotAngle );
            a = interpolate( curr[0], next[0], step, numstep );
            a = transRot( a, center, rotAngle );
            b = interpolate( curr[1], next[1], step, numstep );
            b = transRot( b, center, rotAngle );
            if( show5End ) {
                drawLabel( gc, e, a, b, "5'" );
            } else if( period > 0 && showNumberLabels ) {
                drawLabel( gc, e, a, b, "1" );
            }

            c = interpolate( curr[length - 2], next[length - 2], step, numstep );
            c = transRot( c, center, rotAngle );
            if( show3End ) {
                drawLabel( gc, c, e, a, "3'" );
            } else if( period > 0 && showNumberLabels && length % period == 0 ) {
                drawLabel( gc, c, e, a, String.valueOf( length ) );
            }
        }

        for( i = 0; i < length; i++ ) {
            if( i == 0 ) {
                a = interpolate( curr[i], next[i], step, numstep );
                a = transRot( a, center, rotAngle );
            } else {
                e = a;
                a = b;
            }

            if( i < length - 1 ) {
                b = interpolate( curr[i + 1], next[i + 1], step, numstep );
                b = transRot( b, center, rotAngle );
                if( period > 0 &&
                         showNumberLabels &&
                         (i + 1) % period == 0 ) {
                    drawLabel( gc, e, a, b, String.valueOf( i + 1 ) );
                }
            }

            mate = pt.getMate( i );
            if( mate < length && mate != -1 ) {
                c = interpolate( curr[mate], next[mate], step, numstep );
                c = transRot( c, center, rotAngle );
                // draw pseudo-knots
                if( mate <= i &&
                         pt.getType( i ) >= PairTable.PK1 &&
                         showPseudoknots ) {
                    if( pt.getType( i < 1 ? length - 1 : i - 1 ) != pt.getType( i ) ) {
                        gc.setStroke( bondStroke );
                        gc.setColor( pseudoknotColor );
                        gc.draw( showBases ? resizeLine( a, c, -5 ) : new Line2D.Double( a, c ) );
                    }
                }

                // draw normal bonds or pseudoknots
                if( mate > i ) {
                    switch( pt.getType( i ) ) {
                        case PairTable.NONE:
                            break;
                        case PairTable.NORMAL_BOND:
                            if( showBonds ) {
                                gc.setStroke( bondStroke );
                                gc.setColor( bondColor );
                                gc.draw( showBases ? resizeLine( a, c, -5 ) : new Line2D.Double( a, c ) );
                            }
                            break;
                        case PairTable.KNOT:
                            if( showBonds ) {
                                gc.setStroke( bondStroke );
                                gc.setColor( backboneColor );
                                gc.draw( showBases ? resizeLine( a, c, -5 ) : new Line2D.Double( a, c ) );
                            }
                            break;
                        case PairTable.GU_BOND:
                            if( showBonds ) {
                                gc.setColor( bondColor );
                                m = midPoint( a, c );
                                gc.fill( new Ellipse2D.Double( m.getX() - 2, m.getY() - 2, 4, 4 ) );
                            }
                            break;
                        case PairTable.KNOT_GU:
                            if( showBonds ) {
                                gc.setColor( backboneColor );
                                m = midPoint( a, c );
                                gc.fill( new Ellipse2D.Double( m.getX() - 2, m.getY() - 2, 4, 4 ) );
                            }
                            break;
                        default:
                            if( pt.getType( i < 1 ? length - 1 : i - 1 ) != pt.getType( i ) &&
                                     showPseudoknots ) {
                                gc.setStroke( bondStroke );
                                gc.setColor( pseudoknotColor );
                                gc.draw( showBases ? resizeLine( a, c, -5 ) : new Line2D.Double( a, c ) );
                            }
                    }
                }
            }

            // draw backbone
            if( i < length - 1 ) {
                if( showBackbone ) {
                    gc.setStroke( backboneStroke );
                    if( pt.getType( i ) >= PairTable.PK1 &&
                             pt.getType( i >= length - 1 ? 0 : i + 1 ) >= PairTable.PK1 &&
                             pt.getType( mate < 1 ? length - 1 : mate - 1 ) == pt.getType( i ) &&
                             showPseudoknots ) {
                        gc.setColor( pseudoknotColor );
                    } else {
                        gc.setColor( backboneColor );
                    }
                    gc.draw( showBases ? resizeLine( a, b, -5 ) : new Line2D.Double( a, b ) );
                }
            }

            // draw bases
            if( showBases ) {
                if( baseColors ) {
                    switch( chars.charAt( i ) ) {
                        case 'A':
                        case 'a':
                            gc.setColor( aColor );
                            break;
                        case 'C':
                        case 'c':
                            gc.setColor( cColor );
                            break;
                        case 'G':
                        case 'g':
                            gc.setColor( gColor );
                            break;
                        case 'U':
                        case 'u':
                        case 'T':
                        case 't':
                            gc.setColor( uColor );
                            break;
                        default:
                            gc.setColor( textColor );
                    }
                } else {
                    gc.setColor( textColor );
                }
                gc.drawString( chars.substring( i, i + 1 ),
                               (float) a.getX() - 4f, (float) a.getY() + 3f );
            }
        }
    }


    public static void center( Point2D[] structure,
                               Dimension size,
                               Dimension area ) {
        int i;
        double x, y;

        x = (area.getWidth() - size.getWidth()) / 2;
        y = (area.getHeight() - size.getHeight()) / 2;

        for( i = 0; i < structure.length; i++ ) {
            structure[i].setLocation( structure[i].getX() + x,
                                      structure[i].getY() + y );
        }
    }


}
