class Node:
    def __init__(self, id, latitude, longitude, timestamp):
        self.id = id
        self.lat = latitude
        self.lon = longitude
        self.timestamp = timestamp
        self.tags = {}

class Segment:
    def __init__(self, attrs):
        self.id = int(attrs["id"])
        self.from_node = int(attrs["from"])
        self.to_node = int(attrs["to"])
        self.tags = {}

class Way:
    def __init__(self, attrs):
        self.id = int(attrs["id"])
        self.segs = []
        self.tags = {}

