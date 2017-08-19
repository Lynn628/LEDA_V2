package com.seu.ldea.evolution;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.segment.SliceDataBuild;

public class SliceCorrelation {
       public static void main(String[] args) throws IOException, ParseException{
    		long t1 = System.currentTimeMillis();
    		   /* String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
    			String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
    			String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
    			String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";

    			LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation2
    					.initDataSegment(path, path2, rescalInputDir)
    					.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");
    			LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuildTest//129827
    					.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir,129827, 161771);
    	*/
    			
    			String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
    			 String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ResourcePTMap0722.txt";
    			 String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ClassPTMap-0724.txt";
    			 String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\DBLP";
    			//608035 Book  10367 Article in proced  Article 45075 Article
    			 LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation
    					.initDataSegment(path, path2, rescalInputDir)
    					.segmentDataSet(10367, "http://lsdis.cs.uga.edu/projects/semdis/opus#last_modified_date");
    			LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild
    					.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir, 1, 10367);
    				Iterator<Entry<Integer, HashSet<Integer>>> iterNext = sliceNodes.entrySet().iterator();
    				Iterator<Entry<Integer, HashSet<Integer>>> iterCurr = sliceNodes.entrySet().iterator();
    				iterNext.next();
    			
    				while(iterNext.hasNext()){
    					Entry<Integer, HashSet<Integer>> currEntry = iterCurr.next();
    					Entry<Integer, HashSet<Integer>> nextEntry = iterNext.next();
    					int currSliceId = currEntry.getKey();
    					int nextSliceId = nextEntry.getKey();
    					int sameNodes = 0;
    					HashSet<Integer> currentSet = currEntry.getValue();
    					HashSet<Integer> nextSet = nextEntry.getValue();
    					HashSet<Integer> sameNodeSet = new HashSet<>();
    					sameNodeSet.addAll(currentSet);
    				//	System.out.println("current id " + currSliceId + " next id " + nextSliceId );
    				/*for(int currNode : currentSet){
    						for(int preNode : nextSet){
    							if(currNode == preNode){
    								sameNodes++;
    								//System.out.println(sameNodes);
    							}
    						}
    					}*/
    					sameNodeSet.retainAll(nextSet);
    					double corrForward = sameNodeSet.size()/(currentSet.size()* 1.0);
    					double corrBackward = sameNodeSet.size()/(nextSet.size() * 1.0);
    					/*double corrForward = sameNodes/(currentSet.size()* 1.0);
    					double corrBackward = sameNodes/(nextSet.size() * 1.0);*/
    					System.out.println("current slice size " + currentSet.size() + " " + " next slice size " + nextSet.size());
    					System.out.println("Slice correlation between < " + currSliceId + " , " + nextSliceId + " > : " + corrForward );
    					System.out.println("Slice correlation between < " + nextSliceId + " , " + currSliceId + " > : " + corrBackward);
    				}
       }
}
