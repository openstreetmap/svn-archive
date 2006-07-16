#!/usr/bin/ruby

# script that assumes an empty database and tests the correctness of the API.
# It is intended to run on a testserver. Mysql database access to the database
# behind the API is needed.

# WARNING: The database may be screwed up. DO NOT RUN THIS SCRIPT ON THE PRODUCTION SERVER!

# This script just a start, more tests need to be added (as soon as dao.rb and the mysql-schema are in sync again...)

require "test/unit"
require "test/unit/assertions"
require "mysql"
require "md5"
require "net/http"
require "rexml/document"
require "../www.openstreetmap.org/ruby/api/osm/servinfo"

# get little-osm's data structure
$: << "../utils/little-osm"
require "data/core"
require "data/xml"

class Api0_3Test < Test::Unit::TestCase

  def setup
    # Replace database with empty one
    @server = Mysql.real_connect $DBSERVER, $USERNAME, $PASSWORD
    @server.query "drop database #{$DATABASE};"
    @server.query "create database #{$DATABASE};"
    @server.select_db $DATABASE
    `mysql -u#{$USERNAME} #{"-p"+$PASSWORD unless $PASSWORD.empty?} #{$DATABASE} < ../sql/empty-foo@bar.baz-foobar.dump`

    # prepare http connection to server
  	@http = Net::HTTP.start 'localhost'
  end

  def teardown
    @server.close
    @http.finish
  end

  def query str
    result = []
    @server.query str do |q|
      q.each { |row| result << row }
    end
    result
  end

  def GET path
    req = Net::HTTP::Get.new "/api/0.3/"+path
    req.basic_auth 'foo@bar.baz', 'foobar'
    @http.request req
  end

  def PUT path, osm 
    req = Net::HTTP::Put.new "/api/0.3/"+path
    req.basic_auth 'foo@bar.baz', 'foobar'
    s = "<?xml version='1.0' encoding='UTF-8'?>\n<osm version='0.3' generator='testserver.rb'>"
    s += osm.to_xml.to_s
    s += "</osm>"
    req.body = s
    @http.request req
  end

  def parse resp
    assert_equal "200", resp.code
    OsmPrimitive.from_xml resp.body
  end

  #just verify, that the setup/teardown works and the empty script is ok
  def test_db_emptyness
    user = query "select email, pass_crypt, active, id from users;"
    assert_equal 1, user.size
    assert_equal ["foo@bar.baz", MD5.hexdigest("foobar"), "1", "1"], user[0]

    %W{nodes segments ways areas}.each do |el|
      table = query "select COUNT(*) from #{el};"
      assert_equal 0, table[0][0].to_i, "#{el} is empty"
    end
  end

  def test_node
    assert_equal "404", GET("node/1").code, "No node before anything"

    insert = PUT "node/0", Node.new(23.0, 42.0)
    assert_equal "200", insert.code

    new_id = insert.body.to_i
    assert new_id != 0

    node = parse(GET("node/#{new_id}"))
    assert_equal new_id, node.id
  end

end
