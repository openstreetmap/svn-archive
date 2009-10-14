package org.openstreetmap.osmolt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Copy {

  public static void fileAsStream(InputStream src, File dest, int bufSize,
      boolean force) throws IOException {

    if (dest.exists()) {
      if (force) {
        dest.delete();
      } else {
        throw new IOException("Cannot overwrite existing file");
      }
    }
    byte[] buffer = new byte[bufSize];
    int read = 0;
    InputStream in = null;
    OutputStream out = null;
    try {
      in = src;
      out = new FileOutputStream(dest);
      while (true) {
        read = in.read(buffer);
        if (read == -1) {
          // -1 bedeutet EOF
          break;
        }
        out.write(buffer, 0, read);
      }
    } finally {
      // Sicherstellen, dass die Streams auch
      // bei einem throw geschlossen werden.
      // Falls in null ist, ist out auch null!
      if (in != null) {
        // Falls tatsächlich in.close() und out.close()
        // Exceptions werfen, die jenige von 'out' geworfen wird.
        try {
          in.close();
        } finally {
          if (out != null) {
            out.close();
          }
        }
      }
    }
  }

  public static void file(File src, File dest, int bufSize, boolean force)
      throws IOException {
    fileAsStream(new FileInputStream(src), dest, bufSize, force);
  }
}