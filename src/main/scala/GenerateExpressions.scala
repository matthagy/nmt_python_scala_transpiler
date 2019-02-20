import scala.util.Random

object GenerateExpressions {

  def main(_args: Array[String]): Unit = {
    var args = _args
    if (args.length == 2) {
      args = args.drop(1)
    }

    if (args.length != 1) {
      println("Usage: NUMBER_OF_ENTRIES")
      System.exit(1)
    }

    val r = new Random(0xCAFE)
    Range(0, Integer.parseInt(args(0))).foreach(x => {
      val expression = ExpressionGenerator(r.nextInt()).generateTopLevelExpression()
      println(expression.python + "|" + expression.scala)
    })
  }
}
