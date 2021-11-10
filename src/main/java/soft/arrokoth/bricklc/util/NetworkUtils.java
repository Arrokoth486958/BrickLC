package soft.arrokoth.bricklc.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils
{
    public static void downloadFileWithRedirect(URL url, File saveFile) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);

        InputStream inputStream = connection.getInputStream();
        byte[] getData = readInputStream(inputStream);

        File saveDir = new File(saveFile.getParentFile().getAbsolutePath());
        saveDir.mkdirs();

        if (new String(getData).contains("<p>Found. Redirecting to <a href="))
        {
            String str = new String(getData).substring(new String(getData).indexOf("href=\"") + "href=\"".length());
            connection.disconnect();
            downloadFile(new URL(str.substring(0, str.indexOf("\">"))), saveFile);
            return;
        }

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
    }

    public static void downloadFile(URL url, File saveFile) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);

        InputStream inputStream = connection.getInputStream();
        byte[] getData = readInputStream(inputStream);

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
    }

    private static byte[] readInputStream(InputStream stream) throws IOException
    {
        ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int readLen;
        while ((readLen = stream.read(bytes)) != -1)
        {
            bufferedOutputStream.write(bytes, 0, readLen);
        }
        bufferedOutputStream.close();
        stream.close();
        return bufferedOutputStream.toByteArray();
    }
}
