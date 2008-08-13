
class makeEnums:

  def addEquivalent(self, mainTag, k, v):
    self.equiv.append({
      'fn': lambda tag: tag.get(k,'')==v,
      'equiv': mainTag})
    
  def addSimple(self, name, k, v, options={}):
    self.enums[name] = {
      'name':name,
      'fn': lambda tag: tag.get(k,'') == v}
    
    if(options.has_key('linkroads')):
      self.addEquivalent(name, k, v+"_link")

  def __init__(self):
    self.enums = {}
    self.equiv = []

    # Big roads
    for t in ('motorway','primary','trunk','secondary','tertiary'):
      self.addSimple(t, 'highway', t, {'linkroads':True})
      
    # Normal roads
    for t in ('unclassified','track','service'):
      self.addSimple(t,'highway',t)

    # Things which aren't much different from normal roads
    for t in ('residential', 'road', 'living_street', 'raceway', 'urban'):
      self.addEquivalent('unclassified', 'highway', t)
    self.addEquivalent('track', 'highway', 'byway')

    # Paths
    for t in ('footway','bridleway','cycleway'):
      self.addSimple(t,'highway',t)
    for t in ('footpath','pedestrian','path','steps'):
      self.addEquivalent('footway', 'highway', t)
    
    # Rail
    for t in ('rail','subway'):
      self.addSimple(t,'railway',t)
    for t in ('light_rail', 'disused', 'abandoned', 'narrow_gauge', 'preserved'):
      self.addEquivalent('railway', 'railway', t)
    
    # Water
    for t in ('river', 'canal', 'stream'):
      self.addSimple(t,'waterway',t)
    self.addEquivalent('stream', 'waterway', 'drain')

    # Utilities
    self.addSimple('powerline','power','line')
    self.addSimple('pipeline','man_made','pipeline')

    # Airports
    self.addSimple('runway','aeroway','runway')
    self.addSimple('runway','aeroway','taxiway')

    # Wires
    self.addSimple('aerialway', 'aerialway','cable_car')
    for t in ('chair_lift','drag_lift'):
      self.addEquivalent('aerialway', 'aerialway', t)

    # Walls
    self.addSimple('wall','man_made','wall')
    for t in ('harbour_wall', 'city_wall', 'breakwater'):
      self.addEquivalent('wall', 'man_made', t)
    self.addEquivalent('wall', 'barrier', 'fence')

    # Natural features
    for t in ('cliff', 'coastline'):
      self.addSimple(t,'natural', t)
    

if(__name__ == "__main__"):
  a = makeEnums()
  for name in sorted(a.enums.keys()):
    print name
