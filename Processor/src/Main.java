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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main{
    static boolean stopOrder = false;
    public static Registry r = null;
    public static ProcessorManager pManager;
    static ProcessorInterface pi;
    static BalancerInterface bi;

    static ConcurrentHashMap<String, Integer> processorState = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> processorLoad = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, List<Task>> processorData = new ConcurrentHashMap<>();

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
                try { MulticastReceiver(4447); } catch (Exception ignored) {}}
        });
        multicastThread.start();
    }
    public static void startTaskQueueReceiver(){

        Thread multicastThread = new Thread(new Runnable() {
            public void run() {
                try {MulticastReceiver(4448);} catch (Exception ignored) {}}
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
        startTaskQueueReceiver();
        startProcessorKiller();
        startProcessor(1, 2002);
        startProcessor(2, 2003);
    }
    private synchronized static void MulticastReceiver(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[25600];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);
        if(port == 4447) {
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
                if (type.equals("setup")) {
                    processorState.putIfAbsent(processor, 0);
                    processorLoad.putIfAbsent(processor, Integer.parseInt(queue));
                    bi.addProcessor(processor, queue);
                }
                if (type.equals("update")) {
                    processorState.replace(processor, 0);
                    processorLoad.replace(processor, Integer.parseInt(queue));
                }
                if(stopOrder)
                    Thread.currentThread().interrupt();
            }
        }
        else{
            while (true) {
                DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
                socket1.receive(packet1);
                String msg1 = new String(packet1.getData(), 0, packet1.getLength());
                List<String> qList = Arrays.asList(msg1.split(","));
                String status = qList.get(0);
                String taskId = qList.get(1);
                String processor = qList.get(2);
                if (status.equals("0")) {
                    String script = qList.get(3);
                    String file = qList.get(4);
                    Task task = new Task();
                    task.id = taskId;
                    task.script = script;
                    task.file = file;
                    if (processorData.containsKey(processor)) {
                        processorData.get(processor).add(task);
                    } else {
                        List<Task> tasks = new ArrayList<>();
                        tasks.add(task);
                        processorData.put(processor, tasks);
                    }
                } else {
                    List<Task> tasks = processorData.get(processor);
                    tasks.removeIf(t -> t.id.equals(taskId));
                }
                if(stopOrder)
                    Thread.currentThread().interrupt();
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
                        processorLoad.remove(k);
                        Thread resumeTasksThread = new Thread(new Runnable() {
                            public void run() {
                                try { resumeTasks(k); } catch (Exception ignored) {}}
                        });
                        resumeTasksThread.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Processor:\t"+k);
                System.out.println("Last HB:\t"+v);
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            });
            if(stopOrder)
                Thread.currentThread().interrupt();
        }
    }
    private static void resumeTasks(String processor) throws IOException, NotBoundException, InterruptedException {
        String keyWithLowestValue = null;
        int minValue = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : processorLoad.entrySet()) {
            int value = entry.getValue();
            if (value < minValue) {
                minValue = value;
                keyWithLowestValue = entry.getKey();
            }
        }
        pi = (ProcessorInterface) Naming.lookup(keyWithLowestValue);
        List<Task> tasks = processorData.get(processor);

        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("Sending "+tasks.size()+" tasks to "+keyWithLowestValue+" from "+processor);
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        for (Task task : tasks) {
            String file = task.file;
            String script = task.script;
            System.out.println("Sending Task "+task.id+" to "+keyWithLowestValue);
            pi.sendRequest(script, file);
        }
        Thread.currentThread().interrupt();
    }
}
