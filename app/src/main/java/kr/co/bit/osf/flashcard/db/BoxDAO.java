package kr.co.bit.osf.flashcard.db;

import java.util.List;

public interface BoxDAO {
    BoxDTO addBox(String name);
    BoxDTO getBox(String name);
    BoxDTO getBox(int id);
    boolean addBox(BoxDTO box);
    boolean deleteBox(int id);
    boolean updateBox(BoxDTO newValue);
    boolean updateBoxSeq(int id, int seq);
    boolean updateBoxSeq(List<BoxDTO> boxList);
    List<BoxDTO> getBoxAll();
}
