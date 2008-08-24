
import java.io.File;



/**
 * Comandozeilen schnittstelle von osmparser
 * 
 * @author Josias Polchau
 * 
 * Copyright (C) 2008 Josias Polchau
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 * 
 * */

public class osmparse implements ProcessbarAccess {

public static void main(String[] args) throws Exception {
	if ((args.length ==0)||(args[0].equals("--help"))){printHelp();
	}
	else{
		int i=0;
		String inputfile=null;
		String outputfile=null;
		String mapFeatures=null;
		boolean differentFiles=false;
		while (i<args.length){
			switch (args[i].charAt(0)) {
			case 'x':
				if (args[i].charAt(1)=='=') inputfile = args[i].substring(2);
			break;
			case 'f':
				if (args[i].charAt(1)=='=') mapFeatures = args[i].substring(2);
			break;
			case 'o':
				if (args[i].charAt(1)=='=') outputfile = args[i].substring(2);
			break;
			case 'd':
				differentFiles=true;
				args[i]=args[i].substring(1);
				--i;
			break;
			case '-':
				args[i]=args[i].substring(1);
				--i;
			break;

			default:
				break;
			} 
			i++;
		}
		if ((outputfile==null)) differentFiles=true;
		
		if ((inputfile==null)||(!(new File(inputfile).exists()))) System.out.println("bitte gib ein Inputfile an");
		else if ((mapFeatures==null)||(!(new File(mapFeatures).exists()))) System.out.println("bitte gib ein MapFeatures-File an");
		else
		{
			osmparser proggi = new osmparser(mapFeatures,inputfile,outputfile,differentFiles,new osmparse());
			proggi.run();
		}
	}
}

private static void printHelp() {

	System.out.println("OSMParser");
	System.out.println("systax: osmparse OPTION\n" +
			"\n" +
			"required options:\n" +
			"-x=[Inputfile]      osm-XML data\n" +
			"-f=[mapFeatures]    XML file with requested Tags (see readme)\n" +
			"\n" +
			"optional:\n" +
			"-o=[outputfile]     file to write the Openlayer-Text. if not specified saves each Tag to different Openlayer-Text-files\n" +
			"\n" +
			"e.g.:      >java osmparse f=mapFeatures.xml x=map.osm \n" +
			"\n\n" +
			"OSMParser  Copyright (C) 2008  Josias Polchau\n\n" +
			"This program comes with ABSOLUTELY NO WARRANTY.\n" +
			"This is free software, and you are welcome to redistribute it\n" +
			"under certain conditions; look at the readme-file for details.\n");
	

}

public void addvalue() {
System.out.println("#");
	
}

public void processAdd() {
	System.out.println("#");
	
}

public void processStart() {
	System.out.println("|");	
}

public void processStop() {
	System.out.println("|");	
	
}

}
