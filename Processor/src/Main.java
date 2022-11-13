import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

public class Main{
    public static Registry r = null;
    public static ProcessorManager pManager;

    public static void main(String[] args){
        UUID identificador;
        String port="2002";
        identificador = UUID.fromString(UUID.nameUUIDFromBytes((port).getBytes()).toString());
        Processor p = new Processor(identificador,2002);
        try{
            r = LocateRegistry.createRegistry(p.getPort());
        }catch(RemoteException a){
            a.printStackTrace();
        }
        try{
            pManager = new ProcessorManager();
            r.rebind("Processor", pManager);
            System.out.println("Processor ready");
        }catch(Exception e) {
            System.out.println("->" + e.getMessage());
        }
    }
}