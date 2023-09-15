package run.rekt.burpvotd;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

/**
 * The main extension class for the "Verse of the Day" BurpSuite extension.
 *
 * @author Gabe Rust
 */
@SuppressWarnings("unused")
public class VOTDExtension implements BurpExtension {

    /**
     * Initializes the extension with provided API.
     * This method sets the extension name, registers the main panel, and handles extension unloading events.
     *
     * @param api The MontoyaApi instance that provides access to various burp functionalities.
     */
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Verse of the Day");

        VOTDPanel panel = new VOTDPanel(api);
        api.userInterface().registerSuiteTab("Verse of the Day", panel);

        // Stop automatic update timer to remove reference from Event Dispatch Loop so that extension can be
        // fully and cleanly unloaded
        api.extension().registerUnloadingHandler(panel::stopTimer);
    }

}
