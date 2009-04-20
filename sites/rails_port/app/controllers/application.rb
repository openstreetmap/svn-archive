# Filters added to this controller will be run for all controllers in the application.
# Likewise, all the methods added will be available for all controllers.
class ApplicationController < ActionController::Base

  if OSM_STATUS == :database_readonly or OSM_STATUS == :database_offline
    session :off
  end

  def authorize_web
    if session[:user]
      @user = User.find(session[:user], :conditions => {:visible => true})
    elsif session[:token]
      @user = User.authenticate(:token => session[:token])
      session[:user] = @user.id
    end
  rescue Exception => ex
    logger.info("Exception authorizing user: #{ex.to_s}")
    @user = nil
  end

  def require_user
    redirect_to :controller => 'user', :action => 'login', :referer => request.request_uri unless @user
  end

  ##
  # sets up the @user object for use by other methods. this is mostly called
  # from the authorize method, but can be called elsewhere if authorisation
  # is optional.
  def setup_user_auth
    username, passwd = get_auth_data # parse from headers
    # authenticate per-scheme
    if username.nil?
      @user = nil # no authentication provided - perhaps first connect (client should retry after 401)
    elsif username == 'token' 
      @user = User.authenticate(:token => passwd) # preferred - random token for user from db, passed in basic auth
    else
      @user = User.authenticate(:username => username, :password => passwd) # basic auth
    end
  end

  def authorize(realm='Web Password', errormessage="Couldn't authenticate you") 
    # make the @user object from any auth sources we have
    setup_user_auth

    # handle authenticate pass/fail
    unless @user
      # no auth, the user does not exist or the password was wrong
      response.headers["Status"] = "Unauthorized" 
      response.headers["WWW-Authenticate"] = "Basic realm=\"#{realm}\"" 
      render :text => errormessage, :status => :unauthorized
      return false
    end 
  end 

  def check_database_readable(need_api = false)
    if OSM_STATUS == :database_offline or (need_api and OSM_STATUS == :api_offline)
      redirect_to :controller => 'site', :action => 'offline'
    end
  end

  def check_database_writable(need_api = false)
    if OSM_STATUS == :database_offline or OSM_STATUS == :database_readonly or
       (need_api and (OSM_STATUS == :api_offline or OSM_STATUS == :api_readonly))
      redirect_to :controller => 'site', :action => 'offline'
    end
  end

  def check_api_readable
    if OSM_STATUS == :database_offline or OSM_STATUS == :api_offline
      response.headers['Error'] = "Database offline for maintenance"
      render :nothing => true, :status => :service_unavailable
      return false
    end
  end

  def check_api_writable
    if OSM_STATUS == :database_offline or OSM_STATUS == :database_readonly or
       OSM_STATUS == :api_offline or OSM_STATUS == :api_readonly
      response.headers['Error'] = "Database offline for maintenance"
      render :nothing => true, :status => :service_unavailable
      return false
    end
  end

  def require_public_data
    unless @user.data_public?
      response.headers['Error'] = "You must make your edits public to upload new data"
      render :nothing => true, :status => :forbidden
      return false
    end
  end

  # Report and error to the user
  # (If anyone ever fixes Rails so it can set a http status "reason phrase",
  #  rather than only a status code and having the web engine make up a 
  #  phrase from that, we can also put the error message into the status
  #  message. For now, rails won't let us)
  def report_error(message)
    render :text => message, :status => :bad_request
    # Todo: some sort of escaping of problem characters in the message
    response.headers['Error'] = message
  end

private 

  # extract authorisation credentials from headers, returns user = nil if none
  def get_auth_data 
    if request.env.has_key? 'X-HTTP_AUTHORIZATION'          # where mod_rewrite might have put it 
      authdata = request.env['X-HTTP_AUTHORIZATION'].to_s.split 
    elsif request.env.has_key? 'REDIRECT_X_HTTP_AUTHORIZATION'          # mod_fcgi 
      authdata = request.env['REDIRECT_X_HTTP_AUTHORIZATION'].to_s.split 
    elsif request.env.has_key? 'HTTP_AUTHORIZATION'         # regular location
      authdata = request.env['HTTP_AUTHORIZATION'].to_s.split
    end 
    # only basic authentication supported
    if authdata and authdata[0] == 'Basic' 
      user, pass = Base64.decode64(authdata[1]).split(':',2)
    end 
    return [user, pass] 
  end 

end
