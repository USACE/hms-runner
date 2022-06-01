package usace.wat.plugin;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
public class TimeWindow {
    @JsonProperty
    private LocalDateTime starttime;
    @JsonProperty
    private LocalDateTime endtime;
}
