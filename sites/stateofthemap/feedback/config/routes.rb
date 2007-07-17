ActionController::Routing::Routes.draw do |map|

  map.connect '/', :controller => 'feedback', :action => 'index'
  # Install the default route as the lowest priority.
  map.connect ':controller/:action/:id.:format'
  map.connect ':controller/:action/:id'
end
