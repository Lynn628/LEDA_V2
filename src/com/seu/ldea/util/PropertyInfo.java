package com.seu.ldea.util;

public class PropertyInfo {
	 private String propertyName;
	 private double timeInfoPercentage;
	
	 public PropertyInfo(String propertyName, double timeInfoPercentage) {
		 this.propertyName = propertyName;
		 this.timeInfoPercentage = timeInfoPercentage;
		
	}
	 public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	 public String getPropertyName() {
		return propertyName;
	}
	 public void setTimeInfoPercentage(double timeInfoPercentage) {
		this.timeInfoPercentage = timeInfoPercentage;
	}
	 public double getTimeInfoPercentage() {
		return timeInfoPercentage;
	}
	 public String toString(){
		 return propertyName + " " + String.valueOf(timeInfoPercentage);
	 }
}
