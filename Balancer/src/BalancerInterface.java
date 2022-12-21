import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public interface BalancerInterface extends Remote {
    public ArrayList<String> SendRequest(String script, String IDFile) throws IOException, InterruptedException;
    public ConcurrentHashMap<String, String> getProcessStates() throws RemoteException;
    public void addProcessor(String processor, String queue) throws RemoteException;
    public void removeProcessor(String processor) throws RemoteException;
}
