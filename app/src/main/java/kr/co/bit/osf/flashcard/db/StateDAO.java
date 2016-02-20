package kr.co.bit.osf.flashcard.db;

public interface StateDAO {
    boolean addState(int boxId, int cardId);
    StateDTO getState();
    StateDTO getState(int id);
    boolean deleteState(int id);
    boolean updateState(StateDTO newValue);
    boolean updateState(int boxId, int cardId);
}
