package requests.templates;

import static core.ApiCall.GET;

public class GetTemplate {


    public static void main(String[] args) {

        GET("")
                .header("", "")
                .queryParam("", "")
                .execute();
    }
}
