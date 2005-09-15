require 'singleton'
require 'osm/dao'

module Apache

  class SimpleAuth

    include Singleton
    
    ## authenticate
    # this checks the given password against the password stored
    # in the database
    def authenticate(r)
      # grab the plain text password from the request
      pw = r.get_basic_auth_pw
      # grab the remote user
      name = r.user

      server = r.server
      begin
        # check against the database
        dao_validates = false
        if name == 'token'
          dao_validates = OSM::Dao.instance.check_user_token?(pw)
        else
          dao_validates = OSM::Dao.instance.check_user?(name, pw)
        end

        if dao_validates
          server.log_info("SimpleAuth: OK");
          return OK
        else
          server.log_info("SimpleAuth: Failed");
          r.note_basic_auth_failure
          return AUTH_REQUIRED
        end
      rescue RuntimeError => e
        server.log_info("SimpleAuth: Error: %s", e.error);
        return AUTH_REQUIRED
      end
    end

    ## authorize
    # check that a user has the rights to the resource they're 
    # trying to access. this is normally done through the apache
    # Require user/group directives, so we check them here
    #
    # NOTE: this is stolen wholesale from silly-auth.rb
    def authorize(r)
      for method_mask, requirement in r.requires
        w, *args = requirement.split
        case w
        when "valid-user"
          # must be valid, or we would have failed at authenticate
          return OK
        when "user"
          # check that the user is in the list of OK users
          if args.include?(r.connection.user)
            return OK
          end
        else
          # OSM doesn't have a concept of groups yet, but we'd add
          # the code here once it does
          return DECLINED
        end
      end
      # if we haven't been accepted or declined then return that
      # authentication is required
      r.note_basic_auth_failure
      return AUTH_REQUIRED
    end

  end

end
