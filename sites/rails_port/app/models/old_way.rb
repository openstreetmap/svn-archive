class OldWay < ActiveRecord::Base
  set_table_name 'ways'

  belongs_to :user

  def self.from_way(way)
    old_way = OldWay.new
    old_way.visible = way.visible
    old_way.user_id = way.user_id
    old_way.timestamp = way.timestamp
    old_way.id = way.id
    old_way.nds = way.nds
    old_way.tags = way.tags
    return old_way
  end

  def save_with_dependencies!

    # dont touch this unless you really have figured out why it's called
    # (Rails doesn't deal well with the old ways table (called 'ways') because
    # it doesn't have a unique key. It knows how to insert and auto_increment
    # id and get it back but we have that and we want to get the 'version' back
    # we could add another column but thats a lot of data. No, set_primary_key
    # doesn't work either.
    save!
    clear_aggregation_cache
    clear_association_cache
    @attributes.update(OldWay.find(:first, :conditions => ['id = ? AND timestamp = ?', self.id, self.timestamp]).instance_variable_get('@attributes'))

    # ok, you can touch from here on

    self.tags.each do |k,v|
      tag = OldWayTag.new
      tag.k = k
      tag.v = v
      tag.id = self.id
      tag.version = self.version
      tag.save!
    end

    sequence = 1
    self.nds.each do |n|
      nd = OldWayNode.new
      nd.id = [self.id, self.version, sequence]
      nd.node_id = n
      nd.save!
      sequence += 1
    end
  end

  def nds
    unless @nds
        @nds = Array.new
        OldWayNode.find(:all, :conditions => ["id = ? AND version = ?", self.id, self.version], :order => "sequence_id").each do |nd|
            @nds += [nd.node_id]
        end
    end
    @nds
  end

  def tags
    unless @tags
        @tags = Hash.new
        OldWayTag.find(:all, :conditions => ["id = ? AND version = ?", self.id, self.version]).each do |tag|
            @tags[tag.k] = tag.v
        end
    end
    @tags = Hash.new unless @tags
    @tags
  end

  def nds=(s)
    @nds = s
  end

  def tags=(t)
    @tags = t
  end

#  has_many :way_nodes, :class_name => 'OldWayNode', :foreign_key => 'id'
#  has_many :way_tags, :class_name => 'OldWayTag', :foreign_key => 'id'

  def old_nodes
    OldWayNode.find(:all, :conditions => ['id = ? AND version = ?', self.id, self.version])    
  end

  def old_tags
    OldWayTag.find(:all, :conditions => ['id = ? AND version = ?', self.id, self.version])    
  end

  def to_xml_node
    el1 = XML::Node.new 'way'
    el1['id'] = self.id.to_s
    el1['visible'] = self.visible.to_s
    el1['timestamp'] = self.timestamp.xmlschema
    el1['user'] = self.user.display_name if self.user.data_public?
    
    self.old_nodes.each do |nd| # FIXME need to make sure they come back in the right order
      e = XML::Node.new 'nd'
      e['ref'] = nd.node_id.to_s
      el1 << e
    end
 
    self.old_tags.each do |tag|
      e = XML::Node.new 'tag'
      e['k'] = tag.k
      e['v'] = tag.v
      el1 << e
    end
    return el1
  end

  # Read full version of old way
  # For get_nodes_undelete, uses same nodes, even if they've moved since
  # For get_nodes_revert,   allocates new ids 
  # Currently returns Potlatch-style array
  
  def get_nodes_undelete
	points = []
	self.nds.each do |n|
	  node=Node.find(n)
	  points << [node.lon, node.lat, n, node.visible ? 1 : 0, node.tags_as_hash]
    end
	points
  end
  
  def get_nodes_revert
    points=[]
    self.nds.each do |n|
      oldnode=OldNode.find(:first, :conditions=>['id=? AND timestamp<=?',n,self.timestamp], :order=>"timestamp DESC")
      curnode=Node.find(n)
      id=n; v=curnode.visible ? 1 : 0
      if oldnode.lat!=curnode.lat or oldnode.lon!=curnode.lon or oldnode.tags!=curnode.tags then
        # node has changed: if it's in other ways, give it a new id
        if curnode.ways-[self.id] then id=-1; v=nil end
      end
      points << [oldnode.lon, oldnode.lat, id, v, oldnode.tags_as_hash]
    end
    points
  end

  # Temporary method to match interface to nodes
  def tags_as_hash
    return self.tags
  end

  # Temporary method to match interface to ways
  def way_nodes
    return self.old_nodes
  end

  # Pretend we're not in any relations
  def containing_relation_members
    return []
  end
end
