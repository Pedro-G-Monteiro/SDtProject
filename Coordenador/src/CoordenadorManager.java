import java.io.IOException;
import java.net.*;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoordenadorManager extends UnicastRemoteObject implements CoordenadorInterface{

    public boolean stopOrder = false;
    public ProcessorInterface pi;
    public BalancerInterface bi;
    public ConcurrentHashMap<String, Integer> processorState = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Integer> processorLoad = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, List<Task>> processorData = new ConcurrentHashMap<>();
    public boolean hasSentProcessorInfo = false;
    public boolean connectedToBalancer = false;
    public final int coordPort = 2100;

    protected CoordenadorManager() throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {
        startHBReceiver();
        startTaskQueueReceiver();
        startProcessorKiller();
        startHBSender();
        connectToBalancer();
    }
    private void sendHB(int port) throws IOException, InterruptedException {
        while(true){
            String mensagem = "rmi://localhost:"+coordPort+"/Coordenador";
            //System.out.println("Sending MC: "+mensagem);
            sendMulticast(port, mensagem);
            if (hasSentProcessorInfo)
                Thread.sleep(5000);
            else
                Thread.sleep(1000);
        }
    }
    private void startHBSender(){
        Thread HBThread = new Thread(new Runnable() {
            public void run() {
                try { sendHB(4449); } catch (Exception ignored) {}}
        });
        HBThread.start();
    }
    private void HBReceiver(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[25600];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);
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
                if(connectedToBalancer)
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
    private void QueueReceiver(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[25600];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);
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

    private void processorKiller() throws InterruptedException{
        while(true){
            Thread.sleep(1000);
            processorState.forEach((k, v) ->{
                v = v + 1;
                processorState.replace(k, v);
                if(v>30 && connectedToBalancer){
                    processorState.remove(k);
                    System.out.println("Removed Processor "+k);
                    try {
                        bi.removeProcessor(k);
                        System.out.println("Removing Processor "+k+" from Balancer");
                        processorLoad.remove(k);
                        if(processorData.get(k) != null){
                            Thread resumeTasksThread = new Thread(new Runnable() {
                                public void run() {
                                    try { resumeTasks(k); } catch (Exception ignored) {}}
                            });
                            resumeTasksThread.start();
                        }
                        else
                            processorData.remove(k);
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
    private void resumeTasks(String processor) throws IOException, NotBoundException, InterruptedException {
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
    public void startHBReceiver() throws RemoteException{
        Thread multicastThread = new Thread(new Runnable() {
            public void run() {
                try { HBReceiver(4447); } catch (Exception ignored) {}}
        });
        multicastThread.start();
    }
    public void startTaskQueueReceiver() throws RemoteException{
        Thread multicastThread = new Thread(new Runnable() {
            public void run() {
                try {QueueReceiver(4448);} catch (Exception ignored) {}}
        });
        multicastThread.start();
    }
    public void startProcessorKiller() throws RemoteException{
        Thread killerThread = new Thread(new Runnable() {
            public void run() {
                try { processorKiller(); } catch (Exception ignored) {}}
        });
        killerThread.start();
    }
    public void connectToBalancer() {
        Thread balancerConnector = new Thread(new Runnable() {
            public void run() {
                while (!connectedToBalancer) {
                    try {
                        bi = (BalancerInterface) Naming.lookup("rmi://localhost:2001/Balancer");
                        connectedToBalancer = true;
                        System.out.println("Connected to Balancer!");
                    } catch (ConnectException | MalformedURLException e) {
                        //System.out.println("Error connecting to Balancer: " + e.getMessage());
                    } catch (NotBoundException e) {
                        //System.out.println("Balancer not found: " + e.getMessage());
                    } catch (RemoteException e) {
                        //System.out.println("Remote error: " + e.getMessage());
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread sleep interrupted: " + e.getMessage());
                    }
                }
            }
        });
        balancerConnector.start();
    }
    public void sendMulticast(int port, String msg) throws IOException{
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
        socket.close();
    }
    public ConcurrentHashMap<String, String> getProcessors() throws RemoteException{
        ConcurrentHashMap<String, String> aux = new ConcurrentHashMap<>();
        processorLoad.forEach((k, v) -> {
            aux.put(k, Integer.toString(v));
        });
        hasSentProcessorInfo = true;
        return aux;
    }
}

