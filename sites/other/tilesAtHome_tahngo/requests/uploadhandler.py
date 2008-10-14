from tempfile import mkstemp

# handle uploaded tileset file
# returns true, if it has been successfully handled (no need
# to save the entry in the db anymore)
def handle_uploaded_tileset(file, form):
  # don't handle .zip files but save for later use
  if file.name.lower().endswith('.zip'):
      return False

  #if temporary_file_path exists, the file has been saved, otherwise it's in RAM
  #try: fullpath = file.temporary_file_path()
  #except:
  #    (tmp_fd,tmpfile) = mkstemp()
  #    f = os.fdopen(tmp_fd, 'w+b')
  #    for chunk in file.chunks():
  #      f.write(chunk)
  #    f.close()
  # TODO set upload user id field
  #upload user id form['user_id']
  return False
