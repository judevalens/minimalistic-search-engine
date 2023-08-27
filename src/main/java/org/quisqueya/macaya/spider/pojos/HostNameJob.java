package org.quisqueya.macaya.spider.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HostNameJob {
    public String hostname;
    public long lastTS;
    @JsonProperty("pages_list")
    public String pagesList;

    public HostNameJob(String hostname, long lastTS, String crawListKey) {
        this.hostname = hostname;
        this.lastTS = lastTS;
        this.pagesList = crawListKey;
    }
}
