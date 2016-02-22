package kr.co.bit.osf.flashcard.db;

import android.os.Parcel;
import android.os.Parcelable;

public class CardDTO implements Parcelable {
    private int id;
    private String name;
    private String imagePath;   // image full path
    private int imageId;        // image id (only demo data)
    private int type;           // 0:user card, 1:demo card
    int seq;
    int boxId;                  // Box id

    public CardDTO() { }

    public CardDTO(String name, String imagePath, int boxId) {
        this.name = name;
        this.imagePath = imagePath;
        this.imageId = 0;
        this.type = FlashCardDB.CardEntry.TYPE_USER;
        this.seq = 0;
        this.boxId = boxId;
    }

    public CardDTO(String name, String imagePath, int type, int boxId) {
        this(name, imagePath, boxId);
        this.type = type;
    }

    public CardDTO(int id, String name, String imagePath, int type, int boxId) {
        this(name, imagePath, type, boxId);
        this.id = id;
    }

    public CardDTO(int id, String name, String imagePath, int type, int seq, int boxId) {
        this(id, name, imagePath, type, boxId);
        this.seq = seq;
    }

    public CardDTO(int id, String name, String imagePath, int imageId, int type, int seq, int boxId) {
        this(id, name, imagePath, type, seq, boxId);
        this.imageId = imageId;
    }

    protected CardDTO(Parcel in) {
        id = in.readInt();
        name = in.readString();
        imagePath = in.readString();
        imageId = in.readInt();
        type = in.readInt();
        seq = in.readInt();
        boxId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(imagePath);
        dest.writeInt(imageId);
        dest.writeInt(type);
        dest.writeInt(seq);
        dest.writeInt(boxId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CardDTO> CREATOR = new Creator<CardDTO>() {
        @Override
        public CardDTO createFromParcel(Parcel in) {
            return new CardDTO(in);
        }

        @Override
        public CardDTO[] newArray(int size) {
            return new CardDTO[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
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
        if (imageId != cardDTO.imageId) return false;
        if (type != cardDTO.type) return false;
        if (seq != cardDTO.seq) return false;
        if (boxId != cardDTO.boxId) return false;
        if (!name.equals(cardDTO.name)) return false;
        return imagePath.equals(cardDTO.imagePath);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + imagePath.hashCode();
        result = 31 * result + imageId;
        result = 31 * result + type;
        result = 31 * result + seq;
        result = 31 * result + boxId;
        return result;
    }

    @Override
    public String toString() {
        return "CardDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", imageId=" + imageId +
                ", type=" + type +
                ", seq=" + seq +
                ", boxId=" + boxId +
                '}';
    }
}
