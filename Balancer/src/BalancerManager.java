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

public class BalancerManager extends UnicastRemoteObject implements BalancerInterface{

    static HashMap<String, String> processorState = new HashMap<>();
    protected BalancerManager() throws RemoteException {
        Thread multicastThread = new Thread(new Runnable() {
            public void run() {
                try { MulticastReceiver(4446); } catch (Exception ignored) {}}
        });
        multicastThread.start();
    }
    static int counter = 0;

    public ArrayList<String> SendRequest(String script, String IDFile) throws RemoteException{
        ProcessorInterface pi1;
        ProcessorInterface pi2;
        ArrayList<String> result = new ArrayList<>();
        try {
            pi1 = (ProcessorInterface) Naming.lookup("rmi://localhost:2002/Processor");
            pi2 = (ProcessorInterface) Naming.lookup("rmi://localhost:2003/Processor");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        counter ++;
        if(counter%2 == 0){
            pi1.sendRequest(script, IDFile);
            result.add(Integer.toString(counter));
            result.add(String.valueOf(counter%2+1));
            return result;
        }
        else{
            pi2.sendRequest(script, IDFile);
            result.add(Integer.toString(counter));
            result.add(String.valueOf(counter%2+1));
            return result;
        }
    }
    private void MulticastReceiver(int port) throws IOException {
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
            String processor = qList.get(0);
            String queue = qList.get(1);
            if(processorState.containsKey(processor)){
                processorState.replace(processor, queue);
            }
            else
                processorState.putIfAbsent(processor, queue);
            System.out.println("Processor:\t"+processor);
            System.out.println("Queue:\t\t"+queue);
            System.out.println("------------------------------------------------------");
        }
    }
    public HashMap<String, String> getProcessStates() throws RemoteException{
        return processorState;
    }
}