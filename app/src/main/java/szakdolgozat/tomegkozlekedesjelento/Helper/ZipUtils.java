package szakdolgozat.tomegkozlekedesjelento.Helper;

import android.content.Context;
import android.net.Uri;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    public static File unzipToCache(Context context, Uri zipUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(zipUri);
        File outputDir = new File(context.getCacheDir(), "gtfs");
        if (outputDir.exists()) deleteRecursive(outputDir);
        outputDir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
        }
        return outputDir;
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }
}
