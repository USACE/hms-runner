package usace.wat.plugin;
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
