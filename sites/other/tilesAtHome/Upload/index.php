<html><head><title>Upload tiles</title>
</head>
<body>
<h1>Upload tiles</h1>

<form action="tile2.php" method="post" enctype="multipart/form-data">
<p>
  Upload file <input type="file" name="file" size="50">
  <input type="hidden" name="version" value="website_upload">
  with credentials <input type="input" name="mp" value="username|password">
  <input type="submit" value="OK">
</p>

</form>
</body>
</head>