module GeocoderHelper
  def result_to_html(result)
    html_options = {}
    #html_options[:title] = strip_tags(result[:description]) if result[:description]
    html_options[:href] = "?mlat=#{result[:lat]}&mlon=#{result[:lon]}&zoom=#{result[:zoom]}"
    html = ""
    html << result[:prefix] if result[:prefix]
    html << " " if result[:prefix] and result[:name]
    html << link_to_function(result[:name],"setPosition(#{result[:lat]}, #{result[:lon]}, #{result[:zoom]})", html_options)  if result[:name]
    html << result[:suffix] if result[:suffix]
    return html
  end
end
