package wh

import java.awt.Color

import scala.collection.mutable
import scala.math.pow

/**
 * A single model.
 */
class Model (val modelType: ModelType, var loc: Point) {

  def within(p: Point): Boolean = loc.distanceSquared(p) <= pow(modelType.size/2, 2)

  /**
   * Whether this model's base overlaps the other one's base.
   * @param other the other model
   * @param otherLoc if Some(loc), whether it would overlap if the other were at loc
   * @return whether it overlaps
   */
  def overlaps(other: Model, otherLoc: Option[Point] = None): Boolean = {
    loc.distanceSquared(otherLoc getOrElse other.loc) <= pow((modelType.size + other.modelType.size)/2, 2)
  }

  /**
   * Distance between models' bases.
   * Will need to be rewritten for non-circular bases.
   * @param other other model
   * @param otherLoc if Some(loc), whether it would overlap if the other were at loc
   * @return the distance
   */
  def modelDistance(other: Model, otherLoc: Option[Point] = None): Double = {
    math.sqrt(loc.distanceSquared(otherLoc getOrElse other.loc)) - modelType.size/2.0 - other.modelType.size/2.0
  }

  override def toString = s"${modelType.name} at ${loc.toString}"

  var squad: Squad = null
  def sameSquad(other: Model): Boolean = squad != null && squad.models.contains(other)
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
  def +(that: Point): Point = new Point(x + that.x, y + that.y)
  def -(that: Point): Point = this + -that
  def unary_-(): Point = new Point(-x, -y)

  override def toString: String = {
    f"$x%.1f, $y%.1f"
  }
}

class Army (val name: String) {
  var squads: List[Squad] = List()
  def models: List[Model] = squads.foldLeft(List[Model]())((list, squad) => list ++ squad.models)
}

object Library {
  val types = mutable.HashMap[String, ModelType](
    "A" -> new ModelType("A", 1.0, 5.0, Color.GREEN),
    "B" -> new ModelType("B", 0.8, 6.0, Color.CYAN)
  )

  def defaultArmy: Army = {
    val army = new Army("Arnie's Army")

    val squadA = new Squad(types("A"), "A Squad", army)
    val squadB = new Squad(types("B"), "1st B", army)
    squadA.add(new Model(types("A"), new Point(4, 4)))
    squadA.add(new Model(types("A"), new Point(4, 7)))
    squadA.add(new Model(types("A"), new Point(6, 4)))
    squadA.add(new Model(types("A"), new Point(7, 5)))

    squadB.add(new Model(types("B"), new Point(10, 10)))
    squadB.add(new Model(types("B"), new Point(10, 12)))

    army
  }
}