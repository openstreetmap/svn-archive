__version__ = "0.1"

def conservation_organization(tags, type):
    """Fix for a MassGIS OpenSpace todo: typo in 'conservation_organization'
       in bulk import.
    """
    
    if type == "node":
        return False 
    
    changed = False
    
    if 'ownership' in tags and tags['ownership'] == 'conservation_rganization':
        tags['ownership'] = 'conservation_organization'
        changed = True
    
    tags['created_by'] = 'change_tags.py - massgis %s' % __version__

    return changed    

def playgrounds(tags, type):
    changed = False
    
    if type == "way" and 'name' in tags:
        name = tags['name']
    
        if name.endswith("Playground"):
            if not 'leisure' in tags or tags['leisure'] != "playground":
                changed = True
                tags['leisure'] = 'playground'
    tags['created_by'] = 'change_tags.py - massgis area_names %s' % __version__

    return changed    

def more_shrt_nms(tags, type):
    import re
    map = {
      " Rec Area$": ' Recreation Area', 
      " Nwr$": ' National Wildlife Refuge', 
      " Wma$": ' Wildlife Management Area', 
      " Nha$": ' Natural Heritage Area', 
      " Wce$": ' Wildlife Conservation Easement', 
      " Mdws$": ' Meadows', 
      " Reserv\.$": ' Reservation', 
      " St Forest$": ' State Forest', 
      " Conservation La$": ' Conservation Land', 
      " Cons Lnd$": ' Conservation Land', 
      " Cons Land$": ' Conservation Land', 
      " Cons Area$": ' Conservation Area', 
      " Consv Land$": ' Conservation Land', 
      " Sanc$": ' Sanctuary', 
      " Consv Prjt$": ' Conservation Project',
      " Pk$": ' Park',
      " Conserv\. Land$": " Conservation Land"
    }
    remap = {}
    for key, value in map.items():
        pat = re.compile(key)
        remap[pat] = value
        
    changed = False
    
    if type == "way" and 'name' in tags:
        name = tags['name']
        for key, value in remap.items():
            new_name = key.sub(value, name)
            if new_name != name:
                tags['name'] = new_name
                changed = True
                break
         
    tags['created_by'] = 'change_tags.py - massgis short_names %s' % __version__

    return changed    


def area_names(tags, type):
    """Fix for MassGIS OpenSpace areas: do better keying based on name, fix some common typos."""
    
    changed = False
    
    if type == "way" and 'name' in tags:
        name = tags['name']
        
        # Common playground typos
        if name.endswith("Plgd"):
            name = name.replace("Plgd", "Playground")
            tags['name'] = name
            changed = True
        elif name.endswith("Plygrd"):
            name = name.replace("Plygrd", "Playground")
            tags['name'] = name
            changed = True
        elif name.endswith("Playgroung"):
            name = name.replace("Playgroung", "Playground")
            tags['name'] = name
            changed = True
        
        if name.endswith("Golf Course"):
            if not 'leisure' in tags or tags['leisure'] != "golf_course":
                changed = True
                tags['leisure'] = "golf_course"
                if 'landuse' in tags:
                    del tags['landuse']
        elif name.endswith("State Park"):
            if not 'leisure' in tags or tags['leisure'] != "nature_reserve":
                changed = True
                tags['leisure'] = 'nature_reserve'
                
        elif (name.endswith("Park") and tags['massgis:FEE_OWNER'] != "National Park Service") \
                or name.endswith("Playground") or name.endswith("Field"):
            if not 'leisure' in tags or tags['leisure'] != "park":
                changed = True         
                tags['leisure'] = "park"

    tags['created_by'] = 'change_tags.py - massgis area_names %s' % __version__

    return changed    

def ramps(tags, type):
    import re

    changed = False
    
    if type == "way" and 'name' in tags and 'highway' in tags:
        name = tags['name']
        status = tags['highway']
    
        # re.match checks for a match only at the beginning of the string
        if re.match("Ramp\W", name):
            tags['orig_name'] = name
            del tags['name']
            tags['ramp'] = "yes"

            if status == 'motorway' or status == 'motorway_link':
	        tags['highway'] = 'motorway_link'
            elif status == 'trunk' or status == 'trunk_link':
                tags['highway'] = 'trunk_link'
            elif status == 'primary' or status == 'primary_link':
                tags['highway'] = 'primary_link'
            elif status == 'secondary' or status == 'secondary_link':
                tags['highway'] = 'secondary_link'
            elif status == 'tertiary' or status == 'tertiary_link':
                tags['highway'] = 'tertiary_link'
            else:
                raise Exception("Found a highway with unhandled type: %s" % status)

            changed = True

    tags['created_by'] = 'change_tags.py - massgis ramps %s' % __version__

    return changed   

def clear_node_tags(tags, type):
    changed = False
    if 'attribution' in tags and 'source' in tags:
        if tags['attribution'] == 'Office of Geographic and Environmental Information (MassGIS)' and len(tags) == 2 or (len(tags) == 3 and 'created_by' in tags):
            tags = {}
            changed = True
    return changed        
