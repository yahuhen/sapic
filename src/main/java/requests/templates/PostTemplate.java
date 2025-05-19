package requests.templates;

import static core.ApiCall.POST;

public class PostTemplate {

    public static void main(String[] args) {

        // put the body in block between 'three double-quote' marks
        String jsonBody = """
                
                """;


        POST("")
                .header("", "")
                .body(jsonBody)
                .execute();
    }
}
