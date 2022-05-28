package usace.wat.plugin;
import java.time.LocalDateTime;

public class TimeWindow {
    @JsonProperty
    private LocalDateTime starttime;
    @JsonProperty
    private LocalDateTime endtime;
}
