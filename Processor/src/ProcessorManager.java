import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Base64;
import java.util.HashMap;
import static java.nio.file.Files.readAllBytes;

public class ProcessorManager extends UnicastRemoteObject implements ProcessorInterface{

    HashMap<String, String> Files = new HashMap<>();

    static FileInterface fi;

    static {
        try {
            fi = (FileInterface) Naming.lookup("rmi://localhost:2000/Storage");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    protected ProcessorManager() throws RemoteException{

    }

    public void sendRequest(String script, String IDFile) throws RemoteException {
        if(script==null || IDFile == null)
            return;

        Files.put(IDFile, script);
        System.out.println(script);

        try {
            Process runtimeProcess = Runtime.getRuntime().exec("C:\\Users\\alexa\\OneDrive\\Ambiente de Trabalho\\Projeto\\Storage\\savedFiles\\script.bat");
            BufferedReader reader = new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()));
            StringBuilder output = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null){
                output.append(line).append(System.getProperty("line.separator"));
            }
            runtimeProcess.waitFor();
            reader.close();

            System.out.println("Script executado!");

            saveFile(); //SUBMIT OUTPUT

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String FileToBase64(File file){
        try {
            byte[] fileContent = readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }

    public static void saveFile() throws IOException{
        File f = new File("C:\\Users\\Usuario\\OneDrive\\Ambiente de Trabalho\\Projeto\\Storage\\savedFiles\\output.txt");
        String base64 = FileToBase64(f);
        FileData fd = new FileData(null, "output.txt", base64);
        String UUID = fi.addFile(fd);
        System.out.println("Ficheiro guardado!");
        System.out.println(UUID);
    }

    public int getEstado() throws RemoteException {
        return 1;
    }

}
