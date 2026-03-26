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
	
	// 增加：使用者點擊 DVD 增加觀看次數
    void incrementViewCount(Integer id);

    // 增加：同步 Redis 數據到資料庫
    void syncRankingToDatabase();

    // 取得前 N 名的熱門 DVD
    List<DVD> getTopPopularDvds(int n);
}
