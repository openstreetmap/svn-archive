# The note model represents a current existing note, that is, the latest version. Use OldNote for historical notes.

class Note < GeoRecord
  require 'xml/libxml'

  set_table_name 'current_notes'
  
  validates_presence_of :timestamp
  validates_inclusion_of :visible, :in => [ true, false ]
  validates_numericality_of :latitude, :longitude
  validate :validate_position

  belongs_to :trace
  belongs_to :node

  # Sanity check the latitude and longitude and add an error if it's broken
  def validate_position
    errors.add_to_base("Note is not in the world") unless in_world?
  end

  #
  # Search for notes within bounding_box
  #
  def self.search(bounding_box)
    min_lon, min_lat, max_lon, max_lat = *bounding_box
    find_by_area(min_lat, min_lon, max_lat, max_lon, :conditions => 'visible = 1')
  end

  # Read in xml as text and return it's Node object representation
  def self.from_xml(xml, create=false)
    begin
      p = XML::Parser.new
      p.string = xml
      doc = p.parse
  
      note = Note.new

      doc.find('//osm/note').each do |pt|
        note.lat = pt['lat'].to_f
        note.lon = pt['lon'].to_f

        return nil unless note.in_world?

        unless create
          if pt['id'] != '0'
            note.id = pt['id'].to_i
          end
        end

        note.visible = pt['visible'] and pt['visible'] == 'true'

        note.summary = pt['summary']
	if pt['description']
	  note.description = pt['description']
        end
	if pt['email']
	  note.email = pt['email']
        end
	if pt['status']
	  note.status = pt['status']
        end
	if pt['trace']
	  note.trace = pt['trace']
        end
	if pt['node']
	  note.node = pt['node']
        end
	
        if pt['date']
	  note.date = Time.parse(pt['date'])
	end
        if pt['timestamp']
	  note.timestamp = Time.parse(pt['timestamp'])
        else
	  if create
            note.timestamp = Time.now
	  end
	end
	
      end
    rescue
      note = nil
    end

    return note
  end

  # Save this node with the appropriate OldNode object to represent it's history.
  def save_with_history!
    Note.transaction do
      self.timestamp = Time.now
      self.save!
      old_note = OldNote.from_note(self)
      old_note.save!
    end
  end

  # Turn this Node in to a complete OSM XML object with <osm> wrapper
  def to_xml
    doc = OSM::API.new.get_xml_doc
    doc.root << to_xml_note()
    return doc
  end

  # Turn this Note in to an XML Note without the <osm> wrapper.
  def to_xml_note(user_display_name_cache = nil)
    el1 = XML::Node.new 'note'
    el1['id'] = self.id.to_s
    el1['lat'] = self.lat.to_s
    el1['lon'] = self.lon.to_s
    el1['summary'] = self.summary
    el1['description'] = self.description
    el1['status'] = self.status
    el1['trace'] = self.trace
    el1['node'] = self.node
    el1['visible'] = self.visible.to_s
    el1['date'] = self.date.xmlschema
    el1['timestamp'] = self.timestamp.xmlschema
    return el1
  end
end
