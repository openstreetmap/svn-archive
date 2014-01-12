/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust.gui;


import java.util.List;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustAction;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustServiceCommand;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandler;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandlerException;


/**
 * Uploads the user's modifications to the MapDust service.
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustActionUploader {

    /** The <code>MapdustActionUploader</code> instance */
    private static MapdustActionUploader instance;

    /** The <code>MapdustServiceHandler</code> object */
    private static MapdustServiceHandler handler;

    /**
     * Builds a <code>MapdustActionUploader</code> object
     */
    private MapdustActionUploader() {
        handler = new MapdustServiceHandler();
    }

    /**
     * Returns a new instance of the <code>MapdustActionUploader</code> object.
     * If the instance is null, then creates a new one. Otherwise returns the
     * existing instance.
     *
     * @return new instance of <code>MapdustActionUploader</code>
     */
    public static MapdustActionUploader getInstance() {
        if (instance == null) {
            instance = new MapdustActionUploader();
        }
        return instance;
    }

    /**
     * Uploads the given data to the MapdDst service.
     *
     * @param actionList A list of <code>MapdustAction</code> objects
     * @throws MapdustActionUploaderException If there was some error during the
     * upload to MapDust service
     */
    public void uploadData(List<MapdustAction> actionList)
            throws MapdustActionUploaderException {
        if (actionList != null && actionList.size() > 0) {
            try {
                for (MapdustAction action : actionList) {
                    dispatchAction(action);
                }
            } catch (MapdustServiceHandlerException e) {
                throw new MapdustActionUploaderException(
                        "Error durring uploading data to Mapdust service!", e);
            }
        }

    }

    /**
     * Handles the given action based on the command type. If the command is
     * "addBug" then creates a new bug. If the command is "commentBug" then
     * creates a new comment for the given bug. Otherwise changes the status of
     * the given bug.
     *
     * @param action A <code>MapdustAction</code> object
     * @throws MapdustServiceHandlerException If there was some error during the
     * upload to MapDust service
     */
    private void dispatchAction(MapdustAction action)
            throws MapdustServiceHandlerException {
        if (action.getCommand().equals(MapdustServiceCommand.ADD_BUG)) {
            /* create new bug */
            handler.addBug(action.getMapdustBug());
        } else {
            if (action.getCommand().equals(MapdustServiceCommand.COMMENT_BUG)) {
                /* comment bug */
                handler.commentBug(action.getMapdustComment());
            } else {
                /* change bug status */
                handler.changeBugStatus(action.getNewStatus(),
                        action.getMapdustComment());
            }
        }
    }

}
