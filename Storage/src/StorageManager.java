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

public class StorageManager extends UnicastRemoteObject implements StorageInterface {

    private ArrayList<FileData> fileList = new ArrayList<>();

    public Lock lk = new Lock();
    protected StorageManager() throws RemoteException{

    }
    public StorageManager(ArrayList<FileData> fileList) throws RemoteException, InterruptedException {
        lk.lock();
        this.fileList=fileList;
        lk.unlock();
    }
    public void base64ToFile(FileData f) throws IOException {
        byte[] decodedImg = Base64.getDecoder().decode(f.getFileBase64().getBytes(StandardCharsets.UTF_8));
        Path destinationFile = Paths.get("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Storage\\savedFiles", f.getFileName());
        Files.write(destinationFile, decodedImg);
    }

    public String addFile(FileData f) throws RemoteException, InterruptedException {
        UUID id;
        id = UUID.fromString(UUID.nameUUIDFromBytes(String.valueOf(f.getFileBase64()).getBytes()).toString());
        f.setFileID(id);
        lk.lock();
        this.fileList.add(f);
        try {
            base64ToFile(f);
        }catch (Exception e) {
            System.out.println("base64ToFile Error: " + e.getMessage());
        }
        lk.unlock();
        return f.getFileID();
    }
    public String getFileBase64(String FileID) throws RemoteException, InterruptedException {
        lk.lock();
        for (FileData fileData : fileList) {
            if (FileID.equals(fileData.getFileID())) {
                return fileData.getFileBase64();
            }
        }
        lk.unlock();
        return null;
    }

    public String getFileName(String FileID) throws RemoteException, InterruptedException {
        lk.lock();
        for (FileData fileData : fileList) {
            if (FileID.equals(fileData.getFileID())) {
                return fileData.getFileName();
            }
        }
        lk.unlock();
        return null;
    }

    public void saveOutput(FileData f) throws RemoteException, InterruptedException {
        lk.lock();
        fileList.add(f);
        lk.unlock();
    }

    public ArrayList<FileData> fileList() throws RemoteException, InterruptedException {
        return fileList;
    }
}