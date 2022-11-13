import java.io.Serializable;
import java.util.UUID;

public class FileData implements Serializable {

    private String fileID;

    private String fileName;

    private String fileBase64;

    public FileData(String fileID, String fileName, String fileBase64){
        this.fileID = fileID;
        this.fileName = fileName;
        this.fileBase64 = fileBase64;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(UUID fileID) {
        this.fileID = fileID.toString();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }
}