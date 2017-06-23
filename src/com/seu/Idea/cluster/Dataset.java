package com.seu.Idea.cluster;

public class Dataset {
	//���ݼ�RESCAL�����ļ�����λ��
    public String datasetDirectory;
    //���ݼ�RESCAL�����ļ�
    public String datasetEmbedingPath;
    //rdf�ļ�λ��
    public String rdfPath;
    
    public Dataset(String rdfPath){
    	this.rdfPath = rdfPath;
    }
    
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
