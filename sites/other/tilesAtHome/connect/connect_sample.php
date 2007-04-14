<?php

  $DbSuccess = ConnectDB();
  
  // Set $NoExitOnDbFail to handle database errors yourself, default is to exit the script
  if(!$DbSuccess && !$NoExitOnDbFail)
    exit;
    

  function ConnectDB(){
    if(!@mysql_connect("server","user","pass"))
      return(0);

    if(!mysql_select_db("db"))
      return(0);
      
    return(1);
    }
?>
