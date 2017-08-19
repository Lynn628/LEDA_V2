package com.seu.ldea.commonutil;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBPool {
	   private static String driver = "com.mysql.jdbc.Driver";
	   private static String url = "jdbc:mysql://localhost:3306/rescaldb";
	   private static String user = "root";
	   private static String password = "root";
	 
	   //初始化数据源
	   private static BasicDataSource ds;
	      public static BasicDataSource initDataSource(){
	    	  try{
	    	  BasicDataSource dataSource = new BasicDataSource();
	    		//设置连接池所需的驱动
	    		dataSource.setDriverClassName(driver);
	    		//设置链接数据库的URL
	    		dataSource.setUrl(url);
	    		//设置连接数据库的用户名
	    		dataSource.setUsername(user);
	    		dataSource.setPassword(password);
	    		//设置连接池的初始连接数
	    	    dataSource.setInitialSize(20);
	    	    //设置连接池最多connection
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
	      //回收数据库连接
	      public static void shutdownDataSource() throws SQLException{
	    	  BasicDataSource basicDataSource =(BasicDataSource)ds;
	    	  basicDataSource.close();
	      }
	     
}
