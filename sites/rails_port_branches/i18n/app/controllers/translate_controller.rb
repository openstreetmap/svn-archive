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
    @title = 'view a string'
    @entry = Translation.find(:all, :conditions => ['id = ?', params[:id]])
  end

  def rss
    if params[:id] == 'pending'
      Locale.set params[:locale]
      @entries = Translation.find(:all, :conditions => ['text IS NULL AND language_id = ?', Locale.language.id], :order => 'id DESC', :limit => 20)
      @title = "OpenStreetMap pending translations for #{Locale.language} (#{Locale.country.english_name})"
      @description = "Recent OpenStreetmap pending translation strings for #{Locale.language} (#{Locale.country.english_name})"
      @link = "http://www.openstreetmap.org/translate/#{params[:locale]}/#{params[:id]}"
    else if params[:id] == 'complete'
      @entries = Translation.find(:all, :conditions => ['text IS NOT NULL AND language_id = ?', Locale.language.id], :order =>     'id DESC', :limit => 20)
      @tiitle = "OpenStreetMap complete translations for #{Locale.language} (#{Locale.country.english_name})"
      @description = "Recent OpenStreetmap complete translation strings for #{Locale.language} (#{Locale.country.english_name})"
      @link = "http://www.openstreetmap.org/translate/#{params[:locale]}/#{params[:id]}"
    end
    end
  
    render :content_type => Mime::RSS

  end
end
