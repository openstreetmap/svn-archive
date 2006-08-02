
module OSM

  def test uri, queries, session
    session << "HTTP/1.1 200/OK\r\nServer: little-osm\r\nContent-type: text/plain\r\n\r\n"
    session << "little-osm is up and running. API version 0.3\n"
    session << "URL: #{Thread.current['uri'].to_s}"
    session << "From: #{Thread.current['session'].peeraddr[2]}:#{Thread.current['session'].peeraddr[1]}"
  end

end

