import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class BalancerReqManager extends UnicastRemoteObject implements BalancerReqInterface{

    protected BalancerReqManager() throws RemoteException{

    }

    public ArrayList<String> submitRequest(String script, UUID FileID) throws RemoteException{

    }

    public ArrayList<FileData> fileList() throws RemoteException{

    }
}