package controller.effects

import java.awt.Graphics2D

import ss_test.Canvas
import wh.{Point, Model}

import scala.swing.Color


sealed abstract class EffectClass
case object Selection extends EffectClass
case object Mouseover extends EffectClass
case object Information extends EffectClass

/**
 * Used to add graphical effects to objects. e.g. selection circles, etc.
 */
trait Effect {
  val effectClass: EffectClass
  def paint(g: Graphics2D, canvas: Canvas): Unit
}

trait MouseoverEffect extends Effect {
  val source: Any
  override val effectClass = Mouseover
}

abstract class ModelEffect(val model: Model) extends Effect

class ModelSelection (override val model: Model) extends ModelEffect(model) {
  override val effectClass = Selection

  override def paint(g: Graphics2D, canvas: Canvas): Unit = {
    g.setColor(new Color(255, 200, 200))
    val radius: Int = (model.modelType.size * canvas.ppi / 2.0 * 1.1).toInt
    g.drawOval((model.loc.x * canvas.ppi).toInt - radius, (model.loc.y * canvas.ppi).toInt - radius, radius*2, radius*2)
  }
}

class ModelInvalidPosition (override val model: Model) extends ModelEffect(model) {
  override val effectClass = Information

  override def paint(g: Graphics2D, canvas: Canvas): Unit = {
    g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f))
    val radius: Int = (model.modelType.size * canvas.ppi / 2.0 * 1.1).toInt
    g.fillOval((model.loc.x * canvas.ppi).toInt - radius, (model.loc.y * canvas.ppi).toInt - radius, radius*2, radius*2)
  }
}

class ModelMouseover (override val model: Model) extends ModelEffect(model) with MouseoverEffect {
  override val source: Model = model

  override def paint(g: Graphics2D, canvas: Canvas): Unit = {
    g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.4f))
    val radius: Int = (model.modelType.size * canvas.ppi / 2.0*1.1).toInt
    g.fillOval((model.loc.x * canvas.ppi).toInt - radius, (model.loc.y * canvas.ppi).toInt - radius, radius*2, radius*2)
  }
}

class MovementLine (override val model: Model, val loc: Point, val valid: Boolean, val origin: Option[Point] = None)
  extends ModelMouseover(model) {

  val originLoc: Point = origin.getOrElse(model.loc)

  override def paint(g: Graphics2D, canvas: Canvas): Unit = {
    val ppi = canvas.ppi
    if (valid)
      g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.4f))
    else
      g.setColor(new Color(1.0f, 0.5f, 0.5f, 0.4f))
    val radius = (model.modelType.size * ppi / 2.0).toInt
    g.fillOval((loc.x*ppi).toInt - radius, (loc.y*ppi).toInt - radius, radius*2, radius*2)
    g.drawLine((originLoc.x*ppi).toInt, (originLoc.y*ppi).toInt, (loc.x*ppi).toInt, (loc.y*ppi).toInt)
  }
}

class SquadMouseover (override val model: Model) extends ModelMouseover(model) {
  val subeffects: List[ModelMouseover] = model.squad.models.map(new ModelMouseover(_))

  override def paint(g: Graphics2D, canvas: Canvas): Unit = {
    subeffects.foreach(_.paint(g, canvas))
  }
}