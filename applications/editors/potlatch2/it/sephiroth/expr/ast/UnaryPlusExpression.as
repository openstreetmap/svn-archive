package it.sephiroth.expr.ast
{
	import it.sephiroth.expr.SWFContext;
	
	public class UnaryPlusExpression implements IExpression
	{
		private var _value: IExpression;
		
		public function UnaryPlusExpression( value: IExpression )
		{
			_value = value;
		}
		
		public function evaluate(): Number
		{
			return _value.evaluate();
		}
		
		public function toString(): String
		{
			return _value + " + ";
		}
		
		public function compile( c: SWFContext ): void
		{
			return _value.compile( c );
		}

	}
}