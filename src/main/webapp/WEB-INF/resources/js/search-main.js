
// 예약날짜 모달
let resModal = $('#resModal');
let topOpenBtn = $("div.datetime-selector a");
let topCloseBtn = $(".btn-close");
let bottomCloseBtn =  $("div.sticky-bottom-btns > button");
let btnBack = $(".btn-back");
let pickupSearchList = $('div#pickup_search_list'); // 레스토랑Info 감싸는 div



$(function () {
  btnBack.click(function () {
    location.href = "/";
  } );

  // 히든 input 태그값으로 모달창에 정보 입력되게 하는 메소드
  assigningInfo();

  // 모달창 정보를 히든 input과 span 태그에 각각 저장하고 session
  fillingInfo();

  //초기 데이터 넣기
  reload()

  topOpenBtn.click(function () {
    resModal.css("visibility", 'visible');
    resModal.css("transform", 'translateY(-100%)');
    assigningInfo();
  });
  topCloseBtn.click(function () {
    resModal.css("transform", 'translateY(+100%)');
    resModal.css("visibility", 'hidden');
  });
  bottomCloseBtn.click(function () {
    fillingInfo();
    reload()
    resModal.css("transform", 'translateY(+100%)');
    resModal.css("visibility", 'hidden');
  });
});

// 추천순 버튼 클릭
let sortButton = document.querySelector('._54rkq61');
let targetElement = document.querySelector('._1cjtq760._1ltqxco1o._54rkq63');
targetElement.style.visibility='hidden';
let isClicked = true;

sortButton.addEventListener('click', function() {
  if (!isClicked) {
    targetElement.style.visibility = 'hidden';
    isClicked = true;
  } else {
    targetElement.style.visibility = 'visible';
    isClicked = false;
  }
});