class WayController < ApplicationController
  require 'xml/libxml'

  before_filter :authorize
  after_filter :compress_output

  def create
    if request.put?
      way = Way.from_xml(request.raw_post, true)

      if way
        way.user_id = @user.id
        unless way.preconditions_ok? # are the segments (and their nodes) visible?
          render :nothing => true, :status => 412
          return
        end

        if way.save_with_history
          render :text => way.id.to_s
          return
        else
          render :nothing => true, :status => 500
          return
        end
        return
      else
        render :nothing => true, :status => 400 # if we got here the doc didnt parse
        return
      end
    end

    render :nothing => true, :status => 500 # something went very wrong
  end

  def full
    unless Way.exists?(params[:id])
      render :nothing => true, :status => 404
      return
    end

    way = Way.find(params[:id])

    unless way.visible
        render :nothing => true, :status => 410
        return
    end

	# In future, we might want to do all the data fetch in one step
	seg_ids = way.segs + [-1]
	segments = Segment.find_by_sql "select * from current_segments where visible = 1 and id IN (#{seg_ids.join(',')})"

	node_ids = segments.collect {|segment| segment.node_a }
	node_ids += segments.collect {|segment| segment.node_b }
	node_ids += [-1]
	nodes = Node.find(:all, :conditions => "visible = 1 AND id IN (#{node_ids.join(',')})")
	
	# Render
	doc = OSM::API.new.get_xml_doc
	nodes.each do |node|
		doc.root << node.to_xml_node()
	end
	segments.each do |segment|
		doc.root << segment.to_xml_node()
	end
	doc.root << way.to_xml_node()

    render :text => doc.to_s
  end

  def rest
    unless Way.exists?(params[:id])
      render :nothing => true, :status => 404
      return
    end

    way = Way.find(params[:id])
    case request.method

    when :get
      unless way.visible
        render :nothing => true, :status => 410
        return
      end
      render :text => way.to_xml.to_s

    when :delete
      unless way.visible
        render :nothing => true, :status => 410
        return
      end

      way.visible = false
      way.save_with_history
      render :nothing => true
      return
    when :put
      way = Way.from_xml(request.raw_post)

      if way
        way_in_db = Way.find(way.id)
        if way_in_db
          way_in_db.user_id = @user.id
          way_in_db.tags = way.tags
          way_in_db.segs = way.segs
          way_in_db.timestamp = way.timestamp
          way_in_db.visible = true
          if way_in_db.save_with_history
            render :text => way.id
          else
            render :nothing => true, :status => 500
          end
          return
        else
          render :nothing => true, :status => 404 # way doesn't exist yet
        end
      else
        render :nothing => true, :status => 400 # if we got here the doc didnt parse
        return
      end
    end
  end

  def ways
    response.headers["Content-Type"] = 'text/xml'
    ids = params['ways'].split(',').collect {|w| w.to_i }
    if ids.length > 0
      waylist = Way.find(ids)
      doc = OSM::API.new.get_xml_doc
      waylist.each do |way|
        doc.root << way.to_xml_node
      end 
      render :text => doc.to_s
    else
      render :nothing => true, :status => 400
    end
  end

end
