class RelationController < ApplicationController
  require 'xml/libxml'

  session :off
  before_filter :authorize, :only => [:create, :update, :delete]
  before_filter :check_write_availability, :only => [:create, :update, :delete]
  before_filter :check_read_availability, :except => [:create, :update, :delete]
  after_filter :compress_output

  def create
    begin
      if request.put?
        relation = Relation.from_xml(request.raw_post, true)

        # We assume that an exception has been thrown if there was an error 
        # generating the relation
        #if relation
          relation.create_with_history @user
          render :text => relation.id.to_s, :content_type => "text/plain"
        #else
         # render :text => "Couldn't get turn the input into a relation.", :status => :bad_request
        #end
      else
        render :nothing => true, :status => :method_not_allowed
      end
    rescue OSM::APIError => ex
      render ex.render_opts
    end
  end

  def read
    begin
      relation = Relation.find(params[:id])
      response.headers['Last-Modified'] = relation.timestamp.rfc822
      if relation.visible
        render :text => relation.to_xml.to_s, :content_type => "text/xml"
      else
        render :text => "", :status => :gone
      end
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    rescue
      render :nothing => true, :status => :internal_server_error
    end
  end

  def update
    logger.debug request.raw_post
    begin
      relation = Relation.find(params[:id])
      new_relation = Relation.from_xml(request.raw_post)

      if new_relation and new_relation.id == relation.id
        relation.update_from new_relation, @user
        render :text => relation.version.to_s, :content_type => "text/plain"
      else
        render :nothing => true, :status => :bad_request
      end
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    rescue OSM::APIError => ex
      render ex.render_opts
    end
  end

  def delete
    begin
      relation = Relation.find(params[:id])
      new_relation = Relation.from_xml(request.raw_post)
      if new_relation and new_relation.id == relation.id
        relation.delete_with_history!(new_relation, @user)
        render :text => relation.version.to_s, :content_type => "text/plain"
      else
        render :nothing => true, :status => :bad_request
      end
    rescue OSM::APIError => ex
      render ex.render_opts
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    end
  end

  # -----------------------------------------------------------------
  # full
  # 
  # input parameters: id
  #
  # returns XML representation of one relation object plus all its
  # members, plus all nodes part of member ways
  # -----------------------------------------------------------------
  def full
    begin
      relation = Relation.find(params[:id])

      if relation.visible

        # first collect nodes, ways, and relations referenced by this relation.
        
        ways = Way.find_by_sql("select w.* from current_ways w,current_relation_members rm where "+
            "rm.member_type='way' and rm.member_id=w.id and rm.id=#{relation.id}");
        nodes = Node.find_by_sql("select n.* from current_nodes n,current_relation_members rm where "+
            "rm.member_type='node' and rm.member_id=n.id and rm.id=#{relation.id}");
        # note query is built to exclude self just in case.
        relations = Relation.find_by_sql("select r.* from current_relations r,current_relation_members rm where "+
            "rm.member_type='relation' and rm.member_id=r.id and rm.id=#{relation.id} and r.id<>rm.id");

        # now additionally collect nodes referenced by ways. Note how we recursively 
        # evaluate ways but NOT relations.

        node_ids = nodes.collect {|node| node.id }
        way_node_ids = ways.collect { |way|
           way.way_nodes.collect { |way_node| way_node.node_id }
        }
        way_node_ids.flatten!
        way_node_ids.uniq
        way_node_ids -= node_ids
        nodes += Node.find(way_node_ids)
    
        # create XML.
        doc = OSM::API.new.get_xml_doc
        visible_nodes = {}
        user_display_name_cache = {}

        nodes.each do |node|
          if node.visible? # should be unnecessary if data is consistent.
            doc.root << node.to_xml_node(user_display_name_cache)
            visible_nodes[node.id] = node
          end
        end
        ways.each do |way|
          if way.visible? # should be unnecessary if data is consistent.
            doc.root << way.to_xml_node(visible_nodes, user_display_name_cache)
          end
        end
        relations.each do |rel|
          if rel.visible? # should be unnecessary if data is consistent.
            doc.root << rel.to_xml_node(user_display_name_cache)
          end
        end
        # finally add self and output
        doc.root << relation.to_xml_node(user_display_name_cache)
        render :text => doc.to_s, :content_type => "text/xml"

      else
        render :nothing => true, :status => :gone
      end

    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found

    rescue
      render :nothing => true, :status => :internal_server_error
    end
  end

  def relations
    ids = params['relations'].split(',').collect { |w| w.to_i }

    if ids.length > 0
      doc = OSM::API.new.get_xml_doc

      Relation.find(ids).each do |relation|
        doc.root << relation.to_xml_node
      end

      render :text => doc.to_s, :content_type => "text/xml"
    else
      render :text => "You need to supply a comma separated list of ids.", :status => :bad_request
    end
  rescue ActiveRecord::RecordNotFound
    render :text => "Could not find one of the relations", :status => :not_found
  end

  def relations_for_way
    relations_for_object("way")
  end
  def relations_for_node
    relations_for_object("node")
  end
  def relations_for_relation
    relations_for_object("relation")
  end

  def relations_for_object(objtype)
    relationids = RelationMember.find(:all, :conditions => ['member_type=? and member_id=?', objtype, params[:id]]).collect { |ws| ws.id[0] }.uniq

    doc = OSM::API.new.get_xml_doc

    Relation.find(relationids).each do |relation|
      doc.root << relation.to_xml_node if relation.visible
    end

    render :text => doc.to_s, :content_type => "text/xml"
  end
end
