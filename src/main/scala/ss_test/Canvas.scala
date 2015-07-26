package ss_test

import controller.Effect

import scala.swing.{Point, Graphics2D, Color, Panel}

import wh.{Point => BPoint, Model}
/**
 * Canvas for representing the board.
 */
class Canvas(val main: Main.type) extends Panel {

  val ppi: Double = 40  // pixels per inch
  val bColor = new Color(20, 100, 20)
  var effects: List[Effect] = List()

  override def paintComponent(g: Graphics2D) {

    // Start by erasing this Canvas
    g.setBackground(bColor)
    g.clearRect(0, 0, size.width, size.height)

    main.army.models.foreach { m =>
      val radius: Int = (m.modelType.size * ppi / 2.0).toInt
      g.setColor(m.modelType.color)
      g.fillOval((m.loc.x * ppi).toInt - radius, (m.loc.y * ppi).toInt - radius, radius*2, radius*2)
    }

    effects.foreach { effect =>
      effect.paint(g, this)
    }
  }

  def pixelsToBoard(point: Point): BPoint = new BPoint(point.x.toDouble / ppi, point.y.toDouble / ppi)

  def modelUnder(point: Point): Option[Model] = {
    val bp = pixelsToBoard(point)
    main.army.models.find(_.within(bp))
  }

  def pointOrModel(point: Point): Either[BPoint, Model] = {
    modelUnder(point).toRight(pixelsToBoard(point))
  }
}
