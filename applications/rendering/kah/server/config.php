<?php
function getBaseDir()
{
  return("files");
}
function getLayers()
{
  return(array("tile","layer1","layer2","layer3"));
}
function isValidLayer($Layer)
{
  return(in_array($Layer, getLayers()));
}