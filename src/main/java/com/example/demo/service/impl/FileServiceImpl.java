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
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.DVDMapper;
import com.example.demo.dto.FileQueryParams;
import com.example.demo.model.DVD;
import com.example.demo.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service
public class FileServiceImpl implements FileService{
	
	@Autowired
    private  DVDMapper dvdMapper;
	
	@Autowired
    private DataSource dataSource;
	
	@Autowired
    private StringRedisTemplate stringRedisTemplate;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	// 定義 Redis Queue 的 Key 名稱
    private static final String UPLOAD_QUEUE_KEY = "dvd:upload_queue";
	
	private static final String RANKING_KEY = "dvd:popular_ranking";

    private static final int BATCH_SIZE = 1000;  // 設定批次寫入的大小
    
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
	
	
	// 解決 Sync 問題的核心：
    // 1. @Transactional：確保資料庫出錯時會 Rollback (倒帶)
    // 2. @CacheEvict：上傳成功後，清空 Redis 裡的舊列表快取 
    // ★ 注意：value 必須替換成你當初 @Cacheable 設定的名稱！
	
    //@Transactional(rollbackFor = Exception.class)
    //@CacheEvict(value = "dvds", allEntries = true)
    
    
    @Override
	public void uploadFile(MultipartFile file) throws Exception {
		List<DVD> dvdList = new ArrayList<>();
		int lineNumber = 0; // 用來記錄目前讀到第幾行
		
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
                    // dvdList.add(dvd);
                    
                    // 將 DVD 物件轉成 JSON 字串
                    String dvdJson = objectMapper.writeValueAsString(dvd);
                    
                    // 塞進 Redis 的 List (從右邊推入)
                    stringRedisTemplate.opsForList().rightPush(UPLOAD_QUEUE_KEY, dvdJson);
                } else {
                	throw new IllegalArgumentException("第 " + lineNumber + " 行資料格式有誤，應包含 3 個欄位，實際內容: " + line);
                }
                
                /*// 達到 BATCH_SIZE 就先寫入資料庫並清空 List，保護記憶體
                if (dvdList.size() >= BATCH_SIZE) {
                    dvdMapper.batchInsertDvds(dvdList);
                    dvdList.clear();
                }*/
            }
        } 
		log.info("檔案解析完成，資料已成功放入 Redis Queue");
		/*
        if (!dvdList.isEmpty()) {
        	dvdMapper.batchInsertDvds(dvdList);
        }*/
		
	}
	
	
	@Cacheable(value = "dvds", key = "#fileQueryParams.limit + '-' + #fileQueryParams.offset")
	@Override
	public List<DVD> getAllDvds(FileQueryParams fileQueryParams) {
		
        System.out.println("-----向MySQL資料庫查詢DVD----");
        
		int limit = fileQueryParams.getLimit();
        int offset = fileQueryParams.getOffset();
        
        /*
        int pageNumber = offset / limit;
        // 建立分頁請求 (第幾頁, 每頁幾筆)
        Pageable pageable = PageRequest.of(pageNumber, limit);
        // 向資料庫查詢
        Page<DVD> jpaPage = dvdRepository.findAll(pageable);
        */
        
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

	// 使用者點擊時，更新 Redis
	@Override
	public void incrementViewCount(Integer id) {
		// 使用 Redis ZSET，key 是 dvd:popular_ranking，member 是 DVD ID，每次增加 1
        stringRedisTemplate.opsForZSet().incrementScore(RANKING_KEY, id.toString(), 1);
	}

	// 定時同步回 MySQL
	@Override
	public void syncRankingToDatabase() {
		System.out.println("-------- 開始將 Redis 點擊數同步至 MySQL -------");
		// 取得所有 Redis 中的排行榜數據 (ID 與 分數)
        Set<TypedTuple<String>> rankingSet = stringRedisTemplate.opsForZSet().rangeWithScores(RANKING_KEY, 0, -1);
        
        if (rankingSet == null || rankingSet.isEmpty()) {
            System.out.println("Redis 目前沒有數據需要同步。");
            return;
        }
		
        for (TypedTuple<String> tuple : rankingSet) {
            Integer dvdId = Integer.parseInt(tuple.getValue());
            Integer viewCount = tuple.getScore().intValue();

            // 更新到 MySQL
            try {
                dvdMapper.updateViewCount(dvdId, viewCount);
                // System.out.println("同步成功: DVD ID " + dvdId + " 目前觀看數: " + viewCount);
            } catch (Exception e) {
                System.err.println("同步 DVD ID " + dvdId + " 失敗: " + e.getMessage());
            }
        }
	}


	@Override
	public List<DVD> getTopPopularDvds(int n) {
		// 從 Redis ZSET 取得分數最高的前 n 名 ID (由高到低)
	    Set<String> topIds = stringRedisTemplate.opsForZSet().reverseRange(RANKING_KEY, 0, n - 1);
	    System.out.println("topIds : "+topIds);
	    
	    if (topIds == null || topIds.isEmpty()) {
	        return new ArrayList<>();
	    }
	    
	    // 拿著 30 個 ID，"一次" 去資料庫把電影都查出來
	    List<DVD> dvdsFromDb = dvdMapper.findByIds(topIds);
	    System.out.println("dvdsFromDb :"+ dvdsFromDb);
	    // 因為 SQL 的 IN 語法回傳的順序通常是亂的，我們必須依據 Redis 的原始分數重新排序，並補上分數
	    List<DVD> popularList = new ArrayList<>();
	    for (String idStr : topIds) {
	        Integer id = Integer.parseInt(idStr);
	        // 在剛剛查出來的 list 中找出這部電影
	        DVD dvd = dvdsFromDb.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
	        
	        if (dvd != null) {
	            Double score = stringRedisTemplate.opsForZSet().score(RANKING_KEY, idStr);
	            dvd.setViewCount(score != null ? score.intValue() : 0);
	            popularList.add(dvd);
	        }
	    }
	    return popularList;
	}
	
	
	
}
