using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.Geometry
{
    public class Array2<T>
    {
        public int Width
        {
            get { return width; }
            set { width = value; }
        }

        public int Height
        {
            get { return height; }
            set { height = value; }
        }

        public Array2 (int width, int height)
        {
            this.width = width;
            this.height = height;

            data = new T[width * height];
        }

        public T GetValue (int x, int y)
        {
            return data[x + y * width];
        }

        public void SetValue (T value, int x, int y)
        {
            data[x + y * width] = value;
        }

        public void Initialize (T initialValue)
        {
            for (int i = 0; i < data.Length; i++)
                data[i] = initialValue;
        }

        private T[] data;
        private int width;
        private int height;
    }
}
