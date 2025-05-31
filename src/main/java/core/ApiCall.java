package core;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ApiCall {

    private final String baseUrl;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private String requestBody;
    private AuthType authType = AuthType.NONE;
    private String authUsername;
    private String authPassword;
    private String authToken;
    private String apiKey;
    private String apiKeyHeaderName;
    List<NameValuePair> requestOauthParams = new ArrayList<>();
    HttpMethod method;
    private static final String LINE_SEPARATOR = System.lineSeparator();

    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }

    public enum AuthType {
        NONE, BASIC, BEARER, OAUTH2, API_KEY
    }

    public ApiCall(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ApiCall request(HttpMethod method) {
        this.method = method;
        return this;
    }

    public static CallDetails<?> GET(String url) {
        return new ApiCall(url).request(HttpMethod.GET).new CallDetails<>();
    }

    public static CallDetailsWithBody POST(String url) {
        return new ApiCall(url).request(HttpMethod.POST).new CallDetailsWithBody();
    }

    public static CallDetailsWithBody PUT(String url) {
        return new ApiCall(url).request(HttpMethod.PUT).new CallDetailsWithBody();
    }

    public static CallDetails<?> DELETE(String url) {
        return new ApiCall(url).request(HttpMethod.DELETE).new CallDetails<>();
    }

    public static CallDetailsWithBody PATCH(String url) {
        return new ApiCall(url).request(HttpMethod.PATCH).new CallDetailsWithBody();
    }

    public static CallDetails<?> HEAD(String url) {
        return new ApiCall(url).request(HttpMethod.HEAD).new CallDetails<>();
    }

    public static CallDetails<?> OPTIONS(String url) {
        return new ApiCall(url).request(HttpMethod.OPTIONS).new CallDetails<>();
    }

    @SuppressWarnings("unchecked")
    public class CallDetails<T extends CallDetails<T>> {

        public T header(String name, String value) {
            headers.put(name, value);
            return (T) this;
        }

        public T queryParam(String name, String value) {
            queryParams.put(name, value);
            return (T) this;
        }

        public T basicAuth(String username, String password) {
            authType = AuthType.BASIC;
            authUsername = username;
            authPassword = password;
            return (T) this;
        }

        public T bearerAuth(String token) {
            authType = AuthType.BEARER;
            authToken = token;
            return (T) this;
        }

        public T apiKeyAuth(String headerName, String key) {
            authType = AuthType.API_KEY;
            apiKeyHeaderName = headerName;
            apiKey = key;
            return (T) this;
        }

        public T OAuth2(Map<String, String> oauthParams) {

            authType = AuthType.OAUTH2;
            oauthParams.forEach((param, value) -> requestOauthParams.add(new BasicNameValuePair(param, value)));

            return (T) this;
        }

        public void execute() {
            ApiCall.this.execute();
        }

    }

    public class CallDetailsWithBody extends CallDetails<CallDetailsWithBody> {
        public CallDetailsWithBody body(String body) {
            requestBody = body;
            return this;
        }
    }

    private void execute() {

        try (CloseableHttpClient httpClient = createHttpClient()) {

            URI uri = buildUri();
            ClassicHttpRequest request = createRequest(method, uri);

            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }

            addAuthHeaders(request);

            if (requestBody != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {

                String contentType;
                Optional<Map.Entry<String, String>> mayBeContentType = headers.entrySet().stream()
                        .filter(e -> e.getKey().equalsIgnoreCase("Content-Type"))
                        .findAny();

                contentType = mayBeContentType.isPresent() ? mayBeContentType.get().getValue() : "text/plain";

                StringEntity entity = new StringEntity(requestBody, ContentType.create(contentType));
                request.setEntity(entity);
            }

            httpClient.execute(request, resp -> {
                System.out.println();
                Response theResponse = new Response(resp);
                System.out.format("=== Executing %s %s ===", request.getMethod(), request.getRequestUri()).println(LINE_SEPARATOR);
                System.out.println(theResponse);
                System.out.println();
                System.out.println("============================");
                return null;
            });

        } catch (IOException | URISyntaxException e) {
            System.err.println("Error executing request: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private URI buildUri() throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl);

        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            uriBuilder.addParameter(param.getKey(), param.getValue());
        }
        return uriBuilder.build();
    }

    private CloseableHttpClient createHttpClient() {
        if (authType == AuthType.BASIC) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(null, -1),
                    new UsernamePasswordCredentials(authUsername, authPassword.toCharArray()));

            return HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .build();
        } else {
            return HttpClients.createDefault();
        }
    }

    private ClassicHttpRequest createRequest(HttpMethod method, URI uri) {
        return switch (method) {
            case GET -> new HttpGet(uri);
            case POST -> new HttpPost(uri);
            case PUT -> new HttpPut(uri);
            case DELETE -> new HttpDelete(uri);
            case PATCH -> new HttpPatch(uri);
            case HEAD -> new HttpHead(uri);
            case OPTIONS -> new HttpOptions(uri);
        };
    }

    private void addAuthHeaders(ClassicHttpRequest request) {
        switch (authType) {
            case BEARER -> request.addHeader("Authorization", "Bearer " + authToken);
            case API_KEY -> request.addHeader(apiKeyHeaderName, apiKey);
            case OAUTH2 -> {
                request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                request.setEntity(new UrlEncodedFormEntity(requestOauthParams));
            }
            default -> {}
        }
    }

    public static class Response {
        private final int statusCode;
        private final Map<String, String> headers = new HashMap<>();
        private final String body;

        public Response(ClassicHttpResponse response) throws IOException {
            this.statusCode = response.getCode();

            for (Header header : response.getHeaders()) {
                headers.put(header.getName(), header.getValue());
            }

            HttpEntity entity = response.getEntity();
            try {
                this.body = entity != null ? EntityUtils.toString(entity) : null;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public int getStatusCode() {
            return statusCode;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("Response Code: ").append(statusCode).append(LINE_SEPARATOR).append(LINE_SEPARATOR);

            sb.append("Response Headers:").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                sb.append(header.getKey()).append(": ").append(header.getValue()).append(LINE_SEPARATOR);
            }

            sb.append(LINE_SEPARATOR).append("Response Body:").append(LINE_SEPARATOR).append(LINE_SEPARATOR).append(body);

            return sb.toString();
        }
    }


}
