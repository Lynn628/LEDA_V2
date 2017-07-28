package com.seu.ldea.entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.uima.cas.StringArrayFS;

/**
 * @author Lynn
 *
 */
public class Dataset {
	//���ݼ�RESCAL�����ļ�����λ��
    private String datasetDirectory;
    //���ݼ�RESCAL�����ļ�
    private String datasetEmbedingPath;
    //rdf�ļ�λ��
    private String rdfPath;
    //��ȡ��Ԫ���ļ��ķ�ʽ��trueΪ�ӱ�����model������ȡ�� falseΪ��ͼ���ݿ��ȡ�ļ�
    private boolean flag;
    //����ͼ���ݿ��url
    private String url = "jdbc://virtuoso://localhost:1111";
    //ͼ��
    private String graphName;
    //�û���
    private String userName = "dba" ;
    //����
    private String password = "dba";
    
	// �洢resource-id��map
	private  HashMap<String, Integer> rMap;
	// �洢predicate-id��map
	private  HashMap<String, Integer> pMap;
	
	public void setpMap(HashMap<String, Integer> pMap) {
		this.pMap = pMap;
	}
	
	public void setrMap(HashMap<String, Integer> rMap) {
		this.rMap = rMap;
	}
	
	public HashMap<String, Integer> getpMap() {
		return pMap;
	}
	
	public  HashMap<String, Integer> getrMap() {
		return rMap;
	}
	
    //�ӱ��ض�ȡ
    public Dataset(String rdfPath){
    	this.rdfPath = rdfPath;
    }
    
    
    
    public Dataset(String url, String graphName, String userName, String password){
    	this.url = url;
    	this.userName = userName;
    	this.password = password;
    	this.graphName = graphName;
    }
    
    public String getGraphName() {
		return graphName;
	}
    
    public String getPassword() {
		return password;
	}
    
    public String getUrl() {
		return url;
	}
    
    public String getUserName() {
		return userName;
	}
    
    //���ڲ�Ӧ������datasetembeding�ļ�����Dataset�������������ȡdatasetEmbeding�ļ�λ�ã�Ȼ���������dataset�����datasetEmbeding
    public Dataset(String datasetDirectory, String datasetEmbedingPath, String rdfPath) {
		super();
		this.datasetDirectory = datasetDirectory;
		this.datasetEmbedingPath = datasetEmbedingPath;
		this.rdfPath = rdfPath;
	}

    public Dataset(String datasetDirectory, String datasetEmbedingPath){
    	this.datasetDirectory = datasetDirectory;
    	this.datasetEmbedingPath = datasetDirectory;
    }
    
    public String getDatasetDirectory() {
		return datasetDirectory;
	}
    
    public String getDatasetEmbedingPath() {
		return datasetEmbedingPath;
	}
    
    public void setDatasetDirectory(String datasetDirectory) {
		this.datasetDirectory = datasetDirectory;
	}
    
    public void setDatasetEmbedingPath(String datasetEmbedingPath) {
		this.datasetEmbedingPath = datasetEmbedingPath;
	}
    
    
	public static HashMap<Integer, String> getDataSetClass(String dir, String dstFileName) throws IOException{
	String wordsFile = dir + "\\words";
	FileReader fileReader = new FileReader(wordsFile);
	BufferedReader bufferedReader = new BufferedReader(fileReader);
	String line = "";
	//��ȡ�����ݼ���������
	HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
	HashMap<Integer, String> datasetAllClass = new HashMap<>();
	// type��uri���
	int num = 0;
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
		int classId = Integer.parseInt(bArr1[i]);
		//�ռ������ݼ�����
		datasetAllClass.put(classId, resourceURI.get(classId));
     }
	//д��
	String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\" + dstFileName +".txt";
	FileWriter fileWriter = new FileWriter(dstFile);
	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	for (Entry<Integer, String> entry : datasetAllClass.entrySet()) {
		bufferedWriter.write(entry.getKey() + " ");
		String uri = entry.getValue();
		bufferedWriter.write(uri);
		bufferedWriter.newLine();
		bufferedWriter.flush();
	}
	bufferedWriter.close();
	return datasetAllClass;
	}
}
