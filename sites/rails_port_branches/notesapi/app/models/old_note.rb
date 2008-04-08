class OldNote < GeoRecord
  set_table_name 'notes'
  
  validates_presence_of :timestamp
  validates_inclusion_of :visible, :in => [ true, false ]
  validates_numericality_of :latitude, :longitude
  validate :validate_position

  def validate_position
    errors.add_to_base("Note is not in the world") unless in_world?
  end

  def in_world?
    return false if self.lat < -90 or self.lat > 90
    return false if self.lon < -180 or self.lon > 180
    return true
  end

  def self.from_note(note)
    old_note = OldNote.new
    old_note.latitude = note.latitude
    old_note.longitude = note.longitude
    old_note.visible = note.visible
    old_note.timestamp = note.timestamp
    old_note.date = note.date
    old_note.id = note.id
    old_note.summary = note.summary
    old_note.description = note.description
    old_note.status = note.status
    old_note.trace = note.trace
    old_note.node = note.node
    return old_note
  end

  def to_xml_note
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
