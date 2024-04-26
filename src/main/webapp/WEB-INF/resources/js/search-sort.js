let isFetching = false;

let sortByNumber = 0;
let sortLabels = $('label.nakw3z1');
let isZzimed = false;
let totalNum = 0;

sortLabels.find('input').eq(0).prop('checked', true);
//정렬모달에서 확인버튼을 눌렀을 시 정보들 재 주입
sortLabels.each(function(index, label) {
    $(label).on('click',function() {
        if(sortByNumber !== index){
            $('span.__label').text($(this).find("span").text());
            sortLabels.find('input').prop('checked', false);
            $(this).find('input').prop('checked', true);
            sortByNumber = index;
            //소트모달 지우기
            targetElement.style.visibility = 'hidden';
            isClicked = true;
            //모든값 지우기.
            reload();
        } else {
            return false;
        }
    });
});

async function reload() {
    pageNumber = 0; // page number 초기화
    pickupSearchList.empty();
    let categorySeq = $(".categorySeq").val();
    await allAmount(categorySeq);
    await ajaxAndAppendRestaurantInfo();
}

async function allAmount(categorySeq){
    return new Promise((resolve, reject) => {
        $.get({
            url: "/api/search/getRowNum/",
            method: 'GET',
            data: {
                categorySeq: categorySeq
            },
            success: function(data) {
                $('strong').text("(" + data + ")");
                totalNum = data;
                resolve();
            },
            error: function(xhr, status, error) {
                console.error("API 요청 실패:", error);
                reject(error);
            }
        });
    });
}

let $root = $("#root");
$root.on('scroll', () => {
    setTimeout(() => {
        if ($(window).innerHeight() + $root.scrollTop() >= $('#main').height() - 20 && !isFetching) {
            ajaxAndAppendRestaurantInfo();
        }
    }, 1000);
});


let pageNumber = 0; // 현재 페이지 번호
let amountInOnePage = 0;

async function ajaxAndAppendRestaurantInfo() {
    if (isFetching) return;
    isFetching = true;
    $('#loading').css('display', 'block');
    let categorySeq = $(".categorySeq").val();
    while(amountInOnePage < 5 && pageNumber <= totalNum){
        try {
            const data = await fetchData(categorySeq);
            for (const restaurantInfo of data) {
                let restaurantInfoElement = createRestaurantInfoElement(restaurantInfo);
                restaurantInfoElement.get(0).style.display = "none";
                pickupSearchList.append(restaurantInfoElement);
                let grayNum1 = await addRestaurantCss(restaurantInfoElement);
                console.log(grayNum1);
                if(grayNum1 !== 3){
                    restaurantInfoElement.get(0).style.display = "block";
                    amountInOnePage ++;
                    if(amountInOnePage === 5){
                        amountInOnePage = 0;
                        $('#loading').css('display', 'none');
                        setTimeout(() => isFetching = false, 1000);
                        return false;
                    }
                }else{
                }
                isZzimed = false;
            }
        } catch (error) {
            console.error('Error loading restaurantInfo:', error);
        } finally {
            pageNumber += 5;
        }
    }
    $('#loading').css('display', 'none');
    setTimeout(() => isFetching = false, 1000);
}

async function fetchData(categorySeq) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: `/api/search/?page=${pageNumber}&sortNum=${sortByNumber}&categorySeq=${categorySeq}`,
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                resolve(data);
            },
            error: function(xhr, status, error) {
                reject(error);
            }
        });
    });
}

function createRestaurantInfoElement(restaurantInfo) {
    // Html을 문자열로 내보내면 appendChild로 변환못해서(87번줄코드) 감싸줄 태그를 우선 만들기
    let restaurantDiv = $("<div></div>");
    let openTime = new Date('1970-01-01T' + restaurantInfo.openHour);
    let closeTime = new Date('1970-01-01T' + restaurantInfo.closeHour);
    let formattedOpenHour = openTime.getHours() + ":" + (openTime.getMinutes() < 10 ? '0' : '') + openTime.getMinutes();
    let formattedCloseHour = closeTime.getHours() + ":" + (closeTime.getMinutes() < 10 ? '0' : '') + closeTime.getMinutes();
    let text = "영업시간 " + formattedOpenHour + " ~ " + formattedCloseHour;
    if (restaurantInfo.zzimed) {
        isZzimed = true;
    }
    // 만든 html 문자열 태그속에 넣기
    restaurantDiv.html(`
        <div class="saved-restaurant-list-item">
            <div class="restaurant-info">
                <div class="tb">
                    <a href="/restaurant/detail/${restaurantInfo.restaurant_seq}">
                        <img class="img"
                            src="https://kr.object.ncloudstorage.com/bitcamp-6th-bucket-97/storage/${restaurantInfo.image_url}"
                            alt="식당메인 사진"/>
                    </a>
                </div>
                <div class="detail">
                    <div class="detail-header">
                        <div class="txt">
                            <a href="/restaurant/detail/${restaurantInfo.restaurant_seq}">
                                <h4 class="name">${restaurantInfo.restaurant_name}</h4>
                                <p class="excerpt">${restaurantInfo.description}</p>
                            </a>
                        </div>
                        <a class="btn-bookmark" data-restaurant-seq="${restaurantInfo.restaurant_seq}">북마크</a>
                    </div>
                    <a href="/restaurant/detail/${restaurantInfo.restaurant_seq}">
                        <div class="restaurant-meta">
                            <div class="_1xnbt310 _1xnbt311">
                                <div class="_1xnbt313">
                                    <img
                                        class="_1xnbt314"
                                        width="16"
                                        height="16"
                                        src="/images/star-yellow.svg"/>
                                    <span class="_1xnbt316 _1ltqxco1d">${restaurantInfo.rating}</span>
                                    <span class="_1xnbt317 _1ltqxco1g">${restaurantInfo.review_total}</span>
                                </div>
                            </div>
                            <div class="tag-wrapper">
                                <div>
                                    <span class="tags">${restaurantInfo.tag}</span>
                                    <span class="price">${text}</span>
                                </div>
                            </div>
                        </div>
                    </a>
                </div>
            </div>
        </div>
    `);
    restaurantDiv.find('div.detail').append(generateTimetable());
    return restaurantDiv;
}



async function addRestaurantCss(restaurantElement){
    if(isZzimed) {
        $(restaurantElement).find('a.btn-bookmark').addClass('active');
    }
    $(restaurantElement).find(".btn-bookmark").on("click", handleBookmarkClick);

    let calYear = $('#calYear').text();
    let calMonth = $('#calMonth').text();
    let calDate = $('td.choiceDay').text();
    let timeRadio = $('input[name="eachRestaurantTime"]', restaurantElement);
    let restaurantSeq = $(restaurantElement).find('a.btn-bookmark').data("restaurant-seq");
    let selectedDate = calYear+"-"+calMonth+"-"+calDate;
    let todayTargetInputs = timeRadio.not('.yesterday, .tomorrow');
    let yesterdayTargetInputs = $('input[name="eachRestaurantTime"].yesterday', restaurantElement);
    let tomorrowTargetInputs = $('input[name="eachRestaurantTime"].tomorrow', restaurantElement);
    let currentDate = new Date(selectedDate);
    let grayNum2 = 0;

    if(todayTargetInputs.length !== 0){
        grayNum2 += await matchWithAjaxStatusAsync(restaurantSeq, selectedDate,  todayTargetInputs);
    }
    if(yesterdayTargetInputs.length !== 0){
        let prevDate = new Date(currentDate.getTime() - 24 * 60 * 60 * 1000);
        let prevDateString = prevDate.toISOString().slice(0,10);
        grayNum2 += await matchWithAjaxStatusAsync(restaurantSeq, prevDateString, yesterdayTargetInputs);
    }
    if(tomorrowTargetInputs.length !== 0){
        let nextDate = new Date(currentDate.getTime() + 24 * 60 * 60 * 1000);
        let nextDateString = nextDate.toISOString().slice(0,10);
        grayNum2 += await matchWithAjaxStatusAsync(restaurantSeq, nextDateString, tomorrowTargetInputs);
    }
    return grayNum2;
}

function matchWithAjaxStatusAsync(restaurantSeq, selectedDate, targetInputs) {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: "POST",
            url: "/api/restaurants/getTimeStatuses/" + restaurantSeq + "/" + selectedDate,
            success: function (timeStatuses) {
                let grayNum3 = 0;
                for (let key in timeStatuses) {
                    if (timeStatuses.hasOwnProperty(key)) {
                        let timeStatus = timeStatuses[key];
                        let $matchingRadio = targetInputs.filter('[value="' + key + '"]');
                        if ($matchingRadio.length > 0) {
                            if (timeStatus !== 0) {
                                if (timeStatus === 3) {
                                    $matchingRadio.addClass('notOpen');
                                } else if (timeStatus === 2) {
                                    $matchingRadio.addClass('outdated');
                                } else if (timeStatus === 1) {
                                    $matchingRadio.addClass('booked');
                                }
                                grayNum3++;
                                $matchingRadio.prop("checked", false);
                                $matchingRadio.prop("disabled", true);
                            }
                        }
                    }
                }
                resolve(grayNum3);
            },
            error: function (xhr, status, error) {
                console.error(xhr.responseText);
                reject(error);
            }
        });
    });
}

//각 식당들의 가능 시간의 css를 넣기 element가 생성되어야 하기 때문에 따로 분리하였다.
function generateTimetable() {
    let timetableList = $('<div>').addClass('timetable-list timetable-list-sm');
    let inputTime = $('input.hour').val();
    let currentTime = new Date();
    currentTime.setHours(parseInt(inputTime.slice(0, 2)));
    currentTime.setMinutes(parseInt(inputTime.slice(2, 4)));

    let startTime = new Date(currentTime);
    startTime.setMinutes(currentTime.getMinutes() - 30);

    let endTime = new Date(currentTime);
    endTime.setMinutes(currentTime.getMinutes() + 30);

    currentTime = startTime;
    let isFirstElement = true;

    while (currentTime <= endTime) {
        let hours = currentTime.getHours();
        let minutes = currentTime.getMinutes();

        let button = $('<button>').addClass("timetable-list-item");

        let span = $('<span>').addClass("time");

        let input = $("<input>").attr({
            type: "radio",
            style: "display: none",
            name: "eachRestaurantTime",
            value: (hours < 10 ? '0' : '') + hours + "" +  (minutes < 10 ? '0' : '') + minutes
        });

        if (isFirstElement  && startTime.getHours() === 23 && startTime.getMinutes() === 30) {
            input.addClass("yesterday"); // 00시 전에는 표시 X
        } else if (currentTime.getTime() === endTime.getTime() && endTime.getHours() === 0 && endTime.getMinutes() === 0) {
            input.addClass("tomorrow");// 24시 이후에는 표시 X
        }
        span.text((hours > 12 ? "오후" : "오전") + (hours > 12 ? hours - 12 : hours) + ":" + (minutes < 10 ? '0' : '') + minutes);
        button.append(input, span);
        timetableList.append(button);

        // 30분씩 증가시킵니다.
        currentTime.setMinutes(currentTime.getMinutes() + 30);
        isFirstElement = false; // 플래그 업데이트
    }
    return timetableList;
}