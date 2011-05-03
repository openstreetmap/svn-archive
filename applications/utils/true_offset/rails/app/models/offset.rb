# True Offset Process 
#
# Copyright (c) 2010-2011, Dermot McNally <dermotm@gmail.com>
# Copyright (c) 2010-2011, Bartosz Fabianowski <bartosz@fabianowski.eu>
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of the author nor the names of its contributors may be used
#   to endorse or promote products derived from this software without specific
#   prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# $Id$



class Offset < ActiveRecord::Base
 def self.lookup(provider, zoom, lat, lon)
   sql = <<-SQL
     SELECT
    	 name,
	TRUE AS match,
	:lat AS lat, 
	:lon AS lon,
	provider,
	zoom_min,
	zoom_max,
	offset_north,
	offset_east,
       ST_Distance(boundary::geometry, 'POINT(:lon :lat)'::geometry) AS radius
     FROM
       offsets
     WHERE
       provider = :provider
       AND
       (zoom_min IS NULL OR zoom_min <= :zoom)
       AND
       (zoom_max IS NULL OR zoom_max >= :zoom)
       AND
       boundary && ST_GeomFromText('POINT(:lon :lat)', -1)

	UNION

     SELECT
         name,
        FALSE AS match,
        :lat AS lat, 
        :lon AS lon,
        provider,
        NULL AS zoom_min,
        NULL AS zoom_max,
        NULL AS offset_north,
        NULL AS offset_east,
       ST_Distance(boundary::geometry, 'POINT(:lon :lat)'::geometry) AS radius
     FROM
       offsets
     WHERE
       provider = :provider
	AND
	boundary && ST_GeomFromText('LINESTRING(:west :north, :east :north, :east :south, :west :south, :west :north)', -1)

	UNION

     SELECT
        'No Match' AS name,
        FALSE AS match,
        :lat AS lat, 
        :lon AS lon,
        :provider AS provider,
        NULL AS zoom_min,
        NULL AS zoom_max,
        NULL AS offset_north,
        NULL AS offset_east,
	10.0 AS radius

	ORDER BY match DESC, radius
	LIMIT 1

   SQL

   self.find_by_sql([sql, {:provider => provider, :zoom => zoom, :lat => lat.to_f, :lon => lon.to_f, :north => lat.to_f + 5.0, :south => lat.to_f - 5.0, :east => lon.to_f + 5.0, :west => lon.to_f - 5.0}])
 end


def bbox_north
	lat.to_f + bbox_offset
end

def bbox_south
	lat.to_f - bbox_offset
end

def bbox_east
	lon.to_f + bbox_offset
end

def bbox_west
	lon.to_f - bbox_offset
end

def bbox_offset
	[radius_to_bbox_offset, 5.0].min
end

private

def radius_to_bbox_offset
	Math.sqrt(radius.to_f**2 / 2)
end

end
