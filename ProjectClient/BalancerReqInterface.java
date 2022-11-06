import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public interface BalancerReqInterface extends Remote{
    ArrayList<String> submitRequest(String script, UUID FileID) throws RemoteException;
}