import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.UUID;

public interface FileListInterface extends Remote{
    void base64ToFile(FileData f) throws IOException;

    UUID addFile(FileData f) throws RemoteException;

    String getFileName(UUID FileID) throws RemoteException;

    HashMap<UUID, String> fileList() throws RemoteException;
}