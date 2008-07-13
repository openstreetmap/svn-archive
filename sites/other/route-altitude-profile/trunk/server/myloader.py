from google.appengine.ext import bulkload
from google.appengine.api import datastore_types
from google.appengine.api import datastore
from google.appengine.ext import search

class AltitudeLoader(bulkload.Loader):
  def __init__(self):
    bulkload.Loader.__init__(self, 'Altitude',
                         [('pos', int),
                          ('alt', int)
                          ])

  def HandleEntity(self, entity):
    name="P" + str(entity['pos'])
    del entity['pos']
    newent = datastore.Entity('Altitude',name=name)
    newent.update(entity)
    return newent

if __name__ == '__main__':
  bulkload.main(AltitudeLoader())
