import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ProcessorManager extends UnicastRemoteObject implements ProcessorListInterface{

    HashMap<UUID, String> fileData;
    HashMap<Integer, Integer> processorInfos;

    HashMap<HashMap<Integer, Integer>, HashMap<UUID, String>> filesToProcess;
    protected ProcessorManager() throws RemoteException{

    }

    public void addRequest(int idProcessor, int idPedido, String script, UUID idFicheiro) throws RemoteException{
        this.fileData.put(idFicheiro, script);
        this.processorInfos.put(idProcessor, idPedido);
        this.filesToProcess.put(this.processorInfos, this.fileData);
    }

}
