# The ChangesetController is the RESTful interface to Changeset objects

class ChangesetController < ApplicationController
  require 'xml/libxml'
  require 'diff_reader'

  before_filter :authorize, :only => [:create, :update, :delete, :upload]
  before_filter :check_write_availability, :only => [:create, :update, :delete, :upload]
  before_filter :check_read_availability, :except => [:create, :update, :delete, :upload]
  after_filter :compress_output

  # Create a changeset from XML.
  def create
    if request.put?
      cs = Changeset.from_xml(request.raw_post, true)

      if cs
        cs.user_id = @user.id
        cs.save_with_tags!
        render :text => cs.id.to_s, :content_type => "text/plain"
      else
        render :nothing => true, :status => :bad_request
      end
    else
      render :nothing => true, :status => :method_not_allowed
    end
  end

  def create_prim(ids, prim, nd)
    prim.version = 0
    prim.user_id = @user.id
    prim.visible = true
    prim.save_with_history!

    ids[nd['id'].to_i] = prim.id
  end

  def fix_way(w, node_ids)
    w.nds = w.instance_eval { @nds }.
      map { |nd| node_ids[nd] || nd }
    return w
  end

  def fix_rel(r, ids)
    r.members = r.instance_eval { @members }.
      map { |memb| [memb[0], ids[memb[0]][memb[1].to_i] || memb[1], memb[2]] }
    return r
  end
  
  def read
    begin
      changeset = Changeset.find(params[:id])
      render :text => changeset.to_xml.to_s, :content_type => "text/xml"
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    end
  end
  
  def close 
    begin
      unless request.put?
        render :nothing => true, :status => :method_not_allowed
        return
      end
      changeset = Changeset.find(params[:id])
      changeset.open = false
      changeset.save!
      render :nothing => true
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    end
  end

  ##
  # Upload a diff in a single transaction.
  #
  # This means that each change within the diff must succeed, i.e: that
  # each version number mentioned is still current. Otherwise the entire
  # transaction *must* be rolled back.
  #
  # Furthermore, each element in the diff can only reference the current
  # changeset.
  #
  # Returns: a diffResult document, as described in 
  # http://wiki.openstreetmap.org/index.php/OSM_Protocol_Version_0.6
  def upload
    # only allow POST requests, as the upload method is most definitely
    # not idempotent, as several uploads with placeholder IDs will have
    # different side-effects.
    # see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.1.2
    unless request.post?
      render :nothing => true, :status => :method_not_allowed
      return
    end

    changeset = Changeset.find(params[:id])
    
    diff_reader = DiffReader.new(request.raw_post, changeset)
    Changeset.transaction do
      result = diff_reader.commit
      render :text => result.to_s, :content_type => "text/xml"
    end
    
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  rescue OSM::APIError => ex
    render ex.render_opts
  end

  ##
  # download the changeset as an osmChange document.
  #
  # to make it easier to revert diffs it would be better if the osmChange
  # format were reversible, i.e: contained both old and new versions of 
  # modified elements. but it doesn't at the moment...
  #
  # this method cannot order the database changes fully (i.e: timestamp and
  # version number may be too coarse) so the resulting diff may not apply
  # to a different database. however since changesets are not atomic this 
  # behaviour cannot be guaranteed anyway and is the result of a design
  # choice.
  def download
    changeset = Changeset.find(params[:id])
    
    # get all the elements in the changeset and stick them in a big array.
    elements = [changeset.old_nodes, 
                changeset.old_ways, 
                changeset.old_relations].flatten
    
    # sort the elements by timestamp and version number, as this is the 
    # almost sensible ordering available. this would be much nicer if 
    # global (SVN-style) versioning were used - then that would be 
    # unambiguous.
    elements.sort! do |a, b| 
      if (a.timestamp == b.timestamp)
        a.version <=> b.version
      else
        a.timestamp <=> b.timestamp 
      end
    end
    
    # create an osmChange document for the output
    result = OSM::API.new.get_xml_doc
    result.root.name = "osmChange"

    # generate an output element for each operation. note: we avoid looking
    # at the history because it is simpler - but it would be more correct to 
    # check these assertions.
    elements.each do |elt|
      result.root <<
        if (elt.version == 1) 
          # first version, so it must be newly-created.
          created = XML::Node.new "create"
          created << elt.to_xml_node
        else
          # get the previous version from the element history
          prev_elt = elt.class.find(:first, :conditions => 
                                    ['id = ? and version = ?',
                                     elt.id, elt.version])
          unless elt.visible
            # if the element isn't visible then it must have been deleted, so
            # output the *previous* XML
            deleted = XML::Node.new "delete"
            deleted << prev_elt.to_xml_node
          else
            # must be a modify, for which we don't need the previous version
            # yet...
            modified = XML::Node.new "modify"
            modified << elt.to_xml_node
          end
        end
    end

    render :text => result.to_s, :content_type => "text/xml"
            
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  rescue OSM::APIError => ex
    render ex.render_opts
  end

end
