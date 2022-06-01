package usace.wat.plugin;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;

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
        try {
            return mapper.readValue(file, ModelPayload.class);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ModelPayload();
    }
    public String ModelFilePath(){
        return model_configuration.ModelFilePath();
    }
    public String ModelName(){
        return model_configuration.ModelName();
    }
}

