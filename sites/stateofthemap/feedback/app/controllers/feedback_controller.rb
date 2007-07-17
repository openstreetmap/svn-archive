class FeedbackController < ApplicationController

  before_filter :authorize_web
  layout 'site'
  logger = ActiveRecord::Base.logger

  def login
    if params[:user]
      email = params[:user][:email]
      u = User.find_by_email(email)
      if Feedback.find_by_user_id(u.id)
        flash[:notice] = "You have already left feedback"
        return
      end
      if u
        u.token = User.make_token
        u.save
        session[:token] = u.token
        redirect_to :controller => 'feedback', :action => 'index'
        return
      else
        flash[:notice] = "Please enter the email you used to sign up to SOTM"
      end
    end
  end

  def logout
    if session[:token]
      u = User.find_by_token(session[:token])
      if u
        u.token = User.make_token
        u.save
      end
    end
    session[:token] = nil
    redirect_to 'http://www.stateofthemap.org'
  end

  def index
    @talks_relevant = {:variable => 'talks', :attribute => 'relevant'}
    @talks_technical_level = {:variable => 'talks', :attribute => 'technical_level'}
    @talks_variety = {:variable => 'talks', :attribute => 'variety'}
    @talks_lightning_talks = {:variable => 'talks', :attribute => 'lightning'}
    @talks_comments = {:variable => 'talks', :attribute => 'comments'}

    @social_meet_people = {:variable => 'social', :attribute => 'meet_people'}
    @social_balance = {:variable => 'social', :attribute => 'balance'}
    @social_venues = {:variable => 'social', :attribute => 'venues'}
    @social_comments = {:variable => 'social', :attribute => 'comments'}

    @conference_good_comments = {:variable => 'conference', :attribute => 'good_comments'}
    @conference_bad_comments = {:variable => 'conference', :attribute => 'bad_comments'}
    @conference_comments = {:variable => 'conference', :attribute => 'comments'}

    if params[:talks]
      params[:talks].each do |k,v|
        if k == 'comments'
          feedback = Feedback.new
          feedback.user_id = @current_user.id
          feedback.comments = v
          feedback.description =  'talks - ' + k
          feedback.save
        else
          feedback = Feedback.new
          feedback.score = v
          feedback.description =  'talks - ' + k
          feedback.user_id = @current_user.id
          feedback.save
        end
      end
      @stage = 'social'
    end

    if params[:social]
      params[:social].each do |k,v|
        if k == 'comments'
          feedback = Feedback.new
          feedback.user_id = @current_user.id
          feedback.comments = v
          feedback.description = 'social -' + k
          feedback.save
        else
          feedback = Feedback.new
          feedback.score = v
          feedback.description =  'social - ' + k
          feedback.user_id = @current_user.id
          feedback.save
        end
      end
      @stage = 'background'
    end
    
    if params[:background]
      params[:background].each do |k,v|
        if k == 'comments'
          feedback = Feedback.new
          feedback.user_id = @current_user.id
          feedback.comments = v
          feedback.description = 'background -' + k
          feedback.save
        else
          feedback = Feedback.new
          feedback.score = v
          feedback.description =  'background - ' + k
          feedback.user_id = @current_user.id
          feedback.save
        end

      end
      @stage = 'conference'
    end
    
    if params[:conference]
      params[:conference].each do |k,v|
        if k.match('comments')
          feedback = Feedback.new
          feedback.user_id = @current_user.id
          feedback.comments = v
          feedback.description = 'conference -' + k
          feedback.save
        else
          feedback = Feedback.new
          feedback.score = v
          feedback.description =  'conference - ' + k
          feedback.user_id = @current_user.id
          feedback.save
        end
      end
        @stage = 'end'
    end
  end
end

