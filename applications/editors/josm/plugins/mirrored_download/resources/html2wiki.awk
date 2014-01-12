{
  buf = $0;
  result = "";
  match_idx = match(buf, "\<a id=\"[^\"]*\"\/\>");
  while (match_idx > 0)
  {
    result = result substr(buf, 0, match_idx);
    result = result "<span id=\"" substr(buf, match_idx+7, RLENGTH-10) "\"></span>";
    buf = substr(buf, match_idx+RLENGTH);
    match_idx = match(buf, "\<a id=\"[^\"]*\"\/\>");
  };
  result = result buf

  buf = result;
  result = "";
  match_idx = match(buf, "\<a href=\"[^\"]*\"\>");
  while (match_idx > 0)
  {
    result = result substr(buf, 0, match_idx);
    result = result "[[" substr(buf, match_idx+9, RLENGTH-11) "|";
    buf = substr(buf, match_idx+RLENGTH);
    match_idx = match(buf, "\<\/a\>");
    result = result substr(buf, 0, match_idx);
    result = result "]]";
    buf = substr(buf, match_idx+RLENGTH);
    match_idx = match(buf, "\<a href=\"[^\"]*\"\>");
  };
  result = result buf
  print result;
}
