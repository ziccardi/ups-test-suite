package org.jboss.aerogear.test;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CSV {
    final PrintWriter pw;

    private CSV() {
        this.pw = null;
    }

    public CSV(final String outputPath, final String... columns) throws IOException {
        pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputPath)));
        String header = String.join(",", columns);
        pw.println("#" + header);
    }

    public void addLine(String... columns) throws IOException {
        pw.println(String.join(",", columns));
    }

    public void close() {
        pw.close();
    }

    static class NOOP extends CSV {

        public static final NOOP INSTANCE = new NOOP();

        private NOOP() {
        }

        @Override
        public void addLine(String... columns) {
        }

        @Override
        public void close() {
        }
    }
}