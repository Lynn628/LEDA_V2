package com.seu.ldea.time;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.seu.ldea.commonutil.BuildFromFile;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;

/**
 * ��LabelClasswithtime �е�classPTMapת��ΪInteractive matrix
 * @author Lynn
 *
 */

public class InteractiveMatrix {
	/**
	 * ���ݴ����ȵ�ÿ��class�Լ���Ӧ��ʱ�����Լ�ֵ
	 * @param classPTMap
	 * @return
	 */
	public static HashMap<Integer, HashMap<String, TimeSpan>> getInteractiveMatrix(HashMap<Integer, HashMap<String, TimeSpan>> classPTMap){
		HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix = classPTMap;
		//�����е�ʱ�� ����
		HashSet<String> propertySet =  new HashSet<>();
		//�����е�class id �� String��map
	//	HashMap<Integer, String> classMap = new HashMap<>();
		//ͳ���ж�����Ч��ʱ�����Ժ�ʱ������
		Iterator<Entry<Integer, HashMap<String, TimeSpan>>> iteratorOuter = interactiveMatrix.entrySet().iterator();

		while(iteratorOuter.hasNext()){
		     Entry<Integer, HashMap<String, TimeSpan>> entry = iteratorOuter.next();
			 HashMap<String, TimeSpan> pts = entry.getValue();
			// System.out.println("pts size before  " + pts.size());
			 Iterator<Entry<String, TimeSpan>> iterator = pts.entrySet().iterator();
			 while(iterator.hasNext()){
				 Entry<String, TimeSpan> pt = iterator.next();
				 String property = pt.getKey();
				 propertySet.add(property);
		     }
		}
		//�������class���߱���ʱ������
		for(Entry<Integer, HashMap<String, TimeSpan>> entry : interactiveMatrix.entrySet()){
			 HashMap<String, TimeSpan> pts = entry.getValue();
			 //System.out.println("pts before " + pts.size()) ;
			 for(String property : propertySet){
				
				// System.out.println("property -- " + property);
				 if(!pts.containsKey(property)){
					 pts.put(property, new TimeSpan("", ""));
				 }
			 }
		}
		//System.out.println("class # " + interactiveMatrix.size());
		//System.out.println("time property #  " + propertySet.size());
		return interactiveMatrix;
	}
	

	
	/**
	 * 
	 * @param interactiveMatrix
	 * @param name
	 * @param dir
	 * @throws IOException
	 */
	public static void showInteractiveMatrix(HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix, String name, String dir) throws IOException{
		       //���д���ĵ�
				String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\" + name + ".txt";
				HashMap<Integer, String> uriMap = ResourceInfo.getReourceURIMap(dir);
				FileWriter fileWriter = new FileWriter(dstFile);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				for (Entry<Integer, HashMap<String, TimeSpan>> entry : interactiveMatrix.entrySet()) {
				   System.out.println(entry.getKey() + " ");
					bufferedWriter.write(entry.getKey() + ": " + uriMap.get(entry.getKey()));
					bufferedWriter.newLine();
					HashMap<String, TimeSpan> classInfo = entry.getValue();
					for(Entry<String, TimeSpan> entry2 : classInfo.entrySet()){
						System.out.print(entry2.getKey() + " -- " + entry2.getValue().toString() + "  ");
					bufferedWriter.write(entry2.getKey() + " -- " + entry2.getValue().toString());
					bufferedWriter.newLine();
				}
					System.out.println("\n----");
					bufferedWriter.newLine();
					bufferedWriter.flush();
				}
				bufferedWriter.close();
	}
	
    public static void main(String[] args) throws IOException, ParseException{
	    long t1 = System.currentTimeMillis();
	    String dir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\DBLP";
	/*	Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/LinkedMDB.org", "dba", "dba");
		LabelClassWithTime.resourceTimeInfo = LabelResourceWithTimeTest2.timeExtraction(dataset, "LinkedMDB-ResourcePTMap0709-1",
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\LinkedMDB2");
		//System.out.println("resourceTimeInfoSize in Main ---" + resourceTimeInfo.size());
       //�������ݿ�����		
   	     HashMap<Integer, HashMap<String, TimeSpan>> classPTMap2 = LabelClassWithTime.getClassTimeSpanInfo(
   		 LabelClassWithTime.getClassTimeInformation("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\LinkedMDB2" , "LinkedMDB-ClassPTMapSet0709-1"),  "LinkedMDB-ClassPTMap0709-1");
		 showInteractiveMatrix( getInteractiveMatrix(classPTMap2), "LinkedMDB2-Interactive-Matrix-0709-1" );*/
		 //�����ļ�����
		 HashMap<Integer, HashMap<String, TimeSpan>> classPTMap  = BuildFromFile.getClassPTMap("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ClassPTMap-0724.txt");
	   	 showInteractiveMatrix(getInteractiveMatrix(classPTMap) , "DBLP-Interactie-Matrix-from-File-0724",dir );
		
	   	long t2 = System.currentTimeMillis();
		double timeCost = (t2 - t1) / 1000.0; 
		System.out.println(timeCost + " ----------s ");
    }
}
