
public class MyMaths
{
    public static double pow(double n, int p)
    {
        double result=1;
        for(int count=0; count<p; count++)
            result *= n;
        return result;
    }


    // From Wikipedia logarithm article - accurate to around 14 decimal places
    // within the ranges we're interested in
    public static double log(double n)
    {
        double v=0, n2=(n-1)/(n+1);
        double factor = n2, factorMultiplier=n2*n2;

        // Series calculation
        
        for(int count=1; count<1000; count+=2)
        {
            v+= (1/(double)count) * factor;
            factor *= factorMultiplier;
        }
        return v*2;
    }
}

        
