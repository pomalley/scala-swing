package controller.states

import controller.StateManager
import wh.{Squad, Point, Model, Army}

/**
 * This state wraps the whole move phase.
 */
class MovePhase(override val manager: StateManager, val army: Army) extends State[MoveResults](manager) {
  val originalPositions: Map[Model, Point] = army.models.map(m => m -> m.loc).toMap
  /**
   * Called when user selects a squad. Currently unimplemented.
   * @param squad the squad the user selects
   */
  override def squadSelected(squad: Squad): Unit = ???

  /**
   * Called when user selects a model.
   * @param model The model that a user has selected.
   */
  override def modelSelected(model: Model): Unit = ???

  /**
   * Called when this state is first pushed onto the stack, or when the state
   * above it is popped off the stack.
   */
  override def onActivate(): Unit = ???

  /**
   * Called when user selects a point.
   * @param point the point
   */
  override def pointSelected(point: Point): Unit = ???

  override def undoClicked(): Unit = ???

  /**
   * Called for each mouse move.
   * @param point the location of the mouse
   * @param model the model the mouse is over, if any
   */
  override def mouseMove(point: Point, model: Option[Model]): Unit = ???

  override def doneClicked(): Unit = ???
}

class MoveResults(val originalPositions: Map[Model, Point], val finalPositions: Map[Model, Point])
