class SiteController < ApplicationController
  before_filter :authorize_web
  before_filter :require_user, :only => [:edit]

  def export
    render :action => 'index'
  end

  def goto_way
    way = Way.find(params[:id])

    begin
      node = way.way_nodes.first.node
      redirect_to :controller => 'site', :action => 'index', :lat => node.latitude, :lon => node.longitude, :zoom => 6
    rescue
      redirect_to :back
    end
  end
end
