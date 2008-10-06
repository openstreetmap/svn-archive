import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Options implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6418186631698139520L;
	
	
	
	
	public static String std_OSM_file = System.getProperty("user.home");
	public static String std_MF_file = System.getProperty("user.home");


	
	

	private final static String optionfilename = "options.opt";
	private final static String optionPath = System.getProperty("user.home")
			+ "/.osmolt/";
	
	
	static void saveOptions() {

		try {
			

			File outp = new File(optionPath + optionfilename);
			if (!outp.exists()) {

				File outp2 = new File(System.getProperty("user.home")
						+ "/.osmolt");
				outp2.mkdirs();
				outp.createNewFile();
			}
			ObjectOutputStream objOut;
			objOut = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(optionPath + optionfilename)));

			objOut.writeObject(new Options());
			objOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void openOptions() {
		try {
			ObjectInputStream objIn;
			objIn = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(optionPath + optionfilename)));

			@SuppressWarnings("unused")
			Options poly2 = (Options) objIn.readObject();

			objIn.close();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeObject(ObjectOutputStream os) throws IOException {
		os.defaultWriteObject();
		os.writeUTF(std_OSM_file);
		os.writeUTF(std_MF_file);

	}

	private void readObject(ObjectInputStream is) throws IOException,
			ClassNotFoundException {
		is.defaultReadObject();
		std_OSM_file = is.readUTF();
		std_MF_file = is.readUTF();

	}
}
