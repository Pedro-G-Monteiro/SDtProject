import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static java.nio.file.Files.readAllBytes;

public class ProcessorManager extends UnicastRemoteObject implements ProcessorInterface{

    HashMap<String, String> Files = new HashMap<>();

    static FileInterface fi;
    private int procId;
    private int procPort;

    private Queue<String> procQueue = new LinkedList();

    static {
        try {
            fi = (FileInterface) Naming.lookup("rmi://localhost:2000/Storage");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    protected ProcessorManager(int processId, int port) throws RemoteException{
        procId = processId;
        procPort = port;
        Thread multicastThread = new Thread(new Runnable() {
            public void run() {
                try { sendHeartbeats(procPort); } catch (Exception ignored) {}}
        });
        Thread processThread = new Thread(new Runnable() {
            public void run() {
                try { procRequest(); } catch (Exception ignored) {}}
        });
        multicastThread.start();
        processThread.start();
    }

    public void sendRequest(String script, String IDFile) throws RemoteException {
        if(script==null || IDFile == null)
            return;
        else{
            procQueue.add(script+","+IDFile);
        }
    }
    private void procRequest() throws IOException, InterruptedException{
        System.out.println("["+procId+"]Thread started");
        while(true){
            if(procQueue.iterator().hasNext()) {
                System.out.println("-------------------["+procId+"]Starting process-------------------");
                String qItem = procQueue.remove();
                List<String> qList = Arrays.asList(qItem.split(","));
                String script = qList.get(0);
                String IDFile = qList.get(1);
                Files.put(IDFile, script);
                System.out.println(script);

                try {
                    base64ToFile(script, "script");
                    String b64 = fi.getFileBase64(IDFile);
                    base64ToFile(b64, "infile");
                    FileToBase64(new File("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp\\infile.txt"));
                    Process runtimeProcess = Runtime.getRuntime().exec("D:/Uni/3º Ano/1º Semestre/Sistemas Distribuídos//Trabalho Prático/Sprint 4/Processor/temp/script.bat",
                            null,
                            new File("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp"));
                    runtimeProcess.waitFor();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()));
                    StringBuilder output = new StringBuilder();

                    String line;
                    while((line = reader.readLine()) != null){
                        output.append(line).append(System.getProperty("line.separator"));
                        System.out.println(line);
                    }
                    runtimeProcess.waitFor();
                    reader.close();

                    System.out.println("["+procId+"]Script executado!");

                    saveFile(); //SUBMIT OUTPUT
                    deleteFile(new File("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp\\infile.txt"));
                    deleteFile(new File("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp\\outfile.txt"));
                    deleteFile(new File("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp\\script.bat"));


                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                //System.out.println(procQueue.size);
            }
            Thread.sleep(1000);
        }
    }

    public void deleteFile(File f){
        try{
            f.delete();
            System.out.println("Ficheiro apagado!");
        } catch (Exception e) {
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
        File f = new File("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp\\outfile.txt");
        String base64 = FileToBase64(f);
        FileData fd = new FileData(null, "output.txt", base64);
        String UUID = fi.addFile(fd);
        System.out.println("Ficheiro guardado!");
        System.out.println(UUID);
    }

    public int getEstado() throws RemoteException {
        return 1;
    }
    public void base64ToFile(String s, String type) throws IOException {
        byte[] decodedImg = Base64.getDecoder().decode(s.getBytes(StandardCharsets.UTF_8));
        Path destinationFile;
        if(type == "script")
            destinationFile = Paths.get("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp", "script.bat");
        else
            destinationFile = Paths.get("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 4\\Processor\\temp", "infile.txt");
        java.nio.file.Files.write(destinationFile, decodedImg);
    }
    public void sendHeartbeats(int port) throws IOException, InterruptedException{
        while(true){
            String mensagem = "rmi://localhost:"+procPort+"/Processor,"+procQueue.size();
            sendMulticast(4446, mensagem);
            Thread.sleep(8000);
        }
    }
    public void sendMulticast(int port, String msg) throws IOException, InterruptedException{
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
        socket.close();
    }

}
