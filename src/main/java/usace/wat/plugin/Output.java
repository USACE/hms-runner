package usace.wat.plugin;
import com.fasterxml.jackson.annotation.JsonProperty;
public class Output {
    @JsonProperty
    private String name;
    @JsonProperty
    private String parameter;
    @JsonProperty
    private String format;
    public String getName() {
        return name;
    }
    public String getParameter() {
        return parameter;
    }
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
    public void setName(String name) {
        this.name = name;
    }
}
