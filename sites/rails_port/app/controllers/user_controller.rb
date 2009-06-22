class UserController < ApplicationController
  layout 'site'

  before_filter :authorize, :only => [:api_details, :api_gpx_files]
  before_filter :authorize_web, :except => [:api_details, :api_gpx_files]
  before_filter :set_locale, :except => [:api_details, :api_gpx_files]
  before_filter :require_user, :only => [:set_home, :account, :go_public, :make_friend, :remove_friend, :upload_image, :delete_image]
  before_filter :check_database_readable, :except => [:api_details, :api_gpx_files]
  before_filter :check_database_writable, :only => [:login, :new, :set_home, :account, :go_public, :make_friend, :remove_friend, :upload_image, :delete_image]
  before_filter :check_api_readable, :only => [:api_details, :api_gpx_files]
  before_filter :require_allow_read_prefs, :only => [:api_details]
  before_filter :require_allow_read_gpx, :only => [:api_gpx_files]

  filter_parameter_logging :password, :pass_crypt, :pass_crypt_confirmation

  def save
    @title = t 'user.new.title'

    if Acl.find_by_address(request.remote_ip, :conditions => {:k => "no_account_creation"})
      render :action => 'new'
    else
      @user = User.new(params[:user])

      @user.visible = true
      @user.data_public = true
      @user.description = "" if @user.description.nil?
      @user.creation_ip = request.remote_ip
      @user.languages = request.user_preferred_languages

      if @user.save
        flash[:notice] = t 'user.new.flash create success message'
        Notifier.deliver_signup_confirm(@user, @user.tokens.create)
        redirect_to :action => 'login'
      else
        render :action => 'new'
      end
    end
  end

  def account
    @title = t 'user.account.title'
    @tokens = @user.oauth_tokens.find :all, :conditions => 'oauth_tokens.invalidated_at is null and oauth_tokens.authorized_at is not null'

    if params[:user] and params[:user][:display_name] and params[:user][:description]
      if params[:user][:email] != @user.email
        @user.new_email = params[:user][:email]
      end

      @user.display_name = params[:user][:display_name]

      if params[:user][:pass_crypt].length > 0 or params[:user][:pass_crypt_confirmation].length > 0
        @user.pass_crypt = params[:user][:pass_crypt]
        @user.pass_crypt_confirmation = params[:user][:pass_crypt_confirmation]
      end

      @user.description = params[:user][:description]
      @user.languages = params[:user][:languages].split(",")
      @user.home_lat = params[:user][:home_lat]
      @user.home_lon = params[:user][:home_lon]

      if @user.save
        set_locale

        if params[:user][:email] == @user.new_email
          flash[:notice] = t 'user.account.flash update success confirm needed'
          Notifier.deliver_email_confirm(@user, @user.tokens.create)
        else
          flash[:notice] = t 'user.account.flash update success'
        end
      end
    end
  end

  def set_home
    if params[:user][:home_lat] and params[:user][:home_lon]
      @user.home_lat = params[:user][:home_lat].to_f
      @user.home_lon = params[:user][:home_lon].to_f
      if @user.save
        flash[:notice] = t 'user.set_home.flash success'
        redirect_to :controller => 'user', :action => 'account'
      end
    end
  end

  def go_public
    @user.data_public = true
    @user.save
    flash[:notice] = t 'user.go_public.flash success'
    redirect_to :controller => 'user', :action => 'account', :display_name => @user.display_name
  end

  def lost_password
    @title = t 'user.lost_password.title'

    if params[:user] and params[:user][:email]
      user = User.find_by_email(params[:user][:email], :conditions => {:visible => true})

      if user
        token = user.tokens.create
        Notifier.deliver_lost_password(user, token)
        flash[:notice] = t 'user.lost_password.notice email on way'
      else
        flash[:notice] = t 'user.lost_password.notice email cannot find'
      end
    end
  end

  def reset_password
    @title = t 'user.reset_password.title'

    if params['token']
      token = UserToken.find_by_token(params[:token])
      if token
        pass = OSM::make_token(8)
        user = token.user
        user.pass_crypt = pass
        user.pass_crypt_confirmation = pass
        user.active = true
        user.email_valid = true
        user.save!
        token.destroy
        Notifier.deliver_reset_password(user, pass)
        flash[:notice] = t 'user.reset_password.flash changed check mail'
      else
        flash[:notice] = t 'user.reset_password.flash token bad'
      end
    end

    redirect_to :action => 'login'
  end

  def new
    @title = t 'user.new.title'

    # The user is logged in already, so don't show them the signup page, instead
    # send them to the home page
    redirect_to :controller => 'site', :action => 'index' if session[:user]
  end

  def login
    if session[:user]
      # The user is logged in already, if the referer param exists, redirect them to that
      if params[:referer]
        redirect_to params[:referer]
      else
        redirect_to :controller => 'site', :action => 'index'
      end
      return
    end

    @title = t 'user.login.title'

    if params[:user]
      email_or_display_name = params[:user][:email]
      pass = params[:user][:password]
      user = User.authenticate(:username => email_or_display_name, :password => pass)
      if user
        session[:user] = user.id
        if params[:referer]
          redirect_to params[:referer]
        else
          redirect_to :controller => 'site', :action => 'index'
        end
        return
      elsif User.authenticate(:username => email_or_display_name, :password => pass, :inactive => true)
        @notice = t 'user.login.account not active'
      else
        @notice = t 'user.login.auth failure'
      end
    end
  end

  def logout
    if session[:token]
      token = UserToken.find_by_token(session[:token])
      if token
        token.destroy
      end
      session[:token] = nil
    end
    session[:user] = nil
    if params[:referer]
      redirect_to params[:referer]
    else
      redirect_to :controller => 'site', :action => 'index'
    end
  end

  def confirm
    if params[:confirm_action]
      token = UserToken.find_by_token(params[:confirm_string])
      if token and !token.user.active?
        @user = token.user
        @user.active = true
        @user.email_valid = true
        @user.save!
        token.destroy
        flash[:notice] = t 'user.confirm.success'
        session[:user] = @user.id
        redirect_to :action => 'account', :display_name => @user.display_name
      else
        @notice = t 'user.confirm.failure'
      end
    end
  end

  def confirm_email
    if params[:confirm_action]
      token = UserToken.find_by_token(params[:confirm_string])
      if token and token.user.new_email?
        @user = token.user
        @user.email = @user.new_email
        @user.new_email = nil
        @user.active = true
        @user.email_valid = true
        @user.save!
        token.destroy
        flash[:notice] = t 'user.confirm_email.success'
        session[:user] = @user.id
        redirect_to :action => 'account', :display_name => @user.display_name
      else
        @notice = t 'user.confirm_email.failure'
      end
    end
  end

  def upload_image
    @user.image = params[:user][:image]
    @user.save!
    redirect_to :controller => 'user', :action => 'view', :display_name => @user.display_name
  end

  def delete_image
    @user.image = nil
    @user.save!
    redirect_to :controller => 'user', :action => 'view', :display_name => @user.display_name
  end

  def api_details
    render :text => @user.to_xml.to_s, :content_type => "text/xml"
  end

  def api_gpx_files
    doc = OSM::API.new.get_xml_doc
    @user.traces.each do |trace|
      doc.root << trace.to_xml_node() if trace.public? or trace.user == @user
    end
    render :text => doc.to_s, :content_type => "text/xml"
  end

  def view
    @this_user = User.find_by_display_name(params[:display_name], :conditions => {:visible => true})

    if @this_user
      @title = @this_user.display_name
    else
      @title = t 'user.no_such_user.title'
      @not_found_user = params[:display_name]
      render :action => 'no_such_user', :status => :not_found
    end
  end

  def make_friend
    if params[:display_name]     
      name = params[:display_name]
      new_friend = User.find_by_display_name(name, :conditions => {:visible => true})
      friend = Friend.new
      friend.user_id = @user.id
      friend.friend_user_id = new_friend.id
      unless @user.is_friends_with?(new_friend)
        if friend.save
          flash[:notice] = t 'user.make_friend.success', :name => name
          Notifier.deliver_friend_notification(friend)
        else
          friend.add_error(t('user.make_friend.failed', :name => name))
        end
      else
        flash[:notice] = t 'user.make_friend.already_a_friend', :name => name
      end

      redirect_to :controller => 'user', :action => 'view'
    end
  end

  def remove_friend
    if params[:display_name]     
      name = params[:display_name]
      friend = User.find_by_display_name(name, :conditions => {:visible => true})
      if @user.is_friends_with?(friend)
        Friend.delete_all "user_id = #{@user.id} AND friend_user_id = #{friend.id}"
        flash[:notice] = t 'user.remove_friend.success', :name => friend.display_name
      else
        flash[:notice] = t 'user.remove_friend.not_a_friend', :name => friend.display_name
      end

      redirect_to :controller => 'user', :action => 'view'
    end
  end
end
