<?xml version='1.0' encoding="utf-8"?>
<!DOCTYPE WMT_MS_Capabilities SYSTEM
 "http://www.idee.es/wms/PNOA/capabilities_1_1_1.dtd"
 [
 <!ELEMENT VendorSpecificCapabilities EMPTY>
 ]>
<WMT_MS_Capabilities version="1.1.0" xmlns="http://www.opengis.net/wms" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wms http://schemas.opengis.net/wms/1.1.0/capabilities_1_1_0.xsd" updateSequence="0">
  <Service>
    <Name>OSM-WMS</Name>
    <Title>OpenStreetMap WMS</Title>
    <Abstract>WMS for OpenStreetMap data. All contents under copyleft license. See www.openstreetmap.org for more information.</Abstract>
    <KeywordList>
      <Keyword>openstreetmap</Keyword>
    </KeywordList>
    <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://www.openstreetmap.org/" />
    <!-- Contact information -->
    <ContactInformation>
      <ContactPersonPrimary>
        <ContactPerson>-</ContactPerson>
        <ContactOrganization>OpenStreetMap Foundation</ContactOrganization>
      </ContactPersonPrimary>
      <ContactPosition>-</ContactPosition>
      <ContactAddress>
        <AddressType>-</AddressType>
        <Address>-</Address>
        <City>-</City>
        <StateOrProvince>-</StateOrProvince>
        <PostCode>-</PostCode>
        <Country>-</Country>
      </ContactAddress>
      <ContactVoiceTelephone>-</ContactVoiceTelephone>
      <ContactElectronicMailAddress>-</ContactElectronicMailAddress>
    </ContactInformation>
    <!-- Fees or access constraints imposed. -->
    <Fees>Free as in Free Beer</Fees>
    <AccessConstraints>Contents under copyleft license. Please see http://wiki.openstreetmap.org/index.php/OpenStreetMap_License</AccessConstraints>
    <LayerLimit>1</LayerLimit>
    <MaxWidth>2048</MaxWidth>
    <MaxHeight>2048</MaxHeight>
  </Service>
  <Capability>
    <Request>
      <GetCapabilities>
<?php
// $server_addr = "http://{$_SERVER['SERVER_NAME']}:{$_SERVER['SERVER_PORT']}{$_SERVER['SCRIPT_NAME']}";
$server_addr = "http://{$_SERVER['SERVER_NAME']}{$_SERVER['SCRIPT_NAME']}";
?>
      <Format>application/vnd.ogc.wms_xml</Format>
        <DCPType>
          <HTTP>
            <Get>
              <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="<?php echo $server_addr; ?>" />
            </Get>
            <!--          <Post>
            <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
             xlink:type="simple"
             xlink:href="<?php echo $server_addr; ?>"" />
          </Post>-->
          </HTTP>
        </DCPType>
      </GetCapabilities>
      <GetMap>
        <Format>image/png</Format>
        <Format>image/gif</Format>
        <Format>image/jpeg</Format>
        <DCPType>
          <HTTP>
            <Get>
              <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="<?php echo $server_addr; ?>" />
            </Get>
          </HTTP>
        </DCPType>
      </GetMap>
<!--      <GetFeatureInfo>
        <Format>text/xml</Format>
        <Format>text/plain</Format>
        <Format>text/html</Format>
        <DCPType>
          <HTTP>
            <Get>
              <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="<?php echo $server_addr; ?>" />
            </Get>
          </HTTP>
        </DCPType>
      </GetFeatureInfo>-->
    </Request>
    <Exception>
      <Format>application/vnd.ogc.se_xml</Format>
      <Format>text/xml</Format>
      <Format>application/vnd.ogc.se_inimage</Format>
      <Format>application/vnd.ogc.se_blank</Format> 
    </Exception>
    <VendorSpecificCapabilities />    
    <Layer>
      <Title>OpenStreetMap</Title>
<!--       <SRS>EPSG:4326</SRS> -->
<?php
	foreach(datafactory::$available_crs as $crs)
	{	echo "<SRS>$crs</SRS>";	}
?>
      <LatLonBoundingBox minx="-180" miny="-90" maxx="180" maxy="90" />
      <BoundingBox SRS="EPSG:4326" minx="-180" miny="-90" maxx="180" maxy="90" />
<?php
     echo wireframe::getCapabilities();
?>
    </Layer>
  </Capability>
</WMT_MS_Capabilities>
