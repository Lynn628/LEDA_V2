package com.seu.Idea.cluster;

public class Dataset {
	//数据集RESCAL输入文件所在位置
    public String datasetDirectory;
    //数据集RESCAL张量文件
    public String datasetEmbedingPath;
    //rdf文件位置
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
