import java.util.UUID;

public class Processor {
    private UUID ID;
    private int Port;

    public Processor(UUID _ID, int _port)
    {
        this.ID = _ID;
        this.Port = _port;
    }

    public UUID getIdentificador()
    {
        return this.ID;
    }
    public int getPort() {
        return Port;
    }
}