package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

public class Combination {
	
	private long n = 0;
	private long k = 0;
	long[] data = null;
	
	public Combination(long n, long k){
	    if (n < 0 || k < 0)
			try {
				throw new Exception("Negative parameter in constructor");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
	    this.n = n;
	    this.k = k;
	    this.data = new long[(int)k];
	    for (long i = 0; i < k; ++i)
	      this.data[(int)i] = i;
	  }
	
	public static long Choose(long n, long k)
	{
	  if (n < 0 || k < 0)
		try {
			throw new Exception("Invalid negative parameter in Choose()");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	  if (n < k) return 0;
	  if (n == k) return 1;

	  long delta, iMax;

	  if (k < n-k) // ex: Choose(100,3)
	  {
	    delta = n-k;
	    iMax = k;
	  }
	  else         // ex: Choose(100,97)
	  {
	    delta = k;
	    iMax = n-k;
	  }

	  long ans = delta + 1;

	  for (long i = 2; i <= iMax; ++i)
	  {
	    ans = (ans * (delta + i)) / i; 
	  }

	  return ans;
	}
	public long Choose()
	{
	 
	  if (n < k) return 0;
	  if (n == k) return 1;

	  long delta, iMax;

	  if (k < n-k) // ex: Choose(100,3)
	  {
	    delta = n-k;
	    iMax = k;
	  }
	  else         // ex: Choose(100,97)
	  {
	    delta = k;
	    iMax = n-k;
	  }

	  long ans = delta + 1;

	  for (long i = 2; i <= iMax; ++i)
	  {
	    ans = (ans * (delta + i)) / i; 
	  }

	  return ans;
	}

	
	public Combination Successor()
	{
	  if (this.data.length == 0 ||
	      this.data[0] == this.n - this.k)
	    return null;

	  Combination ans = new Combination(this.n, this.k);

	  long i;
	  for (i = 0; i < this.k; ++i){
	    ans.data[(int)i] = this.data[(int)i];
	  }
	  for (i = this.k - 1; i > 0 && ans.data[(int)i] == this.n - this.k + i; --i) {};
	 
	  ++ans.data[(int)i];

	  for (long j = i; j < this.k - 1; ++j){
	    ans.data[(int)j+1] = ans.data[(int)j] + 1;
	  }  
	  return ans;
	}
	
	public String ToString()
	  {
	    StringBuilder sb = new StringBuilder();
	    sb.append("{");
	    for (long i = 0; i < this.k; ++i){
	      sb.append(this.data[(int)i]);
	      if (i<this.k-1) sb.append(", ");
	    }
	    sb.append("}");
	    return sb.toString();
	  }

}
