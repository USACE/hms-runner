package usace.wat.plugin;

public class LinkedInput {
    @JsonProperty
    private Input input;
    @JsonProperty
    private Output source;
    public Input getInput() {
        return input;
    }
    public void setInput(Input input) {
        this.input = input;
    }
}
