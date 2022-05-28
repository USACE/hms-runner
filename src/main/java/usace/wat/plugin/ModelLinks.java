package usace.wat.plugin;

public class ModelLinks {
    @JsonProperty
    private LinkedInput[] linked_inputs;
    @JsonProperty
    private Output[] required_outputs;
    public LinkedInput[] getLinked_inputs() {
        return linked_inputs;
    }
    public Output[] getRequired_outputs() {
        return required_outputs;
    }
    public void setRequired_outputs(Output[] required_outputs) {
        this.required_outputs = required_outputs;
    }
    public void setLinked_inputs(LinkedInput[] linked_inputs) {
        this.linked_inputs = linked_inputs;
    }
}
