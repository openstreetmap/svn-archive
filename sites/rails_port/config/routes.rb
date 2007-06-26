ActionController::Routing::Routes.draw do |map|

  # API
  map.connect "api/#{API_VERSION}/node/create", :controller => 'node', :action => 'create'
  map.connect "api/#{API_VERSION}/node/:id/segments", :controller => 'segment', :action => 'segments_for_node', :id => /\d+/
  map.connect "api/#{API_VERSION}/node/:id/history", :controller => 'old_node', :action => 'history', :id => /\d+/
  map.connect "api/#{API_VERSION}/node/:id", :controller => 'node', :action => 'rest', :id => /\d+/
  map.connect "api/#{API_VERSION}/nodes", :controller => 'node', :action => 'nodes', :id => nil
  
  map.connect "api/#{API_VERSION}/segment/create", :controller => 'segment', :action => 'create'
  map.connect "api/#{API_VERSION}/segment/:id/ways", :controller => 'way', :action => 'ways_for_segment', :id => /\d+/
  map.connect "api/#{API_VERSION}/segment/:id/history", :controller => 'old_segment', :action => 'history', :id => /\d+/
  map.connect "api/#{API_VERSION}/segment/:id", :controller => 'segment', :action => 'rest', :id => /\d+/
  map.connect "api/#{API_VERSION}/segments", :controller => 'segment', :action => 'segments', :id => nil
  
  map.connect "api/#{API_VERSION}/way/create", :controller => 'way', :action => 'create'
  map.connect "api/#{API_VERSION}/way/:id/history", :controller => 'old_way', :action => 'history', :id => /\d+/
  map.connect "api/#{API_VERSION}/way/:id/full", :controller => 'way', :action => 'full', :id => /\d+/
  map.connect "api/#{API_VERSION}/way/:id", :controller => 'way', :action => 'rest', :id => /\d+/
  map.connect "api/#{API_VERSION}/ways", :controller => 'way', :action => 'ways', :id => nil

  map.connect "api/#{API_VERSION}/map", :controller => 'api', :action => 'map'
  
  map.connect "api/#{API_VERSION}/trackpoints", :controller => 'api', :action => 'trackpoints'
  
  map.connect "api/#{API_VERSION}/search", :controller => 'search', :action => 'search_all'
  map.connect "api/#{API_VERSION}/way/search", :controller => 'search', :action => 'search_ways'
  map.connect "api/#{API_VERSION}/segment/search", :controller => 'search', :action => 'search_segments'
  map.connect "api/#{API_VERSION}/nodes/search", :controller => 'search', :action => 'search_nodes'
  
  map.connect "api/#{API_VERSION}/user/details", :controller => 'user', :action => 'api_details'
  map.connect "api/#{API_VERSION}/user/gpx_files", :controller => 'user', :action => 'api_gpx_files'
 
  map.connect "api/#{API_VERSION}/gpx/create/:filename/:description/:tags", :controller => 'trace', :action => 'api_create'
  map.connect "api/#{API_VERSION}/gpx/:id/details", :controller => 'trace', :action => 'api_details'
  map.connect "api/#{API_VERSION}/gpx/:id/data", :controller => 'trace', :action => 'api_data'
  
  # Potlatch API
  
  map.connect "api/#{API_VERSION}/amf", :controller =>'amf', :action =>'talk'
  map.connect "api/#{API_VERSION}/swf/trackpoints", :controller =>'swf', :action =>'trackpoints'
  
  # web site

  map.connect '/', :controller => 'site', :action => 'index'
  map.connect '/user/save', :controller => 'user', :action => 'save'
  map.connect '/user/confirm', :controller => 'user', :action => 'confirm'
  map.connect '/user/go_public', :controller => 'user', :action => 'go_public'
  map.connect '/user/reset_password', :controller => 'user', :action => 'reset_password'
  map.connect '/index.html', :controller => 'site', :action => 'index'
  map.connect '/edit.html', :controller => 'site', :action => 'edit'
  map.connect '/search.html', :controller => 'way_tag', :action => 'search'
  map.connect '/login.html', :controller => 'user', :action => 'login'
  map.connect '/logout.html', :controller => 'user', :action => 'logout'
  map.connect '/create-account.html', :controller => 'user', :action => 'new'
  map.connect '/forgot-password.html', :controller => 'user', :action => 'lost_password'

  # traces  
  map.connect '/traces', :controller => 'trace', :action => 'list'
  map.connect '/traces/page/:page', :controller => 'trace', :action => 'list'
  map.connect '/traces/mine', :controller => 'trace', :action => 'mine'
  map.connect '/trace/create', :controller => 'trace', :action => 'create'
  map.connect '/traces/mine/page/:page', :controller => 'trace', :action => 'mine'
  map.connect '/traces/mine/tag/:tag', :controller => 'trace', :action => 'mine'
  map.connect '/traces/mine/tag/:tag/page/:page', :controller => 'trace', :action => 'mine'
  map.connect '/traces/rss', :controller => 'trace', :action => 'georss'
  map.connect '/user/:display_name/traces', :controller => 'trace', :action => 'list', :id => nil
  map.connect '/user/:display_name/traces/page/:page', :controller => 'trace', :action => 'list', :id => nil
  map.connect '/user/:display_name/traces/:id', :controller => 'trace', :action => 'view', :id => nil
  map.connect '/user/:display_name/traces/:id/picture', :controller => 'trace', :action => 'picture', :id => nil
  map.connect '/user/:display_name/traces/:id/icon', :controller => 'trace', :action => 'icon', :id => nil
  map.connect '/traces/tag/:tag', :controller => 'trace', :action => 'list', :id => nil
  map.connect '/traces/tag/:tag/page/:page', :controller => 'trace', :action => 'list', :id => nil

  # user pages
  map.connect '/user/:display_name/make_friend', :controller => 'user', :action => 'make_friend'
  map.connect '/user/:display_name', :controller => 'user', :action => 'view'
  map.connect '/user/:display_name/diary', :controller => 'diary_entry', :action => 'list'
  map.connect '/user/:display_name/diary/:id', :controller => 'diary_entry', :action => 'list', :id => /\d+/
  map.connect '/user/:display_name/diary/rss', :controller => 'diary_entry', :action => 'rss'
  map.connect '/user/:display_name/diary/newpost', :controller => 'diary_entry', :action => 'new'
  map.connect '/user/:display_name/account', :controller => 'user', :action => 'account'
  map.connect '/user/:display_name/set_home', :controller => 'user', :action => 'set_home'
  map.connect '/diary', :controller => 'diary_entry', :action => 'list'
  map.connect '/diary/rss', :controller => 'diary_entry', :action => 'rss'
  map.connect '/diary/:language', :controller => 'diary_entry', :action => 'list'
  map.connect '/diary/:language/rss', :controller => 'diary_entry', :action => 'rss'

  # test pages
  map.connect '/test/populate/:table/:from/:count', :controller => 'test', :action => 'populate'
  map.connect '/test/populate/:table/:count', :controller => 'test', :action => 'populate', :from => 1

  # geocoder
  map.connect '/geocoder/search/', :controller => 'geocoder', :action => 'search'
  map.connect '/geocoder/results/', :controller => 'geocoder', :action => 'results'
  map.connect '/postcode/:postcode/', :controller => 'geocoder', :action => 'search'

  # messages

  map.connect '/user/:display_name/inbox', :controller => 'message', :action => 'inbox'
  map.connect '/message/new/:user_id', :controller => 'message', :action => 'new'
  map.connect '/message/read/:message_id', :controller => 'message', :action => 'read'
  map.connect '/message/mark/:message_id', :controller => 'message', :action => 'mark'
  
  # fall through
     map.connect ':controller/:id/:action'
  map.connect ':controller/:action'
end
