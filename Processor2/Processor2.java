import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Processor2 {

    public static Registry r = null;

    public static FileManager fileList;

    public static void main(String[] args) {

        try {
            r = LocateRegistry.createRegistry(2004);
        } catch (RemoteException a) {
            a.printStackTrace();
        }

        try {
            fileList = new FileManager();
            r.rebind("processor2", fileList);

            System.out.println("File server ready");
        } catch (Exception e) {
            System.out.println("File server main " + e.getMessage());
        }
    }
}