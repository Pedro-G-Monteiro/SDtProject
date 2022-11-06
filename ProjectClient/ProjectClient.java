import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class ProjectClient {

    public static String FileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }

    private static void reachServer(BalancerReqInterface br, int nPedidos) {
        while (nPedidos != 0) {
            FileListInterface l;
            File f = new File("C:\\Users\\Usuario\\OneDrive\\Ambiente de Trabalho\\Doggo.jpg");
            String base64 = FileToBase64(f);
            UUID UUID;

            String processorID = br.getProcessor(String.valueOf(nPedidos));
            String[] aux = processorID.split("//", 2);
            String[] processorIdentification = aux[1].split("/",2);

            try {
                l = (FileListInterface) Naming.lookup(processorID);
                FileData fd = new FileData(null, "Doggo.jpg", base64);
                UUID = l.addFile(fd);
                System.out.println("Your request was sent to: " + processorIdentification[1]);
                System.out.print("Your File UUID: ");
                System.out.println(UUID);

            } catch (RemoteException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            nPedidos--;
        }
    }

    public static void GetServer(BalancerReqInterface br) {
        reachServer(br, 13);
    }

    public static void loadBalance() {
        GetServer(new RoundRobinLoadBalancer());
    }

    public static void main(String[] args) {
        loadBalance();
    }
}