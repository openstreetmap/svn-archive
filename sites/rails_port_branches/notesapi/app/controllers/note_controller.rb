# The NoteController is the RESTful interface to Note objects

class NoteController < ApplicationController
  require 'xml/libxml'

  session :off
  before_filter :check_write_availability, :only => [:create, :update, :delete]
  before_filter :check_read_availability, :except => [:create, :update, :delete]
  after_filter :compress_output

  # Create a note from XML.
  def create
    if request.put?
      note = Note.from_xml(request.raw_post, true)
      
      if note
        note.visible = true
        note.save_with_history!
        
        render :text => note.id.to_s, :content_type => "text/plain"
      else
        render :nothing => true, :status => :bad_request
      end
    else
      render :nothing => true, :status => :method_not_allowed
    end
  end
  
  # Dump the details on a note given in params[:id]
  def read
    begin
      note = Note.find(params[:id])
      if note.visible
        response.headers['Last-Modified'] = note.timestamp.rfc822
        render :text => note.to_xml.to_s, :content_type => "text/xml"
      else
        render :text => "", :status => :gone
      end
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    end
  end
  
  # Update a note from given XML
  def update
    begin
      note = Note.find(params[:id])
      new_note = Note.from_xml(request.raw_post)
      
      if new_note and new_note.id == note.id
        note = new_note
	note.visible = true
        note.save_with_history!
        
        render :nothing => true
      else
        render :nothing => true, :status => :bad_request
      end
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    end
  end
  
  # Delete a note. Doesn't actually delete it, but retains its history in a wiki-like way.
  def delete
    begin
      note = Note.find(params[:id])
      
      if note.visible
        note.user_id = @user.id
        note.visible = 0
        note.save_with_history!
        
        render :nothing => true
      else
        render :text => "", :status => :gone
      end
    rescue ActiveRecord::RecordNotFound
      render :nothing => true, :status => :not_found
    end
  end

end

