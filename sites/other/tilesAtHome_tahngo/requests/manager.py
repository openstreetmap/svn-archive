from django.db import connection, backend, models
from django.core.exceptions import ObjectDoesNotExist


#------------------------------------------------------------------
# extended manager for the 'Request' model that allows
# SELECT FOR UPDATE

class RequestManager(models.Manager):

  #------------------------------------------------------------------
  # retrieve the next request that should be handed out and lock it.
  # it will only be unlocked on req.save()
  def get_next_and_lock(self):

      table = self.model._meta.db_table
      #clientping_col = self.model._meta.get_field('clientping_time').column
      query = 'select id from %s where %s=0 ' \
          'ORDER BY %s,%s LIMIT 1 FOR UPDATE' \
           % (table, 'status','priority','request_time')

      cursor = connection.cursor()
      cursor.execute(query)
      row = cursor.fetchone()
      if row: req_id = row[0]
      else  : req_id = None
      return self.get(id = req_id)


#------------------------------------------------------------------
# extended manager for the 'Upload' model that allows
# SELECT FOR UPDATE

class UploadManager(models.Manager):

  #------------------------------------------------------------------
  # retrieve the next request that should be handed out and lock it.
  # it will only be unlocked on req.save()
  def get_next_and_lock(self):

      table = self.model._meta.db_table
      #clientping_col = self.model._meta.get_field('clientping_time').column
      query = 'select id from %s where %s=0 ' \
          'ORDER BY %s LIMIT 1 FOR UPDATE' \
           % (table, 'is_locked','upload_time')

      cursor = connection.cursor()
      cursor.execute(query)
      row = cursor.fetchone()
      if row: req_id = row[0]
      else  : req_id = None
      return self.get(id = req_id)
