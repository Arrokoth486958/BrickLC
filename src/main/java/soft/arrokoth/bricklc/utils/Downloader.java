package soft.arrokoth.bricklc.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class Downloader
{
    public static void downloadFile(URL url, File saveFile) throws IOException
    {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoInput(true);

        InputStream inputStream = connection.getInputStream();
        byte[] getData = readInputStream(inputStream);
        System.out.println(saveFile.getParentFile().getAbsolutePath());

        File saveDir = new File(saveFile.getParentFile().getAbsolutePath());
        saveDir.mkdirs();

        File file = saveFile;
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(getData);

        if (fileOutputStream != null)
        {
            fileOutputStream.close();
        }
        if (inputStream != null)
        {
            inputStream.close();
        }
//        fileOutputStream.close();
//        inputStream.close();
    }

    private static byte[] readInputStream(InputStream stream) throws IOException
    {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = stream.read(buffer)) != -1)
        {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
