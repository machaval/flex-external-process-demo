package github.machaval.transformation.utils;

import io.envoyproxy.envoy.config.core.v3.HeaderMap;
import io.envoyproxy.envoy.config.core.v3.HeaderValue;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class Utils {

    public static String PATH_HEADER_NAME = ":path";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String CONTENT_LENGTH_HEADER_NAME = "content-length";


    private static final Metadata.Key<String> EXCEPTION_TYPE_KEY = Metadata.Key.of("exception-type", Metadata.ASCII_STRING_MARSHALLER);

    public static HashMap<String, String> headersAsMap(HeaderMap headers) {
        //TODO replace with a multimap
        HashMap<String, String> headersMap = new HashMap<>();
        List<HeaderValue> headersList = headers.getHeadersList();
        for (HeaderValue headerValue : headersList) {
            String strValue = getHeaderValue(headerValue);
            headersMap.put(headerValue.getKey(), strValue);
        }
        return headersMap;
    }

    public static String getHeaderValue(HeaderValue headerValue) {
        String value = headerValue.getValue();
        return value.isEmpty() ? headerValue.getRawValue().toString(StandardCharsets.UTF_8) : value;
    }

    public static StatusRuntimeException wrapException(Throwable t) {
        var trailers = new Metadata();
        trailers.put(EXCEPTION_TYPE_KEY, t.getClass().getCanonicalName());
        return new StatusRuntimeException(Status.INTERNAL.withCause(t).withDescription(t.getMessage()), trailers);
    }

    public static StatusRuntimeException newException(String t) {
        var trailers = new Metadata();
        return new StatusRuntimeException(Status.INTERNAL.withDescription(t), trailers);
    }


}

