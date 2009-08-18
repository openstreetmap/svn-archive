class OauthController < ApplicationController
  layout 'site'

  before_filter :authorize_web, :except => [:request_token, :access_token]
  before_filter :require_user, :only => [:oauthorize]
  before_filter :verify_oauth_consumer_signature, :only => [:request_token]
  before_filter :verify_oauth_request_token, :only => [:access_token]
  # Uncomment the following if you are using restful_open_id_authentication
  # skip_before_filter :verify_authenticity_token

  def request_token
    @token = current_client_application.create_request_token

    logger.info "in REQUEST TOKEN"
    if @token
      logger.info "request token params: #{params.inspect}"
      # request tokens indicate what permissions the client *wants*, not
      # necessarily the same as those which the user allows.
      current_client_application.permissions.each do |pref|
        logger.info "PARAMS found #{pref}"
        @token.write_attribute(pref, true)
      end
      @token.save!

      render :text => @token.to_query
    else
      render :nothing => true, :status => 401
    end
  end 
  
  def access_token
    @token = current_token && current_token.exchange!
    if @token
      render :text => @token.to_query
    else
      render :nothing => true, :status => 401
    end
  end

  def oauthorize
    @token = RequestToken.find_by_token params[:oauth_token]
    unless @token.invalidated?    
      if request.post? 
        any_auth = false
        @token.client_application.permissions.each do |pref|
          if params[pref]
            logger.info "OAUTHORIZE PARAMS found #{pref}"
            @token.write_attribute(pref, true)
            any_auth ||= true
          else
            @token.write_attribute(pref, false)
          end
        end
        
        if any_auth
          @token.authorize!(@user)
          redirect_url = params[:oauth_callback] || @token.client_application.callback_url
          if redirect_url
            redirect_to "#{redirect_url}?oauth_token=#{@token.token}"
          else
            render :action => "authorize_success"
          end
        else
          @token.invalidate!
          render :action => "authorize_failure"
        end
      end
    else
      render :action => "authorize_failure"
    end
  end
  
  def revoke
    @token = @user.oauth_tokens.find_by_token params[:token]
    if @token
      @token.invalidate!
      flash[:notice] = "You've revoked the token for #{@token.client_application.name}"
    end
    logger.info "about to redirect"
    redirect_to :controller => 'oauth_clients', :action => 'index'
  end
  
end
