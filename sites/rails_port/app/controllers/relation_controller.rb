class RelationController < ApplicationController
  require 'xml/libxml'

  session :off
  before_filter :authorize, :only => [:create, :update, :delete]
  before_filter :require_public_data, :only => [:create, :update, :delete]
  before_filter :check_api_writable, :only => [:create, :update, :delete]
  before_filter :check_api_readable, :except => [:create, :update, :delete]
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

        # first find the ids of nodes, ways and relations referenced by this
        # relation - note that we exclude this relation just in case.

        node_ids = relation.members.select { |m| m[0] == 'Node' }.map { |m| m[1] }
        way_ids = relation.members.select { |m| m[0] == 'Way' }.map { |m| m[1] }
        relation_ids = relation.members.select { |m| m[0] == 'Relation' and m[1] != relation.id }.map { |m| m[1] }

        # next load the relations and the ways.

        relations = Relation.find(relation_ids, :include => [:relation_tags])
        ways = Way.find(way_ids, :include => [:way_nodes, :way_tags])

        # now additionally collect nodes referenced by ways. Note how we 
        # recursively evaluate ways but NOT relations.

        way_node_ids = ways.collect { |way|
           way.way_nodes.collect { |way_node| way_node.node_id }
        }
        node_ids += way_node_ids.flatten
        nodes = Node.find(node_ids.uniq, :include => :node_tags)
    
        # create XML.
        doc = OSM::API.new.get_xml_doc
        visible_nodes = {}
        visible_members = { "Node" => {}, "Way" => {}, "Relation" => {} }
        changeset_cache = {}
        user_display_name_cache = {}

        nodes.each do |node|
          if node.visible? # should be unnecessary if data is consistent.
            doc.root << node.to_xml_node(changeset_cache, user_display_name_cache)
            visible_nodes[node.id] = node
            visible_members["Node"][node.id] = true
          end
        end
        ways.each do |way|
          if way.visible? # should be unnecessary if data is consistent.
            doc.root << way.to_xml_node(visible_nodes, changeset_cache, user_display_name_cache)
            visible_members["Way"][way.id] = true
          end
        end
        relations.each do |rel|
          if rel.visible? # should be unnecessary if data is consistent.
            doc.root << rel.to_xml_node(nil, changeset_cache, user_display_name_cache)
            visible_members["Relation"][rel.id] = true
          end
        end
        # finally add self and output
        doc.root << relation.to_xml_node(visible_members, changeset_cache, user_display_name_cache)
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
    relations_for_object("Way")
  end
  def relations_for_node
    relations_for_object("Node")
  end
  def relations_for_relation
    relations_for_object("Relation")
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
