package kr.co.bit.osf.flashcard.common;

public interface IntentRequestCode {
    int NONE = 1000;

    int CARD_ADD = 1001;
    int CARD_DELETE = 1002;
    int CARD_EDIT = 1003;
    int CARD_DELETE_LIST = 1004;

    int CARD_LIST_VIEW = 1005;
    int CARD_VIEW = 1006;

    int BOX_ADD = 1101;
    int BOX_DELETE = 1102;
    int BOX_EDIT = 1103;

    int CAPTURE_IMAGE = 1201;
    int SELECT_PICTURE = 1202;
}
