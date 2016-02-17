package kr.co.bit.osf.flashcard.db;

public class CardDTO {
    private long id;
    private String name;
    private String imagePath;   // image full path
    private int type;           // 0:user card, 1:demo card
    int seq;
    int boxId;                  // Box id

    public CardDTO() { }

    public CardDTO(String name, String imagePath, int boxId) {
        this.name = name;
        this.imagePath = imagePath;
        this.boxId = boxId;
    }

    public CardDTO(String name, String imagePath, int type, int boxId) {
        this(name, imagePath, boxId);
        this.type = type;
    }

    public CardDTO(long id, String name, String imagePath, int type, int boxId) {
        this(name, imagePath, type, boxId);
        this.id = id;
    }

    public CardDTO(long id, String name, String imagePath, int type, int seq, int boxId) {
        this(id, name, imagePath, type, boxId);
        this.seq = seq;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int order) {
        this.seq = order;
    }

    public int getBoxId() {
        return boxId;
    }

    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardDTO cardDTO = (CardDTO) o;

        if (id != cardDTO.id) return false;
        if (type != cardDTO.type) return false;
        if (seq != cardDTO.seq) return false;
        if (boxId != cardDTO.boxId) return false;
        if (!name.equals(cardDTO.name)) return false;
        return imagePath.equals(cardDTO.imagePath);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + imagePath.hashCode();
        result = 31 * result + type;
        result = 31 * result + boxId;
        return result;
    }

    @Override
    public String toString() {
        return "CardDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", type=" + type +
                ", seq=" + seq +
                ", boxId=" + boxId +
                '}';
    }
}
