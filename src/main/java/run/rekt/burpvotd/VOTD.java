package run.rekt.burpvotd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.google.gson.*;

/**
 * Represents a Verse Of The Day (VOTD) utility for fetching the verse from BibleGateway.
 *
 * @author Gabe Rust
 */
public class VOTD {

    /**
     * Default verse displayed in case of any errors or issues.
     */
    public static final String DEFAULT_VOTD = "<h1>Something isn't working right now. Here, have a cookiecutter "
            + "John 3:16 from the superior translation, ESV:</h1>For God so loved the world, that he gave his only "
            + "Son, that whoever believes in him should not perish but have eternal life.";

    private final MontoyaApi api;

    /**
     * Constructor to initialize with the MontoyaApi instance.
     *
     * @param api The MontoyaApi instance to be used for HTTP requests.
     */
    public VOTD(MontoyaApi api) {
        this.api = api;
    }

    /**
     * Wraps the provided text in a global styled HTML format for display.
     *
     * @param text The raw text to be wrapped.
     * @return The styled HTML wrapped text.
     */
    private String wrap(String text) {
        return "<html><div><div style=\"font-size:1.5em;border: 3px solid black;padding:20px;\">"
                + text
                + "</div><p style=\"padding-left:5px\">Powered by <a href=\"https://biblegateway.com\">"
                + "BibleGateway.com</a></p></div></html>";
    }

    /**
     * Fetches the Verse Of The Day (VOTD) for the given translation from BibleGateway.
     *
     * @param translation The Bible translation for which the VOTD needs to be fetched.
     * @return The Verse Of The Day wrapped in styled HTML.
     */
    public String getVOTD(String translation) {
        try {
            String version = URLEncoder.encode(translation.strip().toUpperCase(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.httpRequestFromUrl(
                    "https://www.biblegateway.com/votd/get/?format=json&version=" + version
            );
            HttpRequestResponse rr = api.http().sendRequest(request);

            JsonObject obj = JsonParser.parseString(rr.response().bodyToString()).getAsJsonObject();
            if (obj.has("error")) {
                return wrap("<h1>Invalid translation. Working examples include ESV, NIV, KJV, and NKJV.</h1>");
            } else if (obj.has("votd")) {
                JsonObject votd = obj.getAsJsonObject("votd");
                return wrap("<h1>" + votd.get("display_ref").getAsString()
                        + "</h1>"
                        + votd.get("content").getAsString().replaceAll("h3>", "h2>"));
            }
            throw new Exception();
        } catch (Exception e) {
            return wrap(DEFAULT_VOTD);
        }
    }

    /**
     * Returns the default Verse Of The Day.
     *
     * @return The default VOTD wrapped in styled HTML.
     */
    public String getDefaultVotd() {
        return wrap(DEFAULT_VOTD);
    }

}
