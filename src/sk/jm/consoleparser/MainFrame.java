package sk.jm.consoleparser;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

/**
 * Created by Juraj on 17.7.2014.
 */
public class MainFrame extends JFrame {
    private TextProcessor.SearchOptions lastSearchOption = TextProcessor.SearchOptions.contains;
    private boolean autoScroll = true;
    TextProcessor textProcessor;
    ConsoleMonitor consoleMonitor;
    Preferences preferences = Preferences.userNodeForPackage(sk.jm.consoleparser.Main.class);
    private Thread labelsThread;

    private JPanel mainPanel;
    private JButton pauseButton;
    private JTabbedPane tabbedPane1;
    private JButton stopButton;
    private JTextField runTextField;
    private JTextPane textPaneOut;
    private JButton runButton;
    private JButton clearTextButton;
    private JButton actRunButton;
    private JButton actDebugRunButton;
    private JScrollPane scrollPaneOut;
    private JCheckBox autoscrollCheckBox;
    private JTextField targetDirectoryTextField;
    private JTextArea textAreaIgnoredList;
    private JTextField searchSqlTextField;
    private JButton containsButton;
    private JButton startsWithButton;
    private JButton endsWithButton;
    private JTextPane textPaneSQL;
    private JTextPane textPaneIgnored;
    private JLabel containsLabel;
    private JScrollPane scrollPaneSQL;
    private JLabel sqlCountsLabel;
    private JButton removeAllSqlButton;

    public MainFrame() {
        super("Console parser");
        setContentPane(mainPanel);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        changeLaf(this);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width/6, screenSize.height/4);
        this.setSize(1280, 800);

        setVisible(true);
        loadSettings();
        actRunButton.grabFocus();

        labelsThread = new Thread(runnableLabelsUpdate);
        labelsThread.start();
        // ------------------------------Initialization-----------------------------------------------
        // -------------------------------------------------------------------------------------------

        DefaultCaret caret = (DefaultCaret)textPaneSQL.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

//        textPaneOut.setBackground(Color.black);
        Document documentOut = textPaneOut.getDocument();
        textProcessor = new TextProcessor(documentOut, textPaneIgnored.getDocument(), textPaneSQL.getDocument(), sqlCountsLabel);
        consoleMonitor = new ConsoleMonitor(textProcessor);

        // this will close any running activator process when closing main window (exiting application)
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                saveSettings();
                if (consoleMonitor.isRunning())
                    consoleMonitor.stopMonitor();
                System.exit(0);
            }
        } );

        scrollPaneOut.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            BoundedRangeModel brm = scrollPaneOut.getVerticalScrollBar().getModel();
            int length, oldLength = documentOut.getLength();
            boolean wasAtBottom = true;
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!autoScroll) return;
                if (!brm.getValueIsAdjusting()) {
                    if (wasAtBottom) {
                        // this will allow us to scroll up when at bottom
                        length = documentOut.getLength();
                        if (oldLength != length) {
                            oldLength = length;
                            brm.setValue(brm.getMaximum());
                        }
                    }
                } else
                    wasAtBottom = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());

            }
        });

        stopButton.addActionListener(e -> consoleMonitor.stopMonitor());
        pauseButton.addActionListener(e -> ((JButton)e.getSource()).setText(consoleMonitor.pause() ? "Resume" : "Pause"));
        actRunButton.addActionListener(e -> startMonitor("act run"));
        actDebugRunButton.addActionListener(e -> startMonitor("act -jvm-debug run"));
        runButton.addActionListener(e -> startMonitor(runTextField.getText().trim()));

        autoscrollCheckBox.addActionListener(e -> autoScroll = ((JCheckBox)e.getSource()).isSelected());
        clearTextButton.addActionListener(e -> textPaneOut.setText(""));

        containsButton.addActionListener(e -> handleSearch(((JButton)e.getSource()).getText(), TextProcessor.SearchOptions.contains));
        startsWithButton.addActionListener(e -> handleSearch(((JButton)e.getSource()).getText(), TextProcessor.SearchOptions.startsWith));
        endsWithButton.addActionListener(e -> handleSearch(((JButton) e.getSource()).getText(), TextProcessor.SearchOptions.endsWith));
        searchSqlTextField.addActionListener(e -> handleSearch(null, null));
        removeAllSqlButton.addActionListener(e -> textProcessor.clearSqls());
    }

    private void startMonitor(String command) {
        consoleMonitor.startMonitor(command, targetDirectoryTextField.getText(), textAreaIgnoredList.getText());
    }

    private void handleSearch(String buttonText, TextProcessor.SearchOptions searchOption) {
        if (buttonText != null)
            containsLabel.setText(buttonText);
        if (searchOption != null)
            lastSearchOption = searchOption;
        textPaneSQL.setText(textProcessor.getFilteredSQL(searchSqlTextField.getText(), lastSearchOption));
//        scrollPaneSQL.getVerticalScrollBar().setValue(0);
    }

    public static void changeLaf(JFrame frame) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void loadSettings() {
        targetDirectoryTextField.setText(preferences.get("dirPath", "C:\\Users\\Juraj\\git\\cloudfarms\\"));
        runTextField.setText(preferences.get("command", "act run"));
        textAreaIgnoredList.setText(preferences.get("ignoreList", "[debug] c.j.b.PreparedStatementHandle - select hash \n" +
                "[info] application - \n" +
                "[debug] c.j.b.PreparedStatementHandle - {call\n" +
                "[debug] c.j.b.PreparedStatementHandle - select id from organization order by id"));
    }

    private void saveSettings() {
        preferences.put("dirPath", targetDirectoryTextField.getText());
        preferences.put("command", runTextField.getText());
        preferences.put("ignoreList", textAreaIgnoredList.getText());
    }



    Runnable runnableLabelsUpdate = () -> {
        while (true) {
            Utils.sleep(500);
            sqlCountsLabel.setText(textProcessor.getSqlsCounts());
        }
    };
}
