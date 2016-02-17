package kr.co.bit.osf.flashcard.db;

public class StateDTO {
    private long id;
    int boxId;                  // Box id
    int cardId;                 // Card id

    public StateDTO() { }

    public StateDTO(int boxId, int cardId) {
        this.boxId = boxId;
        this.cardId = cardId;
    }

    public StateDTO(int id, int boxId, int cardId) {
        this(boxId, cardId);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getBoxId() {
        return boxId;
    }

    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateDTO stateDTO = (StateDTO) o;

        if (id != stateDTO.id) return false;
        if (boxId != stateDTO.boxId) return false;
        return cardId == stateDTO.cardId;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + boxId;
        result = 31 * result + cardId;
        return result;
    }

    @Override
    public String toString() {
        return "StateDTO{" +
                "id=" + id +
                ", boxId=" + boxId +
                ", cardId=" + cardId +
                '}';
    }
}
