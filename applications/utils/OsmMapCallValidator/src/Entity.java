import java.util.HashMap;
import java.util.Map;


public class Entity {
	public boolean validated;
	public long id;
	public String timestamp;
	public Map<String,String> tags;
	public int version;
	public int changeset;
	public String user;
	public int uid;
	
	public Entity(long id, int version, int changeset, int uid, String user, String timestamp) {
		validated = false;
		this.id = id;
		this.version = version;
		this.changeset = changeset;
		this.uid = uid;
		this.user = user;
		this.timestamp = timestamp;
		this.tags = new HashMap<String, String>();
	}
	
	public void setTag(String key, String value) {
		tags.put(key, value);
	}
	
	public boolean compare(Entity e2) {
		if (id != e2.id) {
			System.out.println("Error: Id's did not match " + id + "!=" + e2.id);
			return false;
		}
		
		if (version != e2.version) {
			System.out.println("Error: Versions's did not match " + version + "!=" + e2.version);
			return false;
		}
		
		if (changeset != e2.changeset) {
			System.out.println("Error: changesets's did not match " + changeset + "!=" + e2.changeset);
			return false;
		}
		
		if (uid != e2.uid) {
			System.out.println("Error: Uid's did not match " + uid + "!=" + e2.uid);
			return false;
		}
		
		if (!user.equals(e2.user)) {
			System.out.println("Error: Users's did not match " + user + "!=" + e2.user);
			return false;
		}
		
		if (!timestamp.equals(e2.timestamp)) {
			System.out.println("Error: Users's did not match " + timestamp + "!=" + e2.timestamp);
			return false;
		}
		
		if (tags.size() != e2.tags.size()) {
			System.out.println("Error: Number of tags did not match " + tags.size() + "!=" + e2.tags.size());
			return false;
		}
		
		for (String key : tags.keySet()) {
			String value = tags.get(key);
			String value2 = e2.tags.get(key);
			if (!value.equals(value2)) {
				System.out.println("Error: tags did not match for " + key + " " + value + "!=" + value2);
				return false;
			}
		}
		
		return true;
	}
}
