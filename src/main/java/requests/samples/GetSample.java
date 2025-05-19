package requests.samples;

import static core.ApiCall.GET;

public class GetSample {

    public static void main(String[] args) {
        GET("https://jsonplaceholder.typicode.com/posts") // Required: Set your URL
                .header("Accept", "application/json") // Optional: Add any headers, copy/paste this line for another header
                .queryParam("userId", "1") // Optional: Add query parameters
                // Optional: Choose ONE authentication method if needed
                // basicAuth("username", "password");
                // bearerAuth("your-token");
                // apiKeyAuth("your-api-key", "X-API-Key");
                .execute(); // Required: Execute the request
    }
}
