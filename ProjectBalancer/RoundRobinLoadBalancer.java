import java.util.Random;

public class RoundRobinLoadBalancer extends ProjectBalancer implements BalancerReqInterface{

    public String getProcessor(String IDClient) {
        Random random = new Random();
        return processorsIP.get(random.nextInt(processorsIP.size()));
    }
}