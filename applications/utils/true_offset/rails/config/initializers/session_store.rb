# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_offset_session',
  :secret      => 'd96fc23d24a953ff728c4f5b59c7af9dcb15ba0173a70c27da58392d6bf81d765e1f5ff3fcbb143cb6e06578157682c8463054a729556161b02d474096acc981'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
