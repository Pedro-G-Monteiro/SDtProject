import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class BalancerManager extends UnicastRemoteObject implements BalancerInterface{
    protected BalancerManager() throws RemoteException {

    }
    static int counter = 0;

    public ArrayList<String> SendRequest(String script, String IDFile) throws RemoteException{
        ProcessorInterface pi;
        ArrayList<String> result = new ArrayList<>();
        try {
            pi = (ProcessorInterface) Naming.lookup("rmi://localhost:2002/Processor");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        counter ++;
        pi.sendRequest(script, IDFile);
        result.add(Integer.toString(counter));
        result.add("1");
        return result;
    }
}