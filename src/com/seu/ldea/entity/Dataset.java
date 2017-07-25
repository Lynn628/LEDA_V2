package com.seu.ldea.entity;

import java.util.HashMap;

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
}
