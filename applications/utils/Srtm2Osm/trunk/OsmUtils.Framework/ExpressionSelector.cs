using System;
using System.Collections.Generic;
using System.Text;
using Ciloci.Flee;

namespace OsmUtils.Framework
{
    public class ExpressionSelector : IOsmElementSelector
    {
        public ExpressionSelector (string expression)
        {
            // set the invariant culture for the expression parsing
            ExpressionFactory.SetParseCulture (System.Globalization.CultureInfo.InvariantCulture);

            contextWay = new ExpressionContext ();
            contextWay.Imports.ImportStaticMembers (typeof (ExpressionFunctions));
            contextWay.Variables.DefineVariable ("e", typeof (OsmWay));

            // Create a generic expression that evaluates to a boolean
            try
            {
                expressionObjWay = ExpressionFactory.CreateGeneric<bool> (expression, contextWay);
            }
            catch (ExpressionCompileException ex)
            {
                throw new OsmElementSelectorException (String.Format (System.Globalization.CultureInfo.InvariantCulture,
                    "Error parsing the expression '{0}'", expression), ex);
            }

            contextNode = new ExpressionContext ();
            contextNode.Imports.ImportStaticMembers (typeof (ExpressionFunctions));
            contextNode.Variables.DefineVariable ("e", typeof (OsmNode));

            // Create a generic expression that evaluates to a boolean
            try
            {
                expressionObjNode = ExpressionFactory.CreateGeneric<bool> (expression, contextNode);
            }
            catch (ExpressionCompileException ex)
            {
                throw new OsmElementSelectorException (String.Format (System.Globalization.CultureInfo.InvariantCulture,
                    "Error parsing the expression '{0}'", expression), ex);
            }
        }

        #region IOsmElementSelector Members

        public bool IsMatch (OsmObjectBase element)
        {
            try
            {
                if (element is OsmWay)
                {
                    contextWay.Variables.SetVariableValue ("e", element);
                    return expressionObjWay.Evaluate ();
                }
                else if (element is OsmNode)
                {
                    contextNode.Variables.SetVariableValue ("e", element);
                    return expressionObjNode.Evaluate ();
                }
                else
                    throw new NotSupportedException (String.Format (
                        System.Globalization.CultureInfo.InvariantCulture,
                        "Osm object type '{0}' not supported.", element.GetType ().FullName));
            }
            catch (KeyNotFoundException)
            {
                // this probably means that an element does not have a required tag, 
                // so the expression does not match
                return false;
            }
        }

        #endregion

        private ExpressionContext contextWay, contextNode;
        private IGenericExpression<bool> expressionObjWay, expressionObjNode;
    }
}
