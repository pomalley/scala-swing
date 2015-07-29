package wh

import scala.math.pow


object Rules {
  // must set these before checking rules
  var armyA: Army = _
  var armyB: Army = _

  val minEnemyMoveDistance = 3.0

  def otherArmy(army: Army): Army = if (army == armyA) armyB else armyA

  /**
   * Is the proposed move for this model valid?
   * This is a bit backwards because we return None for success.
   * @param mover model trying to move
   * @param point proposed location
   * @param original original point--if None, use model's current loc instead
   * @param moveBonus additive bonus to move, e.g. from a run
   * @return If valid, None; if invalid, String explaining reason for failure
   */
  def validMove(mover: Model, point: Point, original: Option[Point] = None, moveBonus: Int = 0): Option[String] = {
    val distSq = point.distanceSquared(original.getOrElse(mover.loc))
    if (distSq > pow(mover.modelType.move + moveBonus, 2))
      return Some("Cannot move this far.")
    if (mover.squad.army.models.filterNot(mover.squad.contains).filter(_.overlaps(mover, Some(point))).nonEmpty)
      return Some("Cannot overlap friendly model.")
    if (otherArmy(mover.squad.army).models.filter(_.modelDistance(mover, Some(point)) < minEnemyMoveDistance).nonEmpty)
      return Some(s"Cannot be within $minEnemyMoveDistance of an enemy.")
    None
  }
}
