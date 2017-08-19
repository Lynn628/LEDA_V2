package com.seu.ldea.entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
/**
 * ʱ��ʵ���࣬����ʱ����Ϣ��ʵ��
 * @author Lynn
 *
 */
public class ResourceInfo {
	// ����Id,���������
	Integer reourceId;
	//��Դ������
	Integer type;
	
	// �洢ʱ����Ϣ��<pId,<t1, t2>>��
	HashMap<String, HashSet<TimeSpan>> timeInfoPair = new HashMap<>();
	public ResourceInfo(Integer rId) {
		this.reourceId = rId;
			// TODO Auto-generated constructor stub
		}
		
	public void setType(Integer type) {
		this.type = type;
	}
	
	public Integer getType() {
		return type;
	}
	

	public HashMap<String, HashSet<TimeSpan>> getPredicateTimeMap() {
		return timeInfoPair;
	}
   
	public void setPredicateTimeMap(HashMap<String, HashSet<TimeSpan>> timeInfoPair) {
		// TODO Auto-generated method stub
		this.timeInfoPair = timeInfoPair;
	}
	public String toString() {
		Iterator<Entry<String, HashSet<TimeSpan>>> iter = timeInfoPair.entrySet().iterator();
		String spanStr = "";
		System.out.println("The time predicate # on this Resource -----" + timeInfoPair.entrySet().size());
		while (iter.hasNext()) {
			Entry<String, HashSet<TimeSpan>> entry = iter.next();
			String pre = (String) entry.getKey();
			HashSet<TimeSpan> spanSet = (HashSet<TimeSpan>) entry.getValue();
			spanStr += "< " + pre + ", ";
			for (TimeSpan span : spanSet) {
				spanStr += span.toString() + "; ";
			}
			spanStr += "> ";
		}
		return spanStr;
	}

	/**
	 * ���ļ���ȡ�����͵�ʵ���Լ���ʵ������
	 * @param dir
	 * @return
	 * @throws IOException 
	 */
	public static HashMap<Integer, Integer> getReourceTypeMap(String dir) throws IOException{
		int num = 0;
		HashMap<Integer, Integer> resourceType = new HashMap<>();
		String wordsFile = dir + "\\words";
		FileReader fileReader = new FileReader(wordsFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			int index = line.indexOf(":");
			String subStr1 = line.substring(0, index);
			String subStr2 = line.substring(index + 1);
			if (subStr2.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				num = Integer.parseInt(subStr1);
				break;
			}
		}
		// �����ļ�
		String colFile = dir + "\\" + num + "-cols";
		// �����ļ�
		String rowFile = dir + "\\" + num + "-rows";
		FileReader fr1 = new FileReader(colFile);
		FileReader fr2 = new FileReader(rowFile);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		String bString1 = br1.readLine();
		String bString2 = br2.readLine();
		br1.close();
		br2.close();
		// �洢��
		String[] bArr1 = bString1.split(" ");
		// �洢Entity
		String[] bArr2 = bString2.split(" ");
		bufferedReader.close();
		
		for (int i = 0; i < bArr1.length; i++) {
		//	System.out.println("entity id is " + bArr2[i] + " class id is " + bArr1[i]);
			int entityId = Integer.parseInt(bArr2[i]);
			
			int classId = Integer.parseInt(bArr1[i]);
		    resourceType.put(entityId, classId);
	  }
	    return resourceType;
	}
	

	/**
	 * ���ļ���ȡ�����͵�ʵ���Լ���ʵ������
	 * @param dir
	 * @return
	 * @throws IOException 
	 */
	public static HashMap<Integer, Integer> getReourceTypeMap2(String dir) throws IOException{
		int num = 0;
		HashMap<Integer, Integer> resourceType = new HashMap<>();
		String tripleFile = dir + "\\triple";
		FileReader fileReader = new FileReader(tripleFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
		//	int index = line.indexOf(":");
			String[] lineArr = line.split(" ");
			if(Integer.parseInt(lineArr[1]) == 1){
				resourceType.put(Integer.parseInt(lineArr[0]), Integer.parseInt(lineArr[2]));
		
			}
		}
	    bufferedReader.close();
	    return resourceType;
	}
	
	/**
	 * ���ļ�ÿ����Դ��id �� URIӳ��
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer, String> getReourceURIMap(String dir) throws IOException{
		HashMap<Integer, String> resourceURI = new HashMap<>();
		String entityidFile = dir + "\\entity-ids";
		FileReader fr = new FileReader(entityidFile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while((line = br.readLine()) != null){
		    int index = line.indexOf(":");
		    int id = Integer.parseInt(line.substring(0, index));
		   String uri = line.substring(index+1);
			resourceURI.put(id, uri);
		}
		br.close();
		return resourceURI;
	}
	
	/**
	 * ��ȡ���ݼ���������
	 * @param dir��RESCAL�����ļ���Ŀ¼��ַ
	 * @return
	 * @throws IOException
	 */
	public  static HashMap<Integer, String > getAllClass(String dir) throws IOException{
		HashMap<Integer, String> result = new HashMap<>();
		HashMap<Integer, String> uriMap =  getReourceURIMap(dir);
		int num = 0;
		String wordsFile = dir + "\\words";
		FileReader fileReader = new FileReader(wordsFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		//�ҵ�type��id
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			int index = line.indexOf(":");
			String subStr1 = line.substring(0, index);
			String subStr2 = line.substring(index + 1);
			if (subStr2.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				num = Integer.parseInt(subStr1);
				break;
			}
		}
		// �����ļ�
		String colFile = dir + "\\" + num + "-cols";
		// �����ļ�
		String rowFile = dir + "\\" + num + "-rows";
		FileReader fr1 = new FileReader(colFile);
		FileReader fr2 = new FileReader(rowFile);
		BufferedReader br1 = new BufferedReader(fr1);
		String bString1 = br1.readLine();
		br1.close();
		// �洢��
		String[] bArr1 = bString1.split(" ");
		bufferedReader.close();
		
		for (int i = 0; i < bArr1.length; i++) {
			int classId  = Integer.parseInt(bArr1[i]);
			if(!result.containsKey(classId)){
				result.put(classId, uriMap.get(classId));
			}
	  }
		FileWriter fileWriter = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-AllClass");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for(Entry<Integer, String> entry : result.entrySet()){
	
			bufferedWriter.write(entry.getKey() + ": " + entry.getValue());
		    bufferedWriter.newLine();
		}
		bufferedWriter.close();
		System.out.println("end");
		return result;
	}
	
	public static void main(String[] args) throws IOException {
      String dir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";
      getAllClass(dir);
	}

	
	

}
