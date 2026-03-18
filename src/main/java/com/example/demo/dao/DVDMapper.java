package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.model.DVD;

@Mapper
public interface DVDMapper {
	
	// 1. 批次新增 DVD
    void batchInsertDvds(@Param("dvdList") List<DVD> dvdList);
    
    // 2. 分頁查詢
    List<DVD> getDvds(@Param("limit") Integer limit, @Param("offset") Integer offset);
    
    // 3. 查詢總筆數
    Integer countDvds();
}
