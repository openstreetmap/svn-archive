package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;



/**
 * Testet, dass ein subgraph keine kleinere Junction enth�lt
 * @author  Jörg Possin, Simon Scheider
 */
public class JMinimality {

	private boolean CheckMinimal = true;
	private final ArrayList<Channel> E;
	private final int Grid[][];
	private final ArrayList<Channel> OrEn;
	private final ArrayList<Channel> OrEx;
	private final int n;
	private final List<List<Object>> L = new ArrayList<List<Object>>(); //The list of columns to be sorted
	private long EEovern = 0;
	private final HashSet<Channel> subgraph = new HashSet<Channel>();//The candidate subgraph to be tested
	private ProgressMonitor pm;
	private final boolean pmenabled;
	private final ArrayList<HashSet<Channel>> junctions = new ArrayList<HashSet<Channel>>();
	private final boolean searchFirstJunction;
	private final ArrayList<Channel> subJunction = new ArrayList<Channel>();
	private final JPrepare jprepare;
	private boolean Check = false;
	private Iterator<int[]> it;


	public JMinimality(int[][] Grid, int n,
			ArrayList<Channel> E,
			ArrayList<Channel> entries,
			ArrayList<Channel> exits,
			ChannelDiGraph channeldigraph,
			boolean junctionsearch) {

		this.E = E;
		this.n = n;
		this.Grid = Grid;
		this.OrEn = entries;
		this.OrEx = exits;
		this.pmenabled = false;
		this.searchFirstJunction = junctionsearch;
		this.jprepare = new JPrepare(channeldigraph);
	}

	public JMinimality(int[][] Grid, int n,
			ArrayList<Channel> E,
			ArrayList<Channel> entries,
			ArrayList<Channel> exits,
			ChannelDiGraph channeldigraph,
			ProgressMonitor pm,
			boolean junctionsearch) {

		this.E = E;
		this.n = n;
		this.Grid = Grid;
		this.OrEn = entries;
		this.OrEx = exits;
		this.pm = pm;
		this.pmenabled = true;
		this.searchFirstJunction = junctionsearch;
		this.jprepare = new JPrepare(channeldigraph);
		//this.jCheck= new JCheck();
	}

	public void GenerateSubcolumns () {/*Generates all combinations of subcolumns in the grid*/
		if (pmenabled) {
			pm.setCustomText(tr ("generate all combinations from entrie/exit candidates"));
		}

		Combination c = new Combination(Grid.length, n);
		EEovern = (int) Combination.Choose(Grid.length*Grid.length, n*n);
		long ans = c.Choose(); //This is the number of subcolumns to be generated
		int[][] v; // this is a column variable containing n y-index entries plus true false values (0/1)
		List<Object> C; //The column is packed together with 2 indices into this variable
		for (int i = 0; i < Grid.length;i++) {
			int h = 0;	//this is the index denoting the "n out of Grid.length"- combination, indicating a subcolumn of length n
			do {
				int missing = 0;
				C = new ArrayList<Object>(3);
				v = new int[n][2];
				C.add(i);//the first position of column variable C is the column index
				C.add(h);//the second is the entry-subset index
				for(int t = 0; t < c.data.length; t++){
					if (Grid[(int)c.data[t]][i] == 0){
						missing++;
						v[t][1]= 0; //false
					}
					else{
						v[t][1]= 1;
					} //true
					v[t][0]=(int)c.data[t];	//Write a y index of the combination into column
				}
				if (missing <=1){//If column has at most one missing entry
					C.add(v);//insert column as the third position of column variable C
					L.add(C); //Insert C in list to be ordered
				}
				h++; //Iterate through all subcolumns
				if (h < ans){c = c.Successor();}//generate the next combination
			}while(h < ans);
			c = new Combination(Grid.length, n); //For each original column in the grid, generate new subcolumns
		}
		Collections.sort(L, new Comparator<List<Object>>() {
			public int compare(List<Object> o1, List<Object> o2) {
				return (Integer)o1.get(1) - (Integer)o2.get(1); //sort according to h index in each column
			}});
	}

	public boolean IterateThroughKn() {//Iterates through all K_{n-1} subgrids of the Grid and tests them
		if (L.size()==0) {
			return true;
		}
		if (pmenabled) {
			pm.setTicksCount(L.size());
			pm.setCustomText("Iterates through all K_{n-1} subgrids of the Grid and tests them");
		}
		Combination c;
		Iterator<List<Object>> l = L.listIterator();
		List<Object> C;
		ArrayList<int[]> CandidateK = new ArrayList<int[]>(n*n); //saves the candidate K_{n-1} in entry-exit pairs
		long lindex= 0;
		int h = 0;
		int m = 0;
		int[][] v;
		int x_i;
		int y_j;
		int progressmonitorcounter = 1;
		boolean mchanged = false;
		boolean hchanged = false;
		C = l.next();
		do{ //Loop over list of columns L
			if (mchanged){
				C = l.next(); //Iterator in L
				lindex++; //Index in L
				if (hchanged) {
					m=1;
					hchanged = false;
				}
			}
			if ((Integer)C.get(1)==h && l.hasNext()){ //m counts the set of columns with index h
				m++;
				mchanged = true;
			}
			else{
				if (l.hasNext()==false){
					m++;lindex++;
				} //At the end of L, counter are set one up
				c = new Combination(m, n);
				long ans = c.Choose();
				int missing = 0;
				boolean smallerjunction = false;
				for (int k =0; k<ans;k++){ //Makes sure that subset of m columns contains an n*n subgrid, because ans = m over n would be 0 otherwise
					for (int y = 0; y < n; y++){//Iterates over all rows of subgrid k
						missing =0;	//test = "";
						for (int x = 0; x <c.data.length;x++) { //Iterates over all columns of subgrid k
							x_i=((Integer)L.get((int)(lindex-m+c.data[x])).get(0));//columnindex in grid
							v=((int[][])(L.get((int)(lindex-m+c.data[x])).get(2))); //subcolumn of grid
							y_j= v[y][0]; //rowindex in grid
							if (v[y][1]==0){
								missing++;
							}else{
								CandidateK.add(new int[]{y_j,x_i});
							}//save entry/exit tuple
							if (smallerjunction == false && ((!OrEn.contains(E.get(v[y][0]))) &&(!OrEx.contains(E.get(x_i))))){ // Tests, whether y or x is not an original entry/exit
								smallerjunction = true; //Then k identifies a different junction than the original one
							}
							//test = test+" ("+y_j+", "+x_i+", "+v[y][1]+")";
						}
						if (missing > 1){
							break;
						}//If a row has more than one missing value, break
					}
					if (missing <=1 && smallerjunction == true){//The k-subgrid is a different junction candidate satisfying total reachability
						CheckMinimal = CheckSmallJunction(CandidateK)==false;// If the candidate is a smaller junction, then minimality is false
						//log.info("durchlauf: " + durchlauf + " Wert von CheckMinimal: " + CheckMinimal);
						if (!CheckMinimal) {
							break;
						}
					}
					CandidateK.clear();
					if (k+1 < ans){c = c.Successor();} //Produces the m over n combinations
				}
				m=1; //Sets m to the first column with next index h+1
				h++;
				mchanged = false;
				hchanged = true;
			}
			if (pmenabled) {
				progressmonitorcounter++;
				pm.setTicks(progressmonitorcounter);
			}
		}
		while(l.hasNext() && CheckMinimal);
		return CheckMinimal;
	}

	/**
	 * gibt true zurück, wenn Kandidat eine Kreuzung ist, aber nicht, wenn junctionsearch auf true gesetzt ist
	 * @param CandidateK
	 * @return
	 */
	public boolean CheckSmallJunction(ArrayList<int[]> CandidateK){
		Check = false;
		subgraph.clear();//Zu konstruierender Subgraph
		it = CandidateK.iterator();
		//Reconstruct small Junction from paths
		while (it.hasNext()){
			int[]point = it.next();
			for (int j = 0; j < E.get(point[0]).getReachableNodes().size(); j++) {
				if(E.get(point[0]).getReachableNodeAt(j).equals(E.get(point[1]))){
					subgraph.addAll(E.get(point[0]).getPathsAt(E.get(point[0]).getReachableNodeAt(j)));
					subgraph.add(E.get(point[0]));
				}
			}
		}
		jprepare.jPrepare(new ArrayList<Channel>(subgraph));
		JCheck jCheck = new JCheck();
		Check = jCheck.jCheck(jprepare.getEntries(), jprepare.getExits(), n);
		jprepare.resetSubgraph();
		if (Check) {
			subJunction.clear();
			subJunction.addAll(subgraph);
			//soll mehr als ein Kandidat gesucht werden? Dann Kandidaten speichern und Check wieder auf false stellen, damit die Hauptschleife weitergeht
			if (!searchFirstJunction) {
				boolean isin = false;
				for (int i = 0; i < junctions.size(); i++) {
					//log.debug("Kreuzung " + i +" hat Channels: " + junctions.get(i).size() + " subgraph: " + subgraph.size());
					if (junctions.get(i).size() == subgraph.size()) {
						Iterator<Channel> it = subgraph.iterator();
						isin = true;
						while (it.hasNext()) {
							if (!junctions.get(i).contains(it.next())) {
								isin = false;
								//log.info("nicht drin");
							}
						}
					}
				}
				if (isin == false) {
					junctions.add(new HashSet<Channel>(subgraph));
					//log.info("Kreuzungskandidat der Liste zugefügt" + junctions.size());
				}
				Check = false;
			}
		}
		return Check;
	}

	/**
	 * enthält alle Channels des zuletzt durchlaufenden Kreuzungskandidaten (muß keine gültige Kreuzung sein)
	 * @return
	 */
	public ArrayList<Channel> getSubJunctionCandidate(){
		return new ArrayList<Channel>(subgraph);
	}

	/**
	 * gibt alle gefundenen Kreuzungskandidaten zurück (ist leer, wenn junctionsearch auf true gesetzt wurde)
	 * @return
	 */
	public ArrayList<HashSet<Channel>> getJunctionCandidates() {
		return junctions;
	}
}
