package kr.co.bit.osf.flashcard.db;

import java.util.List;

public interface CardDAO {
    boolean addCard(CardDTO card);
    CardDTO getCard(int id);
    boolean deleteCard(int id);
    boolean deleteCard(List<CardDTO> list);
    boolean updateCard(CardDTO newValue);
    List<CardDTO> getCardByBoxId(int boxId);
    CardDTO getTopCardByBoxId(int boxId);
    boolean deleteCardByBoxId(int boxId);
    int getCardCountByBoxId(int boxId);
    boolean updateCardSeq(int id, int seq);
    boolean updateCardSeq(List<CardDTO> cardList);
}
