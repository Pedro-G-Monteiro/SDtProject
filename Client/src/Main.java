import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

/*
* Ports:
* 2000 -> Storage
* 2001 -> Balancer
* 2002 -> Processor
* */

public class Main {
    static Scanner getOption = new Scanner(System.in);
    static FileInterface fi;
    static BalancerInterface bi;
    static ProcessorInterface pi;

    static {
        try {
            fi = (FileInterface) Naming.lookup("rmi://localhost:2000/Storage");
            bi = (BalancerInterface) Naming.lookup("rmi://localhost:2001/Balancer");
            pi = (ProcessorInterface) Naming.lookup("rmi://localhost:2002/Processor");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile() throws IOException{
        File f = new File("C:\\Users\\Usuario\\OneDrive\\Ambiente de Trabalho\\Projeto\\teste.txt");
        String base64 = FileToBase64(f);
        FileData fd = new FileData(null, "teste.txt", base64);
        String UUID = fi.addFile(fd);
        System.out.println("Ficheiro guardado!");
        System.out.println(UUID);
    }

    public static void getFile() throws IOException
    {
        String IDFile;
        System.out.println("ID do ficheiro:");
        IDFile = getOption.next();
        String fileName = fi.getFileName(IDFile);
        System.out.println("O nome do ficheiro é: "+ fileName);
    }

    public static void getEstado() throws RemoteException {
        int estado = 0;
        estado = pi.getEstado();
        if(estado==0)
        {
            System.out.println("Não Enviado");
        }
        else if (estado==1)
        {
            System.out.println("Ficheiro no processador.");
        }
    }

    public static void CreateRequest () throws IOException{
        String filepath, id;
        System.out.println("ID do ficheiro a enviar para o processador:");
        id = getOption.next();
        System.out.println("URL do Script:");
        getOption.nextLine();
        filepath = getOption.next();
        ArrayList<String> result = bi.SendRequest(filepath, id);

        System.out.println("ID do pedido: "+ result.get(0));
        System.out.println("ID do processador: " + result.get(1));
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
            System.out.println("| [0] Sair                                              |");
            System.out.println("+-------------------------------------------------------+");
            opcao = getOption.next();
            switch (opcao) {
                case "1" -> saveFile(); //Storage -> SPRINT 1
                case "2" -> getFile(); //Storage -> SPRINT 1
                case "3" -> CreateRequest(); //Balancer -> SPRINT 2
                case "4" -> getEstado(); //Processador -> SPRINT 2
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