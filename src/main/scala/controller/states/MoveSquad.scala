package controller.states

import controller.StateManager
import controller.effects.{MovementLine, ModelMouseover, ModelSelection}
import wh.{Model, Point, Rules, Squad}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * This state is used to move a squad.
 * Movement rules:
 *   * can only move once per phase
 *   * can move up to the movement value of the model type.
 *   * can run for d6 extra inches, but then cannot shoot/charge later
 *   * must be within 1" of another model in the unit
 *   * cannot be closer than 3" to an enemy
 *
 * Uses GetLocation state to move an individual model.
 * @param manager The manager
 * @param squad The squad
 */
class MoveSquad(override val manager: StateManager, val squad: Squad) extends State[Action](manager) {
  var madeMove = false
  val origins = squad.models.map(_.loc)
  var lastMouseover: Model = null

  override def onActivate(): Unit = {
    manager.statusText = s"Moving ${squad.name}: choose model for initial move"
  }

  override def squadSelected(squad: Squad) = {}
  override def modelSelected(model: Model) = {
    if (squad contains model) {
      if (!madeMove) {
        val effect = manager.addEffect(new ModelSelection(model))
        manager.pushState(new GetMoveFor(manager, model)).onComplete {
          case Success(opt) =>
            opt match {
              case Some(point) =>
                val moveVector = point - model.loc
                squad.models.foreach(_.loc += moveVector)
                madeMove = true
                manager.statusText = s"Moving ${squad.name}: choose model to tidy"
              case None =>
            }
            manager.removeEffect(effect)
          case Failure(t) =>
            manager.removeEffect(effect)
        }
      } else {
        // this is a tidy-up
        val effect = manager.addEffect(new ModelSelection(model))
        val origin = origins(squad.models.indexOf(model))
        manager.pushState(new GetTidyMoveFor(manager, model, Some(origin))).onComplete {
          case Success(opt) =>
            opt match {
              case Some(point) =>
                model.loc = point
                manager.statusText = s"Moving ${squad.name}: choose model to tidy"
              case None =>
            }
            manager.removeEffect(effect)
          case Failure(t) =>
            manager.removeEffect(effect)
        }
      }
    }
  }
  override def mouseMove(point: Point, model: Option[Model]): Unit = {
    if (model.nonEmpty && model.get != lastMouseover) {
      manager.addEffect(new ModelMouseover(model.get))
      lastMouseover = model.get
    } else if (model.isEmpty && lastMouseover != null) {
      manager.removeMouseover()
      lastMouseover = null
    }
  }
  override def pointSelected(point: Point) = {}
  override def doneClicked() = {

  }
  override def undoClicked() = {
    println("Undo not implemented yet, sorry.")
  }
}

class GetMoveFor(override val manager: StateManager, val model: Model, val origin: Option[Point] = None)
  extends State[Option[Point]](manager) {

  val baseMsg = s"Move this model"

  def moveRule(point: Point): Option[String] = Rules.validMove(model, point, origin)

  override def onActivate(): Unit = {
    manager.statusText = baseMsg
  }
  override def pointSelected(point: Point) = {
    moveRule(point) match {
      case Some(reason) => println(reason)
      case None => complete(Some(point))
    }
  }
  override def doneClicked(): Unit = {
    promise.success(Some(model.loc))
  }
  override def undoClicked(): Unit = {
    promise.success(None)
  }

  override def squadSelected(squad: Squad): Unit = {}

  override def modelSelected(model: Model): Unit = {}

  override def mouseMove(point: Point, mouseoverModel: Option[Model]): Unit = {
    moveRule(point) match {
      case Some(reason) =>
        manager.statusText = baseMsg + s" -- $reason"
        manager.addEffect(new MovementLine(model, point, false, origin))
      case None =>
        manager.statusText = baseMsg
        manager.addEffect(new MovementLine(model, point, true, origin))
    }
  }
}

class GetTidyMoveFor(override val manager: StateManager, override val model: Model, override val origin: Option[Point])
  extends GetMoveFor(manager, model, origin) {

  override def moveRule(point: Point): Option[String] = Rules.validMove(model, point, origin, squadCoherence = true)
}