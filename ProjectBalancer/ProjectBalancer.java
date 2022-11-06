import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ProjectBalancer {

    public static List<String> processorsIP = new ArrayList<String>();

    static {
        processorsIP.add("rmi://localhost:2003/processor1");
        processorsIP.add("rmi://localhost:2004/processor2");
        processorsIP.add("rmi://localhost:2005/processor3");
    }
}