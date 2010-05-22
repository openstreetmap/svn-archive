/* There is no licence for this, I don't care what you do with it */
#include <stdlib.h>
#include <unistd.h>

#include <gio/gunixoutputstream.h>
#include <webkit/webkit.h>

#define WIDTH 2000

/* compile with:
 * gcc -o webkit-image-gtk webkit-image-gtk.c `pkg-config --cflags --libs webkit-1.0 gio-unix-2.0`
 * Requires GTK+ 2.20 and WebKitGtk+ 1.1.1
 */

static void
on_finished (WebKitWebView      *view,
             WebKitWebFrame     *frame,
             GtkOffscreenWindow *window)
{
	GdkPixbuf *pixbuf;
	GOutputStream *stream;

	pixbuf = gtk_offscreen_window_get_pixbuf (window);

	stream = g_unix_output_stream_new (STDOUT_FILENO, TRUE);
	gdk_pixbuf_save_to_stream (pixbuf, stream, "png", NULL, NULL, NULL);

	exit (1);
}

int
main (int    argc,
      char **argv)
{
	GtkWidget *window;
	GtkWidget *view;

	if (argc != 2)
		exit (20);

	gtk_init (&argc, &argv);

	window = gtk_offscreen_window_new ();

	view = webkit_web_view_new ();
	webkit_web_view_load_uri (WEBKIT_WEB_VIEW (view), argv[1]);
	gtk_widget_set_size_request (view, WIDTH, WIDTH);
	gtk_container_add (GTK_CONTAINER (window), view);

	gtk_widget_show_all (window);

	g_signal_connect (view, "load-finished",
	                  G_CALLBACK (on_finished), window);

	gtk_main ();
	return 0;
}
