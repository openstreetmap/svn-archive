module ActiveRecord
  module ConnectionAdapters
    module SchemaStatements
      def quote_column_names(column_name)
        Array(column_name).map { |e| quote_column_name(e) }.join(", ")
      end

      def add_primary_key(table_name, column_name, options = {})
        column_names = Array(column_name)
        quoted_column_names = column_names.map { |e| quote_column_name(e) }.join(", ")
        execute "ALTER TABLE #{table_name} ADD PRIMARY KEY (#{quoted_column_names})"
      end

      def remove_primary_key(table_name)
        execute "ALTER TABLE #{table_name} DROP PRIMARY KEY"
      end

      def add_foreign_key(table_name, column_name, reftbl, refcol = nil)
        execute "ALTER TABLE #{table_name} ADD " +
	  "FOREIGN KEY (#{quote_column_names(column_name)}) " +
	  "REFERENCES #{reftbl} (#{quote_column_names(refcol || column_name)})"
      end

      alias_method :old_options_include_default?, :options_include_default?

      def options_include_default?(options)
        return false if options[:options] =~ /AUTO_INCREMENT/i
        return old_options_include_default?(options)
      end

      alias_method :old_add_column_options!, :add_column_options!

      def add_column_options!(sql, options)
        sql << " UNSIGNED" if options[:unsigned]
        old_add_column_options!(sql, options)
        sql << " #{options[:options]}"
      end
    end

    class MysqlAdapter
      alias_method :old_native_database_types, :native_database_types

      def native_database_types
        types = old_native_database_types
        types[:bigint] = { :name => "bigint", :limit => 20 }
        types[:double] = { :name => "double" }
        types[:bigint_pk] = { :name => "bigint(20) DEFAULT NULL auto_increment PRIMARY KEY" }
        types[:bigint_pk_64] = { :name => "bigint(64) DEFAULT NULL auto_increment PRIMARY KEY" }
        types[:bigint_auto_64] = { :name => "bigint(64) DEFAULT NULL auto_increment" }
        types[:bigint_auto_11] = { :name => "bigint(11) DEFAULT NULL auto_increment" }
        types[:bigint_auto_20] = { :name => "bigint(20) DEFAULT NULL auto_increment" }
        types
      end

      def change_column(table_name, column_name, type, options = {})
        unless options_include_default?(options)
          options[:default] = select_one("SHOW COLUMNS FROM #{table_name} LIKE '#{column_name}'")["Default"]

          unless type == :string or type == :text
            options.delete(:default) if options[:default] = "";
          end
        end

        change_column_sql = "ALTER TABLE #{table_name} CHANGE #{column_name} #{column_name} #{type_to_sql(type, options[:limit], options[:precision], options[:scale])}"
        add_column_options!(change_column_sql, options)
        execute(change_column_sql) 
      end

      def myisam_table
        return { :id => false, :force => true, :options => "ENGINE=MyIsam" }
      end

      def innodb_table
        return { :id => false, :force => true, :options => "ENGINE=InnoDB" }
      end

      def innodb_option
        return "ENGINE=InnoDB"
      end
 
      def change_engine (table_name, engine)
        execute "ALTER TABLE #{table_name} ENGINE = #{engine}"
      end
    end
  end
end
