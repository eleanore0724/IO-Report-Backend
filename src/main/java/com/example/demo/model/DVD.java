package com.example.demo.model;

import java.io.Serializable;

public class DVD implements Serializable{ // Java 必須把你的 DVD 物件轉換成特定的格式（稱為序列化）才能存進 Redis
	private Integer id;
	
    private String title;
	
    private Integer year;
	
    private String genre;
    
    private Integer viewCount;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public Integer getViewCount() {
		return viewCount;
	}
	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}
	@Override
	public String toString() {
		return "DVD [id=" + id + ", title=" + title + ", year=" + year + ", genre=" + genre + ", viewCount=" + viewCount
				+ "]";
	}
	
    
    
}
