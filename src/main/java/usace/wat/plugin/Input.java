package usace.wat.plugin;
public class Input {
    @JsonProperty
    private String name;
    @JsonProperty
    private String parameter;
    @JsonProperty
    private String format;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
