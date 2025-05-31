package requests.templates;

import static core.ApiCall.*;

public class HeadTemplate {

    public static void main(String[] args) {

        HEAD("")
                .header("", "")
                .queryParam("", "")
                .execute();
    }
}
