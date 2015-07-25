package ss_test

import java.awt.{Color, Graphics2D}

import wh.{Model, Library, Army, Point => BPoint}

import scala.swing.BorderPanel.Position._
import scala.swing._
import scala.swing.event._

object Main extends SimpleSwingApplication {

  val army: Army = Library.defaultArmy

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
//      foreground = Color.blue
//      background = Color.red
      borderPainted = true
      enabled = true
      tooltip = "Click to throw a dart"
    }
    val statusBar = new TextField {
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

    // specify which Components produce events of interest
    listenTo(button)
    listenTo(canvas.mouse.clicks)
    listenTo(canvas.mouse.moves)

    // react to events
    reactions += {
      case ButtonClicked(component) if component == button =>
      case MouseClicked(_, point, _, _, _) =>
      case MouseMoved(source, point, modifiers) =>
        statusBar.text = canvas.modelUnder(point).map(_.modelType.name).getOrElse(canvas.pixelsToBoard(point).toString)
    }
  }
}


class Canvas(val main: Main.type) extends Panel {

  val ppi: Double = 40  // pixels per inch
  val bColor = new Color(20, 100, 20)

  override def paintComponent(g: Graphics2D) {

    // Start by erasing this Canvas
    g.setBackground(bColor)
    g.clearRect(0, 0, size.width, size.height)

    main.army.models.foreach { m =>
      val radius: Int = (m.modelType.size * ppi / 2.0).toInt
      g.setColor(m.modelType.color)
      g.fillOval((m.loc.x * ppi).toInt - radius, (m.loc.y * ppi).toInt - radius, radius*2, radius*2)
    }
  }

  def pixelsToBoard(point: Point): BPoint = new BPoint(point.x.toDouble / ppi, point.y.toDouble / ppi)

  def modelUnder(point: Point): Option[Model] = {
    val bp = pixelsToBoard(point)
    main.army.models.find(_.within(bp))
  }
}