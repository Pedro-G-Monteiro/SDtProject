import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main implements Serializable {
    public static Registry r = null;
    public static CoordenadorManager Coordenador;

    public static void main(String[] args) {

        try {
            r = LocateRegistry.createRegistry(2100);
        } catch (RemoteException a) {
            a.printStackTrace();
        }

        try {
            Coordenador = new CoordenadorManager();
            r.rebind("Coordenador", Coordenador);
            System.out.println("Coordenador service ready");
        } catch (Exception e) {
            System.out.println("Coordenador service main " + e.getMessage());
        }
    }
}