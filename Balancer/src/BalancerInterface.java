import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface BalancerInterface extends Remote {
    public ArrayList<String> SendRequest(String script, String IDFile) throws RemoteException;
}
