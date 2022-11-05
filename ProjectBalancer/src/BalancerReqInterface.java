import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public interface BalancerReqInterface extends Remote{
    public ArrayList<String> submitRequest(String script, UUID FileID) throws RemoteException;

    public ArrayList<FileData> fileList() throws RemoteException;
}