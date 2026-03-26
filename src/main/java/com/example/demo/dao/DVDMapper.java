package com.example.demo.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.model.DVD;

@Mapper
public interface DVDMapper {
	
	// 批次新增
    void batchInsertDvds(@Param("dvdList") List<DVD> dvdList);
    
    // 分頁查詢
    List<DVD> getDvds(@Param("limit") Integer limit, @Param("offset") Integer offset);
    
    // 查詢總筆數
    Integer countDvds();
    
    // 根據 ID，更新該部 DVD 的觀看次數
    @Update("UPDATE dvds SET view_count = #{viewCount} WHERE id = #{id}")
    void updateViewCount(@Param("id") Integer id, @Param("viewCount") Integer viewCount);
    
    // @Select("SELECT * FROM dvds WHERE id = #{id}")
    // DVD findById(Integer id);
    
    @Select({
        "<script>",
        "SELECT * FROM dvds WHERE id IN ",
        "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<DVD> findByIds(@Param("ids") Set<String> ids);
    
}
