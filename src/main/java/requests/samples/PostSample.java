package requests.samples;

import static core.ApiCall.POST;

public class PostSample {

    public static void main(String[] args) {

        // put the body in block between 'three double-quote' marks
        String jsonBody = """
                {
                    "title": "foo",
                    "body": "bar",
                    "userId": 1
                }
                """;

        POST("https://jsonplaceholder.typicode.com/posts") // Required: Set your URL
                .header("Accept", "application/json") // Optional: Add any headers, copy/paste this line for another header
                .header("Content-Type", "application/json")
                .body(jsonBody) // Optional: Add request body
                .execute(); // Required: Execute the request
    }
}
