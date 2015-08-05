package ss_test

import scala.language.reflectiveCalls

import controller.StateManager
import controller.effects.MouseoverEffect
import controller.states.MovePhase
import wh.{Army, Library, Rules, Squad}

import scala.swing.BorderPanel.Position._
import scala.swing.ListView.AbstractRenderer
import scala.swing.{Swing, _}
import scala.swing.event._

object Main extends SimpleSwingApplication {

  val army: Army = Library.defaultArmy
  val statusBar = new TextField {
    columns = 10
  }
  var squadList: ListView[Squad] = _
  val undoButton = new Button {
    text = "Undo"
    xLayoutAlignment = 0.5
    maximumSize = new Dimension(Int.MaxValue, 20)
  }
  val doneButton = new Button {
    text = "Done"
    xLayoutAlignment = 0.5
    maximumSize = new Dimension(Int.MaxValue, 20)
  }
  val sideLayout = new BoxPanel(Orientation.Vertical) {
    border = Swing.EmptyBorder(5, 5, 5, 5)
    contents += Swing.VGlue
    contents += undoButton
    contents += Swing.VStrut(5)
    contents += doneButton
    var next: Int = 0
  }
  def addSideItem(component: Component): Unit = {
    component.xLayoutAlignment = 0.5
    component.maximumSize = new Dimension(Int.MaxValue, 20)
    sideLayout.contents.insert(sideLayout.next, Swing.VStrut(5), component)
    sideLayout.contents.insert(sideLayout.next, component)
    sideLayout.revalidate()
    sideLayout.next += 2
  }
  def removeSideItem(component: Component) = {
    val i = sideLayout.contents.indexOf(component)
    sideLayout.contents.remove(i-1, 2)
    sideLayout.next -= 2
  }

  val ugly = this

  Rules.armyA = army
  Rules.armyB = new Army("Other Army")

  def top = new MainFrame { // top is a required method
    title = "A Sample Scala Swing GUI"

    val canvas = new Canvas(ugly) {
      preferredSize = new Dimension(100, 100)
    }

    val topLabel = new Label {
      text = "This is the thing."
      font = new Font("Ariel", java.awt.Font.ITALIC, 24)
    }

    val stateManager = new StateManager(canvas, ugly)
    stateManager.pushState(new MovePhase(stateManager, army))

    squadList = new ListView[Squad](army.squads) {
      val label = new Label()
      renderer = new AbstractRenderer[Squad, Label](label) {
        override def configure(list: ListView[_], isSelected: Boolean, focused: Boolean, a: Squad, index: Int): Unit = {
          label.text = a.toString
          label.preferredSize = new Dimension(100, 20)
          label.border = Swing.BeveledBorder(Swing.Raised, new Color(204, 153, 0), new Color(102, 51, 0))
          stateManager.configureSquadList(label, list, isSelected, focused, a, index)
        }
      }
    }

    ugly.addSideItem(squadList)
    ugly.addSideItem(Swing.VStrut(5))

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


