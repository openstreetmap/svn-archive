class OldWayController < ApplicationController
  require 'xml/libxml'

  session :off
  before_filter :check_api_readable
  after_filter :compress_output
  around_filter :api_call_handle_error, :api_call_timeout

  def history
    way = Way.find(params[:id])
    
    doc = OSM::API.new.get_xml_doc
    
    way.old_ways.each do |old_way|
      doc.root << old_way.to_xml_node
    end
    
    render :text => doc.to_s, :content_type => "text/xml"
  end
  
  def version
    old_way = OldWay.find(:first, :conditions => {:id => params[:id], :version => params[:version]} )
    if old_way.nil?
      # (RecordNotFound is not raised with find :first...)
      render :nothing => true, :status => :not_found
      return
    end
    
    response.headers['Last-Modified'] = old_way.timestamp.rfc822
    
    doc = OSM::API.new.get_xml_doc
    doc.root << old_way.to_xml_node
    
    render :text => doc.to_s, :content_type => "text/xml"
  end
end
