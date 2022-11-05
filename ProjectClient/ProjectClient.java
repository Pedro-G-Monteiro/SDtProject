import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class ProjectClient {

    public static String FileToBase64(File file){
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }
    public static void main(String[] args) {
        ArrayList<String> reqList = new ArrayList<String>();
        //FileListInterface l = null;
        BalancerReqInterface br = null;
        File f = new File("C:\\Users\\Usuario\\Desktop\\Doggo.jpg");
        String base64 = FileToBase64(f);
        UUID UUID;
        try{

            br = (BalancerReqInterface) Naming.lookup("rmi://localhost:2022/balancerReq");
            reqList = br.submitRequest(("saveFile " + base64), null);
            System.out.println("Request ID: " + reqList.get(0));
            System.out.println("Processor ID" + reqList.get(1));

            /* ACESSO DIRETO A STORAGE

            l  = (FileListInterface) Naming.lookup("rmi://localhost:22222/filelist");UUID = l.addFile(fd);
            FileData fd = new FileData(null, "Doggo.jpg", base64);
            System.out.print("Your File UUID: ");
            System.out.println(UUID);
            */




        } catch(RemoteException e) {
            System.out.println(e.getMessage());
        }catch(Exception e) {e.printStackTrace();}
    }
}

/*
            l  = (FileListInterface) Naming.lookup("rmi://localhost:22222/filelist");
            FileData fd = new FileData(null, "Doggo.jpg", base64);
            UUID = l.addFile(fd);
            System.out.print("Your File UUID: ");
            System.out.println(UUID);

            balancerRequest r = new balancerRequest(????, UUID);
            int idRequest, idProcessor;

            getState() //ATRAVES DO LOOKUP e recebe o estado

*
*
*
* */