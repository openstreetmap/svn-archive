package opendata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ModuleListGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String url = "http://svn.openstreetmap.org/applications/editors/josm/plugins/opendata/";
		String baseDir = "";
		if (args.length > 0) {
			baseDir = args[0];
		}
		try {
			BufferedWriter list = new BufferedWriter(new FileWriter(baseDir+"modules.txt"));
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(baseDir+"modules-icons.zip"));
			for (File file : new File(baseDir+"dist").listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			})) {
				try {
					String filename = file.getName();
					System.out.println("Processing "+filename);
					list.write(filename+";"+url+filename); list.newLine();
					Manifest mf = new JarFile(file).getManifest();
					for (Object att : mf.getMainAttributes().keySet()) {
						Object value = mf.getMainAttributes().get(att);
						if (value != null) {
							list.write("\t"+att+": "+value.toString()); list.newLine();
							if (att.toString().equals("Module-Icon")) {
								// Directory with jar name, including extension
								String name = filename+"/";
								zip.putNextEntry(new ZipEntry(name));
								// Directory tree to image
								String[] items = value.toString().split("/");
								for (int i=0; i<items.length-1; i++) {
									zip.putNextEntry(new ZipEntry(name += items[i]+"/"));
								}
								// Image file
								zip.putNextEntry(new ZipEntry(name += items[items.length-1]));
								try {
									FileInputStream in;
									try {
										in = new FileInputStream(baseDir+"modules/"+filename.replace(".jar", "/")+value.toString());
									} catch (FileNotFoundException e) {
										// If not in module dir, may be in main images directory
										if (value.toString().startsWith("images/")) {
											in = new FileInputStream(baseDir+value.toString());
										} else {
											throw e;
										}
									}
									try {
										byte[] buffer = new byte[4096];
										int n = -1;
										while ((n = in.read(buffer)) > 0) {
											zip.write(buffer, 0, n);
										}
									} finally {
										in.close();
									}
								} catch (IOException e) {
									System.err.println("Cannot load Image-Icon: "+value.toString());
								} finally {
									zip.closeEntry();
								}
							}
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Done");
			zip.close();
			list.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
