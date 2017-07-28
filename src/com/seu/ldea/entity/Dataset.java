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
	//数据集RESCAL输入文件所在位置
    private String datasetDirectory;
    //数据集RESCAL张量文件
    private String datasetEmbedingPath;
    //rdf文件位置
    private String rdfPath;
    //读取三元组文件的方式，true为从本地用model解析获取， false为从图数据库读取文件
    private boolean flag;
    //链接图数据库的url
    private String url = "jdbc://virtuoso://localhost:1111";
    //图名
    private String graphName;
    //用户名
    private String userName = "dba" ;
    //密码
    private String password = "dba";
    
	// 存储resource-id的map
	private  HashMap<String, Integer> rMap;
	// 存储predicate-id的map
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
	
    //从本地读取
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
    
    //后期不应该利用datasetembeding文件创建Dataset，而是由哪里获取datasetEmbeding文件位置，然后设置这个dataset对象的datasetEmbeding
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
	//获取本数据集的所有类
	HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
	HashMap<Integer, String> datasetAllClass = new HashMap<>();
	// type的uri编号
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
	// 宾语文件
	String colFile = dir + "\\" + num + "-cols";
	// 主语文件
	String rowFile = dir + "\\" + num + "-rows";
	FileReader fr1 = new FileReader(colFile);
	FileReader fr2 = new FileReader(rowFile);
	BufferedReader br1 = new BufferedReader(fr1);
	BufferedReader br2 = new BufferedReader(fr2);
	String bString1 = br1.readLine();
	String bString2 = br2.readLine();
	br1.close();
	br2.close();
	// 存储类
	String[] bArr1 = bString1.split(" ");
	// 存储Entity
	String[] bArr2 = bString2.split(" ");
	bufferedReader.close();
	
	for (int i = 0; i < bArr1.length; i++) {
		int classId = Integer.parseInt(bArr1[i]);
		//收集本数据集的类
		datasetAllClass.put(classId, resourceURI.get(classId));
     }
	//写类
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
