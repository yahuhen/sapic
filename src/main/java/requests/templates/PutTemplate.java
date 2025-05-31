package requests.templates;

import static core.ApiCall.*;

public class PutTemplate {

    public static void main(String[] args) {

        // put the body in block between 'three double-quote' marks
        String jsonBody = """
                
                """;


        PUT("")
                .header("", "")
                .body(jsonBody)
                .execute();
    }
}
