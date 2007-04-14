<?php

  function navHeader($Title, $Root = "./",$Sidebar=1){
    print("<html><head><title>$Title</title>");
    print("<link rel=\"stylesheet\" href=\"$Root/styles.css\">");
    print("</head>\n<body>");
    
    if($Sidebar)
      sidebar();
    
    $Logo = "<a href=\"${Root}\"><img src=\"${Root}TilesAtHome.png\" width=\"60\" height=\"28\" border=\"0\" alt=\"logo\"></a>";
    print("<h1>$Logo $Title</h1>\n");
    print "<div id=\"main\">\n";
    
  }
  function navFooter(){
    print "</div><!-- main -->\n</body></html>\n";
  }
  
  function sidebar(){
    $ImgSize = 256;
    printf("<div style=\"float:right; width:%dpx\"><table border=\"0\" cellpadding=\"3\" cellspacing=\"0\">\n",
      $ImgSize+10);
    for($dy = 0; $dy < 3; $dy++){
      $X = 32684;
      $Y = 21834+$dy;
      $Z=16;
      
      
      $Image = sprintf("/~ojw/Tiles/tile.php/%d/%d/%d.png", $Z,$X,$Y);
      printf("<tr><td><img style=\"border:5px solid #888;\" src=\"%s\" width=\"%d\" height=\"%d\"></td></tr>\n", $Image, $ImgSize, $ImgSize);
    }
    print "</table></div>";
  }
?>