require "yaml"

module OSM

  def test uri, queries, session
    puts session
    session << "HTTP/1.1 200/OK\r\nServer: little-osm\r\nContent-type: text/plain\r\n\r\n"
    session << "little-osm is up and running. API version 0.3\n"
    session << "URL: #{uri.to_s}\n"
    session << "From: #{session.peeraddr[2]}:#{session.peeraddr[1]}\n"
    session << "Queries:\n"
    session << queries.to_yaml.to_s << "\n"
  end

end

