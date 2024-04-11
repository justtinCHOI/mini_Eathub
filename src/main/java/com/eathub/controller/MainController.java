package com.eathub.controller;

import com.eathub.conf.SessionConf;
import com.eathub.dto.SearchResultDTO;
import com.eathub.dto.TimeOptionDTO;
import com.eathub.service.RestaurantService;
import com.eathub.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final SearchService searchService;
    private final RestaurantService restaurantService;
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        List<SearchResultDTO> searchResultList = restaurantService.selectSearchResultList(member_seq);
        model.addAttribute("restaurantList", searchResultList);
        return "index";
    }

    @GetMapping("/search/category/{category_seq}")
    public String category(@PathVariable Long category_seq, Model model, HttpSession session){
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        List<TimeOptionDTO> timeOptionDTOS = searchService.generateTimeOptions();
        List<SearchResultDTO> searchResultList = restaurantService.selectSearchCategotyResultList(member_seq, category_seq);
        model.addAttribute("timeOptions", timeOptionDTOS);
        model.addAttribute("restaurantList", searchResultList);
        return "/members/searchResult";
    }

    @GetMapping("/search/top")
    public String top(Model model, HttpSession session){
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        List<SearchResultDTO> searchResultList = restaurantService.selectSearchTopResultList(member_seq);
        model.addAttribute("restaurantList", searchResultList);
        return "/members/searchResult";
    }

    @GetMapping("/search/monthly")
    public String monthly(Model model, HttpSession session){
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        List<SearchResultDTO> searchResultList = restaurantService.selectSearchMonthlyResultList(member_seq);
        model.addAttribute("restaurantList", searchResultList);
        return "/members/searchResult";
    }

    @GetMapping("/search/address/{address}")
    public String location(@PathVariable String address, Model model, HttpSession session){
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        List<TimeOptionDTO> timeOptionDTOS = searchService.generateTimeOptions();

        String KoAddress = restaurantService.getAddressList(address);

        List<String> KoAddr = Arrays.asList(KoAddress.split(","));

        KoAddr = KoAddr.stream().map(loc -> "%" + loc + "%").collect(Collectors.toList());

        List<SearchResultDTO> searchResultList = restaurantService.selectSearchAddressResultList(member_seq, KoAddr);
        model.addAttribute("timeOptions", timeOptionDTOS);
        model.addAttribute("restaurantList", searchResultList);
        return "/members/searchResult";
    }

    @GetMapping("/search/today")
    public String today(Model model, HttpSession session){
        Long member_seq = (Long) session.getAttribute(SessionConf.LOGIN_MEMBER_SEQ);
        List<SearchResultDTO> searchResultList = restaurantService.selectSearchTodayResultList(member_seq);
        model.addAttribute("restaurantList", searchResultList);
        return "/members/searchResult";
    }
}
