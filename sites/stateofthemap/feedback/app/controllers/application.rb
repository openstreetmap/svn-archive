# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  # Pick a unique cookie name to distinguish our session data from others'
  session :session_key => '_feedback_session_id'
  def authorize_web
    @current_user = User.find_by_token(session[:token])
  end


end
