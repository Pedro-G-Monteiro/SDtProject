import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * Ports:
 * 2000 -> Storage
 * 2001 -> Balancer
 * 2002 -> Processor
 * */


public class Main implements Serializable {
    public static Registry r = null;
    public  static BalancerManager balancer;

    public static void main(String[] args) {
        try{
            r = LocateRegistry.createRegistry(2001);
        }catch(RemoteException a){
            a.printStackTrace();
        }
        try{
            balancer = new BalancerManager();
            r.rebind("Balancer", balancer );
            System.out.println("Balancer Service is ready");


        }catch(Exception e) {
            System.out.println("->"+e.getMessage());
        }
    }
}