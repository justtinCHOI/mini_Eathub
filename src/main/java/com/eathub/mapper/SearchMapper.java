package com.eathub.mapper;

import com.eathub.dto.SearchResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchMapper {


    //page 별로 레스토랑번호 순으로 나열하기
    List<SearchResultDTO> selectRestaurantSearchListPage(@Param("page") int page, @Param("sortNum") int sortNum, @Param("categorySeq") int categorySeq);
    int SearchResultNum(@Param("categorySeq") int categorySeq);
}
