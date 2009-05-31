class WayController < ApplicationController
  require 'xml/libxml'

  session :off
  before_filter :authorize, :only => [:create, :update, :delete]
  before_filter :require_public_data, :only => [:create, :update, :delete]
  before_filter :check_api_writable, :only => [:create, :update, :delete]
  before_filter :check_api_readable, :except => [:create, :update, :delete]
  after_filter :compress_output
  around_filter :api_call_handle_error, :api_call_timeout

  def create
    assert_method :put

    way = Way.from_xml(request.raw_post, true)
    
    if way
      way.create_with_history @user
      render :text => way.id.to_s, :content_type => "text/plain"
    else
      render :nothing => true, :status => :bad_request
    end
  end

  def read
    way = Way.find(params[:id])
    
    response.headers['Last-Modified'] = way.timestamp.rfc822
    
    if way.visible
      render :text => way.to_xml.to_s, :content_type => "text/xml"
    else
      render :text => "", :status => :gone
    end
  end

  def update
    way = Way.find(params[:id])
    new_way = Way.from_xml(request.raw_post)
    
    if new_way and new_way.id == way.id
      way.update_from(new_way, @user)
      render :text => way.version.to_s, :content_type => "text/plain"
    else
      render :nothing => true, :status => :bad_request
    end
  end

  # This is the API call to delete a way
  def delete
    way = Way.find(params[:id])
    new_way = Way.from_xml(request.raw_post)
    
    if new_way and new_way.id == way.id
      way.delete_with_history!(new_way, @user)
      render :text => way.version.to_s, :content_type => "text/plain"
    else
      render :nothing => true, :status => :bad_request
    end
  end

  def full
    way = Way.find(params[:id], :include => {:nodes => :node_tags})
    
    if way.visible
      changeset_cache = {}
      user_display_name_cache = {}

      doc = OSM::API.new.get_xml_doc
      way.nodes.each do |node|
        if node.visible
          doc.root << node.to_xml_node(changeset_cache, user_display_name_cache)
        end
      end
      doc.root << way.to_xml_node(nil, changeset_cache, user_display_name_cache)
      
      render :text => doc.to_s, :content_type => "text/xml"
    else
      render :text => "", :status => :gone
    end
  end

  def ways
    begin
      ids = params['ways'].split(',').collect { |w| w.to_i }
    rescue
      ids = []
    end

    if ids.length > 0
      doc = OSM::API.new.get_xml_doc

      Way.find(ids).each do |way|
        doc.root << way.to_xml_node
      end

      render :text => doc.to_s, :content_type => "text/xml"
    else
      render :nothing => true, :status => :bad_request
    end
  end

  ##
  # returns all the ways which are currently using the node given in the 
  # :id parameter. note that this used to return deleted ways as well, but
  # this seemed not to be the expected behaviour, so it was removed.
  def ways_for_node
    wayids = WayNode.find(:all, 
                          :conditions => ['node_id = ?', params[:id]]
                          ).collect { |ws| ws.id[0] }.uniq

    doc = OSM::API.new.get_xml_doc

    Way.find(wayids).each do |way|
      doc.root << way.to_xml_node if way.visible
    end

    render :text => doc.to_s, :content_type => "text/xml"
  end
end
