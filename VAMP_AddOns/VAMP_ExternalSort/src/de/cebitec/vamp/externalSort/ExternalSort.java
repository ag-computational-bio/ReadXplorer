package de.cebitec.vamp.externalSort;

/**
\
 * @author jstraube
 *  This class sorts any text file by given index and returns a sorted file.
 * It uses a merge sort which creates temporary files for merging to save memory. The created files 
 * will be removed after sorting.
 * 
 */
import java.io.*;
import java.util.ArrayList;

public class ExternalSort {

private String sortedFile;
private String chunkName;

    
    public ExternalSort(String path){
          long start = System.nanoTime();
        externalSort(path);
        long time = System.nanoTime() -start;
        System.out.println("The relation was sorted in "+ time);
    }

    private void externalSort(String path) {
        try {
            //file input
            File baseFile = new File(path);
            FileReader intialRelationInput = new FileReader(baseFile);
            BufferedReader initRelationReader = new BufferedReader(intialRelationInput);
            
            String[] row;
            int indexToCompare = 9;
            ArrayList<String[]> tenKRows = new ArrayList<String[]>();

            int numFiles = 0;
              String line="";
              String header="";
            while (line != null) {
// get 10k rows
                for (int i = 0; i < 10000; i++) {
                    
                     line = initRelationReader.readLine();

                    if(line!=null){
                    if(!line.startsWith("@")){
                       
                    if (line == null) {
                        row = null;
                        break;
                    }
                    row = line.split("\\t");
                    tenKRows.add(row);
                    }else{
                        header = header.concat(line+"\n");
                    }
                     }else{
                        
                        break;
                    }

                }
// sort the rows
                tenKRows = mergeSort(tenKRows, indexToCompare);

// write to disk
                chunkName =  baseFile.getParent()+ "/chunk";     
                BufferedWriter bw = new BufferedWriter(new FileWriter(chunkName+numFiles));
                bw.write(header);
               
                for (int i = 0; i < tenKRows.size(); i++) {
                    bw.append(flattenArray(tenKRows.get(i), "\t") + "\n");
                }
                bw.close();
                numFiles++;
                tenKRows.clear();
            }
            header="";
            mergeFiles(baseFile, numFiles, indexToCompare);



            initRelationReader.close();
            intialRelationInput.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

    }

    private  void mergeFiles(File baseFile, int numFiles, int compareIndex) {
        try {
            ArrayList<FileReader> mergefr = new ArrayList<FileReader>();
            ArrayList<BufferedReader> mergefbr = new ArrayList<BufferedReader>();
            ArrayList<String[]> filerows = new ArrayList<String[]>();
            String name =baseFile.getParent() + "/sort_" + baseFile.getName();
            FileWriter fw = new FileWriter(name);
            BufferedWriter bw = new BufferedWriter(fw);
            boolean someFileStillHasRows = false;
          
            for (int i = 0; i < numFiles; i++) {
                File f = new File(chunkName + i );
                f.deleteOnExit();
                mergefr.add(new FileReader(f));
                mergefbr.add(new BufferedReader(mergefr.get(i)));
                // get the first row
                String line = mergefbr.get(i).readLine();
                if (line != null) {
                 if (line.startsWith("@")) {
                     if(i==0){
                   bw.write(line+ "\n");
                     }
                
                  line = mergefbr.get(i).readLine();
                }
                
                    filerows.add(line.split("\\t"));
                    someFileStillHasRows = true;
                } else {
                    filerows.add(null);
                }

            }
          
//have to write something like put the last string and the incomming string check the lexikographical order if 
            String[] row;
            while (someFileStillHasRows) {
                String min;
                int minIndex;

                row = filerows.get(0);
                if (row != null) {
                    min = row[compareIndex];
                    minIndex = 0;
                } else {
                    min = null;
                    minIndex = -1;
                }

// check which one is min
                for (int i = 1; i < filerows.size(); i++) {
                    row = filerows.get(i);
                    if (min != null) {

                        if (row != null && row[compareIndex].compareTo(min) <0) {
                            minIndex = i;
                            min = filerows.get(i)[compareIndex];
                        }
                    } else {
                        if (row != null) {
                            min = row[compareIndex];
                            minIndex = i;
                        }
                    }
                }

                if (minIndex < 0) {
                    someFileStillHasRows = false;
                } else {
// write to the sorted file
                    bw.append(flattenArray(filerows.get(minIndex), "\t") + "\n");

// get another row from the file that had the min
                    String line = mergefbr.get(minIndex).readLine();
                    if (line != null) {
                        filerows.set(minIndex, line.split("\\t"));
                    } else {
                        filerows.set(minIndex, null);
                    }
                }
// check if one still has rows
                for (int i = 0; i < filerows.size(); i++) {

                    someFileStillHasRows = false;
                    if (filerows.get(i) != null) {
                        if (minIndex < 0) {
                            System.out.println("mindex lt 0 and found row not null" + flattenArray(filerows.get(i), " "));
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
                            String line = mergefbr.get(i).readLine();
                            if (line != null) {

                                someFileStillHasRows = true;
                                filerows.set(i, line.split("\\t"));
                            }
                        }

                    }
                }

            }



// close all the files
            bw.close();
            fw.close();
            for (int i = 0; i < mergefbr.size(); i++) {
                mergefbr.get(i).close();
            }
            for (int i = 0; i < mergefr.size(); i++) {
                mergefr.get(i).close();
               
            }

            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    
    }

// sort an arrayList of arrays based on the ith column
    private static ArrayList<String[]> mergeSort(ArrayList<String[]> arr, int index) {
        ArrayList<String[]> left = new ArrayList<String[]>();
        ArrayList<String[]> right = new ArrayList<String[]>();
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
            left = mergeSort(left, index);
            right = mergeSort(right, index);
            return merge(left, right, index);

        }

    }

// merge the the results for mergeSort back together
    private static ArrayList<String[]> merge(ArrayList<String[]> left, ArrayList<String[]> right, int index) {
        ArrayList<String[]> result = new ArrayList<String[]>();
        while (left.size() > 0 && right.size() > 0) {
            if (left.get(0)[index].compareTo(right.get(0)[index])<=0) {
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



// just a utility function to turn arrays into strings with spaces between each element
    private static String flattenArray(String[] arr, String delimiter) {
        String result = "";
        for (int i = 0; i < arr.length; i++) {
            result += arr[i] + delimiter;
        }

        if (result.endsWith("\t")) {
            result = result.substring(0, result.length() - 1);
        }

        return result.trim();
    }

    public String getSortedFile() {
        return sortedFile;
    }
    
    
    
}