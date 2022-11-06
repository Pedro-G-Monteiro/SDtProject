import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class BalancerReqManager extends UnicastRemoteObject implements BalancerReqInterface{

    static int RRcounter = 0;
    static int processCounter = 0;
    ArrayList<String> procList = new ArrayList<>();
    protected BalancerReqManager() throws RemoteException{
        this.procList.add("rmi://localhost:2024/procReq");
        this.procList.add("rmi://localhost:2025/procReq");
        this.procList.add("rmi://localhost:2026/procReq");
    }

    public ArrayList<String> submitRequest(String script, UUID FileID) throws RemoteException {
        ProcessorListInterface p;
        ArrayList<String> result = new ArrayList<>();
        try {
            processCounter++;
            p = (ProcessorListInterface) Naming.lookup(this.procList.get(RRcounter % 3));
            p.addRequest(RRcounter % 3, processCounter,script, FileID);
            result.add(String.valueOf(processCounter));
            result.add(String.valueOf(RRcounter));
            RRcounter++;
            return result;
        } catch (MalformedURLException | NotBoundException a) {
            a.printStackTrace();
        }
        return null;
    }
}