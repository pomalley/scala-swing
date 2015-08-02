package controller.states

import controller.StateManager
import wh.{Squad, Point, Model, Army}

import scala.collection.mutable
import scala.util.{Failure, Success}

/**
 * This state wraps the whole move phase.
 */
class MovePhase(override val manager: StateManager, val army: Army) extends State[MoveResults](manager) {
  val originalPositions: Map[Model, Point] = army.models.map(m => m -> m.loc).toMap
  val finalPositions: mutable.Map[Model, Point] = mutable.Map()
  /**
   * Called when user selects a squad. Currently unimplemented.
   * @param squad the squad the user selects
   */
  override def squadSelected(squad: Squad): Unit = {}

  /**
   * Called when user selects a model.
   * @param model The model that a user has selected.
   */
  override def modelSelected(model: Model): Unit = {
    val squad = model.squad
    val origins = squad.models.map(originalPositions(_))
    manager.pushState(new MoveSquad(manager, squad, origins)).onComplete {
      case Success(locs) =>
        locs.zip(squad.models).foreach{ case (p: Point, m: Model) => finalPositions(m) = p }
      case Failure(t) =>
    }
  }

  /**
   * Called when this state is first pushed onto the stack, or when the state
   * above it is popped off the stack.
   */
  override def onActivate(): Unit = {
    manager.statusText = s"Move phase for army ${army.name}"
  }

  /**
   * Called when user selects a point.
   * @param point the point
   */
  override def pointSelected(point: Point): Unit = {}

  override def undoClicked(): Unit = {}

  /**
   * Called for each mouse move.
   * @param point the location of the mouse
   * @param model the model the mouse is over, if any
   */
  override def mouseMove(point: Point, model: Option[Model]): Unit = {}

  override def doneClicked(): Unit = {}
}

class MoveResults(val originalPositions: Map[Model, Point], val finalPositions: Map[Model, Point])
