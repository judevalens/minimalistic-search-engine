package org.quisqueya.macaya.spider.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueJob {
    @JsonProperty("hostname")
    public String hostname;
    public String url;
    @JsonProperty("normalized_url")
    public String normalizedUrl;
    public double score;

    public QueueJob(String hostname,String url, String normalizedUrl, double score) {
        this.hostname = hostname;
        this.url = url;
        this.normalizedUrl = normalizedUrl;
        this.score = score;
    }
}
