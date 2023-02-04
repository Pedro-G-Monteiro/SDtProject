import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProcessorInterface extends Remote {

    void sendRequest(String script, String IDFile) throws IOException, InterruptedException;

    int getEstado() throws RemoteException;

}