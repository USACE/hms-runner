package usace.wat.plugin;
import com.fasterxml.jackson.annotation.JsonProperty;
public class EventConfiguration {
    @JsonProperty
    private String output_destination;
    @JsonProperty
    private int realization_number;
    @JsonProperty
    private int event_number;
    @JsonProperty
    private TimeWindow time_window;
    @JsonProperty
    private int event_seed;
    @JsonProperty
    private int realization_seed;
}
