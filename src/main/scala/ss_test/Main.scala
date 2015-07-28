package ss_test

import controller.StateManager
import controller.effects.MouseoverEffect
import controller.states.MoveSquad
import wh.{Army, Library}

import scala.swing.BorderPanel.Position._
import scala.swing._
import scala.swing.event._

object Main extends SimpleSwingApplication {

  val army: Army = Library.defaultArmy
  var statusBar: TextField = _

  val ugly = this

  def top = new MainFrame { // top is a required method
    title = "A Sample Scala Swing GUI"

    // declare Components here
    val topLabel = new Label {
      text = "I'm a big label!"
      font = new Font("Ariel", java.awt.Font.ITALIC, 24)
    }
    val button = new Button {
      text = "Throw!"
      borderPainted = true
      enabled = true
      tooltip = "Click to throw a dart"
    }
    ugly.statusBar = new TextField {
      columns = 10
      text = "Click on the target!"
    }
    val canvas = new Canvas(ugly) {
      preferredSize = new Dimension(100, 100)
    }

    // choose a top-level Panel and put components in it
    // Components may include other Panels
    contents = new BorderPanel {
      layout(topLabel) = North
      layout(button) = West
      layout(canvas) = Center
      layout(statusBar) = South
    }
    size = new Dimension(800, 600)
    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(Action("Exit") {
          sys.exit(0)
        })
      }
    }

    val stateManager = new StateManager(canvas, ugly)
    stateManager.pushState(new MoveSquad(stateManager, army.squads.head))

    // specify which Components produce events of interest
    listenTo(button)
    listenTo(canvas.mouse.clicks)
    listenTo(canvas.mouse.moves)

    var lastMouseoverEffect: Option[MouseoverEffect] = None

    // react to events
    reactions += {
      case ButtonClicked(component) if component == button =>
      case MouseClicked(_, point, _, _, _) =>
        canvas.pointOrModel(point) match {
          case Left(boardPoint) => stateManager.pointSelected(boardPoint)
          case Right(model) => stateManager.modelSelected(model)
        }
      case MouseMoved(source, point, modifiers) =>
        canvas.modelUnder(point) match {
          case Some(model) =>
            if (lastMouseoverEffect.map(_.source != model).getOrElse(true)) {
              lastMouseoverEffect.foreach(stateManager.removeEffect(_))
              lastMouseoverEffect = stateManager.modelMouseover(model)
              lastMouseoverEffect.foreach(stateManager.addEffect)
              canvas.repaint()
            }
          case None =>
            if (lastMouseoverEffect.nonEmpty) {
              lastMouseoverEffect.foreach(stateManager.removeEffect(_))
              canvas.repaint()
              lastMouseoverEffect = None
            }
        }
    }
  }
}


