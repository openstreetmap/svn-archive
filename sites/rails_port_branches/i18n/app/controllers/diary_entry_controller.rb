class DiaryEntryController < ApplicationController
  layout 'site', :except => :rss

  before_filter :authorize_web
  before_filter :require_user, :only => [:new]
  before_filter :check_database_availability

  def new
    @title = 'new diary entry'
    if params[:diary_entry]     
      @diary_entry = DiaryEntry.new(params[:diary_entry])
      @diary_entry.user = @user
      if @diary_entry.save 
        redirect_to :controller => 'diary_entry', :action => 'list', :display_name => @user.display_name 
      end
    end
  end

  def comment
    @entry = DiaryEntry.find(params[:id])
    @diary_comment = @entry.diary_comments.build(params[:diary_comment])
    @diary_comment.user = @user
    if @diary_comment.save
      Notifier::deliver_diary_comment_notification(@diary_comment)
      redirect_to :controller => 'diary_entry', :action => 'view', :display_name => @entry.user.display_name, :id => @entry.id
    else
      render :action => 'view'
    end
  end
  
  def list
    if @user && params[:language] == @user.locale
      if params[:display_name]
        @this_user = User.find_by_display_name(params[:display_name])
        if @this_user
          @title = @this_user.display_name + "'s diary" + " (" + params[:language] + ")"
          @entry_pages, @entries = paginate(:diary_entries,
                                            :conditions => ['user_id = ? AND language = ?', @this_user.id, params[:language]],
                                            :order => 'created_at DESC',
                                            :per_page => 20)
        else
          @not_found_user = params[:display_name]
          render :action => 'no_such_user', :status => :not_found
        end
      else
        @title = "Users' diaries" + " (" + params[:language] + ")"
        @entry_pages, @entries = paginate(:diary_entries,
                                          :conditions => ['language = ?', params[:language]],
                                          :order => 'created_at DESC',
                                          :per_page => 20)
      end
    else
      if params[:display_name]
        @this_user = User.find_by_display_name(params[:display_name])
        if @this_user
          @title = @this_user.display_name + "'s diary"
          @entry_pages, @entries = paginate(:diary_entries,
                                            :conditions => ['user_id = ?', @this_user.id],
                                            :order => 'created_at DESC',
                                            :per_page => 20)
        else
          @not_found_user = params[:display_name]
          render :action => 'no_such_user', :status => :not_found
        end
      else
        @title = "Users' diaries"
        @entry_pages, @entries = paginate(:diary_entries,
                                          :order => 'created_at DESC',
                                          :per_page => 20)
      end
    end
  end

  def rss
    if params[:language]
      if params[:display_name]
        user = User.find_by_display_name(params[:display_name])
        @entries = DiaryEntry.find(:all, :conditions => ['user_id = ? AND language = ?', user.id, params[:language]], :order => 'created_at DESC', :limit => 20)   
        @title = "OpenStreetMap diary entries for #{user.display_name} (#{params[:language]})"
        @description = "Recent OpenStreetmap diary entries from #{user.display_name} (#{params[:language]})"
        @link = "http://www.openstreetmap.org/user/#{user.display_name}/diary/#{params[:language]}"
      else
        @entries = DiaryEntry.find(:all, :conditions => ['language = ?', params[:language]], :order => 'created_at DESC', :limit => 20)
        @title = "OpenStreetMap diary entries (#{params[:language]})"
        @description = "Recent diary entries from users of OpenStreetMap (#{params[:language]})"
        @link = "http://www.openstreetmap.org/diary/#{params[:language]}"
      end
    else
      if params[:display_name]
        user = User.find_by_display_name(params[:display_name])
        @entries = DiaryEntry.find(:all, :conditions => ['user_id = ?', user.id], :order => 'created_at DESC', :limit => 20)
        @title = "OpenStreetMap diary entries for #{user.display_name}"
        @description = "Recent OpenStreetmap diary entries from #{user.display_name}"
        @link = "http://www.openstreetmap.org/user/#{user.display_name}/diary"
      else
        @entries = DiaryEntry.find(:all, :order => 'created_at DESC', :limit => 20)
        @title = "OpenStreetMap diary entries"
        @description = "Recent diary entries from users of OpenStreetMap"
        @link = "http://www.openstreetmap.org/diary"
      end
    end
    render :content_type => Mime::RSS
  end

  def view
    user = User.find_by_display_name(params[:display_name])
    @entry = DiaryEntry.find(:first, :conditions => ['user_id = ? AND id = ?', user.id, params[:id]])
  end
end
