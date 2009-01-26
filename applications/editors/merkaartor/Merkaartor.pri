#Header files
HEADERS += \
./Command/Command.h \
./Command/DocumentCommands.h \
./Command/FeatureCommands.h \
./Command/RelationCommands.h \
./Command/RoadCommands.h \
./Command/TrackSegmentCommands.h \
./Command/TrackPointCommands.h \
./Interaction/CreateAreaInteraction.h \
./Interaction/CreateDoubleWayInteraction.h \
./Interaction/CreateNodeInteraction.h \
./Interaction/CreateRoundaboutInteraction.h \
./Interaction/CreateSingleWayInteraction.h \
./Interaction/EditInteraction.h \
./Interaction/Interaction.h \
./Interaction/MoveTrackPointInteraction.h \
./Interaction/ZoomInteraction.h \
./LayerDock.h \
./LayerWidget.h \
./IProgressWindow.h \
./MainWindow.h \
./Map/Coord.h \
./Map/DownloadOSM.h \
./Map/ExportOSM.h \
./Map/ImportGPX.h \
./Map/ImportNGT.h \
./Map/ImportOSM.h \
./Map/ImportNGT.h \
./Map/MapDocument.h \
./Map/MapLayer.h \
./Map/MapTypedef.h \
./Map/MapFeature.h \
./Map/Painting.h \
./Map/Projection.h \
./Map/Relation.h \
./Map/Road.h \
./Map/FeatureManipulations.h \
./Map/TrackPoint.h \
./Map/TrackSegment.h \
./MapView.h \
./PaintStyle/EditPaintStyle.h \
./PaintStyle/PaintStyle.h \
./PaintStyle/PaintStyleEditor.h \ 
./PaintStyle/TagSelector.h \
./PropertiesDock.h \
./InfoDock.h \
./Sync/DirtyList.h \
./Sync/SyncOSM.h \
./TagModel.h \
./Preferences/MerkaartorPreferences.h \
./Preferences/PreferencesDialog.h \
./Preferences/WMSPreferencesDialog.h \
./Preferences/TMSPreferencesDialog.h \
./Utils/LineF.h \
./Utils/ShortcutOverrideFilter.h \
./Utils/SlippyMapWidget.h \
./Utils/EditCompleterDelegate.h \
./Utils/PictureViewerDialog.h \
./Utils/PixmapWidget.h \
./Utils/SelectionDialog.h \
./Utils/SvgCache.h \
./Utils/MDockAncestor.h \
./DirtyDock.h \
./GotoDialog.h \

#Source files
SOURCES += \
./Command/Command.cpp \
./Command/DocumentCommands.cpp \
./Command/FeatureCommands.cpp \
./Command/TrackPointCommands.cpp \
./Command/RelationCommands.cpp \
./Command/RoadCommands.cpp \
./Command/TrackSegmentCommands.cpp \
./Map/Coord.cpp \
./Map/DownloadOSM.cpp \
./Map/ExportOSM.cpp \
./Map/ImportGPX.cpp \
./Map/ImportOSM.cpp \
./Map/ImportNGT.cpp \
./Map/MapDocument.cpp \
./Map/MapLayer.cpp \
./Map/MapFeature.cpp \
./Map/Painting.cpp \
./Map/Projection.cpp \
./Map/Relation.cpp \
./Map/Road.cpp \
./Map/FeatureManipulations.cpp \
./Map/TrackPoint.cpp \
./Map/TrackSegment.cpp \
./MapView.cpp \
./Interaction/CreateAreaInteraction.cpp \
./Interaction/CreateDoubleWayInteraction.cpp \
./Interaction/CreateNodeInteraction.cpp \
./Interaction/CreateSingleWayInteraction.cpp \
./Interaction/CreateRoundaboutInteraction.cpp \
./Interaction/EditInteraction.cpp \
./Interaction/Interaction.cpp \
./Interaction/MoveTrackPointInteraction.cpp \
./Interaction/ZoomInteraction.cpp \
./PaintStyle/EditPaintStyle.cpp \
./PaintStyle/PaintStyle.cpp \
./PaintStyle/PaintStyleEditor.cpp \
./PaintStyle/TagSelector.cpp \
./Sync/DirtyList.cpp \
./Sync/SyncOSM.cpp \
./Main.cpp \
./MainWindow.cpp \
./PropertiesDock.cpp \
./InfoDock.cpp \
./TagModel.cpp \
./LayerDock.cpp \
./LayerWidget.cpp \
./Utils/ShortcutOverrideFilter.cpp \
./Utils/SlippyMapWidget.cpp \
./Utils/EditCompleterDelegate.cpp \
./Utils/PictureViewerDialog.cpp \
./Utils/PixmapWidget.cpp \
./Utils/SelectionDialog.cpp \
./Utils/SvgCache.cpp \
./Utils/MDockAncestor.cpp \
./Preferences/MerkaartorPreferences.cpp \
./Preferences/PreferencesDialog.cpp \
./Preferences/WMSPreferencesDialog.cpp \
./Preferences/TMSPreferencesDialog.cpp \
./DirtyDock.cpp \
./GotoDialog.cpp \

#Forms
FORMS += \
./AboutDialog.ui \
./DownloadMapDialog.ui \
./MainWindow.ui \
./MinimumRoadProperties.ui \
./Sync/SyncListDialog.ui \
./MinimumTrackPointProperties.ui \
./UploadMapDialog.ui \
./GotoDialog.ui \
./MultiProperties.ui \
./MinimumRelationProperties.ui \
./Interaction/CreateDoubleWayDock.ui \
./Interaction/CreateRoundaboutDock.ui \
./PaintStyle/PaintStyleEditor.ui \
./Preferences/PreferencesDialog.ui \
./Preferences/WMSPreferencesDialog.ui \
./Preferences/TMSPreferencesDialog.ui \
./Utils/PictureViewerDialog.ui \
./Utils/SelectionDialog.ui \
./DirtyDock.ui \
./ExportDialog.ui

#Resource file(s)
RESOURCES += \ 
./Icons/AllIcons.qrc \
./Utils/Utils.qrc



