/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.streetside.cubemap;

import java.awt.image.BufferedImage;

import org.openstreetmap.josm.plugins.streetside.utils.GraphicsUtils;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * @author renerr18
 */
@SuppressWarnings("restriction")
public class CubemapBox extends Group {

  public enum CubemapBoxImageType {
    MULTIPLE, SINGLE
  }

  private final Affine affine = new Affine();

  private final ImageView front = new ImageView(), right = new ImageView(), back = new ImageView(),
    left = new ImageView(), up = new ImageView(), down = new ImageView();

  {
    front.setId(CubemapUtils.CubemapFaces.FRONT.getValue());
    right.setId(CubemapUtils.CubemapFaces.RIGHT.getValue());
    back.setId(CubemapUtils.CubemapFaces.BACK.getValue());
    left.setId(CubemapUtils.CubemapFaces.LEFT.getValue());
    up.setId(CubemapUtils.CubemapFaces.UP.getValue());
    down.setId(CubemapUtils.CubemapFaces.DOWN.getValue());

  }
  private final ImageView[] views = new ImageView[] { front, right, back, left, up, down };

  private Image frontImg, rightImg, backImg, leftImg, upImg, downImg, singleImg;

  private final PerspectiveCamera camera;
  private AnimationTimer timer;
  private final CubemapBoxImageType imageType;

 public CubemapBox(
    Image frontImg, Image rightImg, Image backImg, Image leftImg, Image upImg, Image downImg, double size,
    PerspectiveCamera camera) {

    super();

    imageType = CubemapBoxImageType.MULTIPLE;

    this.frontImg = frontImg;
    this.rightImg = rightImg;
    this.backImg = backImg;
    this.leftImg = leftImg;
    this.upImg = upImg;
    this.downImg = downImg;
    this.size.set(size);
    this.camera = camera;

    loadImageViews();

    getTransforms().add(affine);

    getChildren().addAll(views);

    startTimer();
  }

  public void loadImageViews() {

    for (ImageView iv : views) {
      iv.setSmooth(true);
      iv.setPreserveRatio(true);
    }

    validateImageType();
  }

  private void layoutViews() {

    for (ImageView v : views) {
      v.setFitWidth(getSize());
      v.setFitHeight(getSize());
    }

    back.setTranslateX(-0.5 * getSize());
    back.setTranslateY(-0.5 * getSize());
    back.setTranslateZ(-0.5 * getSize());

    front.setTranslateX(-0.5 * getSize());
    front.setTranslateY(-0.5 * getSize());
    front.setTranslateZ(0.5 * getSize());
    front.setRotationAxis(Rotate.Z_AXIS);
    front.setRotate(-180);
    front.getTransforms().add(new Rotate(180, front.getFitHeight() / 2, 0, 0, Rotate.X_AXIS));
    front.setTranslateY(front.getTranslateY() - getSize());

    up.setTranslateX(-0.5 * getSize());
    up.setTranslateY(-1 * getSize());
    up.setRotationAxis(Rotate.X_AXIS);
    up.setRotate(-90);

    down.setTranslateX(-0.5 * getSize());
    down.setTranslateY(0);
    down.setRotationAxis(Rotate.X_AXIS);
    down.setRotate(90);

    left.setTranslateX(-1 * getSize());
    left.setTranslateY(-0.5 * getSize());
    left.setRotationAxis(Rotate.Y_AXIS);
    left.setRotate(90);

    right.setTranslateX(0);
    right.setTranslateY(-0.5 * getSize());
    right.setRotationAxis(Rotate.Y_AXIS);
    right.setRotate(-90);

  }

  /**
   * for single image creates viewports and sets all views(image) to singleImg for multiple... sets images per view.
   */
  private void validateImageType() {
    switch (imageType) {
    case SINGLE:
      loadSingleImageViewports();
      break;
    case MULTIPLE:
      setMultipleImages();
      break;
    }
  }

  private void loadSingleImageViewports() {
    layoutViews();
    double width = singleImg.getWidth(), height = singleImg.getHeight();

    // simple check to see if cells will be square
    if (width / 4 != height / 3) {
      throw new UnsupportedOperationException("Image does not comply with size constraints");
    }
    double cellSize = singleImg.getWidth() - singleImg.getHeight();

    recalculateSize(cellSize);

    double topx = cellSize, topy = 0,

      botx = cellSize, boty = cellSize * 2,

      leftx = 0, lefty = cellSize,

      rightx = cellSize * 2, righty = cellSize,

      fwdx = cellSize, fwdy = cellSize,

      backx = cellSize * 3, backy = cellSize;

    // add top padding x+, y+, width-, height
    up.setViewport(new Rectangle2D(topx, topy, cellSize, cellSize));

    // add left padding x, y+, width, height-
    left.setViewport(new Rectangle2D(leftx, lefty, cellSize - 1, cellSize - 1));

    // add front padding x+, y+, width-, height
    front.setViewport(new Rectangle2D(fwdx, fwdy, cellSize, cellSize));

    // add right padding x, y+, width, height-
    right.setViewport(new Rectangle2D(rightx, righty, cellSize, cellSize));

    // add back padding x, y+, width, height-
    back.setViewport(new Rectangle2D(backx + 1, backy - 1, cellSize - 1, cellSize - 1));

    // add bottom padding x+, y, width-, height-
    down.setViewport(new Rectangle2D(botx, boty, cellSize, cellSize));

    for (ImageView v : views) {
      v.setImage(singleImg);
    }
  }

  private void recalculateSize(double cell) {
    double factor = Math.floor(getSize() / cell);
    setSize(cell * factor);
  }

  public synchronized void setImage(BufferedImage img, int position) {
    views[position].setImage(GraphicsUtils.convertBufferedImage2JavaFXImage(img));

  }

  private void setMultipleImages() {
    GraphicsUtils.PlatformHelper.run(() -> {
      layoutViews();
      front.setImage(frontImg);
      right.setImage(rightImg);
      back.setImage(backImg);
      left.setImage(leftImg);
      up.setImage(upImg);
      down.setImage(downImg);

    });
  }

  public void startTimer() {
    timer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        Transform ct = (camera != null) ? camera.getLocalToSceneTransform() : null;
        if (ct != null) {
          affine.setTx(ct.getTx());
          affine.setTy(ct.getTy());
          affine.setTz(ct.getTz());
        }
      }
    };
    timer.start();
  }

  /*
   * Properties
   */
  private final DoubleProperty size = new SimpleDoubleProperty() {
    @Override
    protected void invalidated() {
      switch (imageType) {
      case SINGLE:
        layoutViews();
        break;
      case MULTIPLE:
        break;
      }

    }
  };

  public final double getSize() {
    return size.get();
  }

  public final void setSize(double value) {
    size.set(value);
  }

  public DoubleProperty sizeProperty() {
    return size;
  }

  public ImageView[] getViews() {
    return views;
  }
}