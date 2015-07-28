package controller.states

import controller.StateManager
import controller.effects.{ModelMouseover, MouseoverEffect, ModelSelection}
import wh.{Squad, Model, Point}

import scala.math._
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

  override def onActivate(): Unit = {
    manager.statusText = s"Moving ${squad.name}: choose model for initial move"
  }

  override def squadSelected(squad: Squad) = {}
  override def modelSelected(model: Model) = {
    if (squad contains model) {
      if (!madeMove) {
        val effect = manager.addEffect(new ModelSelection(model))
        manager.pushState(new GetLocationFrom(manager, model)).onComplete {
          case Success(opt) =>
            opt match {
              case Some(point) =>
                val diff = point - model.loc
                squad.models.foreach(_.loc += diff)
                madeMove = true
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
  override def modelMouseover(model: Model): Option[MouseoverEffect] = {
    Some(new ModelMouseover(model))
  }
  override def pointSelected(point: Point) = {}
  override def doneClicked() = {

  }
  override def undoClicked() = {
    println("Undo not implemented yet, sorry.")
  }
}

class GetLocation(override val manager: StateManager) extends State[Option[Point]](manager) {
  override def onActivate(): Unit = {
    manager.statusText = "Choose location"
  }

  override def modelSelected(model: Model): Unit = {
    // TODO: choose a point near this model
  }
  override def squadSelected(squad: Squad): Unit = {}

  override def pointSelected(point: Point) = {
    promise.success(Some(point))
  }

  override def undoClicked(): Unit = {}

  override def doneClicked(): Unit = {}

  override def modelMouseover(model: Model): Option[MouseoverEffect] = {
    Some(new ModelMouseover(model))
  }
}

class GetLocationFrom(override val manager: StateManager, model: Model) extends GetLocation(manager) {
  override def onActivate(): Unit = {
    manager.statusText = s"Move ${model.toString}"
  }
  override def pointSelected(point: Point) = {
    if (model.loc.distanceSquared(point) <= pow(model.modelType.move, 2))
      promise.success(Some(point))
  }
  override def doneClicked(): Unit = {
    promise.success(Some(model.loc))
  }
  override def undoClicked(): Unit = {
    promise.success(None)
  }
}