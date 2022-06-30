package uk.gov.hmcts.ethos.replacement.functional.util;

import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoServiceTest;

@Slf4j
public class FileUtil {

    private FileUtil() {
    }

    public static String downloadFileFromUrl(String strUrl, String authToken) throws IOException {
        InputStream in = null;
        try {
            var url = new URL(strUrl.replace("127.0.0.1", "localhost"));
            URLConnection uc = url.openConnection();
            uc.setRequestProperty("Authorization", authToken);
            uc.connect();
            in = uc.getInputStream();
            String destinationFile = Constants.DOWNLOAD_FOLDER + "/document-" + getFileSuffix() + ".docx";
            Files.copy(in, Paths.get(destinationFile), StandardCopyOption.REPLACE_EXISTING);
            return destinationFile;
        }
        finally {
           if (in != null)
            {safeCloseInputStream(in); }
        }
    }

    public static String getFileSuffix() {
        return RandomStringUtils.randomAlphanumeric(5);
    }

    public static void safeCloseInputStream(InputStream fis) {
        if (fis != null) {
            try
            { fis.close(); }
            catch (IOException e)
            { log.error(e.toString());}
        }
    }
}
