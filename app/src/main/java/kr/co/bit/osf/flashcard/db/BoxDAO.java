package kr.co.bit.osf.flashcard.db;

import java.util.List;

public interface BoxDAO {
    BoxDTO addBox(String name);
    BoxDTO getBox(String name);
    BoxDTO getBox(int id);
    boolean addBox(BoxDTO box);
    boolean deleteBox(int id);
    boolean updateBox(BoxDTO newValue);
    List<BoxDTO> getBoxAll();
}
