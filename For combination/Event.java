import java.io.Serializable;
import java.time.LocalDateTime;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;

    public Event(LocalDateTime startTime, String description) {
        this.startTime = startTime;
        this.endTime = null;
        this.description = description;
    }

    public Event(LocalDateTime startTime, LocalDateTime endTime, String description) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }
}
