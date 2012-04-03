package de.cebitec.vamp.externalSort;

import java.io.File;
//import java.nio.file.Files;
import java.util.ArrayList;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import net.sf.samtools.BAMFileWriter;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author jstraube
 * 
 * 
 * This class sorts Sam or Bam files by read sequence and returns a sorted Bam file.
 * It uses a merge sort which creates temporary files for merging to save memory. Created files 
 * will be removed after sorting.
 */
public class ExternalSortBAM {

// readers and writers needed for the files
    private SAMFileHeader samheader;
    private File sortedFile;
    private String chunkName;
    private InputOutput io;
    private final int chunkSize = 100000;
    private int workunits;
    private ProgressHandle ph;

    public ExternalSortBAM(String path) {
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.output.name"), false);
        io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.start"));
        long start = System.currentTimeMillis();
        this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.progress.name"));
        externalSort(path);
        long time = System.currentTimeMillis() - start;
        ArrayList<Integer> list = getTime(time);
        io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.finished", list.get(0), list.get(1), list.get(2)));
        ph.finish();
      //  io.getOut().close();
    //     io.closeInputOutput();
    }

    
    private void countLines(File base){
                   SAMFileReader samReader = new SAMFileReader(base);
            samheader = samReader.getFileHeader();
            SAMRecordIterator itLine = samReader.iterator();
            int lines = 0;
            while (itLine.hasNext()) {
                itLine.next();
                lines++;
            }
            workunits = lines * 2;
            ph.start(workunits);
            itLine.close();
            samReader.close();
    }
    
    private void externalSort(String path) {
//        try {
            //file input
            File baseFile = new File(path);
            countLines(baseFile);
            
            SAMFileReader samReader = new SAMFileReader(baseFile);
            SAMRecordIterator it = samReader.iterator();

            ArrayList<SAMRecord> chunkSizeRows = new ArrayList<SAMRecord>();

            int numFiles = 0;
            SAMRecord record = null;
            workunits = 0;
            while (it.hasNext()) {
// get chunkSize rows
                for (int i = 0; i < chunkSize; i++) {
                    record = it.hasNext() ? it.next() : null;
                    if (record != null) {
                        chunkSizeRows.add(record);
                    } else {
                        break;
                    }
                }
// sort the rows
               
                chunkSizeRows =mergeSort(chunkSizeRows);

// write to disk
                chunkName = System.getProperty("user.home") + "/chunk";
                File f = new File(chunkName + numFiles);
                BAMFileWriter bfw = new BAMFileWriter(f);

                bfw.setHeader(samheader);
                for (int i = 0; i < chunkSizeRows.size(); i++) {
                    bfw.addAlignment(chunkSizeRows.get(i));
                }
                bfw.close();
                numFiles++;

                workunits += chunkSizeRows.size();
                chunkSizeRows.clear();
                ph.progress(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.progress.chunks.creating"), workunits);
            }
            it.close();
            samReader.close();
            io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.chunks.created", numFiles));
            io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.chunks.merge"));

            mergeFiles(baseFile, numFiles);


          

//        } catch (Exception ex) {
//            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, 
//                    NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.merge.GeneralError"));
//        }

    }

    private void mergeFiles(File baseFile, int numFiles) {
//        try {

            ArrayList<File> files= new ArrayList<File>();
            ArrayList<SAMRecordIterator> mergeIt = new ArrayList<SAMRecordIterator>();
            ArrayList<SAMRecord> filerows = new ArrayList<SAMRecord>();
            String[] s = baseFile.getName().split("\\.");
            String name = baseFile.getParent() + "/sort_" + s[0] + ".bam";
            File sorted = new File(name);
            BAMFileWriter bfw = new BAMFileWriter(sorted);
            bfw.setHeader(samheader);
            boolean someFileStillHasRows = false;

            for (int i = 0; i < numFiles; i++) {
                File f = new File(chunkName + i);
                files.add(f);
                SAMFileReader fileReader = new SAMFileReader(f);
                mergeIt.add(fileReader.iterator());
                // get the first row
                SAMRecord line = mergeIt.get(i).next();
                if (line != null) {
                    filerows.add(line);
                    someFileStillHasRows = true;
                } else {
                    filerows.add(null);
                }

            }

            SAMRecord row;
            while (someFileStillHasRows) {
                String min;
                int minIndex;

                row = filerows.get(0);
                if (row != null) {
                    min = row.getReadString();
                    minIndex = 0;
                } else {
                    min = null;
                    minIndex = -1;
                }

// check which one is min
                for (int i = 1; i < filerows.size(); i++) {
                    row = filerows.get(i);
                    if (min != null) {

                        if (row != null && row.getReadString().compareTo(min) < 0) {
                            minIndex = i;
                            min = filerows.get(i).getReadString();
                        }
                    } else {
                        if (row != null) {
                            min = row.getReadString();
                            minIndex = i;
                        }
                    }
                }

                if (minIndex < 0) {
                    someFileStillHasRows = false;
                } else {
// write to the sorted file
                    bfw.addAlignment(filerows.get(minIndex));
                    ph.progress(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.progress.write.sortedFile"), workunits++);

// get another row from the file that had the min
                    SAMRecord line = mergeIt.get(minIndex).next();
                    if (line != null) {
                        filerows.set(minIndex, line);
                    } else {
                        filerows.set(minIndex, null);
                    }
                }
// check if one still has rows
                for (int i = 0; i < filerows.size(); i++) {

                    someFileStillHasRows = false;
                    if (filerows.get(i) != null) {
                        if (minIndex < 0) {
                            System.out.println("mindex lt 0 and found row not null" + filerows.get(i).toString());
                            System.exit(-1);
                        }
                        someFileStillHasRows = true;
                        break;
                    }
                }

// check the actual files one more time
                if (!someFileStillHasRows) {

//write the last one not covered above
                    for (int i = 0; i < filerows.size(); i++) {
                        if (filerows.get(i) == null) {
                            SAMRecord line = mergeIt.get(i).next();
                            if (line != null) {
                                someFileStillHasRows = true;
                                filerows.set(i, line);
                            }
                        }

                    }
                }

            }



// close all the files
//            bfw.close();
//            sortedFile = sorted;
//            for (int i = 0; i < mergeIt.size(); i++) {
//                mergeIt.get(i).close();
//                fileReaders.get(i).close();
//                try {
//                    Files.delete(files.get(i).toPath());
//                } catch (IOException e) {
//                    io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.merge.FileDeletionError", files.get(i).getAbsolutePath()));
//                }
//            }
            mergeIt.clear();
            filerows.clear();

//        } catch (Exception ex) {
//            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, 
//                    NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.merge.GeneralError"));
//            System.exit(-1);
//        }
    }

    /**
     * sort an arrayList of arrays based on the ith column.
     */
    private static ArrayList<SAMRecord> mergeSort(ArrayList<SAMRecord> arr) {
        ArrayList<SAMRecord> left = new ArrayList<SAMRecord>();
        ArrayList<SAMRecord> right = new ArrayList<SAMRecord>();
        if (arr.size() <= 1) {
            return arr;
        } else {
            int middle = arr.size() / 2;
            for (int i = 0; i < middle; i++) {
                left.add(arr.get(i));
            }
            for (int j = middle; j < arr.size(); j++) {
                right.add(arr.get(j));
            }
            left = mergeSort(left);
            right = mergeSort(right);
            return merge(left, right);

        }

    }

    /**
     * merge the the results for mergeSort back together.
     */
    private static ArrayList<SAMRecord> merge(ArrayList<SAMRecord> left, ArrayList<SAMRecord> right) {
        ArrayList<SAMRecord> result = new ArrayList<SAMRecord>();
        while (left.size() > 0 && right.size() > 0) {
            if (left.get(0).getReadString().compareTo(right.get(0).getReadString()) <= 0) {
                result.add(left.get(0));
                left.remove(0);
            } else {
                result.add(right.get(0));
                right.remove(0);
            }
        }
        if (left.size() > 0) {
            for (int i = 0; i < left.size(); i++) {
                result.add(left.get(i));
            }
        }
        if (right.size() > 0) {
            for (int i = 0; i < right.size(); i++) {
                result.add(right.get(i));
            }
        }
        return result;
    }

    public File getSortedFile() {
        return sortedFile;
    }

    
    private ArrayList<Integer> getTime(long timeInMillis) {
        ArrayList<Integer> timeList = new ArrayList<Integer>();
        int remdr = (int) (timeInMillis % (24L * 60 * 60 * 1000));

        final int hours = remdr / (60 * 60 * 1000);

        remdr %= 60 * 60 * 1000;

        final int minutes = remdr / (60 * 1000);

        remdr %= 60 * 1000;

        final int seconds = remdr / 1000;
        timeList.add(0, hours);
        timeList.add(1, minutes);
        timeList.add(2, seconds);

        return timeList;
    }
}
