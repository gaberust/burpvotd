package run.rekt.burpvotd;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class VOTDExtension implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Verse of the Day");
        VOTDPanel panel = new VOTDPanel(api);
        api.userInterface().registerSuiteTab("Verse of the Day", panel);
        api.extension().registerUnloadingHandler(panel::stopTimer);
    }

}
