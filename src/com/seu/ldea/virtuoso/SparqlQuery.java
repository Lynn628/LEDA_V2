package com.seu.ldea.virtuoso;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;

import com.seu.ldea.entity.Dataset;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class SparqlQuery {
	/**
	 * 
	 * @param dataset
	 * @return 返回数据库中查询返回的结果
	 */
    public static ResultSet getAllTriplesResultSet(Dataset dataset){
    	String graphName = dataset.getGraphName();
    	String url = dataset.getUrl();
    	String userName = dataset.getUserName();
    	String password = dataset.getPassword();
        VirtGraph graph = new VirtGraph(graphName, url, userName, password);
		 
		Query sparql = QueryFactory.create("select ?s ?p ?o where {?s ?p ?o}");
		VirtuosoQueryExecution virtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql,graph);
		// System.out.println("test2");
		 ResultSet resultSet = virtuosoQueryExecution.execSelect();
		 return resultSet;
    }
    
    /**
     * 
     * 得到此数据集中所有的类
     * @param dataset
     * @return
     */
    public static ResultSet getAllClass(Dataset dataset){
    	String graphName = dataset.getGraphName();
    	String url = dataset.getUrl();
    	String userName = dataset.getUserName();
    	String password = dataset.getPassword();
        VirtGraph graph = new VirtGraph(graphName, url, userName, password);
		 
		Query sparql = QueryFactory.create("select ?o where {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o}");
		VirtuosoQueryExecution virtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql,graph);
		// System.out.println("test2");
		 ResultSet resultSet = virtuosoQueryExecution.execSelect();
		 return resultSet;
    }
    
}
