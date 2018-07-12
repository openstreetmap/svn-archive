//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.imageinfo;

import org.openstreetmap.josm.plugins.streetside.cubemap.CameraTransformer;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapBox;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

@SuppressWarnings("restriction")
public class ThreeSixtyDegreeViewerPanel extends JFXPanel {

  private static final long serialVersionUID = -4940350009018422000L;

  private static Scene cubemapScene;

  private static Scene defaultScene;

  private static Group root;
  private static Group subGroup;
  private static CubemapBox cubemapBox;
  private static PerspectiveCamera camera;
  private static CameraTransformer cameraTransform = new CameraTransformer();

  private static double mousePosX;
  private static double mousePosY;
  private static double mouseOldX;
  private static double mouseOldY;
  private static double mouseDeltaX;
  private static double mouseDeltaY;
  private static double cameraDistance = 5000;

  private static Image front;
  private static Image right;
  private static Image back;
  private static Image left;
  private static Image up;
  private static Image down;

  public ThreeSixtyDegreeViewerPanel() {

  }

  public void initialize() {

    root = new Group();

    camera = new PerspectiveCamera(true);
    cameraTransform.setTranslate(0, 0, 0);
    cameraTransform.getChildren().addAll(camera);
    camera.setNearClip(0.1);
    camera.setFarClip(1000000.0);
    camera.setFieldOfView(42);
    camera.setTranslateZ(-cameraDistance);
    final PointLight light = new PointLight(Color.WHITE);

    cameraTransform.getChildren().add(light);
    light.setTranslateX(camera.getTranslateX());
    light.setTranslateY(camera.getTranslateY());
    light.setTranslateZ(camera.getTranslateZ());

    root.getChildren().add(cameraTransform);

    final double size = 100000D;

    cubemapBox = new CubemapBox(front, right, back, left, up, down, size, camera);

    subGroup = new Group();
    subGroup.getChildren().add(cameraTransform);

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        setScene(createDefaultScene());
      }
    });
  }

  private static Scene createDefaultScene() {

    TextArea textArea = new TextArea();
    textArea.setText("No Streetside image selected.");

    VBox vbox = new VBox(textArea);

    root = new Group();

    camera = new PerspectiveCamera(true);
    cameraTransform.setTranslate(0, 0, 0);
    cameraTransform.getChildren().addAll(camera);
    camera.setNearClip(0.1);
    camera.setFarClip(1000000.0);
    camera.setFieldOfView(42);
    camera.setTranslateZ(-cameraDistance);
    final PointLight light = new PointLight(Color.WHITE);

    cameraTransform.getChildren().add(light);
    light.setTranslateX(camera.getTranslateX());
    light.setTranslateY(camera.getTranslateY());
    light.setTranslateZ(camera.getTranslateZ());

    root.getChildren().add(cameraTransform);

    final double size = 100000D;

    cubemapBox = new CubemapBox(null, null, null, null, null, null, size, camera);

    subGroup = new Group();
    subGroup.getChildren().add(cameraTransform);

    cubemapScene = new Scene(new Group(root), 1024, 668, true, SceneAntialiasing.BALANCED);
    cubemapScene.setFill(Color.TRANSPARENT);
    cubemapScene.setCamera(camera);

    cubemapScene.setOnKeyPressed(event -> {
      double change = 10.0;
      if (event.isShiftDown()) {
        change = 50.0;
      }
      final KeyCode keycode = event.getCode();

      if (keycode == KeyCode.W) {
        camera.setTranslateZ(camera.getTranslateZ() + change);
      }
      if (keycode == KeyCode.S) {
        camera.setTranslateZ(camera.getTranslateZ() - change);
      }

      if (keycode == KeyCode.A) {
        camera.setTranslateX(camera.getTranslateX() - change);
      }
      if (keycode == KeyCode.D) {
        camera.setTranslateX(camera.getTranslateX() + change);
      }
    });

    cubemapScene.setOnMousePressed((MouseEvent me) -> {
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
      mouseOldX = me.getSceneX();
      mouseOldY = me.getSceneY();
    });
    cubemapScene.setOnMouseDragged((MouseEvent me) -> {
      mouseOldX = mousePosX;
      mouseOldY = mousePosY;
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
      mouseDeltaX = mousePosX - mouseOldX;
      mouseDeltaY = mousePosY - mouseOldY;

      double modifier = 10.0;
      final double modifierFactor = 0.1;

      if (me.isControlDown()) {
        modifier = 0.1;
      }
      if (me.isShiftDown()) {
        modifier = 50.0;
      }
      if (me.isPrimaryButtonDown()) {
        cameraTransform.ry.setAngle(
          ((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180
        ); // +
        cameraTransform.rx.setAngle(
          ((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180
        ); // -

      } else if (me.isSecondaryButtonDown()) {
        final double z = camera.getTranslateZ();
        final double newZ = z + mouseDeltaX * modifierFactor * modifier;
        camera.setTranslateZ(newZ);
      } else if (me.isMiddleButtonDown()) {
        cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3); // -
        cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3); // -
      }
    });

    root.getChildren().addAll(cubemapBox, subGroup);
    root.setAutoSizeChildren(true);

    subGroup.setAutoSizeChildren(true);

    // prevent content from disappearing after resizing
    Platform.setImplicitExit(false);

    defaultScene = new Scene(vbox, 200, 100);
    return defaultScene;
  }

  public CubemapBox getCubemapBox() {
    if (cubemapBox == null) {
      // shouldn't happen
      initialize();
    }
    return cubemapBox;
  }

  public Scene getDefaultScene() {
    return defaultScene;
  }

  public Scene getCubemapScene() {
    return cubemapScene;
  }
}