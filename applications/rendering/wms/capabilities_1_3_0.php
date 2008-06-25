<?xml version='1.0' encoding="utf-8"?>
<WMS_Capabilities version="1.3.0" xmlns="http://www.opengis.net/wms" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wms http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd">
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
      <Format>text/xml</Format>
        <DCPType>
          <HTTP>
            <Get>
              <!-- TODO: replace with URL when on a live server! -->
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
        <Format>image/gif</Format>
        <Format>image/png</Format>
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
      <Format>XML</Format>
      <Format>INIMAGE</Format>
      <!--     <Format>BLANK</Format> -->
    </Exception>
    <Layer>
      <Title>OpenStreetMap</Title>
      <CRS>CRS:84</CRS>
<?php
     echo wireframe::getCapabilities();
?>
    </Layer>
  </Capability>
</WMS_Capabilities>
