class WayController < ApplicationController
  require 'xml/libxml'

  before_filter :authorize
  after_filter :compress_output

  def create
    response.headers["Content-Type"] = 'text/xml'
    if request.put?
      way = Way.from_xml(request.raw_post, true)

      if way
        way.user_id = @user.id

        unless way.preconditions_ok? # are the segments (and their nodes) visible?
          render :nothing => true, :status => HTTP_PRECONDITION_FAILED
          return
        end

        if way.save_with_history
          render :text => way.id.to_s
        else
          render :nothing => true, :status => 500
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
    response.headers["Content-Type"] = 'text/xml'
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
      if way.visible
        way.user_id = @user.id
        way.visible = false
        way.save_with_history
        render :nothing => true
      else
        render :nothing => true, :status => 410
      end

    when :put
      new_way = Way.from_xml(request.raw_post)

      if new_way
        unless new_way.preconditions_ok? # are the segments (and their nodes) visible?
          render :nothing => true, :status => HTTP_PRECONDITION_FAILED
          return
        end

        way.user_id = @user.id
        way.tags = new_way.tags
        way.segs = new_way.segs
        way.timestamp = new_way.timestamp
        way.visible = true

        if way.id == new_way.id and way.save_with_history
          render :nothing => true
        else
          render :nothing => true, :status => 500
        end
      else
        render :nothing => true, :status => 400 # if we got here the doc didnt parse
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

  def ways_for_segment
    response.headers["Content-Type"] = 'text/xml'
    wayids = WaySegment.find(:all, :conditions => ['segment_id = ?', params[:id]]).collect { |ws| ws.id }.uniq
    if wayids.length > 0
      waylist = Way.find(wayids)
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
