package com.eathub.service;

import com.eathub.dto.*;
import com.eathub.entity.Reservation;
import com.eathub.entity.RestaurantInfo;
import com.eathub.entity.RestaurantZzim;
import com.eathub.mapper.ObjectStorageMapper;
import com.eathub.mapper.RestaurantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;

import java.util.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantMapper restaurantMapper;
    private final ObjectStorageMapper objectStorageMapper;

    public RestaurantInfo selectRestaurantInfo(Long restaurant_seq) {
        return restaurantMapper.selectRestaurantInfo(restaurant_seq);
    }


    public List<MenuFormDTO> getMenuListByRestaurantSeq(Long restaurant_seq) {
        // 레스토랑 ID로 메뉴 목록 조회 로직 구현
        List<MenuFormDTO> menuList = restaurantMapper.getMenuListByRestaurantSeq(restaurant_seq);
        return menuList;
    }

    //   식당 정보 조회
    public List<RestaurantInfo> selectRestaurantInfoList() {
        return restaurantMapper.selectRestaurantInfoList();
    }

    /**
     * 사용자의 찜 목록에 있는 식당들을 조회하는 메서드입니다.
     *
     * @param member_seq 사용자의 고유 번호입니다. 이 번호를 통해 사용자의 찜 목록을 조회합니다.
     * @return 사용자가 찜한 식당들의 목록을 반환합니다. 각 식당은 SearchResultDTO 객체로 표현됩니다.
     * 이 객체에는 식당의 고유 번호, 이름, 위치 등의 정보와 함께 사용자가 해당 식당을 찜했는지 여부를 나타내는 isZzimed 필드가 포함됩니다.
     */
    public List<SearchResultDTO> selectSearchResultList(Long member_seq) {
        List<SearchResultDTO> searchResultList = restaurantMapper.selectRestaurantSearchList();
        // 일치하는 항목이 있으면 isZzimed 필드를 true로 설정합니다.
//        for (SearchResultDTO restaurant : searchResultList) {
//            restaurant.setZzimed(restaurantMapper.selectZzimList(member_seq).stream()
//                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
//        }
        // 먼저 member_seq에 해당하는 모든 찜 목록을 한 번에 가져옵니다.
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);

        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(zzimList.stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }

        return searchResultList;
    }


//   유저가 찜한 식당 리스트 조회

    /**
     * 찜한 식당 목록을 가져오는 메소드입니다.
     *
     * @param member_seq 회원 번호
     * @return 찜한 식당 정보 목록
     */
    public List<MyPageDTO> getZzimRestaurantList(Long member_seq) {
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);
        List<MyPageDTO> restaurantInfoList = new ArrayList<>();
        for (RestaurantZzim zzim : zzimList) {
            restaurantInfoList.add(restaurantMapper.selectMyPageDTO(zzim.getRestaurant_seq(), member_seq));
        }
        return restaurantInfoList;
    }

    public int getZzimCount(Long restaurant_seq, Long member_seq) {
        return restaurantMapper.checkZzimData(restaurant_seq, member_seq);
    }

//    찜 추가 및 삭제

    /**
     * 회원의 찜한 식당을 토글하는 메소드입니다.
     *
     * @param member_seq     회원 번호
     * @param restaurant_seq 식당 번호
     * @return 찜 상태가 변경되었는지 여부를 나타내는 boolean 값
     */
    @Transactional
    public boolean toggleZzimRestaurant(Long member_seq, Long restaurant_seq) {

        RestaurantZzim zzim = RestaurantZzim.builder()
                .member_seq(member_seq)
                .restaurant_seq(restaurant_seq)
                .build();

        RestaurantZzim zzimResult = restaurantMapper.selectZzimList(member_seq).stream()
                .filter(zzim1 -> zzim1.getRestaurant_seq().equals(restaurant_seq))
                .findFirst()
                .orElse(null);

        if (zzimResult == null) {
            restaurantMapper.insertZzimRestaurant(zzim);
            restaurantMapper.updateZzimTotal(restaurant_seq, 1);
            return true;
        } else {
            restaurantMapper.deleteZzimRestaurant(zzim);
            restaurantMapper.updateZzimTotal(restaurant_seq, -1);
            return false;
        }
    }

    // 마이페이지 찜목록에 comment 추가
    public void updateZzimComment(Long zzim_seq, String comment) {
        restaurantMapper.updateZzimComment(zzim_seq, comment);
    }


    public Map<String, String> getLocationList() {
        Map<String, String> locations = new LinkedHashMap<>();
        locations.put("SEOUL", "서울");
        locations.put("BUSAN", "부산");
        locations.put("JEJU", "제주");

        return locations;
    }

    public List<CategoryDTO> getCategoryList() {
        return restaurantMapper.selectCategoryList();
    }


    public void insertRestaurant(RestaurantInfo restaurantJoinDTO) {
        restaurantMapper.insertRestaurant(restaurantJoinDTO);
    }

    public void insertReservation(Reservation reservationJoinDTO) {
        restaurantMapper.insertReservation(reservationJoinDTO);
    }

    public String getReservationTime(ReservationJoinDTO reservationJoinDTO) throws ParseException {
        // date 형으로 형변환
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HHmm");
        String date = reservationJoinDTO.getDate();
        String time = reservationJoinDTO.getHour();

        // 문자열을 Date 객체로 파싱
        Date parsedDate = format.parse(date + " " + time);

        // 다른 형식으로 변환할 SimpleDateFormat 객체 생성
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Date 객체를 다른 형식으로 변환
        return outputFormat.format(parsedDate);
    }


    public void insertRestaurantMenu(Long restaurant_seq, MenuFormDTOWrapper menuListWrapper) {
        List<MenuFormDTO> menuForm = menuListWrapper.getMenuList();
        restaurantMapper.insertRestaurantMenu(restaurant_seq, menuForm);
    }

    public RestaurantInfo selectSavedRestaurant(RestaurantJoinDTO restaurantJoinDTO) {

        return restaurantMapper.selectRestaurant(restaurantJoinDTO);
    }

    public void insertRestaurantStatus(RestaurantInfo restaurantJoinDTO) {
        restaurantMapper.insertRestaurantStatus(restaurantJoinDTO);
    }

    public List<MyPageDTO> getOwnerRestaurantList(Long member_seq) {
        return restaurantMapper.selectOwnerRestaurantList(member_seq);
    }

    /**
     * 식당의 상태를 업데이트하는 메서드입니다.
     *
     * @param restaurant_seq 업데이트하려는 식당의 고유 번호입니다.
     * @param status         식당에 설정하려는 새로운 상태입니다.
     *                       이 메서드는 두 단계로 작동한다.
     *                       먼저, restaurantMapper의 updateRestaurantStatus 메서드를 호출하여 식당의 상태를 업데이트합니다.
     *                       그 다음, restaurantMapper의 updateRestaurantInfoStatus 메서드를 호출하여 식당 정보의 상태도 업데이트합니다.
     *                       이 두 메서드는 각각 식당 테이블과 식당 정보 테이블에서 해당 식당의 상태를 업데이트하는 SQL 쿼리를 실행합니다.
     *                       따라서 이 메서드는 식당과 관련된 두 테이블의 상태를 동시에 업데이트하므로, 데이터의 일관성을 유지하는 데 도움이 됨
     */
    public void updateRestaurantStatus(Long restaurant_seq, Long admin_seq, String status, String comment) {
        restaurantMapper.updateRestaurantStatus(restaurant_seq, admin_seq, status, comment);
        restaurantMapper.updateRestaurantInfoStatus(restaurant_seq, status);
    }

    public String getRestaurantType(Long categorySeq) {
        return restaurantMapper.getRestaurantType(categorySeq);
    }

    public OwnerRestaurantDetailDTO selectRestaurantInfoWithType(Long restaurant_seq) {
        return restaurantMapper.selectRestaurantInfoWithType(restaurant_seq);
    }

    //카테고리별 레스토랑 조회
    public List<SearchResultDTO> selectSearchCategotyResultList(Long member_seq, Long category_seq) {
        List<SearchResultDTO> searchResultList = restaurantMapper.selectSearchCategotyResultList(category_seq);
        // 일치하는 항목이 있으면 isZzimed 필드를 true로 설정합니다.
//        for (SearchResultDTO restaurant : searchResultList) {
//            restaurant.setZzimed(restaurantMapper.selectZzimList(member_seq).stream()
//                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
//        }
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);

        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(zzimList.stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }

        return searchResultList;
    }

    //예약 top
    public List<SearchResultDTO> selectSearchTopResultList(Long member_seq) {
        List<SearchResultDTO> searchResultList = restaurantMapper.selectRestaurantTopSearchList();
        // 일치하는 항목이 있으면 isZzimed 필드를 true로 설정합니다.
//        for (SearchResultDTO restaurant : searchResultList) {
//            restaurant.setZzimed(restaurantMapper.selectZzimList(member_seq).stream()
//                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
//        }
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);

        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(zzimList.stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }
        return searchResultList;
    }

    //오늘의 예약
    public List<SearchResultDTO> selectSearchMonthlyResultList(Long member_seq) {
        List<SearchResultDTO> searchResultList = restaurantMapper.selectRestaurantMonthlySearchList();
        // 일치하는 항목이 있으면 isZzimed 필드를 true로 설정합니다.
//        for (SearchResultDTO restaurant : searchResultList) {
//            restaurant.setZzimed(restaurantMapper.selectZzimList(member_seq).stream()
//                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
//        }
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);

        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(zzimList.stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }
        return searchResultList;
    }

    //어디로 가시나요?
    public List<SearchResultDTO> selectSearchAddressResultList(Long member_seq, List address) {
        List<SearchResultDTO> searchResultList = restaurantMapper.selectSearchAddressResultList(address);
        // 일치하는 항목이 있으면 isZzimed 필드를 true로 설정합니다.
//        for (SearchResultDTO restaurant : searchResultList) {
//            restaurant.setZzimed(restaurantMapper.selectZzimList(member_seq).stream()
//                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
//        }
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);

        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(zzimList.stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }
        return searchResultList;
    }

    public String getAddressList(String address) {
        Map<String, String> addressMap = new LinkedHashMap<>();
        addressMap.put("apgujeong,cheongdam", "압구정,청담");
        addressMap.put("hongdae,sinchon", "홍대,신촌");
        addressMap.put("busan", "부산");
        addressMap.put("hapjeong,mangwon", "합정,망원");
        addressMap.put("sungsoo", "성수");
        addressMap.put("gangnam,yeogsam", "강남,역삼");
        addressMap.put("jeju", "제주");

        // 입력된 address에 해당하는 한글 주소 찾기
        for (String key : addressMap.keySet()) {
            if (key.contains(address)) {
                return addressMap.get(key); // 매핑된 한글 주소 문자열 반환
            }
        }
        return address; // 매핑되지 않았다면 원본 address 반환
    }


    // 타임리프에 사용할 시간 옵션을 생성하는 메서드 오픈시간부터  닫는시간 전까지 생성
    public List<TimeOptionDTO> generateTimeOptions(Long restaurant_seq) {
        RestaurantInfo restaurantinfo = restaurantMapper.selectRestaurantInfo(restaurant_seq);
        LocalTime openTime = restaurantinfo.getOpenHour().toLocalTime();
        LocalTime closeTime = restaurantinfo.getCloseHour().toLocalTime();
        LocalTime time = openTime;
        List<TimeOptionDTO> timeOptions = new ArrayList<>();
        while (!time.equals(closeTime)) {
            TimeOptionDTO option = new TimeOptionDTO();
            option.setTime(time.toString());
            timeOptions.add(option);
            time = time.plusMinutes(30);
        }
        return timeOptions;
    }


    //오늘의 식당
    public List<SearchResultDTO> selectSearchTodayResultList(Long member_seq) {
        List<SearchResultDTO> searchResultList = restaurantMapper.selectRestaurantTodaySearchList();
        // 일치하는 항목이 있으면 isZzimed 필드를 true로 설정합니다.
//        for (SearchResultDTO restaurant : searchResultList) {
//            restaurant.setZzimed(restaurantMapper.selectZzimList(member_seq).stream()
//                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
//        }
        List<RestaurantZzim> zzimList = restaurantMapper.selectZzimList(member_seq);

        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(zzimList.stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }
        return searchResultList;
    }

    public List<SearchResultDTO> getRandomRestaurant(Long memSeq) {
        return restaurantMapper.selectRandomRestaurant();
    }

    //session 초기값에 저장할 오늘 날짜 구하기.
    public String getTodayDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    //session 초기값에 저장할 가장 가까운 예약 가능 시간 구하기.
    public LocalTime getNextReservationTime() {
        // 현재 시간 가져오기
        LocalTime currentTime = LocalTime.now();

        // 다음 30분 단위의 예약 가능한 시간 계산
        int currentMinute = currentTime.getMinute();
        int nextReservationMinute = ((currentMinute / 30) + 1) * 30; // 다음 예약 가능한 분
        LocalTime nextReservationTime;

        if (nextReservationMinute == 60) {
            nextReservationTime = currentTime.plusHours(1).withMinute(0); // 정시로 설정
        } else {
            nextReservationTime = currentTime.withMinute(nextReservationMinute);
        }

        // 예약 가능한 시간 출력
        return nextReservationTime.withSecond(0).withNano(0);
    }

    public void saveRestaurantImage(String uuid, Long restaurantSeq) {
        restaurantMapper.insertRestaurantImage(uuid, restaurantSeq);
    }

    public void saveRestaurantDetail(RestaurantDetailDTO restaurantDetailDTO) {
        restaurantMapper.insertRestaurantDetail(restaurantDetailDTO);
    }

    public RestaurantDetailDTO getRestaurantDetail(Long restaurantSeq) {
        return restaurantMapper.selectRestaurantDetail(restaurantSeq);
    }

    public Long getRestaurantSeqByResSeq(Long res_seq) {
        return restaurantMapper.selectRestaurantSeqByResSeq(res_seq);
    }


    public List<SearchResultDTO> selectSearchBestResultList(Long member_seq) {
        List<SearchResultDTO> searchResultList = restaurantMapper.selectRestaurantBestSearchList();
        // 일치하는 항목이 있으면 isZzimed 필드를 true로 설정합니다.
        for (SearchResultDTO restaurant : searchResultList) {
            restaurant.setZzimed(restaurantMapper.selectZzimList(member_seq).stream()
                    .anyMatch(zzim -> zzim.getRestaurant_seq().equals(restaurant.getRestaurant_seq())));
        }
        return searchResultList;
    }

    public RestaurantEditDTO getRestaurantJoinDTO(Long restaurantSeq, Long memSeq) {
        // memSeq로 검색한 restaurant중 restaurantSeq가 있으면 OK, 아니면 권한 없음
        List<MyPageDTO> ownerRestaurantList = getOwnerRestaurantList(memSeq);
        for (MyPageDTO myPageDTO : ownerRestaurantList) {
            if (myPageDTO.getRestaurant_seq().equals(restaurantSeq)) {
                return restaurantMapper.selectRestaurantByRestaurantSeq(restaurantSeq);
            }
        }
        return null;
    }

    // 비슷한 레스토랑을 찾는 서비스 메서드
    public List<SearchResultDTO> selectSearchsameRestaurant(Long restaurant_seq, Long category_seq) {
        List<SearchResultDTO> allRestaurants = restaurantMapper.selectSearchsameRestaurantByCategory(restaurant_seq, category_seq);
        List<SearchResultDTO> randomRestaurants = new ArrayList<>();

        // allRestaurants 리스트의 크기가 8보다 작으면 그대로 반환합니다.
        if (allRestaurants.size() <= 8) {
            return allRestaurants;
        }

        // 중복되지 않도록 랜덤하게 4개의 인덱스를 선택하여 randomRestaurants 리스트에 추가합니다.
        Set<Integer> indexes = new HashSet<>();
        Random random = new Random();
        while (indexes.size() < 8) {
            int index = random.nextInt(allRestaurants.size());
            indexes.add(index);
        }
        for (int index : indexes) {
            randomRestaurants.add(allRestaurants.get(index));
        }
        return randomRestaurants;
    }

    /*사진 개수 찾기*/
    public int getPictureCountByRestaurantSeq(Long restaurant_seq) {
        return restaurantMapper.getPictureCountByRestaurantSeq(restaurant_seq);
    }


    public void updateRestaurantInfo(RestaurantEditDTO restaurantJoinDTO) {
        restaurantMapper.updateRestaurantInfo(restaurantJoinDTO);
    }

    public List<PictureDTO> selectAllPictures(Long restaurant_seq) {
        return restaurantMapper.selectAllPictures(restaurant_seq);
    }

    public List<RestaurantDetailDTO> getRestaurantDetailList() {
        return restaurantMapper.selectAllRestaurantDetailDTO();
    }

    public void updateRestaurantDetail(RestaurantDetailDTO restaurantDetailDTO) {
        restaurantMapper.updateRestaurantDetail(restaurantDetailDTO);
    }

    public void updateRestaurantImage(String uuid, Long restaurantSeq) {
        restaurantMapper.updateRestaurantImage(uuid, restaurantSeq);
    }

    public void updateRestaurantDetailExceptImg(RestaurantDetailDTO restaurantDetailDTO) {
        restaurantMapper.updateRestaurantDetailExceptImg(restaurantDetailDTO);
    }


    //회원번호, 식당번호, 지정된날짜정보에 해당되는 예약의 시간정보를 HHmm 형식으로 반환한다.
    public List<LocalTime> getBookedTimes(Long restaurant_seq, String selectedDate) {
        //회원번호, 식당번호에 해당하는 예약을 전부 뽑기
        List<Reservation> reservations = restaurantMapper.selectOnesReservation(restaurant_seq);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //yyyy-MM-dd HH:mm:ss 형식으로 되어있는 각각의 시간정보가  yyyy-MM-dd 형식의 selectedDate 를 포함하는지 보자.
        List<LocalTime> bookedTimes = new ArrayList<>();
        for (Reservation reservation : reservations) {
            String[] dateParts = reservation.getRes_date().split(" ");
            if (dateParts[0].equals(selectedDate)) {
                LocalDateTime reservationDateTime = LocalDateTime.parse(reservation.getRes_date(), formatter);
                bookedTimes.add(reservationDateTime.toLocalTime());
            }
        }
        return bookedTimes;
    }
}
