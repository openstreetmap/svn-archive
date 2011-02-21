import java.util.ArrayList;


public class Way extends Entity{
	
	private ArrayList<Node> nodes;

	public Way(long id, int version, int changeset, int uid, String user,
			String timestamp) {
		super(id, version, changeset, uid, user, timestamp);
		nodes = new ArrayList<Node>();
	}
	
	public void add(Node n) {
		nodes.add(n);
	}
	
	public int noNodes() {
		return nodes.size();
	}
	
	public Node getNode(int i) {
		return nodes.get(i);
	}

}
