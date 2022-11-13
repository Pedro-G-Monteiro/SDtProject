import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FileInterface extends Remote{
    void base64ToFile(FileData f) throws IOException;

    String addFile(FileData f) throws RemoteException;

    String getFileName(String FileID) throws RemoteException;

    ArrayList<FileData> fileList() throws RemoteException;

}