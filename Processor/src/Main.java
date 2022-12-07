import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main{
    public static Registry r = null;
    public static ProcessorManager pManager;
    static BalancerInterface bi;

    static HashMap<String, Integer> processorState = new HashMap<>();

    public static void startProcessor(int id, int port) throws IOException {

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    UUID identificador = UUID.fromString(UUID.nameUUIDFromBytes(String.valueOf(port).getBytes()).toString());
                    Registry r = LocateRegistry.createRegistry(port);
                    Processor p = new Processor(identificador,port);
                    pManager = new ProcessorManager(id, port);
                    r.rebind("Processor", pManager);
                    System.out.println("Processor " + id + " ready");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
    public static void startHBReceiver(){

        Thread multicastThread = new Thread(new Runnable() {
            public void run() {
                try { MulticastReceiver(4446); } catch (Exception ignored) {}}
        });
        multicastThread.start();
    }
    public static void startProcessorKiller(){
        Thread killerThread = new Thread(new Runnable() {
            public void run() {
                try { processorKiller(); } catch (Exception ignored) {}}
        });
        killerThread.start();
    }
    public static void main(String[] args) throws IOException {
        startHBReceiver();
        startProcessorKiller();
        startProcessor(1, 2002);
        startProcessor(2, 2003);
    }
    private static void MulticastReceiver(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);
        try {
            bi = (BalancerInterface) Naming.lookup("rmi://localhost:2001/Balancer");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg1 = new String(packet1.getData(), 0, packet1.getLength());
            List<String> qList = Arrays.asList(msg1.split(","));
            String type = qList.get(0);
            String processor = qList.get(1);
            String queue = qList.get(2);
            if(type.equals("setup")){
                processorState.putIfAbsent(processor, 0);
                bi.addProcessor(processor, queue);
            }
            if(type.equals("update")){
                processorState.replace(processor, 0);
            }
        }
    }
    private static void processorKiller() throws InterruptedException{
        try {
            bi = (BalancerInterface) Naming.lookup("rmi://localhost:2001/Balancer");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        while(true){
            Thread.sleep(1000);
            processorState.forEach((k, v) ->{
                v = v + 1;
                processorState.replace(k, v);
                if(v>30){
                    processorState.remove(k);
                    try {
                        bi.removeProcessor(k);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Processor:\t"+k);
                System.out.println("Last HB:\t"+v);
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            });
        }
    }
}