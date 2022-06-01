package usace.wat.plugin;
import com.fasterxml.jackson.annotation.JsonProperty;
public class ModelConfiguration {
    @JsonProperty
    private String model_name;
    @JsonProperty
    private String model_configuration_path;
    public String ModelFilePath(){
        return model_configuration_path;
    }
    public String ModelName(){
        return model_name;
    }
}
