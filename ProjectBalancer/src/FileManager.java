import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class FileManager extends UnicastRemoteObject implements FileListInterface {

    private HashMap<UUID, String> fileList = new HashMap<>();
    protected FileManager() throws RemoteException{

    }
    public FileManager(HashMap<UUID, String> fileList) throws RemoteException{
        this.fileList=fileList;
    }
    public void base64ToFile(FileData f) throws IOException {
        byte[] decodedImg = Base64.getDecoder()
                .decode(f.getFileBase64().getBytes(StandardCharsets.UTF_8));
        Path destinationFile = Paths.get("./savedFiles", f.getFileName());
        Files.write(destinationFile, decodedImg);
    }
    public UUID addFile(FileData f) throws RemoteException {
        UUID id;
        id = UUID.fromString(UUID.nameUUIDFromBytes(String.valueOf(f.getFileBase64()).getBytes()).toString());;
        f.setFileID(id);
        this.fileList.put(f.getFileID(), f.getFileName());
        try {
            base64ToFile(f);
        }catch (Exception e) {
            System.out.println("base64ToFile Error: " + e.getMessage());
        }
        return id;
    }
    public String getFileName(UUID FileID) throws RemoteException{
        if(fileList.get(FileID) != null){
            return fileList.get(FileID);
        }
        else{
            return null;
        }
    }
    public HashMap<UUID, String> fileList() throws RemoteException {
        return fileList;
    }
}