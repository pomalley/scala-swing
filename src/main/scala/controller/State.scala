package controller

import ss_test.Canvas
import wh.{Model, Point, Squad}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.math.pow

/**
 * For controlling the state of the UI.
 */
class StateManager(val canvas: Canvas) {
  var stateStack: List[InputProcessor] = List()
  def currentState = stateStack.head

  def pushState[Result](state: State[Result]): Future[Result] = {
    stateStack ::= state
    state.promise.future.map { res =>
      if (stateStack.head == state) {
        stateStack = stateStack.tail
      }
      canvas.repaint()
      res
    }
  }

  def repaint() = canvas.repaint()

  def squadSelected(squad: Squad): Unit = currentState.squadSelected(squad)
  def modelSelected(model: Model): Unit = currentState.modelSelected(model)
  def pointSelected(point: Point): Unit = currentState.pointSelected(point)
  def doneClicked(): Unit = currentState.doneClicked()
  def undoClicked(): Unit = currentState.undoClicked()

  def addEffect[T <: Effect](effect: T): T = {
    canvas.effects ::= effect
    effect
  }
  def removeEffect(effect: Effect): Unit = {
    canvas.effects = canvas.effects.filterNot(_ == effect)
  }
}

trait InputProcessor {
  def displayName: String
  def squadSelected(squad: Squad): Unit
  def modelSelected(model: Model): Unit
  def pointSelected(point: Point): Unit
  def doneClicked(): Unit
  def undoClicked(): Unit
}

abstract class State[Result] (val manager: StateManager) extends InputProcessor {
  val promise = Promise[Result]()

  def complete(result: Result): Unit = {
    promise.success(result)
  }
}

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

  override def displayName = s"Move ${squad.name}"
  override def squadSelected(squad: Squad) = {}
  override def modelSelected(model: Model) = {
    if (squad contains model) {
      if (!madeMove) {
        val effect = manager.addEffect(new ModelSelection(model))
        manager.repaint()
        manager.pushState(new GetLocationFrom(manager, model)).onSuccess {
          case point =>
            val diff = point - model.loc
            squad.models.foreach { m =>
              m.loc += diff
            }
            manager.removeEffect(effect)
            manager.repaint()
            madeMove = true
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
  override def displayName: String = "Choose location"

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
  override def pointSelected(point: Point) = {
    if (model.loc.distanceSquared(point) <= pow(model.modelType.move, 2))
      promise.success(point)
  }
}

class Action(val description: String, val subActions: List[Action] = List())

/*
Example states:
Move phase, army A
  while (has squads left to move):
    select next squad               -- this goes onto stack ("move squad")
    optional: declare run           -- these next are queue
    select model to move
    select location
    select model to tidy or done    -- this goes onto stack ("tidy model")
      if tidy: select location      -- after complete, popped from stack
                                    -- now pop this "move squad" from stack

e.g. tidy model
after first model has moved (and squad automatically positioned), we enter the state "tidy or complete". when the user
clicks the done button, we pop ourselves, returning a "squad move" action to our parent phase (which is probably
something like a "move army" phase). when the user selects a model (in this unit), we instead push a "select location"
phase onto the stack. when the user then selects a location (or cancels) this gets popped from the stack and the
location is returned to the "squad move" phase.

squadMove.modelSelected(model) = {
  // ... check for model validity, etc
  // push this guy to the stack and handle the result when done
  futureLoc = Manager.push(new GetLocation(model, original_location, whatever))
  futureLoc onSuccess { case loc: move model, etc. }
}

GetLocation.popSelf[result](location) = {
  Manager.
}

Action -- recorded for undoing, replays, etc
Examples:
  move model from loc -> loc
  move unit (many move models)
  attack unit with unit

*/