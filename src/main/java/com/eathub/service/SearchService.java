package com.eathub.service;

import com.eathub.dto.RestaurantDetailDTO;
import com.eathub.dto.SearchResultDTO;
import com.eathub.dto.TimeOptionDTO;
import com.eathub.entity.RestaurantZzim;
import com.eathub.mapper.RestaurantMapper;
import com.eathub.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {


    private final RestaurantMapper restaurantMapper;
    private final SearchMapper searchMapper;

    // 타임리프에 사용할 시간 옵션을 생성하는 메서드 6시부터 23시 30분까지 30분 단위로 생성
    public List<TimeOptionDTO> generateTimeOptions() {
        List<TimeOptionDTO> timeOptions = new ArrayList<>();
        LocalTime time = LocalTime.of(0, 0);
        while (!time.equals(LocalTime.of(23, 30))) {
            TimeOptionDTO option = new TimeOptionDTO();
            option.setTime(time.toString());
            timeOptions.add(option);
            time = time.plusMinutes(30);
        }
        TimeOptionDTO option = new TimeOptionDTO();
        option.setTime(time.toString());
        timeOptions.add(option);
        return timeOptions;
    }

    //session 초기값에 저장할 오늘 날짜 구하기.
    public String getTodayDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
    //wantingDate 이 형식에 맞는지 확인
    public boolean isValidDateFormat(String wantingDate) {
        if (wantingDate == null || wantingDate.isEmpty()) {
            // 입력 문자열이 null 또는 빈 문자열인 경우 유효하지 않은 형식으로 간주
            return false;
        }

        try {
            // "yyyy-MM-dd" 형식으로 파싱을 시도하여 LocalDate 객체로 변환
            LocalDate.parse(wantingDate, DateTimeFormatter.ISO_DATE);
            // 예외가 발생하지 않았으므로 유효한 형식임을 반환
            return true;
        } catch (DateTimeParseException e) {
            // 예외 발생 시 유효하지 않은 형식으로 간주
            return false;
        }
    }

    //session 초기값에 저장할 가장 가까운 예약 가능 시간 구하기.
    public String[] getDateTime(){

        String[] Arr= new String[2];
        LocalDate date = LocalDate.now();

        // 현재 시간 가져오기
        LocalTime currentTime = LocalTime.now();

        // 다음 30분 단위의 예약 가능한 시간 계산
        int currentMinute = currentTime.getMinute();
        int nextReservationMinute = ((currentMinute / 30) + 1) * 30; // 다음 예약 가능한 분
        LocalTime nextReservationTime;

        if (nextReservationMinute == 60) {
            nextReservationTime = currentTime.plusHours(1).withMinute(0); // 정시로 설정
            if(nextReservationTime.getHour() == 0){
                date = date.plusDays(1);;
            }
        } else {
            nextReservationTime = currentTime.withMinute(nextReservationMinute);
        }

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Arr[0] =  date.format(formatter1);

        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HHmm");
        Arr[1] = nextReservationTime.format(formatter2);

        return Arr;
    }



    //현재시간부터 자정 전까지 시간들
    public List<String> getAvailableTimes() {
        //현재 시간 구하기
        LocalTime currentTime = LocalTime.now();
        int currentMinute = currentTime.getMinute();
        int nextReservationMinute = ((currentMinute / 30) + 1) * 30; // 다음 예약 가능한 분
        LocalTime nextReservationTime;
        if (nextReservationMinute == 60) {
            nextReservationTime = currentTime.plusHours(1).withMinute(0); // 정시로 설정
        } else {
            nextReservationTime = currentTime.withMinute(nextReservationMinute);
        }

        //저장할 데이터의 형태
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
        LocalTime time = LocalTime.of(6, 0);

        List<String> outdatedTimes = new ArrayList<>();

        if(Integer.parseInt(time.format(formatter))   <   Integer.parseInt(nextReservationTime.format(formatter))){
            while (!time.format(formatter).equals(nextReservationTime.format(formatter))) {
                outdatedTimes.add(time.format(formatter));
                time = time.plusMinutes(30);
            }
        }
        return outdatedTimes;
    }

    //ajax에 들어가는 값들
    public List<SearchResultDTO> selectSearchResultList(Long member_seq, int page, int sortNum, int categorySeq) {
        List<SearchResultDTO> searchResultList = searchMapper.selectRestaurantSearchListPage(page, sortNum, categorySeq);
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);

        //각각의 식당마다 회원이 한 zzimList안에 있는 식당번호라면 zzimed 필드에 true값 넣기
        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(zzimList.stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }

        //식당정보에서 img 를 가지고 와서 SearchResultDTO img_url 에 집어넣기.
        List<RestaurantDetailDTO> restaurantDetailDTOList = restaurantMapper.selectAllRestaurantDetailDTO();
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
        return searchResultList;
    }


    //ajax에 들어가는 값들
    public int SearchResultListNum(int categorySeq) {
        return searchMapper.SearchResultNum(categorySeq);
    }
}
