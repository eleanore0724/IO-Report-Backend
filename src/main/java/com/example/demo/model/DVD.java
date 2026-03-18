package com.example.demo.model;


public class DVD {

	private Integer id;
	
    private String title;
	
    private Integer year;
	
    private String genre;
	
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
	@Override
	public String toString() {
		return "DVD [id=" + id + ", title=" + title + ", year=" + year + ", genre=" + genre + "]";
	}
    
    
}
