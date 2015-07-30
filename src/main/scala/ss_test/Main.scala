package ss_test

import controller.StateManager
import controller.effects.MouseoverEffect
import controller.states.MoveSquad
import wh.{Army, Library, Rules}

import scala.swing.BorderPanel.Position._
import scala.swing._
import scala.swing.event._

object Main extends SimpleSwingApplication {

  val army: Army = Library.defaultArmy
  var statusBar: TextField = _

  val ugly = this

  Rules.armyA = army
  Rules.armyB = new Army()

  def top = new MainFrame { // top is a required method
    title = "A Sample Scala Swing GUI"

    // declare Components here
    val topLabel = new Label {
      text = "This is the thing."
      font = new Font("Ariel", java.awt.Font.ITALIC, 24)
    }
    val doneButton = new Button {
      text = "Done"
      borderPainted = true
      enabled = true
      //tooltip = "Click to throw a dart"
    }
    val undoButton = new Button {
      text = "Undo"
      borderPainted = true
      enabled = true
    }
    val sideLayout = new BoxPanel(Orientation.Vertical) {
      contents ++= Seq(doneButton, undoButton)
    }

    ugly.statusBar = new TextField {
      columns = 10
    }
    val canvas = new Canvas(ugly) {
      preferredSize = new Dimension(100, 100)
    }

    // choose a top-level Panel and put components in it
    // Components may include other Panels
    contents = new BorderPanel {
      layout(topLabel) = North
      layout(sideLayout) = West
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
    listenTo(doneButton)
    listenTo(undoButton)
    listenTo(canvas.mouse.clicks)
    listenTo(canvas.mouse.moves)

    var lastMouseoverEffect: Option[MouseoverEffect] = None

    // react to events
    reactions += {
      case ButtonClicked(component) if component == doneButton => stateManager.doneClicked()
      case ButtonClicked(component) if component == undoButton => stateManager.undoClicked()
      case MouseClicked(_, point, _, _, _) =>
        canvas.pointOrModel(point) match {
          case Left(boardPoint) => stateManager.pointSelected(boardPoint)
          case Right(model) => stateManager.modelSelected(model)
        }
      case MouseMoved(source, point, modifiers) =>
        stateManager.mouseMove(canvas.pixelsToBoard(point), canvas.modelUnder(point))
    }
  }
}


