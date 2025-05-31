package requests.templates;

import static core.ApiCall.*;

public class OptionsTemplate {

    public static void main(String[] args) {

        OPTIONS("")
                .header("", "")
                .queryParam("", "")
                .execute();
    }
}
