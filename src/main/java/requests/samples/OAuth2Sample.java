package requests.samples;

import java.util.HashMap;
import java.util.Map;

import static core.ApiCall.*;

public class OAuth2Sample {

    public static void main(String[] args) {
        // put required OAuth parameters here
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("client_id", "Abc1234567");
        params.put("client_secret", "ENDWzXfqenUbDd0zKVz");

        POST("https://httpbin.org/anything")
                .OAuth2(params)
                .execute();

        // In a real scenario, by this request you will obtain the token
        // Take the token from response and add it to Bearer authentication to the next request

        /*{
            "token_type": "Bearer",
            "expires_in": 86400,
            "access_token": "UHyJ5wQmYv4bLPZplsZ4_MM4ecxUXIYlT-6F1U5HkAT9M8ibuSRRPMHFXVLo0YaQeZ1yUUwg",
            "scope": "access",
            "refresh_token": "bZxT8mm0cVldk8QBvmeGW0Tw"
        }*/

    }
}
