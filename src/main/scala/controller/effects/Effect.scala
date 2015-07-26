package controller.effects

import java.awt.Graphics2D

import ss_test.Canvas
import wh.Model

import scala.swing.Color


sealed abstract class EffectClass
case object Selection extends EffectClass
case object Mouseover extends EffectClass

/**
 * Used to add graphical effects to objects. e.g. selection circles, etc.
 */
trait Effect {
  val effectClass: EffectClass
  def paint(g: Graphics2D, canvas: Canvas): Unit
}

abstract class ModelEffect(val mode: Model) extends Effect

class ModelSelection (val model: Model) extends ModelEffect(model) {
  override val effectClass = Selection

  override def paint(g: Graphics2D, canvas: Canvas): Unit = {
    g.setColor(new Color(255, 200, 200))
    val radius: Int = (model.modelType.size * canvas.ppi / 2.0 * 1.1).toInt
    g.drawOval((model.loc.x * canvas.ppi).toInt - radius, (model.loc.y * canvas.ppi).toInt - radius, radius*2, radius*2)
  }
}

class ModelMouseover (val model: Model) extends ModelEffect(model) {
  override val effectClass = Mouseover

  override def paint(g: Graphics2D, canvas: Canvas): Unit = {
    g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.4f))
    val radius: Int = (model.modelType.size * canvas.ppi / 2.0*1.1).toInt
    g.fillOval((model.loc.x * canvas.ppi).toInt - radius, (model.loc.y * canvas.ppi).toInt - radius, radius*2, radius*2)
  }
}