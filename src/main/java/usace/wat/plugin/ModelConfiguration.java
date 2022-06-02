package usace.wat.plugin;
import com.fasterxml.jackson.annotation.JsonProperty;
public class ModelConfiguration {
    @JsonProperty
    private String model_name;
    @JsonProperty
    private ResourceInfo[] model_configuration_paths;
    public ResourceInfo[] ModelFilePaths(){
        return model_configuration_paths;
    }
    public String ModelName(){
        return model_name;
    }
}
