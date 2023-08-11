package usace.cc.plugin.hmsrunner;

import usace.cc.plugin.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;

import java.net.URI;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class pushOutputsAction {
    private Action action;
    private Payload mp;
    private PluginManager pm;
    private String modelOutputDestination;
    private String jobID;

    public pushOutputsAction(Action a, Payload mp, PluginManager pm, String modelOutputDestination, String jobID) {
        this.action = a;
        this.mp = mp;
        this.pm = pm;
        this.modelOutputDestination = modelOutputDestination;
        this.jobID = jobID;
    }
    public void computeAction(){
        String outputPaths = "";
        for (DataSource output : mp.getOutputs()) {
            Path path = Paths.get(modelOutputDestination + output.getName());
            outputPaths += path + ","; // this will leave an extra comma at the end
            byte[] data;
            try {
                data = Files.readAllBytes(path);
                pm.putFile(data, output,0);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        if(outputPaths.length() > 0) { //ensure there were outputs
            write_paths_to_s3(outputPaths.substring(0, outputPaths.length()-1)); // truncate the extra comma at the end
        }
    }

    /**
     *  Converts comma-separated paths to an array to put into a json for s3
     */
    private void write_paths_to_s3(String paths) {
        String bucketName = System.getenv("S3_BUCKET");
        String key = bucketName + "/" + jobID + ".json";
        String[] pathsArray = paths.split(",");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String[]> output = new HashMap<String, String[]>();
        output.put("message", pathsArray);

        String jsonContent = "";
        try {
            jsonContent = objectMapper.writeValueAsString(output);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
        }

        S3Client s3 = S3Client.builder()
            .endpointOverride(URI.create("http://host.docker.internal:9000")) // remove when using aws, no endpoint needed
            .region(Region.of("us-east-1"))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("user", "password"))) //need proper creds for aws
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true) // this line forces path-style access, remove when using aws
                .build())
            .build();
        PutObjectResponse response = s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(),
            RequestBody.fromString(jsonContent)
        );

        System.out.println("Object uploaded with ETag: " + response.eTag());
    }
}
