import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Processor3 {

    public static Registry r = null;

    public static FileManager fileList;

    public static void main(String[] args) {

        try {
            r = LocateRegistry.createRegistry(2005);
        } catch (RemoteException a) {
            a.printStackTrace();
        }

        try {
            fileList = new FileManager();
            r.rebind("processor3", fileList);

            System.out.println("File server ready");
        } catch (Exception e) {
            System.out.println("File server main " + e.getMessage());
        }
    }
}