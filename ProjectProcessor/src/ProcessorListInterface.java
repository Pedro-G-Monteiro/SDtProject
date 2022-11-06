import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ProcessorListInterface extends Remote {
    public void addRequest(int idProcessor, int idPedido, String script, UUID idFicheiro) throws RemoteException;

}
