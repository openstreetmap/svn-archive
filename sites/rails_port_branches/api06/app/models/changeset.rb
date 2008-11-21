class Changeset < ActiveRecord::Base
  require 'xml/libxml'

  belongs_to :user

  has_many :changeset_tags, :foreign_key => 'id'
  
  has_many :nodes
  has_many :ways
  has_many :relations
  has_many :old_nodes
  has_many :old_ways
  has_many :old_relations
  
  validates_presence_of :user_id, :created_at, :closed_at
  
  # over-expansion factor to use when updating the bounding box
  EXPAND = 0.1

  # maximum number of elements allowed in a changeset
  MAX_ELEMENTS = 50000

  # maximum time a changeset is allowed to be open for (note that this
  # is in days - so one hour is Rational(1,24)).
  MAX_TIME_OPEN = 1

  # idle timeout increment, one hour as a rational number of days.
  # NOTE: DO NOT CHANGE THIS TO 1.hour! when this was done the idle
  # timeout changed to 1 second, which meant all changesets closed 
  # almost immediately.
  IDLE_TIMEOUT = Rational(1,24)

  # Use a method like this, so that we can easily change how we
  # determine whether a changeset is open, without breaking code in at 
  # least 6 controllers
  def is_open?
    # a changeset is open (that is, it will accept further changes) when
    # it has not yet run out of time and its capacity is small enough.
    # note that this may not be a hard limit - due to timing changes and
    # concurrency it is possible that some changesets may be slightly 
    # longer than strictly allowed or have slightly more changes in them.
    return ((closed_at > DateTime.now) and (num_changes <= MAX_ELEMENTS))
  end

  def set_closed_time_now
    closed_at = DateTime.now
  end
  
  def self.from_xml(xml, create=false)
    begin
      p = XML::Parser.new
      p.string = xml
      doc = p.parse

      cs = Changeset.new

      doc.find('//osm/changeset').each do |pt|
        if create
          cs.created_at = Time.now
          # initial close time is 1h ahead, but will be increased on each
          # modification.
          cs.closed_at = Time.now + IDLE_TIMEOUT
          # initially we have no changes in a changeset
          cs.num_changes = 0
        end

        pt.find('tag').each do |tag|
          cs.add_tag_keyval(tag['k'], tag['v'])
        end
      end
    rescue Exception => ex
      cs = nil
    end

    return cs
  end

  ##
  # returns the bounding box of the changeset. it is possible that some
  # or all of the values will be nil, indicating that they are undefined.
  def bbox
    @bbox ||= [ min_lon, min_lat, max_lon, max_lat ]
  end

  ##
  # expand the bounding box to include the given bounding box. also, 
  # expand a little bit more in the direction of the expansion, so that
  # further expansions may be unnecessary. this is an optimisation 
  # suggested on the wiki page by kleptog.
  def update_bbox!(array)
    # ensure that bbox is cached and has no nils in it. if there are any
    # nils, just use the bounding box update to write over them.
    @bbox = bbox.zip(array).collect { |a, b| a.nil? ? b : a }

    # FIXME - this looks nasty and violates DRY... is there any prettier 
    # way to do this? 
    @bbox[0] = array[0] + EXPAND * (@bbox[0] - @bbox[2]) if array[0] < @bbox[0]
    @bbox[1] = array[1] + EXPAND * (@bbox[1] - @bbox[3]) if array[1] < @bbox[1]
    @bbox[2] = array[2] + EXPAND * (@bbox[2] - @bbox[0]) if array[2] > @bbox[2]
    @bbox[3] = array[3] + EXPAND * (@bbox[3] - @bbox[1]) if array[3] > @bbox[3]

    # update active record. rails 2.1's dirty handling should take care of
    # whether this object needs saving or not.
    self.min_lon, self.min_lat, self.max_lon, self.max_lat = @bbox
  end

  ##
  # the number of elements is also passed in so that we can ensure that
  # a single changeset doesn't contain too many elements. this, of course,
  # destroys the optimisation described in the bbox method above.
  def add_changes!(elements)
    self.num_changes += elements
  end

  def tags_as_hash
    return tags
  end

  def tags
    unless @tags
      @tags = {}
      self.changeset_tags.each do |tag|
        @tags[tag.k] = tag.v
      end
    end
    @tags
  end

  def tags=(t)
    @tags = t
  end

  def add_tag_keyval(k, v)
    @tags = Hash.new unless @tags
    @tags[k] = v
  end

  def save_with_tags!
    t = Time.now

    # do the changeset update and the changeset tags update in the
    # same transaction to ensure consistency.
    Changeset.transaction do
      # set the auto-close time to be one hour in the future unless
      # that would make it more than 24h long, in which case clip to
      # 24h, as this has been decided is a reasonable time limit.
      if (closed_at - created_at) > (MAX_TIME_OPEN - IDLE_TIMEOUT)
        self.closed_at = created_at + MAX_TIME_OPEN
      else
        self.closed_at = DateTime.now + IDLE_TIMEOUT
      end
      self.save!

      tags = self.tags
      ChangesetTag.delete_all(['id = ?', self.id])

      tags.each do |k,v|
        tag = ChangesetTag.new
        tag.k = k
        tag.v = v
        tag.id = self.id
        tag.save!
      end
    end
  end
  
  def to_xml
    doc = OSM::API.new.get_xml_doc
    doc.root << to_xml_node()
    return doc
  end
  
  def to_xml_node(user_display_name_cache = nil)
    el1 = XML::Node.new 'changeset'
    el1['id'] = self.id.to_s

    user_display_name_cache = {} if user_display_name_cache.nil?

    if user_display_name_cache and user_display_name_cache.key?(self.user_id)
      # use the cache if available
    elsif self.user.data_public?
      user_display_name_cache[self.user_id] = self.user.display_name
    else
      user_display_name_cache[self.user_id] = nil
    end

    el1['user'] = user_display_name_cache[self.user_id] unless user_display_name_cache[self.user_id].nil?
    el1['uid'] = self.user_id.to_s if self.user.data_public?

    self.tags.each do |k,v|
      el2 = XML::Node.new('tag')
      el2['k'] = k.to_s
      el2['v'] = v.to_s
      el1 << el2
    end
    
    el1['created_at'] = self.created_at.xmlschema
    el1['closed_at'] = self.closed_at.xmlschema unless is_open?
    el1['open'] = is_open?.to_s

    el1['min_lon'] = (bbox[0].to_f / GeoRecord::SCALE).to_s unless bbox[0].nil?
    el1['min_lat'] = (bbox[1].to_f / GeoRecord::SCALE).to_s unless bbox[1].nil?
    el1['max_lon'] = (bbox[2].to_f / GeoRecord::SCALE).to_s unless bbox[2].nil?
    el1['max_lat'] = (bbox[3].to_f / GeoRecord::SCALE).to_s unless bbox[3].nil?
    
    # NOTE: changesets don't include the XML of the changes within them,
    # they are just structures for tagging. to get the osmChange of a
    # changeset, see the download method of the controller.

    return el1
  end

  ##
  # update this instance from another instance given and the user who is
  # doing the updating. note that this method is not for updating the
  # bounding box, only the tags of the changeset.
  def update_from(other, user)
    # ensure that only the user who opened the changeset may modify it.
    unless user.id == self.user_id 
      raise OSM::APIUserChangesetMismatchError 
    end
    
    # can't change a closed changeset
    unless is_open?
      raise OSM::APIChangesetAlreadyClosedError.new(self)
    end

    # copy the other's tags
    self.tags = other.tags

    save_with_tags!
  end
end
