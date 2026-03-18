package com.example.demo.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.DVDMapper;
import com.example.demo.dto.FileQueryParams;
import com.example.demo.model.DVD;
import com.example.demo.service.FileService;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Component
public class FileServiceImpl implements FileService{
	
	@Autowired
    private  DVDMapper dvdMapper;
	
	@Autowired
    private DataSource dataSource;
	
	public void uploadFile(MultipartFile file) throws Exception {
		List<DVD> dvdList = new ArrayList<>();
		
		// 取得檔案的位元組輸入流，一點一滴的讀取這個檔案的內容，最後用BufferedReader包起來提升讀取效能
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",");
                
                if (data.length == 3) {
                    DVD dvd = new DVD();
                    dvd.setTitle(data[0].trim());
                    dvd.setYear(Integer.parseInt(data[1].trim()));
                    dvd.setGenre(data[2].trim());
                    dvdList.add(dvd);
                }
            }
        }
        
        if (!dvdList.isEmpty()) {
        	dvdMapper.batchInsertDvds(dvdList);
        }
		
	}
	
	
	public List<DVD> getAllDvds(FileQueryParams fileQueryParams) {
		int limit = fileQueryParams.getLimit();
        int offset = fileQueryParams.getOffset();
        
        /*
        int pageNumber = offset / limit;
        // 建立分頁請求 (第幾頁, 每頁幾筆)
        Pageable pageable = PageRequest.of(pageNumber, limit);
        // 向資料庫查詢
        Page<DVD> jpaPage = dvdRepository.findAll(pageable);*/
        
        return dvdMapper.getDvds(limit, offset);
    }


	@Override
	public Integer countDvds(FileQueryParams fileQueryParams) {
        long totalCount = dvdMapper.countDvds();
        return (int) totalCount;
	}


	@Override
	public byte[] generateReportPdf() throws Exception {
		
        InputStream reportStream = new ClassPathResource("reports/getData.jrxml").getInputStream(); // 將該檔案轉換成資料流（InputStream），以便後續程式讀取。
        
        if (reportStream == null) {
            throw new RuntimeException("找不到報表檔案：reports/getData.jrxml");
        } else {
            System.out.println("成功讀取 reports/getData.jrxml");
        }

        // .jrxml 轉 .jasper
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        
        Map<String, Object> parameters = new HashMap<>();
        try (Connection conn = dataSource.getConnection()){
        // 填充報表資料(編譯後的報表模板, 報表參數, 資料庫連線)
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

        // 轉成 PDF 二進位陣列
        return JasperExportManager.exportReportToPdf(jasperPrint);
		}
	}
}
