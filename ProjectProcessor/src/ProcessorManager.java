import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class ProcessorManager extends UnicastRemoteObject implements ProcessorListInterface{

    protected ProcessorManager() throws RemoteException{

    }

    void addRequest(String script, UUID idFicheiro) throws RemoteException{

    }

}
