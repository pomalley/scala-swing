package controller.states

import controller.StateManager
import controller.effects.MouseoverEffect
import wh.{Model, Point, Squad}

import scala.concurrent.Promise

/**
 * Base trait for States. Represents the lifecycle of a state and what inputs it can react to.
 * These methods are how the state transforms the UI to accomplish its goal.
 */
trait InputProcessor {
  /**
   * Called when user selects a squad. Currently unimplemented.
   * @param squad the squad the user selects
   */
  def squadSelected(squad: Squad): Unit

  /**
   * Called when user selects a model.
   * @param model The model that a user has selected.
   */
  def modelSelected(model: Model): Unit

  /**
   * Called when user selects a point.
   * @param point the point
   */
  def pointSelected(point: Point): Unit

  /**
   * Called when user mouses over a model.
   * This must return the effect so the controller can remove the effect when the mouseover is done.
   * The state manager actually adds the effect, do not do so in this function.
   * @param model the model
   * @return the effect
   */
  def modelMouseover(model: Model): Option[MouseoverEffect]
  def doneClicked(): Unit
  def undoClicked(): Unit

  /**
   * Called when this state is first pushed onto the stack, or when the state
   * above it is popped off the stack.
   */
  def onActivate(): Unit
}

/**
 * Base class for States. Essentially just the machinery for fulfilling the promise to return a given object.
 * @param manager StateManager to which we belong
 * @tparam Result The type of result we promise.
 */
abstract class State[Result] (val manager: StateManager) extends InputProcessor {
  val promise = Promise[Result]()

  def complete(result: Result): Unit = {
    promise.success(result)
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