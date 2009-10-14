package org.openstreetmap.osmolt;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Kopiere {

	public void datei(String dateiname, String destPath) {
		InputStream in = getClass().getResourceAsStream(dateiname);
		BufferedInputStream bufIn = new BufferedInputStream(in);

		BufferedOutputStream bufOut = null;

		try {
			bufOut = new BufferedOutputStream(new FileOutputStream(destPath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		byte[] inByte = new byte[4096];
		int count = -1;
		try {
			while ((count = bufIn.read(inByte)) != -1) {
				System.out.println(count);
				bufOut.write(inByte, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			bufOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bufIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}