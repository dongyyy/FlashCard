# FlashCard

project name
    - FlashCard

package name
    - kr.co.bit.osf.flashcard

activity 목록
    - MainActivity (activity_main.xml)
    - BoxListAcivity (activity_box_list.xml)
    - CardListActivity (activity_card_list.xml)
    - CardViewActivity (activity_card_view.xml)
    
activity 주요 기능
  - MainActivity
    - FlashCard DB 초기화
    - Demo Data 만들기
  - BoxListActivity
    - Box를 추가
    - Box를 수정
    - Box를 삭제
  - CardListActivity
    - Card를 추가
      : 갤러리나 FlashCard 디렉토리에 있는 이미지 불러오기
      : 카메라로 촬영한 이미지를 FlashCard 디렉토리에 저장하고 불러오기
      : 설명 추가
    - Card를 수정
      : 갤러리나 FlashCard 디렉토리에 있는 이미지 불러오기
      : 카메라로 촬영한 이미지를 FlashCard 디렉토리에 저장하고 불러오기
      : 설명 수정
    - delete Card
  - CardViewActivity
    : Card를 보여주고 이미지를 클릭시 Flip animation으로
      사용자가 입력한 text를 보여주기
      
activity State 자료 (StateDTO)
  : 다른 activity로 전환되기 전에 반드시 state 자료를 수정하고 저장할 것
  - MainActivity 
    : boxId = 0, cardId = 0
  - BoxListActivity
    : boxId = 0, cardId = 0
  - CardListActivity
    : boxId = N, cardId = 0
  - CardListActivity
    : boxId = N, cardId = M

naming
  - widget name
    : activity name prefix + name + widget name
    ex) cardViewImageView, cardViewTextView
        boxListBoxDeleteButton, cardListEditCardButton
        cardListCaptureButton, ...
  - widget text
    : resource string에 추가하여 사용할 것.
    ex) card_list_capture_button_text, 
        box_list_box_delete_button_text, ...
    
