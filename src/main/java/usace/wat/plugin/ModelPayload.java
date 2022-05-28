package usace.wat.plugin;
import java.io.File;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ModelPayload {
    @JsonProperty
    private String target_plugin;
    @JsonProperty
    private ModelConfiguration model_configuration;
    @JsonProperty
    private ModelLinks[] model_links;
    @JsonProperty
    private EventConfiguration event_config;
    public static ModelPayload readYaml(final File file) {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        return mapper.readValue(file, ModelPayload.class);
    }
    public String ModelFilePath(){
        return model_configuration.ModelFilePath();
    }
    public String ModelName(){
        return model_configuration.ModelName();
    }
}

