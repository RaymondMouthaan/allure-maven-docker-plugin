package org.mouthaan.allure.maven.docker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "file_name",
        "content_base64"
})
public class AllureResult {
    @JsonProperty("file_name")
    public String fileName;
    @JsonProperty("content_base64")
    public String contentBase64;

    public AllureResult(String fileName, String contentBase64) {
        this.fileName = fileName;
        this.contentBase64 = contentBase64;
    }
}
