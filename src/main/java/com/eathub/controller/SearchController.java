package com.eathub.controller;

import com.eathub.conf.SessionConf;
import com.eathub.dto.CategoryDTO;
import com.eathub.dto.RestaurantDetailDTO;
import com.eathub.dto.SearchResultDTO;
import com.eathub.dto.TimeOptionDTO;
import com.eathub.service.RestaurantService;
import com.eathub.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;
    private final RestaurantService restaurantService;
    @ModelAttribute("page")
    public String page() {
        return "search";
    }
    @GetMapping("")
    public String search(Model model, HttpSession session, @RequestParam(value = "categorySeq",defaultValue = "0") int categorySeq){
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        String wantingDate = (String) session.getAttribute("wantingDate");
        String wantingHour = (String) session.getAttribute("wantingHour");
        // 시간태그 생성해서 가져오기
        List<TimeOptionDTO> timeOptionDTOS = searchService.generateTimeOptions();
        // 레스토랑 목록 가져오기
        List<SearchResultDTO> searchResultList = restaurantService.selectSearchResultList(member_seq);
        List<RestaurantDetailDTO> restaurantDetailDTOList = restaurantService.getRestaurantDetailList();

        try {
            for (SearchResultDTO searchResultDTO : searchResultList) {
                Long restaurant_seq = searchResultDTO.getRestaurant_seq();
                for (RestaurantDetailDTO restaurantDetailDTO : restaurantDetailDTOList) {
                    if (restaurantDetailDTO.getRestaurant_seq().equals(restaurant_seq)) {
                        searchResultDTO.setImage_url(restaurantDetailDTO.getImage_url());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategory_seq((long) categorySeq);
        model.addAttribute("timeOptions", timeOptionDTOS);
        model.addAttribute("restaurantList", searchResultList);
        model.addAttribute("categoryDTO", categoryDTO);
        // 세션에 값이 없거나 형식에 맞지 않으면 세션 생성
        if(!searchService.isValidDateFormat(wantingDate)
            ||  wantingHour == null){
            String[] Arr = searchService.getDateTime();
            session.setAttribute("wantingDate", Arr[0]);
            session.setAttribute("wantingHour", Arr[1]);
        }
        if(session.getAttribute("wantingPerson") == null){
            session.setAttribute("wantingPerson", 1);
        }
        return "/members/searchResult";
    }

    @GetMapping("/result")
    public String searchResult(Model model){
        List<TimeOptionDTO> timeOptionDTOS = searchService.generateTimeOptions();
        model.addAttribute("timeOptions", timeOptionDTOS);
        return "/members/searchResult";
    }
}
