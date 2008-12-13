#include <stdio.h>
#include <stdlib.h>

#include <webkit.h>

#define WIDTH 2000

/* compile with:
gcc -W webkit-image-gtk.cpp `pkg-config --libs --cflags WebKitGtk gtk+-2.0`
*/

static void
on_finished (WebKitWebView *view, WebKitWebFrame *frame)
{
        GdkPixmap *pixmap;
        GdkColormap *cmap;
        GdkPixbuf *pixbuf;
        gchar *buffer;
        gsize size;

        pixmap = gtk_widget_get_snapshot (GTK_WIDGET (view), NULL);
        cmap = gdk_colormap_get_system ();
        pixbuf = gdk_pixbuf_get_from_drawable (NULL, GDK_DRAWABLE (pixmap), cmap,
                                               0, 0, 0, 0, WIDTH, WIDTH);

        gdk_pixbuf_save_to_buffer (pixbuf, &buffer, &size, "png", NULL, NULL);

        fwrite (buffer, 1, size, stdout);

        exit (1);
}

int main (int argc, char **argv)
{
        GtkWidget *window;
        GtkWidget *view;

        if (argc != 2)
                exit (20);

        gtk_init (&argc, &argv);

        /* Horribly hacky */
        window = gtk_window_new (GTK_WINDOW_POPUP);
        gtk_window_set_opacity (GTK_WINDOW (window), 0.0);

        view = webkit_web_view_new ();
        webkit_web_view_open (WEBKIT_WEB_VIEW (view), argv[1]);
        gtk_widget_set_size_request (view, WIDTH, WIDTH);
        gtk_container_add (GTK_CONTAINER (window), view);

        gtk_widget_show_all (window);

        g_signal_connect (G_OBJECT (view), "load-finished",
                          G_CALLBACK (on_finished), NULL);

        gtk_main ();
        return 0;
}
