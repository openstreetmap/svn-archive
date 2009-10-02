module ApplicationHelper
  def htmlize(text)
    return sanitize(auto_link(simple_format(text), :urls))
  end

  def rss_link_to(*args)
    return link_to(image_tag("RSS.gif", :size => "16x16", :border => 0), Hash[*args], { :class => "rsssmall" });
  end

  def atom_link_to(*args)
    return link_to(image_tag("RSS.gif", :size => "16x16", :border => 0), Hash[*args], { :class => "rsssmall" });
  end

  def javascript_strings
    js = ""

    js << "<script type='text/javascript'>\n"
    js << "rails_i18n = new Array();\n"
    js << javascript_strings_for_key("javascripts")
    js << "</script>\n"

    return js
  end

private

  def javascript_strings_for_key(key)
    js = ""
    value = t(key)

    if value.is_a?(String)
      js << "rails_i18n['#{key}'] = '" << escape_javascript(value) << "';\n"
    else
      value.each_key do |k|
        js << javascript_strings_for_key("#{key}.#{k}")
      end
    end

    return js
  end
end
