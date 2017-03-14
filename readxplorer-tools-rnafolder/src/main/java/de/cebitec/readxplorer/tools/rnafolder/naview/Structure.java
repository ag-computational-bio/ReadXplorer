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

package de.cebitec.readxplorer.tools.rnafolder.naview;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NAVIEW -- A program to make a modified radial drawing of an RNA secondary
 * structure.
 * <p>
 * Copyright (c) 1988 Robert E. Bruccoleri Copying of this software, in whole or
 * in part, is permitted provided that the copies are not made for commercial
 * purposes, appropriate credit for the use of the software is given, this
 * copyright notice appears, and notice is given that the copying is by
 * permission of Robert E. Bruccoleri. Any other copying requires specific
 * permission.
 * <p>
 * See R. Bruccoleri and G. Heinrich, Computer Applications in the Biosciences,
 * 4, 167-173 (1988) for a full description.
 * <p>
 * In November 1997, Michael Zuker made a number of changes to bring naview up
 * to modern standards. All functions defined in naview are now declared before
 * main() with arguments and argument types. When functions are defined, their
 * argument types are declared with the function and these definitions are
 * removed after the '{'. The 'void' declaration was used as necessary for
 * functions.
 * <p>
 * The troublesome na_scanf function was deleted and replaced by scanf. Finally,
 * there is now no default for the minimum separation of bases. A floating point
 * number must be entered. However, as before an entry < 0 will be moved up to 0 and an entry
 * > 0.5 will be reduced to 0.5.
 * <p>
 * Adapted for use as a subroutine in the Vienna RNA Package by Ivo Hofacker,
 * May 1998: deleted output routines, replaced main() by
 * naview_xy_coordinates(), which fills the X and Y arrays used by PS_rna_plot()
 * etc. added ansi prototypes and fixed memory leaks.
 * <p>
 * Adapted for use in RNAMovies and therefor translated to java by Alexander
 * Kaiser, April 2006.
 * <p>
 * @author R. Bruccoleri (1988), I. Hofacker (1998), A. Kaiser (2006).
 * @version NAVIEW-Java 0.5.
 * <p>
 */
public class Structure {

    private static final Logger LOG = LoggerFactory.getLogger( Structure.class.getName() );

    private static final double ANUM = 9999.0;
    private static final double pi = Math.PI;
    private static final int maxiter = 500;

    private Base[] bases;
    private List<Region> regions;
    private List<Loop> loops;
    private PairTable pt;
    private int nbase = 0;
    private int nregion = 0;
    private int nloop = 0;
    private double xmin, xmax, ymin, ymax;
    private Loop root = null;


    // Vienna (dot-bracket) input
    public Structure( String sequence, String structure ) {
        this( new PairTable( sequence, structure ) );
    }


    // DCSE input
    public Structure( String sequence, String structure, String helices ) {
        this( new PairTable( sequence, structure, helices ) );
    }


    public Structure( PairTable pt ) {
        int i;

        nbase = pt.size();
        this.pt = pt;

        // initialize arrays for bases, regions and loops
        bases = new Base[nbase + 1];
        regions = new ArrayList<>( nbase + 1 );
        loops = new ArrayList<>( nbase + 1 );

        // build up data structures
        readBases();
        findRegions();
        constructLoop( 0 );
        findCentralLoop();

        // show debug output
        dumpLoops();

        // calculate positions of bases
        traverseLoop( root, null );

        // get min and max point;
        xmin = bases[1].x;
        ymin = bases[1].y;
        xmax = bases[1].x;
        ymax = bases[1].y;

        for( i = 2; i <= nbase; i++ ) {
            if( bases[i].x < xmin ) {
                xmin = bases[i].x;
            }
            if( bases[i].x > xmax ) {
                xmax = bases[i].x;
            }
            if( bases[i].y < ymin ) {
                ymin = bases[i].y;
            }
            if( bases[i].y > ymax ) {
                ymax = bases[i].y;
            }
        }

    }


    public PairTable getPairTable() {
        return pt;
    }


    public int getWidth() {
        return getWidth( 1 );
    }


    public int getWidth( double xscale ) {
        return (int) Math.round( xscale * (xmax - xmin) );
    }


    public int getHeight() {
        return getHeight( 1 );
    }


    public int getHeight( double yscale ) {
        return (int) Math.round( yscale * (ymax - ymin) );
    }


    public Point2D[] getNormalizedCoordinates( double xscale, double yscale,
                                               double xoff, double yoff ) {
        return this.getCoordinates( xscale, yscale,
                                    xoff - xscale * xmin, yoff - yscale * ymin );
    }


    public Point2D[] getCoordinates( double xscale, double yscale,
                                     double xoff, double yoff ) {
        int i;
        Point2D[] pa;

        pa = new Point2D[nbase];
        for( i = 0; i < nbase; i++ ) {
            pa[i] = new Point2D.Double( xscale * bases[i + 1].x + xoff,
                                        yscale * bases[i + 1].y + yoff );
        }
        return pa;
    }


    public Point2D[] getCoordinates() {
        return this.getCoordinates( 1, 1, 0, 0 );
    }


    public int length() {
        return pt.size();
    }


    public Base[] getBases() {
        return bases;
    }


    public Loop[] getLoops() {
        return loops.toArray( new Loop[]{} );
    }


    public Region[] getRegions() {
        return regions.toArray( new Region[]{} );
    }


    public Loop getCentralLoop() {
        return root;
    }

//------------------private methods--------------------------------------------

    private void generateRegion( Connection connection ) {
        int l, start, end, i, mate;
        Region region;

        region = connection.region;
        l = 0;
        if( connection.start == region.start1 ) {
            start = region.start1;
            end = region.end1;
        } else {
            start = region.start2;
            end = region.end2;
        }
        if( bases[connection.start].x > ANUM - 100.0 ||
            bases[connection.end].x > ANUM - 100.0 ) {
            LOG.error( "Bad region passed to generateRegion. " +
                       "Coordinates not defined." );
            return;
        }
        for( i = start + 1; i <= end; i++ ) {
            l++;
            bases[i].x = bases[connection.start].x + l * connection.xrad;
            bases[i].y = bases[connection.start].y + l * connection.yrad;
            mate = bases[i].mate;
            bases[mate].x = bases[connection.end].x + l * connection.xrad;
            bases[mate].y = bases[connection.end].y + l * connection.yrad;
        }
    }


    private static Point2D findCenterForArc( int n, double b ) {
        double h, hhi, hlow, r, disc, theta, e, phi;
        int iter;

        hhi = (n + 1) / pi;
        hlow = -hhi - b / (n + 1.000001 - b);
        if( b < 1 ) {
            hlow = 0;
        }
        iter = 0;
        do {
            h = (hhi + hlow) / 2.0;
            r = Math.sqrt( h * h + b * b / 4.0 );
            disc = 1.0 - 0.5 / (r * r);
            if( Math.abs( disc ) > 1.0 ) {
                LOG.error( "Unexpected large magnitude discriminant: " + disc );
                return new Point2D.Double( 0.0, 0.0 );
            }
            theta = Math.acos( disc );
            phi = Math.acos( h / r );
            e = theta * (n + 1) + 2 * phi - 2 * pi;
            if( e > 0.0 ) {
                hlow = h;
            } else {
                hhi = h;
            }
        } while( Math.abs( e ) > 0.0001 && ++iter < maxiter );
        if( iter >= maxiter ) {
            LOG.error( "Iteration failed in findCenterForArc" );
            return new Point2D.Double( 0.0, 0.0 );
        }
        return new Point2D.Double( h, theta );
    }


    private void constructCircleSegment( int start, int end ) {
        double dx, dy, rr, h, angleinc, midx, midy, xn, yn, nrx, nry, mx, my, a;
        int l, j, i;
        Point2D p;

        dx = bases[end].x - bases[start].x;
        dy = bases[end].y - bases[start].y;
        rr = Math.sqrt( dx * dx + dy * dy );
        l = end - start;
        if( l < 0 ) {
            l += nbase + 1;
        }
        if( rr >= l ) {
            dx /= rr;
            dy /= rr;
            for( j = 1; j < l; j++ ) {
                i = start + j;
                if( i > nbase ) {
                    i -= nbase + 1;
                }
                bases[i].x = bases[start].x + dx * (double) j / (double) l;
                bases[i].y = bases[start].y + dy * (double) j / (double) l;
            }
        } else {
            p = findCenterForArc( l - 1, rr );
            h = p.getX();
            angleinc = p.getY();
            dx /= rr;
            dy /= rr;
            midx = bases[start].x + dx * rr / 2.0;
            midy = bases[start].y + dy * rr / 2.0;
            xn = dy;
            yn = -dx;
            nrx = midx + h * xn;
            nry = midy + h * yn;
            mx = bases[start].x - nrx;
            my = bases[start].y - nry;
            rr = Math.sqrt( mx * mx + my * my );
            a = Math.atan2( my, mx );
            for( j = 1; j < l; j++ ) {
                i = start + j;
                if( i > nbase ) {
                    i -= nbase + 1;
                }
                bases[i].x = nrx + rr * Math.cos( a + j * angleinc );
                bases[i].y = nry + rr * Math.sin( a + j * angleinc );
            }
        }
    }


    private void constructExtrudedSegment( Connection connection,
                                           Connection cnext ) {
        double astart, aend1, aend2, aave, dx, dy, a1, a2, ac, rr, da, dac;
        int start, end, n, nstart, nend;
        boolean collision;

        astart = connection.angle;
        aend2 = aend1 = cnext.angle;
        if( aend2 < astart ) {
            aend2 += 2 * pi;
        }
        aave = (astart + aend2) / 2.0;
        start = connection.end;
        end = cnext.start;
        n = end - start;
        if( n < 0 ) {
            n += nbase + 1;
        }
        da = cnext.angle - connection.angle;
        if( da < 0.0 ) {
            da += 2 * pi;
        }
        if( n == 2 ) {
            constructCircleSegment( start, end );
        } else {
            dx = bases[end].x - bases[start].x;
            dy = bases[end].y - bases[start].y;
            rr = Math.sqrt( dx * dx + dy * dy );
            dx /= rr;
            dy /= rr;
            if( rr >= 1.5 && da <= pi / 2 ) {
                nstart = start + 1;
                if( nstart > nbase ) {
                    nstart -= nbase + 1;
                }
                nend = end - 1;
                if( nend < 0 ) {
                    nend += nbase + 1;
                }
                bases[nstart].x = bases[start].x + 0.5 * dx;
                bases[nstart].y = bases[start].y + 0.5 * dy;
                bases[nend].x = bases[end].x - 0.5 * dx;
                bases[nend].y = bases[end].y - 0.5 * dy;
                start = nstart;
                end = nend;
            }
            do {
                collision = false;
                constructCircleSegment( start, end );
                nstart = start + 1;
                if( nstart > nbase ) {
                    nstart -= nbase + 1;
                }
                dx = bases[nstart].x - bases[start].x;
                dy = bases[nstart].y - bases[start].y;
                a1 = Math.atan2( dy, dx );
                if( a1 < 0.0 ) {
                    a1 += 2 * pi;
                }
                dac = a1 - astart;
                if( dac < 0.0 ) {
                    dac += 2 * pi;
                }
                if( dac > pi ) {
                    collision = true;
                }
                nend = end - 1;
                if( nend < 0 ) {
                    nend += nbase + 1;
                }
                dx = bases[nend].x - bases[end].x;
                dy = bases[nend].y - bases[end].y;
                a2 = Math.atan2( dy, dx );
                if( a2 < 0.0 ) {
                    a2 += 2 * pi;
                }
                dac = aend1 - a2;
                if( dac < 0.0 ) {
                    dac += 2 * pi;
                }
                if( dac > pi ) {
                    collision = true;
                }
                if( collision ) {
                    ac = Math.min( aave, astart + 0.5 );
                    bases[nstart].x = bases[start].x + Math.cos( ac );
                    bases[nstart].y = bases[start].y + Math.sin( ac );
                    start = nstart;
                    ac = Math.max( aave, aend2 - 0.5 );
                    bases[nend].x = bases[end].x + Math.cos( ac );
                    bases[nend].y = bases[end].y + Math.sin( ac );
                    end = nend;
                    n -= 2;
                }
            } while( collision && n > 1 );
        }
    }


    private static int getMiddleConnection( int icstart, int icend,
                                            Connection anchor, Connection croot,
                                            Loop loop ) {
        int count, ret, ic, i, nconn;
        boolean done;
        Connection[] connections;

        connections = loop.getConnections();
        nconn = connections.length;
        count = 0;
        ret = -1;
        ic = icstart;
        done = false;
        while( !done ) {
            if( ++count > nconn * 2 ) {
                LOG.error( "infinite loop detected" );
                return -1;
            }
            if( anchor != null && connections[ic] == croot ) {
                ret = ic;
            }
            done = ic == icend;
            if( ++ic >= nconn ) {
                ic = 0;
            }
        }
        if( ret == -1 ) {
            for( i = 1, ic = icstart; i < (count + 1) / 2; i++ ) {
                if( ++ic >= nconn ) {
                    ic = 0;
                }
            }
            ret = ic;
        }
        return ret;
    }


    private void traverseLoop( Loop loop, Connection anchor ) {
        int icroot, icstart, icend, icmiddle;
        int imaxloop, icstart1, icnext, ic, icup, icdown, direction;
        int i, j, n, count, nconn, sign, bar;
        double xs, ys, xe, ye, xn, yn, angleinc, maxang, r;
        double rr, rc, rcn, radius, xc, yc, xo, yo;
        double cx, cy, cnx, cny, cnextx, cnexty, lnx, lny, rl, sx, sy;
        double dx, dy;
        double midx, midy, nmidx, nmidy, nrx, nry, mx, my, vx, vy, dotmv;
        double da, dan, dc, a, ac, acn, astart, aend;
        boolean done, done_all, rooted;
        Connection croot, cprev, cnext, connection;
        Connection[] connections;

        connections = loop.getConnections();
        nconn = connections.length;

        angleinc = 2 * pi / (nbase + 1);
        imaxloop = 0;
        icroot = -1;
        croot = null;
        for( ic = 0; ic < nconn; ic++ ) {
            connection = connections[ic];
            xs = -Math.sin( angleinc * connection.start );
            ys = Math.cos( angleinc * connection.start );
            xe = -Math.sin( angleinc * connection.end );
            ye = Math.cos( angleinc * connection.end );
            xn = ye - ys;
            yn = xs - xe;
            r = Math.sqrt( xn * xn + yn * yn );
            connection.xrad = xn / r;
            connection.yrad = yn / r;
            connection.angle = Math.atan2( yn, xn );
            if( connection.angle < 0.0 ) {
                connection.angle += 2 * pi;
            }
            if( anchor != null &&
                anchor.region.equals( connection.region ) ) {
                icroot = ic;
                croot = connection;
            }
        }

        set_radius: // I deserve to be hung for this ...
        for( ;; ) {
            radius = loop.getRadius( nbase );

            if( anchor == null ) {
                xc = yc = 0.0;
            } else {
                xo = (bases[croot.start].x + bases[croot.end].x) / 2.0;
                yo = (bases[croot.start].y + bases[croot.end].y) / 2.0;
                xc = xo - radius * croot.xrad;
                yc = yo - radius * croot.yrad;
            }

            icstart = icroot == -1 ? 0 : icroot;
            connection = connections[icstart];
            count = 0;

            LOG.trace( "Now processing Loop #" + loop.number +
                       " (radius=" + radius + ")" );

            done = false;
            do {
                j = icstart - 1;
                if( j < 0 ) {
                    j = nconn - 1;
                }
                cprev = connections[j];
                if( !cprev.isConnected( connection ) ) {
                    done = true;
                } else {
                    icstart = j;
                    connection = cprev;
                }
                if( ++count > nconn ) {
                    maxang = -1.0;
                    for( ic = 0; ic < nconn; ic++ ) {
                        j = ic + 1;
                        if( j >= nconn ) {
                            j = 0;
                        }
                        connection = connections[ic];
                        cnext = connections[j];
                        ac = cnext.angle - connection.angle;
                        if( ac < 0.0 ) {
                            ac += 2 * pi;
                        }
                        if( ac > maxang ) {
                            maxang = ac;
                            imaxloop = ic;
                        }
                    }
                    icend = imaxloop;
                    icstart = imaxloop + 1;
                    if( icstart >= nconn ) {
                        icstart = 0;
                    }
                    connection = connections[icend];
                    connection.broken = true;
                    done = true;
                }
            } while( !done );
            done_all = false;
            icstart1 = icstart;
            LOG.trace( "icstart1 = " + icstart1 );
            while( !done_all ) {
                count = 0;
                done = false;
                rooted = false;
                icend = icstart;
                while( !done ) {
                    connection = connections[icend];
                    if( icend == icroot ) {
                        rooted = true;
                    }
                    j = icend + 1;
                    if( j >= nconn ) {
                        j = 0;
                    }
                    cnext = connections[j];
                    if( connection.isConnected( cnext ) ) {
                        if( ++count >= nconn ) {
                            break;
                        }
                        icend = j;
                    } else {
                        done = true;
                    }
                }
                icmiddle = getMiddleConnection( icstart, icend, anchor, croot, loop );
                ic = icup = icdown = icmiddle;
                LOG.trace( "icstart = " + icstart + ", icmiddle = " + icmiddle +
                           ", icend = " + icend );
                done = false;
                direction = 0;
                while( !done ) {
                    if( direction < 0 ) {
                        ic = icup;
                    } else if( direction == 0 ) {
                        ic = icmiddle;
                    } else {
                        ic = icdown;
                    }
                    if( ic >= 0 ) {
                        connection = connections[ic];
                        if( anchor == null || croot != connection ) {
                            if( direction == 0 ) {
                                astart = connection.angle - Math.asin( 1.0 / 2.0 / radius );
                                aend = connection.angle + Math.asin( 1.0 / 2.0 / radius );
                                bases[connection.start].x = xc + radius * Math.cos( astart );
                                bases[connection.start].y = yc + radius * Math.sin( astart );
                                bases[connection.end].x = xc + radius * Math.cos( aend );
                                bases[connection.end].y = yc + radius * Math.sin( aend );
                            } else if( direction < 0 ) {
                                j = ic + 1;
                                if( j >= nconn ) {
                                    j = 0;
                                }
                                connection = connections[ic];
                                cnext = connections[j];
                                cx = connection.xrad;
                                cy = connection.yrad;
                                ac = (connection.angle + cnext.angle) / 2.0;
                                if( connection.angle > cnext.angle ) {
                                    ac -= pi;
                                }
                                cnx = Math.cos( ac );
                                cny = Math.sin( ac );
                                lnx = cny;
                                lny = -cnx;
                                da = cnext.angle - connection.angle;
                                if( da < 0.0 ) {
                                    da += 2 * pi;
                                }
                                if( connection.extruded ) {
                                    rl = da <= pi / 2 ? 2.0 : 1.5;
                                } else {
                                    rl = 1.0;
                                }
                                bases[connection.end].x = bases[cnext.start].x + rl * lnx;
                                bases[connection.end].y = bases[cnext.start].y + rl * lny;
                                bases[connection.start].x = bases[connection.end].x + cy;
                                bases[connection.start].y = bases[connection.end].y - cx;
                            } else {
                                j = ic - 1;
                                if( j < 0 ) {
                                    j = nconn - 1;
                                }
                                connection = connections[j];
                                cnext = connections[ic];
                                cnextx = cnext.xrad;
                                cnexty = cnext.yrad;
                                ac = (connection.angle + cnext.angle) / 2.0;
                                if( connection.angle > cnext.angle ) {
                                    ac -= pi;
                                }
                                cnx = Math.cos( ac );
                                cny = Math.sin( ac );
                                lnx = -cny;
                                lny = cnx;
                                da = cnext.angle - connection.angle;
                                if( da < 0.0 ) {
                                    da += 2 * pi;
                                }
                                if( connection.extruded ) {
                                    rl = da <= pi / 2 ? 2.0 : 1.5;
                                } else {
                                    rl = 1.0;
                                }
                                bases[cnext.start].x = bases[connection.end].x + rl * lnx;
                                bases[cnext.start].y = bases[connection.end].y + rl * lny;
                                bases[cnext.end].x = bases[cnext.start].x - cnexty;
                                bases[cnext.end].y = bases[cnext.start].y + cnextx;
                            }
                        }
                    }
                    if( direction < 0 ) {
                        if( icdown == icend ) {
                            icdown = -1;
                        } else if( icdown >= 0 ) {
                            if( ++icdown >= nconn ) {
                                icdown = 0;
                            }
                        }
                        direction = 1;
                    } else {
                        if( icup == icstart ) {
                            icup = -1;
                        } else if( icup >= 0 ) {
                            if( --icup < 0 ) {
                                icup = nconn - 1;
                            }
                        }
                        direction = -1;
                    }
                    done = icup == -1 && icdown == -1;
                }
                icnext = icend + 1;
                if( icnext >= nconn ) {
                    icnext = 0;
                }
                if( icend != icstart && (!(icstart == icstart1 && icnext == icstart1)) ) {
                    connection = connections[icstart];
                    cnext = connections[icend];
                    dx = bases[cnext.end].x - bases[connection.start].x;
                    dy = bases[cnext.end].y - bases[connection.start].y;
                    midx = bases[connection.start].x + dx / 2.0;
                    midy = bases[connection.start].y + dy / 2.0;
                    rr = Math.sqrt( dx * dx + dy * dy );
                    mx = dx / rr;
                    my = dy / rr;
                    vx = (xc - midx) / rr;
                    vy = (yc - midy) / rr;
                    dotmv = vx * mx + vy * my;
                    nrx = dotmv * mx - vx;
                    nry = dotmv * my - vy;
                    rr = Math.sqrt( nrx * nrx + nry * nry );
                    nrx /= rr;
                    nry /= rr;
                    dx = bases[connection.start].x - xc;
                    dy = bases[connection.start].y - yc;
                    ac = Math.atan2( dy, dx );
                    if( ac < 0.0 ) {
                        ac += 2 * pi;
                    }
                    dx = bases[cnext.end].x - xc;
                    dy = bases[cnext.end].y - yc;
                    acn = Math.atan2( dy, dx );
                    if( acn < 0.0 ) {
                        acn += 2 * pi;
                    }
                    if( acn < ac ) {
                        acn += 2 * pi;
                    }
                    if( acn - ac > pi ) {
                        sign = -1;
                    } else {
                        sign = 1;
                    }
                    nmidx = xc + sign * radius * nrx;
                    nmidy = yc + sign * radius * nry;
                    if( rooted ) {
                        xc -= nmidx - midx;
                        yc -= nmidy - midy;
                    } else {
                        for( ic = icstart; true; bar = ++ic >= nconn ? (ic = 0) : 0 ) {
                            connection = connections[ic];
                            bases[connection.start].x += nmidx - midx;
                            bases[connection.start].y += nmidy - midy;
                            bases[connection.end].x += nmidx - midx;
                            bases[connection.end].y += nmidy - midy;
                            if( ic == icend ) {
                                break;
                            }
                        }
                    }
                }
                icstart = icnext;
                done_all = icstart == icstart1;
            }
            for( ic = 0; ic < nconn; ic++ ) {
                connection = connections[ic];
                j = ic + 1;
                if( j >= nconn ) {
                    j = 0;
                }
                cnext = connections[j];
                dx = bases[connection.end].x - xc;
                dy = bases[connection.end].y - yc;
                rc = Math.sqrt( dx * dx + dy * dy );
                ac = Math.atan2( dy, dx );
                if( ac < 0.0 ) {
                    ac += 2 * pi;
                }
                dx = bases[cnext.start].x - xc;
                dy = bases[cnext.start].y - yc;
                rcn = Math.sqrt( dx * dx + dy * dy );
                acn = Math.atan2( dy, dx );
                if( acn < 0.0 ) {
                    acn += 2 * pi;
                }
                if( acn < ac ) {
                    acn += 2 * pi;
                }
                dan = acn - ac;
                dc = cnext.angle - connection.angle;
                if( dc <= 0.0 ) {
                    dc += 2 * pi;
                }
                if( Math.abs( dan - dc ) > pi ) {
                    if( connection.extruded ) {
                        LOG.warn( "Loop #" + loop.number + " has crossed regions" );
                    } else if( (cnext.start - connection.end) != 1 ) {
                        connection.extruded = true;
                        continue set_radius;
                    }
                }
                if( connection.extruded ) {
                    constructExtrudedSegment( connection, cnext );
                } else {
                    n = cnext.start - connection.end;
                    if( n < 0 ) {
                        n += nbase + 1;
                    }
                    angleinc = dan / n;
                    for( j = 1; j < n; j++ ) {
                        i = connection.end + j;
                        if( i > nbase ) {
                            i -= nbase + 1;
                        }
                        a = ac + j * angleinc;
                        rr = rc + (rcn - rc) * (a - ac) / dan;
                        bases[i].x = xc + rr * Math.cos( a );
                        bases[i].y = yc + rr * Math.sin( a );
                    }
                }
            }
            for( ic = 0; ic < nconn; ic++ ) {
                if( icroot != ic ) {
                    connection = connections[ic];
                    generateRegion( connection );
                    traverseLoop( connection.loop, connection );
                }
            }
            n = 0;
            sx = 0.0;
            sy = 0.0;
            for( ic = 0; ic < nconn; ic++ ) {
                j = ic + 1;
                if( j >= nconn ) {
                    j = 0;
                }
                connection = connections[ic];
                cnext = connections[j];
                n += 2;
                sx += bases[connection.start].x + bases[connection.end].x;
                sy += bases[connection.start].y + bases[connection.end].y;
                if( !connection.extruded ) {
                    for( j = connection.end + 1; j != cnext.start; j++ ) {
                        if( j > nbase ) {
                            j -= nbase + 1;
                        }
                        n++;
                        sx += bases[j].x;
                        sy += bases[j].y;
                    }
                }
            }
            loop.x = sx / n;
            loop.y = sy / n;
            break;
        }
    }


    private void dumpLoops() {
        int i;
        Loop loop;

        LOG.trace( "Root loop is: #" + root.number );
        for( i = 0; i < loops.size(); i++ ) {
            loop = loops.get( i );
            LOG.trace( loop.toString() );

            for( Connection connection : loop.connections ) {
                LOG.trace( "  " + connection.toString() );
            }
        }
    }


    private void findCentralLoop() {
        int i, d, maxconn, maxdepth;
        Loop loop;

        maxconn = 0;
        maxdepth = -1;

        for( i = 0; i < loops.size(); i++ ) {
            loop = loops.get( i );

            if( loop.connections.size() > maxconn ) {
                maxdepth = loop.getDepth();
                maxconn = loop.connections.size();
                root = loop;
            } else if( (d = loop.getDepth()) > maxdepth &&
                       loop.connections.size() == maxconn ) {
                maxdepth = d;
                root = loop;
            }
        }
    }


    private Loop constructLoop( int ib ) {
        int i, mate;
        Loop loop, retloop;
        Region region;

        retloop = new Loop( nloop++ );
        loops.add( retloop );

        i = ib;
        do {
            if( (mate = bases[i].mate) != 0 ) {
                region = bases[i].region;

                if( !bases[region.start1].extracted ) {
                    if( i == region.start1 ) {
                        bases[region.start1].extracted = true;
                        bases[region.end1].extracted = true;
                        bases[region.start2].extracted = true;
                        bases[region.end2].extracted = true;
                        loop = constructLoop( region.end1 < nbase ? region.end1 + 1 : 0 );
                    } else if( i == region.start2 ) {
                        bases[region.start1].extracted = true;
                        bases[region.end1].extracted = true;
                        bases[region.start2].extracted = true;
                        bases[region.end2].extracted = true;
                        loop = constructLoop( region.end2 < nbase ? region.end2 + 1 : 0 );
                    } else {
                        LOG.error( "Error in constructLoop: " +
                                   i + " not found in region table." );
                        return null;
                    }

                    if( i == region.start1 ) {
                        retloop.connect( region.start1, region.end2, loop, region );
                        loop.connect( region.start2, region.end1, retloop, region );
                    } else {
                        retloop.connect( region.start2, region.end1, loop, region );
                        loop.connect( region.start1, region.end2, retloop, region );
                    }
                }
                i = mate;
            }
            if( ++i > nbase ) {
                i = 0;
            }
        } while( i != ib );
        return retloop;
    }


    private void findRegions() {
        int i, mate;
        boolean mark[];
        Region region;

        mark = new boolean[nbase + 1];
        for( i = 0; i <= nbase; i++ ) {
            mark[i] = false;
        }

        for( i = 0; i <= nbase; i++ ) {

            if( (mate = bases[i].mate) > 0 && !mark[i] ) {
                region = new Region( nregion++, i, 0, 0, mate );
                regions.add( region );
                mark[i] = true;
                mark[mate] = true;
                bases[i].region = bases[mate].region = region;

                for( i++, mate--;
                     i < mate && bases[i].mate == mate;
                     i++, mate-- ) {
                    mark[i] = mark[mate] = true;
                    bases[i].region = bases[mate].region = region;
                }

                region.end1 = --i;
                region.start2 = mate + 1;

                LOG.trace( region.toString() );
            }
        }
    }


    private void readBases() {
        int i, npairs;

        bases[0] = new Base( 0 );

        for( npairs = 0, i = 0; i < pt.size(); i++ ) {
            if( pt.getType( i ) > PairTable.NONE && pt.getType( i ) < PairTable.PK1 ) {
                bases[i + 1] = new Base( pt.getMate( i ) + 1 );

                if( pt.getMate( i ) > i ) {
                    npairs++;
                }
            } else {
                bases[i + 1] = new Base( 0 );
            }
        }

        if( npairs == 0 ) {
            bases[1].mate = nbase;
            bases[nbase].mate = 1;
        }
    }


}
