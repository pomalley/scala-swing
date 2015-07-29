package wh

import scala.math.pow


object Rules {
  // must set these before checking rules
  var armyA: Army = _
  var armyB: Army = _

  val minEnemyMoveDistance = 3.0
  val squadCoherenceDistance = 1.0

  def otherArmy(army: Army): Army = if (army == armyA) armyB else armyA

  /**
   * Is the proposed move for this model valid?
   * This is a bit backwards because we return None for success.
   * @param mover model trying to move
   * @param point proposed location
   * @param original original point--if None, use model's current loc instead
   * @param moveBonus additive bonus to move, e.g. from a run
   * @param squadCoherence if True, require to be w/in range of a model in this squad
   * @return If valid, None; if invalid, String explaining reason for failure
   */
  def validMove(mover: Model,
                point: Point,
                original: Option[Point] = None,
                moveBonus: Int = 0,
                squadCoherence: Boolean = false
               ): Option[String] = {
    val distSq = point.distanceSquared(original.getOrElse(mover.loc))
    if (distSq > pow(mover.modelType.move + moveBonus, 2))
      return Some("Cannot move this far.")
    if (mover.squad.army.models.filterNot(mover.squad.contains).exists(_.overlaps(mover, Some(point))))
      return Some("Cannot overlap friendly model.")
    if (otherArmy(mover.squad.army).models.exists(_.modelDistance(mover, Some(point)) < minEnemyMoveDistance))
      return Some(s"Cannot be within $minEnemyMoveDistance of an enemy.")
    if (squadCoherence && mover.squad.models.length > 1 &&
        !mover.squad.models.exists(other => other != mover && other.modelDistance(mover, Some(point)) < squadCoherenceDistance)) {
      return Some(s"Must be within $squadCoherenceDistance of another model in this unit.")
    }
    None
  }
}
