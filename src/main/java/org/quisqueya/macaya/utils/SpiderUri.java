package org.quisqueya.macaya.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.HttpUrl;
import org.apache.commons.codec.net.PercentCodec;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.hadoop.shaded.org.apache.http.NameValuePair;
import org.apache.hadoop.shaded.org.apache.http.client.utils.URLEncodedUtils;
import org.glassfish.jersey.server.Uri;

import javax.management.Query;

public class SpiderUri {

    final static String DEFAULT_SCHEME = "http";
    private Path path;


    public static URI normalize(URI uri) {
        var normalizedUri = uri.normalize();
        var path = Paths.get(normalizedUri.getPath());
        path.iterator().forEachRemaining((currentPath) -> {
        });
        return normalizedUri;
    }

    public static URI spiderNormalize(URI uri) {
        var normalizedUri = uri.normalize();
        var path = Paths.get(normalizedUri.getPath());

        /*
        Removing directory index. Default directory indexes are generally not needed in URIs. Examples:
        http://example.com/a/index.html → http://example.com/a/
        http://example.com/default.asp → http://example.com/
         */
        if (path.getNameCount() == 1) {
            var baseIndex = FileNameUtils.getBaseName(path.toString());
            if (baseIndex.equals("index") || baseIndex.equals("default")) {
                path = Paths.get("/");
            }
        }
        try {
            normalizedUri = new URI(DEFAULT_SCHEME, uri.getHost(), path.toString(), sortQuery(normalizedUri), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return normalizedUri;
    }

    private static String sortQuery(URI uri) {
        HttpUrl url = HttpUrl.get(uri);
        if (url == null) {
            return null;
        }
        HttpUrl.Builder queryBuilder = url.newBuilder();

        var paramKeys = url.queryParameterNames().stream().sorted().collect(Collectors.toList());
        if (paramKeys.size() == 0 || (paramKeys.size() == 1 && paramKeys.get(0).isEmpty())) {
            return null;
        }
        paramKeys.forEach((key) -> {
            queryBuilder.removeAllQueryParameters(key);
            queryBuilder.addQueryParameter(key, url.queryParameter(key));
        });
        return queryBuilder.build().query();
    }

}
