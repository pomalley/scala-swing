package wh

import java.awt.Color

import scala.collection.mutable
import scala.math.pow

/**
 * A single model.
 */
class Model (val modelType: ModelType, var loc: Point) {
  def within(p: Point): Boolean = loc.distanceSquared(p) <= pow(modelType.size/2, 2)
  def overlaps(other: Model): Boolean = {
    loc.distanceSquared(other.loc) <= pow((modelType.size + other.modelType.size)/2, 2)
  }
}

class ModelType(val name: String, val size: Double, val move: Double, val color: Color) {
  def makeModel(loc: Point): Model = {
    new Model(this, loc)
  }
}

class Point(var x: Double, var y: Double) {
  def distanceSquared(other: Point): Double = {
    pow(other.x - x, 2) + pow(other.y - y, 2)
  }
}

class Army {
  var models: List[Model] = List[Model]()
}

object Library {
  val types = mutable.HashMap[String, ModelType](
    "A" -> new ModelType("A", 1.0, 5.0, Color.GREEN),
    "B" -> new ModelType("B", 0.8, 6.0, Color.CYAN)
  )

  def defaultArmy: Army = {
    new Army {
      models = List(
        types("A").makeModel(new Point(1, 1)),
        types("B").makeModel(new Point(3, 5))
      )
    }
  }
}