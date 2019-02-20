import scala.collection.immutable.HashSet
import scala.util.Random

trait Scope {
  def generateVariable(random: Random): Variable

  def generateFunctionVariable(random: Random): Variable

  def variables: HashSet[Variable]

  def functionVariables: HashSet[Variable]
}

class BaseScope() extends Scope {
  private var _variables: HashSet[Variable] = HashSet.empty
  private var _functionVariables: HashSet[Variable] = HashSet.empty

  private def generate(random: Random): Variable = {
    val existing: HashSet[Variable] = _variables ++ _functionVariables
    if (existing.size == 26)
      throw new RuntimeException("Using too many variables")

    var v: Variable = null
    do {
      v = Variable((random.nextInt(26) + 'a'.toInt).toChar.toString)
    } while (existing(v))
    v
  }

  override def generateVariable(random: Random): Variable = {
    var v = generate(random)
    _variables = _variables + v
    v
  }

  override def generateFunctionVariable(random: Random): Variable = {
    var v = generate(random)
    _functionVariables = _functionVariables + v
    v
  }

  override def variables: HashSet[Variable] = _variables

  override def functionVariables: HashSet[Variable] = _functionVariables
}

class ChildScope(var parent: Scope) extends BaseScope() {
  override def variables: HashSet[Variable] = super.variables ++ parent.variables

  override def functionVariables: HashSet[Variable] = super.variables ++ parent.functionVariables
}


class TopScope extends BaseScope {
  def childScope: ChildScope = new ChildScope(this)
}

class ExpressionGenerator(private val random: Random,
                          private val topScope: TopScope,
                          private val scopes: List[Scope],
                          private val variablesToUse: List[Variable] = List(),
                          private val parent: Option[ExpressionGenerator] = None) {

  def pushScope(variables: List[Variable] = List()): ExpressionGenerator = {
    val childScope = topScope.childScope
    new ExpressionGenerator(random, topScope, childScope :: scopes, variables, Some(this))
  }

  def useRandomVariable(): (Variable, ExpressionGenerator) = {
    assert(variablesToUse.nonEmpty)
    val shuffledVariables = random.shuffle(variablesToUse)
    (shuffledVariables.head,
      new ExpressionGenerator(
        random,
        topScope,
        scopes,
        shuffledVariables.tail,
        parent))
  }

  def scope: Scope = scopes.head

  def generateExpressionUsingVariable(variable: Variable): Expression = {
    random.nextFloat() match {
      case f: Float if f < 0.5 => variable
      case f: Float if f < 0.8 => generateFunctionCall(List(variable))
      case f: Float if f <= 1.0 => generateFunctionCall(List(variable, generateExpression()))
    }
  }

  def generateFunctionCall(arguments: List[Expression]): FunctionCall =
    FunctionCall(scope.generateFunctionVariable(random), random.shuffle(arguments))

  def generateBinaryExpression(a: Expression, b: Expression): Expression =
    generateFunctionCall(List(a, b))

  def generateExpressionUsingVariables(): Expression = {
    if (variablesToUse.nonEmpty) {
      if (random.nextFloat() < 0.8) {
        useRandomVariable() match {
          case (variable: Variable, generator: ExpressionGenerator) =>
            generator.generateExpressionUsingVariable(variable)
        }
      } else {
        generateBinaryExpression(
          generateExpression(),
          generateExpressionUsingVariables()
        )
      }
    } else {
      generateExpression()
    }
  }

  def randomVariable(): Variable = {
    val a = scope.variables.toArray
    a(random.nextInt(a.length))
  }

  def generateExpression(): Expression = {
    if (variablesToUse.isEmpty) {
      pushScope(List(scope.generateVariable(random))).generateExpressionUsingVariables()
    } else {
      random.nextFloat() match {
        case f: Float if f < 0.2 => randomVariable()
        case f: Float if f < 0.8 => generateFunctionCall(List(randomVariable()))
        case f: Float if f <= 1.0 => generateMapFilterOperation()
      }
    }
  }

  def generateMapFilterOperation(): MapFilterOperation = {
    // Generate sequence expression first for variable management
    val variable = scope.generateVariable(random)

    val sequenceExpression = generateExpressionUsingVariables()
    MapFilterOperation(
      variable,
      pushScope().generateExpressionUsingVariable(variable),
      sequenceExpression,
      if (random.nextFloat() < 0.4)
        Some(pushScope().generateExpressionUsingVariable(variable))
      else
        None
    )
  }

  def generateTopLevelExpression(): Expression = generateMapFilterOperation()
}

object ExpressionGenerator {
  def apply(seed: Int): ExpressionGenerator = {
    var topScope = new TopScope()
    new ExpressionGenerator(new Random(seed), topScope, List(topScope))
  }
}
