import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface StorageInterface extends Remote{
    void base64ToFile(FileData f) throws IOException;

    String addFile(FileData f) throws RemoteException, InterruptedException;

    String getFileBase64(String UUID) throws RemoteException, InterruptedException;

    String getFileName(String FileID) throws RemoteException, InterruptedException;

    ArrayList<FileData> fileList() throws RemoteException, InterruptedException;

}