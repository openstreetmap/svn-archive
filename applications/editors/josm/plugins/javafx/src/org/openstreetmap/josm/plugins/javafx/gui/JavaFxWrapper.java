// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.javafx.gui;
import java.awt.Dimension;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

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
     * Catch exceptions in the JavaFX thread (only instantiated with the JavaFxWrapper).
     * Since most exceptions should be seen through the `future.get()` method in initialize, this is (mostly) safe.
     */
    private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler currentHandler;
        public UncaughtExceptionHandler() {
            currentHandler = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler(this);
        }
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (currentHandler != null && !(e instanceof NoClassDefFoundError)) {
                currentHandler.uncaughtException(t, e);
            } else {
                Logging.error(e);
            }
        }
    }

    private static UncaughtExceptionHandler handler;

    /**
     * <p>
     * <b>Implementation note</b>: when the first {@code JFXPanel} object is
     * created, it implicitly initializes the JavaFX runtime. This is the preferred
     * way to initialize JavaFX in Swing. Since this class extends JFXPanel, the
     * JavaFX node should be passed as a class type and not initialized.
     *
     * @param node The JavaFX node that will be returned later with
     *             {@link JavaFxWrapper#getNode}.
     * @throws ExecutionException If something happened during execution. If this happens, fall back to something else!
     */
    public JavaFxWrapper(Class<T> node) throws ExecutionException {
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
     * @throws ExecutionException If something happened during execution. If this happens, fall back to something else!
     */
    public JavaFxWrapper(T node) throws ExecutionException {
        initialize(node);
    }

    /**
     * This holds common initialization code
     *
     * @param node The node that should be set to this.node
     * @throws ExecutionException If something happened during execution. If this happens, fall back to something else!
     */
    private void initialize(T node) throws ExecutionException {
        this.node = node;
        FutureTask<Scene> task = new FutureTask<>(this::initFX);
        Platform.runLater(task);
        Platform.setImplicitExit(false);
        this.setFocusTraversalKeysEnabled(node.isFocusTraversable());
        try {
            task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logging.error(e);
        }
    }

    /**
     * @return The scene to be used for initializing JavaFX
     */
    protected Scene initFX() {
        initializeExceptionHandler();
        Scene scene = createScene();
        setScene(scene);
        return scene;
    }

    private static void initializeExceptionHandler() {
        if (handler == null)
            handler = new UncaughtExceptionHandler();
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
