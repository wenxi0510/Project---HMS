import java.io.Serializable;
import java.time.LocalTime;

public class TimeRange implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalTime start;
    private LocalTime end;
    private String eventName;

    public TimeRange(LocalTime start, LocalTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        this.start = start;
        this.end = end;
        this.eventName = null; // Default to no event name
    }
    
    public TimeRange(LocalTime start, LocalTime end, String eventName) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        this.start = start;
        this.end = end;
        this.eventName = eventName; // Allow optional event name
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    // Check if this range contains another range
    public boolean contains(TimeRange other) {
        return !this.start.isAfter(other.start) && !this.end.isBefore(other.end);
    }

    // Check if this range overlaps with another range
    public boolean overlaps(TimeRange other) {
        return !this.end.isBefore(other.start) && !this.start.isAfter(other.end);
    }

    @Override
    public String toString() {
        return start + " - " + end + (eventName != null ? " (" + eventName + ")" : "");
    }
}
