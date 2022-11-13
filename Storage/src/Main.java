import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static Registry r=null;
    public static Storage Storage;
    public static void main(String[] args) {

        try {
            r = LocateRegistry.createRegistry(2000);
        } catch (RemoteException a) {
            a.printStackTrace();
        }

        try {
            Storage = new Storage();
            r.rebind("Storage", Storage);

            System.out.println("Storage service ready");
        } catch (Exception e) {
            System.out.println("Storage service main " + e.getMessage());
        }
    }
}