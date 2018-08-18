// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus;

import static org.openstreetmap.josm.eventbus.JosmEventBus.post;

import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.actions.ExpertToggleAction.ExpertModeChangeListener;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.SystemOfMeasurement;
import org.openstreetmap.josm.data.SystemOfMeasurement.SoMChangeListener;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.UndoRedoHandler.CommandQueueListener;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.data.conflict.IConflictListener;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxData.GpxDataChangeListener;
import org.openstreetmap.josm.data.gpx.GpxTrack.GpxTrackChangeListener;
import org.openstreetmap.josm.data.osm.ChangesetCache;
import org.openstreetmap.josm.data.osm.ChangesetCacheListener;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.HighlightUpdateListener;
import org.openstreetmap.josm.data.osm.NoteData;
import org.openstreetmap.josm.data.osm.NoteData.NoteDataUpdateListener;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.data.osm.history.HistoryDataSet;
import org.openstreetmap.josm.data.osm.history.HistoryDataSetListener;
import org.openstreetmap.josm.data.projection.ProjectionChangeListener;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapFrame.MapModeChangeListener;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.conflict.tags.MultiValueCellEditor.NavigationListener;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog.LayerListModelListener;
import org.openstreetmap.josm.gui.dialogs.relation.IMemberModelListener;
import org.openstreetmap.josm.gui.layer.AbstractTileSourceLayer;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MainLayerManager.LayerAvailabilityEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.LayerAvailabilityListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable.PaintableInvalidationListener;
import org.openstreetmap.josm.gui.layer.NoteLayer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer.LayerStateChangeListener;
import org.openstreetmap.josm.gui.layer.imagery.ImageryFilterSettings.FilterChangeListener;
import org.openstreetmap.josm.gui.layer.imagery.TileSourceDisplaySettings.DisplaySettingsChangeListener;
import org.openstreetmap.josm.gui.mappaint.MapPaintStyles;
import org.openstreetmap.josm.gui.mappaint.MapPaintStyles.MapPaintSylesUpdateListener;
import org.openstreetmap.josm.gui.preferences.imagery.AddImageryPanel.ContentValidationListener;
import org.openstreetmap.josm.gui.preferences.server.ProxyPreference;
import org.openstreetmap.josm.gui.preferences.server.ProxyPreferenceListener;
import org.openstreetmap.josm.gui.progress.ProgressMonitor.CancelListener;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetListener;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresets;
import org.openstreetmap.josm.gui.util.KeyPressReleaseListener;
import org.openstreetmap.josm.gui.util.ModifierExListener;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.io.OsmApi.OsmApiInitializationListener;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.eventbus.actions.ExpertModeChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.CommandQueueEvent;
import org.openstreetmap.josm.plugins.eventbus.data.SoMChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.conflict.ConflictsAddedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.conflict.ConflictsRemovedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.osm.NoteDataUpdatedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.osm.SelectedNoteChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.osm.history.HistoryClearedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.osm.history.HistoryUpdatedEvent;
import org.openstreetmap.josm.plugins.eventbus.data.projection.ProjectionChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.MapFrameInitializedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.MapModeChangeEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.ZoomChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.conflict.tags.NextDecisionEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.conflict.tags.PreviousDecisionEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.dialogs.LayerListRefreshedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.dialogs.LayerVisibleEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.dialogs.relation.MemberVisibleEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.layer.LayerStateChangeEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.layer.imagery.FilterChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.mappaint.MapPaintStyleEntryUpdatedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.mappaint.MapPaintStylesUpdatedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.preferences.imagery.ContentValidationEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.preferences.server.ProxyPreferenceChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.progress.OperationCancelledEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.tagging.presets.TaggingPresetModifiedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.util.KeyPressedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.util.KeyReleasedEvent;
import org.openstreetmap.josm.plugins.eventbus.gui.util.ModifierExChangedEvent;
import org.openstreetmap.josm.plugins.eventbus.io.OsmApiInitializedEvent;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.PreferenceChangedListener;
import org.openstreetmap.josm.tools.Logging;

/**
 * Event bus plugin, providing an event bus more powerful than the traditional listeners registration.
 */
public class EventBusPlugin extends Plugin {

    // Defines an instance for each type of listener, and keeps a reference to avoid garbage collection

    private final ActiveLayerChangeListener activeLayerChangeListener = e -> post(e);
    private final ChangesetCacheListener changesetCacheListener = e -> post(e);
    private final DataSelectionListener selectionListener = e -> post(e);
    private final ExpertModeChangeListener expertModeChangeListener = x -> post(new ExpertModeChangedEvent(this, x));
    private final GpxDataChangeListener gpxChangeListener = e -> post(e);
    private final GpxTrackChangeListener gpxTrackChangeListener = e -> post(e);
    private final HighlightUpdateListener highlightUpdateListener = e -> post(e);
    private final SelectionChangedListener selectionChangedListener = e -> post(e);
    private final PreferenceChangedListener preferenceChangedListener = e -> post(e);
    private final PaintableInvalidationListener paintableInvalidationListener = e -> post(e);
    private final NoteDataUpdateListener noteDataUpdateListener = new NoteDataUpdateListener() {
        @Override
        public void selectedNoteChanged(NoteData noteData) {
            post(new SelectedNoteChangedEvent(this, noteData));
        }

        @Override
        public void noteDataUpdated(NoteData data) {
            post(new NoteDataUpdatedEvent(this, data));
        }
    };

    private final IConflictListener conflictsListener = new IConflictListener() {
        @Override
        public void onConflictsRemoved(ConflictCollection conflicts) {
            post(new ConflictsRemovedEvent(this, conflicts));
        }

        @Override
        public void onConflictsAdded(ConflictCollection conflicts) {
            post(new ConflictsAddedEvent(this, conflicts));
        }
    };

    private final FilterChangeListener filterChangeListener = () -> post(new FilterChangedEvent(this));
    private final DisplaySettingsChangeListener displaySettingsChangeListener = e -> post(e);

    private final LayerStateChangeListener layerStateChangeListener =
            (layer, newValue) -> post(new LayerStateChangeEvent(this, layer, newValue));

    private final LayerAvailabilityListener layerAvailabilityListener = new LayerAvailabilityListener() {
        @Override
        public void beforeFirstLayerAdded(LayerAvailabilityEvent e) {
            post(e);
        }

        @Override
        public void afterLastLayerRemoved(LayerAvailabilityEvent e) {
            post(e);
        }
    };

    // CHECKSTYLE.OFF: AnonInnerLengthCheck
    private final LayerChangeListener layerChangeListener = new LayerChangeListener() {
        @Override
        public void layerAdded(LayerAddEvent e) {
            post(e);
            Layer layer = e.getAddedLayer();
            layer.addInvalidationListener(paintableInvalidationListener);
            if (layer instanceof OsmDataLayer) {
                ((OsmDataLayer) layer).addLayerStateChangeListener(layerStateChangeListener);
                DataSet ds = ((OsmDataLayer) layer).getDataSet();
                ds.addDataSetListener(dataSetListener);
                ds.addHighlightUpdateListener(highlightUpdateListener);
                ds.addSelectionListener(selectionListener);
                ds.getConflicts().addConflictListener(conflictsListener);
            } else if (layer instanceof GpxLayer) {
                GpxData gpx = ((GpxLayer) layer).data;
                gpx.addChangeListener(gpxChangeListener);
                // TODO: cannot add a listener for GpxTrackChangeListener. It would require first new events for tracks being added and removed
                Logging.debug("TODO: add" + gpxTrackChangeListener);
            } else if (layer instanceof NoteLayer) {
                NoteData notes = ((NoteLayer) layer).getNoteData();
                notes.addNoteDataUpdateListener(noteDataUpdateListener);
            } else if (layer instanceof ImageryLayer) {
                ((ImageryLayer) layer).getFilterSettings().addFilterChangeListener(filterChangeListener);
                if (layer instanceof AbstractTileSourceLayer) {
                    ((AbstractTileSourceLayer<?>) layer).getDisplaySettings().addSettingsChangeListener(displaySettingsChangeListener);
                }
            }
        }

        @Override
        public void layerRemoving(LayerRemoveEvent e) {
            post(e);
            Layer layer = e.getRemovedLayer();
            layer.removeInvalidationListener(paintableInvalidationListener);
            if (layer instanceof OsmDataLayer) {
                ((OsmDataLayer) layer).removeLayerStateChangeListener(layerStateChangeListener);
                DataSet ds = ((OsmDataLayer) layer).getDataSet();
                ds.removeDataSetListener(dataSetListener);
                ds.removeHighlightUpdateListener(highlightUpdateListener);
                ds.removeSelectionListener(selectionListener);
                ds.getConflicts().removeConflictListener(conflictsListener);
                // TODO: cannot add a listener for GpxTrackChangeListener. It would require first new events for tracks being added and removed
                Logging.debug("TODO: remove" + gpxTrackChangeListener);
            } else if (layer instanceof GpxLayer) {
                GpxData gpx = ((GpxLayer) layer).data;
                gpx.removeChangeListener(gpxChangeListener);
            } else if (layer instanceof NoteLayer) {
                NoteData notes = ((NoteLayer) layer).getNoteData();
                notes.removeNoteDataUpdateListener(noteDataUpdateListener);
            } else if (layer instanceof ImageryLayer) {
                ((ImageryLayer) layer).getFilterSettings().removeFilterChangeListener(filterChangeListener);
                if (layer instanceof AbstractTileSourceLayer) {
                    ((AbstractTileSourceLayer<?>) layer).getDisplaySettings().removeSettingsChangeListener(displaySettingsChangeListener);
                }
            }
        }

        @Override
        public void layerOrderChanged(LayerOrderChangeEvent e) {
            post(e);
        }
    };
    // CHECKSTYLE.ON: AnonInnerLengthCheck

    private final DataSetListener dataSetListener = new DataSetListener() {
        @Override
        public void wayNodesChanged(WayNodesChangedEvent event) {
            post(event);
        }

        @Override
        public void tagsChanged(TagsChangedEvent event) {
            post(event);
        }

        @Override
        public void relationMembersChanged(RelationMembersChangedEvent event) {
            post(event);
        }

        @Override
        public void primitivesRemoved(PrimitivesRemovedEvent event) {
            post(event);
        }

        @Override
        public void primitivesAdded(PrimitivesAddedEvent event) {
            post(event);
        }

        @Override
        public void otherDatasetChange(AbstractDatasetChangedEvent event) {
            post(event);
        }

        @Override
        public void nodeMoved(NodeMovedEvent event) {
            post(event);
        }

        @Override
        public void dataChanged(DataChangedEvent event) {
            post(event);
        }
    };

    private final HistoryDataSetListener historyDataSetListener = new HistoryDataSetListener() {
        @Override
        public void historyUpdated(HistoryDataSet source, PrimitiveId id) {
            post(new HistoryUpdatedEvent(this, source, id));
        }

        @Override
        public void historyDataSetCleared(HistoryDataSet source) {
            post(new HistoryClearedEvent(this, source));
        }
    };

    private final ProjectionChangeListener projectionChangeListener = (oldValue, newValue) -> {
        post(new ProjectionChangedEvent(this, oldValue, newValue));
    };

    private final SoMChangeListener soMChangeListener = (oldSoM, newSoM) -> {
        post(new SoMChangedEvent(this, oldSoM, newSoM));
    };

    private final CommandQueueListener commandQueueListener = (queueSize, redoSize) -> {
        post(new CommandQueueEvent(this, queueSize, redoSize));
    };

    private final NavigationListener navigationListener = new NavigationListener() {
        @Override
        public void gotoNextDecision() {
            post(new NextDecisionEvent(this));
        }

        @Override
        public void gotoPreviousDecision() {
            post(new PreviousDecisionEvent(this));
        }
    };

    private final LayerListModelListener layerListModelListener = new LayerListModelListener() {
        @Override
        public void makeVisible(int index, Layer layer) {
            post(new LayerVisibleEvent(this, index, layer));
        }

        @Override
        public void refresh() {
            post(new LayerListRefreshedEvent(this));
        }
    };

    private final IMemberModelListener memberModelListener = index -> post(new MemberVisibleEvent(this, index));
    // private final DownloadSourceListener downloadSourceListener = source -> post(new DownloadSourceAddedEvent(source)); // TODO: not public
    private final ContentValidationListener contentValidationListener = isValid -> post(new ContentValidationEvent(this, isValid));
    private final ProxyPreferenceListener proxyPreferenceListener = () -> post(new ProxyPreferenceChangedEvent(this));
    private final CancelListener cancelListener = () -> post(new OperationCancelledEvent(this));
    private final TaggingPresetListener taggingPresetListener = () -> post(new TaggingPresetModifiedEvent(this));

    private final MapPaintSylesUpdateListener mapPaintSylesUpdateListener = new MapPaintSylesUpdateListener() {
        @Override
        public void mapPaintStylesUpdated() {
            post(new MapPaintStylesUpdatedEvent(this));
        }

        @Override
        public void mapPaintStyleEntryUpdated(int index) {
            post(new MapPaintStyleEntryUpdatedEvent(this, index));
        }
    };

    private final OsmApiInitializationListener osmApiInitializationListener = api -> post(new OsmApiInitializedEvent(this, api));
    //private final AudioListener audioListener = url -> post(new AudioPlayingEvent(url)); // TODO: not public
    private final ZoomChangeListener zoomChangeListener = () -> post(new ZoomChangedEvent(this));
    private final MapModeChangeListener mapModeChangeListener = (oldMode, newMode) -> post(new MapModeChangeEvent(this, oldMode, newMode));
    private final ModifierExListener modifierExListener = modifiers -> post(new ModifierExChangedEvent(this, modifiers));
    private final KeyPressReleaseListener keyPressReleaseListener = new KeyPressReleaseListener() {
        @Override
        public void doKeyPressed(KeyEvent e) {
            post(new KeyPressedEvent(this, e));
        }

        @Override
        public void doKeyReleased(KeyEvent e) {
            post(new KeyReleasedEvent(this, e));
        }
    };

    /**
     * Constructs a new {@code EventBusPlugin}.
     * @param info plugin information
     */
    public EventBusPlugin(PluginInformation info) {
        super(info);
        registerAllJosmListeners();
        // TODO: cannot add a listener for NavigationListener. It would require first new events for MultiValueCellEditor being created
        Logging.debug("TODO: add" + navigationListener);
        // TODO: cannot add a listener for IMemberModelListener. It would require first new events for GenericRelationEditor being created
        Logging.debug("TODO: add" + memberModelListener);
        // TODO: cannot add a listener for ContentValidationListener. It would require first new events for AddImageryDialog being created
        Logging.debug("TODO: add" + contentValidationListener);
        // TODO: cannot add a listener for CancelListener. It would require first new events for ProgressMonitor being created
        Logging.debug("TODO: add" + cancelListener);
    }

    /**
     * Registers all JOSM listeners.
     */
    void registerAllJosmListeners() {
        ProjectionRegistry.addProjectionChangeListener(projectionChangeListener);
        MainApplication.getLayerManager().addLayerChangeListener(layerChangeListener);
        MainApplication.getLayerManager().addActiveLayerChangeListener(activeLayerChangeListener);
        MainApplication.getLayerManager().addLayerAvailabilityListener(layerAvailabilityListener);
        UndoRedoHandler.getInstance().addCommandQueueListener(commandQueueListener);
        ChangesetCache.getInstance().addChangesetCacheListener(changesetCacheListener);
        DataSet.addSelectionListener(selectionChangedListener);
        ExpertToggleAction.addExpertModeChangeListener(expertModeChangeListener);
        HistoryDataSet.getInstance().addHistoryDataSetListener(historyDataSetListener);
        SystemOfMeasurement.addSoMChangeListener(soMChangeListener);
        MapPaintStyles.addMapPaintSylesUpdateListener(mapPaintSylesUpdateListener);
        ProxyPreference.addProxyPreferenceListener(proxyPreferenceListener);
        MapFrame.addMapModeChangeListener(mapModeChangeListener);
        NavigatableComponent.addZoomChangeListener(zoomChangeListener);
        TaggingPresets.addListener(taggingPresetListener);
        OsmApi.addOsmApiInitializationListener(osmApiInitializationListener);
        Config.getPref().addPreferenceChangeListener(preferenceChangedListener);
    }

    /**
     * Unregisters all JOSM listeners.
     */
    void unregisterAllJosmListeners() {
        ProjectionRegistry.removeProjectionChangeListener(projectionChangeListener);
        MainApplication.getLayerManager().removeLayerChangeListener(layerChangeListener);
        MainApplication.getLayerManager().removeActiveLayerChangeListener(activeLayerChangeListener);
        MainApplication.getLayerManager().removeLayerAvailabilityListener(layerAvailabilityListener);
        UndoRedoHandler.getInstance().removeCommandQueueListener(commandQueueListener);
        ChangesetCache.getInstance().removeChangesetCacheListener(changesetCacheListener);
        DataSet.removeSelectionListener(selectionChangedListener);
        ExpertToggleAction.removeExpertModeChangeListener(expertModeChangeListener);
        HistoryDataSet.getInstance().removeHistoryDataSetListener(historyDataSetListener);
        SystemOfMeasurement.removeSoMChangeListener(soMChangeListener);
        MapPaintStyles.removeMapPaintSylesUpdateListener(mapPaintSylesUpdateListener);
        ProxyPreference.removeProxyPreferenceListener(proxyPreferenceListener);
        MapFrame.removeMapModeChangeListener(mapModeChangeListener);
        NavigatableComponent.removeZoomChangeListener(zoomChangeListener);
        TaggingPresets.removeListener(taggingPresetListener);
        OsmApi.removeOsmApiInitializationListener(osmApiInitializationListener);
        Config.getPref().removePreferenceChangeListener(preferenceChangedListener);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        post(new MapFrameInitializedEvent(this, oldFrame, newFrame));
        if (newFrame != null) {
            LayerListDialog dlg = newFrame.getToggleDialog(LayerListDialog.class);
            if (dlg != null) {
                dlg.getModel().addLayerListModelListener(layerListModelListener);
            }
            newFrame.keyDetector.addKeyListener(keyPressReleaseListener);
            newFrame.keyDetector.addModifierExListener(modifierExListener);
        }
        if (oldFrame != null) {
            LayerListDialog dlg = oldFrame.getToggleDialog(LayerListDialog.class);
            if (dlg != null) {
                dlg.getModel().removeLayerListModelListener(layerListModelListener);
            }
            oldFrame.keyDetector.removeKeyListener(keyPressReleaseListener);
            oldFrame.keyDetector.removeModifierExListener(modifierExListener);
        }
    }
}
