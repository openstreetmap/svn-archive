//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.imageinfo;

import java.awt.image.BufferedImage;

import org.openstreetmap.josm.plugins.streetside.cubemap.CameraTransformer;
import org.openstreetmap.josm.plugins.streetside.cubemap.GraphicsUtils;
import org.openstreetmap.josm.plugins.streetside.utils.CubemapBox;

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
import javafx.scene.transform.NonInvertibleTransformException;


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

	// Supply Image Paths or a NullPointer will occur
	private static Image front;
	private static Image right;
	private static Image back;
	private static Image left;
	private static Image up;
	private static Image down;

	public ThreeSixtyDegreeViewerPanel() {
		// constructor
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
		// cameraTransform.ry.setAngle(-45.0);
		// cameraTransform.rx.setAngle(-10.0);
		// add a Point Light for better viewing of the grid coordinate system
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
				//try {
					setScene(createDefaultScene());
				  //setScene(createScene());
				/*} catch (NonInvertibleTransformException e) {
					Logging.error(I18n.tr("Error initializing StreetsideViewerPanel - JavaFX {0}", e.getMessage()));
				}*/
			}
		});
	}

	public static Scene createScene() /*throws NonInvertibleTransformException*/ {

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

		// Load Cubemap box AFTER camera is initialized
		final double size = 100000D;

		cubemapBox = new CubemapBox(null, null, null, null, null, null, size, camera);

		subGroup = new Group();
		subGroup.getChildren().add(cameraTransform);

		final Scene scene = new Scene(new Group(root), 1024, 668, true, SceneAntialiasing.BALANCED);
		scene.setFill(Color.TRANSPARENT);
		scene.setCamera(camera);

		// First person shooter keyboard movement
		scene.setOnKeyPressed(event -> {
			double change = 10.0;
			// Add shift modifier to simulate "Running Speed"
			if (event.isShiftDown()) {
				change = 50.0;
			}
			// What key did the user press?
			final KeyCode keycode = event.getCode();
			// Step 2c: Add Zoom controls
			if (keycode == KeyCode.W) {
				camera.setTranslateZ(camera.getTranslateZ() + change);
			}
			if (keycode == KeyCode.S) {
				camera.setTranslateZ(camera.getTranslateZ() - change);
			}
			// Step 2d: Add Strafe controls
			if (keycode == KeyCode.A) {
				camera.setTranslateX(camera.getTranslateX() - change);
			}
			if (keycode == KeyCode.D) {
				camera.setTranslateX(camera.getTranslateX() + change);
			}
		});

		scene.setOnMousePressed((MouseEvent me) -> {
			mousePosX = me.getSceneX();
			mousePosY = me.getSceneY();
			mouseOldX = me.getSceneX();
			mouseOldY = me.getSceneY();
		});
		scene.setOnMouseDragged((MouseEvent me) -> {
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
						((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540)
								% 360 - 180); // +
				cameraTransform.rx.setAngle(
						((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540)
								% 360 - 180); // -

			} else if (me.isSecondaryButtonDown()) {
				final double z = camera.getTranslateZ();
				final double newZ = z + mouseDeltaX * modifierFactor * modifier;
				camera.setTranslateZ(newZ);
			} else if (me.isMiddleButtonDown()) {
				cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3); // -
				cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3); // -
			}
		});

		/*scene.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		        System.out.println("Width: " + newSceneWidth);
		    }

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldSceneWidth, Number newSceneWidth) {
				draw();
			}
		});*/
		/*scene.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		        //System.out.println("Height: " + newSceneHeight);
		    	draw();
		    }
		});*/

		root.getChildren().addAll(cubemapBox, subGroup);
		root.setAutoSizeChildren(true);

		subGroup.setAutoSizeChildren(true);

		// prevent content from disappearing after resizing
		Platform.setImplicitExit(false);

		return scene;
	}

	private static Scene createDefaultScene() {
		// TODO: default scene with message? @rrh

		// Load Cubemap box AFTER camera is initialized
		//final double size = 100000D;

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

    // Load Cubemap box AFTER camera is initialized
    final double size = 100000D;

    cubemapBox = new CubemapBox(null, null, null, null, null, null, size, camera);

    subGroup = new Group();
    subGroup.getChildren().add(cameraTransform);

    /*final Scene*/ cubemapScene = new Scene(new Group(root), 1024, 668, true, SceneAntialiasing.BALANCED);
    cubemapScene.setFill(Color.TRANSPARENT);
    cubemapScene.setCamera(camera);

    // First person shooter keyboard movement
    cubemapScene.setOnKeyPressed(event -> {
      double change = 10.0;
      // Add shift modifier to simulate "Running Speed"
      if (event.isShiftDown()) {
        change = 50.0;
      }
      // What key did the user press?
      final KeyCode keycode = event.getCode();
      // Step 2c: Add Zoom controls
      if (keycode == KeyCode.W) {
        camera.setTranslateZ(camera.getTranslateZ() + change);
      }
      if (keycode == KeyCode.S) {
        camera.setTranslateZ(camera.getTranslateZ() - change);
      }
      // Step 2d: Add Strafe controls
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
            ((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540)
                % 360 - 180); // +
        cameraTransform.rx.setAngle(
            ((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540)
                % 360 - 180); // -

      } else if (me.isSecondaryButtonDown()) {
        final double z = camera.getTranslateZ();
        final double newZ = z + mouseDeltaX * modifierFactor * modifier;
        camera.setTranslateZ(newZ);
      } else if (me.isMiddleButtonDown()) {
        cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3); // -
        cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3); // -
      }
    });

    /*scene.widthProperty().addListener(new ChangeListener<Number>() {
        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
            System.out.println("Width: " + newSceneWidth);
        }

      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldSceneWidth, Number newSceneWidth) {
        draw();
      }
    });*/
    /*scene.heightProperty().addListener(new ChangeListener<Number>() {
        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
            //System.out.println("Height: " + newSceneHeight);
          draw();
        }
    });*/

    root.getChildren().addAll(cubemapBox, subGroup);
    root.setAutoSizeChildren(true);

    subGroup.setAutoSizeChildren(true);

    // prevent content from disappearing after resizing
    Platform.setImplicitExit(false);

    //return scene;

		defaultScene = new Scene(vbox, 200, 100);
		return defaultScene;
	}

	public static Scene createScene(BufferedImage img0, BufferedImage img1, BufferedImage img2, BufferedImage img3,
			BufferedImage img4, BufferedImage img5) throws NonInvertibleTransformException {
		front = GraphicsUtils.convertBufferedImage2JavaFXImage(img0);
		right = GraphicsUtils.convertBufferedImage2JavaFXImage(img1);
		back = GraphicsUtils.convertBufferedImage2JavaFXImage(img2);
		left = GraphicsUtils.convertBufferedImage2JavaFXImage(img3);
		up = GraphicsUtils.convertBufferedImage2JavaFXImage(img4);
		down = GraphicsUtils.convertBufferedImage2JavaFXImage(img5);

		root = new Group();

		camera = new PerspectiveCamera(true);
		cameraTransform.setTranslate(0, 0, 0);
		cameraTransform.getChildren().addAll(camera);
		camera.setNearClip(0.1);
		camera.setFarClip(1000000.0);
		camera.setFieldOfView(42);
		camera.setTranslateZ(-cameraDistance);
		// cameraTransform.ry.setAngle(-45.0);
		// cameraTransform.rx.setAngle(-10.0);
		// add a Point Light for better viewing of the grid coordinate system
		final PointLight light = new PointLight(Color.WHITE);

		cameraTransform.getChildren().add(light);
		light.setTranslateX(camera.getTranslateX());
		light.setTranslateY(camera.getTranslateY());
		light.setTranslateZ(camera.getTranslateZ());

		root.getChildren().add(cameraTransform);

		// Load Cubemap box AFTER camera is initialized
		final double size = 100000D;

		cubemapBox = new CubemapBox(front, right, back, left, up, down, size, camera);

		final Group torusGroup = new Group();
		torusGroup.getChildren().add(cameraTransform);

		final Scene scene = new Scene(new Group(root), 1024, 668, true, SceneAntialiasing.BALANCED);
		scene.setFill(Color.TRANSPARENT);
		scene.setCamera(camera);

		// First person shooter keyboard movement
		scene.setOnKeyPressed(event -> {
			double change = 10.0;
			// Add shift modifier to simulate "Running Speed"
			if (event.isShiftDown()) {
				change = 50.0;
			}
			// What key did the user press?
			final KeyCode keycode = event.getCode();
			// Step 2c: Add Zoom controls
			if (keycode == KeyCode.W) {
				camera.setTranslateZ(camera.getTranslateZ() + change);
			}
			if (keycode == KeyCode.S) {
				camera.setTranslateZ(camera.getTranslateZ() - change);
			}
			// Step 2d: Add Strafe controls
			if (keycode == KeyCode.A) {
				camera.setTranslateX(camera.getTranslateX() - change);
			}
			if (keycode == KeyCode.D) {
				camera.setTranslateX(camera.getTranslateX() + change);
			}

		});

		scene.setOnMousePressed((MouseEvent me) -> {
			mousePosX = me.getSceneX();
			mousePosY = me.getSceneY();
			mouseOldX = me.getSceneX();
			mouseOldY = me.getSceneY();
		});
		scene.setOnMouseDragged((MouseEvent me) -> {
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
						((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540)
								% 360 - 180); // +
				cameraTransform.rx.setAngle(
						((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540)
								% 360 - 180); // -

			} else if (me.isSecondaryButtonDown()) {
				final double z = camera.getTranslateZ();
				final double newZ = z + mouseDeltaX * modifierFactor * modifier;
				camera.setTranslateZ(newZ);
			} else if (me.isMiddleButtonDown()) {
				cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3); // -
				cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3); // -
			}
		});

		root.getChildren().addAll(cubemapBox, torusGroup);
		root.setAutoSizeChildren(true);

		return scene;
	}

	/*public void setCubemapImages(BufferedImage img, BufferedImage img1, BufferedImage img2, BufferedImage img3,
			BufferedImage img4, BufferedImage img5) {
		cubemapBox = null;

		GraphicsUtils.PlatformHelper.run(new Runnable() {
			@Override
			public void run() {
				try {
					// initialize without imagery.
					scene = createScene(img, img1, img2, img3, img4, img5);
					setScene(scene);
				} catch (NonInvertibleTransformException e) {
					Logging.error(I18n.tr("Error initializing StreetsideViewerPanel - JavaFX {0}", e.getMessage()));
				}
			}
		});
	}*/

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