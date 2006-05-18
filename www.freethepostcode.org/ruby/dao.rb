require 'mysql'


module OSM
  class Dao
    include Singleton

    MYSQL_SERVER = '127.0.0.1'
    MYSQL_USER = 'postcode'
    MYSQL_PASS = 'kc8dFusmw'
    MYSQL_DATABASE = 'postcode'

    def get_connection
      begin
        return Mysql.real_connect(MYSQL_SERVER, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE)

      rescue MysqlError => e
        mysql_error(e)
      end
    end

    def quote(string)
      return Mysql.quote(string)
    end

    def q(s); quote(s); end


    def call_sql
      dbh = nil
      begin
        dbh = get_connection
        sql = yield
        res = dbh.query(sql)
        if res.nil? then return true else return res end
      rescue MysqlError =>ex
        mysql_error(ex)
      ensure
        dbh.close unless dbh.nil?
      end
      nil
    end


    def mysql_error(e)
      puts "Error code: ", e.errno, "\n"
      puts "Error message: ", e.error, "\n"
    end

    def email_address?(email)
      return  email.upcase.match(/\b[A-Z0-9._%-]+@[A-Z0-9._%-]+\.[A-Z]{2,4}\b/)
    end

    def confirm?(email, confirmstring)
      res = call_sql { "select count(*) as count from codes where confirmed = 0 and email = '#{q(email)}' and confirmstring = '#{q(confirmstring)}' limit 1" }
      res.each_hash do |row|
        if row['count'] == '1'
          call_sql { "update codes set confirmed = 1 where confirmed = 0 and email = '#{q(email)}' and confirmstring = '#{q(confirmstring)}'" }
          return true
        else
          return false
        end
      end
    end

    def make_confirm_string
      chars = 'abcdefghijklmnopqrtuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
      confirmstring = ''
      for i in 1..20
        confirmstring += chars[(rand * chars.length).to_i].chr
      end
      return confirmstring
    end

    def add_code(email, lat, lon, postcode1, postcode2, confirmstring)
      call_sql { "insert into codes values ('#{q(email)}', #{q(lat.to_s)}, #{q(lon.to_s)}, '#{q(postcode1)}', '#{q(postcode2)}', NOW(), '#{q(confirmstring)}', 0);" }
    end


  end
end
