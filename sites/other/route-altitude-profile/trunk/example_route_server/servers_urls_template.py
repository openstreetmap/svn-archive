def set_url_server_root(server):
  if server == "pg":
    #return 'http://altitude-pg/';
    return 'http://altitude-pg.sprovoost.nl/';
  elif server == "app":
    #return 'http://localhost:8080/';
    return 'http://altitude.sprovoost.nl/';
  else:
    return 2

