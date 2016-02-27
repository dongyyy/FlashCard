package kr.co.bit.osf.flashcard.db;

public interface HelpCountDAO {
    boolean addHelpCount(int activityId);
    void updateHelpCount(int activityId);
    int getHelpCount(int activityId);
    boolean isShowHelp(int activityId);
}
