package org.ryboun.sisa;

import java.io.BufferedInputStream;
import java.io.IOException;

public class TestUtils {

    //loading files from test/resource. WTF, why like this??
    public static String loadStringFileFromResources(String filePath) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(TestUtils.class.getClassLoader()
                .getResourceAsStream(filePath))) {
            byte[] contents = new byte[1024];

            int bytesRead = 0;
            String strFileContents = null;
            while((bytesRead = bis.read(contents)) != -1) {
                strFileContents += new String(contents, 0, bytesRead);
            }

            return strFileContents.substring(4);
        }
    }
}
