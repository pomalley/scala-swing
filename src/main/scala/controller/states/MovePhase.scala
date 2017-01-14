package controller.states

import java.awt.{Color, Font}

import controller.StateManager
import controller.effects.SquadMouseover
import wh._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.swing.{ListView, Label}
import scala.util.{Failure, Success}

/**
 * This state wraps the whole move phase.
 */
class MovePhase(override val manager: StateManager, val army: Army) extends State[MoveResults](manager) {
  val originalPositions: Map[Model, Point] = army.models.map(m => m -> m.loc).toMap
  val finalPositions: mutable.Map[Model, Point] = mutable.Map()
  var movedSquads: Set[Squad] = Set()

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
        movedSquads += squad
        manager.refreshUI()
        checkCompleteness()
      case Failure(_) =>
        squad.models.foreach(m => m.loc = originalPositions(m))
        movedSquads -= squad
        manager.refreshUI()
        checkCompleteness()
    }
  }


  /**
   * Give the current state an opportunity to customize how the squad list is displayed by modifying the label parameter.
   * @param label the label to modify
   * @param list the complete list
   * @param isSelected whether it's selected
   * @param focused whether it's focused
   * @param squad the squad
   * @param index index in the list
   */
  override def configureSquadList(label: Label,
                                  list: ListView[_],
                                  isSelected: Boolean,
                                  focused: Boolean,
                                  squad: Squad,
                                  index: Int): Unit = {
    if (!squad.models.iterator.map(Rules.validPosition).forall(x => x)) {
      label.foreground = Color.RED
    } else {
      label.foreground = Color.BLACK
    }
    if (movedSquads.contains(squad)) {
      label.font = label.font.deriveFont(Font.PLAIN)
    } else {
      label.font = label.font.deriveFont(Font.BOLD)
    }
  }

  /**
   * Called when this state is first pushed onto the stack, or when the state
   * above it is popped off the stack.
   */
  override def onActivate(): Unit = {
    manager.statusText = s"Move phase for army ${army.name}"
    checkCompleteness()
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
  override def mouseMove(point: Point, model: Option[Model]): Unit = {
    model.map(new SquadMouseover(_)) match {
      case Some(effect) => manager.addEffect(effect)
      case None => manager.removeMouseover()
    }
  }

  override def doneClicked(): Unit = {}

  def checkCompleteness(): Unit = {
    if (army.models.forall(Rules.validPosition)) {
      manager.updateDoneButton(enabled = true, "End move phase")
    } else {
      manager.updateDoneButton(enabled = false, "Not all models are in valid positions")
    }
  }
}

class MoveResults(val originalPositions: Map[Model, Point], val finalPositions: Map[Model, Point])
