import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public interface FileListInterface extends Remote{

    public UUID addFile(FileData f) throws RemoteException;

    public String getFileName(UUID FileID) throws RemoteException;

    public HashMap<UUID, String> fileList() throws RemoteException;
}