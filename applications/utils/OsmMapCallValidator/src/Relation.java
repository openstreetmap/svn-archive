import java.util.ArrayList;


public class Relation extends Entity{

	private ArrayList<RelationMember> members;
	
	public Relation(long id, int version, int changeset, int uid, String user,
			String timestamp) {
		super(id, version, changeset, uid, user, timestamp);
		
		members = new ArrayList<RelationMember>();
	}
	
	public void addMember(RelationMember rm) {
		members.add(rm);
	}
	
	public int noMembers() {
		return members.size();
	}
	
	public RelationMember getMember(int i) {
		return members.get(i);
	}

}
