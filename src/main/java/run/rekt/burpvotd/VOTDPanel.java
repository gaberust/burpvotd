package run.rekt.burpvotd;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

import burp.api.montoya.MontoyaApi;

/**
 * Represents the main UI panel for the "Verse of the Day" Burp Suite extension.
 *
 * @author Gabe Rust
 */
public class VOTDPanel extends JPanel {

    private final MontoyaApi api;
    private final VOTD votd;

    private static final String TRANSLATION_KEY = "run.rekt.burpvotd.TRANSLATION";
    private static final String DEFAULT_TRANSLATION = "ESV";

    private final Timer timer;

    private final JTextField translationTextField = new JTextField();
    private final JEditorPane contentPane = new JEditorPane();

    /**
     * Constructs the main panel with provided Montoya API.
     *
     * @param api The MontoyaApi instance that provides access to various burp functionalities.
     */
    public VOTDPanel(MontoyaApi api) {
        this.api = api;
        this.votd = new VOTD(api);
        String storedTranslation = api.persistence().preferences().getString(TRANSLATION_KEY);
        if (storedTranslation == null) {
            storedTranslation = DEFAULT_TRANSLATION;
        }
        translationTextField.setText(storedTranslation.strip().toUpperCase());

        translationTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                api.persistence().preferences()
                        .setString(TRANSLATION_KEY, translationTextField.getText().strip().toUpperCase());
                updateContent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                api.persistence().preferences()
                        .setString(TRANSLATION_KEY, translationTextField.getText().strip().toUpperCase());
                updateContent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                api.persistence().preferences()
                        .setString(TRANSLATION_KEY, translationTextField.getText().strip().toUpperCase());
                updateContent();
            }
        });

        JButton manualRefreshButton = new JButton("Manual Refresh");
        manualRefreshButton.addActionListener((e) -> updateContent());

        // Update Verse of the Day once per hour
        timer = new Timer(3600000, (e) -> updateContent());
        timer.start();

        updateContent();

        contentPane.setContentType("text/html");
        contentPane.setMaximumSize(new Dimension(700, Short.MAX_VALUE));
        contentPane.setPreferredSize(new Dimension(700, Short.MAX_VALUE));
        contentPane.setEditable(false);
        contentPane.setOpaque(false);
        contentPane.addHyperlinkListener((e) -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) { /* NOTHING I CAN DO HERE */ }
                }
            }
        });

        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(contentPane);

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        JPanel upperPane = new JPanel(new FlowLayout());
        add(upperPane, BorderLayout.NORTH);

        JLabel translationLabel = new JLabel("Translation: ");
        upperPane.add(translationLabel);
        upperPane.add(translationTextField);
        upperPane.add(manualRefreshButton);
    }

    /**
     * Refreshes the content (Verse of the Day) on a separate thread based on the selected translation.
     */
    private void updateContent() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return votd.getVOTD(translationTextField.getText());
            }

            @Override
            protected void done() {
                try {
                    String votdText = get();
                    contentPane.setText(votdText);
                } catch (Exception e) {
                    api.logging().logToError(e);
                    contentPane.setText(votd.getDefaultVotd());
                }
            }
        };
        worker.execute();
    }

    /**
     * Stops the timer used for automatic refreshing.
     * This is necessary for the extension to be fully unloaded,
     * as SWING keeps a reference to running timers, preventing garbage collection.
     */
    public void stopTimer() {
        timer.stop();
    }

}
