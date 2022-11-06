import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ProjectBalancer {
    public static Registry r = null;

    public static BalancerReqManager balancer;

    public static void main(String[] args) {

        try {
            r = LocateRegistry.createRegistry(2023);
        } catch (RemoteException a) {
            a.printStackTrace();
        }

        try {
            balancer = new BalancerReqManager();
            r.rebind("balancer", balancer);
            System.out.println("File server ready");
        } catch (Exception e) {
            System.out.println("File server main " + e.getMessage());
        }
    }
}