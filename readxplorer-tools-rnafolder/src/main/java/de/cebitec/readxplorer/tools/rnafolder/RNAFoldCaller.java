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

package de.cebitec.readxplorer.tools.rnafolder;


import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;


/**
 * Executes RNAFold on the bibiserv of the Bielefeld University at
 * http://bibiserv.techfak.uni-bielefeld.de/rnafold/submission.html with the
 * given intput data and returns the resulting string.
 * <p>
 * @author Rolf Hilker
 */
public final class RNAFoldCaller {


    /**
     * Instantiation not allowed.
     */
    private RNAFoldCaller() {
    }


    /**
     * Calls http://bibiserv.techfak.uni-bielefeld.de/rnafold/submission.html
     * with the given string and returns the result string of the program.
     * <p>
     * @param selSequence the sequence to start RNA folder with
     * @param header      header string for the query
     * <p>
     * @return the resulting folded rna string
     * <p>
     * @throws RNAFoldException
     */
    @SuppressWarnings( "SleepWhileHoldingLock" )
    public static String callRNAFolder( String selSequence, String header ) throws RNAFoldException {

        selSequence = ">".concat( header ).concat( "\r\n" ).concat( selSequence );

        /* declare addresslocation for service */
        final String server = "http://bibiwsserv.techfak.uni-bielefeld.de";

        try {
            /* declare where to find the describing WSDL */
            final URL wsdl = new URL( "http://bibiserv.techfak.uni-bielefeld.de/wsdl/RNAfold.wsdl" );

            /* declare where to find the describing WSDL */
            if( selSequence == null || selSequence.isEmpty() ) {
                throw new RNAFoldException( NbBundle.getMessage( RNAFoldCaller.class, "RFException.HighlightError" ) );
                //System.err.println("java RNAfoldCOrig -F <FastaFile> [-T <double>] \n"); //return popup with msg
            }

            /* prepare the call (the same for all called methods) */
            Service ser = new Service( wsdl, new QName( server + "/RNAfold/axis/RNAfoldPort", "RNAfoldImplementationService" ) );
            Call call = (Call) ser.createCall( new QName( "RNAfoldPort" ), "request_orig" );
            /* call and get id */
            String id = (String) call.invoke( new Object[]{ new Object[]{ "T", 37.0 }, selSequence } );

            int statuscode = 601;
            while( (statuscode > 600) && (statuscode < 700) ) {
                try {
                    Thread.sleep( 2500 );
                    call = (Call) ser.createCall( new QName( "RNAfoldPort" ), "response_orig" );
                    // call and get result as DOM Tree(if finished)
                    return (String) call.invoke( new Object[]{ id } );

                } catch( InterruptedException e ) {
                    throw new RNAFoldException( NbBundle.getMessage( RNAFoldCaller.class, "RFException.ThreadError" ) );
                } catch( RemoteException e ) {
                    // on error WS will throw a soapfault as hobitstatuscode
                    Element root = ((AxisFault) e).lookupFaultDetail( new QName(
                            "http://hobit.sourceforge.net/xsds/hobitStatuscode.xsd", "hobitStatuscode" ) );
                    if( root == null ) {
                        throw new RNAFoldException( NbBundle.getMessage( RNAFoldCaller.class, "RFException.RemoteError" ) + e.toString() );
                    }
                    //String description = root.getLastChild().getFirstChild().getNodeValue();
                    statuscode = Integer.parseInt( root.getFirstChild().getFirstChild().getNodeValue() );
                    // print error to parent
                    //throw new RNAFoldException("(" + statuscode + " - " + description + ")");
                }
            }

            /* error handling with proper information for the user */

        } catch( RemoteException e ) {
            /* on error WS will throw a soapfault as hobitstatuscode */
            Element root = ((AxisFault) e).lookupFaultDetail( new QName(
                    "http://hobit.sourceforge.net/xsds/hobitStatuscode.xsd", "hobitStatuscode" ) );
            if( root == null ) {
                throw new RNAFoldException( NbBundle.getMessage( RNAFoldCaller.class, "RFException.ThreadError" ) + " " + e.toString() );
            } else {
                String description = root.getLastChild().getFirstChild().getNodeValue();
                String code = root.getFirstChild().getFirstChild().getNodeValue();
                throw new RNAFoldException( "Remote Error: Statuscode:  " + code + ", Description: " + description );
            }

            /*
             * Using this kind of Webservice there is only one one field for
             * returning an error message. When an axception occours, the client
             * side of Axis will throw a RemoteException which includes the
             * class name of the thrown exception. There is no way to get more
             * information, like the original stacktrace !!!
             */
        } catch( MalformedURLException e ) {
            throw new RNAFoldException( NbBundle.getMessage( RNAFoldCaller.class, "RFException.URLError" ) );
            //System.err.println("failed (" + e.toString() + ")");
        } catch( ServiceException e ) {
            throw new RNAFoldException( NbBundle.getMessage( RNAFoldCaller.class, "RFException.ServiceError" ) + " " + server );
        }

        //should never be reached!
        return "";
    }


}
