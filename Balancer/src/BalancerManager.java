import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.parseInt;

public class BalancerManager extends UnicastRemoteObject implements BalancerInterface{

    static ConcurrentHashMap<String, String> processorState = new ConcurrentHashMap<>();
    static boolean stopOrder = false;
    public Lock lk = new Lock();

    protected BalancerManager() throws RemoteException {
        Thread multicastThread = new Thread(new Runnable() {
            public void run() {
                try { MulticastReceiver(4446); } catch (Exception ignored) {}}
        });
        multicastThread.start();
    }
    static int counter = 0;

    public ArrayList<String> SendRequest(String script, String IDFile) throws IOException, InterruptedException {
        ProcessorInterface pi1;
        ProcessorInterface pi2;
        ArrayList<String> result = new ArrayList<>();
        try {
            pi1 = (ProcessorInterface) Naming.lookup("rmi://localhost:2002/Processor");
            pi2 = (ProcessorInterface) Naming.lookup("rmi://localhost:2003/Processor");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        lk.lock();
        int load1 = parseInt(processorState.get("rmi://localhost:2002/Processor"));
        int load2 = parseInt(processorState.get("rmi://localhost:2003/Processor"));
        counter++;
        lk.unlock();
        if(load1<load2){
            pi1.sendRequest(script, IDFile);
            result.add(Integer.toString(counter));
            result.add("1");
            return result;
        }
        else{
            pi2.sendRequest(script, IDFile);
            result.add(Integer.toString(counter));
            result.add("2");
            return result;
        }
    }
    private void MulticastReceiver(int port) throws IOException, InterruptedException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg1 = new String(packet1.getData(), 0, packet1.getLength()); //mensagem recebida
            List<String> qList = Arrays.asList(msg1.split(","));
            String type = qList.get(0);
            String processor = qList.get(1);
            String queue = qList.get(2);
            lk.lock();
            if(type.equals("update")){
                if(processorState.containsKey(processor)){
                    processorState.replace(processor, queue);
                }
                System.out.println("Processor:\t"+processor);
                System.out.println("Queue:\t\t"+queue);
                System.out.println("------------------------------------------------------");
            }
            lk.unlock();
            if(stopOrder)
                Thread.currentThread().interrupt();
        }
    }
    public ConcurrentHashMap<String, String> getProcessStates() throws RemoteException{
        return processorState;
    }
    public void addProcessor(String processor, String queue) throws RemoteException, InterruptedException {
        lk.lock();
        processorState.putIfAbsent(processor, queue);
        lk.unlock();
    }
    public void removeProcessor(String processor) throws RemoteException, InterruptedException {
        lk.lock();
        processorState.remove(processor);
        System.out.println("Removed processor "+processor);
        lk.unlock();
    }
}