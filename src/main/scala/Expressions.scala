import scala.collection.Iterable

trait Expression {
  def children: List[Expression] = Nil

  def containsVariable(v: Variable): Boolean = false

  def python: String

  def scala: String
}

case class Variable(name: String) extends Expression {
  override def containsVariable(v: Variable): Boolean = name == v.name

  override def python: String = name

  override def scala: String = name
}

trait ExpressionWithProtectedVariables extends Expression {
  def protectedVariables: Iterable[Variable]

  def assertVariableNotProtected(v: Variable): Unit =
    if (protectedVariables.exists(_.name == v.name))
      throw new RuntimeException(s"Reusing variable ${v.name} in $this")
}

case class FunctionCall(functionName: Variable, arguments: List[Expression]) extends Expression {
  override def children: List[Expression] = arguments

  override def containsVariable(v: Variable): Boolean = {
    arguments.exists(_.containsVariable(v))
  }

  override def python: String = makeExpression(_.python)

  override def scala: String = makeExpression(_.scala)

  private def makeExpression(f: Expression => String): String =
    functionName.name + arguments.map(f).mkString("(", ",", ")")
}

case class AnonymousUnaryFunction(variable: Variable, expression: Expression) extends ExpressionWithProtectedVariables {
  assert(expression.containsVariable(variable))

  override def children: List[Expression] = List(expression)

  override def protectedVariables: Iterable[Variable] = List(variable)

  override def containsVariable(v: Variable): Boolean = {
    assertVariableNotProtected(v)
    expression.containsVariable(v)
  }

  override def python: String = s"lambda ${variable.name}: ${expression.python}"

  override def scala: String = s"${variable.name} => ${expression.scala}"
}

case class MapFilterOperation(variable: Variable,
                         elementExpression: Expression,
                         sequenceExpression: Expression,
                         filterExpression: Option[Expression]) extends Expression {
  assert(elementExpression.containsVariable(variable))
  assert(filterExpression.isEmpty || filterExpression.exists(_.containsVariable(variable)))

  override def children: List[Expression] = List(elementExpression, sequenceExpression)

  override def python: String = {
    if (filterExpression.isEmpty && elementExpression == variable) {
      sequenceExpression.python
    } else {
      s"[${elementExpression.python} for ${variable.name} in ${sequenceExpression.python}" + {
        (filterExpression match {
          case Some(expr) => s" if ${expr.python}"
          case None => ""
        }) + "]"
      }
    }
  }

  override def scala: String = {
    var base = sequenceExpression.scala
    val map = if (elementExpression == variable) "" else
      applyAnonymousFunction("map", elementExpression)
    filterExpression match {
      case Some(expr) =>
        base + applyAnonymousFunction("filter", expr) + map
      case None =>
        base + map
    }
  }

  private def applyAnonymousFunction(method: String, expression: Expression): String =
    s".$method(${AnonymousUnaryFunction(variable, expression).scala})"
}