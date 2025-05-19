package core;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
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
    private Map<String, String> requestOauthParams = new HashMap<>();
    HttpMethod method;

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

        public T apiKeyAuth(String key, String headerName) {
            authType = AuthType.API_KEY;
            apiKey = key;
            apiKeyHeaderName = headerName;
            return (T) this;
        }

        public T OAuth2(Map<String, String> oauthParams) {
            authType = AuthType.OAUTH2;
            requestOauthParams = oauthParams;
            return (T) this;
        }

        public void execute() {
            ApiCall.this.execute();
        }

        public Response executeOAuth2TokenRequest() {
            return ApiCall.this.executeOAuth2TokenRequest();
        }
    }

    public class CallDetailsWithBody extends CallDetails<CallDetailsWithBody> {
        public CallDetailsWithBody body(String body) {
            requestBody = body;
            return this;
        }
    }

    public void execute() {

        try (CloseableHttpClient httpClient = createHttpClient()) {

            URI uri = buildUri();
            ClassicHttpRequest request = createRequest(method, uri);

            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }

            addAuthHeaders(request);

            // Add body if applicable
            if (requestBody != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {

                String contentType;
                Optional<Map.Entry<String, String>> mayBeContentType = headers.entrySet().stream()
                        .filter(e -> e.getKey().equalsIgnoreCase("Content-Type"))
                        .findAny();

                contentType = mayBeContentType.isPresent() ? mayBeContentType.get().getValue() : "text/plain";

                StringEntity entity = new StringEntity(requestBody, ContentType.create(contentType));
                request.setEntity(entity);
            }

            Response response = httpClient.execute(request, resp -> {
                Response theResponse = new Response(resp);
                System.out.println("=== GET Example Response ===");
                System.out.println(theResponse);
                System.out.println("============================\n");
                return theResponse;
            });

            // Optional: Check if response was successful (status code 2xx)
            if (response.isSuccess()) {
                System.out.println("Request was successful!");
            } else {
                System.out.println("Request failed!");
            }

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
        // Create the appropriate client based on auth type
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
            case BEARER:
                request.addHeader("Authorization", "Bearer " + authToken);
                break;
            case API_KEY:
                request.addHeader(apiKeyHeaderName, apiKey);
                break;
            case OAUTH2:
                // This would typically involve getting a token first
                if (requestOauthParams.containsKey("access_token")) {
                    request.addHeader("Authorization", "Bearer " + requestOauthParams.get("access_token"));
                }
                break;
            default:
                // No additional headers needed
                break;
        }
    }

    public Response executeOAuth2TokenRequest() {
        if (authType != AuthType.OAUTH2 || !requestOauthParams.containsKey("token_url")) {
            throw new IllegalStateException("OAuth2 not properly configured");
        }

        HttpPost tokenRequest = new HttpPost(requestOauthParams.get("token_url"));
        tokenRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");

        List<NameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("grant_type", requestOauthParams.getOrDefault("grant_type", "client_credentials")));

        if (requestOauthParams.containsKey("client_id")) {
            form.add(new BasicNameValuePair("client_id", requestOauthParams.get("client_id")));
        }

        if (requestOauthParams.containsKey("client_secret")) {
            form.add(new BasicNameValuePair("client_secret", requestOauthParams.get("client_secret")));
        }

        if (requestOauthParams.containsKey("username") && requestOauthParams.containsKey("password")) {
            form.add(new BasicNameValuePair("username", requestOauthParams.get("username")));
            form.add(new BasicNameValuePair("password", requestOauthParams.get("password")));
        }

        if (requestOauthParams.containsKey("scope")) {
            form.add(new BasicNameValuePair("scope", requestOauthParams.get("scope")));
        }

        tokenRequest.setEntity(new UrlEncodedFormEntity(form));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(tokenRequest)) {

            return new Response(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            sb.append("Status Code: ").append(statusCode).append("\n\n");

            sb.append("Headers:\n");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                sb.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
            }

            sb.append("\nBody:\n").append(body);

            return sb.toString();
        }
    }


}
