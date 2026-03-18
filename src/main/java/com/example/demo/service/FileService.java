package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.FileQueryParams;
import com.example.demo.model.DVD;


public interface FileService {
	public void uploadFile(MultipartFile file) throws Exception;
	
	public List<DVD> getAllDvds(FileQueryParams fileQueryParams);
	
	public Integer countDvds(FileQueryParams fileQueryParams);
	
	public byte[] generateReportPdf() throws Exception;
}
