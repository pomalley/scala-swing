package wh

import scala.math.pow


object Rules {
  // must set these before checking rules
  var armyA: Army = _
  var armyB: Army = _

  /**
   * Is the proposed move for this model valid?
   * This is a bit backwards because we return None for success.
   * @param model model trying to move
   * @param point proposed location
   * @param original original point--if None, use model's current loc instead
   * @return If valid, None; if invalid, String explaining reason for failure
   */
  def validMove(model: Model, point: Point, original: Option[Point] = None): Option[String] = {
    val distSq = point.distanceSquared(original.getOrElse(model.loc))
    if (distSq > pow(model.modelType.move, 2))
      return Some("Cannot move this far.")

    None
  }
}
