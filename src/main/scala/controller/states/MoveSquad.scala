package controller.states

import controller.StateManager
import controller.effects.ModelSelection
import wh.{Squad, Model, Point}

import scala.math._
import scala.concurrent.ExecutionContext.Implicits.global

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

  override def onActivate(): Unit = {
    manager.statusText = s"Moving ${squad.name}: choose model for initial move"
  }

  override def squadSelected(squad: Squad) = {}
  override def modelSelected(model: Model) = {
    if (squad contains model) {
      if (!madeMove) {
        val effect = manager.addEffect(new ModelSelection(model))
        manager.pushState(new GetLocationFrom(manager, model)).onSuccess {
          case point =>
            val diff = point - model.loc
            squad.models.foreach { m =>
              m.loc += diff
            }
            manager.removeEffect(effect)
            madeMove = true
            manager.statusText = s"Moving ${squad.name}: choose model to tidy"
        }
      }
    }
  }
  override def pointSelected(point: Point) = {}
  override def doneClicked() = {

  }
  override def undoClicked() = {
    println("Undo not implemented yet, sorry.")
  }
}

class GetLocation(override val manager: StateManager) extends State[Point](manager) {
  override def onActivate(): Unit = {
    manager.statusText = "Choose location"
  }

  override def modelSelected(model: Model): Unit = {
    // TODO: choose a point near this model
  }
  override def squadSelected(squad: Squad): Unit = {}

  override def pointSelected(point: Point) = {
    promise.success(point)
  }

  override def undoClicked(): Unit = {}

  override def doneClicked(): Unit = {}

}

class GetLocationFrom(override val manager: StateManager, model: Model) extends GetLocation(manager) {
  override def onActivate(): Unit = {
    manager.statusText = s"Move ${model.toString}"
  }
  override def pointSelected(point: Point) = {
    if (model.loc.distanceSquared(point) <= pow(model.modelType.move, 2))
      promise.success(point)
  }
}