These programs are all designed to be run simultaneously

Data flow:

  requests -> download -> transform -> render -> split -> blankTiles -> archive -> upload


Done:
* requests - downloads rendering requests from server, stores them as empty files
* download - downloads OSM data for each request
* transform - osmarenders data into SVG files
* render - renders the SVG file into tileset images containing loads of tiles at each zoom level
  (only one copy of render can be running at once)
* split - splits those images into separate tiles
* blankTiles - detects and removes blank tile images
* archive - adds multiple tiles to a zip file, to make uploading more efficient
* upload - uploads the ZIP files to server

To do:
* server module to receive, check, decompress, and store the incoming zip files


