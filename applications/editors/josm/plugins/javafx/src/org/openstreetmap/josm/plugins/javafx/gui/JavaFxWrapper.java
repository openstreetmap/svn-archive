// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.javafx.gui;

import java.awt.Dimension;

import org.openstreetmap.josm.tools.Logging;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * This wrapper class wraps arbitrary JavaFX Nodes, so that they can easily be
 * added to a Swing UI.
 *
 * @author Taylor Smock
 * @param <T> Some class that extends {@link Node}
 */
public class JavaFxWrapper<T extends Node> extends JFXPanel {
    private static final long serialVersionUID = 1L;
    transient T node;

    /**
     * <p>
     * <b>Implementation note</b>: when the first {@code JFXPanel} object is
     * created, it implicitly initializes the JavaFX runtime. This is the preferred
     * way to initialize JavaFX in Swing. Since this class extends JFXPanel, the
     * JavaFX node should be passed as a class type and not initialized.
     *
     * @param node The JavaFX node that will be returned later with
     *             {@link JavaFxWrapper#getNode}.
     */
    public JavaFxWrapper(Class<T> node) {
        try {
            initialize(node.getConstructor().newInstance());
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
            Logging.logWithStackTrace(Logging.LEVEL_ERROR, e);
        }
    }

    /**
     * <p>
     * <b>Implementation note</b>: when the first {@code JFXPanel} object is
     * created, it implicitly initializes the JavaFX runtime. This is the preferred
     * way to initialize JavaFX in Swing. If this is the first call, please use the
     * {@code JavaFxWrapper(Class<T> node)} constructor instead.
     *
     * @param node The JavaFX node that will be returned later with
     *             {@link JavaFxWrapper#getNode}.
     */
    public JavaFxWrapper(T node) {
        initialize(node);
    }

    /**
     * This holds common initialization code
     *
     * @param node The node that should be set to this.node
     */
    private void initialize(T node) {
        this.node = node;
        Platform.runLater(this::initFX);
        Platform.setImplicitExit(false);
        this.setFocusTraversalKeysEnabled(node.isFocusTraversable());
    }

    private void initFX() {
        Scene scene = createScene();
        setScene(scene);
    }

    private Scene createScene() {
        Group group = new Group();
        Scene scene = new Scene(group);
        group.getChildren().add(node);
        return scene;
    }

    /**
     * Get the JavaFX {@link Node}
     *
     * @return The Node passed to the class during construction
     */
    public T getNode() {
        return node;
    }

    @Override
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) {
            return super.getMinimumSize();
        }
        Dimension size = null;
        if (node != null) {
            size = new Dimension();
            size.setSize(node.minWidth(-1), node.minHeight(-1));
        }
        return (size != null) ? size : super.getMinimumSize();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Dimension dimension = new Dimension();
        dimension.setSize(node.prefWidth(-1), node.prefHeight(-1));
        return dimension;
    }
}
