package ss_test

import java.awt.Color

import controller.StateManager
import controller.effects.MouseoverEffect
import controller.states.MoveSquad
import wh.{Army, Library, Rules, Squad}

import scala.swing.BorderPanel.Position._
import scala.swing.GridBagPanel.{Anchor, Fill}
import scala.swing.ListView.AbstractRenderer
import scala.swing.event._
import scala.swing.{Action, Swing, _}

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
    }
    val undoButton = new Button {
      text = "Undo"
      borderPainted = true
      enabled = true
    }
    val squadList = new ListView[Squad](army.squads) {
      val label = new Label()
      renderer = new AbstractRenderer[Squad, Label](label) {
        override def configure(list: ListView[_], isSelected: Boolean, focused: Boolean, a: Squad, index: Int): Unit = {
          label.text = a.toString
          label.foreground = Color.red
        }
      }
    }

    val sideLayout = new GridBagPanel {
      border = Swing.EmptyBorder(5, 5, 5, 5)
      val c = new Constraints
      c.fill = Fill.Horizontal
      c.gridx = 0
      c.gridy = 0
      c.weighty = 0
      c.anchor = Anchor.South
      c.insets = new Insets(0, 0, 5, 5)
      layout(squadList) = c
      c.weighty = 1
      c.gridy += 1
      layout(doneButton) = c
      c.weighty = 0
      c.gridy += 1
      layout(undoButton) = c
//      contents ++= Seq(squadList, Swing.HStrut(10), doneButton, undoButton)
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


