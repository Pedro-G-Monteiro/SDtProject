import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class Storage extends UnicastRemoteObject implements FileInterface {

    private ArrayList<FileData> fileList = new ArrayList<>();
    protected Storage() throws RemoteException{

    }
    public Storage(ArrayList<FileData> fileList) throws RemoteException{
        this.fileList=fileList;
    }
    public void base64ToFile(FileData f) throws IOException {
        byte[] decodedImg = Base64.getDecoder().decode(f.getFileBase64().getBytes(StandardCharsets.UTF_8));
        Path destinationFile = Paths.get("C:\\Users\\Usuario\\OneDrive\\Ambiente de Trabalho\\Projeto\\Storage\\savedFiles", f.getFileName());
        Files.write(destinationFile, decodedImg);
    }

    public String addFile(FileData f) throws RemoteException {
        UUID id;
        id = UUID.fromString(UUID.nameUUIDFromBytes(String.valueOf(f.getFileBase64()).getBytes()).toString());
        f.setFileID(id);
        this.fileList.add(f);
        try {
            base64ToFile(f);
        }catch (Exception e) {
            System.out.println("base64ToFile Error: " + e.getMessage());
        }
        return f.getFileID();
    }

    public String getFileName(String FileID) throws RemoteException{
        for (FileData fileData : fileList) {
            if (FileID.equals(fileData.getFileID())) {
                return fileData.getFileName();
            }
        }
        return null;
    }

    public void saveOutput(FileData f) throws RemoteException{
        fileList.add(f);
    }

    public ArrayList<FileData> fileList() throws RemoteException {
        return fileList;
    }
}