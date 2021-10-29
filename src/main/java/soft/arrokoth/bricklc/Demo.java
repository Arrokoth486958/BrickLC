package soft.arrokoth.bricklc;

import soft.arrokoth.bricklc.utils.VersionsManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Demo
{
    public static void main(String[] args) throws IOException
    {
        JFrame frame = new JFrame();
        frame.setIconImage(ImageIO.read(Objects.requireNonNull(Demo.class.getClassLoader().getResource("assets/textures/icon.png"))));
        frame.setSize(new Dimension(512, 512));
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        VersionsManager versionsManager = new VersionsManager();

        try
        {
            System.out.println(versionsManager.getManifestAvailableVersions(true, true, true));
            System.out.println(versionsManager.getLocalAvailableVersions(".minecraft"));
            System.out.println(versionsManager.getMapByVersion("1.17.1"));
            versionsManager.downloadVersionJson("1.7.10", ".minecraft");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
