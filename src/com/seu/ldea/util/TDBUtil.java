package com.seu.ldea.util;


import java.io.IOException;
import java.sql.SQLException;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;



/**
 * ����TDB���ݿ⣬���������ݴ������ݿ��У���ȡModel����ȡModel�е���Ԫ����н���
 * 
 * @author Lynn
 *
 */
public class TDBUtil {
	public static Dataset ds = TDBFactory.createDataset("E:\\DBpediaTDB");// ������һ��test��TDB������洢test��TDB�����ʾʹ�����TDB
	public static Model model = null;
	/**
	 * ��ʼ��TDB���ݿ⣬�����ݿ�װ�����������ļ�
	 */
	public static void initTDB() {
		model = ds.getDefaultModel();// ����ʹ��TDB��Ĭ��Model
		model.read("E:\\DataSet\\Dbpedia2016\\mappingbased_objects_en.ttl", "TTL");// ��ȡRDF�ļ���ָ����model����
		// model.commit();//�������������ݿ��commint����˼�ǰ����ڵĲ��������ύ
		//model.close();// ����ʹ�õ�ʱ��һ��Ҫ��Model��Dataset���йر�

	}
    
	public static void initTDB(String filePath, String fileType ){
		model = ds.getDefaultModel();
		model.read(filePath, fileType);// ��ȡRDF�ļ���ָ����model����
		// model.commit();//�������������ݿ��commint����˼�ǰ����ڵĲ��������ύ
		model.close();// ����ʹ�õ�ʱ��һ��Ҫ��Model��Dataset���йر�
	}
	
	public static void searchTDB() throws IOException, SQLException {
		// ��ʼ�����ӳ�
		//DBUtil2.initDB();
		// DBUtil2.clearTable();
		 ds.begin(ReadWrite.READ) ;
		 String qs1 = "SELECT * where { ?s ?p <http://dbpedia.org/resource/Prize_Amp��re> }" ;

		 try(QueryExecution qExec = QueryExecutionFactory.create(qs1, ds)) {
		     ResultSet rs = qExec.execSelect() ;
		     ResultSetFormatter.out(rs) ;
		 }
		//writeToDB();

	}
	
	public static Model getTDBModel(){
		/*if(model == null){
			initTDB();
			System.out.println("inital tdb and get Model");
			return model;
		}else{
			System.out.println("directly get Model");
			return model;
		}*/
		return ds.getDefaultModel();
	}
	
	
}
