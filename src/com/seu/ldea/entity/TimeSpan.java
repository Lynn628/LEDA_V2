package com.seu.ldea.entity;

import java.util.HashSet;

public class TimeSpan {

	private  String begin;
	private  String end;
	public TimeSpan(String begin, String end) {
		this.begin = begin;
		this.end = end;
		// TODO Auto-generated constructor stub
	}
	
	 public String getBegin() {
		return begin;
	}
	 
	 public String getEnd() {
		return end;
	}
	 
	 public void setBegin(String begin) {
		this.begin = begin;
	}
	 public void setEnd(String end) {
		this.end = end;
	}
	 
     @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return "<" + begin + "," + end + ">";
    }
    //重写equals和hashcode方法，当时间区间相同时，不重复加入 
    public boolean equals(Object obj){
    	if(obj instanceof TimeSpan){
    		TimeSpan object = (TimeSpan) obj;
    	  return (this.begin.equals(object.begin))&&(this.end.equals(object.end));
    	}
    	 return super.equals(obj);
    }
    
    public int hashCode(){
    	return begin.hashCode();
    }
  /*  public int hashCode() {
       TimeSpan span = (TimeSpan) this;
      //  System.out.println("Hash" + name.id);
        return span.hashCode();        
    }*/
    
    public static void main(String[] args){
    	HashSet<TimeSpan> set = new HashSet<>();
     boolean t1 = set.add(new TimeSpan("2011", "2011"));
     boolean t2 = set.add(new TimeSpan("2011", "2011"));
     boolean t3 = set.add(new TimeSpan("2013", "2013"));
     System.out.println("t1 is " + t1 + " t2 is " + t2 + " t3 is " + t3);
    }
}
