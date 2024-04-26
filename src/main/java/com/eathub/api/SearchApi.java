package com.eathub.api;

import com.eathub.conf.SessionConf;
import com.eathub.dto.RestaurantDetailDTO;
import com.eathub.dto.ReviewDTO;
import com.eathub.dto.SearchResultDTO;
import com.eathub.entity.RestaurantInfo;
import com.eathub.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchApi {

    private final SearchService searchService;
    private final SearchService restaurantService;


    //현재시간 부터 자정 전까지 시간들
    @PostMapping("/getAvailableTimes/")
    public List<String> getOutdatedTimes() {
        return searchService.getAvailableTimes();
    }

    @GetMapping("")
    public ResponseEntity<?> getReviews(@RequestParam(value = "page",defaultValue = "1") int page,
                                        @RequestParam(value = "sortNum",defaultValue = "0") int sortNum,
                                        @RequestParam(value = "categorySeq",defaultValue = "0") int categorySeq,
                                        HttpSession session) {
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        List<SearchResultDTO> searchResultList = searchService.selectSearchResultList(member_seq, page, sortNum, categorySeq);

        if (searchResultList == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(searchResultList);
    }


    @GetMapping("/getRowNum/")
    public ResponseEntity<?> getReviews(@RequestParam(value = "categorySeq",defaultValue = "0") int categorySeq) {
        int searchResultNum = searchService.SearchResultListNum(categorySeq);
        return ResponseEntity.ok().body(searchResultNum);
    }
}
