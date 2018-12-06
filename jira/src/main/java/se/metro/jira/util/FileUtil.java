package se.metro.jira.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import se.metro.jira.Configuration;

public final class FileUtil {
    public static String readResourceTextFile(final String path) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(Configuration.class.getResourceAsStream("/" + path), "UTF8"));

            final StringBuilder resultText = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                resultText.append(line).append("\n");
            }
            return resultText.toString();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static String formatByteSize(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(1024));
        final String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
