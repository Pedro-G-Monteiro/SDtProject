import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public interface CoordenadorInterface extends Remote {
    public ConcurrentHashMap<String, String> getProcessors() throws RemoteException;
}
