package com.seu.ldea.entity;

import java.util.HashMap;

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
}
