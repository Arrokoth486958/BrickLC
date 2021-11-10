package soft.arrokoth.bricklc.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils
{
    public static String readFile(File file) throws IOException
    {
        StringBuilder result = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null)
        {
            result.append(line).append("\n");
        }
        reader.close();
        return result.toString();
    }

    public static void unzipFile(File sourceFile, File unzippedPath) throws IOException
    {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(sourceFile));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null)
        {
            unzippedPath.mkdirs();
            if (!zipEntry.isDirectory())
            {
                File finalFile = new File(unzippedPath.getAbsolutePath() + "\\" + zipEntry.getName().replace("/", "\\"));
                finalFile.getParentFile().mkdirs();
                finalFile.createNewFile();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(finalFile));
                byte[] bytes = new byte[1024];
                int readLen;
                while ((readLen = zipInputStream.read(bytes)) != -1)
                {
                    bufferedOutputStream.write(bytes, 0, readLen);
                }
                bufferedOutputStream.close();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    public static void copyFile(File sourceFile, File outputFile) throws IOException
    {
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();

        InputStream input = new FileInputStream(sourceFile);
        OutputStream output = new FileOutputStream(outputFile);

        byte[] buffer = new byte[1024];
        int byteRead;
        while ((byteRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, byteRead);
        }
        input.close();
        output.close();
    }

    private static String readInputStreamAsString(InputStream stream) throws IOException
    {
        StringBuilder result = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        while ((line = reader.readLine()) != null)
        {
            result.append(line).append("\n");
        }
        stream.close();
        return result.toString();
    }

    private static byte[] readInputStreamAsByte(InputStream stream) throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = stream.read(buffer)) != -1)
        {
            bos.write(buffer, 0, len);
        }
        stream.close();
        bos.flush();
        bos.close();
        return bos.toByteArray();
    }
}
