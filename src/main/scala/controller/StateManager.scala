package controller

import controller.effects.{MouseoverEffect, Effect}
import controller.states.{InputProcessor, State}
import ss_test.{Canvas, Main}
import wh.{Model, Point, Squad}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * For controlling the state of the UI.
  */
class StateManager(val canvas: Canvas, val main: Main.type) {
   var stateStack: List[InputProcessor] = List()
   def currentState = stateStack.head
   var currentMouseover: Option[MouseoverEffect] = None

  /**
   * Push a new state onto the stack.
   * @param state state to push
   * @tparam Result type that this state will return
   * @return future for the result of the state
   */
   def pushState[Result](state: State[Result]): Future[Result] = {
     stateStack ::= state
     state.onActivate()
     state.promise.future.map { res =>
       // when done, pop the stack
       popState(mustMatch = state)
       canvas.repaint()
       res
     }
   }

   /**
    * Pop the head of the stateStack and return it.
    * @param mustMatch if provided, the head must match this state
    * @return the state popped, or null if not popped
    */
   def popState(mustMatch: InputProcessor = null): InputProcessor = {
     if (mustMatch != null && stateStack.head != mustMatch)
       return null
     val poppedState: InputProcessor = stateStack.head
     stateStack = stateStack.tail
     stateStack.head.onActivate()
     poppedState
   }

   def repaint() = canvas.repaint()

   def squadSelected(squad: Squad): Unit = currentState.squadSelected(squad)
   def modelSelected(model: Model): Unit = currentState.modelSelected(model)
   def pointSelected(point: Point): Unit = currentState.pointSelected(point)
   def mouseMove(point: Point, model: Option[Model]): Unit = currentState.mouseMove(point, model)
   def doneClicked(): Unit = currentState.doneClicked()
   def undoClicked(): Unit = currentState.undoClicked()

   def addEffect[T <: Effect](effect: T): T = {
     // only keep one mouseover at a time
     effect match {
       case mouseover: MouseoverEffect =>
         removeMouseover()
         currentMouseover = Some(mouseover)
       case _ =>
     }
     canvas.effects ::= effect
     canvas.repaint()
     effect
   }
   def removeEffect(effect: Effect): Unit = {
     canvas.effects = canvas.effects.filterNot(_ == effect)
     canvas.repaint()
   }
   def removeEffects(effects: Seq[Effect]): Unit = {
     canvas.effects = canvas.effects.filterNot(effects.contains)
     canvas.repaint()
   }
  def removeMouseover(): Unit = {
     removeEffects(currentMouseover.toList)
  }

   def statusText: String = main.statusBar.text
   def statusText_=(s: String) = main.statusBar.text_=(s)

 }
