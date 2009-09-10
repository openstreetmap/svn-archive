package it.sephiroth.expr.ast
{
	import hxasm.OpCode;
	import hxasm.Operation;
	
	import it.sephiroth.expr.SWFContext;
	
	public class DivExpression implements IExpression
	{
		private var _left: IExpression;
		private var _right: IExpression;
		
		public function DivExpression( left: IExpression, right: IExpression )
		{
			_left = left;
			_right = right;
		}
		
		public function evaluate(): Number
		{
			return _left.evaluate() / _right.evaluate();
		}
		
		public function toString(): String
		{
			return _left + " " + _right + " / ";
		}
		
		public function compile( c: SWFContext ): void
		{
			_left.compile( c );
			_right.compile( c );
			
			c.ctx.op( OpCode.OOp( Operation.OpDiv ) );
			
			c.subStack( 1 );
		}
	}
}