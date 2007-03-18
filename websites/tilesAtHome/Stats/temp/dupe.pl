while(($ID,$X,$Y,$Z,$User,$Size,$Date) = split(/,/,<>)){

  $Key = "$X:$Y:$Z";
  $Count{$Key}++;
  $LastID{$Key} = $ID;
  
}

while(($Key,$Count) = each(%Count)){
  if($Count > 1){
    ($X,$Y,$Z) = split(/:/,$Key);
    
    printf "delete from `tiles` where `x`=%d and `y`=%d and `z`=%d and `id`!=%d;\n", $X,$Y,$Z,$LastID{$Key};
    
  }
}