import java.io.Serializable;
import java.time.LocalDateTime;

public class Book implements Serializable {

    public final String name;
    public boolean isAvailable;
    public LocalDateTime rentedLastTime;
    public String lastRenter;

    public Book(String name, boolean isAvailable, LocalDateTime rentedLastTime, String lastRenter) {
        this.name = name;
        this.isAvailable = isAvailable;
        this.rentedLastTime = rentedLastTime;
        this.lastRenter = lastRenter;
    }

    @Override
    public String toString() {
        return "Book=" + name;
    }
}
