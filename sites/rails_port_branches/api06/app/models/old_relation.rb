class OldRelation < ActiveRecord::Base
  include ConsistencyValidations
  
  set_table_name 'relations'

  belongs_to :changeset

  def self.from_relation(relation)
    old_relation = OldRelation.new
    old_relation.visible = relation.visible
    old_relation.changeset_id = relation.changeset_id
    old_relation.timestamp = relation.timestamp
    old_relation.id = relation.id
    old_relation.version = relation.version
    old_relation.members = relation.members
    old_relation.tags = relation.tags
    return old_relation
  end

  def save_with_dependencies!

    # see comment in old_way.rb ;-)
    save!
    clear_aggregation_cache
    clear_association_cache
    @attributes.update(OldRelation.find(:first, :conditions => ['id = ? AND timestamp = ?', self.id, self.timestamp]).instance_variable_get('@attributes'))

    # ok, you can touch from here on

    self.tags.each do |k,v|
      tag = OldRelationTag.new
      tag.k = k
      tag.v = v
      tag.id = self.id
      tag.version = self.version
      tag.save!
    end

    i = 1
    self.members.each do |m|
      member = OldRelationMember.new
      member.id = self.id
      member.member_type = m[0]
      member.member_id = m[1]
      member.member_role = m[2]
      member.version = self.version
      member.save!
    end
  end

  def members
    unless @members
        @members = Array.new
        OldRelationMember.find(:all, :conditions => ["id = ? AND version = ?", self.id, self.version]).each do |m|
            @members += [[m.type,m.id,m.role]]
        end
    end
    @members
  end

  def tags
    unless @tags
        @tags = Hash.new
        OldRelationTag.find(:all, :conditions => ["id = ? AND version = ?", self.id, self.version]).each do |tag|
            @tags[tag.k] = tag.v
        end
    end
    @tags = Hash.new unless @tags
    @tags
  end

  def members=(s)
    @members = s
  end

  def tags=(t)
    @tags = t
  end

#  has_many :relation_segments, :class_name => 'OldRelationSegment', :foreign_key => 'id'
#  has_many :relation_tags, :class_name => 'OldRelationTag', :foreign_key => 'id'

  def old_members
    OldRelationMember.find(:all, :conditions => ['id = ? AND version = ?', self.id, self.version])    
  end

  def old_tags
    OldRelationTag.find(:all, :conditions => ['id = ? AND version = ?', self.id, self.version])    
  end

  def to_xml
    doc = OSM::API.new.get_xml_doc
    doc.root << to_xml_node()
    return doc
  end

  def to_xml_node
    el1 = XML::Node.new 'relation'
    el1['id'] = self.id.to_s
    el1['visible'] = self.visible.to_s
    el1['timestamp'] = self.timestamp.xmlschema
    if self.changeset.user.data_public?
      el1['user'] = self.changeset.user.display_name
      el1['uid'] = self.changeset.user.id.to_s
    end
    el1['version'] = self.version.to_s
    el1['changeset'] = self.changeset_id.to_s
    
    self.old_members.each do |member|
      e = XML::Node.new 'member'
      e['type'] = member.member_type.to_s
      e['ref'] = member.member_id.to_s # "id" is considered uncool here as it should be unique in XML
      e['role'] = member.member_role.to_s
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

  # Temporary method to match interface to nodes
  def tags_as_hash
    return self.tags
  end

  # Temporary method to match interface to relations
  def relation_members
    return self.old_members
  end

  # Pretend we're not in any relations
  def containing_relation_members
    return []
  end
end
