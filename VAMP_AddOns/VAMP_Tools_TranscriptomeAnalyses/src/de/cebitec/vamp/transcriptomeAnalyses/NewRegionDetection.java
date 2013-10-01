//package de.cebitec.vamp.transcriptomeAnalyses;
//
//import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
//import java.util.HashMap;
//import java.util.List;
//
///**
// *
// * @author jritter
// */
//public class NewRegionDetection {
//
//    
//    public NewRegionDetection() {
//        
//        
//    }
//    
//    
//    
//     private void runningNewRegionsDetection(int refSeqLength, HashMap<Integer, 
//             List<Integer>> forwardCDSs, HashMap<Integer, List<Integer>> reverseCDSs, 
//             HashMap<Integer, PersistantFeature> allRegionsInHash, int[] fwdCoverage, 
//             int[] revCoverage, int i, int[] forward, int[] reverse, int ratio, 
//             int mm, boolean opt_T, int bg, int up, int down) {
//        
//        int length = refSeqLength;
//        int fwdcount = forward[i];
////    # for the reverse side, we have to come "from the right"
////    # to avoid code duplication, @reverse is searched backwards
//            
//        int rev_i = length - i - 1;
//        int revcount = reverse[rev_i];
//
////    # got through possible forward hits first
//    if (fwdcount == 0) { 
//	int j = 0;
//	int end = 0;
//	
////	# check if the hits can be attributed to a region (up to 700bp downstream)
//	while ((forwardCDSs.get(i+j-end).get(0)) == 0 || j > 700) {
//	    if ((i+j) > length) {
//                end = length;
//            }
//	    j++;
//	} 
//
//	if (forwardCDSs.get(i+j-end).get(0) != 0) {
////	    # if such region(s) exist 
//	    for(int regionID : forwardCDSs.get(i+j-end)) {
//		if (i < allRegionsInHash.get(regionID).getStart()){
////		    # ...and the hit is upstream of the start...
//		    if (fwdcount > bg) {
////			# ...and the number crosses the threshold...
//			int flag;
//			for (int k = 0; ((i+k-end) <= (i+j-end)); k++) {
////			    # and there is sigificant covarage till the old start is reached
//			    if (revCoverage[i+k-end] < bg) {
//                            flag = (i+k-end);
//                        }
//			    if (flag != 0) {
//				new_regs{fwd}{flag}++;
//				break;
//			    }
//			}
////			# then move the start upstream
//			unless (flag) { 
////			    # if it is not set to a "static" value
//			    unless (static_regions{regionID}{start}) {
//				all_regions{regionID}{start} = i;
//			    }
//			} else {
//			    int start = i + 1;
//			    int pos = flag + 1;
//			    String site = "intergenic";
//			    if (reverseCDSs.get(start).get(0) == 0 || reverseCDSs.get(pos).get(0) == 0) {
//                                site = "cis-antisense";
//                            }
//			    push(@{$new_regs{out}{$start}}, "$pos\t+\t$fwd\t$site") unless ($new_regs{fwd}{$flag} > 1);
//			}
//		    }
//		}
//	    }
//	} else {
////	    # if the count crosses the threshold far from a gene
//	    if (fwdcount > bg) {
//		int k = 0;
////		# search for the drop off
//		while (fwdCoverage[i+k-end] > bg){
//		    k++;
//		}
//		int start = i + 1;
//		int flag = i+k-end;
//		new_regs{fwd}{flag}++;
//		int pos = flag + 1;
////		# and report the likely transcript
//		String site = "intergenic";
//		if (reverseCDSs.get(start).get(0) == 0 || reverseCDSs.get(pos).get(0) == 0) {
//                    site = "cis-antisense";
//                }
//		push(@{$new_regs{out}{$start}}, "$pos\t+\t$fwd\t$site") unless ($new_regs{fwd}{$flag} > 1);
//	    }
//	}
//     }
//    
////    # now repeat for the reverse side, but go from right to left
//    if (rev) {
//	int j = 0;
//	int end = 0;
//	while ((reverseCDSs.get(end+rev_i-j).get(0)) == 0 || j > 700) {
//	    if ((rev_i-j) == 0) {
//                end = length;
//            }
//	    j++;
//	} 
//	if (reverseCDSs.get(end+rev_i-j).get(0) == 0) {
//	    for (int regionID : reverseCDSs.get(end+rev_i-j)) {
//		if (rev_i > allRegionsInHash.get(regionID).getStart()) {
//		    if (rev > bg) {
//			int flag;
//			for (int k = 0; (end+rev_i-k) >= (end+rev_i-j); k++) {
//			    if (coverage{rev}[end+rev_i-k] < bg) {
//                            flag = (end+rev_i-k);
//                        }
//			    if (flag) {
//				new_regs{rev}{flag}++;
//				break;
//			    }
//			}
//			unless (flag) { 
//			    unless ($static_regions{$region}{start}) {
//				$all_regions{$region}{start} = $rev_i;
//			    }
//			} else {
//			    int start = rev_i + 1;
//			    int pos = flag + 1;
//			    String site = "intergenic";
//			    if ($cds_forward[$start][0] || $cds_forward[$pos][0]) {
//                                site = "cis-antisense";
//                            }
//			    unless ($new_regs{rev}{$flag} > 1) {
//                            push(@{$new_regs{out}{$start}}, "$pos\t-\t$rev\t$site") ;
//                        }
//			}
//		    }
//		}
//	    }
//	} else {
//	    if (rev > bg) {
//		int k = 0;
//		while (coverage{rev}[end+rev_i-k]  > bg ){
//		    k++;
//		}
//		int start = rev_i + 1;
//		int flag = end+rev_i-k;
//		$new_regs{rev}{flag}++;
//		int pos = flag + 1;
//		String site = "intergenic";
//		if (forwardCDSs.get(start).get(0) == 0 || forwardCDSs.get(pos).get(0) == 0) {
//                    site = "cis-antisense";
//                }
//		unless ($new_regs{rev}{$flag} > 1) {
//                push(@{$new_regs{out}{$start}}, "$pos\t-\t$rev\t$site");
//            }
//	    }
//	}
//    }
//     }
////}
