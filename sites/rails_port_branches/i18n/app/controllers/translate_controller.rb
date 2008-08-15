class TranslateController < ApplicationController
  layout 'site'

  before_filter :authorize_web
  before_filter :require_user, :except => [:rss]
  before_filter :check_database_availability


  def pending
    Locale.set params[:locale]
    if params[:id]
      redirect_to :action => 'view', :id => params[:id]
    end
    @title = 'l10n home'
    unless params[:locale] == "en-US"
      @entry_pages, @entries = paginate(:translations,
                                        :conditions => ['text IS NULL AND language_id = ?', Locale.language.id],
                                        :order => 'id DESC',
                                        :per_page => 10)
    end
  end

  def complete
    Locale.set params[:locale]
    if params[:id]
      redirect_to :action => 'view', :id => params[:id]
    end
    @title = 'l10n home'
    unless params[:locale] == "en-US"
      @entry_pages, @entries = paginate(:translations,
                                        :conditions => ['text IS NOT NULL AND language_id = ?', Locale.language.id],
                                        :order => 'id DESC',
                                        :per_page => 10)
    end
  end

  def view
    @title = "view a string".t
    @entry = Translation.find(:all, :conditions => ['id = ?', params[:id]])
  end

  def stats
    @title = "l10n statistics".t
    unless @user.nil? && @user.locale == "en-US"
      stat = Statistics.find(:first, :conditions => ['locale = ?', @user.locale])
      if stat.nil?
        stat = Statistics.new(:locale => @user.locale, :language => Locale.language.to_s, :country => Locale.country.english_name, :timestamp => Time.now )
        stat.save
      end
      temp = Statistics.find(:first, :conditions => ['locale = ?', @user.locale])
      stat.tr_complete = Translation.count(:conditions => ['text IS NOT NULL AND language_id = ?', Locale.language.id])
      stat.tr_total = Translation.count(:conditions => ['language_id = ?', Locale.language.id])
      stat.tr_percentage = (stat.tr_complete * 100 / stat.tr_total) 
      if (stat.tr_complete > temp.tr_complete) || (stat.tr_total > temp.tr_total)
        stat.timestamp = Time.now
      end
      stat.save
    end
    if params[:sort] == 'percentage'
      @stat_entries = Statistics.find(:all, :order => 'tr_percentage DESC')
    elsif params[:sort] == 'done'
      @stat_entries = Statistics.find(:all, :order => 'tr_complete DESC')
    end
    end
  end

  def rss
    Locale.set params[:locale]
    if params[:id] == 'pending'
      @entries = Translation.find(:all, :conditions => ['text IS NULL AND language_id = ?', Locale.language.id], :order => 'id DESC', :limit => 20)
      @title = "OpenStreetMap pending translations for #{Locale.language} (#{Locale.country.english_name})"
      @description = "Recent OpenStreetmap pending translation strings for #{Locale.language} (#{Locale.country.english_name})"
      @link = "http://www.openstreetmap.org/translate/#{params[:locale]}/#{params[:id]}"
    else if params[:id] == 'complete'
      @entries = Translation.find(:all, :conditions => ['text IS NOT NULL AND language_id = ?', Locale.language.id], :order => 'id DESC', :limit => 20)
      @tiitle = "OpenStreetMap complete translations for #{Locale.language} (#{Locale.country.english_name})"
      @description = "Recent OpenStreetmap complete translation strings for #{Locale.language} (#{Locale.country.english_name})"
      @link = "http://www.openstreetmap.org/translate/#{params[:locale]}/#{params[:id]}"
    end
    end
  
    render :content_type => Mime::RSS

  end
end
