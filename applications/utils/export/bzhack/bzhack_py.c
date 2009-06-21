#include <Python.h>
#include <bzlib.h>

static PyObject * bzhack_decompress(BZ2DecompObject *self, PyObject *args) {
    bz_stream bzs;
    BZ2_bzDecompressInit(bzs, 0, 0);
    
    return 0;
}

static PyMethodDef BZHackMethods[] = {
    { "init", bzhack_init, METH_VARARGS, "init" },
    { NULL, NULL, 0, NULL }
};

PyMODINIT_FUNC
initbzhack(void) {
	(void)Py_InitModule("bzhack", BZHackMethods);
}

