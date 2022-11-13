import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProcessorInterface extends Remote {

    void sendRequest(String script, String IDFile) throws RemoteException;

    int getEstado() throws RemoteException;

}