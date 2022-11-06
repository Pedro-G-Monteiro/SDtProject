import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ProcessorListInterface extends Remote {
    void addRequest(String script, UUID idFicheiro) throws RemoteException;

}
