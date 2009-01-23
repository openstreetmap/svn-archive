class TraceController < ApplicationController
  layout 'site'

  before_filter :authorize_web
  before_filter :require_user, :only => [:mine, :create, :edit, :delete, :make_public]
  before_filter :authorize, :only => [:api_details, :api_data, :api_create]
  before_filter :check_database_availability, :except => [:api_details, :api_data, :api_create]
  before_filter :check_read_availability, :only => [:api_details, :api_data, :api_create]
 
  # Counts and selects pages of GPX traces for various criteria (by user, tags, public etc.).
  #  target_user - if set, specifies the user to fetch traces for.  if not set will fetch all traces
  def list(target_user = nil, action = "list")
    # from display name, pick up user id if one user's traces only
    display_name = params[:display_name]
    if target_user.nil? and !display_name.blank?
      target_user = User.find(:first, :conditions => [ "visible = ? and display_name = ?", true, display_name])
    end

    # set title
    if target_user.nil?
      @title = "Public GPS traces"
    elsif @user and @user == target_user
      @title = "Your GPS traces"
    else
      @title = "Public GPS traces from #{target_user.display_name}"
    end

    @title += " tagged with #{params[:tag]}" if params[:tag]

    # four main cases:
    # 1 - all traces, logged in = all public traces + all user's (i.e + all mine)
    # 2 - all traces, not logged in = all public traces
    # 3 - user's traces, logged in as same user = all user's traces 
    # 4 - user's traces, not logged in as that user = all user's public traces
    if target_user.nil? # all traces
      if @user
        conditions = ["(gpx_files.public = ? OR gpx_files.user_id = ?)", true, @user.id] #1
      else
        conditions  = ["gpx_files.public = ?", true] #2
      end
    else
      if @user and @user == target_user
        conditions = ["gpx_files.user_id = ?", @user.id] #3 (check vs user id, so no join + can't pick up non-public traces by changing name)
      else
        conditions = ["gpx_files.public = ? AND gpx_files.user_id = ?", true, target_user.id] #4
      end
    end
    
    if params[:tag]
      @tag = params[:tag]

      files = Tracetag.find_all_by_tag(params[:tag]).collect { |tt| tt.gpx_id }
      conditions[0] += " AND gpx_files.id IN (#{files.join(',')})"
    end
    
    conditions[0] += " AND gpx_files.visible = ?"
    conditions << true

    @trace_pages, @traces = paginate(:traces,
                                     :include => [:user, :tags],
                                     :conditions => conditions,
                                     :order => "gpx_files.timestamp DESC",
                                     :per_page => 20)

    # put together SET of tags across traces, for related links
    tagset = Hash.new
    if @traces
      @traces.each do |trace|
        trace.tags.reload if params[:tag] # if searched by tag, ActiveRecord won't bring back other tags, so do explicitly here
        trace.tags.each do |tag|
          tagset[tag.tag] = tag.tag
        end
      end
    end
    
    # final helper vars for view
    @action = action
    @display_name = target_user.display_name if target_user
    @all_tags = tagset.values
  end

  def mine
    list(@user, "mine")
  end

  def view
    @trace = Trace.find(params[:id])

    if @trace and @trace.visible? and
       (@trace.public? or @trace.user == @user)
      @title = "Viewing trace #{@trace.name}"
    else
      flash[:notice] = "Trace not found!"
      redirect_to :controller => 'trace', :action => 'list'
    end
  rescue ActiveRecord::RecordNotFound
    flash[:notice] = "Trace not found!"
    redirect_to :controller => 'trace', :action => 'list'
  end

  def create
    if params[:trace]
      logger.info(params[:trace][:gpx_file].class.name)
      if params[:trace][:gpx_file].respond_to?(:read)
        do_create(params[:trace][:gpx_file], params[:trace][:tagstring],
                  params[:trace][:description], params[:trace][:public])

        if @trace.id
          logger.info("id is #{@trace.id}")
          flash[:notice] = "Your GPX file has been uploaded and is awaiting insertion in to the database. This will usually happen within half an hour, and an email will be sent to you on completion."

          redirect_to :action => 'mine'
        end
      else
        @trace = Trace.new({:name => "Dummy",
                            :tagstring => params[:trace][:tagstring],
                            :description => params[:trace][:description],
                            :public => params[:trace][:public],
                            :inserted => false, :user => @user,
                            :timestamp => Time.now})
        @trace.valid?
        @trace.errors.add(:gpx_file, "can't be blank")
      end
    end
  end

  def data
    trace = Trace.find(params[:id])

    if trace.visible? and (trace.public? or (@user and @user == trace.user))
      if request.format == Mime::XML
        send_file(trace.xml_file, :filename => "#{trace.id}.xml", :type => Mime::XML.to_s, :disposition => 'attachment')
      else
        send_file(trace.trace_name, :filename => "#{trace.id}#{trace.extension_name}", :type => trace.mime_type, :disposition => 'attachment')
      end
    else
      render :nothing => true, :status => :not_found
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def edit
    @trace = Trace.find(params[:id])

    if @user and @trace.user == @user
      if params[:trace]
        @trace.description = params[:trace][:description]
        @trace.tagstring = params[:trace][:tagstring]
        if @trace.save
          redirect_to :action => 'view'
        end        
      end
    else
      render :nothing => true, :status => :forbidden
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def delete
    trace = Trace.find(params[:id])

    if @user and trace.user == @user
      if request.post? and trace.visible?
        trace.visible = false
        trace.save
        flash[:notice] = 'Track scheduled for deletion'
        redirect_to :controller => 'traces', :action => 'mine'
      else
        render :nothing => true, :status => :bad_request
      end
    else
      render :nothing => true, :status => :forbidden
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def make_public
    trace = Trace.find(params[:id])

    if @user and trace.user == @user
      if request.post? and !trace.public?
        trace.public = true
        trace.save
        flash[:notice] = 'Track made public'
        redirect_to :controller => 'trace', :action => 'view', :id => params[:id]
      else
        render :nothing => true, :status => :bad_request
      end
    else
      render :nothing => true, :status => :forbidden
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def georss
    conditions = ["gpx_files.public = ?", true]

    if params[:display_name]
      conditions[0] += " AND users.display_name = ?"
      conditions << params[:display_name]
    end

    if params[:tag]
      conditions[0] += " AND EXISTS (SELECT * FROM gpx_file_tags AS gft WHERE gft.gpx_id = gpx_files.id AND gft.tag = ?)"
      conditions << params[:tag]
    end

    traces = Trace.find(:all, :include => :user, :conditions => conditions, 
                        :order => "timestamp DESC", :limit => 20)

    rss = OSM::GeoRSS.new

    traces.each do |trace|
      rss.add(trace.latitude, trace.longitude, trace.name, trace.user.display_name, url_for({:controller => 'trace', :action => 'view', :id => trace.id, :display_name => trace.user.display_name}), "<img src='#{url_for({:controller => 'trace', :action => 'icon', :id => trace.id, :user_login => trace.user.display_name})}'> GPX file with #{trace.size} points from #{trace.user.display_name}", trace.timestamp)
    end

    render :text => rss.to_s, :content_type => "application/rss+xml"
  end

  def picture
    trace = Trace.find(params[:id])

    if trace.inserted?
      if trace.public? or (@user and @user == trace.user)
        send_file(trace.large_picture_name, :filename => "#{trace.id}.gif", :type => 'image/gif', :disposition => 'inline')
      else
        render :nothing => true, :status => :forbidden
      end
    else
      render :nothing => true, :status => :not_found
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def icon
    trace = Trace.find(params[:id])

    if trace.inserted?
      if trace.public? or (@user and @user == trace.user)
        send_file(trace.icon_picture_name, :filename => "#{trace.id}_icon.gif", :type => 'image/gif', :disposition => 'inline')
      else
        render :nothing => true, :status => :forbidden
      end
    else
      render :nothing => true, :status => :not_found
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def api_details
    trace = Trace.find(params[:id])

    if trace.public? or trace.user == @user
      render :text => trace.to_xml.to_s, :content_type => "text/xml"
    else
      render :nothing => true, :status => :forbidden
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def api_data
    trace = Trace.find(params[:id])

    if trace.public? or trace.user == @user
      send_file(trace.trace_name, :filename => "#{trace.id}#{trace.extension_name}", :type => trace.mime_type, :disposition => 'attachment')
    else
      render :nothing => true, :status => :forbidden
    end
  rescue ActiveRecord::RecordNotFound
    render :nothing => true, :status => :not_found
  end

  def api_create
    if request.post?
      do_create(params[:file], params[:tags], params[:description], params[:public])

      if @trace.id
        render :text => @trace.id.to_s, :content_type => "text/plain"
      elsif @trace.valid?
        render :nothing => true, :status => :internal_server_error
      else
        render :nothing => true, :status => :bad_request
      end
    else
      render :nothing => true, :status => :method_not_allowed
    end
  end

private

  def do_create(file, tags, description, public)
    name = file.original_filename.gsub(/[^a-zA-Z0-9.]/, '_')
    filename = "/tmp/#{rand}"

    File.open(filename, "w") { |f| f.write(file.read) }

    @trace = Trace.new({:name => name, :tagstring => tags,
                        :description => description, :public => public})
    @trace.inserted = false
    @trace.user = @user
    @trace.timestamp = Time.now

    if @trace.save
      FileUtils.mv(filename, @trace.trace_name)
    else
      FileUtils.rm_f(filename)
    end
    
    # Finally save whether the user marked the trace as being public
    if @trace.public?
      if @user.trace_public_default.nil?
        @user.preferences.create(:k => "gps.trace.public", :v => "default")
      end
    else
      pref = @user.trace_public_default
      pref.destroy unless pref.nil?
    end
    
  end

end
