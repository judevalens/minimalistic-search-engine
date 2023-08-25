package org.quisqueya.macaya.spider.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONPropertyName;

public class QueueJob {
    public String url;
    @JsonProperty("normalized_url")
    public String normalizedUrl;
    public double score;

    public QueueJob(String url, String normalizedUrl, double score) {
        this.url = url;
        this.normalizedUrl = normalizedUrl;
        this.score = score;
    }
}
