package com.example.demo.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.dto.FileQueryParams;
import com.example.demo.dto.ReportResponse;
import com.example.demo.model.DVD;
import com.example.demo.service.FileService;
import com.example.demo.util.Page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


@CrossOrigin(origins = "*")
@RestController
@Validated
@RequestMapping("/api")
public class FileController {
	
	private static final Logger log = LoggerFactory.getLogger(FileController.class);
	
	@Autowired
	private FileService fileService;
	
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file){
		log.info("接收到檔案上傳請求，檔名: {}", file.getOriginalFilename());
		if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("上傳失敗：請選擇檔案");
        }
		
		String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/plain")) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("上傳失敗：檔案內容類型錯誤");
        }
		
		try {
			fileService.uploadFile(file);
            return ResponseEntity.ok("檔案格式解析成功！資料已加入背景排隊序列");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("處理檔案時發生錯誤：" + e.getMessage());
        }
	}
	
	
	@GetMapping("/dvds")
	public ResponseEntity<?> getAllDvds(
		//分頁
		@RequestParam(defaultValue = "20") @Max(1000) @Min(0)Integer limit,
		@RequestParam(defaultValue = "0") @Min(0) Integer offset	
	){
		System.out.println("進到 getAllDvds");
		
		FileQueryParams fileQueryParams =new FileQueryParams();
		fileQueryParams.setLimit(limit);
		fileQueryParams.setOffset(offset);
		
		try {
			// 取得product總數
			Integer total = fileService.countDvds(fileQueryParams);
		
			// 查全部
			List<DVD> dvdList = fileService.getAllDvds(fileQueryParams);
			
			if (dvdList.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body("目前沒有任何 DVD 資料");
			}
			
			Page<DVD> page = new Page<>();
			page.setLimit(limit);
			page.setOffset(offset);
			page.setTotal(total);
			page.setDvdList(dvdList);
				
			return ResponseEntity.status(HttpStatus.OK).body(page);
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查詢資料時發生錯誤：" + e.getMessage());
		}
	}
	
	@GetMapping("/generate")
	public ResponseEntity<ReportResponse> generateReport() {
		try {
            // 取得報表的 byte 陣列
            byte[] pdfBytes = fileService.generateReportPdf();

            // 將 byte[] 轉成 Base64 字串
            String base64String = Base64.getEncoder().encodeToString(pdfBytes);
            
            ReportResponse responseModel = new ReportResponse();
            responseModel.setFileName("report.pdf");
            responseModel.setFileContentBase64(base64String);
            responseModel.setMessage("報表產生成功");
                    
            /* 
             Map<String, String> result = new HashMap<>();
            result.put("fileName","report.pdf");
            result.put("data", base64String);
            result.put("Message", "報表產生成功");
            */

            return ResponseEntity.ok(responseModel);

        } catch (Exception e) {
            e.printStackTrace();          
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setMessage("報表產生失敗：" + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
	}
	
	@PostMapping("/dvds/click")
	public ResponseEntity<String> clickDvd(@RequestParam Integer id) {
	    try {
	        fileService.incrementViewCount(id);
	        return ResponseEntity.ok("點擊成功");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失敗");
	    }
	}
	
	@GetMapping("/dvds/top")
	public ResponseEntity<List<DVD>> getTopRanking(@RequestParam(defaultValue = "30") int limit) {
	    List<DVD> topDvds = fileService.getTopPopularDvds(limit);
	    return ResponseEntity.ok(topDvds);
	}
}
