package kr.co.bit.osf.flashcard.db;

public class HelpCountDTO {
    private int id;
    private int activityId;
    private int count;

    public HelpCountDTO(int activityId) {
        this.activityId = activityId;
    }

    public HelpCountDTO(int activityId, int count) {
        this.activityId = activityId;
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HelpCountDTO helpCountDTO = (HelpCountDTO) o;

        if (id != helpCountDTO.id) return false;
        if (activityId != helpCountDTO.activityId) return false;
        return count == helpCountDTO.count;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + activityId;
        result = 31 * result + count;
        return result;
    }

    @Override
    public String toString() {
        return "HelpCountDTO{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", count=" + count +
                '}';
    }
}
