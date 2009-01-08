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

def area_names(tags, type):
    """Fix for MassGIS OpenSpace areas: do better keying based on name, fix some common typos."""
    
    changed = False
    
    if type == "way" and 'name' in tags:
        name = tags['name']
        if name.endswith("Plgd"):
            name = name.replace("Plgd", "Playground")
            tags['name'] = name
            changed = True
        elif name.endswith("Playgroung"):
            name = name.replace("Playgroung", "Playground")
            tags['name'] = name
            changed = True
        
        if name.endswith("Golf Course"):
            if tags['leisure'] != "golf_course":
                changed = True
                tags['leisure'] = "golf_course"
        elif name.endswith("Park") or name.endswith("Playground") or name.endswith("Field"):
            if tags['leisure'] != "park":
                changed = True         
                tags['leisure'] = "park"

    tags['created_by'] = 'change_tags.py - massgis area_names %s' % __version__

    return changed    
