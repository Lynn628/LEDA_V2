package com.seu.ldea.commonutil;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBPool {
	   private static String driver = "com.mysql.jdbc.Driver";
	   private static String url = "jdbc:mysql://localhost:3306/rescaldb";
	   private static String user = "root";
	   private static String password = "root";
	 
	   //��ʼ������Դ
	   private static BasicDataSource ds;
	      public static BasicDataSource initDataSource(){
	    	  try{
	    	  BasicDataSource dataSource = new BasicDataSource();
	    		//�������ӳ����������
	    		dataSource.setDriverClassName(driver);
	    		//�����������ݿ��URL
	    		dataSource.setUrl(url);
	    		//�����������ݿ���û���
	    		dataSource.setUsername(user);
	    		dataSource.setPassword(password);
	    		//�������ӳصĳ�ʼ������
	    	    dataSource.setInitialSize(20);
	    	    //�������ӳ����connection
	    	    dataSource.setMaxTotal(30);
	    	    dataSource.setMaxWaitMillis(3000);
	    	    ds = dataSource;
	    	    
	    	  }catch (Exception e) {
				// TODO: handle exception
	    		  System.out.println("Error loading Mysql Driver");
	    		  e.printStackTrace();
			}
	    	  return ds;
	    	 
	      }
	      
	      public  Connection getConnection(){
	    	  Connection connection = null;
	    	  if(ds != null){
	    		  try{
	    			  connection = ds.getConnection();
	    		  }catch (Exception e) {
					// TODO: handle exception
	    			  System.out.println(e.getMessage());
				}
	    		  }
	    	  try{
	    		  connection.setAutoCommit(false);
	    	  }catch (SQLException e) {
             System.out.println(e.getMessage());
             return connection;
			}
	    	  return connection;
	    	  
	      }
	      //�������ݿ�����
	      public static void shutdownDataSource() throws SQLException{
	    	  BasicDataSource basicDataSource =(BasicDataSource)ds;
	    	  basicDataSource.close();
	      }
	     
}
