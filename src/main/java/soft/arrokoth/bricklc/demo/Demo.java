package soft.arrokoth.bricklc.demo;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import soft.arrokoth.bricklc.auth.account.MicrosoftAccount;
import soft.arrokoth.bricklc.auth.account.OfflineAccount;
import soft.arrokoth.bricklc.game.GameLauncher;
import soft.arrokoth.bricklc.game.VersionManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Demo
{
    public static final File gameDictPath = new File(".minecraft");
    public static final File launcherDataPath = new File("BrickLC");

    public static void main(String[] args) throws IOException
    {
        JFrame frame = new JFrame();
        frame.setTitle("BrickLC - Demo");
//        frame.setIconImage(ImageIO.read(Objects.requireNonNull(Demo.class.getClassLoader().getResource("assets/textures/icon.png"))));
        frame.setSize(new Dimension(512, 512));
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setSize(frame.getSize());
        panel.setVisible(true);
        frame.add(panel);

        JTextField javaSelection = new JFormattedTextField();
        JComboBox<String> comboBox = new JComboBox<>();
        JButton launchButton = new JButton("Launch");
        launchButton.setPreferredSize(new Dimension(480, 32));

        panel.add(javaSelection);
        panel.add(comboBox);
        panel.add(launchButton);

        JFrame frameLogin = new JFrame();
        frameLogin.setTitle("BrickLC - Demo");
//        frameLogin.setIconImage(ImageIO.read(Objects.requireNonNull(Demo.class.getClassLoader().getResource("assets/textures/icon.png"))));
        frameLogin.setSize(new Dimension(512, 512));
        frameLogin.setResizable(false);
        frameLogin.setVisible(true);

        JPanel panelLogin = new JPanel();
        frameLogin.add(panelLogin);

        JFXPanel jfxPanel = new JFXPanel();
        panelLogin.add(jfxPanel);

        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                WebView webView = new WebView();
                webView.setPrefSize(480, 480);
                jfxPanel.setScene(new Scene(webView));
                WebEngine webEngine = webView.getEngine();
                webEngine.load("https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf");

                webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue)
                    {
                        if (webEngine.getLocation().contains("https://login.live.com/oauth20_desktop.srf?"))
                        {
                            try
                            {
                                MicrosoftAccount.authenticate(MicrosoftAccount.detachCode(webEngine.getLocation()));
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        javaSelection.setPreferredSize(new Dimension(480, 32));
        javaSelection.setText(System.getProperty("java.library.path").substring(0, System.getProperty("java.library.path").indexOf(";")));

        comboBox.setPreferredSize(new Dimension(480, 32));
        for (String s : VersionManager.getManifestAvailableVersions(true, true, true))
        {
            comboBox.addItem(s);
        }

        launchButton.addActionListener(e ->
        {
            try
            {
                gameDictPath.mkdirs();
                launcherDataPath.mkdirs();
                String selectedVersion = (String) comboBox.getSelectedItem();
                String javaPath = javaSelection.getText();
                VersionManager.downloadVersionJson(selectedVersion, gameDictPath);
                VersionManager.downloadMainJar(selectedVersion, gameDictPath);
                VersionManager.downloadAssets(selectedVersion, gameDictPath);
                VersionManager.downloadLibraries(selectedVersion, gameDictPath, launcherDataPath);
                GameLauncher.generateLaunchBatch(gameDictPath, launcherDataPath, javaPath, GameLauncher.generateJVMArguments(gameDictPath, selectedVersion, 256, 4096, VersionManager.downloadLoggerConfig(selectedVersion, gameDictPath), "BrickLC", "a1.0.0-experimental", true), selectedVersion, new OfflineAccount("DevelopAccount"), true);
                GameLauncher.launchBatch(launcherDataPath);
            }
            catch (IOException | InterruptedException ex)
            {
                ex.printStackTrace();
            }
        });
    }
}
