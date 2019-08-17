package org.mouthaan.allure.maven.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.stream.Stream;

/**
 *
 */
@Mojo(name = "publish", defaultPhase = LifecyclePhase.SITE)
public class AllureDockerPublishMojo extends AbstractMojo {

    /**
     *
     */
    @Parameter(property = "dockerUrl", required = true, readonly = true)
    private String dockerUrl;

    @Parameter(property = "allureResultsDirectory", required = true, readonly = true)
    private String allureResultsDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(dockerUrl);
        getLog().info(allureResultsDirectory);

        AllureResults allureResults = new AllureResults();

        try (Stream<Path> stream = Files.list(Paths.get(allureResultsDirectory)).filter(Files::isRegularFile)) {
            stream.forEach(p -> {
                try {
                    allureResults.results.add(
                            new AllureResult(p.getFileName().toString(), encodeFileBase64(p.toFile()))
                    );
                } catch (IOException e) {
                    getLog().error(e.getMessage());
                }
            });
        } catch (IOException e) {
            getLog().error(e.getMessage());
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final String content = objectMapper.writeValueAsString(allureResults);
            RestClient(content);

        } catch (JsonProcessingException e) {
            getLog().error(e.getMessage());
        }
    }

    private String encodeFileBase64(File file) throws IOException {
        return new String(
                Base64.getEncoder().encode(
                        FileUtils.readFileToByteArray(file)
                )
        );
    }

    private void RestClient(String content) {
        try {

            URL url = new URL(dockerUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(content.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            getLog().info("Output from Server ....");
            while ((output = br.readLine()) != null) {
                getLog().info(output);
            }

            conn.disconnect();

        } catch (IOException e) {
            getLog().error(e.toString());
        }
    }
}
