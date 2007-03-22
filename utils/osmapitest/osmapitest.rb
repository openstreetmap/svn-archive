# hello_test.rb

module PMAPITEST

require 'test/unit'
require 'net/http'
require 'bboxfrompoint'
include Net

class MapTest# < Test::Unit::TestCase

  def initialize(test_name, range, diam_range)
    @test_name = test_name
    @range = range
    @diam_range = diam_range
  end

  # called before every single test
  def setup
    @var1 = 'value1'
    @var2 = 42
  end
  
  @@urlbase = '/api/0.4/'

  # called after every single test
  def teardown
  end

  def test_get_map
    assert true
  end

  def get_node
   
    Net::HTTP.start('www.peoplesmap.com') do |http|
        req = Net::HTTP::Get.new(@@urlbase+'node/40')
        req.basic_auth 'nickblack1@gmail.com', 'test'
        response = http.request(req)
        print response.body
     end
  end

  def create_test_dir

    t = Time.now

    `mkdir /tmp/#{@test_name}/` unless File.exists?("/tmp/#{@test_name}")

    raise $?.to_s if $?.nil? 

    return "/tmp/#{@test_name}/#{t.strftime("%H-%M-%S-%d-%B-%y")}"
  end

  def generate_map_request(params)

    x = params[0]
    y = params[1]
    d = params[2]

    bbox = BboxFromPoint.new.bbox_from_point(x,y,d)
    outdir = create_test_dir
    begin
      `curl -i -u test1@peoplesmap.com:test1 http://www.peoplesmap.com/api/0.4/#{bbox} > #{outdir}`
    rescue SystemCallError
      $stderr.print "Bbox request failed at #{outdir}" + $!
    end

    return "Bbox request made to #{outdir} - centreX: #{x} centreY: #{y} diameter: #{d}"
  end


  def get_range
    xmin = @range[0]
    xmax = @range[1]
    ymin = @range[2]
    ymax = @range[3]
    dmin = @diam_range[0]
    dmax = @diam_range[1]
    res = [] 
    res[0] = (rand(xmax - xmin)) + xmin
    res[1] = (rand(ymax - ymin)) + ymin
    res[2] = (rand(dmax - dmin)) + dmin
    
    return res
   end

   def map_test(attempts)

    n = 0
    if attempts == -1
        while n < 1
          generate_map_request(get_range)
          puts "#{generate_map_request(get_range)}"
        end
    else
        while n < attempts
          generate_map_request(get_range)
          n+=1
        end
    end
  end
end

  class CreateTest

    #Bbox = [xmin, xmax, ymin, ymax]
    def initialize(bbox, urlbase, user, pass)
      @bbox = bbox
      @urlbase = urlbase
      @user = user
      @pass = pass
    end

    def get_from_api(url_extension)

      Net::HTTP.start(@urlbase) do |http|
        req = Net::HTTP::Get.new(url_extension)
        req.basic_auth @user, @pass
        response = http.request(req)
        @res =  response.body
      end
      return @res
    end

    def put_to_api(url_extension, xml_to_put)

      Net::HTTP.start(@urlbase) do |http|
        req = Net::HTTP::Put.new(url_extension)
        req.basic_auth @user, @pass
        response = http.request(req, xml_to_put)
        @res =  response.body 
      end
      return @res
    end

    #Returns the xml for a new node
    def node_to_xml(x,y)
      return "<pmx> <node id='0' x='#{x}' y='#{y}' /></pmx>"
    end 

    def seg_to_xml(from_node, to_node)

      seg_xml = "<pmx version=\"0.4\" generator=\"PeoplesMap server\"> 
                    <segment id=\"0\" from=\"#{from_node}\" to=\"#{to_node}\" visible=\"true\" timestamp=\"#{Time.now}\"> 
                    </segment>
                 </pmx>"

    return seg_xml
    end

    def way_to_xml(seg_id)
      way_xml_body = ""
      way_xml_head = "<pmx version=\"0.4\" generator=\"PeoplesMap server\">
       <way id=\"0\" visible=\"true\" timestamp=\"#{Time.now}\">"

      seg_id.each do |seg|
        way_xml_body << "\n <seg id=\"#{seg}\"/>"
      end

      way_tags = "<tag k=\"name\" v=\"foo\"/>
         <tag k=\"ref\" v=\"bar\"/> "

       way_xml = way_xml_head + way_xml_body + way_tags + "</way></pmx>"

       return way_xml
    end
 
    #Creates new nodes and puts them to the server via the API
    #Retuns an array of new node objects
    #@@Params: n = number of nodes to create
    #@@Params: xinc = x incrementation in meters
    def makenode(n, xinc, yinc)
      x = @bbox[0]
      y = @bbox[2]
      nodes = []
      i = 0
      api_ext = '/api/0.4/node/create'

      while i < n 
          unless x > @bbox[1] or y > @bbox[3]
            begin
              node_id =  put_to_api(api_ext, node_to_xml(x,y))
              puts "Created a node with node id #{node_id}"
              nodes << [node_id,x,y]
              x = x + xinc
              y = y + yinc
            rescue Net::HTTPServerException
              $stderr.print "#{Net::HTTPServerException} whilst attempting to PUT #{node_to_xml(x,y)} to #{api_ext}" 
            ensure
              i+=1
            end
         end
      end
      return nodes 
    end

    #Creates a new segment object and returns an array of segment ids
    #@@Params: array of node objects
    def makeseg(nodes)

    first = true
    segs = []
    api_ext = '/api/0.4/segment/create'

    node_a = nodes[0][0]
    nodes.shift
    nodes.each do |node|
        node_b = node[0]
#        puts "node a is #{node_a} node_b is #{node_b}"
        begin
          seg_id = put_to_api(api_ext,seg_to_xml(node_a, node_b)) 
          puts "Created a segment with id #{seg_id}"
          segs << [seg_id]
          raise "SegIdError" if seg_id == 0 or seg_id.nil?
        rescue Net::HTTPServerException
            $stderr.print "#{Net::HTTPServerException} whilst attempting to PUT #{seg_to_xml(node_a, node_b)} to #{api_ext}"
          end
          node_a = node_b
        end
      
    return segs 
    end

    #Returns a new way object
    #@@Params: array of segments
    def makeway(segs)

    api_ext = '/api/0.4/way/create'
    ways = []

    
      begin
        way_id = put_to_api(api_ext,way_to_xml(segs))
        puts way_to_xml(segs)
        raise "WayIdError" if way_id == 0 or way_id.nil?
        puts "Created a way with id #{way_id}"
        ways << way_id
      rescue Net::HTTPServerException
        $stderr.print "#{Net::HTTPServerException} whilst attempting to PUT #{way_to_xml(segs)} to #{api_ext}"
      rescue Net::HTTPFatalError
        $stderr.print "#{Net::HTTPFatalError} whilst attempting to PUT #{way_to_xml(segs)} to #{api_ext}"
      end

    return ways
      

    end
end
end


