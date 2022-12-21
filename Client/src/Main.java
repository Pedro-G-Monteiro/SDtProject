import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
* Ports:
* 2000 -> Storage
* 2001 -> Balancer
* 2002 -> Processor 1
* 2003 -> Processor 2
* 2004 -> Processor 3
* 2005 -> Processor 4
* */

public class Main {
    static Scanner getOption = new Scanner(System.in);
    static StorageInterface si;
    static BalancerInterface bi;
    static ProcessorInterface pi1;
    static ProcessorInterface pi2;

    static String testUUID;

    static {
        try {
            si = (StorageInterface) Naming.lookup("rmi://localhost:2000/Storage");
            bi = (BalancerInterface) Naming.lookup("rmi://localhost:2001/Balancer");
            pi1 = (ProcessorInterface) Naming.lookup("rmi://localhost:2002/Processor");
            pi2 = (ProcessorInterface) Naming.lookup("rmi://localhost:2003/Processor");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile() throws IOException{
        File f = new File("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 5\\Client\\teste.txt");
        String base64 = FileToBase64(f);
        FileData fd = new FileData(null, "teste.txt", base64);
        String UUID = si.addFile(fd);
        testUUID = UUID;
        System.out.println("Ficheiro guardado!");
        System.out.println(UUID);
    }

    public static void getFile() throws IOException
    {
        String IDFile;
        System.out.println("ID do ficheiro:");
        IDFile = getOption.next();
        String fileName = si.getFileName(IDFile);
        System.out.println("O nome do ficheiro é: "+ fileName);
    }

    public static void getEstado() throws RemoteException {
        int estado1 = 0;
        int estado2 = 0;
        estado1 = pi1.getEstado();
        estado2 = pi2.getEstado();
        if(estado1==0)
        {
            System.out.println("--Processor 1--");
            System.out.println("Não Enviado");
        }
        else if (estado1==1)
        {
            System.out.println("--Processor 1--");
            System.out.println("Ficheiro no processador.");
        }
        if(estado2==0)
        {
            System.out.println("--Processor 2--");
            System.out.println("Não Enviado");
        }
        else if (estado2==1)
        {
            System.out.println("--Processor 2--");
            System.out.println("Ficheiro no processador.");
        }
    }

    public static void CreateRequest () throws IOException{
        String filepath, id;
        System.out.println("ID do ficheiro a enviar para o processador:");
        id = getOption.next();
        System.out.println("Caminho do Script:");
        getOption.nextLine();
        filepath = getOption.next();
        String b64 = FilePathToBase64(Paths.get(filepath));
        ArrayList<String> result = bi.SendRequest(b64, id);
        System.out.println("ID do pedido: "+ result.get(0));
        System.out.println("ID do processador: " + result.get(1));
    }
    public static void createNRequests() throws IOException{
        Integer n;
        System.out.println("Quantidade de pedidos a fazer:");
        n = Integer.valueOf(getOption.next());
        for(int i = 0; i<n; i++) {
            String id = testUUID;
            String b64 = FilePathToBase64(Paths.get("D:\\Uni\\3º Ano\\1º Semestre\\Sistemas Distribuídos\\Trabalho Prático\\Sprint 5\\Client\\script.bat"));
            ArrayList<String> result = bi.SendRequest(b64, id);
            System.out.println("ID do pedido: "+ result.get(0));
            System.out.println("ID do processador: " + result.get(1));
        }
    }
    public static void getProcessStatus() throws RemoteException{
        ConcurrentHashMap<String, String> status = bi.getProcessStates();
        for (Map.Entry<String, String> entry : status.entrySet()) {
            System.out.println("Processor:\t" + entry.getKey());
            System.out.println("Queue:\t\t" + entry.getValue());
        }
    }
    public static void Menu() throws IOException {
        String opcao;
        while(true)
        {
            System.out.println("+-------------------------------------------------------+");
            System.out.println("|                         MENU                          |");
            System.out.println("+-------------------------------------------------------+");
            System.out.println("| [1] Guardar ficheiro na Storage                       |");
            System.out.println("| [2] Enviar um identificador e receber um ficheiro     |");
            System.out.println("| [3] Enviar um pedido de execução para um processador  |");
            System.out.println("| [4] Saber o estado do pedido                          |");
            System.out.println("| [5] Enviar N requests                                 |");
            System.out.println("| [6] Recebe estados dos processadores                  |");
            System.out.println("| [0] Sair                                              |");
            System.out.println("+-------------------------------------------------------+");
            opcao = getOption.next();
            switch (opcao) {
                case "1" -> saveFile(); //Storage -> SPRINT 1
                case "2" -> getFile(); //Storage -> SPRINT 1
                case "3" -> CreateRequest(); //Balancer -> SPRINT 2
                case "4" -> getEstado(); //Processador -> SPRINT 2
                case "5" -> createNRequests();
                case "6" -> getProcessStatus();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }
    public static String FileToBase64(File file){
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }
    public static String FilePathToBase64(Path path){
        try {
            byte[] fileContent = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + e);
        }
    }

    public static void main(String[] args)
    {
        try {
            Menu();
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}