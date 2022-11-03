import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
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
        FileListInterface l = null;
        File f = new File("C:\\Users\\IONJi\\Desktop\\Doggo.jpg");
        String base64 = FileToBase64(f);
        UUID UUID;
        try{
            l  = (FileListInterface) Naming.lookup("rmi://localhost:22222/filelist");
            FileData fd = new FileData(null, "Doggo.jpg", base64);
            UUID = l.addFile(fd);
            System.out.print("Your File UUID: ");
            System.out.println(UUID);

        } catch(RemoteException e) {
            System.out.println(e.getMessage());
        }catch(Exception e) {e.printStackTrace();}
    }
}